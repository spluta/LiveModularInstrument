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
				classArray = ["GlassSines", "FilterDelays", "PulsatingDelays", "BitCrusher", "TriggerDelays", "OverLapSamples", "LoopBuf", "Combulation", "BuchlaFilters", "BuchlaModelSolo", "ReverbDrone", "ShifterFeedback", "Compander", "Distortion2D", "CycleGripper", "Mixer", "Freeze", "AmpMod", "SignalSwitcher", "LoopMachine", "GrainAge", "GingerMan", "SwoopDown", "EQ", "DistortMono", "GrainFreezeNoise", "TFreeze", "PulseBP", "DownShift", "InterruptDistortion", "GrabNLoop", "HarmDoublerUp", "GrainFreezeDrums", "AmpFollower", "EightDelays2", "Melter", "GVerb", "HarmonicDoubler2", "Cutter", "LongDelay", "Filters", "ShifterX2", "Record", "RingModStereo", "BitInterrupter", "InterruptDelays", "InterruptLoop", "CutterThrough", "EQmini", "SpecDelay", "EnvGen", "FilterGrainsB", "ScaleShifterB", "UpDownSines", "SinArray", "SweepingNoise", "SpaceJunk", "BandPassFreeze", "NoisePulse", "GFNoiseMini", "Mute", "ResonDraw", "TestSine", "SampleMashup", "CrackleSynth", "LargeArcLoops", "SampleBank", "Timer", "SampleLoops", "LoopMachineOverLap", "Convolution", "FeedbackControl", "DistGrains", "MixerSolo", "MixerSoloMono", "MincekSine", "MantaBuffers", "GreatExpectations", "GFNoiseMiniSky", "MuteSky", "LowPass", "AnalogSynth", "VDelayInline", "MantaToMidi", "RageTrombones", "SynthStack", "Sampler", "TVFeedback", "FeedbackSynth"].sort;
			},
			'feedback', {classArray = ["Convolution", "CombFilter", "KlankFilter", "KlankFilter2","OscilMidBump", "Compander", "DistortMono", "PinkNoise", "LoopBuf", "AnalysisFilters", "SignalSwitcher", "TVFeedback", "Mixer", "RingModStereo", "LongDelay", "FeedbackControl"].sort},

			'installation', {classArray = ["EQ"]},
			'Matrices', {classArray = ["LoopVidBuf2", "SnareSwitch", "AmpMod", "Compander", "BuchlaFilters", "BitCrusher", "Freeze", "Mixer"].sort},
			'wubbels', {
				classArray = ["WubbelsSine", "WubbelsSine2", "WubbelsSine3", "FilterDelays", "AutoTune", "RhythmicDelays", "Mixer", "EQ", "Compander", "LoopBuf", "MincekSine", "Mute", "AutoTuneFake", "OverlapWubbels"].sort;
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
		classDictionary.add('WubbelsSine'->{arg synthGroup, outBus; WubbelsSine_Mod(synthGroup, outBus)});
		classDictionary.add('WubbelsSine2'->{arg synthGroup, outBus; WubbelsSine2_Mod(synthGroup, outBus)});
		classDictionary.add('GlassSines'->{arg synthGroup, outBus; GlassSines_Mod(synthGroup, outBus)});
		classDictionary.add('RingMod'->{arg synthGroup, outBus; RingMod_Mod(synthGroup, outBus)});
		classDictionary.add('FilterDelays'->{arg synthGroup, outBus; FilterDelays_Mod(synthGroup, outBus)});
		classDictionary.add('PulsatingDelays'->{arg synthGroup, outBus; PulsatingDelays_Mod(synthGroup, outBus)});
		classDictionary.add('BitCrusher'->{arg synthGroup, outBus; BitCrusher_Mod(synthGroup, outBus)});
		classDictionary.add('TriggerDelays'->{arg synthGroup, outBus; TriggerDelays_Mod(synthGroup, outBus)});
		classDictionary.add('OverLapSamples'->{arg synthGroup, outBus; OverLapSamples_Mod(synthGroup, outBus)});
		classDictionary.add('LoopBuf'->{arg synthGroup, outBus; LoopBuf_Mod(synthGroup, outBus)});
		classDictionary.add('Combulation'->{arg synthGroup, outBus; Combulation_Mod(synthGroup, outBus)});
		classDictionary.add('BuchlaModelSolo'->{arg synthGroup, outBus; BuchlaModelSolo_Mod(synthGroup, outBus)});
		classDictionary.add('BuchlaFilters'->{arg synthGroup, outBus; BuchlaFilters_Mod(synthGroup, outBus)});
		classDictionary.add('ReverbDrone'->{arg synthGroup, outBus; ReverbDrone_Mod(synthGroup, outBus)});
		classDictionary.add('ShifterFeedback'->{arg synthGroup, outBus; ShifterFeedback_Mod(synthGroup, outBus)});
		classDictionary.add('EightDelays'->{arg synthGroup, outBus; EightDelays_Mod(synthGroup, outBus)});
		classDictionary.add('EightDelays2'->{arg synthGroup, outBus; EightDelays2_Mod(synthGroup, outBus)});
		classDictionary.add('Compander'->{arg synthGroup, outBus; Compander_Mod(synthGroup, outBus)});
		classDictionary.add('MantaBuffers'->{arg synthGroup, outBus; MantaBuffers_Mod(synthGroup, outBus)});
		classDictionary.add('Distortion2D'->{arg synthGroup, outBus; Distortion2D_Mod(synthGroup, outBus)});
		classDictionary.add('CycleGripper'->{arg synthGroup, outBus; CycleGripper_Mod(synthGroup, outBus)});
		classDictionary.add('CycleGripper2'->{arg synthGroup, outBus; CycleGripper2_Mod(synthGroup, outBus)});
		classDictionary.add('Freeze'->{arg synthGroup, outBus; Freeze_Mod(synthGroup, outBus)});
		classDictionary.add('TFreeze'->{arg synthGroup, outBus; TFreeze_Mod(synthGroup, outBus)});
		classDictionary.add('AmpMod'->{arg synthGroup, outBus; AmpMod_Mod(synthGroup, outBus)});
		classDictionary.add('LoopVidBuf'->{arg synthGroup, outBus; LoopVidBuf_Mod(synthGroup, outBus)});
		classDictionary.add('LoopMachine'->{arg synthGroup, outBus; LoopMachine_Mod(synthGroup, outBus)});
		classDictionary.add('LoopBuf2'->{arg synthGroup, outBus; LoopBuf2_Mod(synthGroup, outBus)});
		classDictionary.add('GrainAge'->{arg synthGroup, outBus; GrainAge_Mod(synthGroup, outBus)});
		classDictionary.add('GingerMan'->{arg synthGroup, outBus; GingerMan_Mod(synthGroup, outBus)});
		classDictionary.add('SwoopDown'->{arg synthGroup, outBus; SwoopDown_Mod(synthGroup, outBus)});
		classDictionary.add('EQ'->{arg synthGroup, outBus; EQ_Mod(synthGroup, outBus)});
		classDictionary.add('DistortMono'->{arg synthGroup, outBus; DistortMono_Mod(synthGroup, outBus)});
		classDictionary.add('GrainFreezeNoise'->{arg synthGroup, outBus; GrainFreezeNoise_Mod(synthGroup, outBus)});
		classDictionary.add('PulseBP'->{arg synthGroup, outBus; PulseBP_Mod(synthGroup, outBus)});
		classDictionary.add('DownShift'->{arg synthGroup, outBus; DownShift_Mod(synthGroup, outBus)});
		classDictionary.add('StraightLoop'->{arg synthGroup, outBus; StraightLoop_Mod(synthGroup, outBus)});
		classDictionary.add('Sand'->{arg synthGroup, outBus; Sand_Mod(synthGroup, outBus)});
		classDictionary.add('GrabNLoop'->{arg synthGroup, outBus; GrabNLoop_Mod(synthGroup, outBus)});
		classDictionary.add('AtdV'->{arg synthGroup, outBus; AtdV_Mod(synthGroup, outBus)});
		classDictionary.add('InterruptDistortion'->{arg synthGroup, outBus; InterruptDistortion_Mod(synthGroup, outBus)});
		classDictionary.add('FloatShifter'->{arg synthGroup, outBus; FloatShifter_Mod(synthGroup, outBus)});
		classDictionary.add('HarmDoublerUp'->{arg synthGroup, outBus; HarmDoublerUp_Mod(synthGroup, outBus)});
		classDictionary.add('GrainFreezeDrums'->{arg synthGroup, outBus; GrainFreezeDrums_Mod(synthGroup, outBus)});
		classDictionary.add('StarDust'->{arg synthGroup, outBus; StarDust_Mod(synthGroup, outBus)});

		classDictionary.add('StraightDelays'->{arg synthGroup, outBus; StraightDelays_Mod(synthGroup, outBus)});
		classDictionary.add('SpecMul'->{arg synthGroup, outBus; SpecMul_Mod(synthGroup, outBus)});
		classDictionary.add('Melter'->{arg synthGroup, outBus; Melter_Mod(synthGroup, outBus)});
		classDictionary.add('GVerb'->{arg synthGroup, outBus; GVerb_Mod(synthGroup, outBus)});
		classDictionary.add('HarmonicDoubler2'->{arg synthGroup, outBus; HarmonicDoubler2_Mod(synthGroup, outBus)});
		classDictionary.add('Cutter'->{arg synthGroup, outBus; Cutter_Mod(synthGroup, outBus)});
		classDictionary.add('LongDelay'->{arg synthGroup, outBus; LongDelay_Mod(synthGroup, outBus)});
		classDictionary.add('AmpInterrupter'->{arg synthGroup, outBus; AmpInterrupter_Mod(synthGroup, outBus)});
		classDictionary.add('Filters'->{arg synthGroup, outBus; Filters_Mod(synthGroup, outBus)});
		classDictionary.add('ShifterX2'->{arg synthGroup, outBus; ShifterX2_Mod(synthGroup, outBus)});
		classDictionary.add('Record'->{arg synthGroup, outBus; Record_Mod(synthGroup, outBus)});
		classDictionary.add('RingModStereo'->{arg synthGroup, outBus; RingModStereo_Mod(synthGroup, outBus)});
		classDictionary.add('InterruptSine'->{arg synthGroup, outBus; InterruptSine_Mod(synthGroup, outBus)});
		classDictionary.add('MiniGripper'->{arg synthGroup, outBus; MiniGripper_Mod(synthGroup, outBus)});
		classDictionary.add('BitInterrupter'->{arg synthGroup, outBus; BitInterrupter_Mod(synthGroup, outBus)});
		classDictionary.add('SampleBank'->{arg synthGroup, outBus; SampleBank_Mod(synthGroup, outBus)});
		classDictionary.add('DecimateGrains'->{arg synthGroup, outBus; DecimateGrains_Mod(synthGroup, outBus)});

		classDictionary.add('InterruptLoop'->{arg synthGroup, outBus; InterruptLoop_Mod(synthGroup, outBus)});
		classDictionary.add('InterruptDelays'->{arg synthGroup, outBus; InterruptDelays_Mod(synthGroup, outBus)});

		classDictionary.add('FeedbackControl'->{arg synthGroup, outBus; FeedbackControl_Mod(synthGroup, outBus)});
		classDictionary.add('AutoTune'->{arg synthGroup, outBus; AutoTune_Mod(synthGroup, outBus)});
		classDictionary.add('RhythmicDelays'->{arg synthGroup, outBus; RhythmicDelays_Mod(synthGroup, outBus)});
		classDictionary.add('WubbelsSine3'->{arg synthGroup, outBus; WubbelsSine3_Mod(synthGroup, outBus)});
		classDictionary.add('TriggerSines'->{arg synthGroup, outBus; TriggerSines_Mod(synthGroup, outBus)});
		classDictionary.add('DelayRingMod'->{arg synthGroup, outBus; DelayRingMod_Mod(synthGroup, outBus)});
		classDictionary.add('SnareSine'->{arg synthGroup, outBus; SnareSine_Mod(synthGroup, outBus)});
		classDictionary.add('CutterThrough'->{arg synthGroup, outBus; CutterThrough_Mod(synthGroup, outBus)});
		classDictionary.add('NeuwirthSine'->{arg synthGroup, outBus; NeuwirthSine_Mod(synthGroup, outBus)});
		classDictionary.add('EQmini'->{arg synthGroup, outBus; EQmini_Mod(synthGroup, outBus)});
		classDictionary.add('SpecDelay'->{arg synthGroup, outBus; SpecDelay_Mod(synthGroup, outBus)});
		classDictionary.add('LowSines'->{arg synthGroup, outBus; LowSines_Mod(synthGroup, outBus)});
		classDictionary.add('EnvGen'->{arg synthGroup, outBus; EnvGen_Mod(synthGroup, outBus)});
		classDictionary.add('DrumBombs'->{arg synthGroup, outBus; DrumBombs_Mod(synthGroup, outBus)});
		classDictionary.add('GrainInterrupt'->{arg synthGroup, outBus; GrainInterrupt_Mod(synthGroup, outBus)});

		classDictionary.add('SampleMashup'->{arg synthGroup, outBus; SampleMashup_Mod(synthGroup, outBus)});
		classDictionary.add('FilterGrains'->{arg synthGroup, outBus; FilterGrains_Mod(synthGroup, outBus)});
		classDictionary.add('FilterGrainsB'->{arg synthGroup, outBus; FilterGrainsB_Mod(synthGroup, outBus)});
		classDictionary.add('ScaleShifter'->{arg synthGroup, outBus; ScaleShifter_Mod(synthGroup, outBus)});
		classDictionary.add('ScaleShifterB'->{arg synthGroup, outBus; ScaleShifterB_Mod(synthGroup, outBus)});
		classDictionary.add('ScaleShifterC'->{arg synthGroup, outBus; ScaleShifterC_Mod(synthGroup, outBus)});
		classDictionary.add('UpDownSines'->{arg synthGroup, outBus; UpDownSines_Mod(synthGroup, outBus)});
		classDictionary.add('ReverseMachine'->{arg synthGroup, outBus; ReverseMachine_Mod(synthGroup, outBus)});
		classDictionary.add('SinArray'->{arg synthGroup, outBus; SinArray_Mod(synthGroup, outBus)});
		classDictionary.add('SweepingNoise'->{arg synthGroup, outBus; SweepingNoise_Mod(synthGroup, outBus)});
		classDictionary.add('SpaceJunk'->{arg synthGroup, outBus; SpaceJunk_Mod(synthGroup, outBus)});
		classDictionary.add('MonoRouter'->{arg synthGroup, outBus; MonoRouter_Mod(synthGroup, outBus)});

		classDictionary.add('BandPassFreeze'->{arg synthGroup, outBus; BandPassFreeze_Mod(synthGroup, outBus)});

		classDictionary.add('NoisePulse'->{arg synthGroup, outBus; NoisePulse_Mod(synthGroup, outBus)});

		classDictionary.add('GFNoiseMini'->{arg synthGroup, outBus; GFNoiseMini_Mod(synthGroup, outBus)});
		classDictionary.add('PESequencer'->{arg synthGroup, outBus; PESequencer_Mod(synthGroup, outBus)});
		classDictionary.add('PESynthTone'->{arg synthGroup, outBus; PESynthTone_Mod(synthGroup, outBus)});
		classDictionary.add('Mute'->{arg synthGroup, outBus; Mute_Mod(synthGroup, outBus)});
		classDictionary.add('ResonDraw'->{arg synthGroup, outBus; ResonDraw_Mod(synthGroup, outBus)});
		classDictionary.add('TestSine'->{arg synthGroup, outBus; TestSine_Mod(synthGroup, outBus)});

		classDictionary.add('KlankFilter'->{arg synthGroup, outBus; KlankFilter_Mod(synthGroup, outBus)});
		classDictionary.add('KlankFilter2'->{arg synthGroup, outBus; KlankFilter2_Mod(synthGroup, outBus)});

		classDictionary.add('AnalResyn2'->{arg synthGroup, outBus; AnalResyn2_Mod(synthGroup, outBus)});
		classDictionary.add('AnalysisFilters'->{arg synthGroup, outBus; AnalysisFilters_Mod(synthGroup, outBus)});
		classDictionary.add('CrackleSynth'->{arg synthGroup, outBus; CrackleSynth_Mod(synthGroup, outBus)});
		classDictionary.add('LargeArcLoops'->{arg synthGroup, outBus; LargeArcLoops_Mod(synthGroup, outBus)});
		classDictionary.add('Mikrophonie'->{arg synthGroup, outBus; Mikrophonie_Mod(synthGroup, outBus)});
		classDictionary.add('PitchShift'->{arg synthGroup, outBus; PitchShift_Mod(synthGroup, outBus)});
		classDictionary.add('LucerneVideo'->{arg synthGroup, outBus; LucerneVideo_Mod(synthGroup, outBus)});


		classDictionary.add('AblingerSine'->{arg synthGroup, outBus; AblingerSine_Mod(synthGroup, outBus)});

		classDictionary.add('PlutaSine'->{arg synthGroup, outBus; PlutaSine_Mod(synthGroup, outBus)});

		classDictionary.add('Timer'->{arg synthGroup, outBus; Timer_Mod(synthGroup, outBus)});

		classDictionary.add('MincekSine'->{arg synthGroup, outBus; MincekSine_Mod(synthGroup, outBus)});


		classDictionary.add('TVFeedback'->{arg synthGroup, outBus; TVFeedback_Mod(synthGroup, outBus)});

		classDictionary.add('SampleLoops'->{arg synthGroup, outBus; SampleLoops_Mod(synthGroup, outBus)});

		classDictionary.add('AutoTuneFake'->{arg synthGroup, outBus; AutoTuneFake_Mod(synthGroup, outBus)});
		classDictionary.add('LoopMachineOverLap'->{arg synthGroup, outBus; LoopMachineOverLap_Mod(synthGroup, outBus)});
		//Matrices

		classDictionary.add('SnareSwitch'->{arg synthGroup, outBus; SnareSwitch_Mod(synthGroup, outBus)});
		classDictionary.add('LoopVidBuf2'->{arg synthGroup, outBus; LoopVidBuf2_Mod(synthGroup, outBus)});

		classDictionary.add('DistGrains'->{arg synthGroup, outBus; DistGrains_Mod(synthGroup, outBus)});
		classDictionary.add('MixerSolo'->{arg synthGroup, outBus; MixerSolo_Mod(synthGroup, outBus)});
		classDictionary.add('MixerSoloMono'->{arg synthGroup, outBus; MixerSoloMono_Mod(synthGroup, outBus)});

		classDictionary.add('MantaBuffers'->{arg synthGroup, outBus; MantaBuffers_Mod(synthGroup, outBus)});

		classDictionary.add('GreatExpectations'->{arg synthGroup, outBus; GreatExpectations_Mod(synthGroup, outBus)});

		classDictionary.add('OverlapWubbels'->{arg synthGroup, outBus; OverlapWubbels_Mod(synthGroup, outBus)});

		classDictionary.add('GFNoiseMiniSky'->{arg synthGroup, outBus; GFNoiseMiniSky_Mod(synthGroup, outBus)});
		classDictionary.add('MuteSky'->{arg synthGroup, outBus; MuteSky_Mod(synthGroup, outBus)});

		classDictionary.add('LowPass'->{arg synthGroup, outBus; LowPass_Mod(synthGroup, outBus)});

		classDictionary.add('VDelayInline'->{arg synthGroup, outBus; VDelayInline_Mod(synthGroup, outBus)});

		classDictionary.add('AnalogSynth'->{arg synthGroup, outBus; AnalogSynth_Mod(synthGroup, outBus)});

		classDictionary.add('MantaToMidi'->{arg synthGroup, outBus; MantaToMidi_Mod(synthGroup, outBus)});

		classDictionary.add('RageTrombones'->{arg synthGroup, outBus; RageTrombones_Mod(synthGroup, outBus)});

		classDictionary.add('SynthStack'->{arg synthGroup, outBus; SynthStack_Mod(synthGroup, outBus)});

		classDictionary.add('Sampler'->{arg synthGroup, outBus; Sampler_Mod(synthGroup, outBus)});

		classDictionary.add('FeedbackSynth'->{arg synthGroup, outBus; FeedbackSynth_Mod(synthGroup, outBus)});

		//mixers

		classDictionary.add('Mixer'->{arg synthGroup, outBus; ModularMainMixer(synthGroup, outBus).init2(2, false)});

		classDictionary.add('SignalSwitcher'->{arg synthGroup, outBus; SignalSwitcher_Mod(synthGroup, outBus).init2(2, false)});

		classDictionary.add('Convolution'->{arg synthGroup, outBus; Convolution_Mod(synthGroup, outBus).init2(2, false)});

		classDictionary.add('AmpFollower'->{arg synthGroup, outBus; AmpFollower_Mod(synthGroup, outBus).init2(2, false)});


		//Analog Synth Modules
		classDictionary.add('Maths'->{arg synthGroup, localBusses;
			Maths_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('RandoCalrissian'->{arg synthGroup, localBusses;
			RandoCalrissian_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('ComplexOscillator'->{arg synthGroup, localBusses;
			ComplexOscillator_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('DualADSR'->{arg synthGroup, localBusses;
			DualADSR_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('VCA'->{arg synthGroup, localBusses;
			VCA_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('VCF'->{arg synthGroup, localBusses;
			VCF_AnalogMod(synthGroup, localBusses)});
		classDictionary.add('Sequencer'->{arg synthGroup, localBusses;
			Sequencer_AnalogMod(synthGroup, localBusses)});
	}

	*initModule {arg className, synthGroup, bus;
		^classDictionary[className.asSymbol].value(synthGroup, bus);
	}

	*initAnalogSynthModule {arg className, synthGroup, localBusses;
		^classDictionary[className.asSymbol].value(synthGroup, localBusses);
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

