SampleBank_Mod : Module_Mod {
	var buffers, volBus, durations, loadFilesButton, ezList, canPlayBuf, savePath, paths2, currentBuffer, lastPlay, files, dirName, lastPlayTime;

	*initClass {
		StartUp.add {
			SynthDef("sampleBankPlayerMono_mod", {arg bufnum, outBus, volBus, gate = 1, pauseGate = 1;
				var in0, in1, env, env2, out, vol, pauseEnv;

				env = EnvGen.kr(Env.new([0,1,1,0],[0.01, BufDur.kr(bufnum)-0.02, 0.01]), gate, doneAction:2);
				env2 = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				vol = In.kr(volBus);

				out = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop: 0);
				out = Pan2.ar(out, Rand(-1.0,1.0));
				Out.ar(outBus, out*env*env2*vol*pauseGate);
			}).writeDefFile;

			SynthDef("sampleBankPlayerStereo_mod", {arg bufnum, outBus, volBus, gate = 1, pauseGate = 1;
				var in0, in1, env, env2, out, vol, pauseEnv;

				env = EnvGen.kr(Env.new([0,1,1,0],[0.01, BufDur.kr(bufnum)-0.02, 0.01]), gate, doneAction:2);
				env2 = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				vol = In.kr(volBus);

				out = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum), loop: 0);

				Out.ar(outBus, out*env*env2*vol*pauseGate);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SampleBank", Rect(318, 645, 310, 310));
		this.initControlsAndSynths(6);

		dontLoadControls.addAll((1..5));

		savePath = "";

		volBus = Bus.control(group.server);

		lastPlayTime = Main.elapsedTime;

		controls.add(EZSlider.new(win,Rect(5, 0, 60, 160), "vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, layout:\vert));
		this.addAssignButton(0,\continuous,Rect(5, 160, 60, 20));

		controls.add(Button(win,Rect(5, 230, 60, 20))
			.states_([ [ "play", Color.green, Color.black ], [ "play", Color.blue, Color.black ]])
			.action_{|v|
				if(Main.elapsedTime-lastPlayTime>0.2,
					{
						lastPlay = currentBuffer;
						lastPlay.postln;
						if(buffers[lastPlay].numChannels == 1, {
							synths.put(lastPlay, Synth("sampleBankPlayerMono_mod",[\bufnum, buffers[lastPlay].bufnum, \outBus, outBus, \volBus, volBus.index], group));
							},{
								synths.put(lastPlay, Synth("sampleBankPlayerStereo_mod",[\bufnum, buffers[lastPlay].bufnum, \outBus, outBus, \volBus, volBus.index], group));
						});
						lastPlayTime = Main.elapsedTime;
						ezList.valueAction_(ezList.value+1);
				});
		});
		this.addAssignButton(1,\onOff,Rect(5, 250, 60, 20));

		controls.add(Button(win,Rect(65, 230, 60, 20))
			.states_([ [ "prev", Color.green, Color.black ], [ "prev", Color.blue, Color.black ]])
			.action_{|v|
				ezList.valueAction_(ezList.value-1);
		});
		this.addAssignButton(2,\onOff,Rect(65, 250, 60, 20));
		controls.add(Button(win,Rect(125, 230, 60, 20))
			.states_([ [ "next", Color.green, Color.black ], [ "next", Color.blue, Color.black ]])
			.action_{|v|
				ezList.valueAction_(ezList.value+1);
		});
		this.addAssignButton(3,\onOff,Rect(125, 250, 60, 20));
		controls.add(Button(win,Rect(185, 230, 60, 20))
			.states_([ [ "stopLast", Color.green, Color.black ], [ "stopLast", Color.blue, Color.black ]])
			.action_{|v|
				lastPlay.postln;
				synths[lastPlay].set(\gate, 0);
		});
		this.addAssignButton(4,\onOff,Rect(185, 250, 60, 20));
		controls.add(Button(win,Rect(245, 230, 60, 20))
			.states_([ [ "stopAll", Color.green, Color.black ], [ "stopAll", Color.blue, Color.black ]])
			.action_{|v|
				synths.do{arg synth;
					synth.postln;
					synth.set(\gate, 0);
				};
		});
		this.addAssignButton(5,\onOff,Rect(245, 250, 60, 20));

		loadFilesButton = Button.new(win,Rect(5, 180, 60, 20))
		.states_([ [ "Load File", Color.red, Color.black ] ])
		.action_{|v|
			Window.allWindows.do{arg item; item.visible = false};
			Dialog.openPanel({ arg path;
				Window.allWindows.do{arg item; item.visible = true};
				this.loadBuffers(path)
				},{
					Window.allWindows.do{arg item; item.visible = true};
			});
		};
		ezList = EZListView.new(win,Rect(70, 0, 240, 200));
	}

	loadBuffers {arg path;
		savePath = path;
		dirName = path.dirname++"/*";
		dirName.postln;
		paths2 = dirName.pathMatch.select({|file| file.contains(".aif") });
		paths2.addAll(dirName.pathMatch.select({|file| file.contains(".wav") }));

		paths2 = paths2.sort;
		paths2.postln;
		paths2.size.postln;

		synths.do{arg item;
			item.free;
		};
		buffers.do{arg item;
			item.free;
		};

		synths = List.newClear(paths2.size);
		buffers.do{arg buffer;
			if(buffer!=nil, {buffer.free;});
		};
		buffers = List.newClear(paths2.size);
		ezList.items.size.do{arg i; ezList.removeItemAt(0)};

		paths2.postln;

		if(paths2.size>0,{savePath = paths2[0]});
		paths2.do({ arg path, i;
			var shortPath;

			shortPath = path.split.pop;

			ezList.addItem(shortPath.asSymbol, {|a| currentBuffer = i.postln});

			buffers.put(i, Buffer.read(group.server, path));
		});

		//something is messed up with the resizeing of EZListView
/*		if(paths2.size>11,{
			"set it to 20X".postln;
			ezList.view.bounds_(Rect(70, 0, 450, 20*paths2.size));
			(1..5).do{arg i;
				i.postln;
				controls[i].bounds_(Rect(controls[i].bounds.left, 5+(paths2.size*20), controls[i].bounds.width, controls[i].bounds.height));
				assignButtons[i].setBounds(Rect(assignButtons[i].bounds.left, 25+(paths2.size*20), assignButtons[i].bounds.width, assignButtons[i].bounds.height));
			};
			win.bounds_(Rect(win.bounds.left, win.bounds.top, win.bounds.width, 50+(paths2.size*20)));
			},{
				"set it to 200".postln;
				ezList.view.bounds_(Rect(70, 0, 450, 200));
				(1..5).do{arg i;
					i.postln;
					controls[i].bounds_(Rect(controls[i].bounds.left, 220, controls[i].bounds.width, controls[i].bounds.height));
					assignButtons[i].setBounds(Rect(assignButtons[i].bounds.left, 240, assignButtons[i].bounds.width, assignButtons[i].bounds.height));
				};
				win.bounds_(Rect(win.bounds.left, win.bounds.top, win.bounds.width, 310));
		});*/
		win.refresh;
		buffers.postln;
		currentBuffer = 0;

	}

	killMeSpecial {
		volBus.free;
		buffers.do{arg buffer;
			if(buffer!=nil, {buffer.free;});
		};
	}

	saveExtra {arg saveArray;
		saveArray.add(savePath.asString);
	}

	loadExtra {arg loadArray;
		savePath = loadArray[4];
		savePath.postln;
		if(savePath.size>0,{
			this.loadBuffers(savePath);
		});
	}
}
