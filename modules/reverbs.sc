GVerb_Mod : Module_Mod {
	*initClass {
		StartUp.add {
			SynthDef("gVerb_mod", { arg inBus, outBus, roomSize, revTime, damping, inputBW, spread, dryLevel, earlyLevel, tailLevel, vol, pauseGate=1, gate=1;
				var in, pauseEnv, env, verb;

				in = In.ar(inBus);

				//in = Resonz.ar(Array.fill(4, {Dust.ar(2)}), 1760 * [1, 2, 4, 8], 0.01).sum * 10;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:0);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				verb = GVerb.ar(
					in,
					roomSize,
					revTime,
					damping,
					inputBW,
					spread,
					dryLevel,
					earlyLevel,
					tailLevel,
					roomSize+1);

				Out.ar(outBus, verb*pauseEnv*env*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GVerb",Rect(618, 645, 250, 300));
		this.initControlsAndSynths(8);

		this.makeMixerToSynthBus;

		synths = List.new;

		synths.add(Synth("gVerb_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \roomSize, 16, \revTime, 1.24, \damping, 0.10, \inputBW, 0.95, \dryLevel, 0.0, \earlyLevel, 0.5, \tailLevel, 0.2, \vol, 0.3], group));

		controls.add(EZSlider.new(win,Rect(5, 0, 60, 220), "vol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0.3, true, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(5, 220, 60, 20));

		controls.add(EZKnob.new(win,Rect(65, 0, 60, 100), "size", ControlSpec(1,300,'exponential'),
			{|v|
				synths[0].set(\roomSize, v.value);
			}, 5, true, layout:\vert));

		controls.add(EZKnob.new(win,Rect(65, 100, 60, 100), "time", ControlSpec(0.01,100,'exponential'),
			{|v|
				synths[0].set(\revTime, v.value);
			}, 1.24, true));

		controls.add(EZKnob.new(win,Rect(125, 0, 60, 100), "damp", ControlSpec(0.01,1),
			{|v|
				synths[0].set(\damping, v.value);
			}, 0.1, true));

		controls.add(EZKnob.new(win,Rect(125, 100, 60, 100), "inDmp", ControlSpec(0.01,1),
			{|v|
				synths[0].set(\inputBW, v.value);
			}, 0.1, true));

		controls.add(EZKnob.new(win,Rect(185, 0, 60, 100), "dry", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\inputBW, v.value);
			}, 0, true));

		controls.add(EZKnob.new(win,Rect(185, 100, 60, 100), "early", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\earlyLevel, v.value);
			}, 0.5, true));

		controls.add(EZKnob.new(win,Rect(65, 200, 60, 100), "tail", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\tailLevel, v.value);
			}, 0.2, true));
	}
}