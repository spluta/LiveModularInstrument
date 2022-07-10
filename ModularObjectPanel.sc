ChannelOutBox {
	var <>outBus, <>view, <>rect, <>channelOutBox;

	*new {arg outBus;
		^super.newCopyArgs(outBus).init;
	}


	init {
		channelOutBox = DragSource(view,rect).font_(Font("Helvetica",10)).maxWidth_(20).maxHeight_(15);
		channelOutBox.object = [outBus, outBus.asString];
		channelOutBox.string = outBus.asString;
		channelOutBox.dragLabel = outBus.asString;
	}
}

ModularObjectPanel {
	var <>server, <>group, outBusIndex, panel, point;

	var <>busAssignSink, inputBusString, synth, mixerGroup, synthGroup, lastSynth, internalBusses, mixer, sepInputBusses, index, xmlSynth, counter, inBusTemp, inBusTempList, isMixer, isRouter, synthAssignSink, synthDisp, synthKill, setupTemp;

	var showButton, hidden, channelOutBox, <>view, setupButtons, inputBusses;

	*new {arg server, group, outBusIndex;
		^super.newCopyArgs(server, group, outBusIndex).init;
	}

	init {
		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		mixer = ModularMixer(mixerGroup);


		isMixer = false;
		isRouter = false;

		view = CompositeView.new().minWidth_(80).minHeight_(60).maxWidth_(80).maxHeight_(60);

		view.background_(Color.rand);
		//view.visible = visible;
		hidden = false;

		busAssignSink = QtBusAssignSink(this, 4);

		synthAssignSink = DragSink().font_(Font("Helvetica",8)).maxWidth_(20).maxHeight_(15).align_(\center);
		synthAssignSink.string="Syn";
		synthAssignSink.background_(Color.yellow);
		synthAssignSink.receiveDragHandler={
			if(ModularClassList.checkSynthName(View.currentDrag.asString),{
				synthDisp.string = View.currentDrag.asString;
				this.makeNewSynth(View.currentDrag.asString);
			});
		};
		synthDisp = StaticText()
		.maxWidth_(45).maxHeight_(15).minWidth_(40).minHeight_(15)
		.font_(Font("Helvetica",8)).align_(\left);
		synthKill = Button().maxWidth_(15).maxHeight_(15).minWidth_(15)
		.states_([["k", Color.black, Color.red]])
		.action_{
			synthDisp.string = "";
			this.killCurrentSynth;
			busAssignSink.removeButtons;
		};

		showButton = Button.new().maxHeight_(15).maxWidth_(40).font_(Font("Helvetica",10))
		.states_([ [ "Hide", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ], [ "Show", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
		.action_({|v|
			if(v.value==1,{
				synth.hide;
				hidden = true;
			},{
				synth.show;
				hidden = false;
			})
		})
		.visible_(true);
		showButton.value_(1);

		channelOutBox = ChannelOutBox(outBusIndex+1);

		view.layout_(
			VLayout(
				[busAssignSink.panel,align:\topLeft],
				HLayout([synthAssignSink,align:\left], synthDisp, [synthKill,align:\right]),
				HLayout([channelOutBox.channelOutBox,align:\bottomLeft], [showButton,align:\bottomRight])
			).margins_(2!4).spacing_(0)
		)
	}

	confirmValidBus {arg bus;
		^ModularServers.servers[server.asSymbol].confirmValidBus(bus);
	}

	visible {
		view.visible = true;
	}

	invisible {
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
			synth = ModularClassList.initModule(synthName, synthGroup, ModularServers.getObjectBusses(group.server).at(outBusIndex));
			isMixer = true;
		},{
			if(ModularClassList.checkSynthName(synthName),{
				synth = ModularClassList.initModule(synthName, synthGroup, ModularServers.getObjectBusses(group.server).at(outBusIndex));
				mixer.outBus = synth.mixerToSynthBus;
				mixer.volBus.set(1);
				isMixer=false;
			})
		})
	}

	sendGUIVals {
		if(synth!=nil,{
			synth.sendGUIVals;
		})
	}

	setInputBusses {arg inputBussesIn;

		inputBusses = inputBussesIn;

		if((synth!=nil)&&(isMixer.not)){mixer.setInputBusses(inputBussesIn)};

	}

	killMe {
		if(synth!=nil,{
			synth.killMe;
		});
	}

	save {
		var saveList;

		saveList = List.newClear(0);
		if(synth!=nil, {
			saveList.add(busAssignSink.busInLabels);
			saveList.add(synth.save);
			saveList.add(hidden);
		});
		^saveList
	}

	load {arg loadArray;
		var temp, soundInBusses, synthName;

		synthName = loadArray[1][0];
		if(synthName!=nil,{
			synthDisp.string = loadArray[1][0];
			this.makeNewSynth(loadArray[1][0]); //load the synth first

			//load the busses
			loadArray[0].do{arg item, i;
				busAssignSink.assignBus(item);
			};


			synth.load(loadArray[1]);
			AppClock.sched(2, {
				if(loadArray[2]!=nil,{
					if(loadArray[2]==false,{
						{showButton.valueAction_(0)}.defer;
					},{
						{showButton.valueAction_(1)}.defer
					});
				});
			});
		})
	}

}

