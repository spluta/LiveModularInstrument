GFNGrainPlayer {
	var group, inBus, outBus, buffer, volBus, bufID;
	var dur, playRate, recSynth, synth, dontStop, recordGroup, playGroup;

	*new {arg group, inBus, outBus, buffer, volBus, bufID;
		^super.newCopyArgs(group, inBus, outBus, buffer, volBus, bufID).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("gfnRecord_mod", {arg inBus, bufnum, dur=0.1, t_trig=0;
				var in, trig, env7;

				in = In.ar(inBus, 1);

				trig = Decay.kr(t_trig, dur);

				env7 = EnvGen.ar(Env.new([0,0,BufFrames.kr(bufnum), 0],[0, BufDur.kr(bufnum),0]), trig);

				BufWr.ar(in, bufnum, env7, 0);
			}).writeDefFile;

			SynthDef("gfn2_mod", {arg inBus, outBus, volBus, bufnum, dur, playRate, attackTime=0, gate=0, pauseGate=0, t_trig=0;
				var in, out, env0, env1, env2, env7, vol, pan, impulse, volOsc, trig, pos;

				vol = In.kr(volBus);

				trig = Decay.kr(t_trig, dur);

				impulse = DelayC.kr(Impulse.kr(playRate), 0.05, 0.05);

				pos = EnvGen.kr(Env.new([0,dur/2, BufDur.kr(bufnum)-0.3], [0,1], 0.95), trig) + (SinOsc.kr(Rand(0.05, 0.1)).range((BufDur.kr(bufnum)-0.45).neg, 0)*EnvGen.kr(Env.new([0,0,0,1],[0,0.2, 2]), trig));

				out = TGrains.ar(2, impulse, bufnum, 1, pos, dur, LFNoise2.kr(Rand(2.0,4.0), 2), 2, 4);

				volOsc = SinOsc.kr(0.3, 0.5).range(0.7, 1);

				env1 = EnvGen.kr(Env.asr(attackTime,1,Rand(0.3,0.5)), gate, doneAction:0);

				env2 =  EnvGen.kr(Env.asr(0.2,1,Rand(1,1.5)), pauseGate, doneAction: 1);

				out = out*env1*env2*volOsc*vol;

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		dontStop = false;
		recordGroup = Group.tail(group);
		playGroup = Group.tail(group);
		recSynth = Synth("gfnRecord_mod", [\inBus, inBus, \bufnum, buffer.bufnum], recordGroup);
		synth = Synth.newPaused("gfn2_mod", [\inBus, inBus, \outBus, outBus, \volBus, volBus, \bufnum, buffer.bufnum, \dur, dur, \playRate, playRate], playGroup);
	}

	go {arg durLow, durHigh;
		dur = rrand(durLow, durHigh);
		playRate = 1/(dur);
		recSynth.set(\dur, dur, \t_trig, 1);
		SystemClock.sched(0.02, {synth.set(\attackTime, 0, \dur, dur, \playRate, playRate, \t_trig, 1, \gate, 1, \pauseGate, 1); nil});
		SystemClock.sched(0.2, {synth.set(\attackTime, 0.2); nil});
		SystemClock.sched(rrand(0.3, 0.6), {if(dontStop.not, {synth.set(\gate, 0)}); nil});
		SystemClock.sched(2, {if(dontStop.not, {buffer.zero}); nil});
	}

	freeze {
		dontStop = true;
	}

	unfreeze {
		synth.set(\gate, 0);
		dontStop = false;
	}

	pause {
		if(synth!=nil,{
			synth.set(\pauseGate, 0);
		});
		dontStop = true;
	}

	unpause {
		if(synth!=nil,{
			synth.set(\pauseGate, 1);
			synth.run(true);
		});
	}

	clearBuf {
		SystemClock.sched(2, {if(dontStop.not, {buffer.zero}); nil});
	}
}

GNFGrainObject {
	var group, inBus, outBus, volBus, <>durLow, <>durHigh, buffers, frozen, rout, routs, routStream, players, playerStream, playerCounter, counterTemp;

	*new {arg group, inBus, outBus, volBus, durLow, durHigh;
		^super.newCopyArgs(group, inBus, outBus, volBus, durLow, durHigh).init;
	}

	init {
		buffers = List.new;
		players = List.new;
		30.do{arg i;
			buffers.add(Buffer.alloc(group.server, 44100, 1));
		};

		fork {
			group.server.sync;
			0.4.wait;
			30.do{arg i;
				players.add(GFNGrainPlayer(group, inBus, outBus, buffers[i], volBus, i));
			};
			nil
		};

		playerStream = Pseq((0..29), inf).asStream;
		playerCounter = 0;
		routs = List.new;
		4.do{routs.add(
			Routine({{
				playerCounter = playerStream.next;
				players[playerCounter].go(durLow, durHigh);
				rrand(0.125, 0.35).wait;
			}.loop})
		)};
		routStream = Pseq([0,1,2,3], inf).asStream;
		rout = routs[routStream.next];
		frozen = true;
	}

	assignNumChannels {arg num;
		players.do{|item| item.assignNumChannels(num)};
	}

	unfreeze {
		this.unpause;
		rout.stop;
		rout = routs[routStream.next];
		rout.reset;
		rout.play;
		players.do{arg item; item.unfreeze};
		frozen = false;
	}

	unfreeze2 {
		players.do{arg item; item.unfreeze};
		this.unpause;
		rout.stop;
		rout = routs[routStream.next];
		rout.reset;
		rout.play;
		frozen = false;
	}

	freeze {
		this.unpause;
		rout.stop;
		if(frozen.not,{
			players.do{arg item; item.freeze};
		});
		frozen = true;
	}

	stopRout {
		rout.stop;
	}

	pause {
		rout.stop;
		players.do{arg item; item.pause};
		counterTemp= playerCounter;

	}

	unpause {
		players.do{arg item; item.unpause};
		15.do{|i| players[(playerCounter-i).wrap(0,29)].clearBuf};
	}

	killMe {
		rout.stop
	}
}

GrainFreezeNoise_Mod : Module_Mod {
	var group, outBus, midiHidControl, manta, activeSynth, activeState, grainObjects, muted, volBus;

	init {
		this.makeWindow("GrainFreezeNoise", Rect(500, 400, 60*8, 55));
		this.initControlsAndSynths(8);

		dontLoadControls = Array.series(7);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		muted = true;

		grainObjects = List.new;
		3.do{grainObjects.add(GNFGrainObject(group, mixerToSynthBus.index, outBus, volBus.index, 0.02, 0.2))};

		controls.add(Button.new()
			.states_([["mute", Color.blue, Color.black ],["mute", Color.black, Color.red ]])
			.action_{|v|
				7.do{arg i; controls[i].value = 0};
				v.value = 1;
				muted = true;
				if(activeSynth!=nil,{grainObjects[activeSynth].pause;});
				activeSynth = nil;
		});

		3.do{arg i;
			controls.add(Button.new()
				.states_([["free", Color.blue, Color.black ],["free", Color.black, Color.blue ]])
				.action_{|v|
					7.do{arg i; controls[i].value = 0};
					v.value = 1;
					if(muted, {
						activeSynth = i;
						muted = false;
						grainObjects[i].unfreeze2;
						},{
							if(activeSynth == i,{
								grainObjects[i].unfreeze;
								},{
									grainObjects[activeSynth].pause;
									activeSynth = i;
									grainObjects[i].unfreeze2;
							});

					});
					activeState = 0;
			});
		};

		3.do{arg i;
			controls.add(Button.new()
				.states_([["freeze", Color.blue, Color.black ],["freeze", Color.black, Color.blue ]])
				.action_{|v|
					7.do{arg i; controls[i].value = 0};
					v.value = 1;
					if(muted, {
						activeSynth = i;
						muted = false;
						grainObjects[i].freeze;
						},{
							grainObjects[activeSynth].pause;
							activeSynth = i;
							grainObjects[i].freeze;
					});
					activeState = 1;
			});
		};

		//I need to add all the AassignButtons
		7.do{|i| this.addAssignButton(i, \onOff)};

		controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
			{arg val; volBus.set(val.value)}, 1, true, 'horz'));
		this.addAssignButton(7, \continuous);


		//multichannel button
		numChannels = 2;


		win.layout_(
			VLayout(
				HLayout(controls[0], controls[1], controls[2], controls[3], controls[4], controls[5], controls[6]),
				HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout, assignButtons[4].layout, assignButtons[5].layout, assignButtons[6].layout),
				HLayout(controls[7].layout, assignButtons[7].layout)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
		controls[0].valueAction = 1;
	}

	pause {
		if(muted.not,{
			grainObjects[activeSynth].pause;
		});
	}

	unpause {
		if(muted.not,{
			if(activeState==0,{
				grainObjects[activeSynth].unfreeze2;
				},{
					grainObjects[activeSynth].freeze;
			})
		})
	}

	killMeSpecial {
		volBus.free;
		grainObjects.do{arg item; item.killMe};
		group.freeAllMsg;
	}
}

GFNoiseMini_Mod : Module_Mod {
	var group, outBus, midiHidControl, manta, activeSynth, activeState, grainObjects, muted, volBus;

	init {
		this.makeWindow("GFNoiseMini", Rect(500, 400, 300, 100));
		this.initControlsAndSynths(4);

		dontLoadControls = Array.series(3);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		muted = true;

		grainObjects = List.new;
		grainObjects.add(GNFGrainObject(group, mixerToSynthBus.index, outBus, volBus.index, 0.02, 0.2));

		controls.add(Button.new()
			.states_([["mute", Color.blue, Color.black ],["mute", Color.black, Color.red ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				muted = true;
				if(activeSynth!=nil,{grainObjects[activeSynth].pause;});
				activeSynth = nil;
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["free", Color.blue, Color.black ],["free", Color.black, Color.blue ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				if(muted, {
					activeSynth = 0;
					muted = false;
					grainObjects[0].unfreeze2;
					},{
						grainObjects[0].unfreeze;
				});
				activeState = 0;
		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["freeze", Color.blue, Color.black ],["freeze", Color.black, Color.blue ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				if(muted, {
					activeSynth = 0;
					muted = false;
					grainObjects[0].freeze;
					},{
						activeSynth = 0;
						grainObjects[0].freeze;
				});
				activeState = 1;
		});

		this.addAssignButton(2, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
			{arg val; volBus.set(val.value)}, 1, true, \horz));
		this.addAssignButton(3, \continuous);


		controls[0].valueAction = 1;

		win.layout_(
			VLayout(
				HLayout(controls[0], controls[1], controls[2]),
				HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout),
				HLayout(controls[3].layout, assignButtons[3].layout)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	pause {
		if(muted.not,{
			grainObjects[activeSynth].pause;
		});
	}

	unpause {
		if(muted.not,{
			if(activeState==0,{
				grainObjects[activeSynth].unfreeze2;
				},{
					grainObjects[activeSynth].freeze;
			})
		})
	}

	killMeSpecial {
		volBus.free;
		grainObjects.do{arg item; item.killMe};
		group.freeAllMsg;
	}
}