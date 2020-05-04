(
s.boot;
p = ProxySpace.push(s);
)

~out.play
~out2.play

(SynthDef("Perc0_NNSynth",{
var maths1,mathsTrig, maths2, maths3, noiseMaths, oscNoise, noise, out, out2, env, envB, oscWobble;

	#maths1, mathsTrig = Maths.ar(\maths1Freq.kr(0.5, 0.1), \maths1Width.kr(0.001, 0.1), \maths1LinExp.kr(1, 0.1), 1);

	noiseMaths = Maths.ar(\noiseMathsFreq.kr(1, 0.1), \noiseMathsWidth.kr(0.001, 0.1), \noiseMathsLinExp.kr(0.5, 0.1), 0, mathsTrig)[0];

	maths2 = Maths.ar(\maths2Freq.kr(20, 0.1), 0.001, 0.99, 0, mathsTrig)[0];
	maths3 = Maths.ar(\maths2Freq.kr, 0.001, 0.5, 0, mathsTrig)[0];

	oscNoise = Lag.ar(LFNoise0.ar([\oscNoiseFreq.kr(1000, 0.1), \oscNoiseFreq.kr], \oscNoiseVol.kr(50, 0.1)), \oscNoiseLag.kr(1/500, 0.1));
	oscWobble = LFNoise2.ar(\wobbleFreq.kr(20, 0.1), \wobbleMul.kr(10,0.1));

	out2 = LFTri.ar(maths2.linlin(0,1,[\lowGliss2.kr(40, 0.1), \lowGliss2.kr*9/8],\hiGliss2.kr(4000, 0.1)), 0, 0.5)*maths3*\out2Vol.kr(1, 0.1);

	oscNoise = [oscNoise[0], SelectX.ar(\oscNoiseWidth.kr(0, 0.1), [oscNoise[0],oscNoise[1]])];
	out = [LFTri.ar(maths1.linlin(0,1,\lowGliss.kr(50, 0.1),\hiGliss.kr(2000, 0.1))+oscNoise[0]+oscWobble, 0, 0.25), LFTri.ar(maths1.linlin(0,1,\lowGliss.kr,\hiGliss.kr)+oscNoise[1]+oscWobble, 0, 0.25)]*\outVol.kr(1, 0.1);

	noise = LFDNoise1.ar([\noiseFreq.kr(5000, 0.1), \noiseFreq.kr], 0.25)*\noiseVol.kr(1, 0.1)*noiseMaths;

	noise = SelectX.ar(\noiseMix.kr(0, 0.1), [noise[0].dup, noise]);

	noise = MoogFF.ar(noise, \noiseFiltFreq.kr(10000, 0.1)*(SelectX.ar(\noiseMathsFiltMult.kr(0, 0.1), [K2A.ar(1), noiseMaths])), \noiseFiltGain.kr(1, 0.1));

	Out.ar(\outBus.kr(0), out+out2+noise)
})


a.set(\maths1Freq, 5)

a.set(\hiGliss2,1400)
a.set(\lowGliss2,100)

(~out = {
	[LFTri.ar(~maths.ar[0].linlin(0,1,200,2000)+~noise.ar, 0, 0.15), LFTri.ar(~maths.ar[0].linlin(0,1,200,2010)+~noise.ar, 0, 0.15)]
});

~out.ar

{Slew.ar(WhiteNoise.ar(0.1), 500, 500)}.play

{Slew.ar(LFNoise0.ar(10000, 0.1), 10000, 10000)}.plot

{SinOsc.ar(220, 0, 0.1)*Trig1.ar(Maths.ar(1, trig:MouseX.kr(-1,1))[1], 0.1)}.play

{LFNoise0.ar(1000, 0.1)}.play

{WhiteNoise.ar(0.1)}.play

