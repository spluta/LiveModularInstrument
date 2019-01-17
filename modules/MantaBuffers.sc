MantaBuffers_Mod : Module_Mod {
	var buffer, localRout, startPos, duration, vol, playRate, synthOutBus, buffer, dur, fade, trigRate, centerPos, center, width, yRange, recordGroup, playGroup, volBus, trigSpec, temp, zTrigs;

	var trigRates, durations;

	*initClass {
		StartUp.add {
			SynthDef("mantaBuffersRecord_mod", {arg inBus, bufnum, gate=1, pauseGate=1, recordOn = 1;
				var in, phasor, env, smallEnv, pauseEnv;

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*recordOn, 0, BufFrames.kr(bufnum));

				in = In.ar(inBus);

				//smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), recordOn);
				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in, bufnum, phasor, loop:1);
			}).writeDefFile;

			SynthDef("mantaBuffersPlay_mod", {arg bufnum, outBus, trigRates = #[ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ], durations = #[ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ], zTrigs = #[ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ], centerPos = #[ 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7, 1.9, 2.1, 2.3, 2.5, 2.7, 2.9, 3.1, 3.3, 3.5, 3.7, 3.9, 4.1, 4.3, 4.5, 4.7, 4.9, 5.1, 5.3, 5.5, 5.7, 5.9, 6.1, 6.3, 6.5, 6.7, 6.9, 7.1, 7.3, 7.5, 7.7, 7.9, 8.1, 8.3, 8.5, 8.7, 8.9, 9.1, 9.3, 9.5 ], vol=0, pauseGate = 1, gate = 1;

				var env, pauseEnv, impulse, out, pan, fade, envs, tGrains;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				impulse = Impulse.kr(trigRates*zTrigs);

				pan = TRand.kr(0.7, 1.0, impulse)*TChoose.kr(impulse, [-1,1]);

				//centerPos = centerPos + (LFNoise2.kr(LFNoise0.kr(0.5, 0.05, 0.11), 0.07) ! 48);

				tGrains = TGrains2.ar(2, impulse, bufnum, 1, centerPos, durations, pan, 2, 0.05, 0.05);

				out = Mix(tGrains);

				Out.ar(outBus, out*env*pauseEnv*vol);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("MantaBuffers", Rect(707, 393, 200, 60));
		this.initControlsAndSynths(50);

		this.makeMixerToSynthBus(1);

		dontLoadControls = [1];

		buffer = Buffer.alloc(group.server, group.server.sampleRate*10, 1);

		synths = List.newClear(2);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		trigSpec = ControlSpec(15, 120, \exp);

		trigRates = Array.fill(48,{0});
		zTrigs = Array.fill(48,{0});
		durations = Array.fill(48,{0});

		synths.put(0, Synth("mantaBuffersRecord_mod", [\inBus, mixerToSynthBus.index, \bufnum, buffer.bufnum], recordGroup));

		yRange = 1;

		synths.put(1, Synth("mantaBuffersPlay_mod", [\bufnum, buffer.bufnum, \outBus, outBus, \vol, 1], playGroup));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				vol = v.value;
				synths[1].set(\vol, v.value);

		}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(Button()
			.states_([["rec", Color.red, Color.black],["lock", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[0].set(\recordOn, 0);
					},{
						synths[0].set(\recordOn, 1);
				})
		});
		this.addAssignButton(1,\onOff, Rect(65, 5, 16, 16));

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
		var counter=0, key2;

		48.do{arg key;
			//oscMsgs.put(key+2, "/manta/pad/"++key.asString);

			key2 = "/MultiBall"++(100+key).asString;
			oscMsgs.put(key+2, key2);

			MidiOscControl.setControllerNoGui(group.server, oscMsgs[key+2],
				{arg xyz, val;
					switch(xyz.asSymbol,
						'y', {
							temp = trigSpec.map(val);
							if(temp<16, {temp = 0});
							trigRates.put(key, temp);
							durations.put(key, 1/max(5, trigRates[key]/4));
							trigRates;
							durations;
							synths[1].set(\trigRates, trigRates, \durations, durations)
						},
						'z', {
							zTrigs.put(key, val);
							synths[1].set(\zTrigs, zTrigs);
						}
					)
				});
			counter=counter+1;
		};

	}

	clearManta {
		(2..50).do{arg num;
			MidiOscControl.clearController(group.server, oscMsgs[num]); //send a message to clear the OSC data from the MidiOscControl
			oscMsgs.put(num, nil);
		}
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);  //controller messages

		saveArray.add(oscMsgs.copyRange(0,0));

		saveArray.add(win.bounds);

		this.saveExtra(saveArray);
		^saveArray
	}

}
