Gain_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("gain_mod", {arg inBus, outBus, vol, gate = 1, pauseGate = 1;
				var in, env, pauseEnv;

				in = In.ar(inBus,2);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, in*env*vol*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Gain", Rect(318, 645, 150, 250));
		this.initControlsAndSynths(1);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("gain_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 1], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,8,'amp'),
			{|v|
			synths[0].set(\vol, v.value)
			}, 1));
		this.addAssignButton(0,\continuous);

		win.layout_(VLayout(
			controls[0].layout, assignButtons[0].layout
			)
		);
		win.layout.spacing = 0;
		win.bounds_(Rect(318, 645, 150, 250));

	}
}