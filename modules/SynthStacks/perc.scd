SynthDef("Gliss0_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0.5, inButt0=0, inButt1=0, inButt2=0, inButt3=0|
				var mathsA, mathsB, modFreq, modSynth, carrier, cFreq, cFeedback, sig;
				var env, mathsEnv, temp;

				#temp, mathsA = Maths.ar(in1.linexp(0, 1, 0.1, 20), 0.5, 0.6, 1);
				#mathsEnv, mathsB = Maths.ar(in2.linexp(0,1,0.1, 20), in4, 0.5, inButt0, inButt1);

				modFreq = 0.1+mathsB.linexp(0,1,3, 50);
				modSynth = [LFTri.ar(modFreq, 0), LFPulse.ar(modFreq, 0), LFSaw.ar(modFreq, 0)];

				cFreq = ((in0.linexp(0,1,0.01,1)+(mathsB*in3)).linexp(0,1,15,5000, \min)+((Lag.ar(modSynth[2], 0.02)*in5+in5)*(3000*(1-(mathsA*0.5))))).clip(-20000, 20000);
				cFeedback = 0.1+modSynth[0].linlin(-1,1,0,0.7);
				carrier = Mix(SinOscFB.ar([cFreq+2,cFreq-2], cFeedback));

				env = Envs.kr(muteGate, pauseGate, gate);

				sig = Pan2.ar(LPF.ar(LowShelf.ar(SoftClipAmp8.ar(carrier, 2), 400, 1, 3), 16000), 0, 0.9*in7);

				Out.ar(outBus, sig*env*mathsEnv);
}).;

(
a = SynthDef("Perc_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0.5, inButt0=0, inButt1=0, inButt2=0, inButt3=0|

	var perc, mathsEnv, mathsTrig, maths, osc, cFreq, trig, impulseFreq, impulse, filtEnv, env;

	impulseFreq = in2.linlin(0,1,0.2, 30);
	impulse = Select.kr(inButt0, [inButt1, Impulse.kr(impulseFreq)]);
	//impulse = Impulse.kr(impulseFreq);

	#mathsEnv, mathsTrig, maths = Maths.ar(in0.linexp(0,1,0.1, 20), 0.05, 0.7, 0, impulse).poll;

	filtEnv = EnvGen.ar(Env.perc, mathsTrig, 1, 0, 1/impulseFreq).poll;

	perc = Splay.ar(WhiteNoise.ar([1,1])*EnvGen.ar(Env.perc(0.01, 0.5), mathsTrig, 1, 0, in1.linlin(0,1,0.01,0.5)), 0.5);
	//perc = RLPF.ar(perc, (filtEnv*10000+TRand.kr(4000,6000,impulse)), 0.2).distort.distort;

	cFreq = maths.linexp(0,1,15,/*MouseX.kr(15, 5000)*/in3.linlin(0,1,400, 10000));

	//osc = Splay.ar(SinOscFB.ar([cFreq+2,cFreq-2], maths.linlin(0,1,0.5,0.7)).distort, 0.5)*mathsEnv;

	osc = Splay.ar(LFTri.ar([cFreq+2,cFreq-2]).distort.distort, 0.5)*mathsEnv;

	env = Envs.kr(muteGate, pauseGate, gate);

	Out.ar(0, (perc+osc)*env);
}).play(s, [\in0, 0.7, \in1, 0.4])
)

a.set(\gate, 0)

a.set(\in0, 0.7, \in1, 0.1, \in2, 0.3, \in3, 0.8, \inButt0, 1)

a.set(\inButt1, 1)

OSCFunc.trace
