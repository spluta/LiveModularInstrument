AmpFollower_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("ampFollower_mod", {arg inBus0, inBus1, outBus, ampMult = 1, whichState = 0, gate = 1, pauseGate = 1;
				var in0, in1, amp, env, out, impulse, dust, whichEnv, pauseEnv;

				in0 = In.ar(inBus0, 8);
				in1 = In.ar(inBus1, 1);
				amp = LagUD.kr(Amplitude.kr(in1)*ampMult, 0.05, 0.15);

				whichEnv =Select.kr(whichState, [1, amp]);

				out = (in0*whichEnv);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init2 {

		this.makeWindow("AmpFollower",Rect(500, 500, 10+(2*55), 300));
		this.initControlsAndSynths(2);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		localBusses.add(Bus.audio(group.server, 8));
		localBusses.add(Bus.audio(group.server, 1));
		localBusses.postln;

		mixerStrips = List.new;
		2.do{arg i;
			mixerStrips.add(DiscreteInput_Mod(mixerGroup, localBusses[i], setups));
			mixerStrips[i].init2(win, Point(5+(i*55), 0))
		};

		synths.add(Synth("ampFollower_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], synthGroup));

		controls.add(EZSlider(win, Rect(55, 60, 50, 180),"ampMult", ControlSpec(0.0,8.0,\amp),
			{|v|
				synths[0].set(\ampMult, v.value);
		}, 1, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(55, 240, 50, 16));

		controls.add(Button(win, Rect(5, 260, 100, 20))
			.states_([["Thru", Color.blue, Color.black],["Gate", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\whichState, butt.value);
			})
		);
		this.addAssignButton(1, \onOff, Rect(5, 280, 100, 20));

		win.front;
	}
}

AmpInterrupter_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("ampInterrupter_mod", {arg inBus0, inBus1, outBus, thresh = 1, whichState = 0, gate = 1, pauseGate = 1;
				var in0, in1, amp, env, out, impulse, dust, processOn, pauseEnv;

				in0 = In.ar(inBus0, 8);
				in1 = In.ar(inBus1, 1);

				amp = LagUD.kr(Amplitude.kr(in1), 0.05, 0.15);

				processOn = Select.kr(whichState, [0, amp]);

				out = Select.ar((processOn>thresh), [in0, in1]);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init2 {

		this.makeWindow("AmpInterrupter",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(2);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		localBusses.add(Bus.audio(group.server, 8));
		localBusses.add(Bus.audio(group.server, 1));
		localBusses.postln;

		mixerStrips = List.new;
		2.do{arg i; mixerStrips.add(DiscreteInput_Mod(mixerGroup, localBusses[i], win, Point(5+(i*55), 0), nil))};

		synths.add(Synth("ampInterrupter_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], synthGroup));

		controls.add(EZSlider(win, Rect(0, 60, 180, 40),"thresh", ControlSpec(0.0,0.25),
			{|v|
				synths[0].set(\thresh, v.value);
		}, 0.2, true, 40, 40, 0, 16, \horz));

		controls.add(Button(win, Rect(0, 100, 180, 20))
			.states_([["Thru", Color.blue, Color.black],["On", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\whichState, butt.value);
			})
		);
		this.addAssignButton(1, \onOff, Rect(0, 120, 180, 20));

		win.front;
	}
}