Sand_Mod : Module_Mod {
	var volBus;

	*initClass {
		StartUp.add {
			SynthDef(\sand_mod, { arg inBus, outBus, volBus, smallGate=1, pauseGate=1, gate=1;
				var inA1, inA2, chainA1, chainA2, inB, inC, inB1, inB2, inB3, chainB, chain1, chain2, chain3, chain4, amp, out, out1, out2, thresh;
				var smallEnv, env, pauseEnv, volume;

				volume = In.kr(volBus);

				smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), smallGate);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				inB = In.ar(inBus)*smallEnv;

				amp = LagUD.kr(Amplitude.kr(inB)*smallEnv*pauseEnv, 0.01, LFNoise2.kr(0.5, 1, 1.75));

				#inA1, inA2 = Dust.ar([3000,3000]*amp, 0.5);

				inC = Dust.ar([300,300]*amp, 0.5)*amp;

				inC = BPF.ar(inC, LFNoise2.kr(0.3, 1000, 8000), 0.2).dup.dup;

				inB = AllpassC.ar(inB, 0.5, LFNoise2.kr(1, 0.05, 0.1), 1.0)+AllpassC.ar(inB, 0.5, LFNoise2.kr(1, 0.05, 0.1), 1.0);

				chainA1 = FFT(LocalBuf(2048), inA1);
				chainA2 = FFT(LocalBuf(2048), inA2);
				chainB = FFT(LocalBuf(2048), inB);

				chain1 = PV_MagMul(chainA1, chainB);
				chain2 = PV_MagMul(chainA2, chainB);

				chain3 = PV_Copy(chain1, LocalBuf(2048));
				chain4 = PV_Copy(chain2, LocalBuf(2048));

				chain1 = PV_BrickWall(chain1, 0.05);
				chain2 = PV_BrickWall(chain2, 0.05);

				chain3 = PV_MagSmear(PV_BrickWall(chain3, -0.05), 10);
				chain4 = PV_MagSmear(PV_BrickWall(chain4, -0.05), 10);

				out1 = 0.5 * [IFFT(chain1), IFFT(chain2)];

				out2 = 0.5 * [IFFT(chain3), IFFT(chain4)];

				out = out1+DelayC.ar(out1, 0.05, LFNoise2.kr(1, 0.005, 0.025))+DelayC.ar(out2, 0.05, LFNoise2.kr(1, 0.005, 0.025));

				thresh = 0.1;

				out = LPF.ar((out+(inC*0.5)), 11000);


				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				Out.ar(outBus, out*volume*env);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Sand", Rect(900, 500, 70, 230));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);

		synths = List.newClear(4);
		synths.put(0, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"vol", ControlSpec(0.0,2.0,\amp),
			{|v|
				volBus.set(v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(Button(win, Rect(5, 185, 60, 16))
			.states_([["play", Color.red, Color.black],["mute", Color.black, Color.green]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[0].set(\smallGate, 0)
				},{
					synths[0].set(\smallGate, 1)
				});
			});
		this.addAssignButton(4, \onOff, Rect(5, 205, 60, 16));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(5, 230, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+2, \volBus, volBus], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+2, \volBus, volBus], group));
						});
						synths.put(2, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+4, \volBus, volBus], group));
						synths.put(3, Synth("sand_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus.index+6, \volBus, volBus], group));
						numChannels = 8;
					}
				)
			};
		);

	}
}