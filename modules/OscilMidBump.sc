OscilMidBump_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\oscilMidBump_mod, { arg inBus, outBus, lfoSpeed = 0.1, db = 0, vol=0, delayTime=0.01, decayTime=1, pauseGate=1, gate=1;

				var in, env, pauseEnv, lfo0, lfo1, mid0, mid1;

				in = In.ar(inBus, 2);

				lfo0 = LFTri.kr(lfoSpeed, 3, 1500, 1600);
				lfo1 = Lag.kr(LFPulse.kr(lfoSpeed, 0, 0.3), 1);

				mid0 = MidEQ.ar(in, lfo0, 0.3, (lfo1*db));
				mid1 = MidEQ.ar(mid0, 3100-lfo0, 0.3, (1-lfo1)*db);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);

				Out.ar(outBus, mid1*vol*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("OscilMidBump", Rect(900, 500, 190, 230));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(2);

		synths = List.newClear(4);
		synths.put(0, Synth("oscilMidBump_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(EZSlider(win, Rect(65, 5, 60, 160),"lfoTime", ControlSpec(1, 20,\linear),
			{|v|
				synths[0].set(\lfoSpeed, v.value.reciprocal);
			}, 1, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));


		controls.add(EZSlider(win, Rect(125, 5, 60, 160),"db", ControlSpec(0,5,\linear),
			{|v|
				synths[0].set(\db, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(125, 165, 60, 16));

		//multichannel button
//		numChannels = 2;
//		controls.add(Button(win,Rect(5, 230, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						3.do{|i| synths[i+1].set(\gate, 0)};
//					},
//					1, {
//						synths.put(1, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+2, \volBus, volBus], group));
//						numChannels = 4;
//					},
//					2, {
//						if(numChannels==2,{
//							synths.put(1, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+2, \volBus, volBus], group));
//						});
//						synths.put(2, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+4, \volBus, volBus], group));
//						synths.put(3, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+6, \volBus, volBus], group));
//						numChannels = 8;
//					}
//				)
//			};
//		);

	}
}