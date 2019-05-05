SamplerSet {
	var <>group, <>buffer, <>startTime, <>variance, <>volBus, <>volume, <>loopTrig, <>fileText, <>onSets, <>durs, <>scMirData, <>scMir;

	*new {arg group;
		^super.new.group_(group).init;
	}

	init {
		onSets = [0];
		durs = [1];

		scMirData = List[[0],[1]];

		startTime=0;

		variance = 0;

		volBus = Bus.control(group.server);
		volBus.set(1);
		volume = 1;

		loopTrig = 0;
	}

	loadBuffer {arg buf;
		buffer = buf;
		scMir = SCMIRAudioFile(buf.path, [[Onsets]]);
		//scMir.extractFeatures();
		scMir.extractOnsets();
		scMirData.put(0, scMir.onsetdata());
		this.calculateDurs;
	}

	loadBufferAgain {arg buf;
		buffer = buf;
		buffer.postln;
		scMir = SCMIRAudioFile.newFromZ(buffer.path.removeExtension++".scmirZ");
		//scMir.extractFeatures();
		//scMir.extractOnsets();
		scMirData.put(0, scMir.onsetdata());
		this.calculateDurs;
	}

	calculateDurs {
		scMirData.put(1, List.newClear(0));
		scMirData[0].do{|item, i|
			if((i!=(scMirData[0].size-1)),
				{
					scMirData[1].add(scMirData[0][i+1]-item)
				},{
					scMirData[1].add(buffer.duration-scMirData[0][scMirData[0].size-1])
			})
		};
	}

}

Sampler_Mod : Module_Mod {
	var buffers, fileNames, bufferNames, buffer, startPos, volBus, duration, overlaps, loadFileButton, fileText, dursText, savePath, fromStopBeginning, startMoved, scMir, onSets, durs, currentPanel, fileText, text1, text2, onSetsButton, varianceSlider, samplerSettings, onSetsText, volSlider, loopTrigButton, panelLoader, loopGroup, trigGroup, clearFileButton;

	*initClass {

		StartUp.add {
			SynthDef("samplerPlayerMono_mod", {arg bufnum, outBus, volBus, startPos=0, gate = 1, pauseGate = 1, loop = 1;
				var in0, in1, env, out, pauseEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:0);

				playBuf = Pan2.ar(PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("samplerPlayerStereo_mod", {arg bufnum, outBus, volBus, startPos=0, gate = 1, pauseGate = 1, loop = 1;
				var in0, in1, env, out, pauseEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:0);

				playBuf = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("samplerTriggerMono_mod", {arg bufnum, outBus, volBus, startPos=0, dur = 1, gate = 1, pauseGate = 1, t_trig = 0, loop = 0;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.new([0,1,1,0], [0.01, dur-0.05, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:0);

				playBuf = Pan2.ar(PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("samplerTriggerStereo_mod", {arg bufnum, outBus, volBus, startPos=0, dur = 1, gate = 1, pauseGate = 1, t_trig = 0, loop = 0;
				var in0, in1, env, out, pauseEnv, pausePlayEnv, playBuf, pan, vol;

				env = EnvGen.kr(Env.new([0,1,1,0], [0.01, dur-0.05, 0.01]), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.1), pauseGate, doneAction:0);

				playBuf = Pan2.ar(PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), 1, startPos, loop), 0);

				vol = In.kr(volBus);
				Out.ar(outBus, playBuf*env*vol*pauseEnv);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("Sampler", Rect(318, 645, 360, 80));
		this.initControlsAndSynths(10);
		loopGroup = Group.tail(group);
		trigGroup = Group.tail(group);

		dontLoadControls = (0..9);

		onSets = ["0"];
		durs = ["1"];

		samplerSettings = Array.fill(8, {SamplerSet.new(group)});



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

		clearFileButton = Button.new()
		.states_([ [ "Clear File", Color.red, Color.black ] ])
		.action_{|v|
			samplerSettings.put(currentPanel, SamplerSet.new(group));
			panelLoader.valueAction = currentPanel;
		};

		fileText = StaticText().string_("no file");
		text1 = StaticText().string_("Onsets:");
		text2 = StaticText().string_("Durs:");

		onSetsText = TextField().string_("0");
		onSetsText.action = {arg item;
			try {
				onSets = item.value.split($ ).asFloat;
			}
			{
				onSets = ["0"];
				onSetsText.string = "0";
				"no!".postln;
			};
			samplerSettings[currentPanel].onSets = onSets;
		};
		dursText = TextField().string_("1");
		dursText.action = {arg item;
			try {
				durs = item.value.split($ ).asFloat;
				if(durs[0]==0,{durs.removeAt(0)});
				if(durs.size<1,{durs = ["1"]});
			}
			{
				durs = ["1"];
				dursText.string = "1";
				"no!".postln;
			};
			samplerSettings[currentPanel].durs = durs;
		};

		onSetsButton = Button()
		.states_([ [ "Load Onsets", Color.green, Color.black ]])
		.action_{|v|
			var temp;
			if(samplerSettings[currentPanel].buffer!=nil,{
				samplerSettings[currentPanel].onSets = samplerSettings[currentPanel].scMirData[0];
				samplerSettings[currentPanel].durs = samplerSettings[currentPanel].scMirData[1];
			},{
				temp = this.getPanelBuffer(currentPanel);
				if(temp>=0,{
					samplerSettings[currentPanel].onSets = samplerSettings[temp].scMirData[0];
					samplerSettings[currentPanel].durs = samplerSettings[temp].scMirData[1];
				})
			});
			this.setOnsetsAndDurs;
		};

		varianceSlider = QtEZSlider.new("variance", ControlSpec(0,1,'amp'),
			{|v|
				samplerSettings[currentPanel].variance = v.value;
		}, 0, false, \horz);
		this.addAssignButton(0,\continuous);

		volSlider = QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				samplerSettings[currentPanel].volBus.set(v.value);
				samplerSettings[currentPanel].volume = v.value;
		}, 1, false, \horz);

		loopTrigButton = Button()
		.states_([ [ "loop", Color.blue, Color.black ], [ "trig", Color.yellow, Color.black ]])
		.action_{|v|
			samplerSettings[currentPanel].loopTrig = v.value;
		};

		panelLoader = PopUpMenu().items_((0..7))
		.action_{|pop|
			currentPanel = pop.value;
			currentPanel.postln;
			this.setOnsetsAndDurs;
			varianceSlider.value = samplerSettings[currentPanel].variance;
			volSlider.value=samplerSettings[currentPanel].volume;
			loopTrigButton.value = samplerSettings[currentPanel].loopTrig;
			fileText.string_(samplerSettings[currentPanel].fileText);
		};

		controls.add(Button.new()
			.states_([["allOff", Color.red, Color.black ], ["allOff", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(0);
				loopGroup.set(\gate, 0);
				trigGroup.set(\gate, -1.01);
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["buf0", Color.red, Color.black ], ["buf0", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(1);
				loopGroup.set(\gate, 0);
				this.playBuffer(0);

		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["buf1", Color.red, Color.black ], ["buf1", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(2);
				loopGroup.set(\gate, 0);
				this.playBuffer(1);
		});
		this.addAssignButton(2, \onOff);

		controls.add(Button.new()
			.states_([["buf2", Color.red, Color.black ], ["buf2", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(3);
				loopGroup.set(\gate, 0);
				this.playBuffer(2);
		});
		this.addAssignButton(3, \onOff);

		controls.add(Button.new()
			.states_([["buf3", Color.red, Color.black ], ["buf3", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(4);
				loopGroup.set(\gate, 0);
				this.playBuffer(3);
		});
		this.addAssignButton(4, \onOff);

		controls.add(Button.new()
			.states_([["buf4", Color.red, Color.black ], ["buf4", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(5);
				loopGroup.set(\gate, 0);
				this.playBuffer(4);
		});
		this.addAssignButton(5, \onOff);

		controls.add(Button.new()
			.states_([["buf5", Color.red, Color.black ], ["buf5", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(6);
				loopGroup.set(\gate, 0);
				this.playBuffer(5);
		});
		this.addAssignButton(6, \onOff);

		controls.add(Button.new()
			.states_([["buf6", Color.red, Color.black ], ["buf6", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(7);
				loopGroup.set(\gate, 0);
				this.playBuffer(6);
		});
		this.addAssignButton(7, \onOff);

		controls.add(Button.new()
			.states_([["buf7", Color.red, Color.black ], ["buf7", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(8);
				loopGroup.set(\gate, 0);
				this.playBuffer(7);
		});
		this.addAssignButton(8, \onOff);

		controls.add(Button.new()
			.states_([["seq", Color.red, Color.black ], ["seq", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(9);
				loopGroup.set(\gate, 0);
				//turn on the sequencer
		});
		this.addAssignButton(9, \onOff);

		win.layout_(
			HLayout(
				VLayout(
					HLayout(loadFileButton, clearFileButton),
					fileText,
					HLayout(text1, onSetsText),
					HLayout(text2,dursText),
					onSetsButton),
				VLayout(
					varianceSlider.layout,
					volSlider.layout,
					loopTrigButton,
					panelLoader
				),
				VLayout(controls[0],assignButtons[0].layout),
				VLayout(controls[1],assignButtons[1].layout),
				VLayout(controls[2],assignButtons[2].layout),
				VLayout(controls[3],assignButtons[3].layout),
				VLayout(controls[4],assignButtons[4].layout),
				VLayout(controls[5],assignButtons[5].layout),
				VLayout(controls[6],assignButtons[6].layout),
				VLayout(controls[7],assignButtons[7].layout),
				VLayout(controls[8],assignButtons[8].layout)
			)
		);
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	getPanelBuffer {arg num;
		var myBuf, counter;

		counter = num;
		myBuf = nil;
		while({(myBuf==nil)&&(counter>=1)},{
			counter = counter-1;
			myBuf = samplerSettings[counter].buffer;
		});
		^(counter)
	}

	getBuffer {arg num;
		var myBuf, counter;

		counter = num;
		myBuf = nil;
		while({(myBuf==nil)&&(counter>=0)},{
			myBuf = samplerSettings[counter].buffer;
			counter = counter-1;
		});
		^myBuf
	}

	playBuffer {arg num;
		var buffer, start, dur;

		buffer = this.getBuffer(num);

		if(buffer!=nil,{

			#start, dur = this.getStartAndDur(num, buffer);

			[start, dur, num, buffer,buffer.numChannels].postln;

			if(buffer.numChannels==1,{
				if(samplerSettings[num].loopTrig==0,{
					"samplerPlayerMono_mod".postln;
					Synth("samplerPlayerMono_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volBus,\startPos, start], loopGroup);
				},{
					"samplerTriggerMono_mod".postln;
					Synth("samplerTriggerMono_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volBus,\startPos, start, \dur, dur], trigGroup);
				})
			},{
				if(samplerSettings[num].loopTrig==0,{
					"samplerPlayerStereo_mod".postln;
					Synth("samplerPlayerStereo_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volBus,\startPos, start], loopGroup);
				},{
					"samplerTriggerStereo_mod".postln;
					Synth("samplerTriggerStereo_mod", [\bufnum, buffer, \outBus, outBus, \volBus, samplerSettings[num].volBus,\startPos, start, \dur, dur], trigGroup);
				})
			})
		});
	}

	setOnsetsAndDurs {
		var temp;

		currentPanel.postln;
		onSets = samplerSettings[currentPanel].onSets;
		durs = samplerSettings[currentPanel].durs;
		temp = "";
		onSets.do{arg item; temp=temp+item.asString};
		onSetsText.string_(temp);
		temp = "";
		durs.do{arg item; temp=temp+item.asString};
		dursText.string_(temp);
	}

	getStartAndDur {arg num, buf;
		var startIndex, startTime, dur, variance;

		startIndex = samplerSettings[num].onSets.size.rand;
		startTime = samplerSettings[num].onSets[startIndex];

		[startIndex, startTime].postln;

		if(samplerSettings[num].loopTrig==0,{
			dur = buf.duration;
		},{
			dur = samplerSettings[num].durs[startIndex%(durs.size)]; //might be a little brute force
		});

		startTime = ((startTime*buf.sampleRate)+((rrand(samplerSettings[num].variance/2.neg, samplerSettings[num].variance/2))*dur*group.server.sampleRate)).wrap(0, buf.numFrames);
		^[startTime,dur];
	}

	setDaButtons {arg int;
		10.do{arg i;
			if(int==i,{
				controls[i].value = 1;
			},{
				controls[i].value = 0;
			});
		}
	}

	loadFile {arg path;


		Buffer.read(group.server, path, action: {arg buf;
			"lockedAndLoaded".postln;
			{fileText.string_(path.split.pop)}.defer;
			samplerSettings[currentPanel].fileText = path.split.pop;
			samplerSettings[currentPanel].loadBuffer(buf);
		});
	}

	loadFilesOnLoad {arg path, num;

		path.postln;
		Buffer.read(group.server, path, action: {arg buf;
			samplerSettings[num].fileText = path.split.pop;
			samplerSettings[num].loadBufferAgain(buf);
			//fileNames.put(currentPanel, path);
			//scMir = SCMIRAudioFile.newFromZ(path.removeExtension++".scmirZ");
			//samplerSettings[num]
		});
	}

	killMeSpecial {

	}

	saveExtra {arg saveArray;
		var temp, lilTemp;

		temp = List.newClear(0);

		samplerSettings.do{arg item,i;
			lilTemp = List.newClear(0);
			if(item.buffer.postln!=nil,{
				lilTemp.add(item.buffer.path);
				item.scMir.save(item.buffer.path.removeExtension++".scmirZ");
			},{lilTemp.add(nil)});
			lilTemp.add(item.variance);
			lilTemp.add(item.volume);
			lilTemp.add(item.loopTrig);
			lilTemp.add(item.onSets);
			lilTemp.add(item.durs);
			temp.add(lilTemp.postln);
		};
		temp.do{arg item; item.postln};
		saveArray.add(temp);
	}

	loadExtra {arg loadArray;
		var temp;

		temp = loadArray;
		temp.postln;

		AppClock.sched(5.0, {
			temp.do{arg item, i;
				item.postln;
				if(item!=nil,{
					this.loadFilesOnLoad(item[0], i);
					samplerSettings[i].variance = item[1];
					samplerSettings[i].volume = item[2];
					samplerSettings[i].volBus.set(item[2]);
					samplerSettings[i].loopTrig = item[3];
					samplerSettings[i].onSets = item[4];
					samplerSettings[i].durs = item[5];
				});
			};
			panelLoader.valueAction=0;
			nil
		});
	}
}