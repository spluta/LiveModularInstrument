Manta_Mod {

	classvar responders;
	classvar <>sendRequest;
	classvar addr;

	*initClass {
		sendRequest = false
	}

	*new {
		^super.new.init;
	}

	*start {

		responders = List.newClear[0];

		//slider
		responders.add(OSCFunc({|msg|
			MidiOscControl.respond(msg[0]++"/"++msg[1].asString, msg[3]);  //always send the message
			if(sendRequest, {
				MidiOscControl.setController("/manta/slider/"++msg[1].asString, \continuous)
		})}, '/manta/slider'));

		responders.add(OSCFunc({|msg|
			MidiOscControl.respond(msg[0]++"/"++msg[1].asString, msg[3]);
			if(sendRequest, {
				MidiOscControl.setController("/manta/value/"++msg[1].asString, \continuous)
		})}, '/manta/value'));

		responders.add(OSCFunc({|msg|
			MidiOscControl.respond(msg[0]++"/"++msg[1].asString, msg[3]);
			if(sendRequest, {
				MidiOscControl.setController("/manta/noteOn/"++msg[1].asString, \onOff);
				//MidiOscControl.setController("/manta/noteOff/"++msg[1].asString, \onOff);
		})}, '/manta/noteOn'));

		responders.add(OSCFunc({ |msg|
			MidiOscControl.respond(msg[0]++"/"++msg[1].asString, msg[3]);
		}, '/manta/noteOff'));
	}

	*resetOSCAddr {}
	*setWCurrentSetup {}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, nothing1, nothing2, keyShort, localControlObject, function;

		localControlObject = object;
		#nothing, nothing1, keyShort = controllerKey.asString.split;
		[nothing, nothing1, keyShort].postln;
		switch(keyShort.asSymbol,
			'noteOn',{
				function = {|val| {localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer};
			},
			'value',{

					function = {|val| {localControlObject.valueAction_(localControlObject.controlSpec.map(val/180))}.defer};
			},
			'slider',{
						function = {|val| {localControlObject.valueAction_(localControlObject.controlSpec.map(val/4096))}.defer};
		});
		^function
	}

	*getMainSwitchControls {arg serverName, controls;
		var functionsKeys, function, controllerKey;

		functionsKeys = List.newClear(0);

		4.do{|i|
			function = {|val| {controls[i].valueAction_(controls[i].value+1)}.defer};
			controllerKey = "/manta/noteOn"++"/"++(49+i).asString;
			functionsKeys.add([function, controllerKey]);
		};

		^functionsKeys
	}
}
