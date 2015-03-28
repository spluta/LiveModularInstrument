Combulation_Mod : Module_Mod {
	var volBus, lowRandBus, hiRandBus;

	*initClass {
		StartUp.add {
			SynthDef("comby2_mod", {arg inBus, outBus, volBus, lowRandBus, hiRandBus, gate = 1, pauseGate = 1;
				var in, env, out, vol, lowRand, hiRand, pauseEnv;

				in = In.ar(inBus);

				vol = In.kr(volBus);
				lowRand = 0.0001;
				hiRand = 0.01;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = CombC.ar(in, 0.01, TRand.kr(lowRand, hiRand, Impulse.kr(LFNoise2.kr(2, 5, 20))), 0.2);

				Out.ar(outBus, Pan2.ar(out*env*vol*pauseEnv, SinOsc.kr(Rand(0.5,0.25))));
			}).writeDefFile;

			SynthDef("comby4_mod", {arg inBus, outBus, volBus, lowRandBus, hiRandBus, gate = 1, pauseGate = 1;
				var in, env, out, vol, lowRand, hiRand, pauseEnv;

				in = In.ar(inBus);

				vol = In.kr(volBus);
				lowRand = 0.0001;
				hiRand = 0.01;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = CombC.ar(in, 0.01, TRand.kr(lowRand, hiRand, Impulse.kr(LFNoise2.kr(2, 5, 20))), 0.2);

				Out.ar(outBus, PanAz.ar(4, out*env*vol*pauseEnv, SinOsc.kr(Rand(0.5,0.25))*2));
			}).writeDefFile;

			SynthDef("comby8_mod", {arg inBus, outBus, volBus, lowRandBus, hiRandBus, gate = 1, pauseGate = 1;
				var in, env, out, vol, lowRand, hiRand, pauseEnv;

				in = In.ar(inBus);

				vol = In.kr(volBus);
				lowRand = 0.0001;
				hiRand = 0.01;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = CombC.ar(in, 0.01, TRand.kr(lowRand, hiRand, Impulse.kr(LFNoise2.kr(2, 5, 20))), 0.2);

				Out.ar(outBus, PanAz.ar(8, out*env*vol*pauseEnv, SinOsc.kr(Rand(0.5,0.25))*2));
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("Combulation", Rect(318, 645, 150, 250));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		lowRandBus = Bus.control(group.server);
		hiRandBus = Bus.control(group.server);

		4.do{synths.add(Synth("comby2_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \lowRandBus, lowRandBus.index, \hiRandBus, hiRandBus.index], group))};

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0));
		this.addAssignButton(0,\continuous);

		//multichannel button
		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths.do{arg item; item.set(\gate, 0)};
						4.do{|i| synths.put(i, Synth("comby2_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \lowRandBus, lowRandBus.index, \hiRandBus, hiRandBus.index], group))};
					},
					1, {
						numChannels = 4;
						synths.do{arg item; item.set(\gate, 0)};
						4.do{|i| synths.put(i, Synth("comby4_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \lowRandBus, lowRandBus.index, \hiRandBus, hiRandBus.index], group))};
					},
					2, {
						numChannels = 8;
						synths.do{arg item; item.set(\gate, 0)};
						4.do{|i| synths.put(i, Synth("comby8_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \lowRandBus, lowRandBus.index, \hiRandBus, hiRandBus.index], group))};
					}
				)
			};
		);

		win.layout_(VLayout(
			controls[0].layout, assignButtons[0].layout, controls[1]
			)
		);
		win.layout.spacing = 0;
		win.bounds_(Rect(318, 645, 150, 250));

	}
}