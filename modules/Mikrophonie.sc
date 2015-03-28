Mikrophonie_Mod : Module_Mod {

	*initClass {
		StartUp.add {

			SynthDef("mikrophonie_mod", {|inBus, outBus, vol=0, hiFreq=10000, lowFreq=30, pan=0, gate = 1, pauseGate = 1|
				var in, out, largeEnv, env, pauseEnv, centerFreq, panPlus, panTot;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				in = In.ar(inBus);

				out = RLPF.ar(in, Lag.kr(max(lowFreq, hiFreq), 0.05), 0.8);
				out = HPF.ar(out, Lag.kr(min(lowFreq, hiFreq), 0.05));

				//centerFreq = ((max(lowFreq, hiFreq)-min(lowFreq, hiFreq))/2)+min(lowFreq, hiFreq);

				//out = Resonz.ar(in, centerFreq, abs(hiFreq-lowFreq)/centerFreq);
				//out = BPF.ar(in, centerFreq, abs(hiFreq-lowFreq)/centerFreq);

				out = env*pauseEnv*vol*out;

				panPlus = LFNoise2.kr(0.3).range(-0.3, 0.3);

				panTot = (pan+panPlus).clip(-1, 1);

				Out.ar(outBus, Pan2.ar(out, panTot));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Mikrophonie",Rect(518, 645, 150, 270));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		synths.add(Synth("mikrophonie_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 0, \hiFreq, 10000, \lowFreq, 30], group));

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,1,'linear'),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(70, 10, 60, 220), "low", ControlSpec(30,16000,'exponential'),
			{|v|
				synths[0].set(\lowFreq, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(70, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(130, 10, 60, 220), "high", ControlSpec(30,16000,'exponential'),
			{|v|
				synths[0].set(\hiFreq, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(2,\continuous, Rect(130, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(190, 10, 60, 220), "pan", ControlSpec(-1, 1),
			{|v|
				synths[0].set(\pan, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(3,\continuous, Rect(190, 230, 60, 20));

	}
}
