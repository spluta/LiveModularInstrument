LowPass_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("lowPass_mod", {arg inBus, outBus, filterFreq=20000, which=0, gate=1, pauseGate=1;
				var env, out, pauseEnv, sound, filt0, filt1, filt2;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				sound = In.ar(inBus, 2);

				filt0 = LPF.ar(sound, filterFreq);
				filt1 = LPF.ar(filt0, 400);
				filt2 = LPF.ar(filt1, 400);
				sound=SelectX.ar(which, [
					filt0,
					filt1,
					filt2
				]);

				Out.ar(outBus, sound*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LowPass");

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("lowPass_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		controls.add(QtEZSlider("freq", ControlSpec(60, 20000, 'exp'),
			{arg val;
				synths[0].set(\filterFreq, val.value);
		}, 20000, true, 'horz'));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider("slope", ControlSpec(0, 2, 'lin', 0.1),
			{arg val;
				synths[0].set(\which, val.value);
			}, 0, false, 'horz'));
		this.addAssignButton(1,\continuous);

		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout)
			)
		);
	}
}