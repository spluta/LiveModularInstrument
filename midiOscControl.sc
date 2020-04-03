MidiOscControl {
	classvar instantRequestModule, <>instantControlObject, instantServer, instantTypeOfController;
	classvar <>actions;


	*new {
		^super.newCopyArgs().init;
	}

	init {
		actions = Dictionary.new;
	}

	*createActionDictionary {
		actions.add('global' -> Dictionary.new);
		ModularServers.servers.keys.do{arg item, i;
			actions.add(item.asSymbol -> Dictionary.new);
		};
	}

	*requestInstantAssign {arg module, controlObject, typeOfController, server;

		instantRequestModule = module;
		instantControlObject = controlObject;
		instantServer = server;
		instantTypeOfController = typeOfController;


		//this sets all controllers to look for moving sliders/buttons

		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest=true;
		};
	}

	*setInstantTypeObject {arg val;
		{instantControlObject.string_(val)}.defer;
	}

	*requestInstantTypeAssign {arg controlObject, server;

		instantControlObject = controlObject;
		instantServer = server;

		//this sets all controllers to look for moving sliders/buttons

		//this should probably be all OSC controllers
		LiveModularInstrument.controllers.do{arg item;
			item.sendTypeRequest=true;
		};
	}

	*clearInstantAssign {
		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest_(false);
			item.sendTypeRequest_(false);
		};
	}

	*getFunctionNSetController {arg module, controlObject, controllerKey, server;
		var function, controlObjectLocal, counter;

		controlObjectLocal = controlObject;
		//get the function
		counter=0;
		while({(counter<LiveModularInstrument.controllers.size)&&(function==nil)},
			{
				function = LiveModularInstrument.controllers[counter].getFunctionFromKey(module, controllerKey, controlObjectLocal);
				counter = counter+1;
			}
		);

		//add the function to the Dictionary
		if(function!=nil,{
			//"function size: ".post; function.size.postln;
			switch(function.size,
				0,{
					this.setFunction(module, controllerKey, function, server);
				},
				1, {
					this.setFunction(module, controllerKey, function, server);
				},
				2, {
					controllerKey = controllerKey.asString;
					controllerKey = controllerKey.copyRange(0, controllerKey.size-2);
					this.setFunction(module, controllerKey++"x", function[0], server);
					this.setFunction(module, controllerKey++"z", function[1], server);
				},
				3, {
					controllerKey = controllerKey.asString;
					controllerKey = controllerKey.copyRange(0, controllerKey.size-2);
					if(controllerKey.contains("Ball")){//Lemur Multiball should go here
						this.setFunction(module, controllerKey++"x", function[0], server);
						this.setFunction(module, controllerKey++"y", function[1], server);
						this.setFunction(module, controllerKey++"z", function[2], server, false);
					}/*{
						if((controllerKey.contains("Slider1"))||(controllerKey.contains("Slider1"))){//Joystick only
							//I really don't understand why this doesn't work
							//function.postln;
							this.setFunction(module, controllerKey.replace("Slider2", "Slider1"), function[0].deepCopy, server);
							this.setFunction(module, controllerKey.replace("Slider1", "Slider2"), function[1].deepCopy, server);
						}
					}*/
			});
		});
	}

	*setFunction {|module, controllerKey, function, server, setMsg=true|
		[module, controllerKey, function, server].postln;

		if(actions[server.asSymbol][controllerKey.asSymbol]==nil,{
			//"set it".postln;
			actions[server.asSymbol].add(controllerKey.asSymbol->function);
			//actions[server.asSymbol].keys.do{|item| item.postln};
		});
		if(setMsg,{module.setOscMsg(controllerKey.asSymbol)});
	}

	*setControllerNoGui {arg key, functions, server;

		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest=false;
		};

		actions[server.asSymbol].add(key.asSymbol->functions);
	}


	*setController {arg controllerKey, typeOfController;
		//possible control types are onOff, continuous, note, slider2D, and range
		var function, localControlObject;

		//[controllerKey, typeOfController, instantTypeOfController].postln;

		if((typeOfController==instantTypeOfController),{

			localControlObject = instantControlObject;

			LiveModularInstrument.controllers.do{arg item;
				item.sendRequest=false;
			};

			this.getFunctionNSetController(instantRequestModule, localControlObject, controllerKey, instantServer);
		});

	}

	*clearController {arg serverKey, oscMsgClear;
		[serverKey, oscMsgClear].postln;
		actions[serverKey.asSymbol].removeAt(oscMsgClear.asSymbol);
		actions[serverKey.asSymbol].removeAt((oscMsgClear++"/z").asSymbol);
	}

	executeFunction {|serverKey, key, val|

	}

	*respond { |key, val|
		ModularServers.serverSwitcher.currentServers.flatten.asSet.do{arg serverKey;
			this.doTheGUI(serverKey, key, val);
		};
		this.doTheGUI('global', key, val);
	}

	*doTheGUI {arg serverKey, key, val;
		var nothing, key2, xyz, tempNode;
		//[serverKey, key, val].postln;
		tempNode = actions[serverKey.asSymbol];
		if(tempNode!=nil,{
			if(tempNode[key.asSymbol]!=nil,{
				tempNode[key.asSymbol].do{arg item;
					item.value(val)}
			});
		});
	}
}