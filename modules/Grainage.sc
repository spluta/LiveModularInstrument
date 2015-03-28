GrainAge_Mod : Module_Mod {
	var volBus, recordGroup, playGroup, buffer;

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

			SynthDef("grainage2_mod", {arg outBus, bufnum, select, volBus, smallGate=0, gate = 1, pauseGate=1;
				var trate, dur, out, env, smallEnv, vol, length, place, pan, pauseEnv;

				length = BufDur.kr(bufnum);

				trate = MouseY.kr(2,200,1);
				dur = 4 / trate;

				place = Select.kr(select, [MouseX.kr(0,length), LFNoise0.kr(10).range(0, length)]);

				pan = Select.kr(select, [MouseX.kr(-1,1), LFNoise0.kr(10).range(-1, 1)]);

				out = TGrains.ar(2, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);

				vol = In.kr(volBus);

				smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, smallEnv*env*out*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("grainage4_mod", {arg outBus, bufnum, select, volBus, smallGate=0, gate = 1, pauseGate=1;
				var trate, dur, out, env, smallEnv, vol, length, place, pan, pauseEnv;

				length = BufDur.kr(bufnum);

				trate = MouseY.kr(2,200,1);
				dur = 4 / trate;

				place = Select.kr(select, [MouseX.kr(0,length), LFNoise0.kr(10).range(0, length)]);

				pan = Select.kr(select, [MouseX.kr(-0.75,0.75), LFNoise0.kr(10).range(-1, 1)]);

				out = TGrains.ar(4, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);

				vol = In.kr(volBus);

				smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, [out[0],out[1],out[3],out[2]]*smallEnv*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("grainage8_mod", {arg outBus, bufnum, select, volBus, smallGate=0, gate = 1, pauseGate=1;
				var trate, dur, out, env, smallEnv, vol, length, place, pan, pauseEnv;

				length = BufDur.kr(bufnum);

				trate = MouseY.kr(2,200,1);
				dur = 4 / trate;

				place = Select.kr(select, [MouseX.kr(0,length), LFNoise0.kr(10).range(0, length)]);

				pan = Select.kr(select, [MouseX.kr(-0.5,0.5), LFNoise0.kr(10).range(-1, 1)]);

				out = TGrains.ar(8, Impulse.ar(trate), bufnum, 1, place, dur, pan, 1, 2);

				vol = In.kr(volBus);

				smallEnv = EnvGen.ar(Env.asr(0.001, 1, 0.001, 'linear'),smallGate);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1, 'linear'),gate,doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, [out[0],out[1],out[7],out[2],out[6],out[3],out[5],out[4]]*smallEnv*env*vol*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GrainAge", Rect(500, 500, 90, 250));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		buffer = Buffer.alloc(group.server, group.server.sampleRate*8, 1);

		volBus = Bus.control(group.server);

		synths = List.newClear(2);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		synths.put(0, Synth("grainageRecord_mod", [\inBus, mixerToSynthBus.index, \bufnum, buffer.bufnum, \run, 1], recordGroup));
		synths.put(1, Synth("grainage2_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0, \volBus, volBus.index], playGroup));

		controls.add(Button(win, Rect(5, 5, 60, 16))
			.states_([["rec", Color.red, Color.black],["lock", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[0].set(\run, 0);
				},{
					synths[0].set(\run, 1);
				})
			});
		this.addAssignButton(0,\onOff, Rect(65, 5, 16, 16));

		controls.add(Button(win, Rect(5, 25, 60, 16))
			.states_([["off", Color.red, Color.black],["on", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\smallGate, 1);
				},{
					synths[1].set(\smallGate, 0);
				})
			});
		this.addAssignButton(1,\onOff, Rect(65, 25, 16, 16));

		controls.add(Button(win, Rect(5, 45, 60, 16))
			.states_([["mouse", Color.red, Color.black],["rand", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\select, 1);
				},{
					synths[1].set(\select, 0);
				})
			});
		this.addAssignButton(2,0, Rect(65, 45, 16, 16));

		controls.add(EZSlider(win, Rect(5, 65, 80, 160),"vol", ControlSpec(0.0,1.0,\amp),
			{|v|
				volBus.set(v.value);
			}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(3,\continuous, Rect(5, 225, 80, 16));
		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 255, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage2_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0, \volBus, volBus.index], playGroup));
					},
					1, {
						numChannels = 4;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage4_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0, \volBus, volBus.index], playGroup));
					},
					2, {
						numChannels = 8;
						synths[1].set(\gate, 0);
						synths.put(1, Synth("grainage8_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \select, 0, \volBus, volBus.index], playGroup));
					}
				)
			};
		);
	}
}