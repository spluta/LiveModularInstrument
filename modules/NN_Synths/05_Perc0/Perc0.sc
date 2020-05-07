Perc0_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("Perc0_NNMod",{
				var maths1, maths1Rise, mathsTrig, endOfCycle, maths2, maths2Rise, maths3, noiseMaths, noiseMathsRise, oscNoise, noise, out, out2, env, envB, oscWobble, onOffSwitch, envs, trigEnv;

				onOffSwitch = (\onOff0.kr(1, 0.01)+\onOff1.kr(1, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);
				//maths1Rise = \maths1Dur.kr(1, 0.1).clip(0.01, 10)*\maths1RiseToFall.kr(0.001, 0.1).clip(0.001, 1);

				#maths1, mathsTrig = Maths.ar(\maths1Rise.kr(0.5), \maths1Fall.kr(0.5), \maths1LinExp.kr(1, 0.1), 1, onOffSwitch-0.05);

				//#maths1, mathsTrig = Maths.ar(maths1Rise, (\maths1Dur.kr-maths1Rise).poll, \maths1LinExp.kr(1, 0.1), 1, onOffSwitch-0.05);

				//#maths1, mathsTrig = Maths.ar(MouseX.kr, MouseY.kr, \maths1LinExp.kr(1, 0.1), 1, onOffSwitch-0.05);

				trigEnv = (onOffSwitch + Lag.kr(Latch.kr(onOffSwitch, mathsTrig), 0.0015)).clip(0,1);

				noiseMaths = Maths.ar(\noiseMathsRise.kr(0.01), \noiseMathsFall.kr(0.5), \noiseMathsLinExp.kr(0.5, 0.1), 0, mathsTrig)[0];

				maths2 = Maths.ar(\maths2Rise.kr(0.01), \maths2Fall.kr(0.01), 0.99, 0, mathsTrig)[0];
				maths3 = maths2.explin(0.001, 1, 0, 1);//Maths.ar(maths2Rise, \maths2Dur.kr-maths2Rise, 0.5, 0, mathsTrig)[0];

				oscNoise = Lag.ar(LFNoise0.ar([\oscNoiseFreq.kr(1000, 0.1), \oscNoiseFreq.kr], \oscNoiseVol.kr(50, 0.1)), \oscNoiseLag.kr(1/500, 0.1));
				oscWobble = LFNoise2.ar(\wobbleFreq.kr(20, 0.1), \wobbleMul.kr(10,0.1));

				out2 = LFTri.ar(maths2.linlin(0,1,[\lowGliss2.kr(40, 0.1), \lowGliss2.kr*9/8],\hiGliss2.kr(4000, 0.1)), 0, 0.5)*maths3*\out2Vol.kr(1, 0.1);

				oscNoise = [oscNoise[0], SelectX.ar(\oscNoiseWidth.kr(0, 0.1), [oscNoise[0],oscNoise[1]])];
				out = [LFTri.ar(maths1.linlin(0,1,\lowGliss.kr(50, 0.1),\hiGliss.kr(2000, 0.1))+oscNoise[0]+oscWobble, 0, 0.25), LFTri.ar(maths1.linlin(0,1,\lowGliss.kr,\hiGliss.kr)+oscNoise[1]+oscWobble, 0, 0.25)]*\outVol.kr(1, 0.1);

				noise = LFDNoise1.ar([\noiseFreq.kr(5000, 0.1), \noiseFreq.kr], 2)*\noiseVol.kr(1, 0.1)*noiseMaths;

				noise = SelectX.ar(\noiseWidth.kr(0, 0.1), [noise[0].dup, noise]);

				noise = MoogFF.ar(noise, \noiseFiltFreq.kr(10000, 0.1)*(SelectX.ar(\noiseFiltMult.kr(0, 0.1), [K2A.ar(1), noiseMaths])), \noiseFiltGain.kr(1, 0.1));


				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				out = (out+out2+noise)*envs*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*trigEnv;

				Out.ar(\outBus.kr, out)
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

		"initNN_Synth".postln;

	}
}


