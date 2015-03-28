Convolution_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("convolution_mod", {arg inBus0, inBus1, outBus, vol, lpFreq=2000, gate = 1, pauseGate = 1;
				var in0, in1, env, out, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = Mix(In.ar(inBus0, 8));
				in1 = Mix(In.ar(inBus1, 8));

				out = Convolution.ar(in0, in1, 2048)*env*vol*pauseEnv;

				Out.ar(outBus, out, 0);
			}).writeDefFile;
		}
	}

	init2 {
		"Convolution".postln;

		this.makeWindow("Convolution", Rect(860, 200, 220, 150));
		this.initControlsAndSynths(1);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		2.do{localBusses.add(Bus.audio(group.server, 8))};
		localBusses.postln;


		mixerStrips = List.new;
		2.do{arg i; mixerStrips.add(DiscreteInput_Mod(mixerGroup, localBusses[i], win, Point(5+(i*55), 0), nil))};

		synths = List.newClear(3);
		synths.put(0, Synth("convolution_mod", [\inBus0, localBusses[0].index, \inBus1, localBusses[1].index, \outBus, outBus.index, \vol, 1], synthGroup));
		controls = List.new;
		controls.add(EZSlider.new(win,Rect(10, 70, 200, 20), "vol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
			}, 1.0, true, layout:\horz));
		this.addAssignButton(0,\continuous,Rect(10, 90, 200, 20));
	}
}
