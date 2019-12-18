PitchShift_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("pitchShift_mod", {
				var in, envs, out;

				in = In.ar(\inBus.kr, 1);

				out = [PitchShift.ar(in, 0.19, \shift.kr(0), 0, 0.001), PitchShift.ar(in, 0.21, \shift.kr, 0, 0.001)];

				envs = Envs.kr(1, \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, out*envs*\vol.kr(0, 0.1));
			}).writeDefFile;
		}
	}

	init {
		"initPitchshift".postln;
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		synths = List.newClear(1);
		synths.put(0, Synth("pitchShift_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \shift, 0], group));

		controls.add(QtEZSlider.new("shift", ControlSpec(-1, 1),
			{|v|
				synths[0].set(\shift, v.value.midiratio.postln)
			}, 0.0, true, \horz));
		this.addAssignButton(0, \continuous);

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1),
			{|v|
				synths[0].set(\vol, v.value)
			}, 1.0, true, \horz));
		this.addAssignButton(1, \continuous);

		this.makeWindow("PitchShift", Rect(0, 0, 200, 40));

		win.layout_(VLayout(
			HLayout(controls[0].layout, assignButtons[0].layout),
			HLayout(controls[1].layout, assignButtons[1].layout)
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];


	}
}