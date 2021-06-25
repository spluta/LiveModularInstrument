Perc0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("Perc0_NNMod",{
				var maths1, mathsTrig, endOfCycle, maths2, maths3, noiseMaths, oscNoise, noise, out, out2, env, envB, oscWobble, onOffSwitch, envs, trigEnv;

				var mlpVals = In.kr(\dataInBus.kr, 26);

				var maths1Rise = mlpVals[0].linexp(0,1,0.001,5.0);
				var maths1Fall = mlpVals[1].linexp(0,1,0.001,5.0);
				var maths1LinExp = mlpVals[2].linlin(0,1,0.0,1.0).lag(0.1);
				var hiGliss = mlpVals[3].linexp(0,1,300.0,8000.0).lag(0.1);
				var lowGliss = mlpVals[4].linexp(0,1,20.0,3000.0).lag(0.1);
				var outVol = mlpVals[5].linlin(0,1,0.0,1.0).lag(0.1);
				var oscNoiseFreq = mlpVals[6].linexp(0,1,500.0,10000.0).lag(0.1);
				var oscNoiseVol = mlpVals[7].linlin(0,1,0.0,200.0).lag(0.1);
				var oscNoiseLag = mlpVals[8].linexp(0,1,0.002,0.1).lag(0.1);
				var oscNoiseWidth = mlpVals[9].linlin(0,1,0.0,1.0).lag(0.1);
				var wobbleFreq = mlpVals[10].linlin(0,1,1.0,30.0).lag(0.1);
				var wobbleMul = mlpVals[11].linlin(0,1,1.0,30.0).lag(0.1);
				var maths2Rise = mlpVals[12].linexp(0,1,0.001,5.0);
				var maths2Fall = mlpVals[13].linexp(0,1,0.001,5.0);
				var hiGliss2 = mlpVals[14].linexp(0,1,300.0,8000.0).lag(0.1);
				var lowGliss2 = mlpVals[15].linlin(0,1,20.0,3000.0).lag(0.1);
				var out2Vol = mlpVals[16].linlin(0,1,0.0,1.0).lag(0.1);
				var noiseMathsRise = mlpVals[17].linexp(0,1,0.001,5.0);
				var noiseMathsFall = mlpVals[18].linexp(0,1,0.001,5.0);
				var noiseMathsLinExp = mlpVals[19].linlin(0,1,0.0,1.0).lag(0.1);
				var noiseFreq = mlpVals[20].linexp(0,1,1000.0,20000.0).lag(0.1);
				var noiseVol = mlpVals[21].linlin(0,1,0.0,1.0).lag(0.1);
				var noiseWidth = mlpVals[22].linlin(0,1,0.0,1.0).lag(0.1);
				var noiseFiltFreq = mlpVals[23].linexp(0,1,30.0,20000.0).lag(0.1);
				var noiseFiltMult = mlpVals[24].linlin(0,1,0.0,1.0).lag(0.1);
				var noiseFiltGain = mlpVals[25].linlin(0,1,0.0,3.5).lag(0.1);

				onOffSwitch = (\onOff0.kr(1, 0.01)+\onOff1.kr(1, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				#maths1, mathsTrig = Maths.ar(maths1Rise, maths1Fall, maths1LinExp, onOffSwitch);


				onOffSwitch = K2A.ar(onOffSwitch);

				noiseMaths = Maths.ar(noiseMathsRise, noiseMathsFall, noiseMathsLinExp,0,1,mathsTrig)[0];

				maths2 = Maths.ar(maths2Rise, maths2Fall, 0.99, 0,1, mathsTrig)[0];
				maths3 = maths2.explin(0.001, 1, 0, 1);

				oscNoise = Lag.ar(LFNoise0.ar([oscNoiseFreq, oscNoiseFreq], oscNoiseVol), oscNoiseLag);
				oscWobble = LFNoise2.ar(wobbleFreq, wobbleMul);

				out2 = LFTri.ar(maths2.linlin(0,1,[lowGliss2, lowGliss2*9/8],hiGliss2), 0, 0.5)*maths3*out2Vol;

				oscNoise = [oscNoise[0], SelectX.ar(oscNoiseWidth, [oscNoise[0],oscNoise[1]])];
				out = [LFTri.ar(maths1.linlin(0,1,lowGliss,hiGliss)+oscNoise[0]+oscWobble, 0, 0.25), LFTri.ar(maths1.linlin(0,1,lowGliss,hiGliss)+oscNoise[1]+oscWobble, 0, 0.25)]*outVol;

				out = out*(maths1.explin(0.0001, 1, 0.0001, 1).explin(0.0001, 1, 0.0001, 1)); //try to use the maths as a vol as well

				noise = LFDNoise1.ar([noiseFreq, noiseFreq], 2)*noiseVol*noiseMaths;

				noise = SelectX.ar(noiseWidth, [noise[0].dup, noise]);

				noise = MoogFF.ar(noise, noiseFiltFreq*(SelectX.ar(noiseFiltMult, [K2A.ar(1), noiseMaths])), noiseFiltGain);


				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				out = (out+out2+noise)*envs*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);//*trigEnv;

				Out.ar(\outBus.kr, out*2)
			}).writeDefFile
		}
	}

	init {

		this.makeWindow("Perc0", Rect(0, 0, 200, 40));

		nnVals =[ [ 'maths1Rise', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'maths1Fall', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'maths1LinExp', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'hiGliss', ControlSpec(300.0, 8000.0, 'exp', 0.0, 0.0, "") ], [ 'lowGliss', ControlSpec(20.0, 3000.0, 'exp', 0.0, 0.0, "") ], [ 'outVol', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'oscNoiseFreq', ControlSpec(500.0, 10000.0, 'exp', 0.0, 0.0, "") ], [ 'oscNoiseVol', ControlSpec(0.0, 200.0, 'linear', 0.0, 0.0, "") ], [ 'oscNoiseLag', ControlSpec(0.002, 0.1, 'exp', 0.0, 0.0, "") ], [ 'oscNoiseWidth', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'wobbleFreq', ControlSpec(1.0, 30.0, 'linear', 0.0, 0.0, "") ], [ 'wobbleMul', ControlSpec(1.0, 30.0, 'linear', 0.0, 0.0, "") ], [ 'maths2Rise', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'maths2Fall', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'hiGliss2', ControlSpec(300.0, 8000.0, 'exp', 0.0, 0.0, "") ], [ 'lowGliss2', ControlSpec(20.0, 3000.0, 'linear', 0.0, 0.0, "") ], [ 'out2Vol', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'noiseMathsRise', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'noiseMathsFall', ControlSpec(0.001, 5.0, 'exp', 0.0, 0.0, "") ], [ 'noiseMathsLinExp', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'noiseFreq', ControlSpec(1000.0, 20000.0, 'exp', 0.0, 0.0, "") ], [ 'noiseVol', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'noiseWidth', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'noiseFiltFreq', ControlSpec(30.0, 20000.0, 'exp', 0.0, 0.0, "") ], [ 'noiseFiltMult', ControlSpec(0.0, 1.0, 'linear', 0.0, 0.0, "") ], [ 'noiseFiltGain', ControlSpec(0.0, 4.0, 'linear', 0.0, 0.0, "") ]]

		;


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}
}


