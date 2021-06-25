FM7_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("FM7_NNMod", {
				var ctls, mods, sig, envs, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 32);

				var freq1 = Lag.kr(mlpVals[0], 0.05).linexp(0,1,2,10000);

				ctls = [[
					Pulse.ar(mlpVals[0].linexp(0,1,0.001, 5000.0),
						mlpVals[1].linlin(0,1,0,1),
						mlpVals[2].linexp(0,1,0.001, 1000.0),
						mlpVals[3].linexp(0,1,0.001, 10000.0)),
					0,
					Pulse.ar(mlpVals[4].linexp(0,1,0.001, 2000.0),
						mlpVals[5].linlin(0,1,0,1),
						mlpVals[6].linlin(0,1,0.001, 20),
						mlpVals[7].linlin(0,1,0, 1))
				],[
					Pulse.ar(mlpVals[8].linexp(0,1,0.001, 5000.0),
						mlpVals[9].linlin(0,1,0,1),
						mlpVals[10].linlin(0,1,0, 200),
						mlpVals[1].linexp(0,1,0.001, 10000.0)),
					0,
					Pulse.ar(mlpVals[12].linexp(0,1,0.001, 2000.0),
						mlpVals[13].linlin(0,1,0,1),
						mlpVals[14].linlin(0,1,0, 20),
						mlpVals[15].linlin(0,1, 0, 1))
				]].addAll(0!3!4);


				mods = [[
					Pulse.ar(mlpVals[16].linexp(0,1,0.001, 200.0),
						mlpVals[17].linlin(0,1,0,1),
						mlpVals[18].linlin(0,1,0,1),
						mlpVals[19].linlin(0,1,0,1)),
					Pulse.ar(mlpVals[20].linlin(0,1,0, 2000.0),
						mlpVals[21].linlin(0,1,0,1),
						mlpVals[22].linlin(0,1,0, 1),
						mlpVals[23].linlin(0,1,0, 1))
				].addAll(0!4),
				[
					Pulse.ar(mlpVals[24].linexp(0,1,0.001, 2000.0),
						mlpVals[25].linlin(0,1,0,1),
						mlpVals[26].linlin(0,1,0,1),
						mlpVals[27].linlin(0,1,0,1)),
					Pulse.ar(mlpVals[28].linexp(0,1,0.001,2000.0),
						mlpVals[29].linlin(0,1,0,1),
						mlpVals[30].linlin(0,1,0, 1),
						mlpVals[31].linlin(0,1,0, 1))
				].addAll(0!4)
				].addAll(0!6!4);

				//ctls = 2.collect{|i|2.collect{|i2| Pulse.ar(*["frC","widC","mulC","addC"].collect{|name| NamedControl.kr((name++i++i2).asSymbol, 1.5.linrand+0.5, 0.1)})}.insert(1,0)}.addAll(0!3!4);

				//mods = 2.collect{|i|2.collect{|i2| Pulse.ar(*["frM","widM","mulM","addM"].collect{|name| NamedControl.kr((name++i++i2).asSymbol, 1.5.linrand+0.5, 0.1)})}.addAll(0!4)}.addAll(0!6!4);

				sig = Splay.ar(FM7.ar(ctls, mods).slice([0,1]), 1)*0.5;

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				sig = Normalizer.ar(sig, 0.9);

				sig = Limiter.ar(sig*envs*onOffSwitch*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1), 0.9);

				Out.ar(\outBus.kr(0), sig);

			}).writeDefFile;
		}
	}

	init {

		this.makeWindow("Noise0", Rect(0, 0, 200, 40));

		nnVals = [ [ 'frC00', ControlSpec(0.001, 5000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widC00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulC00', ControlSpec(0.001, 1000.0, 'exp', 0.0, 0.0, "") ],
			[ 'addC00', ControlSpec(0.001, 10000.0, 'exp', 0.0, 0.0, "") ],
			[ 'frC01', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widC01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulC01', ControlSpec(0.0, 20.0, 'linear', 0.0, 0.0, "") ],
			[ 'addC01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'frC10', ControlSpec(0.001, 5000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widC10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulC10', ControlSpec(0.0, 200.0, 'linear', 0.0, 0.0, "") ],
			[ 'addC10', ControlSpec(0.001, 10000.0, 'exp', 0.0, 0.0, "") ],
			[ 'frC11', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widC11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulC11', ControlSpec(0.0, 20.0, 'linear', 0.0, 0.0, "") ],
			[ 'addC11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],

			[ 'frM00', ControlSpec(0.001, 200.0, 'exp', 0.0, 0.0, "") ],
			[ 'widM00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulM00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'addM00', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'frM01', ControlSpec(0.0, 2000.0, 'linear', 0.0, 0.0, "") ],
			[ 'widM01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulM01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'addM01', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'frM10', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widM10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulM10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'addM10', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'frM11', ControlSpec(0.001, 2000.0, 'exp', 0.0, 0.0, "") ],
			[ 'widM11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'mulM11', ControlSpec(0.0, 200.0, 'linear', 0.0, 0.0, "") ],
			[ 'addM11', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ] ];


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}
}


