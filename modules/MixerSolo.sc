MixerSolo_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("mixerSolo_mod", {arg inBus, outBus, gate=1, pauseGate=1;
				var env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus,2)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeMixerToSynthBus(8);
		this.initControlsAndSynths(0);

		modName = "MixerSolo";
		synths = List.new;
		synths.add(Synth("mixerSolo_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));
	}

	killMe {
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
		mixerToSynthBus.free;
	}

	show {
	}

	hide {
	}
}

MixerSoloMono_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("mixerSoloMono_mod", {arg inBus, outBus, gate=1, pauseGate=1;
				var env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus,1)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeMixerToSynthBus(1);
		this.initControlsAndSynths(0);
		modName = "MixerSoloMono";
		synths = List.new;
		synths.add(Synth("mixerSoloMono_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));
	}

	killMe {
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
		mixerToSynthBus.free;
	}

	show {
	}

	hide {
	}
}
