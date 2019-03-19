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
		actions.postln;
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
		val.postln;
		instantControlObject.postln;
		{instantControlObject.string_(val)}.defer;
	}

	*requestInstantTypeAssign {arg controlObject, server;

		"controlObject".post;controlObject.postln;

		instantControlObject = controlObject;
		instantServer = server;

		//this sets all controllers to look for moving sliders/buttons

		//this should probably be all OSC controllers
		LiveModularInstrument.controllers.do{arg item;
			item.sendTypeRequest=true;
		};
	}

	*clearInstantAssign {
		"clearInstantAssign".postln;
		LiveModularInstrument.controllers.do{arg item;
			item.postln;
			item.sendRequest_(false);
			item.sendTypeRequest_(false);
		};
	}

	*getFunctionNSetController {arg module, controlObject, controllerKey, server;
		var function, controlObjectLocal, counter;

		"getFuncNSet".postln;
		[module, controlObject, controllerKey, server].postln;
		controlObjectLocal = controlObject;
		//get the function
		counter=0;
		while({(counter<LiveModularInstrument.controllers.size)&&(function==nil)},
			{
				function = LiveModularInstrument.controllers[counter].getFunctionFromKey(module, controllerKey, controlObjectLocal);
				counter = counter+1;
			}
		);

		function.postln;

		//add the function to the Dictionary
		if(function!=nil,{
			actions.postln;
			if(function.size.postln<2,{
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

		[key, functions].postln;

		LiveModularInstrument.controllers.do{arg item;
			item.sendRequest=false;
		};

		server.postln;
		actions[server.asSymbol].add(key.asSymbol->functions);

		actions.postln;
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

/*	*setServerSwitcherController {arg controllerKey, typeOfController;
		//possible control types are onOff, continuous, note, slider2D, and range
		var function, localControlObject;

		if((typeOfController==instantTypeOfController),{

			localControlObject = instantControlObject;

			LiveModularInstrument.controllers.do{arg item;
				item.sendRequest=false;
			};

			this.getFunctionNSetController(instantRequestModule, localControlObject, controllerKey, instantServer);
		});

	}*/

	//MidiOscControl.clearController(group.server, oscMsgs[num])
	*clearController {arg serverKey, oscMsgClear;
		[serverKey, oscMsgClear].postln;
		actions[serverKey.asSymbol].postln;
		actions[serverKey.asSymbol].removeAt(oscMsgClear.asSymbol);
	}

	executeFunction {|serverKey, key, val|

	}

/*	*respondGlobal {|key, val|
		this.doTheGUI('global', key, val);
	}*/

	*respond { |key, val|
		ModularServers.serverSwitcher.currentServers.flatten.do{arg serverKey;
			this.doTheGUI(serverKey, key, val);
		};
		//[key,val].postln;
		this.doTheGUI('global', key, val);
	}

	*doTheGUI {arg serverKey, key, val;
		var nothing, key2, xyz, tempNode;
		tempNode = actions[serverKey.asSymbol];
		if(tempNode!=nil,{
			if(tempNode[key.asSymbol]!=nil,{
				tempNode[key.asSymbol].do{arg item; item.value(val)}
			},{

				if(key.asString.beginsWith("/MultiBall")||key.asString.beginsWith("/Fader")||key.asString.beginsWith("/Range"),{
					#nothing, key2, xyz = key.asString.split;
					tempNode = tempNode[("/"++key2.asString).asSymbol];
					if(tempNode!=nil,{
						tempNode.value(xyz,val)
					});
				})
			});
		});
	}
}