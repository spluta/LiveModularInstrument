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
		//need to take out z action
		oscMsg = oscMsg.asString;
		if(oscMsg.last!=($z)){
			OSCReceiver_Mod.sendOSCTypeOSC(oscMsg.copyRange(0,oscMsg.size-3), val);
		};
		//Lemur_Mod.sendOSC(oscMsg, val)
	}

	*removeResponder {arg path;
		responders.removeAt(path.asSymbol);
		speedLimiters.removeAt(path.asSymbol);
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		^nil
	}
}

MyTextView : TextView {
	valueAction_ {arg string;
		this.string = string;
	}

	value {
		^this.string;
	}
}

TypeOSCFuncObject {
	var <>mama, <>oscMsgs, <>location, <>text, <>function, oscMsgs, <>view, <>textField, <>numberBox, label, oscMsg, oscFunc, typeAssignButton, functions, <>frozen = false, speedLimit, speedLimitSend;

	*new {arg mama, oscMsgs, location, text, function;
		^super.new.mama_(mama).oscMsgs_(oscMsgs).location_(location).text_(text).function_(function).init;
	}

	init {

		oscMsg = nil;
		label = StaticText().font_(Font("Helvetica", 10)).string_(text);
		numberBox = NumberBox().maxHeight_(15).maxDecimals_(2).font_(Font("Helvetica", 10)).maxWidth_(50).action_{|val|
			function.value(val.value);
		};
		textField = TextField().font_(Font("Helvetica", 10)).maxHeight_(15)
		.action_{arg field;
			if(oscMsg!=nil,{
				MidiOscControl.clearController(mama.group.server, oscMsg);
			});

			oscMsgs.put(location, field.value.asString);
			oscMsg = field.value.asString;
			functions = List[function];

			speedLimit = SpeedLimit({|val| {numberBox.value_(val)}.defer}, 0.1);
			speedLimitSend = SpeedLimit({|val|
				TypeOSCFunc_Mod.sendOSC(oscMsgs[location], val)}, 0.1);

			functions.add({|val| speedLimit.value(val)});
			functions.add({|val| speedLimitSend.value(val)});

			MidiOscControl.setControllerNoGui(oscMsg, functions, mama.group.server);
		};
		typeAssignButton = mama.addTypeOSCAssignButton(location);

		view = CompositeView();
		view.layout_(HLayout(label, textField, numberBox, typeAssignButton).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);

	}

	asView {^view}

	value {
		^[textField.value, numberBox.value]
	}

	valueAction_ { arg val;
		if(val.size==2)
		{
			textField.valueAction_(val[0].asString);
			numberBox.valueAction_(val[1].asFloat);
		}{textField.valueAction_(val.asString)};
	}

	setExternal_ { arg val;
		numberBox.value_(val);
	}
}