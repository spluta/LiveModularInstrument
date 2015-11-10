MIDI_Mod {

	classvar responders;
	classvar <>sendRequest = false;
	classvar addr;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		MIDIIn.connectAll;
		responders = List.newClear[0];

		//slider
		responders.add(MIDIFunc.cc({|val, num, chan, src|
			MidiOscControl.respond("/cc/"++num.asString++"/"++chan.asString++"/", val/127);
			if(sendRequest, {
				MidiOscControl.setController("/cc/"++num.asString++"/"++chan.asString++"/", \continuous)
		})}, nil));

		responders.add(MIDIFunc.noteOn({|val, num, chan, src|
			MidiOscControl.respond("/noteOn/"++num.asString++"/"++chan.asString++"/", 1);
			if(sendRequest, {
				MidiOscControl.setController("/noteOn/"++num.asString++"/"++chan.asString++"/", \onOff)
		})}, nil));

		responders.add(MIDIFunc.noteOff({ |val, num, chan, src|
			MidiOscControl.respond("/noteOff/"++num.asString++"/"++chan.asString++"/", 0);
		}, nil));

	}

	*resetOSCAddr {}
	*setWCurrentSetup {}

	*getFunctionFromKey {arg module, controllerKey, object;
		var function, localControlObject, nothing, keyShort;
		//here you are. hope this works!
		localControlObject = object;

		#nothing, keyShort = controllerKey.asString.split;
		[nothing, keyShort].postln;
		switch(keyShort.asSymbol,
			'cc', {
				function = {|val| {localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer};
			},
			'noteOn', {
				function = {|val| {localControlObject.valueAction_(val)}.defer};
			}
		);

		^function
	}

	*getMainSwitchControls {arg serverName, controls;
		^nil
	}
}
