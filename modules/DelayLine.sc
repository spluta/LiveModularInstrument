DelayLine_Mod : Module_Mod {
	var delayBuf, recordGroup, playGroup, numFrameLists;
	var recordDelaySynth, tapSynth, loopRecordSynth, loopPlaySynths, phasorBus;
	var loopBufs, currentCue, buttons, oscDef, currentSynth, lastPedalTime, text;

	*initClass {
		StartUp.add {
			SynthDef("delayLine_BufWr_mod",{arg inBus, buffer, phasorBus;
				var phasor;

				phasor = Phasor.ar(0, BufRateScale.kr(buffer), 0, BufFrames.kr(buffer));

				BufWr.ar(In.ar(inBus), buffer, phasor);
				Out.ar(phasorBus, phasor);

			}).writeDefFile;

			SynthDef("delayLine_playDelay_mod",{arg outBus, buffer, phasorBus, delayTime, fadeIn, fadeOut, gate = 1;
				var sound, phase, env;

				phase = Wrap.ar((In.ar(phasorBus)-(delayTime*BufSampleRate.kr(buffer))), 0, BufFrames.kr(buffer));

				sound = BufRd.ar(1, buffer, phase);
				env = EnvGen.ar(Env.asr(fadeIn, 1, fadeOut, \sine), gate, doneAction:2);
				Out.ar(outBus, sound*env);

			}).writeDefFile;

			SynthDef("grabNLoop_Record_mod", {arg inBus, buffer, bufDur;
				var in, env;

				in = In.ar(inBus);

				env = EnvGen.kr(Env.new([0,1,1,0], [0.01, bufDur, 0.01]), 1, doneAction:2);
				RecordBuf.ar(in, buffer, 0, 1, 0, 1, 0, 1);

			}).writeDefFile;

			SynthDef("grabNLoop_Play_mod", {arg outBus, pan, buffer, bufDur, attack, decay, gate=1;
				var sound, playSpeed, delaySound, trigger, counter, toggle, envs, bigEnv;

				trigger = Impulse.kr(1/((bufDur-LFNoise2.kr(0.5).range(bufDur/10, bufDur/7))/3));

				counter = Stepper.kr(trigger, 0, 0, 2, 1);

				toggle = Select.kr(counter, [[1,0,0],[0,1,0],[0,0,1]]);

				envs = EnvGen.kr(Env.asr(bufDur/3, 1, bufDur/3, 'welch'), toggle);

				sound = PlayBuf.ar(1, buffer, 1, toggle, TRand.kr(0, 44000, toggle), 0)*envs;

				bigEnv = EnvGen.kr(Env.asr(attack, 1, decay+2), gate, doneAction:2);

				Out.ar(outBus, Splay.ar(sound, 0.33, 0.5, pan)*bigEnv);
			}).writeDefFile;
		}
	}

	init {
		this.initControlsAndSynths(1);
		this.makeMixerToSynthBus(1);

		dontLoadControls = [0];

		delayBuf = Buffer.alloc(group.server, group.server.sampleRate*16, 1);

		loopBufs = List.fill(3, {Buffer.alloc(group.server, group.server.sampleRate*10, 1)});

		phasorBus = Bus.audio(group.server);

		recordGroup = Group.tail(group);
		playGroup = Group.tail(group);

		recordDelaySynth = Synth("delayLine_BufWr_mod", [\inBus, mixerToSynthBus, \buffer, delayBuf, \phasorBus, phasorBus], recordGroup);

		loopPlaySynths = List.newClear(3);

		buttons = List.fill(35, {|i|
			Button().states_([
				[i, Color.black, Color.rand],
				[i, Color.black, Color.rand]
			])
			.action_({this.setCue(i)})
		}).clump(8);

		text = StaticText().font_(Font("Helvetica", 102));

		controls.add(
			Button().states_([
				["Next", Color.black, Color.rand],
				["Next", Color.black, Color.rand]
			])
			.action_({this.trigger})
		);
		this.addAssignButton(0, \onOff);

		this.makeWindow("DelayLine");

		win.layout_(VLayout(
			text,
			HLayout(controls[0],assignButtons[0]),
			*buttons.collect{|x| HLayout(*x)}
		));

		//window.onClose = {Server.quitAll};

		lastPedalTime = Main.elapsedTime;

		win.view.keyDownAction = {|doc, char, mod, unicode, keycode, key|
			var lastPedalDur;
			lastPedalDur = Main.elapsedTime-lastPedalTime;
			if((key==16777239)&&(lastPedalDur>1), {
				this.trigger;
				lastPedalTime = Main.elapsedTime;
			})
		};

		currentCue = 0;
		this.setCue(currentCue);
}

setCue {arg cueNum;
	currentCue = cueNum-1;
	currentSynth.set(\gate, 0);
	loopPlaySynths.do{arg item; item.set(\gate, 0)};
	this.trigger;
}

trigger {
	currentCue = currentCue+1;
	text.string = currentCue;
	switch(currentCue,
		1, {
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 12, \fadeIn, 8, \fadeOut, 4], playGroup);
		},
		2, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 8, \fadeIn, 4, \fadeOut, 6], playGroup);
		},
		3, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 4, \fadeIn, 6, \fadeOut, 2], playGroup);
		},
		4, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 2, \fadeIn, 2, \fadeOut, 3], playGroup);
		},
		5, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 3, \fadeIn, 3, \fadeOut, 1.5], playGroup);
		},
		6, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 6, \fadeIn, 1.5, \fadeOut, 2], playGroup);
		},
		7, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 2, \fadeIn, 2, \fadeOut, 1.5], playGroup);
		},
		8, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 3, \fadeIn, 1.5, \fadeOut, 4], playGroup);
		},
		9, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 4, \fadeIn, 4, \fadeOut, 4], playGroup);
		},
		10, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 4.5, \fadeIn, 4, \fadeOut, 6], playGroup);
		},
		11, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 3, \fadeIn, 6, \fadeOut, 8], playGroup);
		},
		12, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[0], \bufDur, 4], recordGroup, 'addToTail');
			SystemClock.sched(1.0, {
				currentSynth.set(\gate, 0);
				loopPlaySynths.put(0, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, 0.8, \buffer, loopBufs[0], \bufDur, 3, \attack, 8, \decay, 5], group, 'addToTail'));
			});
		},
		13, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[1], \bufDur, 4], recordGroup, 'addToTail');
			SystemClock.sched(1.0, {
				loopPlaySynths.put(1, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, 0, \buffer, loopBufs[1], \bufDur, 3, \attack, 3, \decay, 5], group, 'addToTail'))
			});
		},
		14, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[2], \bufDur, 4], recordGroup, 'addToTail');
			SystemClock.sched(1.0, {loopPlaySynths.put(2, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, -0.8, \buffer, loopBufs[2], \bufDur, 3, \attack, 3, \decay, 5], group, 'addToTail'))
			});
		},
		15, {
			loopPlaySynths.do{arg item; item.set(\gate, 0)};
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 5, \fadeIn, 5, \fadeOut, 4], playGroup);
		},
		16, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 8, \fadeIn, 4, \fadeOut, 5], playGroup);
		},
		17, {

			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[0], \bufDur, 5], group, 'addToTail');
			SystemClock.sched(1.0, {
				currentSynth.set(\gate, 0);
				loopPlaySynths.put(0, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, 0.8, \buffer, loopBufs[0], \bufDur, 4, \attack, 3, \decay, 8], playGroup, 'addToTail'))
			});
		},
		18, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[1], \bufDur, 5], group, 'addToTail');
			SystemClock.sched(1.0, {
				loopPlaySynths.put(1, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, 0, \buffer, loopBufs[1], \bufDur, 4, \attack, 3, \decay, 8], playGroup, 'addToTail'))
			});
		},
		19, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[2], \bufDur, 5], group, 'addToTail');
			SystemClock.sched(1.0, {
				loopPlaySynths.put(2, Synth("grabNLoop_Play_mod", [\outBus, outBus, \pan, -0.8, \buffer, loopBufs[2], \bufDur, 4, \attack, 3, \decay, 8], playGroup, 'addToTail'))
			});
		},
		20, {
			loopPlaySynths.do{arg item; item.set(\gate, 0)};
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 4.5, \fadeIn, 7, \fadeOut, 6], playGroup);
		},
		21, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 3, \fadeIn, 6, \fadeOut, 6], playGroup);
		},
		22, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 12, \fadeIn, 6, \fadeOut, 3], playGroup);
		},
		23, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 3, \fadeIn, 3, \fadeOut, 1.5], playGroup);
		},
		24, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 6, \fadeIn, 1.5, \fadeOut, 4], playGroup);
		},
		25, {
			currentSynth.set(\gate, 0);
			currentSynth = Synth("delayLine_playDelay_mod", [\outBus, outBus, \buffer, delayBuf, \phasorBus, phasorBus, \delayTime, 4, \fadeIn, 4, \fadeOut, 4], playGroup);
		},
		26, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[0], \bufDur, 6], recordGroup, 'addToTail');
			SystemClock.sched(1.0, {
				currentSynth.set(\gate, 0);
				loopPlaySynths.put(0, Synth("grabNLoop_Play_mod", [\outBus, outBus, \buffer, loopBufs[0], \bufDur, 5, \attack, 4, \decay, 10], playGroup, 'addToTail'));
			});
		},
		27, {
			Synth("grabNLoop_Record_mod", [\inBus, mixerToSynthBus, \buffer, loopBufs[1], \bufDur, 6], recordGroup, 'addToTail');
			SystemClock.sched(1.0, {
				loopPlaySynths.put(1, Synth("grabNLoop_Play_mod", [\outBus, outBus, \buffer, loopBufs[1], \bufDur, 5, \attack, 4, \decay, 10], playGroup, 'addToTail'));
			});
		},
		28, {
			loopPlaySynths.do{arg item; item.set(\gate, 0)};
		}
	);
}

}
