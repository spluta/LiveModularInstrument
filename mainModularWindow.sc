MainProcessingWindow : MidiOscObject {
	var synthList, availableModules, synthLocation, availableBusses, paths, file, text, num;
	var saveButtons, loadButtons, soundInBussesTemp, stereoSoundInBussesTemp, availableBussesTemp, soundInBoxes, stereoSoundInBoxes, classBoxes, classBoxItems, classScroller, modularClassList, xmlBounds;
	var setupButtons, currentSetup, objectBusses, <>assignMantaButton, <>mainSwitchSet;
	var resetOscButton, listeningPort, cpuUsage0, cpuUsage1, cpuUsageRout;

	*new {arg group;
		^super.new.group_(group).init;
	}

	init {

		setups = ModularServers.setups;

		win = Window("Live Modular Instrument"+group.server, Rect(0, Window.screenBounds.height*2, 675, 90*5+110));

		this.initControlsAndSynths(8);

		dontLoadControls = [0,1,2,3];

		soundInBoxes = List.new;
		ModularServers.getSoundInBusses(group.server).do{arg item, i;
			soundInBoxes.add(DragSource(win,Rect(5+(45*i), 0, 45, 16)));
			soundInBoxes[i].setProperty(\align,\center);
			soundInBoxes[i].object = [item, "S"+i.asString];
			soundInBoxes[i].string="S"+i.asString;
			soundInBoxes[i].dragLabel="S"+i.asString;
		};

		stereoSoundInBoxes = List.new;
		ModularServers.getStereoSoundInBusses(group.server).do{arg item, i;
			stereoSoundInBoxes.add(DragSource(win,Rect(5+(90*i), 20, 90, 16)));
			stereoSoundInBoxes[i].setProperty(\align,\center);
			stereoSoundInBoxes[i].object = [item, "S"++(i*2).asString++((i*2)+1).asString];
			stereoSoundInBoxes[i].string="S"++(i*2).asString++((i*2)+1).asString;
			stereoSoundInBoxes[i].dragLabel="S"++(i*2).asString++((i*2)+1).asString;
		};

		classBoxes = List.new;
		classBoxItems = List.new;
		classBoxItems = ModularClassList.classArray.deepCopy;

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

		saveButtons = List.newClear(0);
		loadButtons = List.newClear(0);

		saveButtons.add(Button(win, Rect(5, 90*5+50, 100, 16))
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
		});

		saveButtons.add(Button(win, Rect(5, 90*5+70, 100, 16))
			.states_([["SaveServer", Color.black, Color.green]])
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
						"cancelled".postln;
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				});
		});

		saveButtons.add(Button(win, Rect(5, 90*5+90, 100, 16))
			.states_([["SaveSetup", Color.black, Color.green]])
			.action_{
				visibleArray = List.newClear(0);
				Window.allWindows.do{arg item;

					visibleArray.add(item.visible);
					item.visible = false
				};

				Dialog.savePanel({ arg path;
					ModularServers.servers[group.server.asSymbol].saveSetup(path, currentSetup);

					visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
					},{
						"cancelled".postln;
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				});
		});

		loadButtons.add(Button(win, Rect(110, 90*5+50, 100, 16))
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
					//Window.allWindows.do{arg item, i; item.visible = true};

					},{
						"cancelled".postln;
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})
						};
				});
		});

		loadButtons.add(Button(win, Rect(110, 90*5+70, 100, 16))
			.states_([["LoadServer", Color.green, Color.black]])
			.action_{
				visibleArray = List.newClear(0);
				Window.allWindows.do{arg item;

					visibleArray.add(item.visible);
					item.visible = false
				};
				Dialog.openPanel({ arg path;
					ModularServers.load(path, group.server.postln);

						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})}
					},{
						"cancelled".postln;
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})
						};
				});
		});

		loadButtons.add(Button(win, Rect(110, 90*5+90, 100, 16))
			.states_([["LoadSetup", Color.green, Color.black]])
			.action_{
				visibleArray = List.newClear(0);
				Window.allWindows.do{arg item;

					visibleArray.add(item.visible);
					item.visible = false
				};
				Dialog.openPanel({ arg path;
					ModularServers.servers[group.server.asSymbol].loadSetup(path, currentSetup);

						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})}
					},{
						"cancelled".postln;
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				});
		});

		currentSetup = 0;

		4.do{arg i;
			controls.add(Button(win, Rect(220+(60*i), 90*5+50, 60, 16))
				.states_([["setup"++i, Color.green, Color.black],["setup"++i, Color.black, Color.green]])
				.action_{arg butt;
					var temp;
					(0..3).do{arg i2; controls[i2].value = 0};
					butt.value = 1;
					currentSetup = i;
					ModularServers.changeSetup(group.server, currentSetup);
					MidiOscControl.setControllersWCurrentSetup(group.server, oscMsgs[i]);
				};
			);
		};

		4.do{arg i;
			this.addAssignButton(i, \onOff, Rect(220+(60*i), 90*5+70, 60, 16))
		};

		4.do{arg i;
			controls.add(Button()
				.states_([["setup"++i, Color.green, Color.black],["setup"++i, Color.black, Color.green]])
				.action_{arg butt;
					var temp;
					controls[i].valueAction_(1);
				};
			);
		};

		(4..7).do{arg i;
			this.addAssignButton(i, \onOff, Rect(220+(60*(i-4)), 90*5+90, 60, 16))
		};


		mainSwitchSet = false;

		//think about how to do this that is good for all OSC controllers
		resetOscButton = Button(win, Rect(460, 90*5+50, 90, 16))
		.states_([["ResetOSC"]])
		.action_{
			LiveModularInstrument.controllers.do{arg item;
				item.resetOSCAddr;
			}
		};

		listeningPort = StaticText(win, Rect(550, 90*5+50, 90, 16));
		listeningPort.string = NetAddr.langPort.asString;

		cpuUsage0 = StaticText(win, Rect(460, 90*5+70, 90, 16));

		cpuUsage1 = StaticText(win, Rect(550, 90*5+70, 90, 16));

		cpuUsageRout = Routine({inf.do{
			cpuUsage0.string = group.server.avgCPU.round(0.01).asString;
			cpuUsage1.string = group.server.peakCPU.round(0.01).asString;
			1.wait;
		}});

		AppClock.play(cpuUsageRout);

		win.front;

		win.onClose = {
			//kill all the stuff
			synthList.do{arg item; item.killMe};
			cpuUsageRout.stop;
			thisProcess.recompile;
		}
	}

	hide {
		win.visible = false;
	}

	show {
		win.visible = true;
	}

	changeSetup {arg num;
		synthList.do{arg item; item.changeSetup(num)};
	}

	hitButton {arg num;
		controls[num].valueAction_(1);
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

	*new {
		^super.new.init;
	}

	*boot {arg numServersIn=1, inBusIn=0, outBusIn=0, hardwareBufferSizeIn=64, whichClassListIn, controllersIn;

		numServers=numServersIn; inBus=inBusIn; outBus=outBusIn; controllers = controllersIn; hardwareBufferSize=hardwareBufferSizeIn; whichClassList=whichClassListIn;

		Server.local.options.hardwareBufferSize_(hardwareBufferSize);

		setups = List.newClear(0);
		((numServers*4-1)..0).do{arg i; setups.add('setup'++i.asSymbol)};

		//set up the control devices and control GUI
		if(controllers == nil, {
			controllers = [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start];
		});

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

			ModularServers.modularInputsArray.addServer( ModularServers.servers[("lmi"++(numServers).asString).asSymbol]); //this is stupid

			numServers = numServers+1;
			addingServer = false;
			},{
				if(readyToRollCounter.postln==numServers, {

					"readyToRoll".postln;
					Window.allWindows.do{arg item; if(item.visible==true,{item.front})};

					ModularServers.addInputsArray(inBus);
					ModularServers.updateServerSwitcher;

					MidiOscControl.createActionDictionary;
				});
				Window.allWindows.do{arg item, i; item.front};
		});
	}

	*killMe {
		MainMixer.killMe;
		OSCFunc.allFuncProxies.do{arg item; item.remove};
	}
}

