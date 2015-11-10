ModularClassList {
	classvar <>whichArray, <>classArray, classDictionary;

	*new {arg whichArrayIn;
		whichArray = whichArrayIn;
		this.init2;
	}

	*init2 {
		("whichArray"+whichArray).postln;

		//to do: group these classes into groups and make separate menus for each classification of effect - Sythesis, Reverb, BufPlayers...

		switch(whichArray,
			'normal', {
				classArray = ["GlassSines", "FilterDelays", "PulsatingDelays", "BitCrusher", "TriggerDelays", "OverLapSamples", "LoopBuf", "Combulation", "BuchlaFilters", "BuchlaModelSolo", "ReverbDrone", "ShifterFeedback", "Compander", "Distortion2D", "CycleGripper", "Mixer", "Freeze", "AmpMod", "SignalSwitcher", "LoopMachine", "GrainAge", "GingerMan", "SwoopDown", "EQ", "DistortMono", "GrainFreezeNoise", "TFreeze", "PulseBP", "DownShift", "InterruptDistortion", "GrabNLoop", "HarmDoublerUp", "GrainFreezeDrums", "AmpFollower", "EightDelays2", "Melter", "GVerb", "HarmonicDoubler2", "Cutter", "LongDelay", "Filters", "ShifterX2", "Record", "RingModStereo", "BitInterrupter", "InterruptDelays", "InterruptLoop", "CutterThrough", "EQmini", "SpecDelay", "EnvGen", "FilterGrainsB", "ScaleShifterB", "UpDownSines", "SinArray", "SweepingNoise", "SpaceJunk", "BandPassFreeze", "NoisePulse", "GFNoiseMini", "Mute", "ResonDraw", "TestSine", "SampleMashup", "CrackleSynth", "LargeArcLoops", "SampleBank", "Timer", "SampleLoops", "LoopMachineOverLap", "Convolution", "FeedbackControl", "DistGrains", "MixerSolo", "MincekSine", "MantaBuffers", "GreatExpectations"].sort;
			},
			'feedback', {classArray = ["Convolution", "CombFilter", "KlankFilter", "KlankFilter2","OscilMidBump", "Compander", "DistortMono", "PinkNoise", "LoopBuf", "AnalysisFilters", "SignalSwitcher", "TVFeedback", "Mixer", "RingModStereo", "LongDelay"].sort},

			'installation', {classArray = ["EQ"]},
			'Matrices', {classArray = ["LoopVidBuf2", "SnareSwitch", "AmpMod", "Compander", "BuchlaFilters", "BitCrusher", "Freeze"].sort},
			'wubbels', {
				classArray = ["WubbelsSine", "WubbelsSine2", "WubbelsSine3", "FilterDelays", "AutoTune", "RhythmicDelays", "Mixer", "EQ", "Compander", "LoopBuf", "MincekSine", "Mute", "AutoTuneFake"].sort;
			},'neuwirth', {
				classArray = ["NeuwirthSine", "RingModStereo", "Mixer", "EQ", "Compander"].sort;
			},'stocky', {
				classArray = ["Mikrophonie", "PitchShift"].sort;
			},'atdV', {
				classArray = ["GlassSines", "RingMod", "FilterDelays", "PulsatingDelays", "BitCrusher", "TriggerDelays", "OverLapSamples", "LoopBuf", "Combulation", "BuchlaFilters", "BuchlaModelSolo", "ReverbDrone", "ShifterFeedback", "Compander", "MantaBuffers", "Distortion2D", "CycleGripper", "Mixer", "Freeze", "AmpMod", "SignalSwitcher", "SignalSwitcher4", "LoopMachine", "LoopBuf2", "GrainAge", "GingerMan", "SwoopDown", "EQ", "DistortMono", "GrainFreezeNoise", "TFreeze", "PulseBP", "DownShift", "InterruptDistortion", "StraightLoop", "Sand", "GrabNLoop", "AtdV", "FloatShifter", "HarmDoublerUp", "GrainFreezeDrums", "StarDust", "AmpFollower", "StraightDelays", "EightDelays2", "SpecMul", "Melter", "GVerb", "HarmonicDoubler2", "Cutter", "LongDelay", "Filters", "ShifterX2", "Record", "RingModStereo", "InterruptSine", "MiniGripper", "BitInterrupter", "SampleBank", "DecimateGrains"].sort;
			},'lucerneVideo', {
				classArray = ["LucerneVideo", "ShifterX2", "SignalSwitcher"].sort;
			}
		);


		/*, "LoopVidBuf2", "SnareSwitch", "PedalRouter", "PitchedFeedback", "AmpInterrupter", "StraightLoop2", "HarmonicDoubler, "PrettyChordConv", "SnowDown", "SpaceDelays"*/
		//classArray = classArray.addFirst("nil");

		classDictionary = IdentityDictionary.new;
		classDictionary.add('WubbelsSine'->{arg synthGroup, outBus, setups; WubbelsSine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('WubbelsSine2'->{arg synthGroup, outBus, setups; WubbelsSine2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GlassSines'->{arg synthGroup, outBus, setups; GlassSines_Mod(synthGroup, outBus, setups)});
		classDictionary.add('RingMod'->{arg synthGroup, outBus, setups; RingMod_Mod(synthGroup, outBus, setups)});
		classDictionary.add('FilterDelays'->{arg synthGroup, outBus, setups; FilterDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('PulsatingDelays'->{arg synthGroup, outBus, setups; PulsatingDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('BitCrusher'->{arg synthGroup, outBus, setups; BitCrusher_Mod(synthGroup, outBus, setups)});
		classDictionary.add('TriggerDelays'->{arg synthGroup, outBus, setups; TriggerDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('OverLapSamples'->{arg synthGroup, outBus, setups; OverLapSamples_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LoopBuf'->{arg synthGroup, outBus, setups; LoopBuf_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Combulation'->{arg synthGroup, outBus, setups; Combulation_Mod(synthGroup, outBus, setups)});
		classDictionary.add('BuchlaModelSolo'->{arg synthGroup, outBus, setups; BuchlaModelSolo_Mod(synthGroup, outBus, setups)});
		classDictionary.add('BuchlaFilters'->{arg synthGroup, outBus, setups; BuchlaFilters_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ReverbDrone'->{arg synthGroup, outBus, setups; ReverbDrone_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ShifterFeedback'->{arg synthGroup, outBus, setups; ShifterFeedback_Mod(synthGroup, outBus, setups)});
		classDictionary.add('EightDelays'->{arg synthGroup, outBus, setups; EightDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('EightDelays2'->{arg synthGroup, outBus, setups; EightDelays2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Compander'->{arg synthGroup, outBus, setups; Compander_Mod(synthGroup, outBus, setups)});
		classDictionary.add('MantaBuffers'->{arg synthGroup, outBus, setups; MantaBuffers_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Distortion2D'->{arg synthGroup, outBus, setups; Distortion2D_Mod(synthGroup, outBus, setups)});
		classDictionary.add('CycleGripper'->{arg synthGroup, outBus, setups; CycleGripper_Mod(synthGroup, outBus, setups)});
		classDictionary.add('CycleGripper2'->{arg synthGroup, outBus, setups; CycleGripper2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Freeze'->{arg synthGroup, outBus, setups; Freeze_Mod(synthGroup, outBus, setups)});
		classDictionary.add('TFreeze'->{arg synthGroup, outBus, setups; TFreeze_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AmpMod'->{arg synthGroup, outBus, setups; AmpMod_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LoopVidBuf'->{arg synthGroup, outBus, setups; LoopVidBuf_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LoopMachine'->{arg synthGroup, outBus, setups; LoopMachine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LoopBuf2'->{arg synthGroup, outBus, setups; LoopBuf2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GrainAge'->{arg synthGroup, outBus, setups; GrainAge_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GingerMan'->{arg synthGroup, outBus, setups; GingerMan_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SwoopDown'->{arg synthGroup, outBus, setups; SwoopDown_Mod(synthGroup, outBus, setups)});
		classDictionary.add('EQ'->{arg synthGroup, outBus, setups; EQ_Mod(synthGroup, outBus, setups)});
		classDictionary.add('DistortMono'->{arg synthGroup, outBus, setups; DistortMono_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GrainFreezeNoise'->{arg synthGroup, outBus, setups; GrainFreezeNoise_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SignalSwitcher'->{arg synthGroup, outBus, setups;SignalSwitcher_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SignalSwitcher4'->{arg synthGroup, outBus, setups; SignalSwitcher4_Mod(synthGroup, outBus, setups)});
		classDictionary.add('PulseBP'->{arg synthGroup, outBus, setups; PulseBP_Mod(synthGroup, outBus, setups)});
		classDictionary.add('DownShift'->{arg synthGroup, outBus, setups; DownShift_Mod(synthGroup, outBus, setups)});
		classDictionary.add('StraightLoop'->{arg synthGroup, outBus, setups; StraightLoop_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Sand'->{arg synthGroup, outBus, setups; Sand_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GrabNLoop'->{arg synthGroup, outBus, setups; GrabNLoop_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AtdV'->{arg synthGroup, outBus, setups; AtdV_Mod(synthGroup, outBus, setups)});
		classDictionary.add('InterruptDistortion'->{arg synthGroup, outBus, setups; InterruptDistortion_Mod(synthGroup, outBus, setups)});
		classDictionary.add('FloatShifter'->{arg synthGroup, outBus, setups; FloatShifter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('HarmDoublerUp'->{arg synthGroup, outBus, setups; HarmDoublerUp_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GrainFreezeDrums'->{arg synthGroup, outBus, setups; GrainFreezeDrums_Mod(synthGroup, outBus, setups)});
		classDictionary.add('StarDust'->{arg synthGroup, outBus, setups; StarDust_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AmpFollower'->{arg synthGroup, outBus, setups; AmpFollower_Mod(synthGroup, outBus, setups)});
		classDictionary.add('StraightDelays'->{arg synthGroup, outBus, setups; StraightDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SpecMul'->{arg synthGroup, outBus, setups; SpecMul_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Melter'->{arg synthGroup, outBus, setups; Melter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GVerb'->{arg synthGroup, outBus, setups; GVerb_Mod(synthGroup, outBus, setups)});
		classDictionary.add('HarmonicDoubler2'->{arg synthGroup, outBus, setups; HarmonicDoubler2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Cutter'->{arg synthGroup, outBus, setups; Cutter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LongDelay'->{arg synthGroup, outBus, setups; LongDelay_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AmpInterrupter'->{arg synthGroup, outBus, setups; AmpInterrupter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Filters'->{arg synthGroup, outBus, setups; Filters_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ShifterX2'->{arg synthGroup, outBus, setups; ShifterX2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Record'->{arg synthGroup, outBus, setups; Record_Mod(synthGroup, outBus, setups)});
		classDictionary.add('RingModStereo'->{arg synthGroup, outBus, setups; RingModStereo_Mod(synthGroup, outBus, setups)});
		classDictionary.add('InterruptSine'->{arg synthGroup, outBus, setups; InterruptSine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('MiniGripper'->{arg synthGroup, outBus, setups; MiniGripper_Mod(synthGroup, outBus, setups)});
		classDictionary.add('BitInterrupter'->{arg synthGroup, outBus, setups; BitInterrupter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SampleBank'->{arg synthGroup, outBus, setups; SampleBank_Mod(synthGroup, outBus, setups)});
		classDictionary.add('DecimateGrains'->{arg synthGroup, outBus, setups; DecimateGrains_Mod(synthGroup, outBus, setups)});

		classDictionary.add('InterruptLoop'->{arg synthGroup, outBus, setups; InterruptLoop_Mod(synthGroup, outBus, setups)});
		classDictionary.add('InterruptDelays'->{arg synthGroup, outBus, setups; InterruptDelays_Mod(synthGroup, outBus, setups)});

		classDictionary.add('FeedbackControl'->{arg synthGroup, outBus, setups; FeedbackControl_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AutoTune'->{arg synthGroup, outBus, setups; AutoTune_Mod(synthGroup, outBus, setups)});
		classDictionary.add('RhythmicDelays'->{arg synthGroup, outBus, setups; RhythmicDelays_Mod(synthGroup, outBus, setups)});
		classDictionary.add('WubbelsSine3'->{arg synthGroup, outBus, setups; WubbelsSine3_Mod(synthGroup, outBus, setups)});
		classDictionary.add('TriggerSines'->{arg synthGroup, outBus, setups; TriggerSines_Mod(synthGroup, outBus, setups)});
		classDictionary.add('DelayRingMod'->{arg synthGroup, outBus, setups; DelayRingMod_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SnareSine'->{arg synthGroup, outBus, setups; SnareSine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('CutterThrough'->{arg synthGroup, outBus, setups; CutterThrough_Mod(synthGroup, outBus, setups)});
		classDictionary.add('NeuwirthSine'->{arg synthGroup, outBus, setups; NeuwirthSine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('EQmini'->{arg synthGroup, outBus, setups; EQmini_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SpecDelay'->{arg synthGroup, outBus, setups; SpecDelay_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LowSines'->{arg synthGroup, outBus, setups; LowSines_Mod(synthGroup, outBus, setups)});
		classDictionary.add('EnvGen'->{arg synthGroup, outBus, setups; EnvGen_Mod(synthGroup, outBus, setups)});
		classDictionary.add('DrumBombs'->{arg synthGroup, outBus, setups; DrumBombs_Mod(synthGroup, outBus, setups)});
		classDictionary.add('GrainInterrupt'->{arg synthGroup, outBus, setups; GrainInterrupt_Mod(synthGroup, outBus, setups)});

		classDictionary.add('SampleMashup'->{arg synthGroup, outBus, setups; SampleMashup_Mod(synthGroup, outBus, setups)});
		classDictionary.add('FilterGrains'->{arg synthGroup, outBus, setups; FilterGrains_Mod(synthGroup, outBus, setups)});
		classDictionary.add('FilterGrainsB'->{arg synthGroup, outBus, setups; FilterGrainsB_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ScaleShifter'->{arg synthGroup, outBus, setups; ScaleShifter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ScaleShifterB'->{arg synthGroup, outBus, setups; ScaleShifterB_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ScaleShifterC'->{arg synthGroup, outBus, setups; ScaleShifterC_Mod(synthGroup, outBus, setups)});
		classDictionary.add('UpDownSines'->{arg synthGroup, outBus, setups; UpDownSines_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ReverseMachine'->{arg synthGroup, outBus, setups; ReverseMachine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SinArray'->{arg synthGroup, outBus, setups; SinArray_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SweepingNoise'->{arg synthGroup, outBus, setups; SweepingNoise_Mod(synthGroup, outBus, setups)});
		classDictionary.add('SpaceJunk'->{arg synthGroup, outBus, setups; SpaceJunk_Mod(synthGroup, outBus, setups)});
		classDictionary.add('MonoRouter'->{arg synthGroup, outBus, setups; MonoRouter_Mod(synthGroup, outBus, setups)});

		classDictionary.add('BandPassFreeze'->{arg synthGroup, outBus, setups; BandPassFreeze_Mod(synthGroup, outBus, setups)});

		classDictionary.add('NoisePulse'->{arg synthGroup, outBus, setups; NoisePulse_Mod(synthGroup, outBus, setups)});

		classDictionary.add('GFNoiseMini'->{arg synthGroup, outBus, setups; GFNoiseMini_Mod(synthGroup, outBus, setups)});
		classDictionary.add('PESequencer'->{arg synthGroup, outBus, setups; PESequencer_Mod(synthGroup, outBus, setups)});
		classDictionary.add('PESynthTone'->{arg synthGroup, outBus, setups; PESynthTone_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Mute'->{arg synthGroup, outBus, setups; Mute_Mod(synthGroup, outBus, setups)});
		classDictionary.add('ResonDraw'->{arg synthGroup, outBus, setups; ResonDraw_Mod(synthGroup, outBus, setups)});
		classDictionary.add('TestSine'->{arg synthGroup, outBus, setups; TestSine_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Convolution'->{arg synthGroup, outBus, setups; Convolution_Mod(synthGroup, outBus, setups)});
		classDictionary.add('KlankFilter'->{arg synthGroup, outBus, setups; KlankFilter_Mod(synthGroup, outBus, setups)});
		classDictionary.add('KlankFilter2'->{arg synthGroup, outBus, setups; KlankFilter2_Mod(synthGroup, outBus, setups)});

		classDictionary.add('AnalResyn2'->{arg synthGroup, outBus, setups; AnalResyn2_Mod(synthGroup, outBus, setups)});
		classDictionary.add('AnalysisFilters'->{arg synthGroup, outBus, setups; AnalysisFilters_Mod(synthGroup, outBus, setups)});
		classDictionary.add('CrackleSynth'->{arg synthGroup, outBus, setups; CrackleSynth_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LargeArcLoops'->{arg synthGroup, outBus, setups; LargeArcLoops_Mod(synthGroup, outBus, setups)});
		classDictionary.add('Mikrophonie'->{arg synthGroup, outBus, setups; Mikrophonie_Mod(synthGroup, outBus, setups)});
		classDictionary.add('PitchShift'->{arg synthGroup, outBus, setups; PitchShift_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LucerneVideo'->{arg synthGroup, outBus, setups; LucerneVideo_Mod(synthGroup, outBus, setups)});


		classDictionary.add('AblingerSine'->{arg synthGroup, outBus, setups; AblingerSine_Mod(synthGroup, outBus, setups)});

		classDictionary.add('PlutaSine'->{arg synthGroup, outBus, setups; PlutaSine_Mod(synthGroup, outBus, setups)});

		classDictionary.add('Timer'->{arg synthGroup, outBus, setups; Timer_Mod(synthGroup, outBus, setups)});

		classDictionary.add('MincekSine'->{arg synthGroup, outBus, setups; MincekSine_Mod(synthGroup, outBus, setups)});


		classDictionary.add('TVFeedback'->{arg synthGroup, outBus, setups; TVFeedback_Mod(synthGroup, outBus, setups)});

		classDictionary.add('SampleLoops'->{arg synthGroup, outBus, setups; SampleLoops_Mod(synthGroup, outBus, setups)});

		classDictionary.add('AutoTuneFake'->{arg synthGroup, outBus, setups; AutoTuneFake_Mod(synthGroup, outBus, setups)});
classDictionary.add('LoopMachineOverLap'->{arg synthGroup, outBus, setups; LoopMachineOverLap_Mod(synthGroup, outBus, setups)});
		//Matrices

		classDictionary.add('SnareSwitch'->{arg synthGroup, outBus, setups; SnareSwitch_Mod(synthGroup, outBus, setups)});
		classDictionary.add('LoopVidBuf2'->{arg synthGroup, outBus, setups; LoopVidBuf2_Mod(synthGroup, outBus, setups)});

		classDictionary.add('DistGrains'->{arg synthGroup, outBus, setups; DistGrains_Mod(synthGroup, outBus, setups)});
		classDictionary.add('MixerSolo'->{arg synthGroup, outBus, setups; MixerSolo_Mod(synthGroup, outBus, setups)});

		classDictionary.add('MantaBuffers'->{arg synthGroup, outBus, setups; MantaBuffers_Mod(synthGroup, outBus, setups)});

		classDictionary.add('GreatExpectations'->{arg synthGroup, outBus, setups; GreatExpectations_Mod(synthGroup, outBus, setups)});

	}

	*initModule {arg className, synthGroup, bus, setups;
		"initModule".postln;
		^classDictionary[className.asSymbol].value(synthGroup, bus, setups);
	}

	*initMixer {arg group, outBus, name, numMixers;
		var temp;
		"initMixer".postln;
		//location.postln;
		temp = ModularMainMixer(group);
		temp.init2(outBus, name, numMixers);
		^temp;
	}

	*checkSynthName {arg name;
		if(classArray.indexOfEqual(name)!=nil,{^true},{^false})
	}
}