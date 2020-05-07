Maths {
	*ar { |riseDur=0.5, fallDur=0.5, logExp = 0.5, loop = 1, trig = 0|
		var slewUp, slewDown, riseToFall, oscOut, pulse, trigOut, trigOut2, trigOutSig, sig, frontEdge, freq, dur, backEdge;

		riseDur = riseDur.clip(0.001, 10);//K2A.ar(max(0.001, riseDur));
		fallDur = fallDur.clip(0.001, 10);//K2A.ar(max(0.001, fallDur));

		dur = riseDur+fallDur;
		riseToFall = (riseDur/(riseDur+fallDur)).clip(0.01, 0.99);
		logExp = logExp.clip(0, 1);

		freq = 1/dur;

		pulse = LFPulseReset.ar(freq, 0.999, riseToFall, trig);

		riseToFall = Select.kr(EnvGen.kr(Env([0,0,1], [0.001*dur, 0])), [riseToFall, Latch.kr(riseToFall, A2K.kr(pulse)-0.5)]);

		slewUp = freq/riseToFall;
		slewDown = freq/(1-riseToFall);

		trigOut = Trig1.ar(trig, dur*riseToFall);
		trigOutSig = Slew.ar(trigOut, slewUp, slewDown);

		oscOut = Slew.ar(SelectX.ar(pulse, [Slew.ar(pulse, 44100, slewDown), Slew.ar(pulse, slewUp, 44100)]), slewUp, slewDown);
		oscOut = SelectX.ar(freq>10, [oscOut, Slew.ar(pulse, slewUp, slewDown)]);

		sig = Select.ar(loop, [trigOutSig, oscOut]);

		trigOut = Trig1.kr(trigOut-0.01, 0.001);
		trigOut2 = Trig1.kr(pulse-0.1, 0.001);

		frontEdge = Select.kr(loop, [trigOut, trigOut+trigOut2])-0.1;

		sig = LinSelectX.ar(logExp*2, [sig.explin(0.001, 1, 0, 1), sig, sig.linexp(0, 1, 0.001, 1)]);
		^[sig, frontEdge]
	}
}

/*Maths {
	*ar { |dur=1, riseToFall=0.2, logExp = 0.5, loop = 1, trig = 0|
		var slewUp, slewDown, oscOut, pulse, trigOut, sig, frontEdge, freq;

		dur = dur.max(0.0001, dur);
		riseToFall = riseToFall.clip(0.01, 0.99);
		logExp = logExp.clip(0, 1);

		freq = 1/dur;

		pulse = LFPulseReset.ar(freq, 0, riseToFall, trig)*EnvGen.ar(Env([0,0,1], [0.01, 0.001]));

		//riseToFall = Latch.kr(riseToFall, A2K.kr(pulse)-0.5);

		slewUp = freq/riseToFall;
		slewDown = freq/(1-riseToFall);

		trigOut = Slew.ar(Trig1.ar(trig, dur*riseToFall), slewUp, slewDown);

		oscOut = Slew.ar(SelectX.ar(pulse, [Slew.ar(pulse, 44100, slewDown), Slew.ar(pulse, slewUp, 44100)]), slewUp, slewDown);
		oscOut = SelectX.ar(freq>10, [oscOut, Slew.ar(pulse, slewUp, slewDown)]);

		sig = Select.ar(loop, [trigOut, oscOut]);

		frontEdge = Trig1.ar(sig, 0.0001);

		sig = LinSelectX.ar(logExp*2, [sig.explin(0.001, 1, 0, 1), sig, sig.linexp(0, 1, 0.001, 1)]);
		^[sig, frontEdge]
	}
}*/
//
// Maths {
// 	*ar { |dur=1, riseToFall=0.2, logExp = 0.5, loop = 1, trig = 0|
// 		var slewUp, slewDown, oscOut, pulse, trigOut, sig, frontEdge, freq;
//
// 		dur = dur.max(0.0001, dur);
// 		riseToFall = riseToFall;
// 		logExp = logExp.clip(0, 1);
//
// 		freq = 1/dur;
//
// 		pulse = LFPulseReset.ar(freq, 0, [0.001, 0.5, 0.999], trig)*EnvGen.ar(Env([0,0,1], [0.01, 0.001]));
//
// 		//riseToFall = Latch.kr(riseToFall, A2K.kr(pulse)-0.5);
//
// 		slewUp = freq/(riseToFall.clip(0.001, 0.999));
// 		slewDown = freq/((1-riseToFall).clip(0.001, 0.999));
//
// 		trigOut = Slew.ar(Trig1.ar(trig, dur*riseToFall.clip(0.001, 0.999)), slewUp, slewDown);
//
// 		oscOut = Slew.ar(SelectX.ar(pulse, [Slew.ar(pulse, 44100, slewDown), Slew.ar(pulse, slewUp, 44100)]), slewUp, slewDown);
//
// 		oscOut = LinSelectX.ar(riseToFall*2, oscOut);
//
// 		pulse = LinSelectX.ar(riseToFall*2, pulse);
//
// 		oscOut = SelectX.ar(freq>10, [oscOut, Slew.ar(pulse, slewUp, slewDown)]);
//
// 		sig = Select.ar(loop, [trigOut, oscOut]);
//
// 		frontEdge = Trig1.ar(sig, 0.0001);
//
// 		sig = LinSelectX.ar(logExp*2, [sig.explin(0.001, 1, 0, 1), sig, sig.linexp(0, 1, 0.001, 1)]);
// 		^[sig, frontEdge]
// 	}
// }
