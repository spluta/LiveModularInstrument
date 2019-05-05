Mute_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("mute_mod", {arg inBus, outBus, mute=1, ramp=0.01, gate=1, pauseGate=1;
				var env, out, pauseEnv, muteEnv;

				muteEnv = EnvGen.kr(Env.asr(ramp, 1, ramp), mute, doneAction:0);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 8)*env*muteEnv*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Mute", Rect(500, 500, 290, 75));

		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("mute_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		controls.add(Button(win, Rect(5, 5, 280, 20))
			.states_([["mute", Color.blue, Color.black],["on", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\mute, (butt.value).abs)
			})
		);

		this.addAssignButton(0,\onOff, Rect(5, 25, 280, 20));

		controls.add(EZSlider(win, Rect(5, 50, 280, 20), "ramp", ControlSpec(0.01, 0.25, 'linear'),
			{arg val;
				synths[0].set(\ramp, val.value);
			}, 0.01, true));
	}
}