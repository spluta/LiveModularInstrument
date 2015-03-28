
DrumBombs_Mod : Module_Mod {
	var oscNode, outVol, buffers, bufSeq, trigDiv, counter, muteGate, divRange;

	*initClass {
		StartUp.add {
			SynthDef(\drumBombsAnal_mod, { arg inBus, inVol=0, thresh = 0.1, trigDiv, trigDivLow = 10, trigDivHigh = 10, muteGate = 1, gate = 1, pauseGate = 1;
				var sound, verb, trig, trig1, env, pauseEnv, shifted0, shifted1, shifted, amp, trigEnv, chain;

				sound = In.ar(inBus)*EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0)*inVol;

				amp = Amplitude.kr(sound);

				chain = FFT(LocalBuf(1024), sound);

				trig = Onsets.kr(chain, thresh, \rcomplex);

				//trig1 = Decay2.kr(PulseDivider.kr(trig, trigDiv, 0), 0.05, 0.2);
				//trigDiv = TRand.kr(trigDivLow, trigDivHigh, trig1);

				SendTrig.kr(trig-0.1, 0, amp);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);


			}).writeDefFile;

			SynthDef(\drumBombsPlay_mod, { arg buffer, outBus, outVol=0, gate = 1, pauseGate = 1;
				var sig, env, pauseEnv;

				sig = PlayBuf.ar(2, buffer, 1);
				sig = Rotate2.ar(sig[0],sig[1], Rand(-1, 1));

				env = EnvGen.kr(Env([0,1,1,0],[0.02,BufDur.kr(buffer),0.02]), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, sig*env*pauseEnv*outVol);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("DrumBombs", Rect(500, 500, 255, 260));
		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus;

		this.loadBuffers;

		synths.add(Synth("drumBombsAnal_mod", [\inBus, mixerToSynthBus.index], group));

		controls.add(EZSlider(win, Rect(5, 0, 30, 220),"in", ControlSpec(0.0,2.0,\amp),
			{|v|
				synths[0].set(\inVol, v.value);
			}, 1.0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0,\continuous, Rect(5, 220, 30, 16));

		controls.add(EZSlider(win, Rect(35, 0, 30, 220),"out", ControlSpec(0.0,2.0,\amp),
			{|v|
				outVol = v.value;
			}, 1.0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(1,\continuous, Rect(35, 220, 30, 16));


		controls.add(EZSlider(win, Rect(65, 0, 60, 220),"thresh", ControlSpec(0.01,3),
			{|v|
				synths[0].set(\thresh, v.value);
			}, 0.1, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(2,\continuous, Rect(65, 220, 60, 16));

		controls.add(EZRanger(win, Rect(125, 0, 60, 220),"trigDiv", ControlSpec(1,30,\lin,1),
			{|v|
				//synths[0].set(\trigDiv, v.value);
				divRange = [v.value[0], v.value[1]];
				trigDiv = rrand(divRange[0], divRange[1]);
			}, [5,15], true, 40, 40, 0, 16, \vert));
		this.addAssignButton(3,\continuous, Rect(125, 220, 60, 16));

		muteGate = 0;

		controls.add(Button(win, Rect(5, 240, 180, 16))
			.states_([["mute", Color.green, Color.black],["play", Color.red, Color.black]])
			.action_{arg butt;
				muteGate = butt.value;
			});
		this.addAssignButton(4,\onOff, Rect(5, 260, 180, 16));

		controls.add(Button(win, Rect(190, 0, 60, 60))
			.states_([["force", Color.black, Color.green],["force", Color.green, Color.black]])
			.action_{arg butt;
				this.makeNote;
			});
		this.addAssignButton(5,\onOff, Rect(190, 60, 60, 16));

		controls.add(Button(win, Rect(190, 80, 60, 60))
			.states_([["hit", Color.black, Color.blue],["hit", Color.blue, Color.black]]));
		this.addAssignButton(6,\onOff, Rect(190, 140, 60, 16));

		counter = 0;
		oscNode = OSCresponderNode(nil, '\tr', {|t, r, msg|
				this.makeNote;
			}).add;

		oscNode.value;
	}
	makeNote {
		"hit!".postln;
		{controls[6].value_(controls[6].value+1)}.defer;
		counter = counter+1;
		if((counter>=trigDiv)&&(muteGate == 1),{
			"bomb".postln;
			Synth("drumBombsPlay_mod", [\buffer, bufSeq.next.bufnum, \outBus, outBus, \outVol, outVol], group);
			counter = 0;
			trigDiv = rrand(divRange[0], divRange[1]);
		});
	}

	loadBuffers {
		var path, dirName, paths2;

		path = "/Users/sam/Library/Application Support/SuperCollider/Extensions/liveInterface/sounds/drumBombs/*";

		paths2 = path.pathMatch.select({|file| file.contains(".aiff") });
		paths2.addAll(path.pathMatch.select({|file| file.contains(".aif") }));
		paths2.addAll(path.pathMatch.select({|file| file.contains(".wav") }));

		paths2.postln;

		buffers = List.newClear(paths2.size);

		paths2.postln;

		paths2.do({ arg path, i;
			var shortPath;

			shortPath = path.split.pop;

			buffers.put(i, Buffer.read(group.server, path));
		});
		bufSeq = Pxrand(buffers, inf).asStream;
	}

	killMeSpecial {
		oscNode.remove;
	}

	pause {
		group.set(\pauseGate, 0);
	}

	unpause {
		group.set(\pauseGate, 1);
	}
}