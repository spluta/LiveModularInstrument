DistortMono_Mod : Module_Mod {
	var compThreshBus, gainBus, limitBus;

	*initClass {
		StartUp.add {
			SynthDef("distortMonoA_mod", {arg inBus, outBus, compThreshBus, gainBus, limitBus, delayMult, gate = 1, pauseGate = 1;
				var out, outL, outR, pauseEnv, env, compThresh = 0.5, preGain = 1, limit = 0.5;

				compThresh = In.kr(compThreshBus);
				preGain = In.kr(gainBus);
				limit = In.kr(limitBus);

				out = In.ar(inBus);

				out = Compander.ar(out, out, compThresh, 1, 0.5, 0.01, 0.01);

				outL = Limiter.ar((Mix.new(out+DelayN.ar(out, 0.001*delayMult, 0.001*delayMult))*preGain).softclip, limit);

				outR = Limiter.ar((Mix.new(out+DelayN.ar(out, 0.002*delayMult, 0.0015*delayMult))*preGain).softclip, limit);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				out = LeakDC.ar([outL,outR]*pauseEnv*env);

				Out.ar(outBus, out);

			}).writeDefFile;
			SynthDef("distortMonoB_mod", {arg inBus, outBus, compThreshBus, gainBus, limitBus, delayMult, gate = 1, pauseGate = 1;
				var out, outL, outR, pauseEnv, env, compThresh = 0.5, preGain = 1, limit = 0.5;

				compThresh = In.kr(compThreshBus);
				preGain = In.kr(gainBus);
				limit = In.kr(limitBus);

				out = In.ar(inBus);

				out = Compander.ar(out, out, compThresh, 1, 0.5, 0.01, 0.01);

				outL = Limiter.ar((Mix.new(out+DelayN.ar(out, 0.001*delayMult, 0.001*delayMult))*preGain).softclip, limit);

				outR = Limiter.ar((Mix.new(out+DelayN.ar(out, 0.002*delayMult, 0.0015*delayMult))*preGain).softclip, limit);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				out = LeakDC.ar([outR,outL]*pauseEnv*env);

				Out.ar(outBus, out);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("DistortMono", Rect(800, 800, 190, 220));		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		synths = List.newClear(4);

		compThreshBus = Bus.control(group.server);
		gainBus = Bus.control(group.server);
		limitBus = Bus.control(group.server);

		synths.put(0, Synth("distortMonoA_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \compThreshBus, compThreshBus, \gainBus, gainBus, \limitBus, limitBus, \delayMult, 1], group));

		controls.add(EZSlider(win, Rect(5, 0, 60, 200), "CompThresh", ControlSpec(0,1.0,\linear),
			{|val|
				compThreshBus.set(val.value)
			}, 0.5, true, layout:\vert)
		);
		this.addAssignButton(0,\continuous,Rect(5, 200, 60, 20));

		controls.add(EZSlider(win, Rect(65, 0, 60, 200), "PreGain", ControlSpec(0,10,\linear),
			{|val|
				gainBus.set(val.value)
			}, 1, true, layout:\vert)
		);
		this.addAssignButton(1,\continuous,Rect(65, 200, 60, 20));

		controls.add(EZSlider(win, Rect(125, 0, 60, 200), "Limit", ControlSpec(0,1,\amp),
			{|val|
				limitBus.set(val.value)
			}, 0.5, true, layout:\vert)
		);
		this.addAssignButton(2,\continuous,Rect(125, 200, 60, 20));

	}
}