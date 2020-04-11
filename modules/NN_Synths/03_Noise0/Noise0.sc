Noise0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("Noise0_NNMod",{
				var impulseRate, impulseAdd, envs, onOff, sound, filterMod, dust, filterFreqMod, comb, onOffSwitch;

				//impulseRate = MouseX.kr(200, 20000);
				//impulseAdd = MouseY.kr(0, 20000);

				sound = Lag.ar(Latch.ar(WhiteNoise.ar(1), Impulse.ar(\impulseRate.kr(200))), 1/(\impulseRate.kr+\impulseAdd.kr(0)));
				filterMod = LinExp.kr(LFCub.kr(0.1, 0.5*pi)-1, 1, 180, 8500);



				//SelectX.ar(\ffm_Select.kr, [SinOsc.ar(\ffm_Freq.kr(0.1)), LFNoise2.ar(\ffm_Freq.kr)

				filterFreqMod = //Maths.ar(\ffm_Freq.kr(1), \ffm_Width.kr(0.5), 0.3, 1)[2].linlin(0,1,-1,1)
				SelectX.ar(\ffm_Which.kr(0), [SinOsc.ar(\ffm_Freq.kr(1)), LFNoise2.ar(\ffm_Freq.kr*5)])
				*(\ffm_Amp.kr(0)*\filterFreq.kr(10000, 0.05));

				sound = MoogFF.ar(sound, (\filterFreq.kr+filterFreqMod).clip(20, 20000), \moogGain.kr(1.5));

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				dust = Select.ar(\dustDensity.kr(3000)>2000, [LagUD.ar(Trig1.ar(Dust.ar(\dustDensity.kr), 0.001), 0.0005, 0.0005), K2A.ar(1)]);

				sound = sound*dust;

				comb = CombC.ar(sound, 2, TRand.kr(\combDelayLo.kr(1, 0.1), \combDelayHi.kr(1, 0.1), Impulse.kr(\combRandRate.kr(0))).lag(1/\combRandRate.kr), \combDecay.kr(1, 0.1));

				sound = sound+(comb*\combVol.kr(0, 0.1));

				sound = SoftClipAmp8.ar(sound, \softClipGain.kr(1));

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				Out.ar(\outBus.kr(0), sound.dup*envs*onOffSwitch*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1));
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


