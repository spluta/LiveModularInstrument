LowPass_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("lowPass_mod", {arg inBus, outBus, filterFreq=20000, ramp=0.01, gate=1, pauseGate=1;
				var env, out, pauseEnv, sound;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				sound = LPF.ar(In.ar(inBus, 8), filterFreq);

				Out.ar(outBus, sound*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LowPass", Rect(500, 500, 290, 75));

		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("lowPass_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		controls.add(EZSlider(win, Rect(5, 25, 280, 20), "freq", ControlSpec(60, 20000, 'exp'),
			{arg val;
				synths[0].set(\filterFreq, val.value);
			}, 20000, true));
		this.addAssignButton(0,\continuous, Rect(5, 50, 280, 20));
	}
}