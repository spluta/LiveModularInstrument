Joystick_Mod {

	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>portIDs, <>paths, <>devices;

	*initClass {}

	*new {
		^super.new.init();
	}

	*setPaths {|pathsIn|

		HID.findAvailable;
		paths = List.newClear(0);

		devices = List.newClear(0);
		pathsIn.do{|path|
			var temp;

			try{temp = HID.openPath(path)};

			if(temp!=nil) {
				devices.add(temp);
				paths.add(path);
			};
		}
	}

	*start {
		var address, type;

		"start HID".postln;
		paths.postln;

		paths.do{arg path;
			HID.action = { |...msg|
				var temp;

				temp = msg[4];

				if (temp<12){address = "/HIDButton"++temp++"/"++path}
				{address = "/HIDSlider"++(temp-11)++"/"++path};


				MidiOscControl.respond(address.asSymbol, msg[0]);

				if(sendRequest){
					if (temp<12){type = \onOff}
					{type = \continuous};

					MidiOscControl.setController(address.asSymbol, type)
				};

				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject(address)
				});
			};
		}
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		controllerKey = controllerKey.asString;

		if(controllerKey.contains("HIDButton"),{
			function = {|val|
				{localControlObject.valueAction_(val)}.defer;
			};
		});

		if(controllerKey.contains("HIDSlider"),{
			if(localControlObject.isKindOf(QtEZSlider2D)){
				function = [
					{|val|
						localControlObject.activex_(val);
					},
					{|val|
						localControlObject.activey_(val);
					},
					{|val| "do nothing".postln}]
			}{
				function = {|val|
					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
				};
			}
		});
		^function
	}
}