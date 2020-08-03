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
					"adding: ".post; path.postln;
				},
				"logi", {
					devices.add(MKtl(localPath++logiCount.asString, path, multiIndex:logiCount));
					logiCount = logiCount+1;
					"adding: ".post; path.postln;
				},{
					"adding: ".post; path.postln;
					devices.add(MKtl(localPath, path))
				}
			)
		};
	}

	*callExtreme3D {|msg, path|

		[msg, path].postln;

		temp = msg[4];

		if (temp<12){address = "/HIDButton"++temp++"/"++path}
		{address = "/HIDSlider"++(temp-11)++"/"++path};

		[address.asSymbol, msg[0]].postln;

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
		//[address, num, val].postln;

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
		//[address, val].postln;

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
				"Nintendo".postln;

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
				"set logi".postln;


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



// Joystick_Mod {
//
// 	classvar <>sendServerSwitcherRequest = false;
// 	classvar responders;
// 	classvar <>sendRequest = false, <>sendTypeRequest = false;
// 	classvar <>portIDs, <>paths, <>devices, <>running = true;
// 	classvar <>joystick0, <>joystick1, <>buttons;
// 	classvar temp;
// 	classvar address, type, <>swProTransform;
//
// 	*initClass {
// 		joystick0 = List[List.fill(8, 0), List.fill(8, 0), List.fill(16, 0)];
// 		joystick1 = List[List.fill(8, 0), List.fill(8, 0), List.fill(16, 0)];
// 		buttons = List.fill(8, 0);
// 		swProTransform = Dictionary.newFrom(List[
// 			\1201, {|val| val.linlin(0.107, 0.802, 0, 1)},
// 			\1202, {|val| val.linlin(0.192, 0.9025, 1, 0)},
// 			\1203, {|val| val.linlin(0.098, 0.802, 0, 1)},
// 		\1204, {|val| val.linlin(0.215, 0.918, 1, 0)},]);
// 		/*swProDataTransform = Dictionary.newFrom(List[\48, {arg val; val.linl, \49, 2, \51, 3, \52, 4]);*/
// 	}
//
// 	*new {
// 		^super.new.init();
// 	}
//
// 	*setPaths {|pathsIn|
//
// 		//HID.closeAll;
// 		HID.findAvailable;
//
// 		paths = List.newClear(0);
//
// 		devices = List.newClear(0);
// 		paths = pathsIn;
//
// 		"start HID".postln;
// 		paths.postln;
//
// 		paths.do{arg path;
// 			"trying start ".post;path.postln;
// 			try{HID.openPath(path)};
//
// 		};
// 		"open".postln;
//
// 	}
//
// 	*callExtreme3D {|msg, path|
//
// 		[msg, path].postln;
//
// 		temp = msg[4];
//
// 		if (temp<12){address = "/HIDButton"++temp++"/"++path}
// 		{address = "/HIDSlider"++(temp-11)++"/"++path};
//
// 		[address.asSymbol, msg[0]].postln;
//
// 		MidiOscControl.respond(address.asSymbol, msg[0]);
//
// 		if(sendRequest){
// 			if (temp<12){type = \onOff}
// 			{type = \continuous};
//
// 			MidiOscControl.setController(address.asSymbol, type)
// 		};
//
// 		if(sendTypeRequest,{
// 			MidiOscControl.setInstantTypeObject(address)
// 		});
// 	}
//
// 	*nintendoRespondNCheck {|address, num, val|
// 		//[address, num, val].postln;
//
// 		MidiOscControl.respond(address.asSymbol, val);
// 		if (num<13){
// 			if(sendRequest){
// 				type = \onOff;
// 				MidiOscControl.setController(address.asSymbol, type);
// 			};
// 			if(sendTypeRequest,{
// 				MidiOscControl.setInstantTypeObject(address)
// 			});
// 		};
// 	}
//
// 	*logiRespondNCheck {|address, num, val|
// 		//[address, num, val].postln;
//
// 		MidiOscControl.respond(address.asSymbol, val);
// 		if(sendRequest){
// 			type = \onOff;
// 			MidiOscControl.setController(address.asSymbol, type);
// 		};
// 		if(sendTypeRequest,{
// 			MidiOscControl.setInstantTypeObject(address)
// 		});
// 	}
//
// 	*start {
//
// 		HID.openDevices.do{|item, i| item.info.postln; i.postln};
// 		devices = HID.openDevices;
//
// 		devices.do{|item|
//
//
// 			if(item.info.productName=="Pro Controller"){
// 				var path;
// 				"Nintendo".postln;
// 				path = "Nintendo";
// 				8.do{arg i;
// 					item.elements[i].action = {|msg|
// 						address = "/HIDButton"++(i+1)++"/"++path;
// 						this.nintendoRespondNCheck(address, i+1, msg);
// 				}};
// 				item.elements[1200].action = {|msg|
// 					var val;
// 					val = msg*7/8;
// 					address = "/HIDButton9/"++path;
// 					this.nintendoRespondNCheck(address, 1200, val);
// 				};
// 				(1201..1204).do{|item2, i2|
// 					item.elements[item2].action = {|msg|
// 						address = "/HIDSlider"++(i2+1)++"/"++path;
// 						this.nintendoRespondNCheck(address, item2, swProTransform[item2.asSymbol].value(msg));
// 					};
// 				};
// 			};
//
//
// 			k = MKtl('logi', "logitech-extreme-3d-pro");
//
// 			k.elAt(\joy, \z).action={|el| el.value.postln};
//
// 			if(item.info.productName=="Logitech Extreme 3D"){
// 				var path;
// 				"Extreme".postln;
// 				path = "Extreme";
// 				12.do{arg i;
// 					item.elements[i].action = {|msg|
// 						address = "/HIDButton"++(i+1)++"/"++path;
// 						this.logiRespondNCheck(address, i+1, msg);
// 				}};
// 				(12..15).do{arg item2, i2;
// 					item.elements[item2].action = {|msg|
// 						address = "/HIDSlider"++(i2+1)++"/"++path;
// 						this.logiRespondNCheck(address, item2+1, msg);
// 				}};
// 			};
// 		}
// 	}
//
// 	*getFunctionFromKey {arg module, controllerKey, object;
// 		var nothing, keyShort, localControlObject, function;
//
// 		localControlObject = object;
//
// 		controllerKey = controllerKey.asString;
//
// 		if(controllerKey.contains("HIDButton"),{
// 			function = {|val|
// 				{localControlObject.valueAction_(val)}.defer;
// 			};
// 		});
//
// 		if(controllerKey.contains("HIDSlider"),{
// 			if(localControlObject.isKindOf(QtEZSlider2D)){
// 				function = [
// 					{|val|
// 						localControlObject.activex_(val);
// 					},
// 					{|val|
// 						localControlObject.activey_(val);
// 					},
// 				{|val| "do nothing".postln}]
// 			}{
// 				function = {|val|
// 					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
// 				};
// 			}
// 		});
// 		^function
// 	}
// }