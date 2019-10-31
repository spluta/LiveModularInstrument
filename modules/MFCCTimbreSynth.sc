MFCCTimbreSynth_Mod :  MFCCHarmonySynth_Mod {
	var synthGate, startPos, matchRand, matchBus, trigGroup, grainGroup, whichBuf, centerBus, whichBufBus, trigRateBus, trigBus, monoBuf0, monoBuf1, matchPoint, matchPointBus, loudnessBuf, loudnessBus;

	*initClass {
		StartUp.add {
			SynthDef("mfccTimbreSynth_mod", {arg outBus, buffer, startPos, volBus, gate=1, pauseGate=1;
				var play, env, pauseEnv, vol;

				vol = In.kr(volBus);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				play = PlayBuf.ar(2, buffer, 1, 1, startPos, 1);

				Out.ar(outBus,play*vol*env*pauseEnv*vol);

			}).writeDefFile;

			SynthDef("mfccGrainTrigSynth_mod", {arg inBus, whichBufBus, trigBus, trigRateBus, loudnessBus, gate=1, pauseGate=1;
				var trigs, env, pauseEnv;

				//In.kr(whichBufBus).poll;

				Out.kr(loudnessBus, FluidLoudness.kr(In.ar(inBus), 1, 1, 2048, 2048));

				trigs = PanAz.kr(20, Impulse.kr(In.kr(trigRateBus)), In.kr(whichBufBus)/10, 1, 1, 0);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				//Trig1.kr(trigs.poll, 1).poll;
				Out.kr(trigBus, trigs);
			}).writeDefFile;

			SynthDef("mfccGrainSynth_mod", {arg inBus, outBus, buffer0, buffer1, trigBus, trigRateBus, centerBus, loudnessBus, matchPointBus, loudnessBuf, volBus, gate=1, pauseGate=1;
				var out, trig, dur, centerFrame, ampIn, ampOut, ampMult, grainLoudness, loudIn, vol, env, pauseEnv;

				vol = In.kr(volBus);

				loudIn = In.kr(loudnessBus);

				trig = In.kr(trigBus);
				dur = 8/In.kr(trigRateBus);

				centerFrame = In.kr(centerBus);

				out  = TGrains.ar(2, trig, [buffer0, buffer1], 1, (centerFrame/BufSampleRate.kr(buffer0)+(dur/2)), dur, 0, 0.5);

				grainLoudness = Latch.kr(BufRd.kr(1, loudnessBuf, In.kr(matchPointBus), 1, 0), trig);

				ampMult = (loudIn-grainLoudness).dbamp.clip(0.001, 4);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				Out.ar(outBus, out*vol*env*pauseEnv);

			}).writeDefFile;
		}
	}

	initTheRest {
		goalNode = FluidMatch(group.server, goalGroup, mixerToSynthBus.index, matchNodes, ccfBus, mfccBus, 0);

		matchBus = Bus.control(group.server, 40);

		trigGroup = Group.tail(group);
		grainGroup = Group.tail(group);

		trigBus = Bus.control(group.server, 20).set(1);
		trigRateBus = Bus.control(group.server, 1).set(10);

		whichBufBus = Bus.control(group.server, 1).set(0);
		centerBus = Bus.control(group.server, 1).set(0);
		matchPointBus = Bus.control(group.server, 1).set(0);

		loudnessBus = Bus.control(group.server, 1).set(-300);

		matchRand = 0;

		this.makeWindow("MFCCTimbreSynth");

		this.initControlsAndSynths(8);

		synths = List.newClear(22);
		synths.put(21, Synth("mfccGrainTrigSynth_mod", [\inBus, mixerToSynthBus.index, \whichBufBus, whichBufBus, \trigBus, trigBus, \trigRateBus, trigRateBus, \loudnessBus, loudnessBus], trigGroup));

		texts = List.newClear(0);

		texts = ["on", "off", "on/off", "match/rand", "vol"];

		functions = [
			{arg val;
				synths[20].set(\gate, 0);
				//this.getMatch;
				synthGate = 1;
				synths.put(20, Synth("mfccTimbreSynth_mod", [\outBus, outBus, \buffer, buf, \startPos, startPos, \volBus, volBus], playGroup))
			},

			{arg val; if(synthGate==1){synths[20].set(\gate, 0); synthGate=0}},
			{arg val;
				if (val==1){
					synths[20].set(\gate, 0);
					synthGate = 1;
					synths.put(20, Synth("mfccTimbreSynth_mod", [\outBus, outBus, \buffer, buf, \startPos, startPos, \volBus, volBus], playGroup));
				}{
					if(synthGate==1){synths[20].set(\gate, 0); synthGate=0}
				}
			},
			{arg val;
				matchRand = val;
			},
			{arg val;
				volBus.set(val.linlin(0,1,0,8));
			}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false));
		};

		matchFunc = Task({
			inf.do{
				this.getMatch;
				0.05.wait;
			}
		});

		mainFolderField = TextField()
		.action_{arg val;
			this.loadFile(val.value);
		};

		this.initWindow;
	}

	getMatch {
		if(matchRand==0){
			match = goalNode.getMFCCMatch;

			whichBuf = match[0];
			buf = matchNodes[whichBuf].stereoBuf;
			startPos = match[1];
		}{
			whichBuf = matchNodes.size.rand;
			buf = matchNodes[whichBuf].stereoBuf;
			startPos = buf.numFrames.rand;
		};
		matchPoint = (startPos/441).floor;

		centerBus.set(startPos);
		whichBufBus.set(whichBuf);
		matchPointBus.set(matchPoint);
	}

	makeMatchNodes {|mainFolder|
		{
			PathName(mainFolder.fullPath++"soundFiles/").files.collect({|item| item.pathMatch(".wav")}).do{|file, i|
				var mfccPath, loudnessPath;
				file = file[0];
				mfccPath = PathName(PathName(file).pathOnly).parentPath++"analysisFiles/"++PathName(file).fileNameWithoutExtension++"_mfccTree.wav";
				loudnessPath = PathName(PathName(file).pathOnly).parentPath++"analysisFiles/"++PathName(file).fileNameWithoutExtension++"_loudness.wav";
				matchNodes.add(FluidAnalysisNode(group.server, matchGroup, file, ccfBus, mfccBus)
					.mfccTreeBuf_(Buffer.read(group.server, mfccPath))
					.loudnessBuf_(Buffer.read(group.server, loudnessPath));
				);
				group.server.sync;
				synths.put(i, Synth("mfccGrainSynth_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \buffer0, matchNodes[i].stereoBufL, \buffer1, matchNodes[i].stereoBufR, \trigBus, trigBus.index+i, \trigRateBus, trigRateBus, \centerBus, centerBus, \matchPointBus, matchPointBus, \loudnessBuf, matchNodes[i].loudnessBuf, \loudnessBus, loudnessBus, \volBus, volBus], grainGroup));
			};
		}.fork;
	}

}

MFCCTimbreSynth2_Mod : MFCCTimbreSynth_Mod {
	var pretrigGroup, onsetBus;

	*initClass {
		StartUp.add {
			SynthDef("mfccGrainTrigSynth2_mod", {arg inBus, whichBufBus, onsetBus, trigBus, trigRateBus, loudnessBus, gate=1, pauseGate=1;
				var trigs, onsets, rotation;

				Out.kr(loudnessBus, FluidLoudness.kr(In.ar(inBus), 1, 1, 2048, 2048));

				onsets = In.kr(onsetBus);

				//PulseCount.kr(onsets).poll;

				rotation = Latch.kr(In.kr(whichBufBus)/10, onsets);

				trigs = PanAz.kr(20, Impulse.kr(In.kr(trigRateBus).poll), rotation, 1, 1, 0);

				Out.kr(trigBus, trigs);
			}).writeDefFile;

			SynthDef("mfccGrainSynth2_mod", {arg inBus, outBus, buffer0, buffer1, trigBus, trigRateBus, centerBus, loudnessBus, matchPointBus, onsetBus, loudnessBuf, volBus, gate=1, pauseGate=1;
				var out, trig, dur, centerFrame, ampIn, ampOut, ampMult, grainLoudness, loudIn, vol, onsets;

				vol = In.kr(volBus);

				loudIn = In.kr(loudnessBus);

				trig = In.kr(trigBus);
				dur = 4/In.kr(trigRateBus);

				onsets = In.kr(onsetBus);

				centerFrame = Latch.kr(In.kr(centerBus), onsets);

				out  = TGrains.ar(2, trig, [buffer0, buffer1], 1, (centerFrame/BufSampleRate.kr(buffer0)+(dur/2)), dur, 0, 0.5);

				grainLoudness = Latch.kr(BufRd.kr(1, loudnessBuf, In.kr(matchPointBus), 1, 0), trig);

				ampMult = (loudIn-grainLoudness).dbamp.clip(0.001, 4);

				Out.ar(outBus, out*vol);

			}).writeDefFile;

			SynthDef("mfccOnset_mod", {arg inBus, onsetBus, trigRateBus, threshold=0.5, gate=1, pauseGate=1;
				var play, env, pauseEnv, sig, onsets;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);

				sig = In.ar(inBus);

				onsets = FluidOnsetSlice.ar(sig, 0, threshold, windowSize:512);

				Out.kr(trigRateBus, TRand.kr(20, 40, onsets));

				Out.kr(onsetBus, onsets);
			}).writeDefFile
		}
	}

initTheRest {
		goalNode = FluidMatch(group.server, goalGroup, mixerToSynthBus.index, matchNodes, ccfBus, mfccBus, 0);

		matchBus = Bus.control(group.server, 40);

		pretrigGroup = Group.tail(group);
		trigGroup = Group.tail(group);
		grainGroup = Group.tail(group);

		onsetBus = Bus.control(group.server, 1);

		trigBus = Bus.control(group.server, 20).set(1);
		trigRateBus = Bus.control(group.server, 1).set(10);

		whichBufBus = Bus.control(group.server, 1).set(0);
		centerBus = Bus.control(group.server, 1).set(0);
		matchPointBus = Bus.control(group.server, 1).set(0);

		loudnessBus = Bus.control(group.server, 1).set(-300);

		matchRand = 0;

		this.makeWindow("MFCCTimbreSynth2");

		this.initControlsAndSynths(8);

		synths = List.newClear(23);
		synths.put(21, Synth("mfccGrainTrigSynth2_mod", [\inBus, mixerToSynthBus.index, \whichBufBus, whichBufBus, \trigBus, trigBus, \trigRateBus, trigRateBus, \loudnessBus, loudnessBus, \onsetBus, onsetBus], trigGroup));
		synths.put(22, Synth("mfccOnset_mod", [\inBus, mixerToSynthBus.index, \onsetBus, onsetBus, \trigRateBus, trigRateBus, \threshold, 0.5], pretrigGroup));

		texts = List.newClear(0);

		texts = ["on", "off", "on/off", "match/rand", "vol"];

		functions = [
			{arg val;
				synths[20].set(\gate, 0);
				//this.getMatch;
				synthGate = 1;
				synths.put(20, Synth("mfccTimbreSynth_mod", [\outBus, outBus, \buffer, buf, \startPos, startPos, \volBus, volBus], playGroup))
			},

			{arg val; if(synthGate==1){synths[20].set(\gate, 0); synthGate=0}},
			{arg val;
				if (val==1){
					synths[20].set(\gate, 0);
					synthGate = 1;
					synths.put(20, Synth("mfccTimbreSynth_mod", [\outBus, outBus, \buffer, buf, \startPos, startPos, \volBus, volBus], playGroup));
				}{
					if(synthGate==1){synths[20].set(\gate, 0); synthGate=0}
				}
			},
			{arg val;
				matchRand = val;
			},
			{arg val;
				volBus.set(val.linlin(0,1,0,8));
			}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false));
		};

		matchFunc = Task({
			inf.do{
				this.getMatch;
				0.05.wait;
			}
		});

		mainFolderField = TextField()
		.action_{arg val;
			this.loadFile(val.value);
		};

		this.initWindow;
	}

	makeMatchNodes {|mainFolder|
		{
			PathName(mainFolder.fullPath++"soundFiles/").files.collect({|item| item.pathMatch(".wav")}).do{|file, i|
				var mfccPath, loudnessPath;
				file = file[0];
				mfccPath = PathName(PathName(file).pathOnly).parentPath++"analysisFiles/"++PathName(file).fileNameWithoutExtension++"_mfccTree.wav";
				loudnessPath = PathName(PathName(file).pathOnly).parentPath++"analysisFiles/"++PathName(file).fileNameWithoutExtension++"_loudness.wav";
				matchNodes.add(FluidAnalysisNode(group.server, matchGroup, file, ccfBus, mfccBus)
					.mfccTreeBuf_(Buffer.read(group.server, mfccPath))
					.loudnessBuf_(Buffer.read(group.server, loudnessPath));
				);
				group.server.sync;
				synths.put(i, Synth("mfccGrainSynth2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \buffer0, matchNodes[i].stereoBufL, \buffer1, matchNodes[i].stereoBufR, \trigBus, trigBus.index+i, \trigRateBus, trigRateBus, \centerBus, centerBus, \matchPointBus, matchPointBus, \onsetBus, onsetBus, \loudnessBuf, matchNodes[i].loudnessBuf, \loudnessBus, loudnessBus, \volBus, volBus], grainGroup));
			};
		}.fork;
	}

}