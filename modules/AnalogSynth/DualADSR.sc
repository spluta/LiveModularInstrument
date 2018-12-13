DualADSR_AnalogMod : AnalogModule_Mod {

	var transferBus;

	*initClass {
		StartUp.add {

			SynthDef("DualADSR0_analogMod", {arg attack, decay, sustain, release, trig, cycleOn, maxDur, gateIn, gatePlugged, retrigIn, retrigPlugged, levelIn, levelPlugged, envBus, inverseBus, eodBus, transferBus, gate=1, pauseGate = 1, localPauseGate = 1;

				var envB, impulse, level, doneTrig, localTrigIn, gateB;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				localTrigIn = LocalIn.ar(1)*cycleOn;
				InFeedback.ar(gateIn);

				gateB = (Trig1.ar(trig+localTrigIn+(gatePlugged*InFeedback.ar(gateIn)), (attack+decay)*maxDur)+trig+(gatePlugged*InFeedback.ar(gateIn))-(InFeedback.ar(retrigIn)*retrigPlugged)).clip(-0.1,1); //I think the subtraction works here

				Out.ar(transferBus, gateB);

				level = Select.ar(levelPlugged, [K2A.ar(0.75), InFeedback.ar(levelIn)]);

				envB = EnvGen.ar(Env.adsr(attack*maxDur, decay*maxDur, sustain, release*maxDur, level), gateB);

				//SendPeakRMS.kr(env, 10, 3, '/dualADSR0', groupID);

				Out.ar(envBus, envB);
				Out.ar(inverseBus, envB.neg);

				doneTrig = Trig1.ar(K2A.ar(Done.kr(envB)), 0.01);
				Out.ar(eodBus, doneTrig);

				LocalOut.ar(doneTrig);
			}).writeDefFile;


			SynthDef("DualADSR1_analogMod", {arg attack, decay, sustain, release, trig, cycleOn, maxDur, gateIn, gatePlugged, retrigIn, retrigPlugged, levelIn, levelPlugged, envBus, inverseBus, eodBus, transferBus, gate=1, pauseGate = 1, localPauseGate = 1;

				var envB, impulse, level, doneTrig, localTrigIn, gateB, transferGate;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				localTrigIn = LocalIn.ar(1)*cycleOn;

				gateB = (Trig1.ar(trig+localTrigIn+(gatePlugged*InFeedback.ar(gateIn))-0.1, (attack+decay)*maxDur)+trig+(gatePlugged*InFeedback.ar(gateIn))-(InFeedback.ar(retrigIn)*retrigPlugged)).clip(-0.1,1); //I think the subtraction works here

				transferGate = In.ar(transferBus);

				level = Select.ar(levelPlugged, [K2A.ar(0.75), InFeedback.ar(levelIn)]);

				gateB = Select.ar((gatePlugged+cycleOn+trig).clip(0,1), [transferGate, gateB]);

				envB = EnvGen.ar(Env.adsr(attack*maxDur, decay*maxDur, sustain, release*maxDur, level), gateB);

				//SendPeakRMS.kr(env, 10, 3, '/dualADSR0', groupID);

				Out.ar(envBus, envB);
				Out.ar(inverseBus, envB.neg);

				doneTrig = Trig1.ar(K2A.ar(Done.kr(envB)), 0.01);
				Out.ar(eodBus, doneTrig);

				LocalOut.ar(doneTrig);
			}).writeDefFile;
		}
	}

	init {
		this.initAnalogBusses;

		this.makeWindow("DualADSR",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(26);

		transferBus = Bus.audio(group.server);

		texts = ["s-m-f", "adsr", "gate", "retrig", "level", "envOut", "invEnv", "eod"].dup.flatten.collect{arg item; StaticText().string_(item)};

		synths.add(Synth("DualADSR0_analogMod", [\attack, 0.1, \decay, 0.1, \sustain, 0.1, \release, 0.1, \trig, 0, \cycleOn, 0, \maxDur, 1, \gateIn, localBusses[0], \gatePlugged, 0, \retrigIn, localBusses[0], \retrigPlugged, 0, \levelIn, localBusses[0], \levelPlugged, 0, \envBus, garbageBus, \inverseBus, garbageBus, \eodBus, garbageBus, \transferBus, transferBus], group, \addToTail));
		synths.add(Synth("DualADSR1_analogMod", [\attack, 0.1, \decay, 0.1, \sustain, 0.1, \release, 0.1, \trig, 0, \cycleOn, 0, \maxDur, 1, \gateIn, localBusses[0], \gatePlugged, 0, \retrigIn, localBusses[0], \retrigPlugged, 0, \levelIn, localBusses[0], \levelPlugged, 0, \envBus, garbageBus, \inverseBus, garbageBus, \eodBus, garbageBus, \transferBus, transferBus], group, \addToTail));

		2.do{arg i;

			controls.add(Button()
			.states_([["Trig", Color.black, Color.blue]])
				.mouseDownAction_({arg butt; synths[i].set(\trig, 1);})
				.action_({arg butt; synths[i].set(\trig, 0);})
			);

			controls.add(Button()
			.states_([["CycleOff", Color.black, Color.red],["CycleOn", Color.black, Color.green]])
				.action_({arg butt;
					synths[i].set(\cycleOn, butt.value);
					if(butt.value==1, {synths[i].set(\trig, 1);SystemClock.sched(0.1, {synths[i].set(\trig, 0)})});
				})
			);

			controls.add(QtEZSlider("rate", ControlSpec(0, 2, 'lin', 1), {arg slider;
				switch(slider.value.asInteger,
					0, {synths[i].set(\maxDur, 1)},
					1, {synths[i].set(\maxDur, 10)},
					2, {synths[i].set(\maxDur, 60)}
				);
			}, 0, true, \horz));

			controls.add(QtEZSlider(nil, ControlSpec(0.01, 0.33, 'lin', 0.001), {arg slider; synths[i].set(\attack, slider.value)}, 0, true, \vert, false));
			controls.add(QtEZSlider(nil, ControlSpec(0.01, 0.33, 'lin', 0.001), {arg slider; synths[i].set(\decay, slider.value)}, 0, true, \vert, false));
			controls.add(QtEZSlider(nil, ControlSpec(0.01, 1, 'lin', 0.001), {arg slider; synths[i].set(\sustain, slider.value)}, 0, true, \vert, false));
			controls.add(QtEZSlider(nil, ControlSpec(0.01, 0.33, 'lin', 0.001), {arg slider; synths[i].set(\release, slider.value)}, 0, true, \vert, false));

			this.makePlugIn(synths[i], \gateIn, \gatePlugged);
			this.makePlugIn(synths[i], \retrigIn, \retrigPlugged);
			this.makePlugIn(synths[i], \levelIn, \levelPlugged);

			this.makePlugOut(synths[i], \envBus);
			this.makePlugOut(synths[i], \inverseBus);
			this.makePlugOut(synths[i], \eodBus);
		};

		win.layout = VLayout(
			HLayout(
				VLayout(controls[0], controls[1], texts[0], controls[2].layout),
				VLayout(texts[1], HLayout(controls[3].layout, controls[4].layout, controls[5].layout, controls[6].layout)),
			),
			HLayout(texts[2], controls[7], texts[3], controls[8], texts[4], controls[9]),
			HLayout(texts[5], controls[10], texts[6], controls[11], texts[7], controls[12]),

			HLayout(
				VLayout(controls[13], controls[14], texts[8], controls[15].layout),
				VLayout(texts[9], HLayout(controls[16].layout, controls[17].layout, controls[18].layout, controls[19].layout)),
			),
			HLayout(texts[10], controls[20], texts[11], controls[21], texts[12], controls[22]),
			HLayout(texts[13], controls[23], texts[14], controls[24], texts[15], controls[25])
		)
	}

}

VCA_AnalogMod : AnalogModule_Mod {

	var transferBus, layouts;

	*initClass {
		StartUp.add {

			SynthDef("VCA_analogMod", {arg bias, inBus, inBusPlugged, cvBus, cvBusPlugged, outBus, transferBus, volume, gate=1, pauseGate = 1, localPauseGate = 1;
				var in, cv, out;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				in = InFeedback.ar(inBus)*inBusPlugged;
				cv = InFeedback.ar(cvBus)*cvBusPlugged;
				out = (in*bias)+(in*cv*(1-bias)*volume);

				Out.ar(outBus, out);
				Out.ar(transferBus, out);
			}).writeDefFile;

			SynthDef("VCASum_analogMod", {arg bias, inBus, inBusPlugged, cvBus, cvBusPlugged, outBus, transferBus, volume, gate=1, pauseGate = 1, localPauseGate = 1;
				var transferIn, in, cv, out;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				in = (InFeedback.ar(inBus)*inBusPlugged);
				cv = InFeedback.ar(cvBus)*cvBusPlugged;
				out = (in*bias)+(in*cv*(1-bias)*volume);

				transferIn = Mix(In.ar(transferBus, 5));
				transferIn = (transferIn*bias)+(transferIn*Select.ar(cvBusPlugged,[K2A.ar(1), cv])*(1-bias)*volume);

				out = Select.ar(inBusPlugged, [transferIn, out]);

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		this.initAnalogBusses;

		this.makeWindow("VCA",Rect(500, 500, 180, 300));
		this.initControlsAndSynths(30);

		transferBus = Bus.audio(group.server, 5);

		texts = ["in","cv","out"].dup(5).flatten.addAll(["sum", "cv", "out"]).collect{arg item; StaticText().string_(item)};

		5.do{|i|
			synths.add(Synth("VCA_analogMod", [\bias, 0, \inBus, localBusses[0], \inBusPlugged, 0, \cvBus, localBusses[0], \cvBusPlugged, 0, \outBus, garbageBus, \transferBus, transferBus.index+i, \volume, 1], group, \addToTail));

			this.makePlugIn(synths[i], \inBus, \inBusPlugged);
			controls.add(QtEZSlider("bias", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[i].set(\bias, slider.value)}, 0, true, \vert, false));
			controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[i].set(\volume, slider.value)}, 1, true, \vert, false));
			this.makePlugIn(synths[i], \cvBus, \cvBusPlugged);
			this.makePlugOut(synths[i], \outBus);
		};

		synths.add(Synth("VCASum_analogMod", [\bias, 0, \inBus, localBusses[0], \inBusPlugged, 0, \cvBus, localBusses[0], \cvBusPlugged, 0, \outBus, garbageBus, \transferBus, transferBus, \volume, 1], group, \addToTail));
		this.makePlugIn(synths[5], \inBus, \inBusPlugged);
		controls.add(QtEZSlider("bias", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[5].set(\bias, slider.value)}, 0, true, \vert, false));
		controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'lin', 0.001), {arg slider; synths[5].set(\volume, slider.value)}, 1, true, \vert, false));
		this.makePlugIn(synths[5], \cvBus, \cvBusPlugged);
		this.makePlugOut(synths[5], \outBus);

		layouts = List.newClear(0);

		6.do{|i2|
			layouts.add(VLayout(texts[i2*3], controls[i2*5], controls[i2*5+1].layout, controls[i2*5+2].layout, texts[i2*3+1], controls[i2*5+3], texts[i2*3+2], controls[i2*5+4]));
		};

		win.layout = HLayout(*layouts)
	}
}

		