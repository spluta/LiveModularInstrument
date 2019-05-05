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
			if(function.size<2,{
				this.setFunction(module, controllerKey, function, server);
			},{
				this.setFunction(module, controllerKey, function[0], server);
				controllerKey = controllerKey++"/z";
				this.setFunction(module, controllerKey, function[1], server, false);
			});
		});
	}


	*setFunction {|module, controllerKey, function, server, setMsg=true|
		var tempDict;

		if(actions[server.asSymbol][controllerKey.asSymbol]==nil,{
			tempDict = Dictionary.new;
			actions[server.asSymbol].add(controllerKey.asSymbol->function);
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

		if((typeOfController==instantTypeOfController),{

			localControlObject = instantControlObject;

			LiveModularInstrument.controllers.do{arg item;
				item.sendRequest=false;
			};

			this.getFunctionNSetController(instantRequestModule, localControlObject, controllerKey, instantServer);
		});

	}

	*clearController {arg serverKey, oscMsgClear;
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
		tempNode = actions[serverKey.asSymbol];
		if(tempNode!=nil,{
			if(tempNode[key.asSymbol]!=nil,{
				tempNode[key.asSymbol].do{arg item; item.value(val)}
			},{
/*
				if(key.asString.beginsWith("/MultiBall")||key.asString.beginsWith("/Fader")||key.asString.beginsWith("/Range"),{
					#nothing, key2, xyz = key.asString.split;
					tempNode = tempNode[("/"++key2.asString).asSymbol];
					if(tempNode!=nil,{
						tempNode.value(xyz,val)
					});
				})*/
			});
		});
	}
}