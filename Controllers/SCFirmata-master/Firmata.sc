Firmata2 {
	classvar
	<analogIOMessage = 0xE0,
	<digitalIOMessage = 0x90,
	<reportAnalogPin = 0xC0,
	<reportDigitalPort = 0xD0,
	<setPinMode = 0xF4,
	<protocolVersion = 0xF9,
	<systemReset = 0xFF,

	//sysex commands
	<sysexStart = 0xF0,
	<sysexEnd = 0xF7,
	<reservedCommand = 0x00, // 2nd SysEx data byte is a chip-specific command (AVR, PIC, TI, etc).
	<analogMappingQuery = 0x69, // ask for mapping of analog to pin numbers
	<analogMappingResponse = 0x6A, // reply with mapping info
	<capabilityQuery = 0x6B, // ask for supported modes and resolution of all pins
	<capabilityResponse = 0x6C, // reply with supported modes and resolution
	<pinStateQuery = 0x6D, // ask for a pin's current mode and value
	<pinStateResponse = 0x6E, // reply with a pin's current mode and value
	<extendedAnalog = 0x6F, // analog write (PWM, Servo, etc) to any pin
	<servoConfig = 0x70, // set max angle, minPulse, maxPulse, freq
	<stringData = 0x71, // a string message with 14-bits per char
	<shiftData = 0x75, // shiftOut config/data message (34 bits)
	<i2cRequest = 0x76, // I2C request messages from a host to an I/O board
	<i2cReply = 0x77, // I2C reply messages from an I/O board to a host
	<i2cConfig = 0x78, // Configure special I2C settings such as power pins and delay times
	<reportFirmware = 0x79, // report name and version of the firmware
	<samplingInterval = 0x7A, // sampling interval
	<sysexNonRealtime = 0x7E, // MIDI Reserved for non-realtime messages
	<sysexRealtime = 0x7F, // MIDI Reserved for realtime messages
	<parserError = -1;
	classvar <pinMode;
	classvar <pinDirection;

	*initClass{
		pinMode = (
			INPUT: 0,
			OUTPUT: 1,
			ANALOG: 2,
			PWM: 3,
			SERVO: 4,
			SHIFT: 5,
			I2C: 6
		);
		pinDirection = (
			IN: 0,
			OUT: 1
		)
	}
}
FirmataParser {

	var parseFunctions;
	var state;
	var device;
	var commandData, sysexData;
	var responseFunctions;

	*new{arg device;
		^super.new.init(device);
	}

	init{arg device_;
		device = device_;
		sysexData = Int8Array.new;//sysex can be any size
		commandData = Int8Array.new(32);//max number of data bytes for Firmata is 32 for non-sysex commands
		responseFunctions = IdentityDictionary.new;
		state = \waitingForCommand;
		parseFunctions = (
			waitingForCommand: {arg byte;
				case
				{byte.bitAnd(0xF0) == Firmata2.analogIOMessage} {
					state = \waitingForAnalogIOData; commandData.add(byte.bitXor(Firmata2.analogIOMessage)) }
				{byte.bitAnd(0xF0) == Firmata2.digitalIOMessage} {
					state = \waitingForDigitalIOData; commandData.add(byte.bitXor(Firmata2.digitalIOMessage)) }
				{byte == Firmata2.protocolVersion} { state = \waitingForProtocolVersionData; }
				{byte == Firmata2.sysexStart} { state = \waitingForSysexData; };
			},
			waitingForSysexData: {arg byte;
				if(byte != Firmata2.sysexEnd,
					{ sysexData = sysexData.add(byte);},
					{
						this.parseSysexCommand;
						sysexData = Int8Array.new;
						state = \waitingForCommand;
					}
				);
			},
			\waitingForProtocolVersionData: {arg byte;
				switch(commandData.size,
					0, { commandData.add(byte); },
					1, { "Protocol version: %.%".format(commandData[0], byte).postln; this.reset; }
				);
			},
			\waitingForAnalogIOData: {arg byte;
				switch(commandData.size,
					1, {commandData.add(byte);},
					2, {device.analogPinAction.value(commandData[0], this.parse14BitData([commandData[1], byte])[0]); this.reset;}
				);
			},
			\waitingForDigitalIOData: {arg byte;
				switch(commandData.size,
					1, { commandData.add(byte); },
					2, {device.digitalPortAction.value(commandData[0], this.parse14BitData([commandData[1], byte])[0]); this.reset;}
				);
			}
		);
	}

	//Functions are added to a FIFO
	addResponseFunction{arg key, func;
		var funcList;
		funcList = responseFunctions.atFail(key.asSymbol, {FunctionList.new.array_(Array.new);});
		funcList.array_(funcList.array.addFirst(func));
		responseFunctions.put(key.asSymbol, funcList);
	}

	doResponseFunction{arg key ...args;
		var funcList;
		funcList = responseFunctions.at(key.asSymbol);
		funcList !? {
			funcList.array.pop.value(*args);
		}
	}

	reset{
		commandData = Int8Array.new(32);//max number of data bytes for Firmata is 32 for non-sysex commands
		state = \waitingForCommand;
	}

	parseByte{arg byte;
		var nextState;
		parseFunctions.at(state).value(byte);
	}

	//data bytes are sent in two 7bit bytes for sysex, analog,
	parse14BitData{arg data;
		var result;
		data.pairsDo({arg lsb, msb;
			result = result.add(lsb.bitAnd(127).bitOr(msb << 7));
		});
		^result;
	}

	parseSysexCommand{
		switch(sysexData[0],
			Firmata2.reportFirmware, {
				var version, name;
				version = sysexData.at([1,2]);
				name = String.newFrom(this.parse14BitData(sysexData.copyRange(3, sysexData.size)).collect(_.asAscii));
				"Firmata protocol version: %.% Firmware: %".format(version[0], version[1], name).postln;
			},
			Firmata2.stringData, {
				var str = String.newFrom(this.parse14BitData(sysexData.copyRange(1, sysexData.size - 1)).collect(_.asAscii));
				"Sysex string received: %".format(str).postln;
			},
			Firmata2.capabilityResponse, {
				var capabilityData, temp, result;
				sysexData.drop(1).do({arg item;
					if(item == 127,//pin capability data is separated by 127
						{ capabilityData = capabilityData.add(temp); temp = nil;},
						{ temp = temp.add(item); }
					);
				});
				capabilityData = capabilityData.collect({arg item, i;
					var result = ();
					item.pairsDo({arg mode, resolution;
						result.put(Firmata2.pinMode.findKeyForValue(mode).asSymbol, resolution);
					});
					result;
				});
				device.prSetPinCapabilities(capabilityData);
			},
			Firmata2.pinStateResponse, {
				var pinStateData;
				pinStateData = [
					sysexData[1], //num
					Firmata2.pinMode.findKeyForValue(sysexData[2]), //mode
					sysexData[3..].collect({arg byte, i; byte.bitAnd(127) << (7 * i)}).sum //state
				];
				"Pin state: %".format(pinStateData).postln;
				this.doResponseFunction(\pinStateResponse, *pinStateData);
				device.pinStateResponseAction.value(*pinStateData);
			},
			{
				"Unknown sysex command received: %".format(this.parse14BitData(sysexData)).postln;
			}
		);
	}
}

FirmataDevice {
	var <port; //temp getter
	var parser;
	var listenRoutine;
	var parserState;
	var <>analogPinAction, <>digitalPortAction;
	var <>pinStateResponseAction;
	var <pinCapabilities;

	*new{arg portPath, baudrate = 57600;
		^super.new.init(portPath, baudrate);
	}
	init{arg portPath_, baudrate_;
		parser = FirmataParser.new(this);
		fork{
			port = SerialPort(portPath_, baudrate_, crtscts: true);
			//Wait for Arduino auto reset
			2.wait;
			this.start;
		}
	}

	start{
		listenRoutine = fork {
			loop {
				parser.parseByte(port.read);
			}
		};
	}

	end{ listenRoutine.stop; }
	close{ port.close; }

	setPinMode{arg pinNum, direction;
		var message;
		direction = Firmata2.pinMode.atFail(direction, {"Not a pin mode in Firmata: %".format(direction).error; ^this});
		message = Int8Array[Firmata2.setPinMode, pinNum, direction];
		port.putAll(message);
	}

	setDigitalPortMask{arg portNum, mask;
		var message;
		message = Int8Array[(Firmata2.digitalIOMessage + portNum), mask.bitAnd(127), mask.rightShift(7)];
		port.putAll(message);
	}

	reportAnalogPin{arg pinNum, bool;
		port.putAll(Int8Array[Firmata2.reportAnalogPin + pinNum, bool.asInteger]);
	}

	reportDigitalPort{arg portNum, bool;
		port.putAll(Int8Array[Firmata2.reportDigitalPort + portNum, bool.asInteger]);
	}

	serialize14BitData{arg data;
		^data.collect({arg item;
			[item.bitAnd(127), item << 7]
		}).flat;
	}

	sendRawString{arg str;
		var message = this.serialize14BitData(str.ascii).as(Int8Array);
		message = message.insert(0, Firmata2.stringData);
		this.prSendSysexData(message);
	}

	requestFirmware{ this.prSendSysexData(Firmata2.reportFirmware); }
	requestProtocolVersion{ port.put(Firmata2.protocolVersion); }
	doSystemReset{ port.put(Firmata2.systemReset); }
	queryCapability{ this.prSendSysexData(Firmata2.capabilityQuery); }

	queryPinState{arg pinNum, responseFunc;
		responseFunc !? { parser.addResponseFunction(\pinStateResponse, responseFunc); };
		this.prSendSysexData(Int8Array[Firmata2.pinStateQuery, pinNum]);
	}

	queryAllPinStates{arg responseFunc;
		if(pinCapabilities.isNil,
			{ "No capability data exists for device. Query capability data first.".warn; ^this },
			{ ^pinCapabilities.size.do({arg i;
				this.queryPinState(i, responseFunc.copy);
			}); }
		)
	}

	numberOfPins{
		if(pinCapabilities.isNil,
			{ "No capability data exists for device. Query capability data first.".warn; ^this },
			{ ^pinCapabilities.size; }
		)
	}

	prSendSysexData{arg data;
		var message;
		message = data.asArray;
		message = message.insert(message.size, Firmata2.sysexEnd);
		message = message.insert(0, Firmata2.sysexStart);
		port.putAll(message);
	}

	prSetPinCapabilities{arg data;
		pinCapabilities = data;
		this.changed(\pinCapabilities);
	}
}
