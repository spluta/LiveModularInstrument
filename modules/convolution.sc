Convolution_Mod : MainMixer {

	*initClass {
		StartUp.add {
			SynthDef("convolution_mod", {arg inBus0, inBus1, outBus, gate = 1, pauseGate = 1;
				var in0, in1, env, out, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = In.ar(inBus0, 2);
				in1 = In.ar(inBus1, 2);

				out = [Convolution.ar(in0[0], in1[0], 2048),Convolution.ar(in0[1], in1[1], 2048)]*env*pauseEnv;

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init3 {
		synthName = "Convolution";

		synths.add(Synth("convolution_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], outGroup));

		win.name_(outBus.index.asString+"Convolution");
		win.layout_(HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0));
		win.front;
	}

}
