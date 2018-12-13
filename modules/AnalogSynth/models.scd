{Out.ar(0, LFNoise1.ar(MouseX.kr(1000, 20000), 0.1).poll)}.play

{Out.ar(0, WhiteNoise.ar(0.1))}.play

{
	var rate, trig, lagTime;

	rate = MouseX.kr(50000, 100);
	trig = Impulse.ar(rate);
	lagTime = 1/rate;
	Out.ar(0, Lag.ar(Latch.ar(WhiteNoise.ar(0.1), trig), lagTime));
}.play



(
SynthDef("help-LFClipNoise", { arg out=0;
    Out.ar(out,
        LFClipNoise.ar(MouseX.kr(1000, 20000), 0.25)
    )
}).play;
)

(-0.2).linexp(0.00001, 1, 0.1, 22500); // equivalent.

2**4
(
{
	var tones, freqs;

	//freqs = [205.3, 269.6, 304.4, 522.7, 800, 540];//+Array.fill(6, {PinkNoise.ar*rrand(5, 15)});
	freqs = [170, 245.7, 745.4, 1009.6, 1376.3, 2072.4];

	tones = Mix(Pulse.ar(freqs, LFNoise1.kr(0.1).range(0.4, 0.6),1/2))+LPF.ar(WhiteNoise.ar, 1000);
	tones = (tones).softclip;

	//BPF.ar(tones, 3440)+BPF.ar(tones, 7100);

}.play
)

(
{
	var tones, freqs, noiseTemp;

	//freqs = [205.3, 269.6, 304.4, 522.7, 800, 540];//+Array.fill(6, {PinkNoise.ar*rrand(5, 15)});
	freqs = [170, 252, 832, 1026, 1402, 1732];

	freqs = freqs.collect({arg item; LFNoise2.kr(0.01).range(item-5, item+5)});

	freqs.poll;

	tones = Mix(Pulse.ar(freqs,LFNoise1.kr(0.1).range(0.4, 0.6)!6,1));
	tones = LPF.ar(tones, 4000)+LPF.ar(WhiteNoise.ar(2), 6000);
	tones = (tones).softclip;


}.play
)


//tr-808 cymbal not too bad
(
{
	var tones, freqs, noiseTemp, tonesA, tonesB, cymbalTrigIn, releaseTime = 2;

	cymbalTrigIn = Impulse.ar(0.4);

	freqs = [205.3, 273.6, 304.4, 563, 847, 1730];

	freqs = freqs.collect({arg item; LFNoise2.kr(0.01).range(item-2, item+2)});

	tones = Mix(Pulse.ar(freqs,LFNoise1.kr(0.1).range(0.4, 0.6)!6,1))+LPF.ar(PinkNoise.ar(6), 5000);

	tonesA = (Resonz.ar(tones, 4200, EnvGen.kr(Env([0.2, 1, 0.2], [0.001, releaseTime]), Trig1.ar(cymbalTrigIn, 0.1)).poll, 1).softclip*EnvGen.ar(Env.perc(0.001, releaseTime, 4), cymbalTrigIn)).softclip;
	tonesB = (Resonz.ar(tones*EnvGen.ar(Env.perc(0.001, 0.2, 4), cymbalTrigIn), 7100, EnvGen.kr(Env([0.5, 2, 0.5], [0.0001, releaseTime]), Trig1.ar(cymbalTrigIn, 0.1)), 3).softclip).softclip;

	tones = HPF.ar(tonesA, 2440)+HPF.ar(tonesB, 10000, 3);


	// tones = (HPF.ar(tonesA, 2440)+HPF.ar(tonesB, 10000, 3)/*+DFM1.ar(tonesB, 10000, 0.1)*/).softclip;

}.play
)


s.prepareForRecord
s.record
s.stop;

(
    w = Window("NumberBox Example", Rect(100, 500, 400, 120));
    b =     NumberBox(w, Rect(150, 10, 100, 20));
c = DragSource(w, Rect(150, 50, 100, 20)).object_(24);

    b.value = rrand(1,15);
    b.action = {arg numb; numb.value.postln; };
    w.front
)