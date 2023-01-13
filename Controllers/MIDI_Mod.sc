MIDI_Mod {

	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar addr;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		responders.do{arg item; item.free};

		MIDIIn.connectAll;

		responders = List.newClear[0];

		responders.add(MIDIFunc.cc({|val, num, chan, src|
			var address = "/cc/"++num.asString++"/"++chan.asString++"/";

			MidiOscControl.respond(address, val/127);
			if(sendRequest, {
				MidiOscControl.setController(address, \continuous);
				MidiOscControl.setController(address, \onOff)
		});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)
			});

		}, nil));

		responders.add(MIDIFunc.noteOn({|val, num, chan, src|

			var address = "/note/"++num.asString++"/"++chan.asString++"/";

			MidiOscControl.respond(address, 1);

			if(sendRequest, {
				MidiOscControl.setController(address, \onOff);
				/*MidiOscControl.setController("/noteOff/"++num.asString++"/"++chan.asString++"/", \onOff)*/
		});

			//need to fix noteOn/noteOff to work
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address);

			});

		}, nil));

		responders.add(MIDIFunc.noteOff({ |val, num, chan, src|
			var address = "/note/"++num.asString++"/"++chan.asString++"/";
			MidiOscControl.respond(address, 0);
		}, nil));

	}

	*resetOSCAddr {}
	*setWCurrentSetup {}

	*getFunctionFromKey {arg module, controllerKey, object;
		var function, localControlObject, nothing, keyShort;

		//here you are. hope this works!
		localControlObject = object;

		#nothing, keyShort = controllerKey.asString.split;
		switch(keyShort.asSymbol,
			'cc', {
				//button does not have a controlSpec
				if(localControlObject.respondsTo('controlSpec'),{
					function = {|val| {localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer};
				},{
					function = {|val| {localControlObject.valueAction_(val.round)}.defer};
				});
			},
			'note', {
				function = {|val|
					{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer};
			}
		);

		^function
	}

	*getMainSwitchControls {arg serverName, controls;
		^nil
	}
}
