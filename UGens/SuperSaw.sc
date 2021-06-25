SuperSaw {

	*ar { | freq = 523.3572, mix=0.75, detune = 0.75, spread = 0, mul=1, add=0|
		var center = LFSaw.ar(freq);
		var side_freqs = [
/*			(freq - (freq*detune.lincurve(0,1,0,2,4)*0.11002313)),
			(freq - (freq*detune.lincurve(0,1,0,2,4)*0.06288439)),*/
			(freq - (freq*detune.lincurve(0,1,0,2,4)*0.01952356)),
			(freq + (freq*detune.lincurve(0,1,0,2,4)*0.01991221))
/*			(freq + (freq*detune.lincurve(0,1,0,2,4)*0.06216538)),
			(freq + (freq*detune.lincurve(0,1,0,2,4)*0.10745242))*/
		];
		var outer = side_freqs.collect{ |n|
			LFSaw.ar(n, Rand(0, 2), 1/7)
		}*mix.clip(0,1);

		var sig = Pan2.ar(center)*((1-mix).clip(0.0,1.0))+Splay.ar(outer, spread);

		sig = HPF.ar(sig, freq);
		^(sig*mul)
	}
}
