ComplexOscillator_AnalogMod : AnalogModule_Mod {

	var transferBus, syncBus;

	*initClass {
		StartUp.add {

			SynthDef("complexOscillatorControl_analogMod", {arg freq=0, vOctBus, vOctPlugged, fmBus, fmPlugged, cvBus, fmAtten, cvAtten, cvPlugged, triBus, sqrBus, sawBus, transferBus, syncBus, synchOn, gate=1, pauseGate = 1, localPauseGate = 1;

				var freqLocal, ampMod, oscils, syncFreq, syncFreqs, env, pauseEnv, localPauseEnv;

				freqLocal = Lag.ar(K2A.ar(freq).linexp(0,1,3,22050));
				freqLocal = Select.ar(cvPlugged, [freqLocal, freqLocal+(((InFeedback.ar(cvBus)*cvAtten)).fold(0,1).linexp(0,1,3,22050))]);
				freqLocal = Select.ar(vOctPlugged, [freqLocal, InFeedback.ar(vOctBus).fold(0,1).linexp(0,1,3,22050)]);
				freqLocal = (freqLocal+Select.ar(fmPlugged, [K2A.ar(0), InFeedback.ar(fmBus)+1*0.5*fmAtten*3000]));

				//this part applies the sync
				syncFreq = InFeedback.ar(syncBus);
				syncFreqs = (syncFreq/[7,6,5,4,3,2]).addAll(syncFreq*[2,3/2,4/3,5/4,6/5,7/6]);

				//[syncFreq/7, syncFreq/6, syncFreqs/5, syncFreqs/4, syncFreqs/3, syncFreqs/2, syncFreqs*2, syncFreqs*3/2, syncFreqs*4/3, syncFreqs*5/4, syncFreqs*6/5, syncFreqs*7/6];
				freqLocal = Select.ar(synchOn, [freqLocal, SelectX.ar((freq-0.01*12).floor, syncFreqs)]);

				oscils = LPF.ar([LFTri.ar(freqLocal), Pulse.ar(freqLocal),Saw.ar(freqLocal)], 20000);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				Out.ar(triBus, oscils[0]);
				Out.ar(sqrBus, oscils[1]);
				Out.ar(sawBus, oscils[2]);
				Out.ar(transferBus, oscils);
			}).writeDefFile;

			SynthDef("complexOscillatorMain_analogMod", {arg freq, vOctBus, vOctPlugged, fmBus, fmPlugged, cvBus, fmAtten, cvAtten, cvPlugged, bigFMOn, bigAMOn, bigModIndex, bigModCVAtten, bigModCVIn, bigModCVPlugged, transferBus, inOscSwitch, triBus, sqrBus, sinBus, masterBus, syncBus, orderIn, orderAtten, orderBus, orderPlugged, timbreIn, timbreAtten, timbreBus, timbrePlugged, gate=1, pauseGate = 1, localPauseGate = 1;

				var freqLocal, modIn, oscil, ampMod, masterOsc, order, timbre, env, pauseEnv, localPauseEnv;

				freqLocal = Lag.ar(K2A.ar(freq).linexp(0,1,3,22050));
				freqLocal = Select.ar(cvPlugged, [freqLocal, freqLocal+(((InFeedback.ar(cvBus)*cvAtten)).fold(0,1).linexp(0,1,3,22050))]);
				freqLocal = Select.ar(vOctPlugged, [freqLocal, InFeedback.ar(vOctBus).fold(0,1).linexp(0,1,3,22050)]);
				freqLocal = (freqLocal+Select.ar(fmPlugged, [K2A.ar(0), InFeedback.ar(fmBus)/*+1*0.5*/*fmAtten*3000]));

				Out.ar(syncBus, freqLocal);

				modIn = Select.ar(inOscSwitch, In.ar(transferBus,3));

				bigModIndex = Select.ar(bigModCVPlugged, [K2A.ar(bigModIndex),
					K2A.ar(bigModIndex)+(InFeedback.ar(bigModCVIn)*bigModCVAtten)]);

				ampMod = Select.ar(bigAMOn, [K2A.ar(1), modIn+1*0.5*bigModIndex]);  //assuming we want the signal above 0

				freqLocal = Select.ar(bigFMOn, [freqLocal, freqLocal+((modIn/*+1*0.5*/*bigModIndex*3000))]);
				//freqLocal = Select.ar(bigFMOn, [freqLocal, freqLocal+((modIn*bigModIndex*3000))]);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				Out.ar(triBus, LFTri.ar(freqLocal)*ampMod);
				Out.ar(sqrBus, LFPulse.ar(freqLocal)*ampMod);
				Out.ar(sinBus, SinOsc.ar(freqLocal)*ampMod);

				order = Lag.ar((orderIn+((orderPlugged*InFeedback.ar(orderBus)*orderAtten).fold(0,1))).clip(0,0.99));

				timbre = Lag.ar((timbreIn+((timbrePlugged*InFeedback.ar(timbreBus)*timbreAtten).fold(0,1))).clip(0,0.99));

				masterOsc = LPF.ar(SelectX.ar(timbre, [
					SinOsc.ar(freqLocal, 0, order.linexp(0,1,1,13)).softclip,

					SelectX.ar(order, [Pulse.ar(freqLocal), Saw.ar(freqLocal), Saw.ar(freqLocal*2)])]), 18000);
				Out.ar(masterBus, masterOsc*ampMod);

			}).writeDefFile;
		}
	}

	init {
		this.initAnalogBusses;


		this.makeWindow("ComplexOscillator",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(31);

		transferBus = Bus.audio(group.server, 3);
		syncBus = Bus.audio(group.server);

		synths.add(Synth("complexOscillatorControl_analogMod", [\freq, 440, \vOctBus, localBusses[0], \vOctPlugged, 0, \fmBus, localBusses[0], \fmPlugged, 0, \cvBus, localBusses[0], \fmAtten, 0, \cvAtten, 0, \cvPlugged, 0, \triBus, garbageBus, \sqrBus, garbageBus, \sawBus, garbageBus, \transferBus, transferBus, \syncBus, syncBus], group, \addToTail));

		synths.add(Synth("complexOscillatorMain_analogMod", [\freq, 440, \vOctBus, localBusses[0], \vOctPlugged, 0, \fmBus, localBusses[0], \fmPlugged, 0, \cvBus, localBusses[0], \fmAtten, 0, \cvAtten, 0, \cvPlugged, 0, \bigFMOn, 0, \bigAMOn, 0, \bigModIndex, 0, \bigModCVAtten, 0, \bigModCVIn, localBusses[0], \bigModCVPlugged, 0, \transferBus, transferBus, \inOscSwitch, 0, \triBus, garbageBus, \sqrBus, garbageBus, \sinBus, garbageBus, \masterBus, garbageBus, \syncBus, syncBus, \orderIn, 0, \orderAtten, 0, \orderBus, localBusses[0], \orderPlugged, 0, \timbreIn, 0, \timbreAtten, 0, \timbreBus, localBusses[0], \timbrePlugged, 0], group, \addToTail));


		this.makePlugOut(synths[0], \triBus);
		this.makePlugOut(synths[0], \sqrBus);
		this.makePlugOut(synths[0], \sawBus);

		controls.add(QtEZSlider("freq", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synths[0].set(\freq, slider.value);
		}, 0, true, \horz));

		this.makePlugIn(synths[0], \fmBus, \fmPlugged);
		controls.add(QtEZSlider("fmAtten", ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[0].set(\fmAtten, slider.value);
		}, 0.5, true, \horz));

		this.makePlugIn(synths[0], \cvBus, \cvPlugged);
		controls.add(QtEZSlider(nil, ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[0].set(\cvAtten, slider.value);
		}, 0.5, true, \horz));
		this.makePlugIn(synths[0], \vOctBus, \vOctPlugged);

		//in the middle

		controls.add(Button()
			.states_([["No Sync", Color.black, Color.red], ["Sync", Color.black, Color.green]])
			.action_({arg butt;
				synths[0].set(\sync, butt.value);
		}));

		controls.add(QtEZSlider("AMnilFM", ControlSpec(0, 2, 'lin', 1), {arg slider;
			switch(slider.value.asInteger,
				0, {synths[1].set(\bigAMOn, 1, \bigFMOn, 0);},
				1, {synths[1].set(\bigAMOn, 0, \bigFMOn, 0)},
				2, {synths[1].set(\bigAMOn, 0, \bigFMOn, 1);});
		}, 1, true, \horz));

		controls.add(QtEZSlider("TrSqSa", ControlSpec(0, 2, 'lin', 1), {arg slider;
			synths[1].set(\inOscSwitch, slider.value)
		}, 1, true, \horz));

		controls.add(QtEZSlider("index", ControlSpec(0, 1, 'lin'), {arg slider;
			synths[1].set(\bigModIndex, slider.value);
		}, 0, true, \horz));

		controls.add(QtEZSlider("atten", ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[1].set(\bigModCVAtten, slider.value);
		}, 0.5, true, \horz));

		this.makePlugIn(synths[1], \bigModCVIn, \bigModCVPlugged);

		//mainOsc

		this.makePlugOut(synths[1], \triBus);
		this.makePlugOut(synths[1], \sqrBus);
		this.makePlugOut(synths[1], \sinBus);
		this.makePlugOut(synths[1], \masterBus);

		controls.add(QtEZSlider("freq", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synths[1].set(\freq, slider.value);
		}, 0, true, \horz));

		this.makePlugIn(synths[1], \fmBus, \fmPlugged);
		controls.add(QtEZSlider("fmAtten", ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[1].set(\fmAtten, slider.value);
		}, 0.5, true, \horz));

		this.makePlugIn(synths[1], \cvBus, \cvPlugged);
		controls.add(QtEZSlider(nil, ControlSpec(-1, 1, 'lin'), {arg slider;
			synths[1].set(\cvAtten, slider.value);
		}, 0.5, true, \horz));
		this.makePlugIn(synths[1], \vOctBus, \vOctPlugged);

		//master

		controls.add(QtEZSlider("order", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synths[1].set(\orderIn, slider.value);
		}, 0, true, \horz));
		this.makePlugIn(synths[1], \orderBus, \orderPlugged);
		controls.add(QtEZSlider("atten", ControlSpec(-1, 1, 'lin', 0.001), {arg slider;
			synths[1].set(\orderAtten, slider.value);
		}, 0, true, \horz));

		controls.add(QtEZSlider("timbre", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synths[1].set(\timbreIn, slider.value);
		}, 0, true, \horz));
		this.makePlugIn(synths[1], \timbreBus, \timbrePlugged);
		controls.add(QtEZSlider("atten", ControlSpec(-1, 1, 'lin', 0.001), {arg slider;
			synths[1].set(\timbreAtten, slider.value);
		}, 0, true, \horz));


		texts = ["tri", "square", "saw", "fmIn", "cvIn", "vOct", "modCVIn", "tri", "square", "sin", "master", "fmIn", "cvIn", "vOct", "cvIn", "cvIn"].collect{arg item; StaticText().string_(item)};

		win.layout = HLayout(
			VLayout(
				HLayout(texts[0], controls[0], texts[1], controls[1], texts[2], controls[2]),
				controls[3].layout,
				HLayout(texts[3], controls[4], controls[5].layout),
				HLayout(texts[4], controls[6], controls[7].layout, texts[5], controls[8])
			),
			VLayout(
				controls[9], controls[10].layout, controls[11].layout, controls[12].layout, controls[13].layout, HLayout(texts[6],controls[14])
			),
			VLayout(
				HLayout(texts[7], controls[15], texts[8], controls[16], texts[9], controls[17], texts[10], controls[18]),
				controls[19].layout,
				HLayout(texts[11], controls[20], controls[21].layout),
				HLayout(texts[12], controls[22], controls[23].layout, texts[13], controls[24])
			),
			VLayout(
				controls[25].layout,
				HLayout(texts[14], controls[26], controls[27].layout),
				controls[28].layout,
				HLayout(texts[15], controls[29], controls[30].layout),
			)
		)
	}

}