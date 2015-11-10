MantaBuffers_Mod : Module_Mod {
	var buffer, localRout, startPos, duration, vol, playRate, synthOutBus, buffer, dur, fade, trigRate, centerPos, center, width, yRange, recordGroup, playGroup, volBus;

	*initClass {
		StartUp.add {
			SynthDef("mantaBuffersRecord_mod", {arg inBus, bufnum, smallGate0 = 0, smallGate = 0, gate=1, pauseGate=1, recordOn = 1;
				var in, phasor, env, smallEnv, pauseEnv, internalSmallGate;

				internalSmallGate = 1-((smallGate+smallGate0).clip(0,1));

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*internalSmallGate, 0, BufFrames.kr(bufnum))*recordOn;

				in = In.ar(inBus);

				smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), internalSmallGate);
				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in*smallEnv, bufnum, phasor, loop:1);
			}).writeDefFile;

			SynthDef("mantaBuffersPlay_mod", {arg bufnum, outBus, trigRates = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ], durations = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ], centerPos = [ 0.01, 0.03, 0.05, 0.07, 0.09, 0.11, 0.13, 0.15, 0.17, 0.19, 0.21, 0.23, 0.25, 0.27, 0.29, 0.31, 0.33, 0.35, 0.37, 0.39, 0.41, 0.43, 0.45, 0.47, 0.49, 0.51, 0.53, 0.55, 0.57, 0.59, 0.61, 0.63, 0.65, 0.67, 0.69, 0.71, 0.73, 0.75, 0.77, 0.79, 0.81, 0.83, 0.85, 0.87, 0.89, 0.91, 0.93, 0.95 ], vol=0, pauseGate = 1, gate = 1;

				var env, pauseEnv, impulse, out, pan, fade, envs, tGrains;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				impulse = Impulse.kr(trigRates);

				pan = TRand.kr(0.7, 1.0, impulse)*TChoose.kr(impulse, [-1,1]);

				tGrains = TGrains2.ar(2, impulse, bufnum, 1, centerPos, durations, pan, 0.1, 0.05, 0.05);

				out = Mix(tGrains);

				Out.ar(outBus, out*env*pauseEnv*vol);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LoopMachineOverLap", Rect(707, 393, 709, 217));
		this.initControlsAndSynths(98);

		this.makeMixerToSynthBus(1);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*10, 1);

		synths = List.newClear(2);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		synths.put(0, Synth("mantaBuffersRecord_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum], recordGroup));

		yRange = 1;

		synths.put(1, Synth("mantaBuffersPlay_mod", [\bufnum, buffer.bufnum, \outBus, outBus, \playRate, playRate, \numGrains, 1, \startPos, startPos, \dur, dur, \vol, 1], playGroup));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				vol = v.value;
				synths[1].set(\vol, v.value);

		}, 0, true, \horz));
		this.addAssignButton(1,\continuous);

		/*		controls.add(QtEZSlider.new("playRate", ControlSpec(-4,4,'linear'),
		{|v|
		playRate = v.value;
		if(playRate ==0, {playRate = 0.05});
		if(playRate.abs<0.05, {playRate = 0.05*(playRate.sign)});
		synths[1].set(\playRate, playRate);
		}, 1, true, \horz));
		this.addAssignButton(4,\continuous);*/

		controls.add(Button()
			.states_([["rec", Color.red, Color.black],["lock", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[0].set(\recordOn, 0);
					},{
						synths[0].set(\recordOn, 1);
				})
		});
		this.addAssignButton(0,\onOff, Rect(65, 5, 16, 16));

		controls.add(Button.new()
			.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
			.action_{|v|
				if(v.value==1,{
					this.setManta;
					},{
						this.clearMidiOsc;
				})
		});

		win.layout_(
			VLayout(
				HLayout(controls[0].layout,assignButtons[0].layout),
				HLayout(controls[1],assignButtons[1].layout, controls[2])
			)
		);
	}

	setManta {
		var counter=0;

		(1..48).do{arg key, i;
			oscMsgs.put(i, "/manta/noteOn/"++key.asString);
			oscMsgs.put(i+48, "/manta/noteOff/"++key.asString);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[i],
				{
					trigRates.put(i, rrand(6, 15));
					durations.put(1/max(5, trigRates[i]));
					synth[1].set(\trigRate, trigRates, \durations, durations)
				},
				setups);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[i+48],
				{
					"noteOff".postln;
					trigRates.put(i, 0);
					synth[1].set(\trigRate, trigRates)
				},
				setups);
			counter=counter+1;
		};

	}


}
