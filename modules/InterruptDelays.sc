InterruptDelays_Mod : Module_Mod {
	var volBus, delRange, delGap, delGaps;

	*initClass {
		StartUp.add {
			SynthDef("interruptDelays_mod", {arg inBus, outBus, volBus, delGap0 = 0.5, delGap1 = 0.5, delGap2 = 0.5, delGap3 = 0.5, delGap4 = 0.5, delGap5 = 0.5, delGap6 = 0.5, delGap7 = 0.5, delaySwitch=0, gate = 1, pauseGate = 1;
				var in, out, out0, out1, out2, out3, out4, out5, out6, out7, vol, env, pauseEnv;

				vol = In.kr(volBus);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = In.ar(inBus, 8);

				out0 = Mix(DelayC.ar(in[0], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out1 = Mix(DelayC.ar(in[1], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out2 = Mix(DelayC.ar(in[2], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out3 = Mix(DelayC.ar(in[3], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out4 = Mix(DelayC.ar(in[4], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out5 = Mix(DelayC.ar(in[5], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out6 = Mix(DelayC.ar(in[6], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));
				out7 = Mix(DelayC.ar(in[7], 4.0, [delGap0, delGap2, delGap3, delGap4, delGap5, delGap6, delGap7]));

				out = [out0, out1, out2, out3, out4, out5, out6, out7];

				out = in+(LagUD.kr(delaySwitch, 0.05, 0.2)*out*vol);

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("InterruptDelays", Rect(500,100,80,280));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(8);

		volBus = Bus.control(group.server);

		volBus.set(0);

		synths = List.newClear(4);

		delRange = [0.2,0.3];
		delGaps = List.newClear(8);
		this.calcDelGap;

		synths.put(0, Synth("interruptDelays_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, true, orientation:\horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZRanger("delRange", ControlSpec(0.1, 0.5),
			{|val|
				delRange = val.value;
		}, orientation:\horz));
		this.addAssignButton(1,\range);

		controls.add(Button.new()
			.states_([ [ "Pass", Color.green, Color.black ], [ "Delay", Color.black, Color.green ]])
			.action_({|v|
				this.calcDelGap;
				synths[0].set(\delaySwitch, v.value, \delGap0, delGaps[0], \delGap1, delGaps[1], \delGap2, delGaps[2], \delGap3, delGaps[3], \delGap4, delGaps[4], \delGap5, delGaps[5], \delGap6, delGaps[6], \delGap7, delGaps[7]);
		}));
		this.addAssignButton(2,\onOff);



		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout),
				HLayout(controls[2], assignButtons[2].layout)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	calcDelGap {
		delGap = rrand(delRange[0], delRange[1]);
		8.do{arg i; delGaps.put(i, delGap*(rrand(i+1, i+2)*delGap))};
		delGaps.postln;
	}

	killMeSpecial {
		volBus.free;
	}
}