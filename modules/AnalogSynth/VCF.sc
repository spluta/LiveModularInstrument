VCF_AnalogMod : AnalogModule_Mod {

	var transferBus;

	*initClass {
		StartUp.add {

			SynthDef("VCF", {arg inBus, inBusPlugged, inAtten, cutoff, cutoffCV, cutoffCVPlugged, qRes, qResCV, qResCVPlugged, clipType, fm1CV, fm1Plugged, fm1Atten, fm2CV, fm2Plugged, fm2Atten, filterMode, outBus, gate=1, pauseGate = 1, localPauseGate = 1;

				var freq, in, out, res, fm1, fm2;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				in = InFeedback.ar(inBus)*inBusPlugged*inAtten;
				freq = Select.ar(cutoffCVPlugged, [K2A.ar(cutoff), InFeedback.ar(cutoffCV)]).linexp(0, 1, 20, 20000);
				res = Select.ar(qResCVPlugged, [K2A.ar(qRes), InFeedback.ar(qResCV)]);

				fm1 = InFeedback.ar(fm1CV);
				fm1 = fm1*fm1Plugged*3000*fm1Atten;
				fm2 = InFeedback.ar(fm2CV);
				fm2 = fm2*fm2Plugged*3000*fm2Atten;

				freq = freq+fm1+fm2;

				out = SelectX.ar(filterMode, [MoogFF.ar(in, freq, res.linlin(0,1,0,4), mul:3), //LPF0
					DFM1.ar(in, freq, res.linlin(0,1,0.1,2)), //LPF1
					Resonz.ar(in, freq, res.linexp(0,1,2,0.001).clip(0.001, (22000-(freq/2))/freq), res.linexp(0,1,2,12)), //BP
					DFM1.ar(in, freq, res.linlin(0,1,0.1,2), type:1), //HPF1
					RHPF.ar(in, freq, res.linlin(0,1,2,0.1).clip(0.1, (22000-(freq/2))/freq), 1),
					BRF.ar(in, freq, res.linlin(0,1,0,1,3))
				]);

				out = Select.ar(clipType, [out.clip(-1, 1), out.softclip]);

				out = ReplaceBadValues.ar(out);

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		this.initAnalogBusses;

		this.makeWindow("VCF",Rect(500, 500, 250, 300));
		this.initControlsAndSynths(12);

		texts = ["in","1v/Oct","FM1","FM2","qRes","0-lpf 1-lpf 2-bpf 3-hpf 4-hpf 6-brf", "out"].collect{arg item; StaticText().string_(item)};

		synths.add(Synth("VCF", [\inBus, localBusses[0], \inBusPlugged, 0, \inAtten, 0, \cutoff, 0.1, \cutoffCV, localBusses[0], \cutoffCVPlugged, 0, \qRes, 0, \qResCV, localBusses[0], \qResCVPlugged, 0, \clipType, 0, \fm1CV, localBusses[0], \fm1Plugged, 0, \fm1Atten, 0, \fm2CV, localBusses[0], \fm2Plugged, 0, \fm2Atten,0 , \filterMode, 0, \outBus, garbageBus], group));

		this.makePlugIn(synths[0], \inBus, \inBusPlugged);
		controls.add(QtEZSlider("atten", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[0].set(\inAtten, slider.value)}, 0, true, \horz, false));

		controls.add(QtEZSlider("cutoff", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[0].set(\cutoff, slider.value)}, 0, true, \horz, false));
		this.makePlugIn(synths[0], \cutoffCV, \cutoffCVPlugged);

		this.makePlugIn(synths[0], \fm1CV, \fm1Plugged);
		controls.add(QtEZSlider(nil, ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[0].set(\fm1Atten, slider.value)}, 0, true, \horz, true));

		this.makePlugIn(synths[0], \fm2CV, \fm2Plugged);
		controls.add(QtEZSlider(nil, ControlSpec(-1, 1, 'lin', 0.001), {arg slider; synths[0].set(\fm2Atten, slider.value)}, 0, true, \horz, true));

		this.makePlugIn(synths[0], \qResCV, \qResCVPlugged);
		controls.add(QtEZSlider(nil, ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[0].set(\qRes, slider.value)}, 0, true, \horz, true));

		controls.add(QtEZSlider(nil, ControlSpec(0, 5, 'lin', 0.25), {arg slider; synths[0].set(\filterMode, slider.value)}, 0, true, \horz, true));
		this.makePlugOut(synths[0], \outBus);

		win.layout = VLayout(
			HLayout(texts[0], controls[0]),
			controls[1].layout,
			nil,
			controls[2].layout,
			HLayout(texts[1], controls[3]),
			nil,
			HLayout(texts[2], controls[4]),
			controls[5].layout,
			nil,
			HLayout(texts[3], controls[6]),
			controls[7].layout,
			nil,
			HLayout(texts[4], controls[8]),
			controls[9].layout,
			nil,
			texts[5],
			controls[10].layout,
			nil,
			HLayout(texts[6], controls[11])
		)

	}

}