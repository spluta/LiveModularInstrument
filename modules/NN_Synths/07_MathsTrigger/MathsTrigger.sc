MathsTrigger_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("MathsTrigger_NNMod",{
				var noise1, dust, trigEnv, filtMod, filterFreq, envs, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 15);
				var inc = 0;
				var freq1 = mlpVals[inc].linexp(0,1,1,10000).lag(0.05);
				var mod1 = mlpVals[inc=inc+1].linlin(0,1,1,4000).lag(0.05);
				var freq2 = mlpVals[inc=inc+1].linexp(0,1,1,10000).lag(0.1);
				var mod2 = mlpVals[inc=inc+1].linlin(0,1,1,3000).lag(0.1);
				var lfMult = mlpVals[inc=inc+1].linlin(0,1,0,1).lag(0.1);
				var riseDur = mlpVals[inc=inc+1].linlin(0,1,0.001,0.3).lag(0.1);
				var fallDur = mlpVals[inc=inc+1].linlin(0,1,0.001, 0.3).lag(0.1);
				var logExp = mlpVals[inc=inc+1].linlin(0,1,0,1).lag(0.1);
				var trigRate = mlpVals[inc=inc+1].linlin(0,1,0,200).lag(0.05);
				var impulseRate = mlpVals[inc=inc+1].linlin(0,1,0,200).lag(0.1);
				var lowMathsFreq = mlpVals[inc=inc+1].linexp(0,1,200,12000).lag(0.1);
				var hiMathsFreq = mlpVals[inc=inc+1].linexp(0,1,20,2000).lag(0.1);
				var resFreq = mlpVals[inc=inc+1].linexp(0,1,200,14000).lag(0.1);
				var bwr = mlpVals[inc=inc+1].linexp(0,1,0.02,1).lag(0.1);
				var foldNum = mlpVals[inc=inc+1].linlin(0,1,0,1);

				var trig = Impulse.ar(impulseRate)+Dust.ar(trigRate);

				var maths = Maths.ar(riseDur, fallDur, logExp, 0, 1, trig);
				var out = CrossSquareSine.ar(freq1, mod1, freq2+maths[0].linlin(0,1,lowMathsFreq,hiMathsFreq), mod2, lfMult)*maths[0];

				out = Resonz.ar(out, [resFreq-1,resFreq+1], bwr);
				out = out.fold(foldNum.neg, foldNum)*3;

				//in every synth
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, out*envs);
}).writeDefFile;
		}
	}

	init {

		this.makeWindow("MathsTrigger", Rect(0, 0, 200, 40));

		nnVals = [[\freq1],
			[\mod1],
			[\freq2],
			[\mod2],
			[\lfMult],
			[\riseDur],
			[\fallDur],
			[\logExp],
			[\trigRate],
			[\impulseRate],
			[\lowMathsFreq],
			[\hiMathsFreq],
			[\resFreq],
			[\bwr],
			[\foldNum]
		];


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}