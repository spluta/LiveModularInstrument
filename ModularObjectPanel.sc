ChannelOutBox {
	var <>view, <>rect, <>outBus, channelOutBox;

	*new {arg view, rect, outBus;
		^super.newCopyArgs(view, rect, outBus).init;
	}


	init {
		channelOutBox = DragSource(view,rect);
		channelOutBox.setProperty(\align,\center);
		channelOutBox.object = [outBus, outBus.asString];
		channelOutBox.string = outBus.asString;
		channelOutBox.dragLabel = outBus.asString;
	}
}

ModularObjectPanel {
	var server, group, outBus, location, panel, point;

	var busAssignSink, inputBusString, synth, mixerGroup, synthGroup, lastSynth, internalBusses, mixer, sepInputBusses, index, xmlSynth, counter, inBusTemp, inBusTempList, isMixer, isRouter, synthAssignSink, synthDisp, synthKill, setupTemp;

	var showButton, hidden, channelOutBox, view, setupButtons, possibleSetups, setups, inputBusses;

	*new {arg server, group, outBus, location;
		^super.newCopyArgs(server, group, outBus, location).init;
	}

	init {
		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		mixer = ModularMixer(mixerGroup);

		possibleSetups = ModularServers.getSetups(server);
		setups = List.newClear(0);
		setups.add(possibleSetups[location[2]]);

		isMixer = false;
		isRouter = false;
	}

	addToWindow{arg window, visible;
		var point;

		point = Point((100*location[0]), (90*location[1]+40));

		view = CompositeView.new(window,Rect(point.x, point.y, 100, 95));

		view.background_(ModularServers.setupColors[location[2]]);
		view.visible = visible;
		hidden = false;

		setupButtons = List.new;
		4.do{|i| setupButtons.add(Button(view, Rect(i*25,0,25,10))
			.states_([["",Color.yellow,Color.black],["",Color.black,Color.yellow]])
			.action_{|butt|
				if(butt.value==1,{

					ModularServers.setModularPanelToSetup(server, location, possibleSetups[i]);

					setups.add(possibleSetups[i]);
					if(synth!=nil,{synth.addSetup(possibleSetups[i])});


				},{
					ModularServers.setModularPanelToSetup(server, [location[0],location[1],i], possibleSetups[i]);
					if(synth!=nil,{synth.removeSetup(possibleSetups[i])});
					setups.remove(possibleSetups[i]);
				});
			}
		)};

		setupButtons[location[2]].value = 1;
		setupButtons[location[2]].setProperty(\enabled, false);

		busAssignSink = BusAssignSink(this, view, Point(0,15));

		synthAssignSink = DragSink(view, Rect(0, 48, 25, 20));
		synthAssignSink.string="Syn";
		synthAssignSink.background_(Color.blue);
		synthAssignSink.receiveDragHandler={
			if(ModularClassList.checkSynthName(View.currentDrag.asString),{
				synthDisp.string = View.currentDrag.asString;
				this.makeNewSynth(View.currentDrag.asString);
			});
		};
		synthDisp = StaticText(view, Rect(25, 48, 64, 20));
		synthDisp.font_(Font("Helvetica",10));
		synthKill = Button(view, Rect(84,48,16,20))
		.states_([["k", Color.black, Color.red]])
		.action_{
			synthDisp.string = "";
			this.killCurrentSynth;
			showButton.visible_(false);
			busAssignSink.removeButtons;
		};

		showButton = Button.new(view,Rect(50, 68, 45, 16))
		.states_([ [ "Hide", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ], [ "Show", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
		.action_{|v|
			if(v.value==1,{
				synth.hide;
				hidden = true;
			},{
				synth.show;
				hidden = false;
			})
		}
		.visible_(false);

		channelOutBox = ChannelOutBox(view, Rect(0, 68, 45, 16), outBus.index);
	}

	confirmValidBus {arg bus;
		^ModularServers.servers[server.asSymbol].confirmValidBus(bus);
	}

	visible {
		view.visible = true;
	}

	invisisible {
		view.visible = false;
	}

	pause {
		if(synth!=nil,{
			synth.pause;
			synth.hide;
		});
		view.visible = false;
	}

	resume {
		if(synth!=nil,{
			synth.unpause;
			if(hidden.not,{
				synth.show;
			});
		});
		view.visible = true;
	}

	killCurrentSynth {
		if(synth!=nil,{
			synth.killMe;
		});
		mixer.removeAllMixers;
		synth = nil;
		isMixer = false;
	}

	makeNewSynth {arg synthName;
		showButton.visible_(true);
		this.killCurrentSynth;
		if((synthName=="Mixer")||(synthName=="SignalSwitcher")||(synthName=="SignalSwitcher4")||(synthName=="AmpFollower")||(synthName=="SpecMul")||(synthName=="AmpInterrupter")||(synthName=="RingModStereo")||(synthName=="Convolution")||(synthName=="AnalysisFilters")||(synthName=="LucerneVideo")||(synthName=="TVFeedback"),{
			switch(synthName,
				"Mixer",{
					synth = ModularClassList.initMixer(synthGroup, outBus, outBus.index+"Mixer", 4);
				},
				"SignalSwitcher",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"SignalSwitcher4",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"AmpFollower",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"AmpInterrupter",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"SpecMul",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"RingModStereo",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"Convolution",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"AnalysisFilters",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"LucerneVideo",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"TVFeedback",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2;
				},
				"MixerSolo",{
					synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
					synth.init2
				}
			);
			isMixer = true;
		},{
			synth = ModularClassList.initModule(synthName, synthGroup, outBus, setups);
			mixer.outBus = synth.mixerToSynthBus;
			mixer.volBus.set(1);
			isMixer=false;

		})
	}

	setInputBusses {arg inputBussesIn;

		inputBusses = inputBussesIn;

		if((synth!=nil)&&(isMixer.not),{mixer.setInputBusses(inputBussesIn, synth.numBusses)});
	}

	killMe {
		if(synth!=nil,{
			synth.killMe;
		});
		outBus.free;
	}

	save {
		var saveList, temp;

		saveList = List.newClear(0);
		if(synth!=nil, {
			saveList.add(setups); //add setups
			temp = List.newClear(0);
			mixer.inputBusses.do{arg item;
				temp.add(item); //add busses
			};
			saveList.add(temp);
			saveList.add(synth.save);
		});
		^saveList
	}

	load {arg loadArray;
		var temp, soundInBusses, stereoSoundInBusses;

		synthDisp.string = loadArray[2][0];
		this.makeNewSynth(loadArray[2][0]); //load the synth first

		loadArray[0].do{arg item;
			if(setupButtons[ModularServers.setups.indexOfEqual(item.asString)].value!=1, {
				setupButtons[ModularServers.setups.indexOfEqual(item.asString)].valueAction_(1);
			});
		};

		loadArray[1].do{arg item, i;
			var bus, label, index, temp;

			temp = ModularServers.servers[server.asSymbol].busMap[0][item.asSymbol];

			if(temp!=nil,{
				#bus, index=temp;
				busAssignSink.assignBus(bus, "S"++(index).asString);
			},{
				temp = ModularServers.servers[server.asSymbol].busMap[1][item.asSymbol];
				if(temp!=nil,{
					#bus, index=temp;
					busAssignSink.assignBus(bus, "S"++((index*2)).asString++((index*2+1)).asString);
				},{
					bus = ModularServers.servers[server.asSymbol].busMap[2][item.asSymbol];
					if(bus!=nil,{busAssignSink.assignBus(bus, bus)});
				})
			});
		};


		synth.load(loadArray[2]);
	}

}

