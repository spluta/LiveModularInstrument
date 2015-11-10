MidiOscControl {
	classvar instantRequestModule, instantControlObject, instantServer, instantSetups, instantTypeOfController;
	classvar <>actions;


	*new {
		^super.newCopyArgs().init;
	}

	init {
		actions = Dictionary.new;

	}

	*createActionDictionary {
		ModularServers.servers.keys.do{arg item, i;
			actions.add(item.asSymbol -> Dictionary.new);
		};
		actions.add(\global -> Dictionary.new);
	}

	*requestInstantAssign {arg module, controlObject, typeOfController, server, setupsIn;

		instantRequestModule = module;
		instantControlObject = controlObject;
		instantServer = server;
		instantTypeOfController = typeOfController;
		instantSetups = setupsIn;


		//this sets all controllers to look for moving sliders/buttons

		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest=true;
		};
	}

	*clearInstantAssign {
		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest=false;
		};
	}

	*getFunctionNSetController {arg module, controlObject, controllerKey, server, setups;
		var function, tempDict, controlObjectLocal, counter;

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
			if(server=='global',{
				actions['global'].add(controllerKey.asSymbol->function);
				module.setOscMsg(controllerKey.asSymbol);
				},{
					if(actions[server.asSymbol][controllerKey.asSymbol].postln==nil,{
						tempDict = Dictionary.new;

						actions[server.asSymbol].add(controllerKey.asSymbol->tempDict);
					});

					setups.do{arg setup; actions[server.asSymbol][controllerKey.asSymbol].add(setup.asSymbol->function)};
					module.setOscMsg(controllerKey.asSymbol);
			});
		});
	}

	*setControllerNoGui {arg server, key, function, setups;
		var tempDict;

		setups.postln;
		actions[server.asSymbol].postln;

		if(actions[server.asSymbol][key.asSymbol].postln==nil,{
			tempDict = Dictionary.new;
			actions[server.asSymbol].add(key.asSymbol->tempDict);
		});

		actions[server.asSymbol].postln;

		setups.do{arg setup; actions[server.asSymbol][key.asSymbol].add(setup.asSymbol->function)};
	}


	*setController {arg controllerKey, typeOfController;
		//possible control types are onOff, continuous, note, slider2D, and range
		var function, localControlObject;

		if((typeOfController==instantTypeOfController),{

			localControlObject = instantControlObject;

			LiveModularInstrument.controllers.do{arg item;
				item.sendRequest=false;
			};

			this.getFunctionNSetController(instantRequestModule, localControlObject, controllerKey, instantServer, instantSetups);
		});

	}

	*addFuncToSetup {arg server, setup, msg;
		var temp, key, function;

		key = actions[server.asSymbol][msg.asSymbol].keys.choose;

		function = actions[server.asSymbol][msg.asSymbol][key.asSymbol];

		actions[server.asSymbol][msg.asSymbol].add(setup.asSymbol->function);
	}

	*removeFuncFromSetup {arg server, setup, item;
		actions[server.asSymbol][item.asSymbol].removeAt(setup.asSymbol);
	}

	*clearController {arg serverClear, oscMsgClear;
		actions[serverClear.asSymbol].removeAt(oscMsgClear.asSymbol);
	}

	*setControllersWCurrentSetup {arg serverName, oscMsg;
		LiveModularInstrument.controllers.do{arg item;
			item.setWCurrentSetup(serverName, oscMsg);
		};
	}


	*respond { |key, val|
		var nothing, xyz, tempNode;

		tempNode = actions[\global][key.asSymbol];
		if(tempNode!=nil, {
			tempNode.value(val);
		});

		//only execute the function if the server is currently active
		ModularServers.serverSwitcher.currentServers.do{arg serverKey;
			var setup;
			serverKey = serverKey.asSymbol;
			setup = ModularServers.servers[serverKey].getCurrentSetup.asSymbol;

			if(key.asString.beginsWith("/MultiBall")||key.asString.beginsWith("/Fader")||key.asString.beginsWith("/Range"),{
				#nothing, key, xyz = key.asString.split;
				tempNode = actions[serverKey][("/"++key.asString).asSymbol];
				if(tempNode!=nil, {
					tempNode[setup].value(xyz, val);
				});
				},{
					tempNode = actions[serverKey][key.asSymbol];
					if(tempNode!=nil, {
						tempNode[setup].value(val);
					});
			});
		}
	}
}