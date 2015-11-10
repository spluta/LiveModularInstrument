ReverbDrone_Mod : Module_Mod {
	var buffers, volBus, delayBus, distBus;

	*initClass {
		StartUp.add {
			SynthDef("reverbDrone_mod", { arg out=0, inBus, volBus, delayBus, distBus, gate = 1, pauseGate = 1;
				var in, in2, outSig, trig, local, env, pauseEnv, vol, dist, delTime;

				vol = In.kr(volBus);
				delTime = In.kr(delayBus);
				dist = In.kr(distBus);

				env = EnvGen.kr(Env.asr(0,1,0.5), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = In.ar(inBus,1)*2;

				in2 = CombC.ar(PitchShift.ar(in, 0.2, 0.25, 0, 0.1), 0.2, 0.2, 5);

				outSig = GVerb.ar(Mix.new(in), 100, 10, 0.5, 1, 0.5, 0, 0.1, 0.1);

				outSig = LPF.ar(outSig, 400);

				outSig = outSig+PitchShift.ar(outSig, 0.2, 0.5, 0, 0.1)+in2;
				outSig = outSig+ Resonz.ar(outSig, 100, 1);

				local = LocalIn.ar(2);
				local = OnePole.ar(local, 0.4);
				local = OnePole.ar(local, -0.08);

				local = Rotate2.ar(local[0], local[1], 0.2);

				local = DelayN.ar(local, 0.5, delTime);

				local = LeakDC.ar(local);
				local = ((local + outSig) * dist).softclip;

				LocalOut.ar(local);

				Out.ar(out, (local*0.1)*vol*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("ReverbDrone", Rect(500,300,150, 260));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		delayBus = Bus.control(group.server);
		distBus = Bus.control(group.server);
		synths = List.newClear(4);

		synths.put(0, Synth("reverbDrone_mod", [\out, outBus, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));


		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "Vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value)
			}, 0, layout:\vert));
		this.addAssignButton(0, \continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "delay", ControlSpec(0.1,0.5,'linear'),
			{|v|
				delayBus.set(v.value)
			}, 0.2, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 130, 60, 100), "dist", ControlSpec(1,1.25,'linear'),
			{|v|
				distBus.set(v.value)
			}, 1.1, true));
		this.addAssignButton(2,\continuous, Rect(80, 230, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 275, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("reverbDrone_mod", [\out, outBus.index+2, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("reverbDrone_mod", [\out, outBus.index+2, \inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
						});
						synths.put(2, Synth("reverbDrone_mod", [\out, outBus.index+4,\inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
						synths.put(3, Synth("reverbDrone_mod", [\out, outBus.index+6,\inBus, mixerToSynthBus.index, \volBus, volBus, \delayBus, delayBus, \distBus, distBus], group));
						numChannels = 8;
					}
				)
			};
		);
	}
}