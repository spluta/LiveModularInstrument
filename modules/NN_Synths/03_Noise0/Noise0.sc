Noise0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("Noise0_NNMod",{
				var envs, onOff, sound, filterMod, dust, filterFreqMod, comb, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 14);

				var impulseRate = mlpVals[0].linexp(0, 1, 19, 20000);
				var impulseAdd = mlpVals[1].linlin(0, 1, 0, 20000);
				var filterFreq = mlpVals[2].linexp(0, 1, 20, 20001);
				var moogGain = mlpVals[3].lincurve(0, 1, 1, 4, 1);
				var ffm_Freq = mlpVals[4].linexp(0, 1, 0.1, 30).lag(0.05);
				var ffm_Amp = mlpVals[5].linlin(0, 1, 0, 1);
				var ffm_Which = mlpVals[6].linlin(0, 1, 0, 1);
				var dustDensity = mlpVals[7].linexp(0, 1, 1, 3000);
				var combDelayLo = mlpVals[8].linexp(0, 1, 0.001, 2).lag(0.1);
				var combDelayHi = mlpVals[9].linexp(0, 1, 0.001, 2).lag(0.1);
				var combRandRate = mlpVals[10].linlin(0, 1, 1, 5);
				var combDecay = mlpVals[11].linexp(0, 1, 0.001, 5).lag(0.1);
				var combVol = mlpVals[12].linlin(0, 1, 0, 1);
				var softClipGain = mlpVals[13].linlin(0, 1, 1, 20);

				sound = Lag.ar(Latch.ar(WhiteNoise.ar(1), Impulse.ar(impulseRate)), 1/(impulseRate+impulseAdd));
				filterMod = LinExp.kr(LFCub.kr(0.1, 0.5*pi)-1, 1, 180, 8500);


				filterFreqMod = SelectX.ar(ffm_Which, [SinOsc.ar(ffm_Freq), LFNoise2.ar(ffm_Freq*5)])
				*(ffm_Amp*filterFreq);

				sound = MoogFF.ar(sound, (filterFreq+filterFreqMod).clip(20, 20000), moogGain);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				dust = Select.ar(dustDensity>2000, [LagUD.ar(Trig1.ar(Dust.ar(dustDensity), 0.001), 0.0005, 0.0005), K2A.ar(1)]);

				sound = sound*dust;

				comb = CombC.ar(sound, 2, TRand.kr(combDelayLo, combDelayHi, Impulse.kr(combRandRate)).lag(1/combRandRate), combDecay);

				sound = sound+(comb*\combVol.kr(0, 0.1));

				sound = SoftClipAmp8.ar(sound, softClipGain);

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				sound = sound.dup*envs*onOffSwitch*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				Out.ar(\outBus.kr(0), sound);
			}).writeDefFile;
		}
	}

	setAllVals {
		allValsList = List.fill(8, {List[0,0,0,0,0,1,1,1,1,0,0,0,1,1,0,0,0,0,0,0]});
	}

	init {

		this.makeWindow("Noise0", Rect(0, 0, 200, 40));

		nnVals = [
			[ 'impulseRate', ControlSpec(19.0, 20000.0, 'exp', 0.0, 0.0, "") ],
			[ 'impulseAdd', ControlSpec(0.0, 20000.0, 'linear', 0.0, 0.0, "") ],
			[ 'filterFreq', ControlSpec(20.0, 20001.0, 'exp', 0.0, 0.0, "") ],
			[ 'moogGain', ControlSpec(1.0, 4.0, 'amp', 0.0, 0.0, "") ],
			[ 'ffm_Freq', ControlSpec(0.1, 30.0, 'exp', 0.0, 0.0, "") ],
			[ 'ffm_Amp', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'ffm_Which', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'dustDensity', ControlSpec(1.0, 3000.0, 'exp', 0.0, 0.0, "") ],
			[ 'combDelayLo', ControlSpec(0.001, 2.0, 'exp', 0.0, 0.0, "") ],
			[ 'combDelayHi', ControlSpec(0.001, 2.0, 'exp', 0.0, 0.0, "") ],
			[ 'combRandRate', ControlSpec(1.0, 5.0, 'linear', 0.0, 0.0, "") ],
			[ 'combDecay', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ],
			[ 'combVol', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ],
			[ 'softClipGain', ControlSpec(1.0, 20.0, 'linear', 0.0, 0.0, "") ]
		];

		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		//dontLoadControls = (2..9).addAll((26..31)).addAll((33..35));

		dontLoadControls = (0..(sizeOfNN-1));

		"initNN_Synth".postln;

	}

	/*init2 {arg parent, otherValsBusses, onOffBus, envOnOffBus;
		synths.add(Synth("NN_Synth_Noise0", [\outBus, outBus, \volBus, otherValsBusses[0].index, \envRiseBus, otherValsBusses[1].index, \envFallBus, otherValsBusses[2].index, \onOffBus, onOffBus, \envOnOffBus, envOnOffBus], group));
		this.init_window(parent);
	}*/
}


