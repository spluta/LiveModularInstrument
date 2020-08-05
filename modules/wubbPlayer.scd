LoopBuf_Mod : Module_Mod {
	var buffer0, buffer1, startPos, volBus, duration, overlaps, loadFileButton, fileText, canPlayBuf, savePath, fromStopBeginning, startMoved;

	*initClass {

		StartUp.add {
			SynthDef("wubPlayer_mod", {arg bufnum, outBus, vol, startPos=0, gate = 1, pausePlayGate = 0, pauseGate = 1, t_trig = 0, loop = 0;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);
				pausePlayEnv = EnvGen.kr(Env.asr(0.05,1,0), pausePlayGate, doneAction:0);

				playBuf0 = Pan2.ar(PlayBuf.ar(2, bufnum0, BufRateScale.kr(bufnum0)*pausePlayGate, Decay2.kr(t_trig, 0.1)-0.2, startPos, loop), pan);

				Out.ar(outBus, playBuf*env*vol*pauseEnv*pausePlayEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("wubbPlayer", Rect(318, 645, 360, 80));
		this.initControlsAndSynths(6);

		savePath = "";

		fromStopBeginning = 0;

		dontLoadControls.add(2);

		buffer = Buffer.alloc(group.server, group.server.sampleRate);

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
			buffer = Buffer.read(group.server, savePath, 0, -1,
				{arg buf;
					synths[0].set(\bufnum0, buf.bufnum);
			});
		})
	}

	killMeSpecial {
		buffer0.free;
		buffer1.free;
	}

	saveExtra {arg saveArray;
		saveArray.add(savePath.asString);
	}

	loadExtra {arg loadArray;
		savePath = loadArray[4];
		this.loadFile;
	}
}