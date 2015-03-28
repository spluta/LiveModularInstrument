DecimateGrains_Mod : Module_Mod {
	var sr1Bus, sr2Bus, distVolBus, sineVolBus;

	*initClass {
		StartUp.add {
			SynthDef("decimateGrains_Mod",{ arg inbus, outbus, volume, trigRate, gate = 1, pauseGate = 1;
				var input, dustNoise, env, pauseEnv, trig, fx1, pan;
				var delay1, delay2, delay3, delay4, delay5;
				var grain;

				input=In.ar(inbus, 1);

				dustNoise = LFNoise2.ar(3).range(3, 10);

				//input = input*Trig1.ar(Dust.ar(dustNoise), 1/(dustNoise*2));

				fx1=HPF.ar(Latch.ar(input.round(0.125),Impulse.ar(LFNoise2.ar(2).range(150, 400))), 400);


				delay1 = DelayC.ar(fx1, 0.1, 0.02);
				delay2 = DelayC.ar(fx1, 0.1, 0.04);
				delay3 = DelayC.ar(fx1, 0.1, 0.06);
				delay4 = DelayC.ar(fx1, 0.1, 0.08);
				delay5 = DelayC.ar(fx1, 0.1, 0.1);

				trig = Dust.kr(trigRate);

				fx1 = SelectX.ar(LFNoise2.kr(6).range(0, 1), [fx1, delay1, delay2, delay3, delay4, delay5]);

				pan = LFNoise2.kr(LFNoise0.kr(2).range(2,6)).range(-1,1);

				grain = GrainIn.ar(2, trig, TRand.kr(0.01, 0.1, trig), fx1, pan, -1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outbus,grain*volume*env*pauseEnv);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("DecimateGrains",Rect(718, 645, 135, 270));

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(3);

		synths.add(Synth("decimateGrains_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \volume, 0, \trigRate, 1], group));

		controls.add(EZSlider.new(win,Rect(5, 0, 60, 220), "volume", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\volume, v.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(0, \continuous,Rect(5, 225, 60, 20));

		controls.add(EZSlider.new(win,Rect(70, 0, 60, 220), "trigRate", ControlSpec(0,20,'linear'),
			{|v|
				synths[0].set(\trigRate, v.value);
			}, 0, layout:\vert));

		this.addAssignButton(1,\continuous,Rect(70, 225, 60, 20));

//		//multichannel button
//		controls.add(Button(win,Rect(5, 265, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				synths[0].set(\gate, 0);
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						synths.put(0, Synth("bitCrusher2_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					1, {
//						numChannels = 4;
//						synths.put(0, Synth("bitCrusher4_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					2, {
//						numChannels = 8;
//						synths.put(0, Synth("bitCrusher8_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					}
//				)
//			};
//		);
	}
}


TriggerSines_Mod : Module_Mod {
	var sr1Bus, sr2Bus, distVolBus, sineVolBus;

	*initClass {
		StartUp.add {
			SynthDef("triggerSines_mod",{ arg inbus, outbus, volume=0.2, ampThreshold=0.1, 				t_trig, gate = 1, pauseGate = 1;
					var input, env, pauseEnv, trig, sine0, sine1, sine2, sine3, sine4;
					var freq, hasFreq, fund, skip, amp;

					input=In.ar(inbus, 1);

					//input = PlayBuf.ar(1, 0, loop:1);

					#freq, hasFreq = Pitch.kr(input, ampThreshold: ampThreshold, median: 7);

					//hasFreq = Select.kr(MouseX.kr>0.5, [hasFreq, Latch.kr(hasFreq, MouseX.kr>0.5)]);
					amp = Amplitude.kr(input);
					trig = (amp>ampThreshold);

					//fund = TIRand.kr(30, 50, hasFreq);
					skip = TIRand.kr(5, 15, hasFreq);

					sine0 = Pan2.ar(SinOsc.ar(freq*skip, 0, hasFreq), LFNoise2.kr(0.5).range(-1, 1));
					sine1 = Pan2.ar(SinOsc.ar(freq*2*skip, 0, hasFreq), LFNoise2.kr(0.5).range(-1, 1));
					sine2 = Pan2.ar(SinOsc.ar(freq*3*skip, 0, hasFreq), LFNoise2.kr(0.5).range(-1, 1));
					sine3 = Pan2.ar(SinOsc.ar(freq*4*skip, 0, hasFreq), LFNoise2.kr(0.5).range(-1, 1));
					sine4 = Pan2.ar(SinOsc.ar(freq*5*skip, 0, hasFreq), LFNoise2.kr(0.5).range(-1, 1));

					env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
					pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

					//Out.ar(outbus,input);
					Out.ar(outbus,(sine0+sine1+sine2+sine3+sine4)*volume*env*pauseEnv*amp);
				}).writeDefFile;

		}
	}

	init {
		this.makeWindow("TriggerSines",Rect(718, 645, 135, 270));

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(3);

		synths.add(Synth("triggerSines_mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \volume, 0], group));

		controls.add(EZSlider.new(win,Rect(5, 0, 60, 220), "volume", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\volume, v.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(0, \continuous, Rect(5, 225, 60, 20));

		controls.add(EZSlider.new(win,Rect(70, 0, 60, 220), "thresh", ControlSpec(0.01,0.2,'linear'),
			{|v|
				synths[0].set(\ampThreshold, v.value);
			}, 0, layout:\vert));

		this.addAssignButton(1,\continuous, Rect(70, 225, 60, 20));

//		//multichannel button
//		controls.add(Button(win,Rect(5, 265, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				synths[0].set(\gate, 0);
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						synths.put(0, Synth("bitCrusher2_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					1, {
//						numChannels = 4;
//						synths.put(0, Synth("bitCrusher4_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					2, {
//						numChannels = 8;
//						synths.put(0, Synth("bitCrusher8_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					}
//				)
//			};
//		);
	}
}

DelayRingMod_Mod : Module_Mod {
	var sr1Bus, sr2Bus, distVolBus, sineVolBus;

	*initClass {
		StartUp.add {
			SynthDef("delayRingmod_mod",{ arg inbus, outbus, volume, gate = 1, pauseGate = 1;
				var input, dustNoise, env, pauseEnv, trig, fx1, pan;
				var delay1, delay2, delay3, delay4, delay5;
				var grain;

				input=In.ar(inbus, 1)*volume;

				//input = input*Trig1.ar(Dust.ar(dustNoise), 1/(dustNoise*2));

				delay1 = Pan2.ar(BPF.ar(DelayC.ar(input, 1, LFNoise2.kr(1).range(0.025, 0.05)), LFNoise2.kr(1).range(800,1600), 0.1), LFNoise2.kr(0.25));
				delay2 = Pan2.ar(BPF.ar(DelayC.ar(input, 1, LFNoise2.kr(1.1).range(0.05, 0.075)), LFNoise2.kr(1.1).range(1600,3200), 0.1), LFNoise2.kr(0.25));
				delay3 = Pan2.ar(BPF.ar(DelayC.ar(input, 1, LFNoise2.kr(0.9).range(0.075, 0.1)), LFNoise2.kr(0.9).range(3200,7200), 0.1), LFNoise2.kr(0.25));
				delay4 = Pan2.ar(BPF.ar(DelayC.ar(input, 1, LFNoise2.kr(1.2).range(0.1, 0.125)), LFNoise2.kr(1.2).range(7200,11000), 0.1), LFNoise2.kr(0.25));
				delay5 = Pan2.ar(BPF.ar(DelayC.ar(input, 1.3, LFNoise2.kr(0.95).range(0.125, 0.15)), LFNoise2.kr(0.95).range(11000,20000), 0.1), LFNoise2.kr(0.25));

				fx1 = (SinOsc.ar(LFNoise0.kr(120).range(200, 2000), SinOsc.ar(LFNoise0.kr(121).range(200, 2000)).dup/*+HenonN.ar(LFNoise2.kr(5).exprange(200, 10000)).dup*/))*(delay1+delay2+delay3+delay4+delay5);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outbus,fx1*env*pauseEnv);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("DelayRingMod",Rect(718, 645, 135, 270));

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(3);

		synths.add(Synth("delayRingmod_mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \volume, 0], group));


		controls.add(EZSlider.new(win,Rect(5, 0, 60, 220), "volume", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\volume, v.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(0, \continuous, Rect(5, 225, 60, 20));

//		//multichannel button
//		controls.add(Button(win,Rect(5, 265, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				synths[0].set(\gate, 0);
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						synths.put(0, Synth("bitCrusher2_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					1, {
//						numChannels = 4;
//						synths.put(0, Synth("bitCrusher4_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					},
//					2, {
//						numChannels = 8;
//						synths.put(0, Synth("bitCrusher8_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));
//					}
//				)
//			};
//		);
	}
}