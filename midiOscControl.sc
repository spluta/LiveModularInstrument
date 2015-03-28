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
		var tempDict;

		ModularServers.servers.keys.do{arg item, i;
			tempDict = Dictionary.new;
			//ModularServers.setups.do{arg item2; tempDict.add(item2.asSymbol->())};
			actions.add(item.asSymbol -> tempDict);
		};
		actions.postln;
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


	*addMainSwitch{|serverName, controls|

		ModularServers.setups.do{arg setup;
			var function, controllerKey, tempDict, functionsKeys;

			LiveModularInstrument.controllers.do{arg item;
				functionsKeys = item.getMainSwitchControls(serverName, controls);
				if(functionsKeys!=nil,{
				functionsKeys.do{arg functionKey;
					functionKey.postln;
					#function, controllerKey = functionKey;
					tempDict = Dictionary.new;

					4.do{arg i2; tempDict.add(ModularServers.setups[i2].asSymbol->function)};

					actions[serverName.asSymbol].add(controllerKey.asSymbol->tempDict);
					}
				});
			};

			actions.postln;

		}
	}

	*getFunctionNSetController {arg module, controlObject, controllerKey, server, setups;
		var function, tempDict, controlObjectLocal, counter;

		[module, controlObject, controllerKey, server, setups].postln;

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
			function.postln;
			tempDict = Dictionary.new;

			actions[server.asSymbol].add(controllerKey.asSymbol->tempDict);

			setups.do{arg setup; actions[server.asSymbol][controllerKey.asSymbol].add(setup.asSymbol->function)};
			module.setOscMsg(controllerKey.asSymbol);
		});
	}

	*setControllerNoGui {arg server, key, function, setups;
		var tempDict;

		tempDict = Dictionary.new;
		actions[server.asSymbol].add(key.asSymbol->tempDict);

		setups.do{arg setup; actions[server.asSymbol][key.asSymbol].add(setup.asSymbol->function)};
	}


	*setController {arg controllerKey, typeOfController;
		//possible control types are onOff, continuous, note, slider2D, and range
		var function, localControlObject, tempDict;

		[controllerKey, typeOfController].postln;
		"instantControlObject: ".post; instantControlObject.postln;


		if((typeOfController==instantTypeOfController),{

			"gonna add".postln;

			localControlObject = instantControlObject;

			LiveModularInstrument.controllers.do{arg item;
				item.sendRequest=false;
			};

			this.getFunctionNSetController(instantRequestModule, localControlObject, controllerKey, instantServer, instantSetups);
		});

	}

	*addFuncToSetup {arg server, setup, msg;
		var temp, key, function;

		[server, setup, msg].postln;
		actions.postln;

		key = actions[server.asSymbol][msg.asSymbol].keys.choose;
		key.postln;

		function = actions[server.asSymbol][msg.asSymbol][key.asSymbol];
		function.postln;

		actions[server.asSymbol][msg.asSymbol].add(setup.asSymbol->function);
		actions.postln;
	}

	*removeFuncFromSetup {arg server, setup, item;
		actions[server.asSymbol][item.asSymbol].removeAt(setup.asSymbol);
	}

	*clearController {arg serverClear, oscMsgClear;
		[serverClear, oscMsgClear].postln;
		actions[serverClear.asSymbol].postln;
		actions[serverClear.asSymbol].removeAt(oscMsgClear.asSymbol);
	}

	*setControllersWCurrentSetup {arg serverName, setupNum;
		LiveModularInstrument.controllers.do{arg item;
			item.setWCurrentSetup(serverName, setupNum);
		};
	}


	*respond { |key, val|
		var nothing, xyz, tempNode;

		//[key, val].postln;

		ModularServers.servers.keys.do{arg serverKey;
			var setup;
			setup = ModularServers.servers[serverKey].getCurrentSetup;
			//setup.postln;

			//[serverKey, setup, key].postln;

			if(key.asString.beginsWith("/MultiBall")||key.asString.beginsWith("/Fader")||key.asString.beginsWith("/Range"),{
				#nothing, key, xyz = key.asString.split;
				tempNode = actions[serverKey.asSymbol][("/"++key.asString).asSymbol];
				if(tempNode!=nil, {
					//tempNode.postln;
					tempNode[setup.asSymbol].value(xyz, val);
				});
				},{
					tempNode = actions[serverKey.asSymbol][key.asSymbol];
					if(tempNode!=nil, {
						//tempNode.postln;
						tempNode[setup.asSymbol].value(val);
					});
			});
		}
	}
}