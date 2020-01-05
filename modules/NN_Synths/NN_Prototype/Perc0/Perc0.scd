(SynthDef("Perc0_NNSynth", {//|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0, inButt0=0, inButt1=0, inButt2=0, inButt3=0|
	var mathsA, mathsB, modFreq, modSynth, carrier, cFreq, cFeedback, sig;
	var env, mathsEnv, temp, temp0, trig, freqMod2, noise, noise2, noiseEnv, gliss, lag, envRise, envFall, trigEnv, envOnOff, onOff;

	onOff = Lag.kr(In.kr(\onOffBus.kr.poll), 0.01);

	envOnOff = In.kr(\envOnOffBus.kr);

	#temp, temp0, mathsA = Maths.ar(\mathsAFreq.kr(2).clip(0.1, 20), \mathsAWidth.kr(0.1), \mathsAShape.kr(0.5), envOnOff);
	#mathsEnv, trig, mathsB = Maths.ar(\mathsBFreq.kr(1), \mathsBWidth.kr(0.1), \mathsBShape.kr(0.5), onOff, \mathsBTrig.kr(0));
	//mathsB.poll;

	modFreq = 0.1+mathsB.linexp(0,1,3, 50);
	modSynth = [LFTri.ar(modFreq, 0), LFPulse.ar(modFreq, 0), LFSaw.ar(modFreq, 0)];


	//freqMod2 = (Lag.ar(modSynth[2], 0.02)*\mod2Mult.kr(0.1)+\mod2Mult.kr)*(1000*(1-(mathsA*0.5)));

	cFreq = ((\carFreq.kr(200, 0.1)+(mathsB*\mathsRange.kr(1000)))).clip(-20000, 20000);
	cFeedback = 0.1+modSynth[0].linlin(-1,1,0,0.7);
	carrier = Mix(SinOscFB.ar([cFreq+2,cFreq-2], cFeedback));

	noise = LFNoise0.ar(\noiseFreq.kr(10000));
	//noise = WhiteNoise.ar(0.5);

	noise = RLPF.ar(noise, Latch.kr(\noiseCutoff.kr(1000), trig), \rq.kr(2));
		//LagUD.kr(Trig1.kr(trig, 0.05), 0.01, 0.2).linexp(0,1,\noiseBottom.kr(20),\noiseTop.kr(15000)), \rq.kr(1));
	noiseEnv = LagUD.kr(trig, 0, \noiseDur.kr(0.5));
	noise = noise*noiseEnv;
	noise2 = WhiteNoise.ar(\noise2Vol.kr(0.1))*EnvGen.ar(Env.perc(0.01, 0.5), trig, 1, 0, \timeScale.kr(1));

	lag = LagUD.kr(Trig1.kr(trig, 0.01), 0, 0.1);
	gliss = SinOsc.ar(lag.linexp(0,1, 40, \glissFreq.kr(500)))*lag;

	env = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	sig = Pan2.ar(LPF.ar(LowShelf.ar(SoftClipAmp8.ar(carrier, 2), 800, 1, 3), 16000), 0, 0.9);

	sig = (sig*\sigVol.kr(0.5))+(noise*\noiseVol.kr(1))+(gliss*\glissVol.kr(1));

	envRise = In.kr(\envRiseBus.kr).clip(0.001, 2);
	envFall = In.kr(\envFallBus.kr).clip(0.001, 2);
	trigEnv = LagUD.ar(LFPulse.ar(1/(envRise+envFall), 0, envRise/(envRise+envFall)), envRise, envFall);


	trigEnv = SelectX.ar(envOnOff, [K2A.ar(1), trigEnv]);


	Out.ar(\outBus.kr(0), sig*env*mathsEnv*trigEnv*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOff);
}).load(ModularServers.servers[\lmi1].server)
)

synthVals = [['/Fader/', \carFreq, ControlSpec(20, 5000, \exp)],
	['/Fader2/', \mathsBFreq, ControlSpec(0.1, 20, \exp)],
	['/Fader3/', \mathsRange, ControlSpec(10, 10000, \exp)],
	['/Fader4/', \noiseFreq, ControlSpec(100, 15000)],
	['/Fader5/', \noiseCutoff, ControlSpec(0, 3000)],
	['/Fader6/', \noiseDur, ControlSpec(0, 2)],
	['/Fader7/', \noiseVol, ControlSpec(0, 1)],
	['/Fader8/', \rq, ControlSpec(0.1, 2)],
	['/Fader9/', \fold, ControlSpec(0.1, 1)],
	['/Fader10/', \dustRate, ControlSpec(1000, 1)],
	['/Fader11/', \attack, ControlSpec(0.001, 0.01, \exp)],
	['/Fader12/', \release, ControlSpec(0.001, 0.01, \exp)],
	['/Fader13/', \outFilterFreq, ControlSpec(20, 20000, \exp)],
	['/Fader14/', \outFilterRQ, ControlSpec(0.1, 2, \exp)],
	['/Fader15/', \filtModFreq, ControlSpec(0, 30, \lin)],
	['/Fader16/', \filtModAmp, ControlSpec(0, 1, \amp)],

	['/Fader17/', \vol, ControlSpec(0, 0.2, \amp)],
	['/Fader18/', \envRise, ControlSpec(0.01, 0.4, \exp)],
	['/Fader19/', \envFall, ControlSpec(0.01, 0.4, \exp)]

];