TypeOSCFunc_Mod {

	classvar <>responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddrs, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		if(responders.size!=0,{responders.do{arg item; item.free}});

		responders = ();
	}

	*addResponder {arg path;
		responders.put(path.asSymbol, OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]) }, path.asSymbol));
	}

	*addXYResponder {arg path;
		responders.put(path.asSymbol, OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2]]) }, path.asSymbol));
	}

	*sendOSC {|oscMsg, val|
		//this really should not be hard coded
		Lemur_Mod.netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val);
			});
		}
	}

	*sendOSCxy {|oscMsg, val|
		Lemur_Mod.netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val[1], val[0]);  //x and y are reversed for TouchOSC
			});
		}
	}

	*removeResponder {arg path;
		responders.removeAt(path.asSymbol);
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		^nil
	}

	//
	// *getFunctionFromKey {arg module, controllerKey, object;
	// 	var nothing, keyShort, localControlObject, function;
	//
	// 	localControlObject = object;
	//
	// 	#nothing, keyShort = controllerKey.asString.split;
	// 	if(keyShort.beginsWith("Button"),{
	// 		function = {|val| {localControlObject.valueAction_(val)}.defer};
	// 	});
	//
	// 	if(keyShort.beginsWith("PadButton"),{
	// 		function = {|val|
	// 			if(val == 1,{
	// 				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer
	// 			})
	// 		};
	// 	});
	//
	// 	if(keyShort.beginsWith("Switches"),{
	// 		function = {|val|
	// 		{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer};
	// 	});
	// 	if(keyShort.beginsWith("Fader"),{
	// 		function =  {|xyz, val|
	// 			switch(xyz.asSymbol,
	// 				'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
	// 				'z',{localControlObject.zAction.value(val)}
	// 			)
	// 		};
	// 	});
	// 	if(keyShort.beginsWith("MultiBall"),{
	// 		function = {|xyz, val|
	// 			switch(xyz.asSymbol,
	// 				'x', {{localControlObject.activex_(val)}.defer},
	// 				'y', {{localControlObject.activey_(val)}.defer},
	// 				'z',{localControlObject.zAction.value(val)}
	// 			)
	// 		};
	// 	});
	// 	if(keyShort.beginsWith("Range"),{
	// 		function = {|xyz, val|
	// 			switch(xyz.asSymbol,
	// 				'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
	// 				'z',{localControlObject.zAction.value(val)}
	// 			)
	// 		};
	// 		}
	// 	);
	// 	^function
	// }

}

TypeOSCFuncObject {
	var <>mama, <>oscMsgs, <>location, <>text, <>function, <>viewNumBox, <>isXY, <>addZAction, <>zLocation, <>zFunction, oscMsgs, <>view, <>textField, <>numberBoxes, label, oscMsg, oscMsg_Z, oscFunc, typeAssignButton, functions, <>zAction;

	*new {arg mama, oscMsgs, location, text, function, viewNumBox=true, isXY=false, addZAction=false, zLocation, zFunction;
		^super.new.mama_(mama).oscMsgs_(oscMsgs).location_(location).text_(text).function_(function).viewNumBox_(viewNumBox).isXY_(isXY).addZAction_(addZAction).zLocation_(zLocation).zFunction_(zFunction).init;
	}

	init {
		[mama, oscMsgs, location, text, function].postln;

		zAction = {};

		functions = List[function];

		oscMsg = nil;
		label = StaticText().font_(Font("Helvetica", 10)).string_(text);
		if(isXY==true, {
			numberBoxes = List.fill(2, {NumberBox().maxHeight_(15).maxDecimals_(2).font_(Font("Helvetica", 10)).maxWidth_(50)});
		},{
			numberBoxes = [NumberBox().maxHeight_(15).maxDecimals_(2).font_(Font("Helvetica", 10)).maxWidth_(50)];
		});
		textField = TextField().font_(Font("Helvetica", 10)).maxHeight_(15)
		.action_{arg field;
			"do the action".postln;
			if(oscMsg!=nil,{
				MidiOscControl.clearController(mama.group.server, oscMsg);
				if(oscMsg_Z!=nil){
					MidiOscControl.clearController(mama.group.server, oscMsg_Z);
				};
				//TypeOSCFunc_Mod.removeResponder(oscMsg);
			});
			if(isXY==true, {
				"add XY responder".postln;
				//TypeOSCFunc_Mod.addXYResponder(field.value);
			},{
				//TypeOSCFunc_Mod.addResponder(field.value);
			});
			oscMsgs.put(location, field.value.asString);
			oscMsgs.postln;
			oscMsg = field.value.asString;
			functions = List[function];

			if(viewNumBox,{
				if(numberBoxes.size<2, {
					functions.add({arg val; {numberBoxes[0].value_(val)}.defer});
					functions.add({arg val; TypeOSCFunc_Mod.sendOSC(oscMsgs[location], val)});
				},{
					functions.add({arg val; val.do{arg item,i; {numberBoxes[i].value_(item)}.defer}});
					functions.add({arg val; TypeOSCFunc_Mod.sendOSCxy(oscMsgs[location], val)});
				});
			});

			functions.postln;

			MidiOscControl.setControllerNoGui(oscMsg, functions, mama.group.server);

			if(addZAction,{this.makeZAction(zLocation, oscMsg.copyRange(0, oscMsg.size-3)++"/z", zFunction)});
		};
		typeAssignButton = mama.addTypeOSCAssignButton(location);

		view = CompositeView();
		if(viewNumBox,{
			view.layout_(HLayout(label, textField, HLayout(*numberBoxes), typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		},{
		view.layout_(HLayout(label, textField, typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		});
	}

	makeZAction {|location, msg, function|
		"makeZAction ".post; [location, msg, function].postln;
		oscMsgs.put(location, msg);
		MidiOscControl.setControllerNoGui(msg, List[function], mama.group.server);
		oscMsg_Z = msg;
	}

	asView {^view}

	value {
		^textField.value
	}

	valueAction_ { arg val;
		textField.valueAction_(val);
	}
}