FollowerCrusher_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("followerCrusher_mod", {arg inBus, outBus, inVol=0, synthVol=0, thresh=0.1, offOn = 0;
				var decay, freq, hasFreq, sigVol, sig, fx1, fx2, in, inMix;

				in = In.ar(inBus, 2);
				inMix = Mix.new(in);

				decay = 0.99;
				#freq, hasFreq = Pitch.kr(inMix);

				sigVol = PeakFollower.ar(inMix*hasFreq, decay);

				sigVol = LagUD.ar((sigVol > thresh)*sigVol, 0.01, 0.2);

				sig = SinOsc.ar((freq*4)+SinOsc.ar(freq, 0, SinOsc.kr(0.3).range(200,2000)), 0, 0.5*sigVol*synthVol).clip2(0.1);

				fx1=Latch.ar(sig.round(0.125),Impulse.ar(LFNoise2.ar(0.5).range(3000, 8000)));
				fx2=Latch.ar(sig.round(0.1),Impulse.ar(LFNoise2.ar(0.5).range(3000, 8000)));

				Out.ar(outBus, in*inVol);
				Out.ar(outBus, [fx1, fx2]*offOn);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("FollowerCrusher", Rect(700, 700, 270, 190));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("followerCrusher_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));

		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"inVol", ControlSpec(0.0,1.0,\amp),
			{|v|
				synths[0].set(\inVol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(EZSlider(win, Rect(70, 5, 60, 160),"synthVol", ControlSpec(0.0,1.0,\amp),
			{|v|
				synths[0].set(\synthVol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(1, \continuous, Rect(70, 165, 60, 16));

		controls.add(EZSlider(win, Rect(135, 5, 60, 160),"thresh", ControlSpec(0.02,0.2,\amp),
			{|v|
				synths[0].set(\synthVol, v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(2, \continuous, Rect(135, 165, 60, 16));

		controls.add(Button(win, Rect(200, 5, 60, 60))
			.states_([["input", Color.red, Color.black],["synth", Color.black, Color.green]])
			.action_{arg butt;
				synths[0].set(\offOn, butt.value);
			});
		this.addAssignButton(3, \onOff, Rect(200, 65, 60, 60));
	}
}