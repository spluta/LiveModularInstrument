Distortion2D_Mod : Module_Mod {
	var mouseDelayLine, mouseBus, mouseBuf, recMouse, playMouse, recAudio, playAudio, spec0, spec1;
	var goButton, distortRout, mouseDelayGroup, synthGroup, volBus, delayedMiceGroup, mouseDelayz, delayedMiceBus;

	*initClass {
		StartUp.add {
			SynthDef("mouseDelayz_mod",{ arg mouseBus, delayedMiceBus, delayTime0=0.1, delayTime1=2, gate = 1;
				var in, env, delayTime;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
				in = LagIn.kr(mouseBus, 2, 0.1);

				Out.kr(delayedMiceBus, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+2, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+4, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+6, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+8, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+10, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+12, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
				Out.kr(delayedMiceBus+14, DelayC.kr(in, 2.1, SinOsc.kr(Rand(0.1,0.2)).range(delayTime0, delayTime1)));
			}).writeDefFile;

			SynthDef("mouseDistort_mod",{arg inBus, outBus, attack, sustain, decay, mouseBus, volBus;
				var in, env, time, bigVol, length, vol, mouseIn;
				var out;

				length = attack+sustain+decay;

				mouseIn = In.kr(mouseBus, 2).abs;

				vol = In.kr(volBus);

				in = In.ar(inBus, 1);

				in = CombC.ar(in, 0.2, mouseIn[1], 0.5)+in;

				env = EnvGen.kr(Env([0,1,1,0],[attack, sustain, decay]), doneAction: 2);

				in = in.fold2(mouseIn[0])*env;
				out = Pan2.ar(in,
					Line.kr(Rand(-1.0,1.0), Rand(-1.0,1.0), length), vol
				);

				out = LeakDC.ar(out, 0.995);

				Out.ar(outBus, out);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("Distortion2D", Rect(600, 588, 310, 275));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		mouseDelayGroup = Group.new(group, \addToTail);
		delayedMiceGroup = Group.new(group, \addToTail);
		synthGroup = Group.new(group, \addToTail);

		delayedMiceBus = Bus.control(group.server, 16);

		mouseBus = Bus.control(group.server, 2);
		mouseBus.setn([0.1, 0.002]);

		mouseDelayz = Synth("mouseDelayz_mod", [\mouseBus, mouseBus.index, \delayedMiceBus, delayedMiceBus.index], delayedMiceGroup);

		volBus = Bus.control(group.server);
		volBus.set(0);

		controls.add(QtEZSlider.new("Vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, true, \vert));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider2D(ControlSpec(0.1, 1, 'exp'), ControlSpec(0.002,0.1, 'exp'), {|vals|
			mouseBus.setn([vals[0], vals[1]]);
		}, [0.5,0.05]));
		this.addAssignButton(1,\slider2D);

		controls.add(QtEZRanger("delayRange", ControlSpec(0.1, 2), {|vals|
			mouseDelayz.set(\delayTime0, vals.lo, \delayTime1, vals.hi)
		}, [0.1, 2], true, \horz));
		this.addAssignButton(2,\range);

		//multichannel button
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
					},
					1, {
						numChannels = 4;
					},
					2, {
						numChannels = 8;
					}
				)
			};
		);

		distortRout = Task({{
			switch(numChannels,
				2, {Synth("mouseDistort_mod", [\inBus, mixerToSynthBus, \outBus, outBus, \attack, rrand(2.0,4.0), \sustain, rrand(2.0,4.0), \decay, rrand(2.0,4.0), \mouseBus, delayedMiceBus.index+(8.rand*2), \volBus, volBus.index], synthGroup);
				},
				4,{Synth("mouseDistort_mod", [\inBus, mixerToSynthBus, \outBus, outBus.index+[0,2].choose, \attack, rrand(2.0,4.0), \sustain, rrand(2.0,4.0), \decay, rrand(2.0,4.0), \mouseBus, delayedMiceBus.index+(8.rand*2), \volBus, volBus.index], synthGroup);
				},
				8,{Synth("mouseDistort_mod", [\inBus, mixerToSynthBus, \outBus, outBus.index+[0,2,4,6].choose, \attack, rrand(2.0,4.0), \sustain, rrand(2.0,4.0), \decay, rrand(2.0,4.0), \mouseBus, delayedMiceBus.index+(8.rand*2), \volBus, volBus.index], synthGroup);
				}
			);

			rrand(1.0, 2.0).wait;
		}.loop});

		distortRout.start;

		win.layout_(
			VLayout(
				HLayout(
					VLayout(controls[0], assignButtons[0]),
					VLayout(controls[1], assignButtons[1])
				),
				HLayout(controls[2], assignButtons[2]),
				HLayout(controls[3], nil)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;

	}

	pause {
		distortRout.pause;
		synthGroup.run(false);
		mouseDelayGroup.run(false);
		delayedMiceGroup.run(false);
	}

	unpause {
		distortRout.resume;
		synthGroup.run(true);
		mouseDelayGroup.run(true);
		delayedMiceGroup.run(true);
	}

	killMeSpecial {
		mouseBus.free;
		volBus.free;
		recAudio.set(\gate, 0);
		recMouse.set(\gate, 0);
		playMouse.set(\gate, 0);
		playAudio.set(\gate, 0);
		mouseDelayz.set(\gate, 0);
		mouseDelayLine.free;

		mouseDelayGroup.free;
		delayedMiceGroup.free;
		synthGroup.free;

		distortRout.stop;

	}
}