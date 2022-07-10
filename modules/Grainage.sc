GrainAge_Mod : Module_Mod {
	var recordGroup, playGroup, buffer;

	*initClass {
		StartUp.add {
			SynthDef("grainageRecord_mod", {arg inBus, bufnum, run, gate=1, pauseGate=1;
				var in, phasor, vol, env, smallEnv, pauseEnv;

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				in = In.ar(inBus);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				RecordBuf.ar(in, bufnum, run:run, loop:1);
			}).writeDefFile;

			SynthDef("grainage2_mod", {arg outBus, bufnum, select, vol=1, trate=2, xPlace=0.5, smallGate=0, gate = 1, pauseGate=1;
				var dur, out, env, smallEnv, length, place, pan, pauseEnv;

				length = BufDur.kr(bufnum);

				dur = 4 / trate;

				place = Select.kr(select, [xPlace*length, LFNoise0.kr(10).range(0, length)]);

				pan = Select.kr(select, [xPlace*2-1, LFNoise0.kr(10).range(-1, 1)]);

				out = TGrains.ar(2, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);

				smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, smallEnv*env*out*vol*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GrainAge", Rect(500, 500, 250, 250));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus;

		buffer = Buffer.alloc(group.server, group.server.sampleRate*8, 1);

		synths = List.newClear(2);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		synths.put(0, Synth("grainageRecord_mod", [\inBus, mixerToSynthBus.index, \bufnum, buffer.bufnum, \run, 1], recordGroup));
		synths.put(1, Synth("grainage2_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0], playGroup));

		controls.add(Button()
			.states_([["rec", Color.red, Color.black],["lock", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[0].set(\run, 0);
					},{
						synths[0].set(\run, 1);
				})
		});
		this.addAssignButton(0,\onOff);

		controls.add(Button()
			.states_([["off", Color.red, Color.black],["on", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\smallGate, 1);
					},{
						synths[1].set(\smallGate, 0);
				})
		});
		this.addAssignButton(1,\onOff);

		controls.add(Button()
			.states_([["rand", Color.red, Color.black],["mouse", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\select, 0);
					},{
						synths[1].set(\select, 1);
				})
		});
		this.addAssignButton(2,\onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0.0,1.0,\amp),
			{|v|
				synths[1].set(\vol, v.value);
		}, 0, true, \vert));
		this.addAssignButton(3,\continuous);

		controls.add(QtEZSlider2D.new(ControlSpec(0,1), ControlSpec(200,2,\exp),
			{arg vals;

				synths[1].set(\trate, vals.value[1]);
				synths[1].set(\xPlace, vals.value[0]);
			}
		));
		this.addAssignButton(4,\slider2D);


		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0], controls[1], controls[2]),
					HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),
					controls[4], assignButtons[4]
				),
				VLayout(controls[3], assignButtons[3])
			)
		);
		}


	}