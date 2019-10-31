MFCCTimbreSynth_Mod :  TypeOSCModule_Mod {
	var texts, functions, goalGroup, matchGroup, ccfBus, mfccBus, match, buf, vol0, vol1, trigVol, matchNodes, goalNode, synthGates, mainFolderField, playGroup, volBus;

	*initClass {
		StartUp.add {
			SynthDef("mfccTimbreSynth_mod", {arg outBus, buffer0, buffer1, volBus, vol=0, gate=1, pauseGate=1;
				var trig, smalldur, dur, grains, env, pauseEnv, busVol;

				busVol = In.kr(volBus);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				smalldur = Rand(0.05, 1);
				dur = smalldur+0.05;
				trig = Impulse.kr(1/smalldur);
				grains = [TGrains2.ar(2, trig, buffer0, 1, rrand(dur/2, 1.9-(dur/2)), dur, -1, 0.5),
				TGrains2.ar(2, trig, buffer1, 1, rrand(dur/2, 1.9-(dur/2)), dur, 1, 0.5, 0.05, 0.05)];

				Out.ar(outBus,grains*vol*env*pauseEnv*busVol);

			}).writeDefFile
		}
	}

	init {

		goalGroup = Group.tail(group);
		matchGroup = Group.tail(group);
		playGroup = Group.tail(group);

		volBus = Bus.control(group.server);
		volBus.set(0.5);

		ccfBus = Bus.control(group.server, 14);
		mfccBus = Bus.control(group.server, 40);

		this.makeWindow("MFCCTimbreSynth");

		this.makeMixerToSynthBus(1);

		this.initControlsAndSynths(8);

		synths = List.newClear(3);
		synthGates = List.newClear(3);

		matchNodes = List.newClear(0);

		goalNode = FluidMatch(group.server, goalGroup, mixerToSynthBus.index, matchNodes, ccfBus, mfccBus);

		texts = List.newClear(0);

		texts = ["fader1", "fader1z", "fader2", "fader2z", "trig", "trigOff", "trigVol", "folder", "mfcc/ccf"];

		functions = [

			{arg val; vol0 = val; if(synthGates[0]==1){synths[0].set(\vol, val.linlin(0,1,0,1))}},
			{arg val;
				"zee".postln;
				synthGates.put(0, val);
				if(val==1){
					this.getMatch;
					synths[0].set(\gate, 0);
					//group.queryTree;
					synths.put(0, Synth("mfccHarmonySynth_mod", [\outBus, outBus, \buffer0, buf[0], \buffer1, buf[1], \volBus, volBus, \vol, 0], playGroup))
				}{
					synths[0].set(\gate, 0)
				}
			},

			{arg val; vol1 = val; if(synthGates[1]==1){synths[1].set(\vol, val.linlin(0,1,0,1))}},
			{arg val;
				synthGates.put(1, val);
				if(val==1){
					this.getMatch;
					synths[1].set(\gate, 0);
					synths.put(1, Synth("mfccHarmonySynth_mod", [\outBus, outBus, \buffer0, buf[0], \buffer1, buf[1], \volBus, volBus, \vol, 0], playGroup))
				}{
					synths[1].set(\gate, vol1.linlin(0, 1, -1.1, -4))
				}
			},
			{arg val;
				"why".postln;
				this.getMatch;
				synthGates.put(2, 1);
				synths.put(2, Synth("mfccHarmonySynth_mod", [\outBus, outBus, \buffer0, buf[0], \buffer1, buf[1], \volBus, volBus, \vol, 0.8], playGroup))
			},
			{arg val;
				//this.getMatch;
				if(synthGates[2]==1){synths[2].set(\gate, 0)};
				//SystemClock.schedAbs(0.01, {playGroup.freeAll});
				synthGates.put(2,0);
			},
			{arg val;
				//trigVol = val.linlin(0,1,0,1);
				//synths[2].set(\vol, trigVol);
				volBus.set(val.linlin(0,1,0,8));
			}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false));

		};


		mainFolderField = TextField()
			.action_{arg val;
				this.loadFolder(val.value);
				/*Dialog.openPanel({ arg path;
					visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
					samplesFolder = path;
					this.loadFolder(samplesFolder);
					},{
						visibleArray.do{arg item, i; if(item==true,{Window.allWindows[i].visible = true})};
				});*/

		};

		win.layout_(
			VLayout(
				VLayout(*controls.copyRange(0, 6).collect({arg item; item.view})), mainFolderField
			)
		);
		win.layout.spacing_(1).margins_(1!4);
		win.view.maxHeight_(13*17);
		win.view.resizeTo(13*17,13*17);
		win.front;
	}

	getMatch {
		if(synthGates[2]==1){synths[2].set(\gate, 0)};
		synthGates.put(2,0);
		match = goalNode.getMFCCMatch;
		//match.postln;
		buf = matchNodes[match[0]].monoBufs[match[1].asInteger];
		//buf[0].bufnum.postln;
	}

	loadFolder {arg mainFolder;
		"load the samples".postln;
		mainFolder.postln;

		try {
		mainFolder = PathName(mainFolder);
			if(mainFolder.isFolder){
		if(matchNodes.size>0){matchNodes.do{arg item; item.freeAll}};

		matchNodes = List.newClear(0);

		{
			mainFolder.folders.do{|folder|
				var file;

				file = folder.fullPath++folder.folderName++".wav";
				//file.postln;
				matchNodes.add(FluidAnalysisNode(group.server, matchGroup, file, ccfBus, mfccBus).loadSegments.loadAnalysis);
				group.server.sync;
				//1.wait;
				goalNode.matchNodes_(matchNodes);
				matchNodes.do{|item| item.makeCCFAnalysisSynth; item.makeMFCCAnalysisSynth};
			};
			group.server.sync;
			1.0.wait;
			"loading goalNode".postln;
			//goalNode = FluidMatchAndPlay(group.server, goalGroup, inBus, matchNodes, ccfBus, mfccBus);
		}.fork;
			}{"not a folder".postln; controls[7].textField.value_("");}
		}{
			"try failed".postln;
			controls[7].textField.value_("");
		}
	}

	saveExtra {arg saveArray;
		saveArray.add(mainFolderField.string);
	}

	loadExtra {arg extra;
			extra.postln;
		{
			5.wait;
			mainFolderField.valueAction_(extra);
		}.fork(AppClock)
	}


}