InterruptLoop_Mod : Module_Mod {
	var phasorBus, recordGroup, playGroup, buffer, phaseLength, phaseStart, lengthRange, synthStream, currentSynth;

	*initClass {
		StartUp.add {

			SynthDef("inturruptLoopRecord_mod", {arg inBus, outBus, bufnum, phasorBus, smallGate = 1, gate=1, pauseGate=1;
				var in, phasor, vol, env, smallEnv, pauseEnv;

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*smallGate, 0, BufFrames.kr(bufnum));
				Out.kr(phasorBus, A2K.kr(phasor));

				in = In.ar(inBus,8);

				smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), smallGate);
				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in, bufnum, phasor, loop:1);

				Out.ar(outBus, in*env*smallEnv*pauseEnv);
			}).writeDefFile;
			SynthDef("inturruptLoopPlay_mod", {arg outBus, bufnum, phaseStart, phaseLength, gate=1, pauseGate=1;
				var playBack, phase, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				phase = (Phasor.ar(0, BufRateScale.kr(bufnum), 0, phaseLength)+phaseStart).wrap(0, BufFrames.kr(bufnum));

				playBack = BufRd.ar(8, bufnum, phase, loop:1)*env*pauseEnv;

				XOut.ar(outBus, env, playBack);
			}).writeDefFile;

			SynthDef("inturruptLoopPlayJumpy_mod", {arg outBus, bufnum, phaseStart, phaseLength, gate=1, pauseGate=1;
				var playBack, phase, env, pauseEnv, phaseStart2;

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				//phaseStart2 = TRand.kr(phaseStart-phaseLength, phaseStart, Impulse.kr(1/(phaseLength/BufFrames.kr(bufnum))));
				phaseStart2 = TRand.kr(phaseStart-(BufFrames.kr(bufnum)/8), phaseStart, Impulse.kr((1/(phaseLength/(BufFrames.kr(bufnum)/8)))/(Rand(3, 7).floor)));

				phase = (Phasor.ar(0, BufRateScale.kr(bufnum), 0, phaseLength)+phaseStart2).wrap(0, BufFrames.kr(bufnum));

				playBack = BufRd.ar(8, bufnum, phase, loop:1)*env*pauseEnv;

				XOut.ar(outBus, env, playBack);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("InterruptLoop", Rect(700, 700, 290, 75));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(8);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*8, 8);

		phasorBus = Bus.control(group.server);

		synths = List.newClear(3);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		synths.put(0, Synth("inturruptLoopRecord_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \phasorBus, phasorBus.index], recordGroup));

		synthStream = Pseq([1, 2], inf).asStream;
		currentSynth = synthStream.next;

		controls.add(Button(win, Rect(5, 5, 260, 16))
			.states_([["input", Color.red, Color.black],["loop", Color.black, Color.green]])
			.action_{arg butt;
				if(butt.value==0,{
					controls[1].value_(0);
					synths[1].set(\gate, 0);
					synths[2].set(\gate, 0);
					synths[0].set(\smallGate, 1);
				},{
					synths[currentSynth].set(\gate, 0);
					phasorBus.get({arg val;
						val.postln;
						phaseLength = rrand(lengthRange[0], lengthRange[1])*(buffer.numFrames/8);
						phaseStart = (val-phaseLength).wrap(0, buffer.numFrames);

						currentSynth = synthStream.next;

						synths.put(currentSynth, Synth("inturruptLoopPlay_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \phaseStart, phaseStart, \phaseLength, phaseLength], playGroup));
						synths[0].set(\smallGate, 0);
					});
				});
			});
		this.addAssignButton(0, \onOff, Rect(265, 5, 20, 16));

		controls.add(Button(win, Rect(5, 25, 260, 16))
			.states_([["input", Color.red, Color.black],["jumpy", Color.black, Color.green]])
			.action_{arg butt;
				if(butt.value==0,{
					controls[0].value_(0);
					synths[1].set(\gate, 0);
					synths[2].set(\gate, 0);
					synths[0].set(\smallGate, 1);
				},{
					synths[currentSynth].set(\gate, 0);
					phasorBus.get({arg val;
						val.postln;
						phaseLength = rrand(lengthRange[0], lengthRange[1])*(buffer.numFrames/8);
						phaseStart = (val-phaseLength).wrap(0, buffer.numFrames);

						currentSynth = synthStream.next;

						synths.put(currentSynth, Synth("inturruptLoopPlayJumpy_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \phaseStart, phaseStart, \phaseLength, phaseLength], playGroup));
						synths[0].set(\smallGate, 0);
					});
				});
			});
		this.addAssignButton(1, \onOff, Rect(265, 25, 20, 16));

		controls.add(EZRanger(win, Rect(5, 50, 260, 20), "time", ControlSpec(0.01, 0.1),
			{arg vals;
				lengthRange = vals.value;
			}, [0.5,1], true, layout:\horz));
	}


	killMeSpecial {
		phasorBus.free;
	}
}