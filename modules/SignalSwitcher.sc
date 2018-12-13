SignalSwitcher_Mod : Module_Mod {
	var localBusses, synths, controls, soundInBusses, stereoSoundInBusses, location, mainProcessingWindow, mixerGroup, synthGroup, mixerStrips, pulseRate, impulseOn;

	*initClass {
		{
			SynthDef("signalSwither_mod", {arg inBus0, inBus1, outBus, whichSignal = 0, addSecondSignal=0, pulseRate0=0, onBypass=0, gate = 1, pauseGate = 1;
				var in0, in1, env, out, impulse, signal, pauseEnv;

				impulse = Impulse.kr(pulseRate0);

				signal = Lag.kr(Select.kr(whichSignal, [0, 1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.001);

				in0 = In.ar(inBus0, 8);
				in1 = In.ar(inBus1, 8);

				out = (in0*(1-signal))+(in1*signal)+(Lag.kr(addSecondSignal, 0.001)*in1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}.defer(1);
	}

	init {}

	makeWindow2 {arg name, rect;
		win = Window.new(name, rect);
		win.userCanClose_(false);
		win.front;
		modName = name;
	}

	init2 {|soundInBusses, stereoSoundInBusses, location, mainProcessingWindow|

		this.makeWindow("SignalSwitcher",Rect(0, 0, 10+(3*55), 360));
		this.initControlsAndSynths(5);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		2.do{localBusses.add(Bus.audio(group.server, 8))};

		mixerStrips = List.new;
		2.do{arg i;
			mixerStrips.add(ModularMixerStrip(mixerGroup, localBusses[i], setups));
			mixerStrips[i].init2(win, Point(5+(i*55), 0), nil);
		};

		synths.add(Synth("signalSwither_mod", [\inBus0, localBusses[0].index, \inBus1, localBusses[1], \outBus, outBus], synthGroup));

		impulseOn = false;

		controls.add(Button(win, Rect(5, 280, 75, 16))
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

		controls.add(Button(win, Rect(85, 280, 75, 16))
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

		this.addAssignButton(0, \onOff, Rect(5, 300, 75, 16));
		this.addAssignButton(1, \onOff, Rect(85, 300, 75, 16));

		controls.add(Button(win, Rect(5, 320, 75, 16))
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

		controls.add(Button(win, Rect(85, 320, 75, 16))
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


		this.addAssignButton(2, \onOff, Rect(5, 340, 75, 16));
		this.addAssignButton(3, \onOff, Rect(85, 340, 75, 16));

		controls.add(EZRanger(win, Rect(115, 5, 50, 275), "speed", ControlSpec(0.25, 30, 'linear'),
			{arg val;
				pulseRate = val.value;
				if(impulseOn,{
					synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2);
				})
		}, [4, 7], true, layout:\vert));

		win.front;
	}

	addSetup {arg setup;


		oscMsgs.do{arg item, i;
			if(item!=nil, {
				MidiOscControl.addFuncToSetup(group.server, setup, item)
			})
		};

		//also add the setup change to the mixerStrips
		mixerStrips.do{|item| item.addSetup(setup.asSymbol)};
	}

	removeSetup {|setup|
		oscMsgs.do{arg item, i;
			if(item!=nil, {
				MidiOscControl.removeFuncFromSetup(group.server, setup, item)
			})
		};

		mixerStrips.do{|item| item.removeSetup(setup.asSymbol)}
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
		var temp;

		temp = List.newClear(0);
		//save the regular mixers
		mixerStrips.do{arg item;  //save the all setup mixer items
			temp.add(item.save);
		};
		saveArray.add(temp);
	}

	loadExtra {arg loadArray;
		loadArray[4].do{arg item, i;
			mixerStrips[i].load(item);
		};
		mixerStrips.do{arg item; item.unmute;item.unhide};
	}

	killMeSpecial {
		"kill the mixer";
		localBusses.do{arg item; item.free};
		synthGroup.free;
		mixerGroup.free;
	}
}

MixerSolo_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("mixerSolo_mod", {arg inBus, outBus, gate=1, pauseGate=1;
				var env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus,8)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeMixerToSynthBus(8);
		this.initControlsAndSynths(0);

		modName = "MixerSolo";
		synths = List.new;
		synths.add(Synth("mixerSolo_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));
	}

	killMe {
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
		mixerToSynthBus.free;
	}

	show {
	}

	hide {
	}
}

MixerSoloMono_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("mixerSoloMono_mod", {arg inBus, outBus, gate=1, pauseGate=1;
				var env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus,1)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeMixerToSynthBus(1);
		this.initControlsAndSynths(0);
		modName = "MixerSoloMono";
		synths = List.new;
		synths.add(Synth("mixerSoloMono_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));
	}

	killMe {
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
		mixerToSynthBus.free;
	}

	show {
	}

	hide {
	}
}
