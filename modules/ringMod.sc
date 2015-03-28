RingModStereo_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("ringModStereo", {arg inBus0, inBus1, outBus, vol, lpFreq=2000, gate = 1, pauseGate = 1;
				var in0, in1, env, out, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = In.ar(inBus0, 8);
				in1 = In.ar(inBus1, 8);

				out = in0*in1;

				out = LPF.ar(out, lpFreq)*20*env*vol*pauseEnv;
				Out.ar(outBus, out, 0);
			}).writeDefFile;
		}
	}

	init2 {
		"RingModStereo".postln;
		this.makeWindow("RingModStereo", Rect(860, 200, 220, 150));
		this.initControlsAndSynths(2);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		synths = List.new;

		localBusses = List.new;
		2.do{localBusses.add(Bus.audio(group.server, 8))};
		localBusses.postln;


		mixerStrips = List.new;
		2.do{arg i; mixerStrips.add(DiscreteInput_Mod(mixerGroup, localBusses[i], win, Point(5+(i*55), 0)))};

		synths = List.newClear(3);
		synths.put(0, Synth("ringModStereo", [\inBus0, localBusses[0].index, \inBus1, localBusses[1].index, \outBus, outBus.index, \vol, 1], synthGroup));

		controls.add(EZSlider.new(win,Rect(10, 70, 200, 20), "vol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
			}, 1.0, true, layout:\horz));
		this.addAssignButton(0,\continuous, Rect(10, 90, 200, 20));

		controls.add(EZSlider.new(win,Rect(10, 110, 200, 20), "lpFreq", ControlSpec(2000,20000,'exp'),
			{|v|
				synths[0].set(\lpFreq, v.value);
			}, 2000, true, layout:\horz));
		this.addAssignButton(1,\continuous, Rect(10, 130, 200, 20));
	}
}
