(
SynthDef("Done-help", { arg out, t_trig;
    var line, a, b;

	    line= EnvGen.kr(Env.asr, Impulse.kr(0.3));

    a= SinOsc.ar(440,0,0.1*line); //sound fading out
    b= WhiteNoise.ar(Trig1.kr(Done.kr(line), 0.01)*0.1); //noise starts at end of line

    Out.ar(out, Pan2.ar(a+b));
}).play;
)


(
w = Window.new.front;
b = ControlSpec(0, 3, \linear, 1); // min, max, mapping, step
c = StaticText(w, Rect(20, 20, 150, 20)).align_(\center).background_(Color.rand);
a = Slider(w, Rect(20, 50, 150, 20))
    .focusColor_(Color.red(alpha:0.2))
    .background_(Color.rand)
    .value_(0.5)
    .action_({
        switch(b.map(a.value),
		0, {"0".postln},
		1, {"1".postln},
		2, {"2".postln},
		3, {"3".postln})
        // round the float so it will fit in the NumberBox
        });
a.action.value;

)

pv_collect

a = Bus.audio(s, 5);
a.index


DFM1 - Low and High

{DFM1.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 1500), MouseY(0.1, 1), MouseX).dup}.play

MoogVCF - Low

Resonz - Band

RLPF.ar - Low

RHPF - High

BRF - reject


(
{
	var freq, in, out, res, clipType = 1;

	in = PinkNoise.ar(0.5);//Mix(LFSaw.ar([120, 180], 0, 0.33));
	freq = LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 20, 20000);
	res = MouseY.kr;

	out = SelectX.ar(MouseX.kr(0,6).poll, [MoogFF.ar(in, freq, res.linlin(0,1,0,4), mul:3), //LPF0
		DFM1.ar(in, freq, res.linlin(0,1,0.1,2)), //LPF1
		Resonz.ar(in, freq, res.linexp(0,1,2,0.001).clip(0.001, (22000-(freq/2))/freq), res.linexp(0,1,2,12)), //BP
		DFM1.ar(in, freq, res.linlin(0,1,0.1,2), type:1), //LPF1
		RHPF.ar(in, freq, res.linlin(0,1,2,0.1).clip(0.1, (22000-(freq/2))/freq), 1),
		BRF.ar(in, freq, res.linlin(0,1,0,1,3))
	]);

	out = Select.ar(clipType, [out.clip(-1, 1), out.softclip]);

	out = ReplaceBadValues.ar(out);

}.scope;
)

{MoogFF.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 1500).poll, 3).dup}.play
MoogVCF.ar(
{ |ffreq=390| MoogVCF.ar(Mix(LFSaw.ar([120, 180], 0, 0.33)), LinExp.kr(LFCub.kr(0.1, 0.5*pi), -1, 1, 280, 1500).poll, 0.75, 2.0).dup }.play

	10*(2**10)

Quarks.gui

	1600.explin(1, 22050, 0, 1)
	0.9.linexp(0,1,21.533203125,22050)