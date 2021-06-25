CrossFeedback1_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("CrossFeedback1_NNMod",{
				var noise1, out, foldNum, dust, trigEnv, filtMod, filterFreq, envs, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 12);
				var inc = 0;
				var freq1 = mlpVals[inc].linexp(0,1,1,10000).lag(0.05);
				var freq2 = mlpVals[inc=inc+1].linexp(0,1,5,10000).lag(0.05);
				var modVol1 = mlpVals[inc=inc+1].linlin(0,1,0,3000).lag(0.1);
				var modVol2 = mlpVals[inc=inc+1].linlin(0,1,0,3000).lag(0.1);
				var fold = mlpVals[inc=inc+1].linlin(0,1,0.1,1.0).lag(0.1);
				var dustRate = mlpVals[inc=inc+1].linlin(0,1,1,1000).lag(0.1);
				var attack = mlpVals[inc=inc+1].linexp(0,1,0.001,0.01).lag(0.1);
				var release = mlpVals[inc=inc+1].linexp(0,1,0.001,0.01).lag(0.1);
				var outFilterFreq = mlpVals[inc=inc+1].linexp(0,1,20,20000).lag(0.05);
				var outFilterRQ = mlpVals[inc=inc+1].linexp(0,1,0.05,1).lag(0.1);
				var filtModFreq = mlpVals[inc=inc+1].linlin(0,1,0,30).lag(0.1);
				var filtModAmp = mlpVals[inc=inc+1].linlin(0,1,0,1).lag(0.1);


				out = CrossSineTri.ar(freq1, modVol1, freq2, modVol2);

				out = [out.fold2(fold), out.fold2(fold*0.99)]/fold;

				dust = LagUD.ar(Trig1.ar(Dust.ar(dustRate), attack+release), attack, release);

				out = SelectX.ar((dustRate<800), [out, out*dust]);

				outFilterFreq = (LFTri.ar(filtModFreq)*filtModAmp).linexp(-1.0, 1.0, (outFilterFreq/2).clip(20, 20000), (outFilterFreq*2).clip(20, 20000)).clip(20, 20000);

				out = RLPF.ar(out, outFilterFreq, outFilterRQ);


				//in every synth
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);
				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);
				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, out*envs);
			}).writeDefFile;
		}
	}

	setAllVals {

		allValsList = List.fill(8, {List[0,0,0,0,0,1,1,1,1,0,0,0,1,1,0,0,0,0,0,0]});
	}

	init {

		this.makeWindow("CrossFeedback1", Rect(0, 0, 200, 40));

			nnVals = [[\freq1],
			[\freq2],/*
			[\freq3],*/
			[\modVol1],
			[\modVol2],/*
			[\modVol3],*/
			[\fold],
			[\dustRate],
			[\attack],
			[\release],
			[\outFilterFreq],
			[\outFilterRQ],
			[\filtModFreq],
			[\filtModAmp]
		];

		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}