SerialPort.devices
a = FirmataDevice('/dev/tty.usbmodem1461');

//Print protoclol version and firmware (i.e. sketch in Arduino terms) is running on the device
a.requestFirmware;
a.requestProtocolVersion;
a.doSystemReset;

//Test with firmware: StandardFirmata.ino
a.setPinMode(13, \OUTPUT);

//set pin 13
a.setDigitalPortMask(1, 2r0100000);//on
a.setDigitalPortMask(1, 2r0000000);//off

//start listening to analog in
a.reportAnalogPin(0, true);
a.analogPinAction_({arg num, val;
	[num, val].postln;
})
a.reportAnalogPin(0, false);

//start listening to a digital port
a.setPinMode(11, \INPUT);
a.reportDigitalPort(1, true);
a.digitalPortAction_({arg portNum, mask;
	"Digital port % : %".format(portNum, mask.asBinaryString).postln;
});
a.reportDigitalPort(1, false);

//Updates info about the number of pins available on this device
a.queryCapability;
//Device pin capability data should ow shows how many port and what kind of
//function they can have, and the resolution.
//This data won't be available if the client sketch haven't implemented responses
//capability queries, i.e. this is a part of the protocol, not always the implmetations.
a.pinCapabilities;
a.numberOfPins;

//query pin states
a.queryPinState(0);
a.queryPinState(1);
//can be used to verify messages to pins:
a.setPinMode(13, \INPUT);
a.queryPinState(13, nil);
a.setPinMode(13, \OUTPUT);
a.queryPinState(13);
a.setDigitalPortMask(1, 2r0100000);//on
a.queryPinState(13);
a.setDigitalPortMask(1, 2r0000000);//off
a.queryPinState(13);

//Define a pin state response function to be evaluated when a response from
//the device is received:
a.pinStateResponseAction = {arg pinNum, mode, state; "Got this pin state data: %".format([pinNum, mode, state]).postln; };

//You can also issue a callback function for this specific query.
//It works in the same way as an asynchronous /c_get call to scsynth
//This will be evaluated in addition to the pinStateResponseAction
a.queryPinState(0, {arg ...args; "My 0th pins state is: %".format(args).postln;});

//These callbacks are queued and will be evaluated in the order they were issued.
//This is not an optimal solution, as some queries might not return a response in the
//expected format, i.e. it may return an error or string value response(?)
//There is also a good chance that issuing to many request from the computer at the same time will
//cause the Firmata device's internal serial buffer to overflow, thus losing many of the incoming queries
//which in turn won't be generate responses, for which the one-shot reponses will not be evaluated.
//I have yet to come up with a decent solution for this at this point, so any hints and tips are
//most welcome.
(
Routine({
	20.do {arg i;
		a.queryPinState(1, {arg ...args; "Request callback no. % : %".format(i, args).postln;});
	}
}).play;
)

//Test using firmware: EchoString.ino
a.sendRawString("Kjempebra!");//Replies with the same string