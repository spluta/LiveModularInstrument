LoopMachine_Mod : Module_Mod {
	var volBus0, volBus1, phasorBus, rateBus, recordGroup, playGroup, buffer, rateSwitch, controlIndex, thisSynth, nextSynth;

	*initClass {
		StartUp.add {

			SynthDef("loopBufRecord_mod", {arg inBus, outBus, bufnum, phasorBus, volBus, smallGate = 1, gate=1, pauseGate=1;
				var in, phasor, vol, env, smallEnv, pauseEnv;

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*smallGate, 0, BufFrames.kr(bufnum));
				Out.kr(phasorBus, A2K.kr(phasor));

				in = In.ar(inBus,2);

				//in = In.ar(inBus,2);

				vol = In.kr(volBus);

				smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), smallGate);
				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in*smallEnv, bufnum, phasor, loop:1);

				Out.ar(outBus, in*vol*env*pauseEnv);
			}).writeDefFile;

			SynthDef("loopBufPlay_mod", {arg outBus, bufnum, rateBus, phasorBus, volBus, rateSwitch = 0, gate=1, pauseGate=1;
				var playBack, phaseStart, phase, env, rate, vol, pauseEnv, dust;

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				vol = In.kr(volBus);

				dust = Dust.kr(5);
				rate = Select.kr(rateSwitch, [In.kr(rateBus), TRand.kr(2,4,dust)*Select.kr(TIRand.kr(0,1,dust), [-1, 1])]);

				phaseStart = Select.kr(rateSwitch, [Latch.kr(In.kr(phasorBus),1), TRand.kr(0,BufFrames.kr(bufnum),dust)]);
				phase = (Phasor.ar(0, BufRateScale.kr(bufnum)*rate, 0, BufFrames.kr(bufnum))+phaseStart).wrap(0, BufFrames.kr(bufnum));

				playBack = BufRd.ar(2, bufnum, phase, loop:1)*env*vol*pauseEnv;

				XOut.ar(outBus, env, playBack);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LoopMachine", Rect(605, 67, 245, 287));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus(2);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*8, 2);

		volBus0 = Bus.control(group.server);
		volBus1 = Bus.control(group.server);
		phasorBus = Bus.control(group.server);
		rateBus = Bus.control(group.server);

		synths = List.newClear(3);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		synths.put(0, Synth("loopBufRecord_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \phasorBus, phasorBus.index, \volBus, volBus0.index], recordGroup));

		nextSynth = Pseq([1,2], inf).asStream;
		thisSynth = 1;

		controls.add(QtEZSlider("inVol", ControlSpec(0.0,1.0,\amp),
			{|v|
				volBus0.set(v.value);
		}, 0, true, \vert));
		this.addAssignButton(0, \continuous);

		controls.add(QtEZSlider("loopVol", ControlSpec(0.0,1.0,\amp),
			{|v|
				volBus1.set(v.value);
		}, 0, true, \vert));
		this.addAssignButton(1, \continuous);

		controls.add(QtEZSlider("rate", ControlSpec(-4.0,4.0),
			{|v|
				rateBus.set(v.value);
		}, 0, true, \vert));
		this.addAssignButton(2, \continuous);

		controls.add(Button()
			.states_([["input", Color.red, Color.black],["loop", Color.black, Color.green]])
			.action_{arg butt;
				if(thisSynth!=nil,{
					synths[thisSynth].set(\gate, 0);
				});
				thisSynth = nextSynth.next;
				synths.put(thisSynth, Synth("loopBufPlay_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \rateBus, rateBus.index, \phasorBus, phasorBus.index, \volBus, volBus1.index, \rateSwitch, rateSwitch], playGroup));
				synths[0].set(\smallGate, 0);
				controls[3].value= 1;
				controls[4].value= 0;
		});
		this.addAssignButton(3, \onOff);

		controls.add(Button()
			.states_([["norm", Color.red, Color.black],["norm", Color.black, Color.green]])
			.action_{
				if(synths[thisSynth]!=nil,{
					synths[thisSynth].set(\gate, 0);
				});
				synths[0].set(\smallGate, 1);
				controls[4].value= 1;
				controls[3].value= 0;
		});
		this.addAssignButton(4, \onOff);
		controls[4].valueAction = 1;

		win.layout_(VLayout(
			HLayout(controls[0], controls[1], controls[2]),
			HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),

			HLayout(controls[3], controls[4]),
			HLayout(assignButtons[3], assignButtons[4])
		));
		win.layout.spacing = 5;
		win.layout.margins = [5,5,5,5];

	}


	/*	loadSettings {arg xmlSynth;
	rout = Routine({
	group.server.sync;
	controls.do{arg item, i;
	if(i<3,{
	midiHidTemp = xmlSynth.getAttribute("controls"++i.asString);
	if(midiHidTemp!=nil,{
	controls[i].valueAction_(midiHidTemp.interpret);
	});
	})
	};
	});
	AppClock.play(rout);
	}*/


	killMeSpecial {
		volBus0.free;
		volBus1.free;
		phasorBus.free;
		rateBus.free;
	}
}

LoopMachineOverLap_Mod : Module_Mod {
	var buffer, localRout, startPos, duration, vol, playRate, synthOutBus, buffer, dur, fade, trigRate, centerPos, center, width, yRange, recordGroup, playGroup, volBus;

	*initClass {
		StartUp.add {
			SynthDef("loopBufRecordOverlap_mod", {arg inBus, outBus, bufnum, phasorBus, volBus, smallGate0 = 0, smallGate = 0, gate=1, pauseGate=1;
				var in, phasor, vol, env, smallEnv, pauseEnv, internalSmallGate;

				internalSmallGate = 1-((smallGate+smallGate0).clip(0,1));

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum)*internalSmallGate, 0, BufFrames.kr(bufnum));
				Out.kr(phasorBus, A2K.kr(phasor));

				in = In.ar(inBus,2);

				//in = In.ar(inBus,2);

				vol = In.kr(volBus);

				smallEnv = EnvGen.kr(Env.asr(0.02,1,0.02), internalSmallGate);
				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in*smallEnv, bufnum, phasor, loop:1);

				Out.ar(outBus, in*vol*env*pauseEnv);
			}).writeDefFile;
			SynthDef("loopMachineOverLapSamplePlayer_mod", {arg bufnum, outBus, playRate=1, numGrains = 1, startPos, startPos0, dur, phasorBus, t_trig=0, vol=0, onOff = 0, zOnOff = 0, pauseGate = 1, gate = 1;
				var env, pauseEnv, onOffEnv, impulse, out, pan, fade, trigRate, trigRateA, duration, envs;
				var playBuf, toggle, counter, phaseStart, onOffTotal;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				duration = (BufDur.kr(bufnum)*dur*(1/(playRate.abs))).clip(0.02, BufDur.kr(bufnum)/(playRate.abs));

				trigRate = (numGrains/(duration-0.01)).clip(1/(duration-0.01));

				//fade = (BufDur.kr(bufnum)/playRate - ((BufDur.kr(bufnum)/numGrains))).clip(0.01, duration/2);

				fade = (duration-(duration/numGrains)).clip(0.01, duration/2);

				impulse = Impulse.kr(trigRate);
				counter = Stepper.kr(impulse, 0, 0, 7, 1);

				toggle = Select.kr(counter, [[1,0,0,0,0,0,0,0],[0,1,0,0,0,0,0,0],[0,0,1,0,0,0,0,0],[0,0,0,1,0,0,0,0],[0,0,0,0,1,0,0,0],[0,0,0,0,0,1,0,0],[0,0,0,0,0,0,1,0],[0,0,0,0,0,0,0,1]]);

				envs = EnvGen.kr(Env.asr(fade, 1, fade, 'welch'), toggle);

				phaseStart = Latch.kr(In.kr(phasorBus),Decay.kr(t_trig, 0.01)-0.2);

				startPos = Select.kr((playRate+4/4).floor.clip(0, 1), [startPos0, startPos]);

				counter = Stepper.kr(impulse, 0, 0, 3, 1);

				playBuf = PlayBuf.ar(2, bufnum, playRate*BufRateScale.kr(bufnum), toggle, startPos*BufFrames.kr(bufnum), 1)*envs;

				out = Mix(playBuf);

				onOffTotal = Lag.kr((onOff+zOnOff).clip(0,1), 0.05);

				XOut.ar(outBus, onOffTotal, out*env*pauseEnv*vol);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LoopMachineOverLap", Rect(707, 393, 709, 217));
		this.initControlsAndSynths(10);

		this.makeMixerToSynthBus(2);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*8, 2);

		synths = List.newClear(2);

		playRate = 1; vol = 0; fade = 0.1; trigRate = 1; startPos = 0; dur = 4;

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		volBus = Bus.control(group.server);

		synths.put(0, Synth("loopBufRecordOverlap_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \bufnum, buffer.bufnum, \volBus, volBus.index], recordGroup));

		yRange = 1;

		synths.put(1, Synth("loopMachineOverLapSamplePlayer_mod", [\bufnum, buffer.bufnum, \outBus, outBus, \playRate, playRate, \numGrains, 1, \startPos, startPos, \dur, dur, \vol, vol], playGroup));

		controls.add(QtEZSlider.new("in vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);

		}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("out vol", ControlSpec(0,1,'amp'),
			{|v|
				vol = v.value;
				synths[1].set(\vol, v.value);

		}, 0, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZRanger.new("playRange", ControlSpec(0,1,'linear',0.001),
			{|v|
				dur = (v.value[1]-v.value[0]);
				synths[1].set(\startPos, v.value[0], \startPos0, v.value[1], \dur, dur);
		}, [0,1], true, \horz));
		this.addAssignButton(2,\range);

		controls.add(QtEZSlider.new("overlaps", ControlSpec(1,3,'linear'),
			{|v|
				synths[1].set(\numGrains, v.value);
		}, 1, true, \horz));
		this.addAssignButton(3,\continuous);

		controls.add(QtEZSlider.new("playRate", ControlSpec(-4,4,'linear'),
			{|v|
				playRate = v.value;
				if(playRate ==0, {playRate = 0.05});
				if(playRate.abs<0.05, {playRate = 0.05*(playRate.sign)});
				synths[1].set(\playRate, playRate);
		}, 1, true, \horz));
		this.addAssignButton(4,\continuous);

		controls.add(Button());
		controls.add(Button());
		this.addAssignButton(5, \onOff);
		this.addAssignButton(6, \onOff);

		RadioButtons([controls[5],controls[6]],
			[
				[[ "loop", Color.red, Color.black ], [ "loop", Color.black, Color.red ]], [[ "through", Color.red, Color.black ], [ "through", Color.black, Color.red ]]
			],
			[
				{
					synths[1].set(\onOff, 1, \t_trig, 1);
					synths[0].set(\smallGate, 1);

				},{
					synths[1].set(\onOff, 0);
					synths[0].set(\smallGate, 0);
				}
			],
			1);

		controls.add(QtEZSlider2D.new(ControlSpec(0,1), ControlSpec(0.001,1,\exp),
			{arg vals;
				controls[2].valueAction_([(vals.value[0]-(0.65*vals.value[1]*yRange)).clip(0,1), (vals.value[0]+(0.65*vals.value[1]*yRange)).clip(0,1)]);

			}
		));
		this.addAssignButton(7,\slider2D);


		controls.add(QtEZSlider.new("yRange", ControlSpec(0.01,1,'linear'),
			{|v|
				yRange = v.value;
		}, 1, true, \vert));

		controls.add(TypeOSCFuncObject(this, oscMsgs, 9, "zAction",
			{arg val;
				synths[1].set(\zOnOff, val);
				synths[0].set(\smallGate0, val);
			}, true));


		/*controls.add(Button()
			.states_([ [ "NoZActions", Color.red, Color.black ],  [ "ZActions!", Color.blue, Color.black ]])
			.action_{|v|
				if(v.value==1,{
					controls[7].zAction = {|val|
						synths[1].set(\z2OnOff, val.value);
						synths[0].set(\smallGate0, val.value);
					};
				},{
					controls[7].zAction = {};
				}
				);
			};
		);*/

		win.layout_(
			HLayout(
				VLayout(
					HLayout(controls[0],assignButtons[0]),
					HLayout(controls[1],assignButtons[1]),
					HLayout(controls[2],assignButtons[2]),
					HLayout(controls[3],assignButtons[3]),
					HLayout(controls[4],assignButtons[4]),
					HLayout(controls[5], controls[6]),
					HLayout(assignButtons[5].layout, assignButtons[6].layout),
				),
				VLayout(controls[7], assignButtons[7], controls[9]), controls[8]
			)
		);
	}

}



StraightLoop_Mod : Module_Mod {
	var phaseBus, recordGroup, playGroup, buffer, lengthRange, length, controlIndex;

	*initClass {
		StartUp.add {
			SynthDef("straightLoopRec_mod", {arg inBus, outBus, phaseBus, bufnum, t_trig=0, gate=1, pauseGate=1;
				var in, phasor, phaseStart, env, pauseEnv, resetTrig;

				//resetTrig = Decay2.kr(t_trig, 0.001);

				in = In.ar(inBus,2);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				phaseStart = Latch.kr(phasor, t_trig);

				Out.kr(phaseBus, phaseStart);

				Out.ar(outBus, in);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				BufWr.ar(in, bufnum, phasor, loop:1);
			}).writeDefFile;
			SynthDef("straightLoopPlay_mod", {arg outBus, phaseBus, bufnum, loopDur, gate=1, pauseGate=1;
				var sig1, sig2, env1, env2, trig, trig1, trig2, phaseStart, env, smallEnv, pauseEnv, resetTrig, durs, dur;

				phaseStart = In.kr(phaseBus)-(loopDur*BufSampleRate.kr(bufnum));

				trig  = Impulse.ar(1/(loopDur-0.02));

				//durs = Dwhite(loopDur-0.05
				//trig = TDuty.kr(durs);

				//dur = Demand.kr(trig, 0, durs);

				trig1 = PulseDivider.ar(trig, 2, 0);
				trig2 = PulseDivider.ar(trig, 2, 1);

				env1 = EnvGen.ar(Env.new([0,1,1,0],[0.01, loopDur-0.02, 0.01]), trig1);
				env2 = EnvGen.ar(Env.new([0,1,1,0],[0.01, loopDur-0.02, 0.01]), trig2);

				sig1 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig1, phaseStart-(TRand.ar(0,10000, trig1)), loop: 1)*env1;
				sig2 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig2, phaseStart-(TRand.ar(0,10000, trig2)), loop: 1)*env2;

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				XOut.ar(outBus,env, (sig1+sig2)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("StraightLoop", Rect(600, 600, 235, 90));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus(2);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*60, 2);

		phaseBus = Bus.control(group.server);

		synths = List.newClear(2);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		synths.put(0, Synth("straightLoopRec_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \phaseBus, phaseBus.index, \bufnum, buffer.bufnum], recordGroup));

		controlIndex = 2;

		controls.add(Button(win, Rect(5, 5, 70, 20))
			.states_([["new", Color.red, Color.black],["new", Color.black, Color.green]])
			.action_{arg butt;
				length = rrand(lengthRange[0],lengthRange[1]);
				synths[0].set(\t_trig, 1);
				if(controlIndex!=2,{
					synths[1].set(\gate, 0);
				});
				SystemClock.sched(0.03, {
					synths.put(1, Synth("straightLoopPlay_mod", [\outBus, outBus, \phaseBus, phaseBus.index, \bufnum, buffer.bufnum, \loopDur, length], playGroup));
					nil
				});
				this.setButtons(0);
		});
		this.addAssignButton(0, \onOff, Rect(5, 25, 70, 20));

		controls.add(Button(win, Rect(80, 5, 70, 20))
			.states_([["last", Color.red, Color.black],["last", Color.black, Color.green]])
			.action_{arg butt;

				if(controlIndex==2,{
					synths.put(1, Synth("straightLoopPlay_mod", [\outBus, outBus, \phaseBus, phaseBus.index, \bufnum, buffer.bufnum, \loopDur, length], playGroup));
				});
				this.setButtons(1);
		});
		this.addAssignButton(1, \onOff, Rect(80, 25, 70, 20));

		controls.add(Button(win, Rect(155, 5, 70, 20))
			.states_([["thru", Color.red, Color.black],["thru", Color.black, Color.green]])
			.action_{arg butt;
				this.setButtons(2);
				if(butt.value==1,{
					synths[1].set(\gate, 0);
				})
		});
		this.addAssignButton(2, \onOff, Rect(155, 25, 70, 20));

		controls[2].valueAction=1;

		controls.add(EZRanger(win, Rect(5, 50, 225, 40), "length", ControlSpec(0.25, 2),
			{arg vals;
				lengthRange = vals.value;
		}, [0.5,1], true, 50, 30, 30, layout:\horz));
	}

	setButtons {arg val;
		controlIndex = val;
		3.do{|i| controls[i].value=0};
		controls[val].value=1;
	}

	killMeSpecial {
		phaseBus.free;
		buffer.free;
	}
}


StraightLoop2_Mod : Module_Mod {
	var buffer, currentTime, controlIndex, length, recording, recordGroup, playGroup;

	*initClass {
		StartUp.add {
			SynthDef("straightLoop2Rec_mod", {arg inBus, phaseBus, bufnum, t_trig=0, gate=1, pauseGate=1;
				var in, phasor, phaseStart, env, pauseEnv, resetTrig;

				//resetTrig = Decay2.kr(t_trig, 0.001);

				in = In.ar(inBus,2);

				RecordBuf.ar(in, bufnum, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

			}).writeDefFile;
			SynthDef("straightLoop2Play_mod", {arg outBus, phaseBus, bufnum, loopDur, gate=1, pauseGate=1;
				var sig1, sig2, env1, env2, trig, trig1, trig2, env, smallEnv, pauseEnv, resetTrig, durs, dur;

				trig  = Impulse.ar(1/(loopDur-0.02));

				trig1 = PulseDivider.ar(trig, 2, 0);
				trig2 = PulseDivider.ar(trig, 2, 1);

				env1 = EnvGen.ar(Env.new([0,1,1,0],[0.01, loopDur-0.02, 0.01]), trig1);
				env2 = EnvGen.ar(Env.new([0,1,1,0],[0.01, loopDur-0.02, 0.01]), trig2);

				sig1 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig1)*env1;
				sig2 = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), trig2)*env2;

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus,(sig1+sig2)*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("StraightLoop2", Rect(600, 600, 235, 90));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(2);

		buffer = Buffer.alloc(group.server, group.server.sampleRate*60, 2);

		synths = List.newClear(2);

		recordGroup = Group.tail(group);
		playGroup = Group.tail(group);

		length = 0;
		recording = false;

		controls.add(Button(win, Rect(5, 5, 70, 20))
			.states_([["rec", Color.red, Color.black],["stop", Color.black, Color.green]])
			.action_{arg butt;
				if(butt.value == 1,{
					synths[1].set(\gate, 0);
					synths.put(0, Synth("straightLoop2Rec_mod", [\inBus, mixerToSynthBus.index, \bufnum, buffer.bufnum], recordGroup));
					currentTime = Main.elapsedTime;
					recording = true;
				},
				{
					//synths[0].set(\gate, 0);
					length = Main.elapsedTime-currentTime;
					recording = false;
				});
		});
		this.addAssignButton(0, \onOff, Rect(5, 25, 70, 20));

		controls.add(Button(win, Rect(80, 5, 70, 20))
			.states_([["last", Color.red, Color.black],["last", Color.black, Color.green]])
			.action_{arg butt;
				if(recording==true,{
					length = Main.elapsedTime-currentTime;
					recording = false;
					controls[0].value=0;
				});
				synths[1].set(\gate, 0);
				synths.put(1, Synth("straightLoop2Play_mod", [\outBus, outBus, \bufnum, buffer.bufnum, \loopDur, length], playGroup));
				//this.setButtons(1);
		});
		this.addAssignButton(1, \onOff, Rect(80, 25, 70, 20));

		controls.add(Button(win, Rect(155, 5, 70, 20))
			.states_([["stop", Color.red, Color.black],["stop", Color.black, Color.green]])
			.action_{arg butt;
				synths[1].set(\gate, 0);
		});
		this.addAssignButton(2, \onOff, Rect(155, 25, 70, 20));
	}

	loadSettings {arg xmlSynth;

	}
}


GrabNLoop_Mod : Module_Mod {
	var phaseBusses, volBus, recordGroup, playGroup, buffers, lengthRange, length, bufferSeq, nextBuffer;

	*initClass {
		StartUp.add {
			SynthDef("grabNLoopRec_mod", {arg inBus, outBus, phaseBus, bufnum, t_trig=0, gate=1, pauseGate=1;
				var in, phasor, phaseStart, env, pauseEnv, resetTrig;

				//resetTrig = Decay2.kr(t_trig, 0.001);

				in = In.ar(inBus,1);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				phaseStart = Latch.kr(phasor, t_trig);

				env = EnvGen.kr(Env.asr(0, 1, 5), gate, doneAction:2);

				Out.kr(phaseBus, phaseStart);

				//Out.ar(outBus, in);

				BufWr.ar(in, bufnum, phasor, loop:1);
			}).writeDefFile;
			SynthDef("grabNLoopPlay_mod", {arg outBus, phaseBus, bufnum, loopDur, volBus, gate=1, pauseGate=1;
				var sig1, sig2, env1, env2, trig, trig1, trig2, phaseStart, env, smallEnv, pauseEnv, resetTrig, durs, dur, phaseAdjust, vol;

				phaseStart = Latch.kr(In.kr(phaseBus), Line.kr(-1, 1, 0.1))-(loopDur*BufSampleRate.kr(bufnum));

				trig  = Impulse.ar(2/(loopDur-0.02));

				trig1 = PulseDivider.ar(trig, 2, 0);
				trig2 = PulseDivider.ar(trig, 2, 1);

				env1 = EnvGen.ar(Env.sine(loopDur), trig1);
				env2 = EnvGen.ar(Env.sine(loopDur), trig2);

				phaseAdjust = SinOsc.kr(Rand(1/(3*(3+loopDur)), 2/(3*(3+loopDur))), 1.5pi, (loopDur+3)/2, (loopDur+3)/2)*BufSampleRate.kr(bufnum);

				sig1 = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), trig1, (phaseStart+phaseAdjust), loop: 1)*env1;
				sig2 = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), trig2, phaseStart+phaseAdjust, loop: 1)*env2;

				env = EnvGen.kr(Env.asr(4,1,4,1,'welch'), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				vol = In.kr(volBus);

				Out.ar(outBus, Pan2.ar((sig1+sig2), LFNoise2.kr(0.2))*env*pauseEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GrabNLoop", Rect(600, 600, 235, 90));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		bufferSeq = Pseq(#[0,1], inf).asStream;

		buffers = List.new;
		2.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate*30, 1))};

		phaseBusses = List.new;
		2.do{phaseBusses.add(Bus.control(group.server))};
		volBus = Bus.control(group.server);

		synths = List.newClear(4);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		nextBuffer = bufferSeq.next;
		synths.put(nextBuffer*2, Synth("grabNLoopRec_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum], recordGroup));


		controls.add(EZSlider(win, Rect(5, 5, 60, 160),"inVol", ControlSpec(0.0,1.0,\amp),
			{|v|
				volBus.set(v.value);
		}, 0, true, 40, 40, 0, 16, \vert));
		this.addAssignButton(0, \continuous, Rect(5, 165, 60, 16));

		controls.add(Button(win, Rect(5, 185, 60, 20))
			.states_([["go", Color.green, Color.black],["go", Color.black, Color.green]])
			.action_{arg butt;
				length = rrand(lengthRange[0],lengthRange[1]);
				synths[nextBuffer*2].set(\t_trig, 1);
				synths[nextBuffer*2].set(\gate, 0);
				synths[(nextBuffer+1).wrap(0,1)*2+1].set(\gate, 0);
				SystemClock.sched(0.1, {
					synths.put(nextBuffer*2+1, Synth("grabNLoopPlay_mod", [\outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum, \loopDur, length, \volBus, volBus.index], playGroup));

					nextBuffer = bufferSeq.next;
					synths.put(nextBuffer*2, Synth("grabNLoopRec_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \phaseBus, phaseBusses[nextBuffer].index, \bufnum, buffers[nextBuffer].bufnum], recordGroup));
					nil
				});
		});
		this.addAssignButton(1, \onOff, Rect(5, 205, 60, 20));


		controls.add(EZRanger(win, Rect(70, 5, 60, 220), "len", ControlSpec(1, 3),
			{arg vals;
				lengthRange = vals.value;
		}, [0.5,1], true, layout:\vert));
	}

	killMeSpecial {
		phaseBusses.do{|item| item.free};
		volBus.free;
		buffers.do{|item| item.free};
	}
}
