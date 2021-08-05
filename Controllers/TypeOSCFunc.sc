TypeOSCFunc_Mod {

	classvar <>responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddrs, ip;
	classvar <>speedLimiters;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		if(responders.size!=0,{responders.do{arg item; item.free}});

		responders = ();
		speedLimiters = ();
	}

	*addResponder {arg path;
		speedLimiters.put(path.asSymbol, SpeedLimit({|msg| MidiOscControl.respond(msg[0], msg[1])},0.02));
		responders.put(path.asSymbol,
			OSCFunc({ |msg|
				speedLimiters[path.asSymbol].value(msg);
		}, path.asSymbol));
	}

	*sendOSC {|oscMsg, val|
		Lemur_Mod.netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val);
			});
		}
	}

	*removeResponder {arg path;
		responders.removeAt(path.asSymbol);
		speedLimiters.removeAt(path.asSymbol);
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		^nil
	}
}

TypeOSCFuncObject {
	var <>mama, <>oscMsgs, <>location, <>text, <>function, <>viewNumBox, <>isXY, <>addZAction, <>zLocation, <>zFunction, oscMsgs, <>view, <>textField, <>numberBox, label, oscMsg, oscMsg_Z, oscFunc, typeAssignButton, functions, <>zAction, <>frozen = false, speedLimit, speedLimitSend;

	*new {arg mama, oscMsgs, location, text, function, viewNumBox=true, isXY=false, addZAction=false, zLocation, zFunction;
		^super.new.mama_(mama).oscMsgs_(oscMsgs).location_(location).text_(text).function_(function).viewNumBox_(viewNumBox).isXY_(isXY).addZAction_(addZAction).zLocation_(zLocation).zFunction_(zFunction).init;
	}

	init {

		zAction = {};

		oscMsg = nil;
		label = StaticText().font_(Font("Helvetica", 10)).string_(text);
		numberBox = NumberBox().maxHeight_(15).maxDecimals_(2).font_(Font("Helvetica", 10)).maxWidth_(50).action_{|val|
			"numberBox".postln;
			function.value(val.value);
		};
		textField = TextField().font_(Font("Helvetica", 10)).maxHeight_(15)
		.action_{arg field;
			if(oscMsg!=nil,{
				MidiOscControl.clearController(mama.group.server, oscMsg);
				if(oscMsg_Z!=nil){
					MidiOscControl.clearController(mama.group.server, oscMsg_Z);
				};
			});

			oscMsgs.put(location, field.value.asString);
			oscMsg = field.value.asString;
			functions = List[function];

			speedLimit = SpeedLimit({|val| {numberBox.value_(val)}.defer}, 0.1);
			speedLimitSend = SpeedLimit({|val| TypeOSCFunc_Mod.sendOSC(oscMsgs[location], val)}, 0.1);

			if(viewNumBox,{
				functions.add({arg val; speedLimit.value(val)});
				functions.add({arg val; speedLimitSend.value(val)});
			});

			MidiOscControl.setControllerNoGui(oscMsg, functions, mama.group.server);

			if(addZAction,{this.makeZAction(zLocation, oscMsg.copyRange(0, oscMsg.size-3)++"/z", zFunction)});
		};
		typeAssignButton = mama.addTypeOSCAssignButton(location);

		view = CompositeView();
		if(viewNumBox,{
			view.layout_(HLayout(label, textField, numberBox, typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		},{
		view.layout_(HLayout(label, textField, typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		});
	}

	makeZAction {|location, msg, function|
		oscMsgs.put(location, msg);
		MidiOscControl.setControllerNoGui(msg, List[function], mama.group.server);
		oscMsg_Z = msg;
	}

	asView {^view}

	value {
		^[textField.value, numberBox.value]
	}

	valueAction_ { arg val;
		val.postln;
		if(val.size==2)
		{
			textField.valueAction_(val[0]);
			numberBox.valueAction_(val[1]);
		}{textField.valueAction_(val)};
	}

	setExternal_ { arg val;
		numberBox.value_(val);
	}
}