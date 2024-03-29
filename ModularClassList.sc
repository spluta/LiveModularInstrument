ModularClassList {
	classvar <>whichArray, <>classArray, classDictionary;

	*new {arg whichArrayIn;
		whichArray = whichArrayIn;
		this.init2;
	}

	*init2 {

		//to do: group these classes into groups and make separate menus for each classification of effect - Sythesis, Reverb, BufPlayers...

		switch(whichArray,
			'normal', {
				classArray = ["GlassSines", "FilterDelays", "PulsatingDelays", "BitCrusher", "TriggerDelays", "OverLapSamples", "LoopBuf", "Combulation", "BuchlaFilters", "BuchlaModelSolo", "ReverbDrone", "ShifterFeedback", "Compander", "Distortion2D", "CycleGripper", "Mixer", "Freeze", "AmpMod", "SignalSwitcher", "LoopMachine", "GrainAge", "GingerMan", "SwoopDown", "EQ", "DistortMono", "GrainFreezeNoise", "TFreeze", "PulseBP", "DownShift", "InterruptDistortion", "GrabNLoop", "HarmDoublerUp", "GrainFreezeDrums", "AmpFollower", "EightDelays2", "Melter", "GVerb", "HarmonicDoubler2", "Cutter", "LongDelay", "Filters", "ShifterX2", "Record", "RingModStereo", "BitInterrupter", "InterruptDelays", "InterruptLoop"/*, "CutterThrough"*/, "SpecDelay", "EnvGen", "FilterGrainsB", "ScaleShifterB", "UpDownSines", "SinArray", "SweepingNoise", "SpaceJunk", "BandPassFreeze", "NoisePulse", "GFNoiseMini", "Mute", "ResonDraw", "TestSine", "SampleMashup", "CrackleSynth", "LargeArcLoops", "SampleBank", "Timer", "LoopMachineOverLap", "Convolution", "FeedbackControl", "DistGrains", "MixerSolo", "MixerSoloMono", "GreatExpectations", "GFNoiseMiniSky", "MuteSky", "LowPass", "VDelayInline", "MantaToMidi"/*, "RageTrombones"*/, "Sampler", "TVFeedback", "FeedbackSynth", "DelayLine", "VST", "SinOsc", "PinkNoise"/*, "MFCCHarmonySynth"*/, "Gain", /*"MFCCTimbreSynth", "MFCCTimbreSynth2", */"BrynHarrison", "StraightLoop2", "PitchShift", "NN_Synths", "KlankFilter", "SpaceInterruption", "SpaceTrigger", "DJMixer", "TinyLoops", "ProtoType", "RingMod2", "LPFInterupter", "AmpMod2", "NessStretchRT", "NessStretchRTBells", "DustAmpMod", "SnareSwitch", "FB100", "FB100B", "Tectonics", "Matrix4GL"].sort;
			},
			'feedback', {classArray = ["Convolution", "CombFilter", "KlankFilter", "KlankFilter2","OscilMidBump", "Compander", "DistortMono", "PinkNoise", "LoopBuf", "AnalysisFilters", "SignalSwitcher", "TVFeedback", "Mixer", "RingModStereo", "LongDelay", "FeedbackControl"].sort},

			'installation', {classArray = ["EQ"]},
			'Matrices', {classArray = ["LoopVidBuf2", "SnareSwitch", "DustAmpMod", "Compander", "BuchlaFilters", "BitCrusher", "Freeze", "Mixer"].sort},
			'wubbels', {
				classArray = ["WubbelsSine", "WubbelsSine2", "WubbelsSine3", "FilterDelays", "AutoTune", "RhythmicDelays", "Mixer", "EQ", "Compander", "LoopBuf", "MincekSine", "Mute", "AutoTuneFake", "OverlapWubbels"].sort;
			},'stocky', {
				classArray = ["Mikrophonie", "PitchShift"].sort;
			},'atdV', {
				classArray = ["GlassSines", "RingMod", "FilterDelays", "PulsatingDelays", "BitCrusher", "TriggerDelays", "OverLapSamples", "LoopBuf", "Combulation", "BuchlaFilters", "BuchlaModelSolo", "ReverbDrone", "ShifterFeedback", "Compander", "MantaBuffers", "Distortion2D", "CycleGripper", "Mixer", "Freeze", "AmpMod", "SignalSwitcher", "SignalSwitcher4", "LoopMachine", "LoopBuf2", "GrainAge", "GingerMan", "SwoopDown", "EQ", "DistortMono", "GrainFreezeNoise", "TFreeze", "PulseBP", "DownShift", "InterruptDistortion", "StraightLoop", "Sand", "GrabNLoop", "AtdV", "FloatShifter", "HarmDoublerUp", "GrainFreezeDrums", "StarDust", "AmpFollower", "StraightDelays", "EightDelays2", "SpecMul", "Melter", "GVerb", "HarmonicDoubler2", "Cutter", "LongDelay", "Filters", "ShifterX2", "Record", "RingModStereo", "InterruptSine", "MiniGripper", "BitInterrupter", "SampleBank", "DecimateGrains"].sort;
			},'lucerneVideo', {
				classArray = ["LucerneVideo", "ShifterX2", "SignalSwitcher"].sort;
			}
		);


		classDictionary = IdentityDictionary.new;

		classArray.do{arg item, i;

			if(item=="Mixer",{
				classDictionary.add('Mixer'->{arg synthGroup, outBus; ModularMainMixer(synthGroup, outBus).init2(2, false)});
			},{
				classDictionary.add(item.asSymbol->(("{arg synthGroup, outBus; "++item++"_Mod(synthGroup, outBus)}").compile.value));
			})
		};


try {	PathName(PathName(NN_Synth_Mod.filenameSymbol.asString).pathOnly).folders.collect{arg folder;
			folder.files.select{arg file;
				file.extension=="sc"
			}
		}.flatten.collect{arg item; item.fileNameWithoutExtension}.do{arg item;
			classDictionary.add((item.asString++"_NNMod").asSymbol->(("{arg synthGroup, outBus; "++item++"_NNMod(synthGroup, outBus)}").compile.value));
		};

		};
	}

	*initModule {arg className, synthGroup, bus;
		var item;
		item = classDictionary[className.asSymbol].value(synthGroup, bus);
		className = className.asString;
		if((className=="SignalSwitcher")||(className=="RingModStereo")||(className=="Convolution")||(className=="AmpFollower")||(className=="TVFeedback")||(className=="DJMixer")||(className=="SpaceInterruption")||(className=="SpaceTrigger")||(className=="NessStretchRT")||(className=="NessStretchRTBells")||(className=="Tectonics"),{
			item.init2(2,false)
		});
		^item
	}

	*initAnalogSynthModule {arg className, synthGroup, localBusses;
		^classDictionary[className.asSymbol].value(synthGroup, localBusses);
	}

	*initNN_Synth{arg className, synthGroup, outBus;
		^classDictionary[className.asSymbol].value(synthGroup, outBus);
	}

	*initMixer {arg group, outBus, numMixers;
		var temp;
		temp = ModularMainMixer(group, outBus, numMixers, false);
		^temp;
	}

	*checkSynthName {arg name;
		if(classArray.indexOfEqual(name)!=nil,{^true},{^false})
	}
}

