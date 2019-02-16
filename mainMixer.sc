QtModularMixerStrip : Module_Mod {
	var <>location, <>parent, assignedChannelsArray, mixer, index, inputBusses, discardBusses, inputBusString, assignInputButton, inputDisplay, assignMidiButton, sepInputBusses, xmlMixerStrip, inBusTemp, inBusTempList, counter, numBusses, temp, busAssignSink, <>panel, waitForSet, waitForType, controls, assignButtons, mixerGroup, reducerGroup, reducer, transferBus, rms, localResponder, transferSynth, busGuiArray, isMainMixer;

	*initClass {
		StartUp.add {

			SynthDef("transferSynthB_mod", {arg transferBus, outBus;
				var signal;

				signal = In.ar(transferBus, 2);

				SendPeakRMS.kr(Mix(signal), 10.0, 3, "/stripVol", transferBus);

				Out.ar(outBus, signal);

			}).writeDefFile;
		}
	}

	init {}

	init2 {arg isMainMixer=false;

		"outBus".postln;
		outBus.postln;

		this.initControlsAndSynths(2);

		mixerGroup = Group.head(group);
		reducerGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 2);

		inputBusses = List.new;

		mixer = ModularMixer(mixerGroup);
		mixer.outBus = transferBus;

		transferSynth = Synth.tail(reducerGroup, "transferSynthB_mod", [\transferBus, transferBus, \outBus, outBus]);

		panel = CompositeView().maxWidth_(70).maxHeight_(150);

		busAssignSink=QtBusAssignSink(this, 1);

		this.init3(isMainMixer);
	}

	init3 {arg isMainMixer;

		//replace the 2 channel transferSynth with a 22 channel synth
		//transferSynth.free;
		//transferSynth = Synth.tail(reducerGroup, "transferSynthB22_mod", [\transferBus, transferBus, \outBus, outBus]);

		location = 1;

		controls.add(QtEZSlider(nil, ControlSpec(0,1,'amp'), {|v|
			mixer.setVol(v.value);
		}, 0, true, 'vert', false, false));

		this.addAssignButton(0, \continuous);

		outBus.postln;
		controls.add(NumberBox().clipLo_(1).clipHi_(22)
			.action_{arg box;
				parent.setOutBus(location, box.value);
				//synths[location].set(\outBus, outBus.index+(box.value-1)) //set the out of the parent to the correct channel
			};
		);

		//don't let the user change the channel if it isn't the main mixer
		if(isMainMixer.not,{controls[1].visible_(false)});

		rms = LevelIndicator().maxWidth_(10);

		localResponder = OSCFunc({ |msg|
			{
				rms.value = msg[4].ampdb.linlin(-40, 0, 0, 1);
				rms.peakLevel = msg[3].ampdb.linlin(-40, 0, 0, 1);
			}.defer;
		}, '/stripVol', group.server.addr, nil, [transferSynth.nodeID]);

		panel.layout_(
			VLayout(
				HLayout([busAssignSink.panel, align:\top], controls[0].layout, rms).margins_(0!4).spacing_(0),
				HLayout(20, assignButtons[0].layout.maxWidth_(20), controls[1].maxWidth_(20).maxHeight_(15)).margins_(0!4).spacing_(0)
			).margins_(0!4).spacing_(0)
		)
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
		inputBusses = mixer.setInputBusses(inputBussesIn);
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
		saveArray.add(busAssignSink.busInLabels.postln);
	}

	loadExtra {arg loadArray;
		loadArray.do{arg item, i;
			busAssignSink.assignBus(item);
		};
	}

	killMe {
		localResponder.free;
		group.freeAll;
	}

}




MainMixer : Module_Mod {
	var <>mixerTransferBus, <>numMixers, isMainMixer, mixerStrips, win, assignDefaultsButton, numMixers, mixerGroup, outGroup, name, localBusses, mixerTransfer, synthName;

	*initClass {
		StartUp.add {

			SynthDef("mixerTransfer_mod", {arg inBus, outBus, gate=1;
				var signal, env;

				signal = In.ar(inBus, 2);

				env = EnvGen.kr(Env.asr(0,1,0), gate);

				Out.ar(outBus, signal*env);

			}).writeDefFile;
		}
	}

	init {}

	init2 {arg numMixersIn=4, isMainMixerIn=true;

		synths = List.newClear(0);

		numMixers = numMixersIn;
		isMainMixer = isMainMixerIn;

		[group, outBus, numMixers, isMainMixer].postln;

		if(isMainMixer==true,{
			name = group.server.name++" Main Out"
		},{
			name = "Mix"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
		});

		win = Window(name, ((70*numMixers)@150), false);
		win.userCanClose_(false);

		mixerGroup = Group.tail(group);
		outGroup = Group.tail(group);

		localBusses = Array.fill(numMixers, {Bus.audio(group.server, 2)});

		mixerStrips = List.newClear(0);

		(numMixers).do{arg i;
			var strip;

			strip = QtModularMixerStrip(mixerGroup, localBusses[i]);
			strip.init2(isMainMixer);
			mixerStrips.add(strip);
		};

		this.init3;
	}

	init3 {
		synthName = "Mixer";

		//set the MixerStrips to know their location

		numMixers.do{arg i;
			synths.add(Synth("mixerTransfer_mod", [\inBus, localBusses[i], \outBus, outBus], outGroup));
			mixerStrips[i].location_(i);
			mixerStrips[i].parent_(this);
		};

		win.layout_(HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0));
		win.front;
	}

	setOutBus {arg location, val;
		synths[location].set(\outBus, outBus.index+val-1);
	}

	saveExtra {}//these are here just so the SignalSwitcher can inherit
	loadExtra {}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(synthName);

		saveArray.add(win.bounds.postln); //save bounds
		temp = List.newClear(0);
		//save the regular mixers
		mixerStrips.do{arg item;  //save the all setup mixer items
			temp.add(item.save);
		};
		saveArray.add(temp);

		this.saveExtra(saveArray);

		^saveArray
	}

	load {arg loadArray;
		var counter;

		loadArray.postln;

		win.bounds_(loadArray[1]);
		loadArray[2].size.postln;
		loadArray[2].do{arg item, i;
			mixerStrips[i].load(item);

		};
		this.loadExtra(loadArray[3]);
	}


	killMeSpecial {
		"kill the mixer";
		mixerStrips.do{arg item; item.killMe};
		localBusses.do{arg item; item.free};
	}

	mute {
		mixerStrips.do{arg item;  //save the all setup mixer items
			item.mute;
		};
	}

	unmute {
		mixerStrips.do{arg item;  //save the all setup mixer items
			item.unmute;
		};
	}

	unhide {
		win.visible = true;
	}

	hide {
		win.visible = false;
	}
}


ModularMainMixer : MainMixer {


	pause {
		mixerStrips.do{|item| item.mute};
	}

	unpause {
		mixerStrips.do{|item| item.unmute};
	}

	show {
		win.visible = true;
		win.front;
	}
}

SignalSwitcher_Mod : ModularMainMixer {
	var pulseRate, impulseOn;

	*initClass {
		{
			SynthDef("signalSwitcher_mod", {arg inBus0, inBus1, outBus, whichSignal = 0, addSecondSignal=0, pulseRate0=0, onBypass=0, gate = 1, pauseGate = 1;
				var in0, in1, env, out, impulse, signal, pauseEnv;

				impulse = Impulse.kr(pulseRate0);

				signal = Lag.kr(Select.kr(whichSignal, [0, 1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.001);

				in0 = In.ar(inBus0, 2);
				in1 = In.ar(inBus1, 2);

				out = (in0*(1-signal))+(in1*signal)+(Lag.kr(addSecondSignal, 0.001)*in1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}.defer(1);
	}

	init {}

	init3 {
		synthName = "SignalSwitcher";

		win.name = "SS"++(ModularServers.getObjectBusses(ModularServers.servers[\lmi0].server).indexOf(outBus)+1);

		this.initControlsAndSynths(5);

		synths.add(Synth("signalSwither_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], outGroup));

		impulseOn = false;

		controls.add(Button()
			.states_([["left", Color.blue, Color.black],["left", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				synths[0].set(\pulseRate0, 0, \whichSignal, 0, \addSecondSignal, 0);
				butt.value_(1);
				controls[1].value_(0);
				controls[2].value_(0);
				controls[3].value_(0);
			})
		);

		controls.add(Button()
			.states_([["right", Color.blue, Color.black],["right", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				synths[0].set(\pulseRate0, 0, \whichSignal, 1, \addSecondSignal, 0);
				butt.value_(1);
				controls[0].value_(0);
				controls[2].value_(0);
				controls[3].value_(0);
			})
		);

		controls[0].value = 1;

		this.addAssignButton(0, \onOff);
		this.addAssignButton(1, \onOff);

		controls.add(Button()
			.states_([["impulse", Color.blue, Color.black],["impulse", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = true;
				synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2, \whichSignal, 2, \addSecondSignal, 0);
				butt.value_(1);
				controls[0].value_(0);
				controls[1].value_(0);
				controls[3].value_(0);
			})
		);

		controls.add(Button()
			.states_([["both", Color.blue, Color.black],["both", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				synths[0].set(\pulseRate0, 0, \whichSignal, 0, \addSecondSignal, 1);
				butt.value_(1);
				controls[0].value_(0);
				controls[1].value_(0);
				controls[2].value_(0);
			})
		);


		this.addAssignButton(2, \onOff);
		this.addAssignButton(3, \onOff);

		controls.add(QtEZRanger("speed", ControlSpec(0.25, 30, 'linear'),
			{arg val;
				pulseRate = val.value;
				if(impulseOn,{
					synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2);
				})
		}, [4, 7], true, \horz));

		win.layout_(
			VLayout(
				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
				HLayout(controls[0].maxWidth_(70).maxHeight_(15), controls[1].maxWidth_(70).maxHeight_(15)),
				HLayout(assignButtons[0].layout, assignButtons[1].layout),
				HLayout(controls[2].maxWidth_(70).maxHeight_(15), controls[3].maxWidth_(70).maxHeight_(15)),
				HLayout(assignButtons[2].layout, assignButtons[3].layout),
				controls[4].layout
			).margins_(0!4).spacing_(0)
		);
		win.view.bounds_((70*numMixers)@240);
		win.front;
	}

	pause {
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
		mixerStrips.do{|item| item.mute};
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
		mixerStrips.do{|item| item.unmute};
	}

	saveExtra {arg saveArray;
		var temp, tempArray;

		tempArray = List.newClear(0);

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		tempArray.add(temp);  //controller messages
		tempArray.add(oscMsgs);

		saveArray.add(tempArray);
	}

	loadExtra {arg loadArray;

		loadArray[0].do{arg controlLevel, i;
			//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
			if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
				controls[i].valueAction_(controlLevel);
			});
		};

		loadArray[1].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
				assignButtons[i].instantButton.value_(1);
			})
		};
	}

}