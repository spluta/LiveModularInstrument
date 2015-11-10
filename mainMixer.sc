ModularMixerStrip : Module_Mod {
	var window, win, point, color;
	var assignedChannelsArray, mixer, index, inputBusses, discardBusses, inputBusString, assignInputButton, inputDisplay, assignMidiButton, sepInputBusses, xmlMixerStrip, inBusTemp, inBusTempList, counter, numBusses, temp, busAssignSink, panel, waitForSet, waitForType, soundInBusses, stereoSoundInBusses, controls, assignButtons, mixerGroup, reducerGroup, reducer, transferBus;

	*initClass {
		StartUp.add {
			SynthDef("mixerStripBusReducer_mod", {arg outBus, rotate=0, mulArray = #[1,1,1,1,1,1,1,1];
				var signal;

				signal = In.ar(outBus, 8);

				//mulArray.poll;
				signal = signal*mulArray;

				signal = RotateN.ar(rotate, signal);

				ReplaceOut.ar(outBus, signal);

			}).writeDefFile;
		}
	}

	init {}

	init2 {arg winIn, pointIn, colorIn;

		window = winIn;
		point = pointIn;
		color = colorIn;

		this.initControlsAndSynths(2);

		mixerGroup = Group.tail(group);
		reducerGroup = Group.tail(group);

		//transferBus = Bus.audio(group.server, 8);

		inputBusses = List.new;

		mixer = ModularMixer(mixerGroup);
		//mixer.outBus = transferBus;
		mixer.outBus = outBus;

		//reducer both moves a signal to the correct channel and reduces a signal down to the correct number of channels

		reducer = Synth("mixerStripBusReducer_mod", [\outBus, outBus], reducerGroup);

		//i am using 'win' as the name for the panel because this allows me to use a double assign button for the slider and not rewrite all that code
		panel = CompositeView.new(window, Rect(point.x, point.y, 50, 300));
		win=panel;

		if(color!=nil,{
			win.background_(color)
		});

		busAssignSink=MixerBusAssignSink(this, panel, Point(0,0));

		soundInBusses = ModularServers.getSoundInBusses(group.server);
		stereoSoundInBusses = ModularServers.getStereoSoundInBusses(group.server);

		controls.add(EZSlider(panel,Rect(0, 100, 50, 120),"", ControlSpec(0,1,'amp'), {|v|
			mixer.setVol(v.value);
		}, 0, true, 0, 45, 0, 0, layout:'vert'));

		this.addAssignButton(0, \continuous, Rect(0, 220, 50, 20));

		controls.add(EZPopUpMenu.new(panel, Rect(0, 240, 50, 16), "",
			["M", "S", "4", "8"],
 			{arg pop;
				var mulArray;

				mulArray = [[1,0,0,0,0,0,0,0], [1,1,0,0,0,0,0,0], [1,1,1,1,0,0,0,0], [1,1,1,1,1,1,1,1]].at(pop.value);
				reducer.set(\mulArray, mulArray.postln);
		}, 3, true, 0, 0));

		numBusses = 2;

		controls.add(EZPopUpMenu.new(panel, Rect(0, 260, 50, 16), "",
			Array.fill(8,{arg i; i.asSymbol}),
			{arg pop;
				var rotate;
					rotate = [0,7,6,5,4,3,2,1].at(pop.value);

					reducer.set(\rotate, rotate);
		}, 0, true, 0, 0));
	}

	confirmValidBus {arg bus;
		^ModularServers.servers[group.server.asSymbol].confirmValidBus(bus);
	}

	mute {
		mixer.mute;
	}

	unmute {
		mixer.unmute;
	}

	hide {
		panel.visible = false;
	}

	unhide {
		panel.visible = true;
	}

	setInputBusses {arg inputBussesIn;
		inputBusses = mixer.setInputBusses(inputBussesIn, numBusses);
	}

	updateInputBusGUI { arg inputBussesIn;
		inputBussesIn[0].do{arg item, i; item = "S"++item.asString; inputBussesIn[0].put(i, item)};
		inputBussesIn = inputBussesIn.flatten;
		inputBusString = "";
		inputBussesIn.do{arg item; inputBusString = inputBusString+item.asString};
		inputDisplay.string = inputBusString;
	}

	assignChannel {arg channelIn;
		index = assignedChannelsArray.indexOfEqual(channelIn);
		if(index==nil,{
			assignedChannelsArray.add(channelIn);
			mixer.addBusPair([channelIn, 0]);
		});
	}

	removeChannel {arg channelIn;
		index = assignedChannelsArray.indexOfEqual(channelIn);
		if(index==nil,{
			assignedChannelsArray.removeAt(channelIn);
			mixer.removeBusPair([channelIn, 0]);
		});
	}

	saveExtra {arg saveArray;
		inputBusses.postln;
		saveArray.add(inputBusses);
	}

	loadExtra {arg loadArray;
		loadArray[4].do{arg item, i;
			var bus, label, index, temp;

			temp = ModularServers.servers[group.server.asSymbol].busMap[0][item.asSymbol];

			if(temp!=nil,{
				#bus, index=temp;
				busAssignSink.assignBus(bus, "S"++index.asString);
				},{
					temp = ModularServers.servers[group.server.asSymbol].busMap[1][item.asSymbol];
					if(temp!=nil,{
						#bus, index=temp;
						busAssignSink.assignBus(bus, "S"++((index*2)).asString++((index*2+1)).asString);
						},{
							bus = ModularServers.servers[group.server.asSymbol].busMap[2][item.asSymbol];
							if(bus!=nil,{busAssignSink.assignBus(bus, bus)});
					})
			});
		};
	}

}


MainMixer {
	var group, mixerStrips, window, assignDefaultsButton, midiHidArray, xmlSynth, currentSetup, xmlMixerLayer, numMixers, mixerTransferBus, setupMixerDict, mixerGroup, outGroup, outMixer;

	*new {arg group;
		^super.newCopyArgs(group).init;
	}

	*initClass {
		{
			SynthDef("mainMixerOut_mod", {arg inBus, outBus;
				Out.ar(outBus, In.ar(inBus, 8));
			}).writeDefFile;
		}.defer(1);
	}

	init {
		numMixers = 8;

		window = Window(group.server.name, Rect(0, 0, 10+(numMixers+1*55), 278));
		window.userCanClose_(false);

		mixerGroup = Group.tail(group);
		outGroup = Group.tail(group);

		mixerTransferBus = Bus.audio(group.server, 8);

		outMixer = Synth("mainMixerOut_mod", [\inBus, mixerTransferBus, \outBus, ModularServers.outBus], outGroup);

		setupMixerDict = Dictionary.new();
		ModularServers.setups.do{|setup, i|
			var mixerTemp;

			mixerTemp = List.new;
			((numMixers/2)..numMixers).do{arg i2;
				var strip;

				strip = ModularMixerStrip(mixerGroup, mixerTransferBus, [ModularServers.setups[i]]);
				strip.init2(window, Point(5+(i2*55), 0), ModularServers.setupColors[i]);
				mixerTemp.add(strip);

			};
			setupMixerDict.add(setup.asSymbol->mixerTemp);
		};

		mixerStrips = List.new;
		(numMixers/2).do{arg i2;
			var strip;

			strip = ModularMixerStrip(mixerGroup, mixerTransferBus, ModularServers.setups);
			strip.init2(window, Point(5+(i2*55), 0), nil);
			mixerStrips.add(strip);
		};

		setupMixerDict.do{|mixerStrips| mixerStrips.do{|item|item.mute;item.hide}};
		currentSetup = 'setup0';
		setupMixerDict[currentSetup.asSymbol].do{|item|item.unmute;item.unhide};
		window.front;
	}

	changeSetup {arg setup;
		setupMixerDict[currentSetup.asSymbol].do{|item| item.mute; item.hide};
		currentSetup = setup;
		setupMixerDict[currentSetup.asSymbol].do{|item| item.unmute; item.unhide;};
	}

	killMe {
		"kill the mixer";
		outMixer.free;
		mixerTransferBus.free;
		window.close;
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(window.bounds); //save bounds
		temp = List.newClear(0);
		//save the regular mixers
		mixerStrips.do{arg item;  //save the all setup mixer items
			temp.add(item.save);
		};
		saveArray.add(temp);

		temp = List.newClear(0);
		ModularServers.setups.do{arg setup;  //save the setup-based mixer items
			setupMixerDict[setup.asSymbol].do{arg item;
				temp.add(item.save);
			};
		};
		saveArray.add(temp);
		^saveArray
	}

	load {arg loadArray;
		var counter;

		window.bounds_(loadArray[0]);
		loadArray[1].do{arg item, i;
			mixerStrips[i].load(item);

		};

		counter = 0;
		ModularServers.setups.do{arg setup;
			setupMixerDict[setup.asSymbol].do{arg item;
				item.load(loadArray[2][counter].postln);
				counter = counter+1;
			};
		};
		mixerStrips.do{arg item; item.unmute;item.unhide};
		setupMixerDict[currentSetup.asSymbol].do{|item|item.unmute;item.unhide};
	}

	mute {
		mixerStrips.do{arg item;  //save the all setup mixer items
			item.mute;
		};
		ModularServers.setups.do{arg setup;
			setupMixerDict[setup.asSymbol].do{arg item;
				item.mute;
			};
		};
	}

	unmute {
		mixerStrips.do{arg item;  //save the all setup mixer items
			item.unmute;
		};
		ModularServers.setups.do{arg setup;
			setupMixerDict[setup.asSymbol].do{arg item;
				item.unmute;
			};
		};
	}

	unhide {
		window.visible = true;
	}

	hide {
		window.visible = false;
	}
}

ModularMainMixer : MainMixer {

	init {

	}

	init2 {arg outBus, name, numMixers;
		numMixers = 4;

		window = Window(name, Rect(0, 0, 10+(numMixers+1*55), 280));
		window.userCanClose_(false);

		mixerStrips = List.new;
		numMixers.do{arg i;
			var strip;

			strip = ModularMixerStrip(group, outBus, ModularServers.setups);
			strip.init2(window, Point(5+(i*55), 0), nil);
			mixerStrips.add(strip);
		};

		window.front;
	}

	killMe {
		"kill the mixer";
		outMixer.free;
		mixerTransferBus.free;
		window.close;
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add("Mixer");

		saveArray.add(window.bounds); //save bounds
		temp = List.newClear(0);
		//save the regular mixers
		mixerStrips.do{arg item;  //save the all setup mixer items
			temp.add(item.save);
		};
		saveArray.add(temp);

		^saveArray
	}

	load {arg loadArray;

		window.bounds_(loadArray[1]);
		loadArray[2].do{arg item, i;
			mixerStrips[i].load(item);
		};
		mixerStrips.do{arg item; item.unmute;item.unhide};
	}

	addSetup {|setup|
		mixerStrips.do{|item| item.addSetup(setup.asSymbol)}
	}

	removeSetup {|setup|
		mixerStrips.do{|item| item.removeSetup(setup.asSymbol)}
	}

	pause {
		mixerStrips.do{|item| item.mute};
	}

	unpause {
		mixerStrips.do{|item| item.unmute};
	}

	show {
		window.visible = true;
	}

	hide {
		window.visible = false;
	}
}

