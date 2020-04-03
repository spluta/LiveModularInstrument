(SynthDef("noise0_NNSynth", {
	var impulseRate, impulseAdd, envs, onOff, sound, filterMod, dust, filterFreqMod, comb;

	//impulseRate = MouseX.kr(200, 20000);
	//impulseAdd = MouseY.kr(0, 20000);

	sound = Lag.ar(Latch.ar(WhiteNoise.ar(1), Impulse.ar(\impulseRate.kr(200))), 1/(\impulseRate.kr+\impulseAdd.kr(0)));
	filterMod = LinExp.kr(LFCub.kr(0.1, 0.5*pi)-1, 1, 180, 8500);



		//SelectX.ar(\ffm_Select.kr, [SinOsc.ar(\ffm_Freq.kr(0.1)), LFNoise2.ar(\ffm_Freq.kr)

	filterFreqMod = //Maths.ar(\ffm_Freq.kr(1), \ffm_Width.kr(0.5), 0.3, 1)[2].linlin(0,1,-1,1)
	SelectX.ar(\ffm_Which.kr(0), [SinOsc.ar(\ffm_Freq.kr(1)), LFNoise2.ar(\ffm_Freq.kr*5)])
	*(\ffm_Amp.kr(0)*\filterFreq.kr(10000, 0.05));

	sound = MoogFF.ar(sound, (\filterFreq.kr+filterFreqMod).clip(20, 20000), \moogGain.kr(1.5));

	envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	dust = Select.ar(\dustDensity.kr(3000)>2000, [LagUD.ar(Trig1.ar(Dust.ar(\dustDensity.kr), 0.001), 0.0005, 0.0005), K2A.ar(1)]);

	sound = sound*dust;

	comb = CombC.ar(sound, 2, TRand.kr(\combDelayLo.kr(1, 0.1), \combDelayHi.kr(1, 0.1), Impulse.kr(\combRandRate.kr(0))).lag(1/\combRandRate.kr), \combDecay.kr(1, 0.1));

	sound = sound+(comb*\combVol.kr(0, 0.1));

	sound = SoftClipAmp8.ar(sound, \softClipGain.kr(1));

	onOff = Lag.kr(In.kr(\onOffBus.kr), 0.01);
	Out.ar(\outBus.kr(0), sound.dup*envs*onOff*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1));

}).load(ModularServers.servers[\lmi1].server)
)

a = {SoftClipAmp8.ar(SinOsc.ar(200), MouseX.kr(0, 20))*0.1}.scope

a = {Clipper8.ar(SinOsc.ar(200), MouseX.kr(0, 1).neg, MouseX.kr(0, 1))*0.1}.scope
a.free


(
{
    var a;
    a = Line.ar(-4, 4, 0.01);
    a.distort
}.plot
)


{ MoogLadder.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 180, 8500), 0.75).dup }.play

{ MoogFF.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 180, 8500), 1.5).dup }.play