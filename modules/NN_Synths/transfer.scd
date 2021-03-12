([[\freq1, ControlSpec(1, 10000, \exp)],
			[\freq2, ControlSpec(5, 10000, \exp)],
			[\modVol1, ControlSpec(0, 3000)],
			[\modVol2, ControlSpec(0, 3000)],
			[\noiseVol, ControlSpec(0, 3000)],
			[\impulse, ControlSpec(100, 20000, \exp)],
			[\filterFreq, ControlSpec(100, 20000, \exp)],
			[\rq, ControlSpec(0.1, 2)],
			[\fold, ControlSpec(0.1, 1)],
			[\dustRate, ControlSpec(1000, 1)],
			[\attack, ControlSpec(0.001, 0.01, \exp)],
			[\release, ControlSpec(0.001, 0.01, \exp)],
			[\outFilterFreq, ControlSpec(20, 20000, \exp)],
			[\outFilterRQ, ControlSpec(0.1, 2, \exp)],
			[\filtModFreq, ControlSpec(0, 30, \lin)],
	[\filtModAmp, ControlSpec(0, 1, \amp)]].do{|item, i|

	//item[1].postln;
	if(item[1].warp.isKindOf(ExponentialWarp))
	{
		("var "++item[0].asString++" = mlpVals["++i++"].linexp(0,1,"++item[1].clipLo++","++item[1].clipHi++");").postln

	}{
		("var "++item[0].asString++" = mlpVals["++i++"].linlin(0,1,"++item[1].clipLo++","++item[1].clipHi++");").postln
	}
})