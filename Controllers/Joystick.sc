Joystick_Mod {

	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>portIDs, <>paths, <>devices, <>running = true;
	classvar <>joystick0, <>joystick1, <>buttons;
	classvar temp;
	classvar address, type, <>swProTransform;

	*initClass {
		swProTransform = Dictionary.newFrom(List[
			\x1, {|val| val.linlin(0.1, 0.87, 0, 1)},
			\y1, {|val| val.linlin(0.129, 0.9, 0, 1)},
			\x2, {|val| val.linlin(0.1, 0.87, 0, 1)},
			\y2, {|val| val.linlin(0.15, 0.904, 0, 1)},]);
		/*swProDataTransform = Dictionary.newFrom(List[\48, {arg val; val.linl, \49, 2, \51, 3, \52, 4]);*/
	}

	*new {
		^super.new.init();
	}

	*setPaths {|pathsIn|
		var nintCount=0, logiCount=0;

		devices = List.newClear(0);
		pathsIn.do{arg path;
			var localPath = path.copyRange(0,3);

			switch(localPath,
				"nint", {
					devices.add(MKtl(localPath++nintCount.asString, path, multiIndex:nintCount));
					nintCount = nintCount+1;
				},
				"logi", {
					devices.add(MKtl(localPath++logiCount.asString, path, multiIndex:logiCount));
					logiCount = logiCount+1;
				},{
					devices.add(MKtl(localPath, path))
				}
			)
		};
	}

	*callExtreme3D {|msg, path|

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
	}

	*nintendoRespondNCheck {|address, num, val|

		MidiOscControl.respond(address.asSymbol, val);
		if (num<13){
			if(sendRequest){
				type = \onOff;
				MidiOscControl.setController(address.asSymbol, type);
			};
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)
			});
		};
	}

	*logiRespondNCheck {|address, val|

		MidiOscControl.respond(address.asSymbol, val);
		if(sendRequest){
			type = \onOff;
			MidiOscControl.setController(address.asSymbol, type);
		};
		if(sendTypeRequest,{
			MidiOscControl.setInstantTypeObject(address)
		});
	}

	*start {


		devices.do{|item|
			if(item.name.asString.containsStringAt(0,"nint")){

				(1..2).do{|num|
					["x","y"].do{|item2|
						item2 = (item2++num).asSymbol;
						item.elAt(\joy, item2).action={|el|
							address = "/"++item.name.asString++"/joy/"++item2.asString;
							this.nintendoRespondNCheck(address, 48, swProTransform[item2].value(el.value));
						}
					}
				};

				8.do{arg i;

					item.elAt(\bt, (i+1).asSymbol).action={|el|
						address = "/"++item.name.asString++"/bt/"++(i+1);
						this.nintendoRespondNCheck(address, i+1, el.value);
				}};

				item.elAt(\arrows).action={|el|
					address = "/"++item.name.asString++"/arrows/";
					this.nintendoRespondNCheck(address, 9, el.value);
				};
			};

			if(item.name.asString.containsStringAt(0,"logi")){
				var path;

				['x','y','z'].do{|item2|

					item.elAt(\joy, item2).action={|el|
						address = "/"++item.name.asString++"/joy/"++item2.asString;
						this.logiRespondNCheck(address, el.value);
					}
				};

				item.elAt(\slider).action={|el|
					address = "/"++item.name.asString++"/slider";
					this.logiRespondNCheck(address, el.value);
				};

				12.do{arg i;
					item.elAt(\bt, (i+1).asSymbol).action={|el|
						address = "/"++item.name.asString++"/bt/"++i.asString;
						this.logiRespondNCheck(address, el.value);
				}};
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
