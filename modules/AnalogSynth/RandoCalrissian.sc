RandoCalrissian_AnalogMod : AnalogModule_Mod {
	var rsFluct0, rsFluct1, rsQuantRand, rsShiftReg, rsNoiseSource;
	var outBusses, controlsStartSize;
	var signal2Bus, signal3Bus, outBus2, outBus3, signal2BusEngaged, signal3BusEngaged;
	var orOutBus, sumOutBus, invOutBus;

	*initClass {
		StartUp.add {

			SynthDef("randoCalrissianFluctuating_analogMod", {arg cvInBus, cvInPlugged, rateAdjustAtten, rateIn, cvOutBus, gateBus, gate=1, pauseGate = 1, localPauseGate = 1;
				var trig, lagTime, rate, rateAdjustIn;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				rateAdjustIn = (InFeedback.ar(cvInBus, 1)*cvInPlugged);

				rate = (rateIn+(rateAdjustIn*rateAdjustAtten)).linexp(0.00001, 1, 0.1, 22500, \minmax);

				trig = Dust.ar(rate);
				lagTime = 1/rate;
				Out.ar(cvOutBus, Lag3.ar(Latch.ar(WhiteNoise.ar(0.5, 0.5), trig), lagTime));
				Out.ar(gateBus, trig);

			}).writeDefFile;

			SynthDef("randoCalrissianQuantizedRandoms_analogMod", {arg quantizationInBus, quantizationPlugged, triggerInBus, triggerInPlugged, quantizationIn, twoNOutbus, nPlus1Outbus, gate=1, pauseGate = 1, localPauseGate = 1;
				var trig, quant, num;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				quant = Select.ar(quantizationPlugged, [K2A.ar(quantizationIn), InFeedback.ar(quantizationInBus).linlin(0, 1, 1, 6.99, \minmax)]);

				trig = Select.ar(triggerInPlugged, [K2A.ar(0), InFeedback.ar(triggerInBus)]);

				num = ((Latch.ar(WhiteNoise.ar(0.5, 0.5), trig)*(2**(quant.floor))).floor)/(2**(quant.floor));
				Out.ar(twoNOutbus, num);

				num  = (((Latch.ar(WhiteNoise.ar(0.5, 0.5), trig)*(quant.floor+1)).floor)/(quant.floor+1)); //outputting 0 to 1
				Out.ar(nPlus1Outbus, num);
			}).writeDefFile;

			SynthDef("randoCalrissianAnalogShiftRegister_analogMod", {arg in0, in1, in2, in3, trig0In, trig1In, trig2In, trig3In, bigTrigIn, in1Plugged, in2Plugged, in3Plugged, bigTrigPlugged, trig0Plugged, trig1Plugged, trig2Plugged, trig3Plugged, out0Bus, out1Bus, out2Bus, out3Bus, t_trig, gate=1, pauseGate = 1, localPauseGate = 1;

				var sig0, sig1, sig2, sig3, trig0, trig1, trig2, trig3, bigTrig;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				bigTrig = Select.ar(bigTrigPlugged, [K2A.ar(0), InFeedback.ar(bigTrigIn)])+Trig.kr(t_trig, 0.1);

				//Out.ar(0, SinOsc.ar(200, 0, 0.1)*Trig1.ar(bigTrig));

				trig0 = Select.ar(trig0Plugged, [bigTrig, InFeedback.ar(trig0In)]);

				//Out.ar(1, SinOsc.ar(300, 0, 0.1)*Trig1.ar(trig0));

				trig1 = Select.ar(trig1Plugged, [bigTrig, InFeedback.ar(trig1In)]);
				trig2 = Select.ar(trig2Plugged, [bigTrig, InFeedback.ar(trig2In)]);
				trig3 = Select.ar(trig3Plugged, [bigTrig, InFeedback.ar(trig3In)]);

				sig0 = Latch.ar(InFeedback.ar(in0), trig0).abs;
				sig1 = Select.ar(in1Plugged, [sig0, Latch.ar(InFeedback.ar(in1), trig1)]).abs;
				sig2 = Select.ar(in2Plugged, [sig1, Latch.ar(InFeedback.ar(in2), trig2)]).abs;
				sig3 = Select.ar(in3Plugged, [sig2, Latch.ar(InFeedback.ar(in3), trig3)]).abs;

				Out.ar(out0Bus, sig0);
				Out.ar(out1Bus, sig1);
				Out.ar(out2Bus, sig2);
				Out.ar(out3Bus, sig3);

			}).writeDefFile;

			SynthDef("noiseSources_analogMod", {arg whiteOutBus, pinkOutBus, metallicOutBus, gate=1, pauseGate = 1, localPauseGate = 1;
				var metallic, tones, freqs, noiseTemp, tonesA, tonesB;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				Out.ar(whiteOutBus, WhiteNoise.ar(1));
				Out.ar(pinkOutBus, PinkNoise.ar(1));

				freqs = [170, 252, 759, 1026, 1402, 2117.4];

				freqs = freqs.collect({arg item; LFNoise2.kr(0.01).range(item-5, item+5)});

				tones = Mix(Pulse.ar(freqs,LFNoise1.kr(0.1).range(0.4, 0.6)!6,1));
				tones = LPF.ar(tones, 4000)+LPF.ar(WhiteNoise.ar(2), 6000);
				tones = (tones).softclip;

				Out.ar(metallicOutBus, tones);
			}).writeDefFile;
		}
	}

	init {

		this.initAnalogBusses;

		this.makeWindow("RandoCalrissian",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(32);

		//busses 61-64 are the out busses, bus 0 is a null bus that all busses are linked to when they are not active, bus 65 is a garbage out bus where all data goes that is not being used

		2.do{synths.add(Synth("randoCalrissianFluctuating_analogMod", [\cvInBus, localBusses[0], \cvInPlugged, 0, \rateAdjustAtten, 0, \rateIn, 0, \cvOutBus, garbageBus, \gateBus, garbageBus], group, \addToTail))};

		synths.add(Synth("randoCalrissianQuantizedRandoms_analogMod", [\quantizationInBus, localBusses[0], \quantizationPlugged, 0, \triggerInBus, localBusses[0], \triggerInPlugged, 0, \quantizationIn, 0, \twoNOutbus, garbageBus, \nPlus1Outbus, garbageBus], group, \addToTail));

		synths.add(Synth("randoCalrissianAnalogShiftRegister_analogMod", [\in0, localBusses[0], \in1, localBusses[0], \in2, localBusses[0], \in3, localBusses[0], \trig0In, localBusses[0], \trig1In, localBusses[0], \trig2In, localBusses[0], \trig3In, localBusses[0], \bigTrigIn, localBusses[0], \in1Plugged, 0, \in2Plugged, 0, \in3Plugged, 0, \bigTrigPlugged, 0, \trig0Plugged, 0, \trig1Plugged, 0, \trig2Plugged, 0, \trig3Plugged, 0, \out0Bus, garbageBus, \out1Bus, garbageBus, \out2Bus, garbageBus, \out3Bus, garbageBus], group, \addToTail));

		synths.add(Synth("noiseSources_analogMod", [\whiteOutBus, garbageBus, \pinkOutBus, garbageBus, \metallicOutBus, garbageBus], group));

		controlsStartSize = controls.size;

		texts = ["cvIn", "cvOut", "gate", "cvIn", "cvOut", "gate", "trigger", "quantIn", "2**n", "n+1", "trig", "out0", "out1", "out2", "out3", "trig0", "trig1", "trig2", "trig3", "cv0", "cv1", "cv2", "cv3", "WhiteNoise", "PinkNoise", "Metallic"].collect{arg item; StaticText().string_(item)};

		//fluctuating 0
		this.makePlugIn(synths[0], \cvInBus, \cvInPlugged);

		controls.add(QtEZSlider("atten", ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[0].set(\rateAdjustAtten, slider.value);
		}, 0, true, \horz));

		controls.add(QtEZSlider("rate", ControlSpec(0, 1, 'lin'), {arg slider;
			synths[0].set(\rateIn, slider.value);
		}, 0, true, \horz));

		this.makePlugOut(synths[0], \cvOutBus);
		this.makePlugOut(synths[0], \gateBus);


		//fluctuating 1

		this.makePlugIn(synths[1], \cvInBus, \cvInPlugged);

		controls.add(QtEZSlider("atten", ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[1].set(\rateAdjustAtten, slider.value);
		}, 0, true, \horz));

		controls.add(QtEZSlider("rate", ControlSpec(0, 1, 'lin'), {arg slider;
			synths[1].set(\rateIn, slider.value);
		}, 0, true, \horz));

		this.makePlugOut(synths[1], \cvOutBus);
		this.makePlugOut(synths[1], \gateBus);


		//quantized randoms

		this.makePlugIn(synths[2], 'triggerInBus', 'triggerInPlugged');
		this.makePlugIn(synths[2], 'quantizationInBus', 'quantizationPlugged');

		controls.add(QtEZSlider("N", ControlSpec(1, 6, 'lin', 1), {arg slider;
			synths[2].set(\quantizationIn, slider.value);
		}, 0, true, \horz));

		this.makePlugOut(synths[2], \twoNOutbus);
		this.makePlugOut(synths[2], \nPlus1Outbus);

		//analog shift register
		[\out0Bus, \out1Bus, \out2Bus, \out3Bus].do{arg item; this.makePlugOut(synths[3], item)};
		[[\bigTrigIn, \bigTrigPlugged], [\trig0In, \trig0Plugged], [\trig1In, \trig1Plugged], [\trig2In, \trig2Plugged], [\trig3In, \trig3Plugged]].do{arg item; this.makePlugIn(synths[3], item[0], item[1])};

		controls.add(Button().states_([["trig", Color.black, Color.red]]).mouseDownAction = {synths[3].set(\t_trig, 1)});
		[[\in0, \in0Plugged], [\in1, \in1Plugged], [\in2, \in2Plugged], [\in3, \in3Plugged]].do{arg item; this.makePlugIn(synths[3], item[0], item[1])};

		[\whiteOutBus, \pinkOutBus, \metallicOutBus].do{|item| this.makePlugOut(synths[4], item)};

		texts = ["cvIn", "cvOut", "gate",
			"cvIn", "cvOut", "gate", //3
			"trigger", "quantIn", "2**n", "n+1", //6
			"out0", "out1", "out2", "out3", "trig", "trig0", "trig1", "trig2", "trig3", "cv0", "cv1", "cv2", "cv3", //10
			"WhiteNoise", "PinkNoise", "Metallic"].collect{arg item; StaticText().string_(item)};//23

		win.layout = VLayout(
			HLayout(texts[0], controls[0], controls[1].layout, controls[2].layout, VLayout(HLayout(texts[1], controls[3]), HLayout(texts[2], controls[4]))),
			HLayout(texts[3], controls[5], controls[6].layout, controls[7].layout, VLayout(HLayout(texts[4], controls[8]), HLayout(texts[5], controls[9]))),
			HLayout(VLayout(HLayout(texts[6], controls[10]), HLayout(texts[7], controls[11])), controls[12].layout, nil, VLayout(HLayout(texts[8], controls[13]), HLayout(texts[9], controls[14]))),

			HLayout(nil, nil, texts[10], controls[15], texts[11], controls[16], texts[12], controls[17], texts[13], controls[18], nil, texts[23], controls[29]),
			HLayout(texts[14], controls[19], texts[15], controls[20], texts[16], controls[21], texts[17], controls[22], texts[18], controls[23],nil, texts[24], controls[30]),
			HLayout(nil, controls[24], texts[19], controls[25], texts[20], controls[26], texts[21], controls[27], texts[22], controls[28],nil, texts[25], controls[31])
		);
	}

}
