AtdV_Mod : Module_Mod {
	var buffers, volBusses, synthGroup, throughGroup, noteOn, noteOff, onSynth;

	*initClass {
		StartUp.add {
			SynthDef("AtdVMuter_mod", {arg inBus, outBus, gate = 1, pauseGate = 1, muteGate = 1;
				var in, pauseEnv, env, muteEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate);

				in = In.ar(inBus, 8);

				Out.ar(outBus, in*env*pauseEnv*muteEnv);
			}).writeDefFile;

			SynthDef("AtdVPlayer_mod", {arg bufnum, outBus, volBus, gate = 1, pauseGate = 1, muteGate = 1;
				var out, out2, vol, pauseEnv, env, muteEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:1);

				vol = In.kr(volBus);

				//out = PlayBuf.ar(2, bufnum, startPos: Rand(44100, 44100*20), loop: 1)*0;

				out2 = PlayBuf.ar(2, bufnum, startPos: Rand(44100, 44100*20), loop: 1);

				ReplaceOut.ar(outBus, [0,0]);
				ReplaceOut.ar(outBus+2, out2*env*vol*pauseEnv*muteEnv);

				//Out.ar(0, out*env*vol*pauseEnv*muteEnv);

			}).writeDefFile;

		}

	}

	init {
		this.makeWindow("AtdV", Rect(600, 645, 330, 240));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus(8);

		synths = List.newClear(6);

		throughGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		volBusses = List.new;
		5.do{ volBusses.add(Bus.control(group.server))};

		buffers = List.new;
		buffers.add(Buffer.read(group.server, "/Users/sam/Documents/WetInk/scoresElectronics/ATD V/useTheseChords/bigChord3.aiff"));
		buffers.add(Buffer.read(group.server, "/Users/sam/Documents/WetInk/scoresElectronics/ATD V/useTheseChords/bigChord1.aiff"));
		buffers.add(Buffer.read(group.server, "/Users/sam/Documents/WetInk/scoresElectronics/ATD V/useTheseChords/bigChord4.aiff"));
		buffers.add(Buffer.read(group.server, "/Users/sam/Documents/WetInk/scoresElectronics/ATD V/useTheseChords/bigChord5.aiff"));
		buffers.add(Buffer.read(group.server, "/Users/sam/Documents/WetInk/scoresElectronics/ATD V/useTheseChords/bigChord2.aiff"));

		5.do{|i|
			controls.add(EZSlider.new(win,Rect(65*i, 0, 60, 200), "vol", ControlSpec(0,1,'amp'),
				{|v|
					volBusses[i].set(v.value);
				}, 0, layout:\vert));
			this.addAssignButton(i,\continuous, Rect(65*i, 200, 60, 20));
		};

		onSynth = nil;

		synths.put(5, Synth("AtdVMuter_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], throughGroup));

//		4.do{|i|
			noteOn = MIDIFunc.noteOn({ |value, num|
					switch(num,
						62, {
							synths.put(0, Synth("AtdVPlayer_mod", [\bufnum, buffers[0].bufnum, \outBus, outBus, \volBus, volBusses[0]], synthGroup));
						},
						64, {
							synths.put(1, Synth("AtdVPlayer_mod", [\bufnum, buffers[1].bufnum, \outBus, outBus, \volBus, volBusses[1]], synthGroup));
						},
						65, {
							synths.put(2, Synth("AtdVPlayer_mod", [\bufnum, buffers[2].bufnum, \outBus, outBus, \volBus, volBusses[2]], synthGroup));
						},
						69, {
							synths.put(3, Synth("AtdVPlayer_mod", [\bufnum, buffers[3].bufnum, \outBus, outBus, \volBus, volBusses[3]], synthGroup));
						},
						71, {
							synths.put(4, Synth("AtdVPlayer_mod", [\bufnum, buffers[4].bufnum, \outBus, outBus, \volBus, volBusses[4]], synthGroup));
						},
						72, {
							synths[5].set(\muteGate, 0)
						}
					)
				},
				(62..72)
			);

			noteOff = MIDIFunc.noteOff({ |value, num|
					switch(num,
						62, {
							synths[0].set(\gate, 0)
						},
						64, {
							synths[1].set(\gate, 0)
						},
						65, {
							synths[2].set(\gate, 0)
						},
						69, {
							synths[3].set(\gate, 0)
						},
						71, {
							synths[4].set(\gate, 0)
						},
						72, {
							synths[5].set(\muteGate, 1)
						}
					)
				},
				(62..72)
			);
	}
}