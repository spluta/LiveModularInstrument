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

			//not updated to use direct vol or 2DSlider

			//
			// SynthDef("grainage4_mod", {arg outBus, bufnum, select, volBus, smallGate=0, gate = 1, pauseGate=1;
			// 	var trate, dur, out, env, smallEnv, vol, length, place, pan, pauseEnv;
			//
			// 	length = BufDur.kr(bufnum);
			//
			// 	trate = MouseY.kr(2,200,1);
			// 	dur = 4 / trate;
			//
			// 	place = Select.kr(select, [MouseX.kr(0,length), LFNoise0.kr(10).range(0, length)]);
			//
			// 	pan = Select.kr(select, [MouseX.kr(-0.75,0.75), LFNoise0.kr(10).range(-1, 1)]);
			//
			// 	out = TGrains.ar(4, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);
			//
			// 	vol = In.kr(volBus);
			//
			// 	smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
			// 	env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
			// 	pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
			//
			// 	Out.ar(outBus, [out[0],out[1],out[3],out[2]]*smallEnv*env*vol*pauseEnv);
			// }).writeDefFile;
			//
			// SynthDef("grainage8_mod", {arg outBus, bufnum, select, volBus, smallGate=0, gate = 1, pauseGate=1;
			// 	var trate, dur, out, env, smallEnv, vol, length, place, pan, pauseEnv;
			//
			// 	length = BufDur.kr(bufnum);
			//
			// 	trate = MouseY.kr(2,200,1);
			// 	dur = 4 / trate;
			//
			// 	place = Select.kr(select, [MouseX.kr(0,length), LFNoise0.kr(10).range(0, length)]);
			//
			// 	pan = Select.kr(select, [MouseX.kr(-0.5,0.5), LFNoise0.kr(10).range(-1, 1)]);
			//
			// 	out = TGrains.ar(8, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);
			//
			// 	vol = In.kr(volBus);
			//
			// 	smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
			// 	env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
			// 	pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
			//
			// 	Out.ar(outBus, [out[0],out[1],out[7],out[2],out[6],out[3],out[5],out[4]]*smallEnv*env*vol*pauseEnv);
			// }).writeDefFile;
		}
	}

/*	load {}*/

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
			.states_([["mouse", Color.red, Color.black],["rand", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\select, 1);
					},{
						synths[1].set(\select, 0);
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

		//multichannel button

/*		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage2_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0], playGroup));
					},
					1, {
						numChannels = 4;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage4_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0], playGroup));
					},
					2, {
						numChannels = 8;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage8_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0], playGroup));
					}
				)
			};
		);*/


		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0], controls[1], controls[2]),
					HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout),
					controls[4].layout, assignButtons[4].layout
				),
				VLayout(controls[3].layout, assignButtons[3].layout)
			)
		);
		}


	}