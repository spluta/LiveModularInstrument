NeuwirthSine_Mod : Module_Mod {
	var midiNoteNum, detune;

	*initClass {
		StartUp.add {
			SynthDef("neuwirthSine_mod", {arg freq, outBus, gate = 1, pauseGate = 1, sine0Gate = 0, sine1Gate = 0, sine2Gate = 0, sine3Gate = 0;
				var sine0, sine1, sine2, sine3, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine0 = SinOsc.ar(32.703, 0, 0.1)*EnvGen.kr(Env.asr(0.1,1,0.1), sine0Gate, doneAction:0);
				sine1 = SinOsc.ar(69.29, 0, 0.1)*EnvGen.kr(Env.asr(0.1,1,0.1), sine1Gate, doneAction:0);
				sine2 = SinOsc.ar(97.99, 0, 0.1)*EnvGen.kr(Env.asr(0.1,1,0.1), sine2Gate, doneAction:0);
				sine3 = SinOsc.ar(55, 0, 0.1)*EnvGen.kr(Env.asr(0.1,1,0.1), sine3Gate, doneAction:0);

				Out.ar(outBus, Mix.new([sine0, sine1, sine2, sine3]).dup*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("NeuwirthSine", Rect(500,100,265,20));

		modName = "NeuwirthSine";
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		synths = List.new;

		synths.add(Synth("neuwirthSine_mod", [\outBus, outBus], group));

		controls.add(Button()
			.states_([["33", Color.red, Color.black],["33", Color.black, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\sine0Gate, 0);
					},
					1, {
						synths[0].set(\sine0Gate, 1);
					}
				)
			};
		);
		this.addAssignButton(0,\onOff);
		//this.addButton(0,\onOff, Rect(5, 25, 60, 20));

		controls.add(Button()
			.states_([["70", Color.red, Color.black],["70", Color.black, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\sine1Gate, 0);
					},
					1, {
						synths[0].set(\sine1Gate, 1);
					}
				)
			};
		);
		this.addAssignButton(1,\onOff);
		//this.addButton(1,\onOff, Rect(70, 25, 60, 20));

		controls.add(Button()
			.states_([["99", Color.red, Color.black],["99", Color.black, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\sine2Gate, 0);
					},
					1, {
						synths[0].set(\sine2Gate, 1);
					}
				)
			};
		);
		this.addAssignButton(2,\onOff);
		//this.addButton(2,\onOff, Rect(135, 25, 60, 20), );

		controls.add(Button()
			.states_([["55", Color.red, Color.black],["55", Color.black, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\sine3Gate, 0);
					},
					1, {
						synths[0].set(\sine3Gate, 1);
					}
				)
			};
		);
		this.addAssignButton(3,\onOff);
		//this.addButton(3,\onOff, Rect(200, 25, 60, 20));


		win.layout_(VLayout(
			HLayout(controls[0], controls[1], controls[2], controls[3]),
			HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout)
		));

	}

}

WubbelsSine_Mod : Module_Mod {
	var midiNoteNum, detune;

	*initClass {
		StartUp.add {
			SynthDef("wubSine_mod", {arg freq, outBus, gate = 1, pauseGate = 1, localGate = 0;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(freq, 0, 0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("WubbelsSine", Rect(500,100,210,20));

		modName = "WubbelsSine";
		this.initControlsAndSynths(0);

		synths = List.new;

		midiNoteNum = 90;

		detune = 0;

		synths.add(Synth("wubSine_mod", [\freq, 73.midicps, \outBus, outBus], group));
		synths.add(Synth("wubSine_mod", [\freq, 75.midicps, \outBus, outBus], group));
		synths.add(Synth("wubSine_mod", [\freq, 80.midicps, \outBus, outBus], group));

		//		controls.add(EZSlider(win, // window
		//			Rect(5, 5, 60, 200), // dimensions
		//			"note", // label
		//			ControlSpec(20, 100, \linear, 0.5), // control spec
		//			{|ez|
		//				midiNoteNum=ez.value;
		//				synths[0].set(\freq, (midiNoteNum+(detune/100)).midicps);
		//			},// action
		//			midiNoteNum, layout:\vert
		//		));
		//
		//		controls.add(EZSlider(win, // window
		//			Rect(70, 5, 60, 200), // dimensions
		//			"detune", // label
		//			ControlSpec(-50, 50, \linear, 1), // control spec
		//			{|ez|
		//				detune=ez.value;
		//				synths[0].set(\freq, (midiNoteNum+(detune/100)).midicps);
		//			},// action
		//			0, true, layout:\vert
		//		));
		//
		//		controls.add(EZSlider(win, // window
		//			Rect(135, 5, 60, 200), // dimensions
		//			"vol", // label
		//			ControlSpec(0, 1), // control spec
		//			{|ez|
		//				synths[0].set(\vol, ez.value);
		//			},// action
		//			midiNoteNum, layout:\vert
		//		));
		//		this.addButton(2,/onOff, Rect(135, 205, 60, 20));
		//
		//		controls.add(Button(win, Rect(5, 230, 205, 20))
		//			.states_([["off", Color.red, Color.black],["on", Color.black, Color.green]])
		//			.action_{|butt|
		//				switch(butt.value,
		//					0, {
		//						synths[0].set(\localGate, 0);
		//					},
		//					1, {
		//						synths[0].set(\localGate, 1);
		//					}
		//				)
		//			};
		//		);
		//		this.addButton(3,3, Rect(5, 250, 205, 20));
	}
}


WubbelsSine2_Mod : Module_Mod {
	var startNote, endNote, duration;

	*initClass {
		StartUp.add {
			SynthDef("wubSine2_mod", {arg outBus, envGate0=0, envGate1=0, envGate2=0, whichFreq=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv, localEnv, freq0, freq1, freq2, freq;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(2,1,3), gate, doneAction:2);

				freq0 = EnvGen.kr(Env.new([68.5.midicps, 68.5.midicps, 76.midicps], [2, 50], 'exponential'), envGate0);
				//freq1 = EnvGen.kr(Env.new([80.midicps, 83.midicps], [11], 'exponential'), envGate1);
				//freq2 = EnvGen.kr(Env.new([988, 1109], [10], 'exponential'), envGate2);
				//XLine.kr(freq0, freq1, dur);

				//freq = Lag.kr(Select.kr(whichFreq, [freq0, freq1, freq2]), 0.3);

				sine = SinOsc.ar(freq0, 0, 0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, 0));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("WubbelsSine2", Rect(500,100,210,50));

		modName = "WubbelsSine2";
		this.initControlsAndSynths(1);

		dontLoadControls = [0];

		synths = List.newClear(1);

		startNote = 60;
		endNote = 72;
		duration = 10;

		//synths.add(Synth("wubSine2", [\freq, midiNoteNum.midicps, \outBus, outBus], group));


		synths.put(0, Synth("wubSine2_mod", [\outBus, outBus], group));

		controls.add(Button(win, Rect(5, 5, 205, 20))
			.states_([["go", Color.red, Color.black],["kill", Color.black, Color.green],["reset", Color.red, Color.green]])
			.action_{|butt|
				switch(butt.value.postln,
					0, {
						synths.put(0, Synth("wubSine2_mod", [\outBus, outBus], group));
					},
					1, {
						synths[0].set(\envGate0, 1, \gate, 1);
					},
					2, {
						synths[0].set(\gate, 0);
					}
				)
			};
		);
		this.addAssignButton(0,\increment, Rect(5, 25, 205, 20));
	}


}

AblingerSine_Mod : Module_Mod {
	var startNote, endNote, duration;

	*initClass {
		StartUp.add {
			SynthDef("blingerSine2_mod", {arg outBus, envGate0=0, envGate1=0, envGate2=0, whichFreq=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv, localEnv, freq0, freq1, freq2, freq;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(2,1,3), gate, doneAction:2);

				freq0 = EnvGen.kr(Env.new([50, 50, 50], [2, 50], 'exponential'), envGate0);
				//freq1 = EnvGen.kr(Env.new([80.midicps, 83.midicps], [11], 'exponential'), envGate1);
				//freq2 = EnvGen.kr(Env.new([988, 1109], [10], 'exponential'), envGate2);
				//XLine.kr(freq0, freq1, dur);

				//freq = Lag.kr(Select.kr(whichFreq, [freq0, freq1, freq2]), 0.3);

				sine = SinOsc.ar(freq0, 0, 0.8);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, 0));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("AblingerSine", Rect(500,100,210,50));

		modName = "AblingerSine";
		this.initControlsAndSynths(1);

		synths = List.newClear(1);

		startNote = 60;
		endNote = 72;
		duration = 10;


		synths.put(0, Synth("blingerSine2_mod", [\outBus, outBus], group));

		controls.add(Button(win, Rect(5, 5, 205, 20))
			.states_([["go", Color.red, Color.black],["kill", Color.black, Color.green],["reset", Color.red, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths.put(0, Synth("blingerSine2_mod", [\outBus, outBus], group));
					},
					1, {
						synths[0].set(\envGate0, 1, \gate, 1);
					},
					2, {
						synths[0].set(\gate, 0);
					}
				)
			};
		);
		this.addButton(0,\onOff, Rect(5, 25, 205, 20));
	}

	load {arg xmlSynth;
		this.loadControllers(xmlSynth);

		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
		win.front;

	}

}

PlutaSine_Mod : Module_Mod {
	var startNote, endNote, duration;

	*initClass {
		StartUp.add {
			SynthDef("plutaSine2_mod", {arg outBus, envGate0=0, envGate1=0, envGate2=0, whichFreq=0, gate = 1, pauseGate = 1;
				var sine, sine1, env, pauseEnv, localEnv, freq0, freq1, freq2, freq;

				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(2,1,3), gate, doneAction:2);

				freq0 = EnvGen.kr(Env.new([778, 778, 778], [2, 50], 'exponential'), envGate0);

				sine = SinOsc.ar(freq0, 0, 0.8);
				sine1 = SinOsc.ar(freq0+12, 0, 0.8);


				Out.ar(outBus, [sine, sine1]*AmpComp.kr(freq)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("PlutaSine", Rect(500,100,210,50));

		modName = "PlutaSine";
		this.initControlsAndSynths(1);

		synths = List.newClear(1);

		startNote = 60;
		endNote = 72;
		duration = 10;


		synths.put(0, Synth("plutaSine2_mod", [\outBus, outBus], group));
		/*
		controls.add(Button(win, Rect(5, 5, 205, 20))
		.states_([["go", Color.red, Color.black],["kill", Color.black, Color.green],["reset", Color.red, Color.green]])
		.action_{|butt|
		switch(butt.value,
		0, {
		synths.put(0, Synth("plutaSine2_mod", [\outBus, outBus], group));
		},
		1, {
		synths[0].set(\envGate0, 1, \gate, 1);
		},
		2, {
		synths[0].set(\gate, 0);
		}
		)
		};
		);
		this.addButton(0,\onOff, Rect(5, 25, 205, 20));*/
	}

	load {arg xmlSynth;
		this.loadControllers(xmlSynth);

		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
		win.front;

	}

}



RhythmicDelays_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("rhythmicDelays_mod", {arg inBus, outBus, pauseGate = 1, gate = 1;
				var in, env, pauseEnv, out0, out1, out;

				in = In.ar(inBus, 1);

				out0 = Pan2.ar(CombC.ar(in, 3, 0.22727, 4), LFNoise2.kr(0.3).range(-1,1));
				out1 = Pan2.ar(CombC.ar(in, 3, 0.151515, 4), LFNoise2.kr(0.3).range(-1,1));

				out = SelectX.ar(LFNoise2.kr(0.3).range(0,2), [out0, out1]);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(3,1,3), gate, doneAction:2);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("RhythmicDelays",Rect(680, 421, 110, 207));

		this.initControlsAndSynths(0);

		this.makeMixerToSynthBus;

		synths = List.newClear(0);

		synths.add(Synth("rhythmicDelays_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));

	}
}


WubbelsSine3_Mod : Module_Mod {
	var midiNoteNum, detune;

	*initClass {
		StartUp.add {
			SynthDef("wubSine3_mod", {arg freq, outBus, vol=0, gate = 1, pauseGate = 1, localGate = 0;
				var sine, env, pauseEnv, localEnv;

				localEnv = EnvGen.kr(Env.asr(0.2,1,0.2), localGate, doneAction:0);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = [SinOsc.ar(51.913087197493-1.5, 0, 0.25), SinOsc.ar(51.913087197493+1.5, 0, 0.25)];

				Out.ar(outBus, sine*AmpComp.kr(51.913087197493)*env*pauseEnv*localEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("WubbelsSine", Rect(500,100,210,250));

		modName = "WubbelsSine3";
		this.initControlsAndSynths(4);

		synths = List.new;

		midiNoteNum = 90;

		detune = 0;

		synths.add(Synth("wubSine3_mod", [\freq, midiNoteNum.midicps, \outBus, outBus], group));

		controls.add(EZSlider(win, // window
			Rect(5, 5, 60, 200), // dimensions
			"note", // label
			ControlSpec(20, 100, \linear, 0.5), // control spec
			{|ez|
				midiNoteNum=ez.value;
				synths[0].set(\freq, (midiNoteNum+(detune/100)).midicps);
			},// action
			midiNoteNum, layout:\vert
		));

		controls.add(EZSlider(win, // window
			Rect(70, 5, 60, 200), // dimensions
			"detune", // label
			ControlSpec(-50, 50, \linear, 1), // control spec
			{|ez|
				detune=ez.value;
				synths[0].set(\freq, (midiNoteNum+(detune/100)).midicps);
			},// action
			0, true, layout:\vert
		));

		controls.add(EZSlider(win, // window
			Rect(135, 5, 60, 200), // dimensions
			"vol", // label
			ControlSpec(0, 1), // control spec
			{|ez|
				synths[0].set(\vol, ez.value);
			},// action
			midiNoteNum, layout:\vert
		));
		this.addButton(2, \onOff, Rect(135, 205, 60, 20));

		controls.add(Button(win, Rect(5, 230, 205, 20))
			.states_([["off", Color.red, Color.black],["on", Color.black, Color.green]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\localGate, 0);
					},
					1, {
						synths[0].set(\localGate, 1);
					}
				)
			};
		);
		this.addButton(3,3, Rect(5, 250, 205, 20));
	}
}

AutoTuneFake_Mod : Module_Mod {
	var freq, vol, onOff, freqSpecs, whichScale, cMajor;

	*initClass {
		StartUp.add {
			SynthDef("fakeAutoTune_mod", {arg outBus, vol, freq, whichScale=0, onOff = 0;
				var osc, cScale, baseFreq, volume;

				baseFreq = freq.midicps;

				osc = SinOsc.ar(baseFreq, 0, 0.5);

				volume = Lag.kr(onOff, 0.01)*Lag.kr(vol, 0.05);

				Out.ar(outBus, osc.dup*volume);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("AutoTuneFake", Rect(500, 500, 350, 260));

		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		whichScale = 0;

		synths.add(Synth("fakeAutoTune_mod", [\outBus, outBus, \vol, 0, \freq, 440], group));


		cMajor = [62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84, 86, 88, 89, 91, 93, 95];

		freqSpecs = [ControlSpec(77, 91, 'lin', 1), ControlSpec(89, 103, 'lin', 1)/*[77, 79, 81, 83, 84, 86, 88, 89, 91]*/];

		controls.add(QtEZSlider2D(ControlSpec.new, ControlSpec.new,
			{|vals|
				freq = vals[0];
				vol = vals[1];

				switch(whichScale,
					0, {
						freq = cMajor[freq*cMajor.size];
						if(freq == nil, {freq = 95});
					},
					{
						freq = freqSpecs[whichScale-1].map(freq);
					}
				);

				synths[0].set(\freq, freq, \vol, vol);
		}, [0,0], false));

		controls[0].zAction = {|val|
			onOff = val.value;
			synths[0].set(\onOff, onOff);
		};

		this.addAssignButton(0,\slider2D);

		controls.add(Button()
			.states_([
				["c major", Color.red, Color.black],
				["c major", Color.black, Color.red]
			])
			.action_({arg butt;
				whichScale = 0;
				synths[0].set(\whichScale, 0);
				controls[2].value = 0;
				controls[3].value = 0;
			})
		);
		this.addAssignButton(1,\onOff);

		controls.add(Button()
			.states_([
				["chrom0", Color.red, Color.black],
				["chrom0", Color.black, Color.red]
			])
			.action_({arg butt;
				whichScale = 1;
				synths[0].set(\whichScale, 1);

				controls[1].value = 0;
				controls[3].value = 0;
			})
		);
		this.addAssignButton(2,\onOff);

		controls.add(Button()
			.states_([
				["chrom1", Color.red, Color.black],
				["chrom1", Color.black, Color.red]
			])
			.action_({arg butt;
				whichScale = 2;
				synths[0].set(\whichScale, 2);

				controls[1].value = 0;
				controls[2].value = 0;
			})
		);
		this.addAssignButton(3,\onOff);

		numChannels = 2;

		win.layout_(HLayout(
			VLayout(controls[0].layout,assignButtons[0].layout),
			VLayout(controls[1], controls[2], controls[3]),
			VLayout(assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout)
		)
		);

	}

	// load {}
}


OverlapWubbels_Mod : Module_Mod {
	var startPos, startPos0, duration, loadFileButton, fileText, savePath, shortPath, vol, playRate, synthOutBus, buffer0, buffer1, monoStereo, dur, fade, trigRate, centerPos, center, width, yRange;


	init {

		this.makeWindow("OverlapWubbels", Rect(707, 393, 709, 217));
		this.initControlsAndSynths(7);

		savePath = "";

		synths = List.newClear(1);

		playRate = 1; vol = 0; fade = 0.1; trigRate = 1; startPos = 0; dur = 4;

		buffer0 = Buffer.alloc(group.server, group.server.sampleRate);
		buffer1 = Buffer.alloc(group.server, group.server.sampleRate);

		yRange = 1;

		synths.put(0, Synth("overLapSamplePlayer_mod", [\bufnum0, buffer0.bufnum,  \bufnum1, buffer1.bufnum, \whichOut, 0, \playRate, playRate, \numGrains, 1, \startPos, startPos, \dur, dur, \outBus, outBus, \vol, 0], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				vol = v.value;
				synths[0].set(\vol, v.value);

		}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZRanger.new("playRange", ControlSpec(0,1,'linear',0.001),
			{|v|
				startPos = v.value[0];
				startPos0 = v.value[1];
				dur = (v.value[1]-v.value[0]);
				synths[0].set(\startPos, startPos, \startPos0, startPos0, \dur, dur);
		}, [0,1], true, \horz));
		this.addAssignButton(1,\range);

		controls.add(QtEZSlider.new("overlaps", ControlSpec(1,3,'linear'),
			{|v|
				synths[0].set(\numGrains, v.value);
		}, 1, true, \horz));
		this.addAssignButton(2,\continuous);

		controls.add(QtEZSlider.new("playRate", ControlSpec(0.96,1,'linear'),
			{|v|
				synths[0].set(\playRate, v.value);
		}, 1, true, \horz));
		this.addAssignButton(3,\continuous);

		controls.add(QtEZSlider2D.new(ControlSpec(0,1), ControlSpec(0.001,1,\exp),
			{arg vals;
				controls[1].valueAction_([(vals.value[0]-(0.65*vals.value[1]*yRange)).clip(0,1), (vals.value[0]+(0.65*vals.value[1]*yRange)).clip(0,1)]);}
		));
		this.addAssignButton(4,\slider2D);

		controls.add(QtEZSlider.new("yRange", ControlSpec(0.01,1,'linear'),
			{|v|
				yRange = v.value;
		}, 1, true, \vert));
		this.addAssignButton(5,\continuous);

		loadFileButton = Button()
		.states_([ [ "Load File", Color.red, Color.black ] ])
		.action_{|v|
			visibleArray = List.newClear;
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};

			Dialog.openPanel({ arg path;
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				savePath = path;
				this.loadFile;
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};
		fileText = StaticText();

		controls.add(Button()
			.states_([ [ "NoZActions", Color.red, Color.black ],  [ "ZActions!", Color.blue, Color.black ]])
			.action_{|v|
				if(v.value ==1,{
					controls[4].zAction = {|val|
						synths[0].set(\onOff, val.value)
					};
					synths[0].set(\onOff, 0)
				},{
					synths[0].set(\onOff, 1);
					controls[4].zAction = {};
				}
				);
			};
		);

		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0].layout,assignButtons[0].layout),
					HLayout(controls[1].layout,assignButtons[1].layout),
					HLayout(controls[2].layout,assignButtons[2].layout),
					HLayout(controls[3].layout,assignButtons[3].layout),
					HLayout(loadFileButton, fileText, controls[6])
				),
				VLayout(controls[4].layout, assignButtons[4].layout), controls[5].layout
			)
		);
	}

	loadFile {
		if(savePath.size>0,{
			if(File.exists(savePath),{
				fileText.string_(savePath.split.pop);
				synths[0].set(\whichOut, 0);
				buffer0 = Buffer.readChannel(group.server, savePath, 0, -1, [0],
					{arg buf;
						synths[0].set(\bufnum0, buf.bufnum);
						synths[0].set(\whichOut, 0);
						SystemClock.sched(1.0, {synths[0].set(\trigOnOff, 1)});
				});
				SystemClock.sched(0.5, {
					buffer1 = Buffer.readChannel(group.server, savePath, 0, -1, [1],
						{arg buf;
							synths[0].set(\bufnum1, buf.bufnum, \whichOut, 1);
					});
				});
			})
		})
		}

		saveExtra {arg saveArray;
			saveArray.add(savePath.asString);
		}

		loadExtra {arg loadArray;
			savePath = loadArray[4];
			this.loadFile;
		}
	}