AmpFollower_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("ampFollower_mod", {arg inBus0, inBus1, outBus, ampMult = 1, whichState = 0, gate = 1, pauseGate = 1;
				var in0, in1, amp, env, out, impulse, dust, whichEnv, pauseEnv;

				in0 = In.ar(inBus0, 2);
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

	init3 {
		this.initControlsAndSynths(2);

		synthName = "AmpFollower";

		synths.add(Synth("ampFollower_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], outGroup));

		controls.add(QtEZSlider("ampMult", ControlSpec(0.0,8.0,\amp),
			{|v|
				synths[0].set(\ampMult, v.value);
		}, 1, true, \vert));
		controls[0].maxWidth_(40);
		this.addAssignButton(0, \continuous);
		assignButtons[0].instantButton.maxWidth_(40);

		controls.add(Button().maxHeight_(15)
			.states_([["Thru", Color.blue, Color.black],["Gate", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\whichState, butt.value);
			})
		);
		this.addAssignButton(1, \onOff);

		win.name_(outBus.index.asString+"AmpFollower");
		win.layout_(
			VLayout(
				HLayout(mixerStrips[0].panel, mixerStrips[1].panel, VLayout(controls[0], assignButtons[0])),
			HLayout(controls[1], assignButtons[1])
		).margins_(0!4).spacing_(0));
	win.front;
}
}


AmpInterrupter_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("ampInterrupter_mod", {arg inBus0, inBus1, outBus, thresh = 1, whichState = 0, gate = 1, pauseGate = 1;
				var in0, in1, amp, env, out, impulse, dust, processOn, pauseEnv;

				in0 = In.ar(inBus0, 2);
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

		this.makeWindow("AmpInterrupter");
		this.initControlsAndSynths(2);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		localBusses.add(Bus.audio(group.server, 2));
		localBusses.add(Bus.audio(group.server, 1));

		mixerStrips = List.new;
		2.do{arg i; mixerStrips.add(QtDiscreteInput_Mod(mixerGroup, localBusses[i]))};

		synths.add(Synth("ampInterrupter_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], synthGroup));

		controls.add(QtEZSlider("thresh", ControlSpec(0.0,0.25),
			{|v|
				synths[0].set(\thresh, v.value);
		}, 0.2, true, \horz));

		controls.add(Button()
			.states_([["Thru", Color.blue, Color.black],["On", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\whichState, butt.value);
			})
		);
		this.addAssignButton(1, \onOff);

		win.layout_(
			VLayout(
				HLayout(mixerStrips[0].panel, mixerStrips[1].panel, controls[0]),
				HLayout(controls[1], assignButtons[1])
		).margins_(0!4).spacing_(0));
		win.front;
	}
}