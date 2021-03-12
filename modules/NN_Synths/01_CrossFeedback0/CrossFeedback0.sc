CrossFeedback0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("CrossFeedback0_NNMod",{
				var localIn, noise1, osc1, osc1a, osc1b, osc2, out, foldNum, dust, trigEnv, filtMod, envs, onOffSwitch;

				var mlpVals = In.kr(\dataInBus.kr, 16);

				var freq1 = Lag.kr(mlpVals[0], 0.05).linexp(0,1,2,10000);
				var freq2 = Lag.kr(mlpVals[1], 0.05).linexp(0,1, 5, 10000);
				var modVol1 = mlpVals[2].linlin(0, 1, 0, 3000);
				var modVol2 = mlpVals[3].linlin(0, 1, 0, 3000);
				var noiseVol = mlpVals[4].linlin(0, 1, 0, 3000);
				var impulse = mlpVals[5].linexp(0,1, 100, 20000);
				var filterFreq = Lag.kr(mlpVals[6], 0.05).linexp(0, 1, 200, 10000);
				var rq = Lag.kr(mlpVals[7],  0.05).linlin(0, 1, 0.2, 2);
				var fold = mlpVals[8].linlin(0,1,0.1,1);
				var dustRate = mlpVals[9].linlin(0,1, 1000, 1);
				var attack = mlpVals[10].linexp(0, 1, 0.001, 0.01);
				var release = mlpVals[11].linexp(0, 1, 0.001, 0.01);
				var outFilterFreq = Lag.kr(mlpVals[12],  0.05).linexp(0, 1, 20, 20000);
				var outFilterRQ = mlpVals[13].linexp(0, 1, 0.1, 1);
				var filtModFreq = mlpVals[14].linlin(0, 1, 0, 30);
				var filtModAmp = mlpVals[15].lincurve(0, 1, 0, 1, 1);

				localIn = LocalIn.ar(1);

				noise1 = RLPF.ar(Latch.ar(WhiteNoise.ar(noiseVol), Impulse.ar(impulse)), filterFreq, rq);
				//Poll.kr(Impulse.kr(2)*\isCurrent.kr, noise1);

				osc1 = SinOscFB.ar(freq1+(localIn*modVol1)+noise1, freq1.linlin(100, 300, 2, 0.0));
				osc1 = SelectX.ar(freq1, [osc1.linlin(-1.0,1.0, 0.0, 1.0), osc1]);
				osc2 = LFTri.ar(freq2+(osc1*modVol2));
				osc2 = LeakDC.ar(osc2);

				LocalOut.ar(osc2);

				out = [osc2.fold2(fold), osc2.fold2(fold*0.99)]/fold;
				dust = LagUD.ar(Trig1.ar(Dust.ar(dustRate), attack + release), attack, release);

				out = SelectX.ar((dustRate<800), [out, out*dust]);
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.05), \isCurrent.kr*onOffSwitch, onOffSwitch]);
				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1)*onOffSwitch;


				outFilterFreq = (LFTri.ar(filtModFreq)*filtModAmp).linexp(-1.0, 1.0, (outFilterFreq/2).clip(40, 18000), (outFilterFreq*2).clip(40, 18000));
				out = RLPF.ar(out, outFilterFreq, outFilterRQ);
				out = LeakDC.ar(out);

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