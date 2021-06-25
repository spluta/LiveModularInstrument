FM7Saw_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("FM7Saw_NNMod", {
				var ctls, mods, sig, filt, envs, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 53);

				var filtFrq = mlpVals[0].linexp(0,1,20,20000);
				var filtQ = mlpVals[1].linexp(0,1,0.1,0.999).lag(0.1);
				var filtSat = mlpVals[2].linlin(0,1,0,1).lag(0.1);
				var filtMode = mlpVals[3].linlin(0,1,0,2).lag(0.1);
				var filtMix = mlpVals[4].linlin(0,1,0,1).lag(0.1);
				var distAmp = mlpVals[5].linlin(0,1,0,1);
				var distSmooth = mlpVals[6].linlin(0,1,0,1);
				var nothin = mlpVals[7].linexp(0,1,0,1);

				var busCount = 7;
				ctls = [[
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linexp(0,1,0.001,1000),
						mlpVals[busCount=busCount+1].linexp(0,1,0.1, 15000)),
					0,
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0.001,20),
						mlpVals[busCount=busCount+1].linlin(0,1,0, 1))
				],[
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0.001,1000),
						mlpVals[busCount=busCount+1].linexp(0,1,0.001, 15000.0)),
					0,
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0.001,20),
						mlpVals[busCount=busCount+1].linlin(0,1,0, 1))
				],[
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0.001,999),
						mlpVals[busCount=busCount+1].linexp(0,1,0.001, 15000)),
					0,
					LFSaw.ar(mlpVals[busCount=busCount+1].linexp(0,1,0.001, 2000.0),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0.001,20),
						mlpVals[busCount=busCount+1].linlin(0,1,0, 1))
				]].addAll(0!3!3);

				mods = [[
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
				].addAll(0!3),
				[
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
				].addAll(0!3),
				[
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
					LFSaw.ar(mlpVals[busCount=busCount+1].linlin(0,1,0, 5),
						0,
						mlpVals[busCount=busCount+1].linlin(0,1,0,1),
						mlpVals[busCount=busCount+1].linlin(0,1,0,1)),
				].addAll(0!3)
				].addAll(0!6!3);

				sig = Splay.ar(FM7.ar(ctls, mods).slice([0,1]), 1)*0.5;

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				filt = SelectX.ar(filtMode, [
					BMoog.ar(sig, filtFrq, filtQ, 0, filtSat),
					BBandPass.ar(sig, filtFrq, filtQ)]);

				sig = SelectX.ar(filtMix, [sig, filt]);

				sig = Normalizer.ar(sig, 0.9);

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				sig = Limiter.ar(sig*envs*onOffSwitch*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1), 0.9);

				Out.ar(\outBus.kr(0), sig);

			}).writeDefFile;
		}
	}

	init {

		this.makeWindow("Noise0", Rect(0, 0, 200, 40));

		nnVals =[ [ 'filtFrq', ControlSpec(20.0, 20000.0, 'exp', 0.0, 0.0, "") ], [ 'filtQ', ControlSpec(0.01, 1.0, 'exp', 0.0, 0.0, "") ], [ 'filtSat', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'filtMode', ControlSpec(0.0, 2.0, 'linear', 0.0, 0.0, "") ], [ 'filtMix', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'distAmp', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'distSmooth', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ nil, ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frC00', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC00', ControlSpec(0.001, 1000.0, 'exp', 0.0, 0.0, "") ], [ 'addC00', ControlSpec(0.1, 15000.0, 'exp', 0.0, 0.0, "") ], [ 'frC01', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC01', ControlSpec(0.001, 20.0, 'linear', 0.0, 0.0, "") ], [ 'addC01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frC10', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC10', ControlSpec(0.001, 1000.0, 'linear', 0.0, 0.0, "") ], [ 'addC10', ControlSpec(0.001, 15000.0, 'exp', 0.0, 0.0, "") ], [ 'frC11', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC11', ControlSpec(0.001, 20.0, 'linear', 0.0, 0.0, "") ], [ 'addC11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frC20', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC20', ControlSpec(0.001, 999.0, 'linear', 0.0, 0.0, "") ], [ 'addC20', ControlSpec(0.001, 15000.0, 'exp', 0.0, 0.0, "") ], [ 'frC21', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ], [ 'mulC21', ControlSpec(0.001, 20.0, 'linear', 0.0, 0.0, "") ], [ 'addC21', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM00', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM01', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM02', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM02', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM02', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM10', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM11', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM12', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM12', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM12', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM20', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM20', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM20', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM21', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM21', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM21', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'frM22', ControlSpec(0.0, 5.0, 'linear', 0.0, 0.0, "") ], [ 'mulM22', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'addM22', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ] ]
		;


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}
}


