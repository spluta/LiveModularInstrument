BrynHarrison_Mod : Module_Mod {
	var phaseBusses, volBus, recordGroup, playGroup, buffers, lengthRange, length, dur, bufferSeq, currentSynth, nextBuffer, recSynths;

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
			SynthDef("brynPlay_mod", {arg outBus, phaseBus, bufnum, loopDur, dur, vol=0, gate=1, pauseGate=1;
				var sig1, sig2, env1, env2, trig, trig1, trig2, phaseStart, env, smallEnv, pauseEnv;

				phaseStart = Latch.kr(In.kr(phaseBus), Line.kr(-0.1, 1, 0.01))-(loopDur*BufSampleRate.kr(bufnum));

				//phaseStart.poll;

				trig  = DelayC.kr(Impulse.kr(2/(loopDur-0.5)), 0.01);

				trig1 = PulseDivider.kr(trig, 2, 0);
				trig2 = PulseDivider.kr(trig, 2, 1);

				env1 = EnvGen.ar(Env([0,1,1,0], [0.1, loopDur-0.1, 0.1]), trig1);
				env2 = EnvGen.ar(Env([0,1,1,0], [0.1, loopDur-0.1, 0.1]), trig2).poll;

				sig1 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig1, phaseStart, loop: 1)*env1;
				sig2 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig2, phaseStart, loop: 1)*env2;

				//env = EnvGen.kr(Env.asr(0,1,0.01), gate, doneAction: 2);

				env = EnvGen.kr(Env([0.01,1,0.01], [dur, 0.01], \exp), gate, doneAction: 2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, (sig1+sig2)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BrynHarrison", Rect(600, 600, 235, 90));
		this.initControlsAndSynths(6);

		this.makeMixerToSynthBus(2);

		bufferSeq = Pseq(#[0,1,2,3], inf).asStream;

		buffers = List.new;
		4.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate*16, 2))};

		phaseBusses = List.new;
		4.do{phaseBusses.add(Bus.control(group.server))};
		volBus = Bus.control(group.server);

		synths = List.newClear(4);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		nextBuffer = bufferSeq.next;

		recSynths = List.newClear(0);

		4.do{|i|
			recSynths.add(Synth("brynRec_mod", [\inBus, mixerToSynthBus.index, \phaseBus, phaseBusses[i].index, \bufnum, buffers[i].bufnum], recordGroup));
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
					dur = rrand(1.5,3.0);
					Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum, \loopDur, length, \volBus, volBus.index, \dur, dur], playGroup);
				}
			);

			this.addAssignButton(2, \onOff);

		controls.add(Button()
				.states_([["Go", Color.black, Color.red],["Go", Color.black, Color.red]])
				.action_{
					"go same".postln;
					//length = rrand(lengthRange[0],lengthRange[1]);
					//dur = rrand(2.0,4.0);
					Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum, \loopDur, length, \volBus, volBus.index, \dur, dur], playGroup);
				}
			);

			this.addAssignButton(3, \onOff);

			/*controls.add(QtEZSlider("go", ControlSpec(0.01,2.0,\exp),
				{|v|
					synths[i].set(\vol, v.value);
			}, 0, true, \vert)
			.zAction_{|val|
				if(val.value==1, {
					"zOn".postln;
					length = rrand(lengthRange[0],lengthRange[1]);
					//synths.put(i, Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum, \loopDur, length, \volBus, volBus.index, \dur, rrand(1.0,3.0)], playGroup));

					Synth("brynPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum, \loopDur, length, \volBus, volBus.index, \dur, rrand(2.0,4.0)], playGroup);

					nextBuffer = bufferSeq.next;
					nil
				},{
					"zOff".postln;
					//synths[i].set(\gate, 0);
				});
			});*/


		win.layout_(
			VLayout(
				VLayout(
					HLayout(controls[0].layout,assignButtons[0].layout),
					HLayout(controls[1].layout,assignButtons[1].layout)
				),
				HLayout(
					VLayout(controls[2],assignButtons[2].layout),
					VLayout(controls[3],assignButtons[3].layout)
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
