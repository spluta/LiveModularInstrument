CombFilter_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\combFilter_mod, { arg inBus, outBus, vol=0, delayTime=0.01, decayTime=1, pauseGate=1, gate=1;

				var in, env, pauseEnv;

				in = In.ar(inBus);

				in = CombC.ar(in, 0.05, delayTime, decayTime, vol);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);

				Out.ar(outBus, in.dup*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("CombFilter", Rect(900, 500, 190, 230));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);
		synths.put(0, Synth("combFilter_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(EZSlider(win, Rect(65, 5, 60, 160),"freq", ControlSpec(40,10000,\exponential),
			{|v|
				synths[0].set(\delayTime, v.value.reciprocal);
			}, 100, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(1, \continuous, Rect(65, 165, 60, 16));


		controls.add(EZSlider(win, Rect(125, 5, 60, 160),"decTime", ControlSpec(0.1,5,\linear),
			{|v|
				synths[0].set(\decayTime, v.value);
			}, 1, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(2, \continuous, Rect(125, 165, 60, 16));

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

KlankFilter_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\klankFilter_mod, { arg inBus, outBus, i_freq, vol=0, delayTime=0.01, decayTime=1, pauseGate=1, gate=1;

				var in, env, pauseEnv;

				in = In.ar(inBus);

				in = Klank.ar(`[Array.series(60,1,1), Array.fill(60,1/60), Array.fill(60,0.1)
], in, i_freq);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);

				Out.ar(outBus, in.dup*env*pauseEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("KlankFilter", Rect(900, 500, 190, 230));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);
		synths.put(0, Synth("klankFilter_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \i_freq, 100], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(EZNumber(win, Rect(65, 5, 60, 40),"freq", ControlSpec(30,400),
			{|v|
				synths[0].set(\gate, 0);
				synths.put(0, Synth("klankFilter_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \i_freq, v.value], group));
			}, 100, layout:\line2));


		controls.add(EZSlider(win, Rect(125, 5, 60, 160),"decTime", ControlSpec(0.1,5,\linear),
			{|v|
				synths[0].set(\decayTime, v.value);
			}, 1, true, 40, 40, 0, 16, \vert));
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


KlankFilter2_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\klankFilter2_mod, { arg inBus, outBus, i_freq, vol=0, delayTime=0.01, decayTime=1, pauseGate=1, gate=1;

				var in, env, pauseEnv;

				in = In.ar(inBus);

				in = Klank.ar(`[[2341, 1787.7, 1582, 1434.3, 1117.74, 872.9, 809.6, 710.3, 613.2, 483.2, 432.6, 372.2, 327.7, 288.5, 253.2, 221.5, 165, 141.93, 124, 107.3, 58.99].addAll([2341, 1787.7, 1582, 1434.3, 1117.74, 872.9, 809.6, 710.3, 613.2, 483.2, 432.6, 372.2, 327.7, 288.5, 253.2, 221.5, 165, 141.93, 124, 107.3, 58.99]*2), Array.fill(24, 1/24), Array.fill(24,1)
], in);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);

				Out.ar(outBus, in.dup*env*pauseEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("KlankFilter2", Rect(900, 500, 190, 230));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);
		synths.put(0, Synth("klankFilter2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \i_freq, 100], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

	}
}



PinkNoise_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\pinknoise_mod, { arg outBus, vol=0, pauseGate=1, gate=1;

				var env, pauseEnv, out;

				out = PinkNoise.ar([vol, vol]);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("PinkNoise", Rect(900, 500, 190, 230));
		this.initControlsAndSynths(1);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);
		synths.put(0, Synth("pinknoise_mod", [\outBus, outBus, \vol, 0], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

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

