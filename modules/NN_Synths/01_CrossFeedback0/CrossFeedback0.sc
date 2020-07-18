CrossFeedback0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("CrossFeedback0_NNMod",{
				var localIn, noise1, osc1, osc1a, osc1b, osc2, out, foldNum, dust, trigEnv, filtMod, filterFreq, envs, onOffSwitch;

				localIn = LocalIn.ar(1);

				noise1 = RLPF.ar(
					Latch.ar(WhiteNoise.ar(\noiseVol.kr(0).clip(0, 3000)), Impulse.ar(\impulse.kr(10000).clip(100, 20000))),
				Lag.kr(\filterFreq.kr(10000), 0.05).clip(200, 20000), Lag.kr(\rq.kr(0.5), 0.05).clip(0.2, 2));

				osc1 = SinOscFB.ar(\freq1.kr(300, 0.05).clip(2, 10000)+(localIn*\modVol1.kr(1).clip(0, 3000))+noise1, \freq1.kr.linlin(100, 300, 2, 0.0));

				osc1 = SelectX.ar(\freq1.kr.linlin(15.0, 25.0, 0.0, 1.0), [osc1.linlin(-1.0,1.0, 0.0, 1.0), osc1]);

				osc2 = LFTri.ar(\freq2.kr(500, 0.05).clip(2, 10000)+(osc1*\modVol2.kr(1).clip(0, 3000)));

				osc2 = LeakDC.ar(osc2);

				LocalOut.ar(osc2);

				foldNum = \fold.kr(1).clip(0.1,1);

				out = [osc2.fold2(foldNum), osc2.fold2(foldNum*0.99)]/foldNum;

				dust = LagUD.ar(Trig1.ar(Dust.ar(\dustRate.kr(1000).clip(1, 1000)), \attack.kr(0.001).clip(0.001, 0.01)+\release.kr(0.001).clip(0.001, 0.01)), \attack.kr, \release.kr);

				out = SelectX.ar((\dustRate.kr<800), [out, out*dust]);

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1)*onOffSwitch;

				filterFreq = \outFilterFreq.kr(20000, 0.05).clip(20, 20000);

				filterFreq = (LFTri.ar(\filtModFreq.kr(0))*(\filtModAmp.kr(0).clip(0,1))).linexp(-1.0, 1.0, (filterFreq/2).clip(20, 20000), (filterFreq*2).clip(20, 20000));

				out = RLPF.ar(out, filterFreq, \outFilterRQ.kr(1).clip(0.1, 1));

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, out*envs);
			}).writeDefFile;
		}
	}

	setAllVals {

		allValsList = List.fill(8, {List[0,0,0,0,0,1,1,1,1,0,0,0,1,1,0,0,0,0,0,0]});
	}

	init {

		this.makeWindow("CrossFeedback0", Rect(0, 0, 200, 40));

		numModels = 8;
		sizeOfNN = 16;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));

		nnVals = [[\freq1, ControlSpec(1, 10000, \exp)],
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
			[\filtModAmp, ControlSpec(0, 1, \amp)]

		];

		"initNN_Synth".postln;

	}

}