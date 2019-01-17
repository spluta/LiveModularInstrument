ModularMixerStrip : Module_Mod {
	var window, win, point, color;
	var assignedChannelsArray, mixer, index, inputBusses, discardBusses, inputBusString, assignInputButton, inputDisplay, assignMidiButton, sepInputBusses, xmlMixerStrip, inBusTemp, inBusTempList, counter, numBusses, temp, busAssignSink, panel, waitForSet, waitForType, soundInBusses, stereoSoundInBusses, controls, assignButtons, mixerGroup, reducerGroup, reducer, transferBus, rms, localResponder, transferSynth;

	*initClass {
		StartUp.add {
			SynthDef("mixerStripBusReducer_mod", {arg outBus, rotate=0, mulArray = #[1,1,1,1,1,1,1,1];
				var signal;

				signal = In.ar(outBus, 8);
				signal = signal*mulArray;

				signal = RotateN.ar(rotate, signal);

				SendPeakRMS.kr(Mix(signal), Rand(1,1.1), 3, '/stripVol', outBus);

				ReplaceOut.ar(outBus, signal);

			}).writeDefFile;

			SynthDef("transferSynth_mod", {arg transferBus, outBus;

				Out.ar(outBus, In.ar(transferBus,8));

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

		transferBus = Bus.audio(group.server, 8);

		inputBusses = List.new;

		mixer = ModularMixer(mixerGroup);
		mixer.outBus = transferBus;
		//mixer.outBus = outBus;


		//reducer both moves a signal to the correct channel and reduces a signal down to the correct number of channels

		reducer = Synth("mixerStripBusReducer_mod", [\outBus, transferBus], reducerGroup);

		transferSynth = Synth("transferSynth_mod", [\transferBus, transferBus, \outBus, outBus], reducerGroup, 'addToTail');

		//i am using 'win' as the name for the panel because this allows me to use a double assign button for the slider and not rewrite all that code
		panel = CompositeView.new(window, Rect(point.x, point.y, 50, 300));
		win=panel;

		if(color!=nil,{
			win.background_(color)
		});

		busAssignSink=MixerBusAssignSink(this, panel, Point(0,0));

		soundInBusses = ModularServers.getSoundInBusses(group.server);
		stereoSoundInBusses = ModularServers.getStereoSoundInBusses(group.server);

		controls.add(EZSlider(panel,Rect(0, 100, 40, 120),"", ControlSpec(0,1,'amp'), {|v|
			mixer.setVol(v.value);
		}, 0, true, 0, 45, 0, 0, layout:'vert'));

		this.addAssignButton(0, \continuous, Rect(0, 220, 50, 20));

		controls.add(EZPopUpMenu.new(panel, Rect(0, 240, 50, 16), "",
			["M", "S", "4", "8"],
 			{arg pop;
				var mulArray;

				mulArray = [[1,0,0,0,0,0,0,0], [1,1,0,0,0,0,0,0], [1,1,1,1,0,0,0,0], [1,1,1,1,1,1,1,1]].at(pop.value);
				reducer.set(\mulArray, mulArray);
		}, 3, true, 0, 0));

		numBusses = 2;

		controls.add(EZPopUpMenu.new(panel, Rect(0, 260, 50, 16), "",
			Array.fill(8,{arg i; i.asSymbol}),
			{arg pop;
				var rotate;
					rotate = [0,7,6,5,4,3,2,1].at(pop.value);

					reducer.set(\rotate, rotate);
		}, 0, true, 0, 0));

		//set up the volume strip indicator
		rms = LevelIndicator(panel, Rect(40,100,10,120));

		localResponder = OSCFunc({ |msg|
			{
				rms.value = msg[4].ampdb.linlin(-40, 0, 0, 1);
				rms.peakLevel = msg[3].ampdb.linlin(-40, 0, 0, 1);
			}.defer;
	}, '/stripVol', group.server.addr, nil, [reducer.nodeID]);
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

	killMeSpecial {
		localResponder.free;
	}

}