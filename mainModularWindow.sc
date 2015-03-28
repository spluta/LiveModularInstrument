
MainProcessingWindow {
	var serverName, <>win, synthList, availableModules, synthLocation, availableBusses, paths, file, text, num, visibleArray;
	var saveButton, loadButton, xmlSynthBars, xmlBusses, xmlSoundInBusses, xmlInternalBusses, soundInBussesTemp, stereoSoundInBussesTemp, availableBussesTemp, soundInBoxes, stereoSoundInBoxes, classBoxes, classBoxItems, classScroller, modularClassList, xmlBounds;
	var setupButtons, objectBusses, <>assignMantaButton, <>mainSwitchSet, smallWin;
	var xmlLocalBusses, xmlLocalBussesSize, extraLocalBusses, temp, resetLemurButton, listeningPort, controls, assignButtons;

	*new {arg serverName;
		^super.newCopyArgs(serverName).init;
	}

	init {
		win = Window("Live Modular Instrument"+serverName, Rect(0, Window.screenBounds.height*2, 675, 90*5+90));

		soundInBoxes = List.new;
		ModularServers.getSoundInBusses(serverName).do{arg item, i;
			soundInBoxes.add(DragSource(win,Rect(5+(45*i), 0, 45, 16)));
			soundInBoxes[i].setProperty(\align,\center);
			soundInBoxes[i].object = [item, "S"+i.asString];
			soundInBoxes[i].string="S"+i.asString;
			soundInBoxes[i].dragLabel="S"+i.asString;
		};

		stereoSoundInBoxes = List.new;
		ModularServers.getStereoSoundInBusses(serverName).do{arg item, i;
			stereoSoundInBoxes.add(DragSource(win,Rect(5+(90*i), 20, 90, 16)));
			stereoSoundInBoxes[i].setProperty(\align,\center);
			stereoSoundInBoxes[i].object = [item, "S"++(i*2).asString++((i*2)+1).asString];
			stereoSoundInBoxes[i].string="S"++(i*2).asString++((i*2)+1).asString;
			stereoSoundInBoxes[i].dragLabel="S"++(i*2).asString++((i*2)+1).asString;
		};

		classBoxes = List.new;
		classBoxItems = List.new;
		classBoxItems = ModularClassList.classArray.deepCopy;
		"classBoxItems".post;classBoxItems.postln;

		min(20, classBoxItems.size).do{arg i;
			classBoxes.add(DragSource(win,Rect(500, i*25, 150, 20)));
			classBoxes[i].object = classBoxItems[i];
		};

		classScroller = EZScroller(win, Rect(650, 0, 20, 20*25), 20, classBoxItems.size, {
			arg scroller;

			classBoxes.do{|drag, i| drag.object_(classBoxItems[scroller.value.asInteger+i] ? ""); };
		});
		classScroller.visible_(classBoxItems.size>20);

		synthList = List.new;

		saveButton = Button(win, Rect(5, 90*5+50, 120, 16))
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
					"cancelled".postln;
					visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};

		loadButton = Button(win, Rect(110, 90*5+50, 120, 16))
		.states_([["Load", Color.green, Color.black]])
		.action_{
			visibleArray = List.newClear(0);
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};
			Dialog.openPanel({ arg path;
				ModularServers.load(path);

				//i need to completely redo how things are saved and loaded
				Window.allWindows.do{arg item, i; item.visible = true};
				},{
					"cancelled".postln;
					visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})
					};
			});
		};

		//setups = ModularServers.getSetups(serverName);

		//this.initControlLists(4);
		controls = List.new;
		//assignButtons = List.new;

		4.do{arg i;
			controls.add(Button(win, Rect(220+(60*i), 90*5+50, 60, 16))
				.states_([["setup"++i, Color.green, Color.black],["setup"++i, Color.black, Color.green]])
				.action_{arg butt;
					var temp;
					controls.do{arg item; item.value = 0;};
					butt.value = 1;
					ModularServers.changeSetup(serverName, i);

					MidiOscControl.setControllersWCurrentSetup(serverName, i);
				};
			);
			//i need to see why I inherited from Module here. I just want to be able to assign this switch
			//this.addDoubleAssignButton(Rect(220+(60*i), 90*5+70, 60, 16),i,0);
		};

		assignMantaButton = Button(win, Rect(465, 90*5+50, 16, 16))
		.states_([["A", Color.black, Color.red],["C", Color.red, Color.black]])
		.action_{arg butt;
			if(butt.value==1,{
				mainSwitchSet = true;
				MidiOscControl.addMainSwitch(serverName, controls);
				},{
					mainSwitchSet = false;
					4.do{|i| Manta.removeGlobal(\noteOn, 49+i)};
					MidiOscControl.removeMainSwitch(serverName);
			})
		};

		mainSwitchSet = false;

		resetLemurButton = Button(win, Rect(485, 90*5+50, 16, 16))
		.states_([["R"]])
		.action_{
			LiveModularInstrument.controllers.do{arg item;
				item.resetOSCAddr;
			}
		};

		listeningPort = StaticText(win, Rect(505, 90*5+50, 90, 16));
		listeningPort.string = NetAddr.langPort.asString;

		win.front;

		win.onClose = {
			//kill all the stuff
			synthList.do{arg item; item.killMe};
			thisProcess.recompile;
		}
	}

	changeSetup {arg num;
		synthList.do{arg item; item.changeSetup(num)};
	}

	setName {arg name;
		win.name_(name);
	}
}



LiveModularInstrument {
	classvar <>numServers, <>inBus, <>outBus, hardwareBufferSize, whichClassList, servers, modularInputArray, <>controllers;
	classvar xmlDoc, xmlRoot, xmlMainProc, xmlMainMixer, xmlInputArray, file;
	classvar numServers, setups, windows;
	classvar readyToRollCounter, addingServer=false;

	*new {arg numServers=1, inBus=0, outBus=0, hardwareBufferSize=64, whichClassList;
		^super.newCopyArgs(numServers, inBus, outBus, hardwareBufferSize, whichClassList).init;
	}

	*boot {arg numServersIn=1, inBusIn=0, outBusIn=0, hardwareBufferSizeIn=64, whichClassListIn, controllersIn;

		numServers=numServersIn; inBus=inBusIn; outBus=outBusIn; controllers = controllersIn; hardwareBufferSize=hardwareBufferSizeIn; whichClassList=whichClassListIn;

		Server.local.options.hardwareBufferSize_(hardwareBufferSize);

		setups = List.newClear(0);
		((numServers*4-1)..0).do{arg i; setups.add('setup'++i.asSymbol)};

		//set up the control devices and control GUI
		if(controllers == nil, {
			controllers = [Manta_Mod, Lemur_Mod, MIDI_Mod];
		});

		controllers.do{arg item;
			item.new;
		};

		if(whichClassList == nil, {whichClassList = 'normal'});
		ModularClassList.new(whichClassList);

		if(MIDIClient.initialized.not,{ MIDIIn.connectAll });

		MidiOscControl.new;

		readyToRollCounter = 0;


		numServers.postln;
		ModularServers.boot(numServers, inBus, outBus); //sends readToRoll messages once the servers are loaded
	}

	*addServer {
		addingServer = true;
		ModularServers.addServer;
	}

	*readyToRoll {
		//receives this message from the ModularServers to know that they are ready to be populated

		readyToRollCounter = readyToRollCounter+1;
		if(addingServer,{
			"adding new server".postln;

			ModularServers.modularInputArray.addServer( ModularServers.servers[("lmi"++(numServers).asString).asSymbol]); //this is stupid

			numServers = numServers+1;
			addingServer = false;
			},{
				if(readyToRollCounter.postln==numServers, {

					"readyToRoll".postln;
					Window.allWindows.do{arg item; if(item.visible==true,{item.front})};

					ModularServers.addInputsArray(inBus);

					MidiOscControl.createActionDictionary;
				});
				Window.allWindows.do{arg item, i; item.front};
		});
	}

	*killMe {
		MainMixer.killMe;
		Manta.clearDicts;
		MIDIResponder.removeAll;
		OSCFunc.allFuncProxies.do{arg item; item.remove};
		Lemur.killMe;
	}
}

