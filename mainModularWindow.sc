MainProcessingWindow : MidiOscObject {
	var <>modularObjects;
	var synthList, availableModules;
	var sendGUIVals, listeningPort, cpuUsage0, cpuUsage1, cpuUsageRout, cpuUsageRoutA, waitRand, waitRandA;
	var modulesButton, inputsButton, saveButton, loadButton, saveServerButton, loadServerButton;
	var buttonView, infoView, objectView, lemurPorts, tempOSCs;

	*new {arg group, modularObjects;
		^super.new.group_(group).modularObjects_(modularObjects).init;
	}

	init {

		this.initControlsAndSynths(0);

		buttonView = CompositeView().maxHeight_(15);
		infoView = CompositeView().maxHeight_(15);
		objectView = CompositeView().maxHeight_(modularObjects.size.sqrt*60).maxWidth_(modularObjects.size.sqrt*80);

		inputsButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["Inputs", Color.black, Color.grey]])
		.action_{
			InBusWindow_Mod.makeWindow
		};

		modulesButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["Modules", Color.black, Color.grey]])
		.action_{
			ClassWindow_Mod.makeWindow
		};

		saveButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["Save", Color.black, Color.green]])
		.action_{
			visibleArray = List.newClear(0);
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};

			Dialog.savePanel({ arg path;
				ModularServers.save(path);

				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};

		saveServerButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["S Server", Color.black, Color.green]])
		.action_{
			visibleArray = List.newClear(0);
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};

			Dialog.savePanel({ arg path;
				ModularServers.save(path, group.server);
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};

		loadButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["Load", Color.green, Color.black]])
		.action_{
			visibleArray = List.newClear(0);
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};
			Dialog.openPanel({ arg path;
				ModularServers.load(path);

				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})}

			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})
				};
			});
		};

		loadServerButton = Button().font_(Font("Helvetica", 10)).maxWidth_(53)
		.states_([["L Server", Color.green, Color.black]])
		.action_{
			visibleArray = List.newClear(0);
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};
			Dialog.openPanel({ arg path;
				ModularServers.load(path, group.server);
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})}
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})
				};
			});
		};

		//think about how to do this that is good for all OSC controllers
		sendGUIVals = Button().font_(Font("Helvetica", 10)).maxWidth_(60)
		.states_([["SendGUIVals"]])
		.action_{
			ModularServers.servers.do{arg item; item.sendGUIVals};
			ModularServers.modularInputsArray.sendGUIVals;
		};


		listeningPort = StaticText().font_(Font("Helvetica", 10)).maxWidth_(60);
		listeningPort.string = NetAddr.langPort.asString;

/*		if(group.server.asString=="lmi1",{
			lemurPorts = Array.fill(2, {|i|
				TextField().maxWidth_(60)
				.string_(Lemur_Mod.netAddrs[i].port)
				.action_{arg val;
					Lemur_Mod.setPorts(lemurPorts.collect{|item| item.string.asInteger});
				}
			});

			tempOSCs = List.newClear(2);
			lemurPorts.do{|item, i|
				try{item.string = Lemur_Mod.netAddrs[i].port.asString}{("no port "++i.asString)};

				item.focusGainedAction = {
					tempOSCs.put(0, OSCFunc({|msg, time, addr|
						{item.string = addr.port}.defer;
						tempOSCs[1].free;
					}, '/Switches/x').oneShot);
					tempOSCs.put(1, OSCFunc({|msg, time, addr|
						{item.string = addr.port}.defer;
						tempOSCs[0].free;
					}, '/Container2/Switches/x').oneShot);
				};
				item.focusLostAction = {
					tempOSCs.do{|i2| i2.free}
				}
			};
		},{
			lemurPorts = Array.fill(2, {|i|
				Button().maxWidth_(60)
			})
		});*/



		cpuUsage0 = StaticText().font_(Font("Helvetica", 10)).maxWidth_(60);
		cpuUsage1 = StaticText().font_(Font("Helvetica", 10)).maxWidth_(60);

		//waitRand = rrand(4.9,2.1);
		cpuUsageRout = Routine({inf.do{
			cpuUsage0.string = group.server.avgCPU.round(0.01).asString;
			0.2.wait;
		}});

		//waitRandA = rrand(1.9,2.1);
		cpuUsageRoutA = Routine({inf.do{
			cpuUsage1.string = group.server.peakCPU.round(0.01).asString;
			0.2.wait;
		}});

		win = Window("Live Modular Instrument"+group.server);
		win.view.maxWidth_(objectView.bounds.height+40).maxHeight_(objectView.bounds.width);

		objectView.layout_(GridLayout.rows(*modularObjects.collect({arg item; item.view}).clump(modularObjects.size.sqrt)).margins_(0!4).spacing_(0));
		buttonView.layout_(HLayout(modulesButton, inputsButton, saveButton, loadButton, saveServerButton, loadServerButton).margins_(0!4).spacing_(0));
		infoView.layout_(HLayout(listeningPort,/*lemurPorts[0], lemurPorts[1], */sendGUIVals, cpuUsage0, cpuUsage1).margins_(0!4).spacing_(0));

		win.layout_(VLayout(buttonView, objectView, infoView).margins_(0!4).spacing_(0));

		AppClock.play(cpuUsageRout);
		AppClock.play(cpuUsageRoutA);

		win.front;

		win.onClose = {
			//kill all the stuff
			synthList.do{arg item; item.killMe};
			cpuUsageRout.stop;
			cpuUsageRoutA.stop;
			thisProcess.recompile;
		}
	}

	hide {
		win.visible = false;
	}

	show {
		win.visible = true;
	}

}



LiveModularInstrument {
	classvar <>numServers, <>inBusses, <>outBusses, hardwareBufferSize, whichClassList, servers, <>controllers, path;
	classvar numServers, windows;
	classvar readyToRollCounter, addingServer=false;

	*new {
		^super.new.init;
	}

	*boot {arg numServersIn=1, inBussesIn, whichClassListIn, controllersIn, pathIn=nil;
		/*if(NetAddr.langPort.asSymbol!='57120')
		{
			5.do{
			"fix LangPort to 57120".postln;
			"".postln;
			}
		}*/
		if(
			(controllersIn.indexOf(OSCReceiver_Mod)!=nil)
			and:(OSCReceiver_Mod.inPorts.collect{|item| thisProcess.openUDPPort(item).asInteger}.sum!=OSCReceiver_Mod.inPorts.size)
		)
		{
			5.do{
			"OSC port issue".postln;
			"".postln;
			}
		}
		{
			path = pathIn;
			numServers=numServersIn;
			inBusses = List.newClear(8);

			inBusses=List[0,1,2,3,4,5,6,7];
			inBussesIn.do{arg item, i;
				if(i<8,{
					inBusses.put(i, item);
			})};

			controllers = controllersIn; whichClassList=whichClassListIn;

			//set up the control devices and control GUI

			if(whichClassList == nil, {whichClassList = 'normal'});
			ModularClassList.new(whichClassList);

			readyToRollCounter = 0;
			ModularServers.boot(numServers, inBusses); //sends readyToRoll messages once the servers are loaded
		}
	}

	*addServer {
		addingServer = true;
		ModularServers.addServer;
	}

	*readyToRoll {
		//receives this message from the ModularServers to know that they are ready to be populated

		//set the controllers only on the first readyToRoll message
		if(readyToRollCounter==0,{
			if(controllers == nil, {
				controllers = [MantaCocoa_Mod, Lemur_Mod, MIDI_Mod];
			});
			controllers.do{arg item; item.start};
			MidiOscControl.new;
		});

		readyToRollCounter = readyToRollCounter+1;
		if(addingServer,{

			ModularServers.modularInputsArray.addServer( ModularServers.servers[("lmi"++(numServers).asString).asSymbol]);

			numServers = numServers+1;
			addingServer = false;
		},{
			if(readyToRollCounter==numServers, {
				"readyToRoll".postln;
				Window.allWindows.do{arg item; if(item.visible==true,{item.front})};

				ModularServers.addInputsArray(inBusses);
				ModularServers.updateServerSwitcher;

				MidiOscControl.createActionDictionary;
				if(path!=nil, {ModularServers.load(path)});
				MidiOscControl.responding_(true);
			});
			//Window.allWindows.do{arg item, i; item.front};

		});


	}

	*killMe {
		OSCFunc.allFuncProxies.do{arg item; item.remove};
	}
}

