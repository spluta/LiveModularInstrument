perc should be more aggressive

laser beam should be longer

(
a = {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.1, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0.5, inButt0=0, inButt1=0, inButt2=0, inButt3=0|
	var sound, lfo, lfoMod, env;

	sound = LFSaw.ar(in0.linlin(0,1,20,100)+LFNoise2.kr([0.2,0.18], in1.linlin(0,1,0,3)));

	sound = MoogFF.ar(sound, in2.linlin(0,1,20,2000), in3.linlin(0,1,0,3.8), 0, 5).softclip;

	lfoMod = in4.linlin(0,1,0,200).clip(0,0.5);
	lfo = SinOsc.ar(MouseX.kr.linexp(0,1,0.1,10), 0, lfoMod, 1-lfoMod).poll;

	env = Envs.kr(muteGate, pauseGate, gate);

	sound*lfo
}.play(s)
)

a.set(\in0, 0.2)

{MouseX.kr.linlin(0,1,0,200).clip(0,1).poll; nil}.play