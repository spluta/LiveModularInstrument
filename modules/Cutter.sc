Cutter_Mod : Module_Mod {
	var buffer, volBus;

	*initClass {
		StartUp.add {
			SynthDef("cutter2_mod", {arg inBus, outBus, bufnum, pointClkFreeze, latchPoint, lowRate, highRate, volBus, onOff, overlap = 1, gate = 1, pauseGate = 1;
				var in, trate, dur, xPos, clk, pointClk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, env, pauseEnv, vol;

				vol = In.kr(volBus);

				trate = Select.kr(onOff, [LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate), 0]);
				dur = (1 / trate)*overlap;
				clk = Impulse.kr(trate);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				in = In.ar(inBus);

				BufWr.ar(in, bufnum, phasor, 1);

				xPos = (phasor/SampleRate.ir);

				pointClk = Select.kr(pointClkFreeze, [clk, 0]);

				point0 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 0));
				point1 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 1));
				point2 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 2));
				point3 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 3));
				point4 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 4));
				point5 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 5));
				point6 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 6));
				point7 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 7));

				point = TWChoose.kr(clk, [point0,point1,point2,point3], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

				point = Select.kr(latchPoint, [point, Latch.kr(point, latchPoint)]);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				Out.ar(outBus, TGrains2.ar(2, clk, bufnum, 1.0, point, dur, TRand.kr(-1, 1, clk), 1, 0.01, 0.01, 4)*vol*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Cutter", Rect(900, 400, 240, 115));
		this.initControlsAndSynths(8);

		this.makeMixerToSynthBus(1);

		volBus = Bus.control(group.server);
		buffer = Buffer.alloc(group.server, group.server.sampleRate*60, 1);

		synths.add(Synth("cutter2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));

		controls.add(Button.new()
			.states_([["mute", Color.blue, Color.black ],["mute", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 1);
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["on", Color.blue, Color.black ],["on", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 0);
		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["latch", Color.blue, Color.black ],["latch", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 1, \latchPoint, 0);
		});
		this.addAssignButton(2, \onOff);

		controls.add(Button.new()
			.states_([["latch1", Color.blue, Color.black ],["latch1", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 1);
		});

		this.addAssignButton(3, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
			{arg val; volBus.set(val.value)}, 1, true, orientation:'horz'));
		this.addAssignButton(4, \continuous,Rect(240, 50, 60, 20));

		controls.add(QtEZRanger("speed", ControlSpec(3,20),
			{arg val; synths[0].set(\lowRate, val.value[0], \highRate, val.value[1])}, [3,16], true, orientation:'horz'));

		controls.add(QtEZSlider("overlap", ControlSpec(1,3),
			{arg val; synths[0].set(\overlap, val.value)}, 1, true, orientation:'horz'));
		this.addAssignButton(6, \continuous);

		//multichannel button
/*		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutter2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					},
					1, {
						numChannels = 4;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutter4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					},
					2, {
						numChannels = 8;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutter8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					}
				)
			};
		);*/

		//start me in the off position
		controls[0].value = 1;

		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15), controls[2].maxHeight_(15), controls[3].maxHeight_(15)),
					HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout),
					HLayout(controls[4].layout,assignButtons[4].layout),
					controls[5].layout,
					HLayout(controls[6].layout,assignButtons[6].layout)
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	killMeSpecial {
		buffer.free;
		volBus.free;
	}
}

CutterThrough_Mod : Module_Mod {
	var buffer, volBus;

	*initClass {
		StartUp.add {
			SynthDef("cutterB2_mod", {arg inBus, outBus, bufnum, pointClkFreeze, latchPoint, lowRate, highRate, volBus, onOff, whichSig=0, gate = 1, pauseGate = 1;
				var inSt, in, out, trate, dur, xPos, clk, pointClk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, env, pauseEnv, vol;

				vol = In.kr(volBus);

				trate = Select.kr(onOff, [LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate), 0]);
				dur = 1 / trate;
				clk = Impulse.kr(trate+LFNoise2.kr(1).range((trate/20).neg, trate/20));

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				inSt = In.ar(inBus,2);
				in = Mix.new(inSt);

				BufWr.ar(in, bufnum, phasor, 1);

				xPos = (phasor/SampleRate.ir);

				pointClk = Select.kr(pointClkFreeze, [clk, 0]);

				point0 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 0));
				point1 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 1));
				point2 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 2));
				point3 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 3));
				point4 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 4));
				point5 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 5));
				point6 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 6));
				point7 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 7));

				point = TWChoose.kr(clk, [point0,point1,point2,point3], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

				point = Select.kr(latchPoint, [point, Latch.kr(point, latchPoint)]);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				out = SelectX.ar(Lag.kr(whichSig,0.1)*2, [inSt, TGrains2.ar(2, clk, bufnum, 1.0, point, dur, TRand.kr(-1, 1, clk), 1, 0.01, 0.01, 4)*vol]);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;

			SynthDef("cutterB4_mod", {arg inBus, outBus, bufnum, pointClkFreeze, latchPoint, lowRate, highRate, volBus, onOff, whichSig=0, gate = 1, pauseGate = 1;
				var inSt, in, out, trate, dur, xPos, clk, pointClk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, env, pauseEnv, vol;

				vol = In.kr(volBus);

				trate = Select.kr(onOff, [LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate), 0]);
				dur = 1 / trate;
				clk = Impulse.kr(trate+LFNoise2.kr(1).range((trate/20).neg, trate/20));

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				inSt = In.ar(inBus,4);
				in = Mix.new(inSt);


				BufWr.ar(in, bufnum, phasor, 1);

				xPos = (phasor/SampleRate.ir);

				pointClk = Select.kr(pointClkFreeze, [clk, 0]);

				point0 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 0));
				point1 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 1));
				point2 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 2));
				point3 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 3));
				point4 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 4));
				point5 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 5));
				point6 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 6));
				point7 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 7));

				point = TWChoose.kr(clk, [point0,point1,point2,point3], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

				point = Select.kr(latchPoint, [point, Latch.kr(point, latchPoint)]);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				out = SelectX.ar(Lag.kr(whichSig,0.1)*2, [inSt, TGrains2.ar(4, clk, bufnum, 1.0, point, dur, TRand.kr(-1, 1, clk), 1, 0.01, 0.01, 4)*vol]);

				Out.ar(outBus, out*env*pauseEnv);

			}).writeDefFile;

			SynthDef("cutterB8_mod", {arg inBus, outBus, bufnum, pointClkFreeze, latchPoint, lowRate, highRate, volBus, onOff, whichSig=0, gate = 1, pauseGate = 1;
				var inSt, in, out, trate, dur, xPos, clk, pointClk, point, point0, point1, point2, point3, point4, point5, point6, point7, phasor, env, pauseEnv, vol;

				vol = In.kr(volBus);

				trate = Select.kr(onOff, [LFNoise2.kr(LFNoise2.kr(0.5, 1.5, 2)).range(lowRate, highRate), 0]);
				dur = 1 / trate;
				clk = Impulse.kr(trate+LFNoise2.kr(1).range((trate/20).neg, trate/20));

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				inSt = In.ar(inBus,8);
				in = Mix.new(inSt);

				BufWr.ar(in, bufnum, phasor, 1);

				xPos = (phasor/SampleRate.ir);

				pointClk = Select.kr(pointClkFreeze, [clk, 0]);

				point0 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 0));
				point1 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 1));
				point2 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 2));
				point3 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 3));
				point4 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 4));
				point5 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 5));
				point6 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 6));
				point7 = Latch.kr(xPos, PulseDivider.kr(pointClk, 8, 7));

				point = TWChoose.kr(clk, [point0,point1,point2,point3], [LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1),LFNoise0.kr(0.5, 0.5,1), LFNoise0.kr(0.5, 0.5,1)].normalizeSum);

				point = Select.kr(latchPoint, [point, Latch.kr(point, latchPoint)]);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				out = SelectX.ar(Lag.kr(whichSig,0.1)*2, [inSt, TGrains2.ar(8, clk, bufnum, 1.0, point, dur, TRand.kr(-1, 1, clk), 1, 0.01, 0.01, 4)*vol]);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("CutterThrough", Rect(900, 400, 240, 95));
		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus(8);

		volBus = Bus.control(group.server);
		buffer = Buffer.alloc(group.server, group.server.sampleRate*60, 1);

		synths.add(Synth("cutterB2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));

		controls.add(Button.new()
			.states_([["through", Color.blue, Color.black ],["through", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 1, \pointClkFreeze, 0, \latchPoint, 0, \whichSig, 0);
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["on", Color.blue, Color.black ],["on", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 0, \whichSig, 1);
		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["latch", Color.blue, Color.black ],["latch", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 1, \latchPoint, 0, \whichSig, 1);
		});
		this.addAssignButton(2, \onOff);

		controls.add(Button.new()
			.states_([["latch1", Color.blue, Color.black ],["latch1", Color.black, Color.red ]])
			.action_{|v|
				4.do{arg i; controls[i].value = 0};
				v.value = 1;
				synths[0].set(\onOff, 0, \pointClkFreeze, 0, \latchPoint, 1, \whichSig, 1);
		});

		this.addAssignButton(3, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
			{arg val; volBus.set(val.value)}, 1, true, 'horz'));
		this.addAssignButton(4, \continuous);

		controls.add(QtEZRanger("speed", ControlSpec(3,20),
			{arg val; synths[0].set(\lowRate, val.value[0], \highRate, val.value[1])}, [3,16], true, 'horz'));

		//multichannel button
		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutterB2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					},
					1, {
						numChannels = 4;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutterB4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					},
					2, {
						numChannels = 8;
						synths.do{arg item; item.set(\gate, 0)};
						synths.put(0, Synth("cutterB8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \pointClkFreeze, 0, \latchPoint, 0, \lowRate, 3, \highRate, 17, \vol, 1, \onOff, 1, \volBus, volBus], group));
					}
				)
			};
		);

		//start me in the off position
		controls[0].value = 1;

		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0], controls[1], controls[2], controls[3]),
					HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout),
					HLayout(controls[4].layout,assignButtons[4].layout),
					controls[5].layout
				)
			)
		);
		win.layout.spacing = 0;
	}

	killMeSpecial {
		buffer.free;
		volBus.free;
	}
}
