TypeOSCFunc_Mod {

	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddr, ip;

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

	*removeResponder {arg path;
		responders.removeAt(path.asSymbol);
	}

	*setWCurrentSetup {arg serverName, oscMsg;
		var object, setting;
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		#nothing, keyShort = controllerKey.asString.split;
		if(keyShort.beginsWith("Button"),{
			function = {|val| {localControlObject.valueAction_(val)}.defer};
		});

		if(keyShort.beginsWith("PadButton"),{
			function = {|val|
				if(val == 1,{
				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer
				})
			};
		});

		if(keyShort.beginsWith("Switches"),{
			function = {|val|
				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer};
		});
		if(keyShort.beginsWith("Fader"),{
			function =  {|xyz, val|
				switch(xyz.asSymbol,
					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
		});
		if(keyShort.beginsWith("MultiBall"),{
			function = {|xyz, val|
				switch(xyz.asSymbol,
					'x', {{localControlObject.activex_(val)}.defer},
					'y', {{localControlObject.activey_(val)}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
		});
		if(keyShort.beginsWith("Range"),{
			function = {|xyz, val|
				switch(xyz.asSymbol,
					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
			}
		);
		^function
	}

}

TypeOSCFuncObject {
	var <>mama, <>oscMsgs, <>location, <>text, <>function, <>viewNumBox, oscMsgs, <>view, <>textField, <>numberBox, label, oscMsg, oscFunc, typeAssignButton, functions;

	*new {arg mama, oscMsgs, location, text, function, viewNumBox=true;
		^super.new.mama_(mama).oscMsgs_(oscMsgs).location_(location).text_(text).function_(function).viewNumBox_(viewNumBox).init;
	}

	init {
		[mama, oscMsgs, location, text, function].postln;

		functions = List[function];

		oscMsg = nil;
		label = StaticText().font_(Font("Helvetica", 10)).string_(text);
		numberBox = NumberBox().maxHeight_(15).maxDecimals_(2).font_(Font("Helvetica", 10)).maxWidth_(50);
		textField = TextField().font_(Font("Helvetica", 10)).maxHeight_(15)
		.action_{arg field;
			"do the action".postln;
			if(oscMsg!=nil,{
				MidiOscControl.clearController(oscMsg);
				TypeOSCFunc_Mod.removeResponder(oscMsg);
			});
			TypeOSCFunc_Mod.addResponder(field.value);
			oscMsgs.put(location, field.value.asString);
			oscMsgs.postln;
			oscMsg = field.value.asString;
			functions = List[function];
			if(viewNumBox,{functions.add({arg val; {numberBox.value_(val)}.defer})});

			functions.postln;

			MidiOscControl.setControllerNoGui(oscMsg, functions, mama.group.server);
		};
		typeAssignButton = mama.addTypeOSCAssignButton(location);

		view = CompositeView();
		if(viewNumBox,{
			view.layout_(HLayout(label, textField, numberBox, typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		},{
		view.layout_(HLayout(label, textField, typeAssignButton.layout).spacing_(0).margins_([0,0,0,0])).maxHeight_(15);
		});
	}

	value {
		^textField.value
	}

	valueAction_ { arg val;
		textField.valueAction_(val);
	}
}