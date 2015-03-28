AutoTune_Mod : Module_Mod {

	*initClass {
		StartUp.add {

			SynthDef("autoTune_mod", { |inBus, outBus, whichScale = 0, thresh = 0.05, tuneVol=1, synthVol=1, gate = 1, pauseGate = 1|
				var baseFreq = 100;
				var in, freq, hasFreq, out, synth, amp, env, pauseEnv, cScale, scale, vocoder;
				var sig, ampSig;

				in = In.ar(inBus);

				//in = DelayC.ar(In.ar(inBus), 0.5, 0.5);

				# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.02, median: 7);
				baseFreq = freq.cpsmidi.round.midicps;



				cScale = DegreeToKey.kr(LocalBuf.newFrom([0,0,2,2,2,2,4,4,5,5,5,5,7,7,7,7,9,9,9,9,11,11,12,12]), freq.cpsmidi*2, 12, 1);

				baseFreq = Select.kr(whichScale, [baseFreq, cScale.midicps]);

				amp = Amplitude.kr(in,0.01,0.1);

				synth = LFTri.ar([baseFreq, baseFreq], 0, Lag.kr(amp, 0.3));
				amp = Lag.kr((amp>thresh),0.1);


				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus,synth)
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("AutoTune", Rect(318, 645, 265, 250));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		synths = List.new;

		synths.add(Synth("autoTune_mod",[\inBus, mixerToSynthBus, \outBus, outBus, \thresh, 0.05, \tuneVol, 1, \synthVol, 1], group));

		controls.add(EZSlider.new(win,Rect(5, 5, 80, 220), "thresh", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\thresh, v.value)
			}, 0.02, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(5, 230, 80, 20));

		controls.add(EZSlider.new(win,Rect(90, 5, 80, 220), "synthVol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\tuneVol, v.value)
			}, 1, layout:\vert));
		this.addAssignButton(1,\continuous,Rect(90, 230, 80, 20));

		controls.add(EZSlider.new(win,Rect(175, 5, 80, 220), "vocVol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\synthVol, v.value)
			}, 1, layout:\vert));
		this.addAssignButton(2,\continuous,Rect(175, 230, 80, 20));

		controls.add(Button(win, Rect( 5, 255, 120, 20))
			.states_([
				["chrom", Color.black, Color.red],
				["c major", Color.red, Color.black]
			])
			.action_({arg butt;
				synths[0].set(\whichScale, butt.value);
			})
		);
		this.addAssignButton(3,\onOff,Rect(130, 255, 120, 20),);

//		//multichannel button
//		numChannels = 2;
//		controls.add(Button(win,Rect(10, 255, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						if(synths.size>4,{
//							synths[4..].do{arg item; item.set(\gate, 0)};
//						});
//						synths.do{arg item, i; item.set(\outBus, outBus.index+(i%2))};
//					},
//					1, {
//						numChannels = 4;
//						synths.do{arg item, i; item.set(\outBus, outBus.index+i)};
//					},
//					2, {
//						numChannels = 8;
//						synths.do{arg item, i; item.set(\outBus, outBus.index+i)};
//						4.do{|i|synths.add(Synth("delayFeedback_mod",[\inBus, outBus.index+(i%2), \outBus, outBus.index+i, \volBus, volBus.index], synthGroup))};
//					}
//				)
//			};
//		);

	}
}