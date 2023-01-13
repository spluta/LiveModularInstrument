LPFInterupter_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\lpfinterupter_mod,{arg inBus, controlsBus, outBus;
				var in, out, ctlIn, filtFreq, filt, res, q, centerFreq, oscFreq, onOff, sine, width, z2Lag, sig;

				in = In.ar(inBus, 2);
				ctlIn = In.kr(controlsBus, 6);

				width = Lag.kr(1-ctlIn[4], 0.2);

				oscFreq = Lag.kr(ctlIn[5].linexp(0,1,0.025,20));

				z2Lag = LagUD.kr(ctlIn[3], 0.01, 2);

				sine = (Phasor.kr(ctlIn[3], (oscFreq/ControlRate.ir), -1, 1).abs)*width*z2Lag;

				onOff = (ctlIn[0]+z2Lag).clip(0,1);

				filtFreq = Lag.kr(SelectX.kr(ctlIn[3], [(1-onOff)+ctlIn[1], (1-sine)]).clip(0, 1.0), 0.1);

				filtFreq = Lag.kr(filtFreq.linexp(0, 1, 20, 22050));

				//res = Lag.kr(ctlIn[2].linlin(0,1,0,0.95));
				q = Lag.kr(ctlIn[2].linlin(0,1,0.8,0.075));

				//filt = MoogVCF2.ar(in, filtFreq, res);
				filt = RLPF.ar(in, filtFreq, q);


				Out.ar(outBus, filt);
			}).writeDefFile;

		}
	}

	loadExtra {
	}

	init {
		numControls = 6;
		//textList = Array.fill(numControls, {"text"});
		textList = ["onOff", "lpFreq", "reson", "onOff2", "lfoWidth", "lfoFreq"];
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("lpfinterupter_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}