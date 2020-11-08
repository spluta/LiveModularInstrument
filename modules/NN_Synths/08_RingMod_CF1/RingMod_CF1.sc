RingMod_CF1_NNMod : NN_Synths_Analysis_Mod {
	*initClass {
		StartUp.add {

			SynthDef("RingMod_CF1_NNMod",{
				var localIn, noise1, osc1, osc1a, osc1b, osc2, out, foldNum, dust, trigEnv, filtMod, filterFreq, envs, onOffSwitch, instrAmp;

				localIn = LocalIn.ar(1);

				osc1 = SinOscFB.ar(\freq1.kr(300, 0.01).clip(2, 10000)+(localIn*\modVol1.kr(1).clip(0, 3000)), \freq1.kr.linlin(100, 300, 2, 0.0));

				osc1 = SelectX.ar(\freq1.kr.linlin(15.0, 25.0, 0.0, 1.0), [osc1.linlin(-1.0,1.0, 0.0, 1.0), osc1]);

				osc2 = LFTri.ar(\freq2.kr(500, 0.01).clip(2, 10000)+(osc1*\modVol2.kr(1).clip(0, 3000)));

				osc2 = LeakDC.ar(osc2);

				LocalOut.ar(osc2);

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				osc2 = osc2*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1)*onOffSwitch;

				//fold
				foldNum = \fold.kr(1).clip(0.1,1);
				out = [osc2.fold2(foldNum), osc2.fold2(foldNum*0.99)]/foldNum;

				//filter
				filterFreq = \outFilterFreq.kr(20000, 0.05).clip(20, 20000);
				filterFreq = (LFTri.ar(\filtModFreq.kr(0))*(\filtModAmp.kr(0).clip(0,1))).linexp(-1.0, 1.0, (filterFreq/2).clip(20, 20000), (filterFreq*2).clip(20, 20000));
				out = RLPF.ar(out, filterFreq, \outFilterRQ.kr(1).clip(0.1, 1));

				//envs
				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				instrAmp = Lag.kr(In.kr(\instrAmpBus.kr).linlin(0,0.1,0.0, 1.0, 'minmax'), 0.2);
				instrAmp = Select.kr(\switchState.kr, [1, instrAmp, instrAmp]);

				Out.ar(\outBus.kr, out*envs*instrAmp);
			}).writeDefFile;
		}
	}


	init {

		this.makeWindow("RingMod_CF1", Rect(0, 0, 200, 40));

		numModels = 8;
		sizeOfNN = 7;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));

		nnVals = [[\freq1, ControlSpec(20, 5000, \exp)],
			[\freq2, ControlSpec(200, 5000, \exp)],
			[\modVol1, ControlSpec(0, 300)],
			[\modVol2, ControlSpec(0, 3000)],
			[\fold, ControlSpec(0.1, 1)],
			[\outFilterFreq, ControlSpec(300, 20000, \exp)],
			[\outFilterRQ, ControlSpec(0.1, 2, \exp)]
		];


	}
}
