BandPassLoops_Mod : Module_Mod {
	var largeEnv, group0, group1, largeEnvBus, nextTime, localRout, delayTime, length, pitchStart, pitchEnd, rqStart, rqEnd, volBus, delayVar, waitVar, transferBus, smallLength, duration;

	*initClass {
		StartUp.add {
			SynthDef("bplVerb_mod",{arg inBus, outBus, gate=1, pauseGate=1;
				var in, env, out, pauseEnv;

				in = In.ar(inBus, 1);

				//out = Mix.new(GVerb.ar(in, 200, 3, 1,�1, 15, 0, 0, 1, 200));
				out = Mix.new(GVerb.ar(in, 200, 3, drylevel:0, taillevel:1)*0.2);
				//Out.ar(0, in); Out.ar(1, out);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:0);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;

			SynthDef("bplFilterSynth2_mod", {arg inBus, outBus, pitchStart, pitchEnd, smallLength, duration, volBus;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, smallEnv, volume, xStart, xEnd, impulse, pitch;

				buffer = LocalBuf(88200);

				volume = In.kr(volBus);

				in = In.ar(inBus, 1);

				//Out.ar(0, in);

				env = EnvGen.kr(Env.new([0,1,0], [0.001,duration-0.002], 'linear'), doneAction:2);

				in = BPF.ar(in, Rand(200, 15,000), Rand(0.15, 0.3));

				RecordBuf.ar(in, buffer, loop:0);

				pitch = XLine.kr(pitchStart, pitchEnd, duration);

				impulse = DelayN.ar(Impulse.ar(1/(smallLength)), 0.1, 0.1);

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				out = TGrains.ar(2, impulse, buffer, pitch, smallLength/2, smallLength, Line.kr(xStart, xEnd, duration));

				//out = PlayBuf.ar(1, buffer, pitch, DelayN.kr(impulse, 2, (smallLength/2)+0.05), 0, 1)+PlayBuf.ar(1, buffer, XLine.kr(pitchStart, pitchEnd, duration), DelayN.kr(impulse, 2, 0.05), 0, 1);

				Out.ar(outBus, out*volume*env);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BandPassLoops",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		group0 = Group.head(group);
		group1 = Group.tail(group);

		transferBus = Bus.audio(group.server);
		volBus = Bus.control(group.server);

		volBus.set(0);
		synths = List.new;
		synths.add(Synth("bplVerb_mod", [\inBus, mixerToSynthBus.index, \outBus, transferBus.index], group0));

		localRout = Routine.new({{
			length = 2.0.rand + 5;

			pitchStart = 1;
			pitchEnd = exprand(0.5, 2);

			smallLength = rrand(0.5, 1.5);
			duration = rrand(10, 15);

			switch(numChannels,
				2,{
					Synth("bplFilterSynth2_mod", [\inBus, transferBus.index, \outBus, outBus, \pitchStart, pitchStart, \pitchEnd, pitchEnd, \smallLength, smallLength, \duration, duration, \volBus, volBus.index], group1);
				},
				4,{
					Synth("bplFilterSynth4_mod", [\inBus, transferBus.index, \outBus, outBus, \pitchStart, pitchStart, \pitchEnd, pitchEnd, \smallLength, smallLength, \duration, duration, \volBus, volBus], group1);
				},
				8,{
					Synth("bplFilterSynth8_mod", [\inBus, transferBus.index, \outBus, outBus, \pitchStart, pitchStart, \pitchEnd, pitchEnd, \smallLength, smallLength, \duration, duration, \volBus, volBus], group1);
				}
			);
			(waitVar + (0.35.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous,Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "delay", ControlSpec(1,5,'linear'),
			{|v|
				delayVar = v.value;
			}, 3.0, true));
		this.addAssignButton(1,\continuous,Rect(80, 110, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 130, 60, 100), "wait", ControlSpec(0.3,1.0,'linear'),
			{|v|
				waitVar = v.value;
			}, 0.55, true));
		this.addAssignButton(2,\continuous,Rect(80, 230, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 275, 60, 20))
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


		SystemClock.play(localRout);
	}

	pause {
		largeEnv.set(\pauseGate, 0);
	}

	unpause {
		largeEnv.set(\pauseGate, 1);
	}


	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}
