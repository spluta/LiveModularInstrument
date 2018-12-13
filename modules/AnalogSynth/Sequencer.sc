Sequencer_AnalogMod : AnalogModule_Mod {

	var oscFunc,sliderVals,seqButtons, seqLayout, currentButton;

	*initClass {
		StartUp.add {

			SynthDef("Sequencer_analogMod", {arg startStop, dur, t_trig, trigInBus, trigInPlugged, countTo, jumpLo, jumpHi, trigOutBus, trigOutPlugged, counterInBus, counterInBusPlugged, counterOutBus, valBus, resetVal=0, sliderVals = #[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]/*23*/, gate=1, pauseGate = 1, localPauseGate = 1;

				var impulse, count, val;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				impulse = (Impulse.ar(1/dur)*startStop)+(InFeedback.ar(trigInBus)*trigInPlugged)+Decay2.ar(K2A.ar(t_trig), 0.01, 0.01);

				count = Stepper.ar(impulse, 0, 0, countTo, TIRand.ar(jumpLo, jumpHi, impulse), resetVal);

				count = Select.ar(counterInBusPlugged, [count, InFeedback.ar(counterInBus)]);

				SendReply.ar(impulse, '/sequencer', count, NodeID.ir);

				val = Select.ar(count, K2A.ar(sliderVals));

				//SendTrig.ar(impulse, id, count);
				Out.ar(counterOutBus, count);
				Out.ar(valBus, val);
				Out.ar(trigOutBus, impulse);

			}).writeDefFile;
		}
	}

	init {
		this.initAnalogBusses;

		this.makeWindow("Sequencer",Rect(500, 500, 250, 300));
		this.initControlsAndSynths(33);

		synths.add(Synth("Sequencer_analogMod", [\startStop, 0, \dur, 0.5, \t_trig, 0, \trigInBus, localBusses[0], \trigInPlugged, 0, \countTo, 17, \jumpLo, 1, \jumpHi, 1, \trigOutBus, garbageBus, \trigOutPlugged, 0, \counterInBus, localBusses[0], \counterInBusPlugged, 0, \counterOutBus, garbageBus, \valBus, garbageBus], group));

		texts = ["trigIn","counterIn","Outs:","trig","counter","value"].collect{arg item; StaticText().string_(item)};

		controls.add(Button()
			.states_([["Stopped", Color.black, Color.red],["Going", Color.black, Color.green]])
				.action_({arg butt; synths[0].set(\startStop, butt.value);})
			);

		controls.add(Button()
			.states_([["Trig", Color.black, Color.blue]])
				.mouseDownAction_({arg butt; synths[0].set(\t_trig, 1);})
			);

		this.makePlugIn(synths[0], \trigInBus, \trigInPlugged);
		this.makePlugIn(synths[0], \counterInBus, \counterInPlugged);

		controls.add(QtEZSlider("jump Low", ControlSpec(1, 4, 'lin', 1), {arg slider; synths[0].set(\jumpLo, slider.value)}, 0, true, \horz));
		controls.add(QtEZSlider("jump Hi", ControlSpec(1, 4, 'lin', 1), {arg slider; synths[0].set(\jumpHi, slider.value)}, 0, true, \horz));
		controls.add(QtEZSlider("count to", ControlSpec(0, 22, 'lin', 1), {arg slider; synths[0].set(\countTo, slider.value)}, 17, true, \horz));
		controls.add(QtEZSlider("dur", ControlSpec(0.01, 10, 'exp', 0.001), {arg slider; synths[0].set(\dur, slider.value)}, 1, true, \horz));

		this.makePlugOut(synths[0], \trigOutBus);
		this.makePlugOut(synths[0], \counterOutBus);
		this.makePlugOut(synths[0], \valBus);

		currentButton = 0;

		sliderVals = Array.fill(23, {0});
		seqButtons = List.newClear(0);
		23.do{|i|
			controls.add(QtEZSlider(nil, ControlSpec(0, 1, 'lin', 0.001), {arg slider;
				sliderVals.put(i, slider.value);
				synths[0].setn(\sliderVals, sliderVals);
			}, rrand(0.0,1.0), true, \vert, false));
			seqButtons.add(Button()
				.states_([["", Color.black, Color.black],["",Color.yellow, Color.yellow]])
				.mouseDownAction_({arg butt;
					synths[0].set(\resetVal, i, \t_trig, 1);
					{seqButtons[currentButton].value_(0);
					currentButton = i}.defer;
				})
			);
		};

		oscFunc = OSCFunc({|msg|
			if (msg[2]==synths[0].nodeID, {
				{
					seqButtons[currentButton].value_(0);
					seqButtons[msg[3]].value_(1);
					currentButton = msg[3];
				}.defer;
			})
		}, '/sequencer');

		seqLayout = Array.fill(23, {arg i; VLayout(controls[i+11].layout, seqButtons[i])});

		win.layout = HLayout(
			VLayout(
				HLayout(controls[0], controls[1], texts[0], controls[2], texts[1], controls[3]),
				HLayout(controls[4].layout, controls[5].layout),
				HLayout(controls[6].layout, controls[7].layout),
				texts[2],
				HLayout(texts[3], controls[8], texts[4], controls[9], texts[5], controls[10])
			),
			HLayout(*seqLayout)
		);
	}

	killMeSpecial {
		oscFunc.free;
	}
}