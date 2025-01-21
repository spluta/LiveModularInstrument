MainProcessingWindow : MidiOscObject {
	var <>modularObjects, <>rootDirectory;
	var synthList, availableModules;
	var sendGUIVals, listeningPort, cpuUsage0, cpuUsage1, cpuUsageRout, cpuUsageRoutA, waitRand, waitRandA;
	var modulesButton, inputsButton, saveButton, loadButton, saveServerButton, loadServerButton;
	var buttonView, infoView, objectView, lemurPorts, tempOSCs;

	*new {arg group, modularObjects;
		^super.new.group_(group).modularObjects_(modularObjects).init;
	}

	init {
		//rootDirectory = PathName(PathName(this.class.filenameSymbol.asString).pathOnly).pathOnly;
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

		cpuUsage0 = StaticText().font_(Font("Helvetica", 10)).maxWidth_(60);
		cpuUsage1 = StaticText().font_(Font("Helvetica", 10)).maxWidth_(60);

		cpuUsageRout = Routine({inf.do{
			cpuUsage0.string = group.server.avgCPU.round(0.01).asString;
			0.2.wait;
		}});

		cpuUsageRoutA = Routine({inf.do{
			cpuUsage1.string = group.server.peakCPU.round(0.01).asString;
			0.2.wait;
		}});

		win = Window("Live Modular Instrument"+group.server);

		win.view.bounds.width_(objectView.bounds.height+40).height_(objectView.bounds.width);

		objectView.layout_(GridLayout.rows(*modularObjects.collect({arg item; item.view}).clump(modularObjects.size.sqrt)).margins_(0!4).spacing_(0));
		buttonView.layout_(HLayout(modulesButton, inputsButton, saveButton, loadButton, saveServerButton, loadServerButton).margins_(0!4).spacing_(0));
		infoView.layout_(HLayout(listeningPort, sendGUIVals, cpuUsage0, cpuUsage1).margins_(0!4).spacing_(0));

		win.layout_(VLayout(buttonView, objectView, infoView).margins_(0!4).spacing_(0));

		AppClock.play(cpuUsageRout);
		AppClock.play(cpuUsageRoutA);

		win.front;

		win.view.bounds.width_(objectView.bounds.height+40).height_(objectView.bounds.width);

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
	classvar <>numServers, <>inBusses, <>blockSizes, <>outBusses, hardwareBufferSize, whichClassList, servers, <>controllers, path;
	classvar numServers, windows, driverWin, device, sampleRate;
	classvar readyToRollCounter, addingServer=false;

	*new {
		^super.new.init;
	}

	*boot {arg numServersIn=1, inBussesIn, blockSizesIn, whichClassListIn, controllersIn, audioDevice, pathIn=nil;
		if(
			(controllersIn.indexOf(OSCReceiver_Mod)!=nil)
			and:(OSCReceiver_Mod.inPorts.collect{|item| thisProcess.openUDPPort(item).asInteger}.sum!=OSCReceiver_Mod.inPorts.size)
		){
			5.do{
			"OSC port issue".postln;
			"".postln;
			}
		}{ //else
			path = pathIn;
			numServers=numServersIn;
			inBusses = List.newClear(8);

			inBusses=List[0,1,2,3,4,5,6,7];
			inBussesIn.do{arg item, i;
				if(i<8,{
					inBusses.put(i, item);
			})};

			blockSizes=128!numServers;
			blockSizesIn.do{arg item, i;
				if(i<numServers,{
					blockSizes.put(i, item);
			})};

			controllers = controllersIn; whichClassList=whichClassListIn;

			//set up the control devices and control GUI

			if(whichClassList == nil, {whichClassList = 'normal'});
			ModularClassList.new(whichClassList);

			readyToRollCounter = 0;

			if(audioDevice.notNil){
				this.bootWDevice(audioDevice);
			}{
				device = "BlackUCX";
				sampleRate = 88200;

				driverWin = Window("Which Interface?", Rect(500, 500, 200, 100)).layout_(
					VLayout(
						PopUpMenu().items_(ServerOptions.devices).value_(ServerOptions.devices.indexOfEqual("BlackUCX"))
						.action_{|menu| device = menu.item.asString},
						PopUpMenu().items_(["44100", "48000", "88200", "96000"]).value_(2)
						.action_{|menu| sampleRate = menu.item.asInteger; Server.local.options.sampleRate_(sampleRate)},
						Button().states_([["GO", Color.green, Color.black]])
						.action_{|butt|
							this.bootWDevice(device);
							driverWin.close;
						}
					)
				);
				driverWin.front;
			}
		}
	}

	*bootWDevice {|device|
		Server.local.options.device = device;
		if(device=="MacBook Pro Speakers"){Server.local.options.inDevice="MacBook Pro Microphone"};
		if(device=="MacBook Pro Microphone"){Server.local.options.outDevice="MacBook Pro Speakers"};
		if(device=="External Headphones"){Server.local.options.inDevice="MacBook Pro Microphone"};
		ModularServers.device_(device);
		ModularServers.boot(numServers, inBusses, blockSizes); //sends readyToRoll messages once the servers are loaded
	}

	*colorize_APC {|color=true|
		var num;
		MIDIClient.init;
		num = (0..MIDIClient.destinations.size-1).select{|i| (MIDIClient.destinations[i].device=="APC mini mk2"&&MIDIClient.destinations[i].name=="Control")};
		num.postln;
		
		if(num.size>0){
			var device = MIDIOut(num[0]);
			60.do{|i|device.noteOn(6, i, 0)};
			if(color){
				
				[0,1,2,3,8,9,10,11].do{|i| device.noteOn(6, i, 53)};
				[4, 5, 6, 7, 12, 13, 14, 15,23].do{|i| device.noteOn(6, i, 114)};
				[28, 29, 30, 31, 36, 37, 38, 39,47].do{|i| device.noteOn(6, i, 57)};
				[48, 49, 50, 51, 56, 57, 58, 59, 40,41,42,43,35].do{|i| device.noteOn(6, i, 21)};
			}
		}{
			"APC Mini not found".postln;
		};
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
				controllers = [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), TypeOSCFunc_Mod, MIDI_Mod];
			});
			controllers.do{arg item; item.start};
			MidiOscControl.new;
		});

		// try{
		// 	var m = MIDIOut.newByName("APC mini mk2", "Control");
		// 	2.do{|i|4.do{|i2| m.noteOn(6, 8*i+i2, 67 )}};
		// 	2.do{|i|4.do{|i2| m.noteOn(6, 8*i+i2+4, 94 )}}; 
		// 	m.noteOn(6, 23, 94);
		// }{
		// 	"no APC mini mk2".postln;
		// };

		readyToRollCounter = readyToRollCounter+1;
		if(addingServer,{

			ModularServers.modularInputsArray.addServer(ModularServers.servers[("lmi"++(numServers).asString).asSymbol]);

			numServers = numServers+1;
			addingServer = false;
		},{
			if(readyToRollCounter==numServers, {
				"readyToRoll".postln;
				Window.allWindows.do{arg item; if(item.visible==true,{item.front})};

				ModularServers.addInputsArray(inBusses, device);
				ModularServers.updateServerSwitcher;

				MidiOscControl.createActionDictionary;
				if(path!=nil, {ModularServers.load(path)});
				MidiOscControl.responding_(true);
				ModularServers.modularInputsArray.front;
			});

		});
	}

	*killMe {
		OSCFunc.allFuncProxies.do{arg item; item.remove};
	}
}

