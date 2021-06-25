{
	SuperSaw.ar(200, 1, MouseX.kr, MouseY.kr, 0.1)
}.play

{
	SuperSaw.ar(300, 1, 0.75, 1, 0.1)
}.plot

Ndef(\a).play
(
a = SynthDef("SuperMathsSaw", {
	var mlpVals = In.kr(\dataInBus.kr, 21);

	var iter = 0;

	var freq = Lag.kr(mlpVals[iter], 0.05).linexp(0,1,20,10000);
	var mix = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var detune = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var overdrive = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1,2,20);

	var rise1 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
	var fall1 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
	var logExp1 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var maths_mul1 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,10000);
	//9
	var rise2 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.01,2);
	var fall2 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.01,2);
	var logExp2 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var control_offset = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0.0,1.0);
	var control_scale = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var vca = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var resonance = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1,0,1.8);
	//16
	var rise3 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
	var fall3 = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
	var logExp3 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
	var low_filt_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,20,20000);
	var hi_filt_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,20,20000);
	var moog_res = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,0.995);

	var maths1 = Maths.ar(rise1, fall1, logExp1)[0]*maths_mul1;
	var maths2 = Maths.ar(rise2, fall2, logExp2);
	var maths3 = Maths.ar(rise3, fall3, logExp3).linexp(0,1,low_filt_freq,hi_filt_freq);

	var out = SuperSaw.ar((freq+maths1), mix, detune);
	out = (out*overdrive).softclip;

	out = LPG.ar(out, A2K.kr(maths2[0]), control_offset, control_scale, vca, resonance);
	out = MoogVCF2.ar(out, maths3[0], moog_res);

	out = HPF.ar(HPF.ar(out,40), 40);

	Out.ar(\outBus.kr(0), out*\vol.kr(0.5));
}).play(s, [\dataInBus, b])
)

a.free

b = Bus.control(s, 21);
b.set(*[0.52999997138977, 0.5, 0.91999999880791, 0.070000000298023, 0.029999999329448, 0.56999999284744, 0.55000001192093, 0.85000002384186, 0.03999999910593, 0.23999999463558, 0.94999998807907, 0.03999999910593, 0.87000000476837, 1.0, 0.9200000166893, 0.86000001430511, 0.83999997377396, 0.23000000417233, 0.58999997377396, 0.81000000238419, 0.62000000476837])
b.get
