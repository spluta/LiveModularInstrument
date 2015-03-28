DistGrains_Mod : Module_Mod {
	var rout0, rout1, rout2, pulseRateSpec, shiftArray, shiftWeights, xVal;

	*initClass {
		StartUp.add {

			SynthDef("noiseGliss2_mod", {arg inBus, outBus, pulseRate, shift, vol, gate = 1, pauseGate = 1;
				var in, out0, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				in = In.ar(inBus, 1);

				out0 = PitchShift.ar(in, 0.2, shift);

				out0 = Latch.ar(out0, Impulse.ar(LFNoise0.kr(0.1).range(1000, 5000)));

				out0 = out0*Trig1.ar(Dust.kr(LFNoise0.kr(0.2).range(pulseRate, pulseRate*1.5)), LFNoise0.kr(0.3).range(0.025, 0.05)).lag(0.001);

				out0 = Pan2.ar(out0, SinOsc.ar(LFNoise0.kr(0.2).range(0.25, 1), Rand(0, 2pi)));

				Out.ar(outBus, out0*vol*pauseEnv*env);
			}).writeDefFile;



			SynthDef("noiseGliss4_mod", {arg inBus, outBus, pulseRate, shift, vol, gate = 1, pauseGate = 1;
				var in, out0, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				in = In.ar(inBus, 1);

				out0 = PitchShift.ar(in, 0.2, shift);

				out0 = Latch.ar(out0, Impulse.ar(LFNoise0.kr(0.1).range(1000, 5000)));

				out0 = out0*Trig1.ar(Dust.kr(LFNoise0.kr(0.2).range(pulseRate, pulseRate*1.5)), LFNoise0.kr(0.3).range(0.025, 0.05)).lag(0.001);

				out0 = PanAz.ar(4, out0, SinOsc.ar(LFNoise0.kr(0.2).range(0.25, 1), Rand(0, 2pi)));

				Out.ar(outBus, out0*vol*pauseEnv*env);
			}).writeDefFile;



			SynthDef("noiseGliss8_mod", {arg inBus, outBus, pulseRate, shift, vol, gate = 1, pauseGate = 1;
				var in, out0, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				in = In.ar(inBus, 1);

				out0 = PitchShift.ar(in, 0.2, shift);

				out0 = Latch.ar(out0, Impulse.ar(LFNoise0.kr(0.1).range(1000, 5000)));

				out0 = out0*Trig1.ar(Dust.kr(LFNoise0.kr(0.2).range(pulseRate, pulseRate*1.5)), LFNoise0.kr(0.3).range(0.025, 0.05)).lag(0.001);

				out0 = PanAz.ar(8, out0, SinOsc.ar(LFNoise0.kr(0.2).range(0.25, 1), Rand(0, 2pi)));

				Out.ar(outBus, out0*vol*pauseEnv*env);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("DistGrains", Rect(500,100,290,250));

		modName = "DistGrains";
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		shiftArray = [1, 1.25, 1.5, 1.75];
		shiftArray = shiftArray.addAll((shiftArray*0.5).addAll(shiftArray*2)).add(4).sort;

		3.do{
			synths.add(Synth("noiseGliss2_mod", [\inBus, mixerToSynthBus, \outBus, outBus, \pulseRate, 2, \shift, 1, \vol, 0], group));
		};

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				synths.do{arg item; item.set(\vol, v.value)};
			}, 0, true, \vert));
		this.addAssignButton(0,\continuous);

xVal = 0;

		controls.add(QtEZSlider2D.new(ControlSpec(0, 1), ControlSpec(0.25, 5, 'exp'),
			{arg vals;

				synths.do{arg item; item.set(\pulseRate, vals[1])};

				xVal = vals[0]*shiftArray.size;

		}));
		this.addAssignButton(1,\slider2D);

		rout0 = Routine.new({{
			synths[0].set(\shift, shiftArray[gauss(xVal, 2).fold(0, shiftArray.size).floor]);
			rrand(0.5,2).wait;
		}.loop});

		rout1 = Routine.new({{
			synths[0].set(\shift, shiftArray[gauss(xVal, 2).fold(0, shiftArray.size).floor]);
			rrand(0.5,2).wait;
		}.loop});

		rout2 = Routine.new({{
			synths[0].set(\shift, shiftArray[gauss(xVal, 2).fold(0, shiftArray.size).floor]);
			rrand(0.5,2).wait;
		}.loop});

		rout0.play;
		rout1.play;
		rout2.play;

		//multichannel button

		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths.do{arg item; item.set(\gate, 0)};
						numChannels = 2;
						3.do{|i|
							synths.put(i, Synth("noiseGliss2_mod", [\inBus, mixerToSynthBus, \outBus, outBus, \pulseRate, 2, \shift, 1, \vol, 0], group));
						};
					},
					1, {
						synths.do{arg item; item.set(\gate, 0)};
						numChannels = 4;
						3.do{|i|
							synths.put(i, Synth("noiseGliss4_mod", [\inBus, mixerToSynthBus, \outBus, outBus, \pulseRate, 2, \shift, 1, \vol, 0], group));
						};
					},
					2, {
						synths.do{arg item; item.set(\gate, 0)};
						numChannels = 8;
						3.do{|i|
							synths.put(i, Synth("noiseGliss8_mod", [\inBus, mixerToSynthBus, \outBus, outBus, \pulseRate, 2, \shift, 1, \vol, 0], group));
						};
					}
				)
			};
		);


		win.layout_(
			VLayout(
				HLayout(
					VLayout(controls[0].layout,assignButtons[0].layout),
					VLayout(controls[1].layout,assignButtons[1].layout)
				),controls[2]
			)
		);
	}

	pause {
		synths.do{arg item; item.set(\pauseGate, 0)};
	}

	unpause {
		synths.do{arg item;
			item.set(\pauseGate, 1);
			item.run(true);
		};
	}

	saveSettings {
		xmlSynth.setAttribute("controls2", controls[2].value.asString);
	}

	killMeSpecial {
		rout0.stop;
		rout1.stop;
		rout2.stop;
	}
}