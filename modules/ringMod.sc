RingModStereo_Mod : ModularMainMixer {

	*initClass {
		StartUp.add {
			SynthDef("ringModStereo_mod", {arg inBus0, inBus1, outBus, gate = 1, pauseGate = 1;
				var in0, in1, env, out, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = In.ar(inBus0, 2);
				in1 = In.ar(inBus1, 2);

				out = in0*in1;

				out = out*10*env*pauseEnv;
				Out.ar(outBus, out, 0);
			}).writeDefFile;
		}
	}

	init3 {
		synthName = "RingModStereo";

		win.name = "RingMod"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		synths.add(Synth("ringModStereo_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], outGroup));

		win.layout_(HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0));
		win.front;
	}
}
