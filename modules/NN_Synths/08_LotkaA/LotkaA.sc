LotkaA_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("LotkaA_NNMod", {
				var out,onOffSwitch, envs;

				var mlpVals = In.kr(\dataInBus.kr, 36);

				var iter = 0;

				var lim = Lag.kr(mlpVals[iter], 0.05).linlin(0,1,1,1024);
				var a = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0, 1, 0, 10);
				var b = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0, 1, 0, 10);
				var c = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0, 1, 0, 10);
				var g = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0, 1, 0, 10);
				var dt = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0, 1, 0.00001, 1);

				var freqX = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 100, 22000);
				var resX = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var freqY = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 100, 22000);
				var resY = Lag.kr(mlpVals[iter = iter+1], 0.05);

				var sampleRate = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 50, SampleRate.ir-100);

				var limRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var limFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var limLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var limMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 500);

				var aRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var aFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var aLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var aMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 10);

				var bRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var bFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var bLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var bMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 10);

				var cRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var cFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var cLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var cMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 10);

				var gRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var gFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var gLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var gMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 10);

				var dtRise = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var dtFall = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1, 0.001, 0.5);
				var dtLogExp = Lag.kr(mlpVals[iter = iter+1], 0.05);
				var dtMul = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1, 0, 1);

				var envTrig = EnvGen.ar(Env([1,1,0], [0.1,0]), Decay.kr(Trig1.kr((\onOff0.kr+\onOff2.kr).clip(0,1), 0.01), 0));

				out = (LotkaVolterraA.ar(
					(lim+(Maths.ar(limRise, limFall, limLogExp)[0]*limMul)-(limMul/2)).clip(1,1024),
					1,
					(a+(Maths.ar(aRise, aFall, aLogExp)[0]*aMul)-(aMul/2)).clip(0,10),
					(b+(Maths.ar(bRise, bFall, bLogExp)[0]*bMul)-(bMul/2)).clip(0,10),
					(c+(Maths.ar(cRise, cFall, cLogExp)[0]*cMul)-(cMul/2)).clip(0,10),
					(g+(Maths.ar(gRise, gFall, gLogExp)[0]*gMul)-(gMul/2)).clip(0,10),

					(dt+(Maths.ar(dtRise, dtFall, dtLogExp)[0]*dtMul)-(dtMul/2)).clip(0.00001,1),
					envTrig, freqX, resX, freqY, resY, sampleRate
				).softclip*2).clip(-1,1);

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

		this.makeWindow("LotkaA", Rect(0, 0, 200, 40));

		nnVals = ["lim","a","b","c","g","dt",
			"freqX", "resX", "freqY", "resY",
			"sampleRate",
			"limRise", "limFall", "limLogExp", "limMul",
			"aRise", "aFall", "aLogExp", "aMul",
			"bRise", "bFall", "bLogExp", "bMul",
			"cRise", "cFall", "cLogExp", "cMul",
			"gRise", "gFall", "gLogExp", "gMul",
			"dtRise", "dtFall", "dtLogExp", "dtMul"].collect({|item| [item.asSymbol]});


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}