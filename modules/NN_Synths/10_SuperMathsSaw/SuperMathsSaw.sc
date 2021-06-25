SuperMathsSaw_NNMod : NN_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("SuperMathsSaw_NNMod", {
				var envs, onOffSwitch;
				var mlpVals = In.kr(\dataInBus.kr, 22);

				var iter = 0;

				var freq = Lag.kr(mlpVals[iter], 0.05).linexp(0,1,20,10000);
				var mix = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var detune = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var overdrive = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1,2,20);

				var rise_saw_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
				var fall_saw_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,20);
				var logExp1 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var saw_fr_mul = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.001,10000);
				//9
				var rise_lpg = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.01,2);
				var fall_lpg = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.01,2);
				var logExp2 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var control_offset = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0.0,1.0);
				var control_scale = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var vca = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var resonance = Lag.kr(mlpVals[iter = iter+1], 0.05).linlin(0,1,0,1.8);

				var rise_lowpass = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.1,20);
				var fall_lowpass = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,0.1,20);
				var logExp3 = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,1);
				var low_filt_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,20,20000);
				var hi_filt_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,20,20000);
				var moog_res = Lag.kr(mlpVals[iter = iter+1], 0.05).clip(0,0.995);
				var hpf_freq = Lag.kr(mlpVals[iter = iter+1], 0.05).linexp(0,1,40,20000);
				var maths1, maths2, maths3, out;

								//in every synth
				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);
				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				maths1 = Maths.ar(rise_saw_freq, fall_saw_freq, logExp1, onOffSwitch)[0]*saw_fr_mul;
				maths2 = Maths.ar(rise_lpg, fall_lpg, logExp2, onOffSwitch)[0];
				maths3 = Maths.ar(rise_lowpass, fall_lowpass, logExp3)[0];

				out = SuperSaw.ar((freq+maths1), mix, detune);
				out = (out*overdrive).softclip;

				out = LPG.ar(out, A2K.kr(maths2), control_offset, control_scale, vca, resonance);
				out = MoogVCF2.ar(out, maths3.linexp(0,1,low_filt_freq,hi_filt_freq), moog_res);

				out = HPF.ar(HPF.ar(out,40), hpf_freq);

				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				Out.ar(\outBus.kr, out*envs);
			}).writeDefFile;
		}
	}

	init {

		this.makeWindow("SuperMathsSaw", Rect(0, 0, 200, 40));

		nnVals = ["freq", "mix", "detune", "overdrive", "rise_saw_freq", "fall_saw_freq", "logExp1", "saw_fr_mul", "rise_lpg", "fall_lpg", "logExp2", "control_offset", "control_scale", "vca", "resonance", "rise_lowpass", "fall_lowpass", "logExp3", "low_filt_freq", "hi_filt_freq", "moog_res", "hpf_freq"].collect({|item| [item.asSymbol]});


		numModels = 8;
		sizeOfNN = nnVals.size;

		this.initControlsAndSynths(sizeOfNN);

		dontLoadControls = (0..(sizeOfNN-1));
	}

}
