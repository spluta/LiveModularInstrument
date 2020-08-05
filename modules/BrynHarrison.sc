BrynHarrison_Mod : Module_Mod {
	var phaseBusses, volBus, recordGroup, playGroup, buffers, lengthRange, length, dur, bufferSeq, currentSynth, nextBuffer, recSynths, lastPhase, numLoops;

	*initClass {
		StartUp.add {
			SynthDef("brynRec_mod", {arg inBus, phaseBus, bufnum, t_trig=0, gate=1, pauseGate=1;
				var in, phasor, phaseStart, env, pauseEnv, resetTrig;

				//resetTrig = Decay2.kr(t_trig, 0.001);

				in = In.ar(inBus,2);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				env = EnvGen.kr(Env.asr(0, 1, 5), gate, doneAction:2);

				Out.kr(phaseBus, phasor);

				//Out.ar(outBus, in);

				BufWr.ar(in, bufnum, phasor, loop:1);
			}).writeDefFile;
			SynthDef("brynPlay_mod", {arg outBus, phaseBus, lastPhase, whichPhase, bufnum, loopDur, dur, vol=0, gate=1, pauseGate=1;
				var sig1, sig2, env1, env2, trig, trig1, trig2, phaseStart, env, smallEnv, pauseEnv;

				phaseStart = Select.kr(whichPhase, [Latch.kr(In.kr(phaseBus), Line.kr(-0.1, 1, 0.01))-(loopDur*BufSampleRate.kr(bufnum)), lastPhase]);
				//phaseStart.poll;

				SendReply.kr(Line.kr(-0.3, 1, 0.02), '/phaseStart', [phaseStart]);

				trig  = DelayC.kr(Impulse.kr(2/(loopDur)), 0.01);

				//TGrains2.ar(

				trig1 = PulseDivider.kr(trig, 2, 0);
				trig2 = PulseDivider.kr(trig, 2, 1);

				//env1 = EnvGen.ar(Env([0,1,1,0], [0.1, loopDur-0.1, 0.1]), trig1);
				//env2 = EnvGen.ar(Env([0,1,1,0], [0.1, loopDur-0.1, 0.1]), trig2);

				env1 = Lag.kr(Trig1.kr(trig1, loopDur-0.2));
				env2 = Lag.kr(Trig1.kr(trig2, loopDur-0.2));

				sig1 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig1, phaseStart)*env1;
				sig2 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig2, phaseStart)*env2;

				//env = EnvGen.kr(Env.asr(0,1,0.01), gate, doneAction: 2);

				env = EnvGen.kr(Env([0.01,1,0.01], [dur, 0.01], \exp), gate, doneAction: 2)**2;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, (sig1+sig2)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BrynHarrison", Rect(600, 600, 235, 90));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus(2);

		bufferSeq = Pseq(#[0,1,2,3], inf).asStream;

		buffers = List.new;
		1.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate*120, 2))};

		phaseBusses = List.new;
		1.do{phaseBusses.add(Bus.control(group.server))};
		volBus = Bus.control(group.server);

		synths = List.newClear(4);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		//nextBuffer = bufferSeq.next;

		recSynths = List.newClear(0);

		OSCFunc({arg msg; msg.postln; lastPhase = msg[3]}, '/phaseStart');

		1.do{|i|
			recSynths.add(Synth("brynRec_mod", [\inBus, mixerToSynthBus.index, \phaseBus, phaseBusses[0].index, \bufnum, buffers[0].bufnum], recordGroup));
		};

		lengthRange = List[1,1];

		controls.add(QtEZSlider("len1", ControlSpec(0.05, 2),
			{arg val;
				lengthRange.put(0, val.value);
		}, 0.5, true, orientation:\horz));
		this.addAssignButton(0, \continuous);

		controls.add(QtEZSlider("len2", ControlSpec(0.05, 2),
			{arg val;
				lengthRange.put(1, val.value);
		}, 1, true, orientation:\horz));
		this.addAssignButton(1, \continuous);

			controls.add(Button()
				.states_([["Go", Color.black, Color.red],["Go", Color.black, Color.red]])
				.action_{
					"go".postln;
					length = rrand(lengthRange[0],lengthRange[1]);
				dur = length*(rrand(numLoops,numLoops+1).round);

					Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[0].index, \lastPhase, lastPhase, \whichPhase, 0, \bufnum, buffers[0].bufnum, \loopDur, length, \volBus, volBus.index, \dur, dur], playGroup);
				}
			);

			this.addAssignButton(2, \onOff);

		controls.add(Button()
				.states_([["Go", Color.black, Color.red],["Go", Color.black, Color.red]])
				.action_{
					"go same".postln;
					//length = rrand(lengthRange[0],lengthRange[1]);
					//dur = rrand(2.0,4.0);
					Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[0].index, \lastPhase, lastPhase, \whichPhase, 1, \bufnum, buffers[0].bufnum, \loopDur, length, \volBus, volBus.index, \dur, dur], playGroup);
				}
			);
			this.addAssignButton(3, \onOff);

		controls.add(QtEZSlider("dur", ControlSpec(3, 7),
			{arg val;
				numLoops = val.value;
		}, 4, true, orientation:\horz));
		this.addAssignButton(1, \continuous);

		win.layout_(
			VLayout(
				VLayout(
					HLayout(controls[0],assignButtons[0]),
					HLayout(controls[1],assignButtons[1]),
					HLayout(controls[4])
				),
				HLayout(
					VLayout(controls[2],assignButtons[2]),
					VLayout(controls[3],assignButtons[3])
				)
			)
		);

	}

	killMeSpecial {
		phaseBusses.do{|item| item.free};
		volBus.free;
		buffers.do{|item| item.free};
	}
}
