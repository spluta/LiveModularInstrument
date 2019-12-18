GreatExpectations_Mod : Module_Mod {

	*initClass {
		StartUp.add {

			SynthDef("greatExpectations_mod", { arg outBus, inBus, verbVol, allpassVol, verbTime, gate = 1, pauseGate = 1;
				var in, in2, verbSig, env, pauseEnv, out;
				var shifties, freq, hasFreq, amp, wave, allpass;

				env = EnvGen.kr(Env.asr(0,1,0.5), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = In.ar(inBus,1);

				allpass = CombC.ar(in*allpassVol, 1, Array.fill(60, {rrand(0.05, 1)}), Array.fill(60, {rrand(5,10)}), LFNoise2.kr(LFNoise2.kr(0.5).range(0.1, 0.2)).range(0,1) );

				allpass = Splay.ar(allpass);

				//amp = Amplitude.kr(in, 0.1, 0.3);

				/*#freq, hasFreq = Pitch.kr(in, 440, 120, 700);

				wave = LFTri.ar(freq*[0.12495, 0.12505], 0, amp);*/

				in2 = PitchShift.ar(in, 0.2, [0.125,0.25], 0, 0.1);

				//in2 = CombC.ar(shifties, 0.1, [ 0.052898466587067, 0.040793769359589, 0.053465979099274, 0.049592454433441, 0.05687507390976, 0.057903006076813 ], 5);

				verbSig = GVerb.ar(Mix.new(in2), 100, verbTime, 0.5, 1, 15, 0, 0.1, 0.1, 100);

				verbSig = LPF.ar(verbSig, 400);

				out = (verbSig*verbVol)+(allpass);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
			//}).load(ModularServers.servers[\lmi0].server);
		}
	}

	init {
		this.makeWindow("GreatExpectations", Rect(500,300,300,40));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);

		synths.put(0, Synth("greatExpectations_mod", [\outBus, outBus, \verbVol, 0, \allpassVol, 0, \verbTime, 5, \inBus, mixerToSynthBus.index], group));


		controls.add(QtEZSlider.new("verbVol", ControlSpec(0,8,'amp'),
			{|v|
				synths[0].set(\verbVol, v.value);
			}, 0, true, 'horz'));
		this.addAssignButton(0, \continuous);

		controls.add(QtEZSlider.new("allpassVol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\allpassVol, v.value);
			}, 0, true, 'horz'));
		this.addAssignButton(1, \continuous);

		controls.add(QtEZSlider.new("verbTime", ControlSpec(1,5),
			{|v|
				synths[0].set(\verbTime, v.value);
			}, 5, true, 'horz'));
		this.addAssignButton(2, \continuous);

		win.layout_(
			VLayout(
				HLayout(controls[0].layout,assignButtons[0].layout),
				HLayout(controls[1].layout,assignButtons[1].layout),
				HLayout(controls[2].layout,assignButtons[2].layout)
			)
		);

	}
		//
		// //multichannel button
		// numChannels = 2;
		// controls.add(Button(win,Rect(10, 275, 60, 20))
		// 	.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
		// 	.action_{|butt|
		// 		switch(butt.value,
		// 			0, {
		// 				numChannels = 2;
		// 				3.do{|i| synths[i+1].set(\gate, 0)};
		// 			},
		// 			1, {
		// 				synths.put(1, Synth("reverbDrone_mod", [\out, outBus.index+2, \bufnum, buffers[2].bufnum, \bufnum2, buffers[3].bufnum, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
		// 				numChannels = 4;
		// 			},
		// 			2, {
		// 				if(numChannels==2,{
		// 					synths.put(1, Synth("reverbDrone_mod", [\out, outBus.index+2, \bufnum, buffers[2].bufnum, \bufnum2, buffers[3].bufnum, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
		// 				});
		// 				synths.put(2, Synth("reverbDrone_mod", [\out, outBus.index+4, \bufnum, buffers[4].bufnum, \bufnum2, buffers[5].bufnum, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
		// 				synths.put(3, Synth("reverbDrone_mod", [\out, outBus.index+6, \bufnum, buffers[6].bufnum, \bufnum2, buffers[7].bufnum, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
		// 				numChannels = 8;
		// 			}
		// 		)
		// 	};
		// );
		// }
}