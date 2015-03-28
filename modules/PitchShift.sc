PitchShift_Mod : Module_Mod {
	var shiftBus;

	*initClass {
		StartUp.add {
			SynthDef("pitchShift_mod", {arg inBus, outBus, shiftBus, gate = 1, pauseGate = 1;
				var in, env, out, pauseEnv, shift;

				shift = In.kr(shiftBus);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				in = In.ar(inBus, 1);

				out = [PitchShift.ar(in, 0.07, shift, 0, 0.001), PitchShift.ar(in, 0.07, shift, 0, 0.001)];

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("PitchShift", Rect(560, 10, 80, 190));
		this.initControlsAndSynths(1);

		this.makeMixerToSynthBus;

		shiftBus = Bus.control(group.server);

		synths = List.newClear(4);
		synths.put(0, Synth("pitchShift_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \shiftBus, shiftBus], group));

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 150), "shift", ControlSpec(1,4,'exponential'),
			{|v|
				shiftBus.set(v.value);
			}, 1.0, true, layout:\vert));
		this.addAssignButton(0, \continuous, Rect(10, 160, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(0, 325, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("pitchShift_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \shiftBus, shiftBus], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("pitchShift_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \shiftBus, shiftBus], group));
						});
						2.do{|i| synths.put(i+2, Synth("pitchShift_mod", [\inBus, mixerToSynthBus.index+4+(2*i), \outBus, outBus.index+4+(2*i), \shiftBus, shiftBus], group))};
						numChannels = 8;
					}
				)
			};
		);
	}
}