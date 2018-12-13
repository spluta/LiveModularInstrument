(
// Dust randomly triggers Decay to create an exponential
// decay envelope for the WhiteNoise input source
{
z = Decay.ar(Dust.ar(5,0.5), 0.3, WhiteNoise.ar);
	DelayC.ar(z, 2, MouseX.kr(0.001, 2), 1, z); // input is mixed with delay via the add input
}.play
)

// allocate buffer
b = Buffer.alloc(s,44100,1);

(
// Dust randomly triggers Decay to create an exponential
// decay envelope for the WhiteNoise input source
{
z = Decay.ar(Dust.ar(5,0.5), 0.3, WhiteNoise.ar);
BufDelayC.ar(b.bufnum, z,MouseX.kr(0.001, 1), 1, z); // input is mixed with delay via the add input
}.play
)

b = Buffer.alloc(s, s.sampleRate * 1, 1);
c = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");

// write a signal into a delay, tap it at mutiple times
SynthDef(\test, {arg buffer, playBuf;
    var src, tapPhase, tap1, tap2, tap3;
	    src = PlayBuf.ar(1, playBuf, loop:1)+LocalIn.ar(1);
    tapPhase = DelTapWr.ar(buffer, src);
    #tap1, tap2 = DelTapRd.ar(buffer, tapPhase,
		[MouseX.kr(0, 0.4)+SinOsc.kr(0.05, 0, 0.01, 0.01),  MouseX.kr(0.01, 0.39)+SinOsc.kr(0.052, 0.4, 0.01, 0.01)],     // tap times
        1,                      // no interp
        1         // muls for each tap
        );
	LocalOut.ar(tap1*0.95); //feedback reduces to 0.7 or so on z-up
	    Out.ar(0, [tap1, tap2])
    }).send(s);

x = Synth(\test, [\buffer, b, \playBuf, c]);
x.free;
b.free;




SynthDef(\vDelay_mod, {arg inBus, outBus, delTime, buffer0, buffer1, buffer2, buffer3, buffer4, buffer5, buffer6, buffer7, onOff=0, vol=0, gate = 1, pauseGate = 1;
				var in, tapPhase, taps, env, pauseEnv;

				in = (In.ar(inBus,1)*Lag.kr(onOff))+(LocalIn.ar(1));

				tapPhase = DelTapWr.ar(buffer0, in);

				delTime = Lag.kr(delTime, 0.01);

				//[buffer0, buffer1, buffer2, buffer3, buffer4, buffer5, buffer6, buffer7]

				taps = DelTapRd.ar(buffer0, tapPhase,

						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
/*						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01),
						delTime,//+Rand(0,0.02)+SinOsc.kr(Rand(0.045,0.055), Rand(0.0,1.0), 0.01, 0.01)*/


					1,                      // no interp
					1         // muls for each tap
				);
				//LocalOut.ar(taps*SelectX.kr(LagUD.kr(onOff, 0.01, 0.5), [0,0.95]).poll); //feedback reduces to 0.7 or so on z-up

				LocalOut.ar(taps*0.95); //feedback reduces to 0.7 or so on z-up

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, taps*env*vol*pauseEnv)
			}).writeDefFile;