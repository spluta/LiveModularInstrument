StarDust_Mod : Module_Mod {
	var largeEnv, dustGroup, delayGroup, largeEnvBus, transferBus, volBus, nextTime, localRout, delayTime, length, filterStart, filterEnd, rqStart, rqEnd, xStart, xEnd, filterVol, delayVar, waitVar;

	*initClass {
		StartUp.add {
			SynthDef("starDust_mod", { arg inBus, transferBus, largeEnvBus, inVol=1, pauseGate=1, gate=1, muteGate=1;
				var in, chain, chain2, chain3, amp, pauseEnv, env, muteEnv;

				in = In.ar(inBus)*inVol;
				amp = Amplitude.kr(in);

				chain = FFT(LocalBuf(2048), in);
				chain = PV_BrickWall(chain, 0.14);
				chain = PV_MagAbove(chain, LFNoise2.kr(1).range(0, 0.2));

				chain2 = PV_Copy(chain, LocalBuf(2048));
				chain3 = PV_Copy(chain, LocalBuf(2048));

				chain = PV_RandComb(chain, 0.9, Impulse.kr((LFNoise2.kr(0.5).range(15, 20)+LagUD.kr((amp*15), 0.1, 1))));
				chain2 = PV_RandComb(chain2, 0.9, Impulse.kr((LFNoise2.kr(0.5).range(15, 20)+LagUD.kr((amp*15), 0.1, 1))));
				chain3 = PV_RandComb(chain3, 0.9, Impulse.kr((LFNoise2.kr(0.5).range(15, 20)+LagUD.kr((amp*15), 0.1, 1))));

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:0);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
				muteEnv = EnvGen.kr(Env.asr(1,1,0), muteGate, doneAction:0);

				Out.kr(largeEnvBus, pauseEnv);

				Out.ar(transferBus, [IFFT(chain), IFFT(chain2), IFFT(chain3)]*pauseEnv*env*muteEnv*4);
			}).writeDefFile;


			SynthDef("starDustDelays_mod", {arg transferBus, outBus, largeEnvBus, xStart, xEnd, delayTime, length, volBus;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, decayTime;

				volume = In.kr(volBus);

				in = In.ar(transferBus, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');


				in2 = EnvGen.ar(env, doneAction: 0)*in;

				decayTime = Rand(3.0,6.0);

				delayedSignal = AllpassC.ar(DelayL.ar(in2, 5, delayTime), 0.3, Rand(0.1,0.3), decayTime);

				out = Pan2.ar(delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime+decayTime));

				bigEnv = EnvGen.ar(Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1+decayTime,0.01], 'linear'), doneAction: 2);
				largeEnv = In.kr(largeEnvBus, 1);

				Out.ar(outBus, out*volume*largeEnv*bigEnv);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("StarDust",Rect(618, 645, 220, 270));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		dustGroup = Group.head(group);
		delayGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 3);

		largeEnvBus = Bus.control(group.server);
		volBus = Bus.control(group.server);

		volBus.set(0);

		synths = List.new;

		synths.add(Synth("starDust_mod", [\inBus, mixerToSynthBus.index, \transferBus, transferBus.index, \largeEnvBus, largeEnvBus.index], dustGroup));

		localRout = Routine.new({{
			delayTime = delayVar.rand+(delayVar/3);
			length = 2.0.rand + 2;

			xStart = 1.0.rand2;
			xEnd = 1.0.rand2;
			switch(numChannels,
				2,{Synth("starDustDelays_mod", [\transferBus, transferBus.index+3.rand, \outBus, outBus, \largeEnvBus, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd,\delayTime, delayTime, \length, length, \volBus, volBus.index], delayGroup);},
				4,{Synth("starDustDelays_mod", [\transferBus, transferBus.index+3.rand, \outBus, outBus.index+[0,2].choose, \largeEnvBus, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd,\delayTime, delayTime, \length, length, \volBus, volBus.index], delayGroup);},
				8,{Synth("starDustDelays_mod", [\transferBus, transferBus.index+3.rand, \outBus, outBus.index+[0,2,4,6].choose, \largeEnvBus, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd,\delayTime, delayTime, \length, length, \volBus, volBus.index], delayGroup);}
			);
			(0.3 + (0.35.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "inVol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\inVol, v.value);
			}, 1, true, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(80, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(80, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(150, 10, 60, 100), "delay", ControlSpec(0.5,3,'linear'),
			{|v|
				delayVar = v.value;
			}, 2.0, true));
		this.addAssignButton(2,\continuous, Rect(150, 110, 60, 20));

		controls.add(Button(win, Rect(150, 140, 60, 60))
			.states_([["off", Color.red, Color.black],["ON", Color.green, Color.black]])
			.action_{arg butt;
				if(butt.value == 1, {
					synths[0].set(\muteGate, 1);
				},{
					synths[0].set(\muteGate, 0);
				});
			}
		);
		this.addAssignButton(3,\onOff, Rect(150, 200, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 255, 60, 20))
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
		synths[0].set(\pauseGate, 0);
	}

	unpause {
		synths[0].set(\pauseGate, 1);
	}

	killMeSpecial {
		synths[0].set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
		volBus.free;
		transferBus.free;
	}
}