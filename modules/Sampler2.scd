SamplerSet {
	var <>group, <>buffer, <>scMir, <>startTime, <>variance, <>volume, <>loopTrig, <>fileName, <>durs;

	*new {arg group;
		^super.new.group_(group).init;
	}

	init {
		scMir;

		startTime=0;

		variance = 1;

		volume = Bus.control(group.server);
		volume.set(1);

		loopTrig = 0;
	}

	calculateDurs {
		durs = List.newClear(0);
		scMir.onSets.do{arg item i; if(i!=(scMir.onSets.size-1),{durs.add(scMir.onSets[i+1]-item)},{durs.add()})};
	}

}

Sampler_Mod : Module_Mod {
	var buffers, fileNames, bufferNames, buffer, startPos, volBus, duration, overlaps, loadFileButton, fileText, canPlayBuf, savePath, fromStopBeginning, startMoved, scMir, onsets, durs, currentPanel, text0, text1, onSetsButton, varianceSlider, samplerSettings;

	*initClass {

		StartUp.add {
			SynthDef("samplerPlayerMono_mod", {arg bufnum, outBus, volBus, startPos=0, gate = 1, pauseGate = 1, t_trig = 0, loop = 1;
				var in0, in1, env, out, pauseEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);

				playBuf = Pan2.ar(PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum)*pausePlayGate, 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv*pausePlayEnv);
			}).writeDefFile;

			SynthDef("samplerPlayerStereo_mod", {arg bufnum, outBus, volBus, startPos=0, gate = 1, pauseGate = 1, t_trig = 0, loop = 1;
				var in0, in1, env, out, pauseEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);

				playBuf = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum)*pausePlayGate, 1, startPos, loop);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("samplerTriggerMono_mod", {arg bufnum, outBus, volBus, startPos=0, dur = 1, gate = 1, pauseGate = 1, t_trig = 0, loop = 1;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.new([0,1,1,0], [0.01, dur-0.02, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);

				playBuf = Pan2.ar(PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("samplerTriggerStereo_mod", {arg bufnum, outBus, volBus, startPos=0, dur = 1, gate = 1, pauseGate = 1, t_trig = 0, loop = 1;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.new([0,1,1,0], [0.01, dur-0.02, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:1);

				playBuf = Pan2.ar(PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("Sampler", Rect(318, 645, 360, 80));
		this.initControlsAndSynths(6);

		onSets = ["0"];
		durs = ["1"];

		samplerSettings = Array.fill(8, SamplerSet.new);

		savePath = "";

		currentPanel = 0;

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

				this.loadFile(path);
			},{
				visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
			});
		};

		text0 = StaticText().string_("no file");
		text1 = StaticText().string_("Points:");

		onSetsText = TextField().string_("0");
		onSetsText.action = {arg item;
			try {
				onSets = item.split($ ).asInteger;
			}
			{
				onSets = ["0"];
				onSetsText.string = "0";
				"no!".postln;
			};
		};
		dursText = TextField().string_("1");
		dursText.action = {arg item;
			try {
				durs = item.split($ ).asInteger
			}
			{
				durs = ["1"];
				dursText.string = "1";
				"no!".postln;
			};
		};

		onSetsButton.add(Button()
			.states_([ [ "Load Onsets", Color.green, Color.black ]])
			.action_{|v|
				var temp;

				samplerSettings[currentPanel].scMir.onsetdata();
				samplerSettings[currentPanel].durs = List.newClear(0);
				this.setOnsetsAndDurs;
		});

		varianceSlider = QtEZSlider.new("variance", ControlSpec(0,1,'linear'),
			{|v|
				samplerSettings[currentPanel].variance = v.value;
		}, 0, false, \horz);
		this.addAssignButton(0,\continuous);

		volSlider = QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				samplerSettings[currentPanel].volume = v.value;
		}, 0, false, \horz);

		loopTrigButton = Button()
		.states_([ [ "loop", Color.blue, Color.black ], [ "trig", Color.yellow, Color.black ]])
		.action_{|v|
			samplerSettings[currentPanel].loopTrig = v.value;
		};

		controls.add(Button.new()
			.states_([["allOff", Color.red, Color.black ], ["allOff", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(0);
				synthGroup.set(\gate, 0);
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["buf0", Color.red, Color.black ], ["buf0", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(1);
				synthGroup.set(\gate, 0);
				this.playBuffer(0);

		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["buf1", Color.red, Color.black ], ["buf1", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(2);
				synthGroup.set(\gate, 0);
				this.playBuffer(1);
		});
		this.addAssignButton(2, \onOff);

		controls.add(Button.new()
			.states_([["buf2", Color.red, Color.black ], ["buf2", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(3);
				synthGroup.set(\gate, 0);
				this.playBuffer(2);
		});
		this.addAssignButton(3, \onOff);

		controls.add(Button.new()
			.states_([["buf3", Color.red, Color.black ], ["buf3", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(4);
				synthGroup.set(\gate, 0);
				this.playBuffer(3);
		});
		this.addAssignButton(4, \onOff);

		controls.add(Button.new()
			.states_([["buf4", Color.red, Color.black ], ["buf4", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(5);
				synthGroup.set(\gate, 0);
				this.playBuffer(4);
				}
		});
		this.addAssignButton(5, \onOff);

	controls.add(Button.new()
			.states_([["buf5", Color.red, Color.black ], ["buf5", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(6);
				synthGroup.set(\gate, 0);
				this.playBuffer(5);
				}
		});
		this.addAssignButton(6, \onOff);

controls.add(Button.new()
			.states_([["buf6", Color.red, Color.black ], ["buf6", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(7);
				synthGroup.set(\gate, 0);
				this.playBuffer(6);
				}
		});
		this.addAssignButton(7, \onOff);

controls.add(Button.new()
			.states_([["buf7", Color.red, Color.black ], ["buf7", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(8);
				synthGroup.set(\gate, 0);
				this.playBuffer(7);
				}
		});
		this.addAssignButton(8, \onOff);

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

	getBuffer {arg num;
		var myBuf, counter;

		counter = num;
		myBuf = nil;
		while({(myBuf==nil)&&(counter>=0)},{
			myBuf = samplerSettings[num].buffer;
			counter = counter-1;
		});
		^myBuf
	}

	playBuffer {arg num;
		var buffer, start, dur;

		buffer = this.getBuffer(num);

		if(buffer!=nil,{

			#start, dur = this.getStartAndDur(buffer);

			if(buffer.numChannels==1,{
				if(buffer.loopTrig==0,{
					Synth("samplerPlayerMono_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volume,\startPos, samplerSettings[num].getStart], synthGroup);
				},{
					Synth("samplerTriggerMono_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volume,\startPos, samplerSettings[num].getStart, \dur, samplerSettings[num].getDur], synthGroup);
				})
			},{
				if(buffer.loopTrig==0,{
					Synth("samplerPlayerStereo_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volume,\startPos, samplerSettings[num].getStart], synthGroup);
				},{
					Synth("samplerTriggerStereo_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volume,\startPos, samplerSettings[num].getStart, \dur, samplerSettings[num].getDur], synthGroup);
				})
			})
		});
	}

	setOnsetsAndDurs {
		onSets = samplerSettings[currentPanel].scMir.onsetdata;
		durs = samplerSettings[currentPanel].durs;
		temp = "";
		onSets.do{arg item; temp=temp+item.asString};
		onSetsText.string_(temp);
		temp = "";
		durs.do{arg item; temp=temp+item.asString};
		dursText.string_(temp);
	}

	getStartAndDur {arg buf;
		var startIndex, startTime, dur;

		startIndex = onSets.size.rand;
		startTime = onSets[startIndex];
		dur = durs[startIndex%(durs.size)]; //might be a little brute force
		startTime = startTime*buf.sampleRate+(rrand(variance/2.neg, variance/2)*buf.numFrames);
		^[startTime,dur];
	}

	setDaButtons {arg int;
		8.do{arg i;
			if(int==i,{
				controls[i].value = 1;
			},{
				controls[i].value = 0;
			});
		}
	}

	loadFile {arg path;
		var buffer, scMir;

		if(savePath.size>0,{
			buffer = Buffer.read(group.server, path, 0, -1, {
				fileText.string_(path.split.pop);
				bufferNames.put(currentPanel, path);
				fileNames.put(currentPanel, path);
				scMir = SCMIRAudioFile(buffer.fileName, [[Onsets]]);
				scMir.extractFeatures();
				scMir.extractOnsets();
				samplerSettings[currentPanel].scMir = scMir;
				samplerSettings[currentPanel].calculateDurs;
			});
		})
	}

	killMeSpecial {

	}

	saveExtra {arg saveArray;
		saveArray.add(savePath.asString);
	}

	loadExtra {arg loadArray;
		savePath = loadArray[4];
		this.loadFile;
	}
}