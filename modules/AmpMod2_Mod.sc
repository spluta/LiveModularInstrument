AmpMod2_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\ampmod2_mod,{arg inBus, controlsBus, outBus;
				var ctlIn = In.kr(controlsBus, 3);
				var onOff = (ctlIn[0]+ctlIn[1]).clip(0,1);
				var rate = ctlIn[2].linlin(0,1,1,40);
				var sound = In.ar(inBus, 2);

				onOff = SelectX.ar(onOff.lag(0.01), [DC.ar(1), (Phasor.ar(onOff, rate/SampleRate.ir, -1.0, 1.0, -1.0)>0).lag(0.01)]);

				sound = sound*onOff;

				Out.ar(outBus, sound);
			}).writeDefFile;
		}
	}

	loadExtra {
	}

	init {
		numControls = 3;
		textList = Array.with("OnOff1", "OnOff2", "Rate");
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("ampmod2_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}