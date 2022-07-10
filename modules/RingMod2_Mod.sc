RingMod2_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\ringmod2_mod,{arg inBus, controlsBus, outBus;
				var ctlIn = In.kr(controlsBus, 7);
				var freq= ctlIn[1].linexp(0,1,20, 2000).lag;
				var fb = ctlIn[0].linexp(0,1,0.01,1.9).lag;
				var vol = ctlIn[6].lincurve(0,1,0, 1).lag;

				var noise, sig;
				var noiseFreq=ctlIn[3].linexp(0,1,200, 20000).lag;
				var noiseMul =ctlIn[4].linlin(0,1,1, 2000).lag;
				var onOff = (ctlIn[2]+ctlIn[5]).clip(0,1).lag;

				noise = LFNoise1.ar(noiseFreq)*noiseMul;
				sig = SinOscFB.ar(([freq, freq]+LFNoise2.ar([0.1,0.12], 3)+noise).clip(40, 20000), fb, 1);

				sig = sig*In.ar(inBus,2)*4;

				sig = (T312AX7.ar(sig)*0.4).softclip;

				Out.ar(outBus, sig*vol*onOff);

			}).writeDefFile;
		}
	}

	loadExtra {}

	init {
		numControls = 7;
		textList = Array.with("feedback", "freq", "onOff", "noiseFreq", "noiseMul", "onOff", "vol");
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("ringmod2_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}