OverLapSamples_Mod : Module_Mod {
	var startPos, startPos0, duration, loadFileButton, fileText, savePath, shortPath, vol, playRate, synthOutBus, buffer0, buffer1, monoStereo, dur, fade, trigRate, centerPos, center, width, yRange;

	*initClass {
		StartUp.add {
			SynthDef("overLapSamplePlayer_mod", {arg bufnum0, bufnum1, whichOut = 0, playRate=1, numGrains = 1, startPos, startPos0, dur, outBus, vol=0, trigOnOff=0, onOff = 1, pauseGate = 1, gate = 1;
				var env, pauseEnv, onOffEnv, impulse, out, out0, out1, pan, fade, trigRate, trigRateA, duration, envs, impulse0;
				var playBuf0, playBuf1;
				var toggle;
				var counter;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				onOffEnv = EnvGen.kr(Env.asr(0.01,1,0.01), onOff, doneAction:0);

				duration = (BufDur.kr(bufnum0)*dur).clip(0.02, BufDur.kr(bufnum0));

				trigRate = Select.kr(trigOnOff, [0, numGrains/(duration-0.01)]).clip(1/(duration-0.01), 100);

				fade = (BufDur.kr(bufnum0) - ((BufDur.kr(bufnum0)/numGrains))).clip(0.01, duration/2);

				impulse = Impulse.kr(trigRate);

				counter = Stepper.kr(impulse, 0, 0, 7, 1);

				toggle = Select.kr(counter, [[1,0,0,0,0,0,0,0],[0,1,0,0,0,0,0,0],[0,0,1,0,0,0,0,0],[0,0,0,1,0,0,0,0],[0,0,0,0,1,0,0,0],[0,0,0,0,0,1,0,0],[0,0,0,0,0,0,1,0],[0,0,0,0,0,0,0,1]]);

				pan = Select.kr(whichOut, [TRand.kr(-1,1, toggle), -1]);

				envs = EnvGen.kr(Env.asr(fade, 1, fade, 'welch'), toggle);

				startPos = Select.kr((playRate+4/4).floor.clip(0, 1), [startPos0, startPos]);

				playBuf0 = Pan2.ar(PlayBuf.ar(1, bufnum0, playRate*BufRateScale.kr(bufnum0), toggle, startPos*BufFrames.kr(bufnum0), 0), pan)*envs;

				playBuf1 = Pan2.ar(PlayBuf.ar(1, bufnum1, playRate*BufRateScale.kr(bufnum1), toggle, startPos*BufFrames.kr(bufnum1), 0), 1)*envs;

				out0 = Mix(playBuf0);
				out1 = Mix(playBuf1);

				out = Select.ar(whichOut, [out0, out0+out1]);

				Out.ar(outBus, out*env*pauseEnv*vol*onOffEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("OverLapSamples", Rect(707, 393, 709, 217));
		this.initControlsAndSynths(7);

		savePath = "";

		synths = List.newClear(1);

		playRate = 1; vol = 0; fade = 0.1; trigRate = 1; startPos = 0; dur = 4;

		buffer0 = Buffer.alloc(group.server, group.server.sampleRate);
		buffer1 = Buffer.alloc(group.server, group.server.sampleRate);

		yRange = 1;

		synths.put(0, Synth("overLapSamplePlayer_mod", [\bufnum0, buffer0.bufnum,  \bufnum1, buffer1.bufnum, \whichOut, 0, \playRate, playRate, \numGrains, 1, \startPos, startPos, \dur, dur, \outBus, outBus, \vol, 0], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				vol = v.value;
				synths[0].set(\vol, v.value);

		}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZRanger.new("playRange", ControlSpec(0,1,'linear',0.001),
			{|v|
				startPos = v.value[0];
				startPos0 = v.value[1];
				dur = (v.value[1]-v.value[0]);
				synths[0].set(\startPos, startPos, \startPos0, startPos0, \dur, dur);
		}, [0,1], true, \horz));
		this.addAssignButton(1,\range);

		controls.add(QtEZSlider.new("overlaps", ControlSpec(1,3,'linear'),
			{|v|
				synths[0].set(\numGrains, v.value);
		}, 1, true, \horz));
		this.addAssignButton(2,\continuous);

		controls.add(QtEZSlider.new("playRate", ControlSpec(-4,4,'linear'),
			{|v|
				synths[0].set(\playRate, v.value);
		}, 1, true, \horz));
		this.addAssignButton(3,\continuous);

		controls.add(QtEZSlider2D.new(ControlSpec(0,1), ControlSpec(0.001,1,\exp),
			{arg vals;
				controls[1].valueAction_([(vals.value[0]-(0.65*vals.value[1]*yRange)).clip(0,1), (vals.value[0]+(0.65*vals.value[1]*yRange)).clip(0,1)]);}
		));
		this.addAssignButton(4,\slider2D);

		controls.add(QtEZSlider.new("yRange", ControlSpec(0.01,1,'linear'),
			{|v|
				yRange = v.value;
		}, 1, true, \vert));
		this.addAssignButton(5,\continuous);

		loadFileButton = Button()
		.states_([ [ "Load File", Color.red, Color.black ] ])
		.action_{|v|
			visibleArray = List.newClear;
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};

			Dialog.openPanel({ arg path;
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				savePath = path;
				this.loadFile;
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};
		fileText = StaticText();

		controls.add(Button()
			.states_([ [ "NoZActions", Color.red, Color.black ],  [ "ZActions!", Color.blue, Color.black ]])
			.action_{|v|
				if(v.value ==1,{
					controls[4].zAction = {|val|
						synths[0].set(\onOff, val.value)
					};
					synths[0].set(\onOff, 0)
				},{
					synths[0].set(\onOff, 1);
					controls[4].zAction = {};
				}
			);
		};
	);

	win.layout_(
		HLayout(
			VLayout(
				HLayout(controls[0],assignButtons[0]),
				HLayout(controls[1],assignButtons[1]),
				HLayout(controls[2],assignButtons[2]),
				HLayout(controls[3],assignButtons[3]),
				HLayout(loadFileButton, fileText, controls[6])
			),
			VLayout(controls[4], assignButtons[4]), controls[5]
		)
	);
}

loadFile {
	if(savePath.size>0,{
		fileText.string_(savePath.split.pop);
		synths[0].set(\whichOut, 0);
		buffer0 = Buffer.readChannel(group.server, savePath, 0, -1, [0],
			{arg buf;
				synths[0].set(\bufnum0, buf.bufnum);
				synths[0].set(\whichOut, 0);
				SystemClock.sched(1.0, {synths[0].set(\trigOnOff, 1)});
		});
		SystemClock.sched(0.5, {
			buffer1 = Buffer.readChannel(group.server, savePath, 0, -1, [1],
				{arg buf;
					synths[0].set(\bufnum1, buf.bufnum, \whichOut, 1);
			});
		});
	})
}

saveExtra {arg saveArray;
	saveArray.add(savePath.asString);
}

loadExtra {arg loadArray;
	savePath = loadArray[4];
	this.loadFile;
}
}

LoopBuf_Mod : Module_Mod {
	var buffer0, buffer1, startPos, volBus, duration, overlaps, loadFileButton, fileText, canPlayBuf, savePath, fromStopBeginning, startMoved;

	*initClass {

		StartUp.add {
			SynthDef("loopPlayer_mod", {arg bufnum0, bufnum1, whichOut=0, outBus, vol, startPos=0, gate = 1, pausePlayGate = 0, pauseGate = 1, t_trig = 0, loop = 0;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf0, playBuf1, pan;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);
				pausePlayEnv = EnvGen.kr(Env.asr(0.05,1,0), pausePlayGate, doneAction:0);

				pan = Select.kr(whichOut, [0, -1]);

				playBuf0 = Pan2.ar(PlayBuf.ar(1, bufnum0, BufRateScale.kr(bufnum0)*pausePlayGate, Decay2.kr(t_trig, 0.1)-0.2, startPos, loop), pan);

				playBuf1 = Pan2.ar(PlayBuf.ar(1, bufnum1, BufRateScale.kr(bufnum1)*pausePlayGate, Decay2.kr(t_trig, 0.1)-0.2, startPos, loop), 1);

				out = Select.ar(whichOut, [playBuf0, playBuf0+playBuf1]);

				out = LeakDC.ar(out);

				Out.ar(outBus, out*env*vol*pauseEnv*pausePlayEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LoopBuf", Rect(318, 645, 360, 80));
		this.initControlsAndSynths(6);

		savePath = "";

		fromStopBeginning = 0;

		dontLoadControls.add(2);

		buffer0 = Buffer.alloc(group.server, group.server.sampleRate);
		buffer1 = Buffer.alloc(group.server, group.server.sampleRate);

		startMoved = false;

		synths.add(Synth("loopPlayer_mod", [\bufnum, buffer0.bufnum,  \bufnum1, buffer1.bufnum, \whichOut, 0, \outBus, outBus, \playPauseGate, 0, \vol, 0], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
		}, 0, false, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider("startPos", ControlSpec(0,1),
			{|v|
				var time;

				synths[0].set(\startPos, (v.value*buffer0.numFrames));
				startMoved = true;
				time = SMPTE(v.value*buffer0.duration).asMinSec;
				controls[1].numBox.string = time[0].asString++":"++time[1].round.asString;

		}, 0, false, \horz));

		controls.add(Button()
			.states_([ [ "paused", Color.green, Color.black ], [ "playing", Color.red, Color.black ]])
			.action_{|v|
				if(fromStopBeginning==0,{
					if(startMoved, {synths[0].set(\t_trig, 1)});
					synths[0].set(\pausePlayGate, v.value);
				},{
					synths[0].set(\pausePlayGate, v.value, \t_trig, 1);
				});
				startMoved = false;
		});
		this.addAssignButton(2,\onOff);

		controls.add(Button()
			.states_([ [ "reset", Color.blue, Color.black ]])
			.action_{|v|
				controls[2].value_(0);
				synths[0].set(\pausePlayGate, 0, \t_trig, 1);
		});
		this.addAssignButton(3,\onOff);

		controls.add(Button()
			.states_([ [ "no loop", Color.green, Color.black ], [ "loop", Color.red, Color.black ]])
			.action_{|v|
				controls[1].value_(0);
				synths[0].set(\loop, v.value);
		});

		controls.add(Button()
			.states_([ [ "fromStop", Color.green, Color.black ], [ "fromBeginning", Color.red, Color.black ]])
			.action_{|v|
				fromStopBeginning = v.value;
		});

		loadFileButton = Button.new()
		.states_([ [ "Load File", Color.red, Color.black ] ])
		.action_{|v|
			visibleArray = List.newClear;
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};
			Dialog.openPanel({ arg path;
				var shortPath;

				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};

				savePath = path;
				this.loadFile;
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};
		fileText = StaticText.new();

		win.layout_(
			VLayout(
				HLayout(controls[0],assignButtons[0]),
				HLayout(controls[1]),
				HLayout(controls[2],assignButtons[2], controls[3],assignButtons[3]),
				HLayout(loadFileButton, fileText, controls[4], controls[5])
			)
		);
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	loadFile {
		if(savePath.size>0,{
			controls[2].value_(0);
			fileText.string_(savePath.split.pop);
			synths[0].set(\whichOut, 0, \pausePlayGate, 0);
			buffer0 = Buffer.readChannel(group.server, savePath, 0, -1, [0],
				{arg buf;
					synths[0].set(\bufnum0, buf.bufnum);
			});
			SystemClock.sched(0.5, {
				buffer1 = Buffer.readChannel(group.server, savePath, 0, -1, [1],
					{arg buf2;
						synths[0].set(\bufnum1, buf2.bufnum, \whichOut, 1);
				});
			});
		})
	}

	killMeSpecial {
		buffer0.free;
		buffer1.free;
	}

	saveExtra {arg saveArray;
		savePath.postln;
		saveArray.add(savePath.asString);
	}

	loadExtra {arg loadArray;
		savePath = loadArray;
		this.loadFile;
	}
}


SampleMashup_Mod : Module_Mod {
	var buffers, buffer, durations, loadFilesButton, canPlayBuf, savePath, paths2, startPos, files, dirName, playTask, bufStream, durLow, durHi, dur, volBus, play;

	*initClass {
		StartUp.add {
			SynthDef("sampleMashupPlayerMono_mod", {arg bufnum, outBus, startPos = 0, dur, volBus, gate = 1, pauseGate = 1;
				var in0, in1, env, env2, out, pauseEnv, vol;

				vol = In.kr(volBus);

				env2 = EnvGen.kr(Env.new([0,1,1,0],[0.01, dur, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop: 0);
				out = Pan2.ar(out, Rand(-1.0,1.0));
				Out.ar(outBus, out*env2*pauseGate*vol);
			}).writeDefFile;

			SynthDef("sampleMashupPlayerStereo_mod", {arg bufnum, outBus, startPos = 0, dur, volBus, gate = 1, pauseGate = 1;
				var in0, in1, env, env2, out, pauseEnv, vol;

				vol = In.kr(volBus);

				env2 = EnvGen.kr(Env.new([0,1,1,0],[0.01, dur, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop: 0);

				Out.ar(outBus, out*env2*pauseGate*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SampleMashup", Rect(834, 445, 393, 122));
		this.initControlsAndSynths(4);

		dontLoadControls.add(0);

		savePath = "";
		synths = List.newClear(1);

		volBus = Bus.control(group.server);
		volBus.set(0);

		play = false;

		controls.add(Button()
			.states_([ [ "Off", Color.green, Color.black ], [ "On", Color.blue, Color.black ]])
			.action_{|v|
				if(playTask!=nil,{
					if(v.value == 1,{
						play = true;
						playTask.start;
					},{
						play = false;
						playTask.pause;
						synths[0].set(\gate, 0);
					});
				});

		});
		this.addAssignButton(0,\onOff);

		controls.add(QtEZSlider("Speed", ControlSpec(0.5, 20),
			{arg val;
				durLow = 1/val.value;
		}, 2, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider("Speed", ControlSpec(0.5, 20),
			{arg val;
				durHi = 1/val.value;
		}, 20, true, \horz));
		this.addAssignButton(2,\continuous);

		loadFilesButton = Button.new()
		.states_([ [ "Load File", Color.red, Color.black ] ])
		.action_{|v|
			visibleArray = List.newClear;
			Window.allWindows.do{arg item;

				visibleArray.add(item.visible);
				item.visible = false
			};
			Dialog.openPanel({ arg path;
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				this.loadBuffers(path)
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value)
		}, 0, true, \horz));
		this.addAssignButton(3,\continuous);

		win.layout_(VLayout(
			HLayout(controls[0], loadFilesButton),
			HLayout(assignButtons[0], nil),
			HLayout(controls[1], assignButtons[1]),
			HLayout(controls[2], assignButtons[2]),
			HLayout(controls[3], assignButtons[3])));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	loadBuffers {arg path;
		savePath = path;
		dirName = path.dirname++"/*";
		paths2 = dirName.pathMatch.select({|file| file.contains(".aiff") });
		paths2.addAll(dirName.pathMatch.select({|file| file.contains(".aif") }));
		paths2.addAll(dirName.pathMatch.select({|file| file.contains(".wav") }));

		paths2 = paths2.sort;

		buffers.do{arg buffer;
			if(buffer!=nil, {buffer.free;});
		};
		buffers = List.newClear(paths2.size);

		if(paths2.size>0,{savePath = paths2[0]});
		paths2.do({ arg path, i;
			var shortPath;

			shortPath = path.split.pop;

			buffers.put(i, Buffer.read(group.server, path));
		});

		bufStream = Pxrand(buffers, inf).asStream;
		playTask = Task({{
			buffer = bufStream.next;

			dur = (rrand(durLow, durHi));

			startPos = rrand(0, buffer.numFrames.rand-(dur*group.server.sampleRate));

			if(buffer.numChannels == 1, {
				synths.put(0, Synth("sampleMashupPlayerMono_mod",[\bufnum, buffer.bufnum, \outBus, outBus, \startPos, startPos, \dur, dur, \volBus, volBus], group));
			},{
				synths.put(0, Synth("sampleMashupPlayerStereo_mod",[\bufnum, buffer.bufnum, \outBus, outBus, \startPos, startPos, \dur, dur, \volBus, volBus], group));
			});
			dur.wait;
		}.loop});
	}

	pause {
		synths.do{|item| if(item!=nil, item.set(\gate, 0))};
		if(playTask!=nil,{playTask.pause});
	}

	unpause {
		if(play==true,{
			if(playTask!=nil,{playTask.start});
		});
	}

	killMeSpecial {
		buffers.do{arg buffer;
			if(buffer!=nil, {buffer.free;});
		};
	}

	saveExtra {arg saveArray;
		saveArray.add(savePath.asString);
	}

	loadExtra {arg loadArray;
		savePath = loadArray[4];
		if(savePath.size>0,{
			this.loadBuffers(savePath);
		});
	}
}