Temp_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\temp_mod,{arg inBus, controlsBus, outBus;
var ctlIn,  onOff, env, gate, sound, pitches;

ctlIn = In.kr(controlsBus, 4);

env = EnvGen.kr(Env.asr(0.01,1,0.01), ctlIn[1]);

sound = WhiteNoise.ar(0.5)*ctlIn[0];

Out.ar(outBus, sound);
}).writeDefFile;

	}
}

loadExtra {
	}

	init {
numControls = 2;
textList = Array.fill(numControls, {"text"});
		withTextView = false;
		this.init2;
	}

init3 {
synths.add(Synth("temp_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
}
}