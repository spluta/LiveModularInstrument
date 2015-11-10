SynthDef("greatExpectations_mod", { arg outBus, inBus, vol, delayBus, distBus, gate = 1, pauseGate = 1;
				var in, in2, verbSig, env, pauseEnv, out;
				var shifties;

				env = EnvGen.kr(Env.asr(0,1,0.5), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = In.ar(inBus,1)*2;

				in2 = PitchShift.ar(in, 0.2, [
					LFNoise2.kr(0.1).range(0.249, 0.251),
					LFNoise2.kr(0.1).range(0.499, 0.501),
					LFNoise2.kr(0.1).range(0.749, 0.751),
					LFNoise2.kr(0.1).range(0.749, 0.751),
					LFNoise2.kr(0.1).range(0.499, 0.501),
					LFNoise2.kr(0.1).range(0.249, 0.251)],
					0, 0.3);

				//in2 = CombC.ar(shifties, 0.1, [ 0.052898466587067, 0.040793769359589, 0.053465979099274, 0.049592454433441, 0.05687507390976, 0.057903006076813 ], 5);

				verbSig = GVerb.ar(Mix.new(in2), 100, 10, 0.5, 1, 15, 0, 0.1, 0.1, 100);

				verbSig = LPF.ar(verbSig, 400);

				out = Splay.ar(in2)+verbSig;

				Out.ar(outBus, out*vol*env*pauseEnv);

}).load(s);

SynthDef(\help_PlayBuf, {| out = 0, bufnum = 0 |
    Out.ar(out,
        PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), doneAction:2)
    )
}).play(s, [\out, 0, \bufnum, b]);