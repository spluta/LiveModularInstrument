BuchlaFilter_Mod {
	var <>group, <>type, <>freq, <>rqMax, <>inBus, <>outBus, <>pan, <>outGroup, slider, filter;

	*new {arg group, type, freq, rqMax, inBus, outBus, pan, outGroup;
		^super.newCopyArgs(group, type, freq, rqMax, inBus, outBus, pan, outGroup).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("buchlaLowFilter_mod", {arg inBus, freq, db, lagTime=0, gate = 1, pauseGate = 1;
				var in, out, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in  = In.ar(inBus, 2);

				out = BLowShelf.ar(in,freq,1,Lag.kr(db, lagTime)/*+LFNoise1.kr(0.1)*/);

				ReplaceOut.ar(inBus, out*env*pauseEnv);
			}).writeDefFile;
			SynthDef("buchlaMidFilter_mod", {arg inBus, freq, rq, db, lagTime=0, gate = 1, pauseGate = 1;
				var in, out, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in  = In.ar(inBus, 2);

				out = MidEQ.ar(in,freq, Lag.kr(rq, lagTime), Lag.kr(db, lagTime)*[-1,1]);

				ReplaceOut.ar(inBus, out*env*pauseEnv);
			}).writeDefFile;
			SynthDef("buchlaHighFilter_mod", {arg inBus, freq, db, lagTime=0, gate = 1, pauseGate = 1;
				var in, out, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in  = In.ar(inBus, 2);

				out = BHiShelf.ar(in,freq,1 ,Lag.kr(db, lagTime));

				ReplaceOut.ar(inBus, out*env*pauseEnv);
			}).writeDefFile;
			SynthDef("buchlaOut1_mod", {arg inBus, outBus, gate = 1, pauseGate = 1;
				var in, out, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in  = In.ar(inBus, 2);

				Out.ar(outBus, in*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		switch(type,
			0,{filter = Synth("buchlaLowFilter_mod",[\inBus, inBus, \freq, freq, \db, 0], group)},
			1,{
				if(pan==1,{
					filter = Synth("buchlaMidFilter_mod",[\inBus, inBus+1, \freq, freq, \rq, rqMax, \db, 0], group)
				},{
					filter = Synth("buchlaMidFilter_mod",[\inBus, inBus, \freq, freq, \rq, rqMax, \db, 0], group)
				})
			},
			2,{filter = Synth("buchlaHighFilter_mod",[\inBus, inBus, \freq, freq, \db, 0], group)},
			3,{filter = Synth("buchlaOut1_mod",[\inBus, inBus, \outBus, outBus], group)}
		);
	}

	set {arg val0, val1, lagTime;
		filter.set(\db, val0, \rq, val1, \lagTime, lagTime);
	}

	pause {
		filter.set(\pauseGate, 0);
	}

	unpause {
		filter.set(\pauseGate, 1);
		filter.run(true);
	}

	trigger {arg lowDb, highDb, rqVal=1;
		var randDB, qSet;

		randDB = rrand(lowDb, highDb);
		if(rqVal<0){
			qSet = 1;
		}{
			qSet = rqMax.rand;
		};
		filter.set(\db, randDB, \rq, qSet, \lagTime, 0.05);
		^[randDB, qSet]
	}

	killMe {

		filter.set(\gate, 0);
	}
}

BuchlaFiltersSynths_Mod {
	var <>group, <>inBus, <>outBus, filterList, transferModule, rqMax, freqs, filtNums, inGroup, outGroup, filterSettingArray, negativeFilterList, pan;

	*new {arg group, inBus, outBus;
		^super.newCopyArgs(group, inBus, outBus).init;
	}

	init {
		inGroup = Group.before(group);
		//group is in the middle (where the mid-eqs go)
		outGroup = Group.after(group);
		filterList = List.new;
		freqs = [100,150,250,350,500,630,800,1000,1300,1600,2600,3500,5000,8000,10000];

		//mix the input signal into the output bus first, the filter the audio
		transferModule = BuchlaFilter_Mod(inGroup, 3, nil, nil, inBus, outBus, 0);

		filterList.add(BuchlaFilter_Mod(group, 0, freqs[0], 1, outBus, outBus, 0));

		13.do{arg i;
			rqMax = ((freqs[i+1]-freqs[i]).abs+((freqs[i+1]-freqs[i+2]).abs))/freqs[i+1];
			filterList.add(BuchlaFilter_Mod(group, 1, freqs[i+1], rqMax, outBus, outBus, 0, outGroup));
		};

		filterList.add(BuchlaFilter_Mod(group, 2, freqs[14], 1, outBus, outBus, 0, outGroup));

		filterSettingArray = List.newClear(15);
		filterSettingArray.fill([0,0.1]);
		this.clear;
	}

	pause {
		filterList.do{|item| item.pause}
	}

	unpause {
		filterList.do{|item| item.unpause}
	}

	trigger {arg num;
		if(num==nil, {num = 6});
		filterList.do{arg item; item.trigger(num.neg,num, 1)};
		^filterSettingArray
	}

	clear {
		filterList.do{arg item; item.trigger(0,0, -1)};
		//15.do{arg i; filterSettingArray.put(i, filterList[i].trigger(0,0), 1)};
	}

	setVals {arg filterSettings;
		filterSettings.settingsList.do{arg item, i; filterList[i].set(item[0], item[1], rrand(2,5))}
	}

	triggerRangeNormal {arg filts, db;
		filts.do{arg i; filterList[i].trigger(db.neg,db)}
	}

	triggerRange {arg midEQ, radius, boost;
		filtNums = List.series(15,0,1);
		filterList[midEQ].trigger(boost/2, boost);
		filtNums.removeAt(midEQ);
		radius.do{arg i;
			filtNums.remove(midEQ+i);
			filterList[midEQ+i].trigger((radius-i)*(boost/2), (radius-i)*boost);
		};
		radius.do{arg i;
			filtNums.remove(midEQ-i);
			filterList[midEQ+i].trigger((radius-i)*(boost/2), (radius-i)*boost);
		};
	}

	killMe {
		filterList.do{arg item; item.killMe};
		transferModule.killMe;
		negativeFilterList.do{arg item; item.killMe};
		inGroup.free;
		group.freeAllMsg;
	}
}

BuchlaFilters_Mod : Module_Mod {
	var buchlaFilters, trigVal;

	init {
		this.makeWindow("BuchlaFilters",Rect(946, 618, 185, 75));

		this.makeMixerToSynthBus(2);

		this.initControlsAndSynths(3);

		dontLoadControls = [0,1];

		controls.add(Button.new(win,Rect(5, 5, 80, 20))
			.states_([ [ "Trigger", Color.red, Color.black ] ,[ "Trigger", Color.black, Color.red ] ])
			.action_{|v|
				buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}
			});
		controls.add(Button.new(win,Rect(85, 5, 80, 20))
			.states_([ [ "Clear", Color.red, Color.black ] ,[ "Clear", Color.black, Color.red ] ])
			.action_{|v|
				buchlaFilters.do{|item| if(item!=nil,{item.clear})}
			});

		this.addAssignButton(0,\onOff,Rect(5, 30, 80, 20));

		this.addAssignButton(1,\onOff,Rect(85, 30, 80, 20));

		controls.add(EZSlider.new(win,Rect(5, 50, 160, 20), "trigVal", ControlSpec(1,12,'lin',1),
			{|v|
				trigVal = v.value;
			}, 6, layout:\horz)
		);

		buchlaFilters = List.newClear(4);
		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(group, mixerToSynthBus.index, outBus.index));
	}

	pause {
		buchlaFilters.do{arg item; if(item!=nil,{item.pause})};
	}

	unpause {
		buchlaFilters.do{arg item; if(item!=nil,{item.unpause})};
	}

	killMeSpecial {
		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
	}
}

// BuchlaModelSolo2_Mod :  Module_Mod {
// 	var filterList, rqMax, freqs, filtNums, outGroup, specs, synthGroup, filterGroup, transferBus, buchlaFilters, midiIsSet, mantaIsSet, straightPitch, straightPitchBool, triggerButtons, continuousButtons, ampSpec, fmAmpSpec, padFunctions, noteOnFunctions, noise1Select, noise2Select, ampModNoiseSelect, trigVal, baseVol;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("buchlaFMPatchSolo2_mod", {arg outBus, amp0 = 0, amp1=0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0, amp6 = 0, amp7 = 0, freq0 = 300, freq1=200, ampModFreq=0, ampModVol = 0, filtTrig=1, noise1Select=0, noise2Select = 0, ampModNoiseSelect=0, t_trig, lagTime = 0.1, gate = 1;
// 				var layer2, layer1, layer0, ampMod, seq, env, layer1Noise;
//
// 				layer2 = WhiteNoise.ar(amp5)+PinkNoise.ar(amp6)+BrownNoise.ar(amp7);
//
// 				layer2 = Select.ar(noise2Select, [layer2, Latch.ar(layer2, Dust.ar(TRand.ar(200, 1000, noise2Select)))]);
//
// 				layer1Noise = BrownNoise.ar(amp4);
// 				layer1Noise = Select.ar(noise1Select, [layer1Noise, Latch.ar(layer1Noise, Dust.ar(TRand.ar(200, 1000, noise1Select)))]);
//
// 				layer1 = BPF.ar(WhiteNoise.ar(amp1), Gate.ar(LFNoise2.ar(6, 800, 850), filtTrig), Gate.ar(LFNoise2.ar(5,0.5,1), filtTrig))
// 				+SinOsc.ar(freq1+layer2,0,amp2)+LFSaw.ar(freq1+layer2,0,amp3)+layer1Noise;
//
// 				ampMod = SinOsc.ar(ampModFreq+(Select.kr(ampModNoiseSelect, [0, LFNoise2.kr(TRand.kr(0.5, 2, ampModNoiseSelect)).range(0, 30)])), 0, ampModVol/2, ampModVol/2);
//
// 				layer0 = SinOsc.ar(freq0+layer1, 0, ampModVol-(ampMod*Lag.kr((ampModFreq>1), 1/ampModFreq)));
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
//
// 				Out.ar(outBus, [layer0,layer0]*env)
// 			}).writeDefFile
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("BuchlaModelSolo2",Rect(200, 230, 140, 280));
//
// 		this.initControlsAndSynths(48);
//
// 		assignButtons.add(Button.new(win,Rect(10, 10, 60, 20))
// 			.states_([ [ "Normal", Color.red, Color.black ] ,[ "Pitched", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					straightPitchBool = true;
// 					synths[0].set(\freq0, straightPitch.midicps);
// 					if(mantaIsSet,{
// 						Manta.removePad(continuousButtons[1]);
// 						Manta.removePad(continuousButtons[2]);
// 					});
// 					},{
// 						straightPitchBool = false;
// 						if(mantaIsSet,{
// 							Manta.addPad(continuousButtons[1], {arg val;  synths[0].set(\freq0, specs[0].map(val/200))});
// 							Manta.addPad(continuousButtons[2], {arg val;  synths[0].set(\freq0, specs[1].map(val/200))});
// 						});
// 				})
// 		});
//
// 		baseVol = 0;
// 		controls.add(EZSlider.new(win,Rect(10, 30, 60, 220), "baseVol", ControlSpec(0,200,'linear'),
// 			{|v|
// 				baseVol = v.value;
// 				synths[0].set(\ampModVol, ampSpec.map(baseVol/200));
// 		}, 0, layout:\vert));
//
// 		straightPitch = 60;
// 		straightPitchBool = false;
// 		assignButtons.add(Button.new(win,Rect(10, 250, 60, 20))
// 			.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					this.setManta;
// 					},{
// 						this.clearMidiHid;
// 						mantaIsSet = false;
// 				})
// 		});
//
// 		controls.add(EZSlider.new(win,Rect(70, 30, 60, 220), "trigVal", ControlSpec(1,12,'lin',1),
// 			{|v|
// 				trigVal = v.value;
// 		}, 6, layout:\vert));
//
// 		controls.add(Button(win,Rect(10, 275, 60, 20))
// 			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
// 			.action_{|butt|
// 				switch(butt.value,
// 					0, {
// 						numChannels = 2;
// 						3.do{|i| buchlaFilters[i+1].killMe};
// 					},
// 					1, {
// 						numChannels = 4;
// 						buchlaFilters.put(1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+2));
// 					},
// 					2, {
// 						if(numChannels==2,{
// 							3.do{|i| buchlaFilters.put(i+1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+1))))};
// 							},{
// 								2.do{|i| buchlaFilters.put(i+2,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+2))))};
// 						});
// 						numChannels = 8;
//
// 					}
// 				)
// 			};
// 		);
//
// 		specs = List.new;
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(0.0001, 100, 'exponential'));
//
// 		synthGroup = Group.tail(group);
// 		filterGroup = Group.tail(group);
// 		transferBus = Bus.audio(group.server, 2);
//
// 		synths = List.new;
// 		synths.add(Synth("buchlaFMPatchSolo2_mod", [\outBus, transferBus.index], synthGroup));
//
// 		buchlaFilters = List.newClear(4);
// 		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));
//
// 		triggerButtons = [3,11,20, 9,18,26, 41,1, 28,35,13];
// 		continuousButtons = [4,3,11,20, 2, 10,19,27, 9,18,26, 17,25,34, 12,4];
//
// 		mantaIsSet = false;
// 		ampSpec = ControlSpec(0,1,'linear');
// 		fmAmpSpec = ControlSpec(0,3000,'amp');
//
// 		this.addFunctions;
// 	}
//
// 	pause {
// 		buchlaFilters.do{arg item; if(item!=nil,{item.pause})};
// 		synths.do{|item| item.set(\pauseGate, 0)};
// 	}
//
// 	unpause {
// 		buchlaFilters.do{arg item; if(item!=nil,{item.unpause})};
// 		synths.do{|item| item.set(\pauseGate, 1); item.run(true)};
// 	}
//
// 	setMantaForSetup {arg setup, type, key, function;
// 		switch(type,
// 			0,{Manta.addPadSetup(setup.asSymbol, key, function)},
// 			1,{Manta.addNoteOnSetup(setup.asSymbol, key, function)}
// 		);
// 	}
//
// 	setManta {
// 		setups.do{arg setup;
// 			padFunctions.keys.do{arg key;
// 				this.setMantaForSetup(setup.asSymbol, 0, key, padFunctions[key]);
// 			};
// 			noteOnFunctions.keys.do{arg key;
// 				this.setMantaForSetup(setup.asSymbol, 1, key, noteOnFunctions[key]);
// 			};
// 		};
// 		mantaIsSet = true;
// 	}
//
// 	addSetup {arg setup;
// 		setups.add(setup);
// 		noteOnFunctions.keys.do{arg key;
// 			this.setMantaForSetup(setup.asSymbol, 1, key, noteOnFunctions[key]);
// 		};
// 		padFunctions.keys.do{arg key;
// 			this.setMantaForSetup(setup.asSymbol, 0, key, padFunctions[key]);
// 		};
// 	}
//
// 	addFunctions {
// 		noteOnFunctions = IdentityDictionary.new;
// 		noteOnFunctions.put(triggerButtons[0], {arg val; specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[1], {arg val; specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[2], {arg val; specs.put(2, ControlSpec(rrand(600,950), rrand(5000, 15000), 'exponential'))});
//
// 		noteOnFunctions.put(triggerButtons[3], {arg val; specs.put(3, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[4], {arg val; specs.put(4, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[5], {arg val; specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[6], {arg val; buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}});
// 		noteOnFunctions.put(triggerButtons[7], {arg val; synths[0].set(\filtTrig, 1)});
//
// 		noise1Select = Pseq([0,1],inf).asStream;
// 		noise2Select = Pseq([0,1],inf).asStream;
// 		ampModNoiseSelect = Pseq([0,1],inf).asStream;
// 		noteOnFunctions.put(triggerButtons[8], {arg val; synths[0].set(\noise1Select, noise1Select.next)});
// 		noteOnFunctions.put(triggerButtons[9], {arg val; synths[0].set(\noise2Select, noise2Select.next)});
// 		noteOnFunctions.put(triggerButtons[10], {arg val; synths[0].set(\ampModNoiseSelect, ampModNoiseSelect.next)});
//
// 		padFunctions = IdentityDictionary.new;
//
// 		if(straightPitchBool != true, {
// 			padFunctions.put(continuousButtons[1], {arg val;  synths[0].set(\freq0, specs[0].map(val/200))});
// 			padFunctions.put(continuousButtons[2], {arg val;  synths[0].set(\freq0, specs[1].map(val/200))});
// 			padFunctions.put(continuousButtons[3], {arg val;  synths[0].set(\freq0, specs[2].map(val/200))});
// 		});
//
// 		padFunctions.put(continuousButtons[4], {arg val;  synths[0].set(\amp1, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[5], {arg val;  synths[0].set(\amp2, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[6], {arg val;  synths[0].set(\amp3, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[7], {arg val;  synths[0].set(\amp4, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[8], {arg val;  synths[0].set(\freq1, specs[3].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[9], {arg val;  synths[0].set(\freq1, specs[4].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[10], {arg val;  synths[0].set(\freq1, specs[5].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[11], {arg val;  synths[0].set(\amp5, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[12], {arg val;  synths[0].set(\amp6, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[13], {arg val;  synths[0].set(\amp7, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[14], {arg val;  synths[0].set(\ampModFreq, specs[6].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[15], {arg val;  val = val+baseVol; synths[0].set(\ampModVol, ampSpec.map(max(val-20,0)/200))});
// 	}
//
// 	clearMidiHid {}
//
// 	clearMidiHidSpecial {
// 		setups.do{|setup|
// 			noteOnFunctions.keys.do{arg key;
// 				Manta.removeNoteOnSetup(setup.asSymbol, key);
// 			};
// 			padFunctions.keys.do{arg key;
// 				Manta.removePadSetup(setup.asSymbol, key);
// 			};
// 		}
// 	}
//
// 	save {arg xmlDoc;
// 		xmlSynth = xmlDoc.createElement(modName);
// 		xmlSynth.setAttribute("bounds", win.bounds.asString);
// 		xmlSynth.setAttribute("mantaIsSet", mantaIsSet.asString);
// 		xmlSynth.setAttribute("straightPitch", straightPitch.asString);
// 		xmlSynth.setAttribute("straightPitchBool", straightPitchBool.asString);
//
// 		controls.do{arg item, i;
// 			xmlSynth.setAttribute("controls"++i.asString, item.value.asString);
// 		};
//
// 		^xmlSynth;
// 	}
//
// 	trigger {
// 		synths[0].set(\t_trig, 1);
// 	}
//
// 	killMeSpecial {
// 		this.clearMidiHidSpecial;
// 		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
// 	}
// }

BuchlaFilterSetting_Mod {var freqs, <>settingsList;

	*new {
		^super.newCopyArgs().init;
	}

	init {
		freqs = [100,150,250,350,500,630,800,1000,1300,1600,2600,3500,5000,8000,10000];

		settingsList = List.new;
		settingsList.add(List.new);
		settingsList[0].add(rrand(-12,12));
		settingsList[0].add(0);
		13.do{arg i2;
			settingsList.add(List.new);
			settingsList[i2+1].add(rrand(-12,12));
			settingsList[i2+1].add((((freqs[i2+1]-freqs[i2]).abs+((freqs[i2+1]-freqs[i2+2]).abs))/freqs[i2+1]).rand);
		};
		settingsList.add(List.new);
		settingsList[14].add(rrand(-12,12));
		settingsList[14].add(0);
	}
}

BuchlaSetting_Mod {
	var <>freq0a, <>freq0b, <>freq1a, <>freq1b, <>noise0a, <>noise0b, <>noise1a, <>noise1b, <>noise2a, <>noise2b, <>noise3a, <>noise3b, <>filterSettings, <>currentFilterSetting0, <>currentFilterSetting1, <>volume, freqs, settingsTempList;

	*new {
		^super.newCopyArgs().init;
	}

	init {
		freq0a = freq0b = rrand(200, 3000);
		freq1a = freq1b = rrand(200, 3000);
		noise0a = noise1a = noise2a = noise3a = 0;
		noise0b = noise1b = noise2b = noise3b = 3000;
		volume = 1;

		filterSettings = List.new;

		5.do{arg i;
			settingsTempList = List.new;
			2.do{arg mainIndex;
				settingsTempList.add(BuchlaFilterSetting_Mod.new);
			};
			filterSettings.add(settingsTempList);
		};
		currentFilterSetting0 = filterSettings[0][0];
		currentFilterSetting1 = filterSettings[0][1];
	}

	setCurrentFilter0 {arg num;
		currentFilterSetting0 = filterSettings[num][0];
		^currentFilterSetting0
	}

	setCurrentFilter1 {arg num;
		currentFilterSetting1 = filterSettings[num][1];
		^currentFilterSetting1
	}

	replaceSetting{arg filterBankToSet, filterSettingsIn0, filterSettingsIn1;
		filterSettings.put(filterBankToSet, [filterSettingsIn0.deepCopy, filterSettingsIn1.deepCopy]);
	}
}

BuchlaSettingBank_Mod {
	var bank, xmlRoot, xmlBank, xmlDoc, xmlSetting, xmlBuchlaSetting, currentBankNum, currentSettingNum, filterSettings, file;

	*new {
		^super.newCopyArgs().init;
	}

	init {
		bank = List.new;
		4.do{arg i;
			bank.add(List.newClear(8));
			8.do{arg i2; bank[i].put(i2, BuchlaSetting_Mod.new)};
		};
		currentBankNum = 0;
		currentSettingNum = 0;
	}

	load {arg path;
		xmlDoc = DOMDocument.new(path);
		xmlRoot = xmlDoc.getDocumentElement.getElement("BuchlaSaveSettings");
		4.do{arg i;
			xmlBank = xmlRoot.getElement("IndividualBank"++i);
			8.do{arg i2;
				xmlSetting = xmlBank.getElement("individualSetting"++i2);
				bank[i][i2].freq0a = xmlSetting.getAttribute("freq0a").interpret;
				bank[i][i2].freq0b = xmlSetting.getAttribute("freq0b").interpret;
				bank[i][i2].freq1a = xmlSetting.getAttribute("freq1a").interpret;
				bank[i][i2].freq1b = xmlSetting.getAttribute("freq1b").interpret;
				bank[i][i2].noise0a = xmlSetting.getAttribute("noise0a").interpret;
				bank[i][i2].noise0b = xmlSetting.getAttribute("noise0b").interpret;
				bank[i][i2].noise1a = xmlSetting.getAttribute("noise1a").interpret;
				bank[i][i2].noise1b = xmlSetting.getAttribute("noise1b").interpret;
				bank[i][i2].noise2a = xmlSetting.getAttribute("noise2a").interpret;
				bank[i][i2].noise2b = xmlSetting.getAttribute("noise2b").interpret;
				bank[i][i2].noise3a = xmlSetting.getAttribute("noise3a").interpret;
				bank[i][i2].noise3b = xmlSetting.getAttribute("noise3b").interpret;
				bank[i][i2].volume = xmlSetting.getAttribute("volume").interpret;
				5.do{arg i3;
					var temp0, temp1;
					temp0 = xmlSetting.getAttribute("filterSettings"++i3);
					temp1 = xmlSetting.getAttribute("filterSettings"++(i3+5));
					if((temp0!=nil)and:(temp1!=nil),{
						bank[i][i2].filterSettings[i3][0].settingsList = temp0.interpret;
						bank[i][i2].filterSettings[i3][1].settingsList = temp1.interpret;
					})
				};
			}
		}
	}

	makeXMLDoc {
		xmlRoot = xmlDoc.createElement("BuchlaSaveSettings");
		xmlDoc.appendChild(xmlRoot);
		4.do{arg i;
			xmlBank = xmlDoc.createElement("IndividualBank"++i);
			xmlRoot.appendChild(xmlBank);
			8.do{arg i2;
				xmlSetting = xmlDoc.createElement("individualSetting"++i2);
				xmlBank.appendChild(xmlSetting);
				xmlSetting.setAttribute("freq0a", bank[i][i2].freq0a.asString);
				xmlSetting.setAttribute("freq0b", bank[i][i2].freq0b.asString);
				xmlSetting.setAttribute("freq1a", bank[i][i2].freq1a.asString);
				xmlSetting.setAttribute("freq1b", bank[i][i2].freq1b.asString);
				xmlSetting.setAttribute("noise0a", bank[i][i2].noise0a.asString);
				xmlSetting.setAttribute("noise0b", bank[i][i2].noise0b.asString);
				xmlSetting.setAttribute("noise1a", bank[i][i2].noise1a.asString);
				xmlSetting.setAttribute("noise1b", bank[i][i2].noise1b.asString);
				xmlSetting.setAttribute("noise2a", bank[i][i2].noise2a.asString);
				xmlSetting.setAttribute("noise2b", bank[i][i2].noise2b.asString);
				xmlSetting.setAttribute("noise3a", bank[i][i2].noise3a.asString);
				xmlSetting.setAttribute("noise3b", bank[i][i2].noise3b.asString);
				xmlSetting.setAttribute("volume", bank[i][i2].volume.asString);
				5.do{arg i3;
					xmlSetting.setAttribute("filterSettings"++i3.asString, bank[i][i2].filterSettings[i3][0].settingsList.asString);
				};
				5.do{arg i3;
					xmlSetting.setAttribute("filterSettings"++(i3+5).asString, bank[i][i2].filterSettings[i3][1].settingsList.asString);
				}
			}
		}
	}

	save {arg path;
		xmlDoc = DOMDocument.new;
		this.makeXMLDoc;
		file = File(path, "w");
		xmlDoc.write(file); // output to file with default formatting
		file.close;
	}

	postXML {
		xmlDoc = DOMDocument.new;
		xmlSetting = xmlDoc.createElement("individualSetting");
		xmlDoc.appendChild(xmlSetting);
		xmlSetting.setAttribute("freq0a", bank[currentBankNum][currentSettingNum].freq0a.asString);
		xmlSetting.setAttribute("freq0b", bank[currentBankNum][currentSettingNum].freq0b.asString);
		xmlSetting.setAttribute("freq1a", bank[currentBankNum][currentSettingNum].freq1a.asString);
		xmlSetting.setAttribute("freq1b", bank[currentBankNum][currentSettingNum].freq1b.asString);
		5.do{arg i3;
			xmlSetting.setAttribute("filterSettings"++i3.asString, bank[currentBankNum][currentSettingNum].filterSettings[i3].asString);
		};
		xmlDoc.format;
	}

	setLargeBank {arg val;
		currentBankNum = val;
		^bank[currentBankNum][currentSettingNum];
	}

	setSettingNum {arg val;
		currentSettingNum = val;
		^bank[currentBankNum][currentSettingNum];
	}

	setByKeyboard {arg setBank, setting;
		currentBankNum = setBank;
		currentSettingNum = setting;
		^bank[currentBankNum][currentSettingNum];
	}

	currentBuchlaSetting {
		^bank[currentBankNum][currentSettingNum];
	}

}


//don't delete this
//
// BuchlaModelSave_Mod :  BuchlaModelSolo2_Mod {
// 	var filterButtons, settingButtons, exportButton, settingBank, saveButton, loadButton, currentBuchlaSetting, rangeSliders, freq0Low, freq0High, freq1Low, freq1High, crazyMode, crazyModeButton, replaceButton, replaceSetting, crazyFilterSetting0, crazyFilterSetting1, replaceFreqsButton, currentValList, fmAmpSpecs, bankTemp, settingTemp, midiResponders, currentDownNote, midiIsSet, mantaIsSet, sectionButton, transferBus0, transferBus1, filterGroup0, filterGroup1, noteDownVol;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("buchlaFMPatchSave_mod", {arg outBus0, outBus1, amp0 = 0, amp1=0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0, amp6 = 0, amp7 = 0, freq0 = 300, freq1=200, filtTrig=1, t_trig, lagTime = 0.1, volume = 1, noteDownVol=1, gate = 0;
// 				var layer2, layer1, layer0, seq, env, out;
//
// 				layer2 = WhiteNoise.ar(amp5)+PinkNoise.ar(amp6)+BrownNoise.ar(amp7);
//
// 				layer1 = BPF.ar(WhiteNoise.ar(amp1), Gate.ar(LFNoise2.ar(6, 800, 850), filtTrig), Gate.ar(LFNoise2.ar(5,0.5,1), filtTrig))
// 				+SinOsc.ar(freq1+layer2,0,amp2)+LFSaw.ar(freq1+layer2,0,amp3)+BrownNoise.ar(amp4);
//
// 				layer0 = SinOsc.ar(freq0+layer1, 0, amp0);
//
// 				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction: 0);
//
// 				out = [layer0,layer0]*env*volume*noteDownVol;
//
// 				Out.ar(outBus0, out);
// 				Out.ar(outBus1, out);
// 			}).writeDefFile;
// 			SynthDef("standingWavesSection2Synth", {arg outBus0, outBus1, amp0=0, amp1=0, amp2=0, amp3=0, amp4=0, amp5=0, amp6=0, amp7=0, amp8=0, amp9=0, amp10=0, amp11=0, amp12=0, amp13=0, amp14=0, amp15=0;
// 				var sin0, sin1, sin2, sin3, sin4, sin5, sin6, sin7, sin8, sin9, sin10, sin11, sin12, sin13, sin14, sin15;
//
// 				sin0 = SinOsc.ar(93.midicps-10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp0, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin1 = SinOsc.ar(93.midicps-5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp1, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin2 = SinOsc.ar(93.midicps+5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp2, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin3 = SinOsc.ar(93.midicps+10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp3, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
//
// 				sin4 = SinOsc.ar(82.midicps-10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp4, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin5 = SinOsc.ar(82.midicps-5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp5, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin6 = SinOsc.ar(82.midicps+5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp6, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin7 = SinOsc.ar(82.midicps+10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp7, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
//
// 				sin8 = SinOsc.ar(71.midicps-10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp8, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin9 = SinOsc.ar(71.midicps-5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp9, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin10 = SinOsc.ar(71.midicps+5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp10, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin11 = SinOsc.ar(71.midicps+10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp11, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
//
// 				sin12 = SinOsc.ar(60.midicps-10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp12, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin13 = SinOsc.ar(60.midicps-5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp13, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin14 = SinOsc.ar(60.midicps+5+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp14, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
// 				sin15 = SinOsc.ar(60.midicps+10+LFNoise2.kr(0.1, 5), 0, LagUD.kr(amp15, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5)));
//
// 				Out.ar(outBus0, Splay.ar([sin0, sin1, sin2, sin3, sin4, sin5, sin6, sin7, sin8, sin9, sin10, sin11, sin12, sin13, sin14, sin15], 1));
// 				Out.ar(outBus1, Splay.ar([sin0, sin1, sin2, sin3, sin4, sin5, sin6, sin7, sin8, sin9, sin10, sin11, sin12, sin13, sin14, sin15], 1));
// 			}).writeDefFile
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("BuchlaModelSave",Rect(710, 340, 700, 430));
//
// 		controls = List.new;
// 		assignButtons = List.new;
//
// 		settingBank = BuchlaSettingBank_Mod.new;
//
// 		specs = List.new;
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
//
// 		controls.add(EZSlider.new(win,Rect(10, 190, 80, 220), "pitch", ControlSpec(30,90,'linear',0.5),
// 			{|v|
// 				rangeSliders[0].valueAction_([v.value.midicps, v.value.midicps+(v.value.midicps-(v.value+0.5).midicps)]);
// 		}, 60, layout:\vert));
//
// 		assignButtons.add(Button.new(win,Rect(650, 355, 60, 20))
// 			.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					this.setAllManta;
// 					},{
// 						this.clearAllManta;
// 				})
// 		});
// 		assignButtons.add(Button.new(win,Rect(650, 385, 60, 20))
// 			.states_([ [ "A-MIDI", Color.red, Color.black ] ,[ "C-MIDI", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					this.setMIDIKeyboard;
// 					},{
// 						this.clearAllMIDI;
// 				})
// 		});
//
// 		rangeSliders = List.new;
//
// 		rangeSliders.add(EZRanger.new(win,Rect(90, 210, 80, 220), "freq0", ControlSpec(30.midicps, 3000, 'exponential'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.freq0a = min(v[0], v[1]);
// 				currentBuchlaSetting.freq0b = max(v[0], v[1]);
//
// 				specs.put(0, ControlSpec(v[0], v[1], 'exponential'));
// 				specs.put(1, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\freq0, specs[0].map((currentValList[2])/200));
//
// 				crazyMode = false;
// 		}, [200,3000], false, layout:\vert));
//
// 		rangeSliders.add(EZRanger.new(win,Rect(170, 210, 80, 220), "freq1", ControlSpec(20, 3000, 'exponential'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.freq1a = min(v[0], v[1]);
// 				currentBuchlaSetting.freq1b = max(v[0], v[1]);
//
// 				specs.put(2, ControlSpec(v[0], v[1], 'exponential'));
// 				specs.put(3, ControlSpec(v[0], v[1], 'exponential'));
// 				specs.put(4, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\freq1, specs[2].map((currentValList[9])/200));
// 				crazyMode = false;
// 		}, [200,3000], false, layout:\vert));
//
// 		rangeSliders.add(EZRanger.new(win,Rect(250, 210, 80, 220), "noise0", ControlSpec(10, 3000, 'amp'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.noise0a = min(v[0], v[1]);
// 				currentBuchlaSetting.noise0b = max(v[0], v[1]);
//
// 				fmAmpSpecs.put(3, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\amp4, fmAmpSpecs[3].map((currentValList[6])/200));
// 				crazyMode = false;
// 		}, [0,3000], false, layout:\vert));
//
// 		rangeSliders.add(EZRanger.new(win,Rect(330, 210, 80, 220), "noise1", ControlSpec(10, 3000, 'amp'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.noise1a = min(v[0], v[1]);
// 				currentBuchlaSetting.noise1b = max(v[0], v[1]);
//
// 				fmAmpSpecs.put(4, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\amp5, fmAmpSpecs[4].map((currentValList[10])/200));
// 				crazyMode = false;
// 		}, [0,3000], false, layout:\vert));
//
// 		rangeSliders.add(EZRanger.new(win,Rect(410, 210, 80, 220), "noise2", ControlSpec(10, 3000, 'amp'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.noise2a = min(v[0], v[1]);
// 				currentBuchlaSetting.noise2b = max(v[0], v[1]);
//
// 				fmAmpSpecs.put(5, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\amp6, fmAmpSpecs[5].map((currentValList[11])/200));
// 				crazyMode = false;
// 		}, [0,3000], false, layout:\vert));
//
// 		rangeSliders.add(EZRanger.new(win,Rect(490, 210, 80, 220), "noise3", ControlSpec(10, 3000, 'amp'),
// 			{|v|
// 				v = v.value;
// 				currentBuchlaSetting.noise3a = min(v[0], v[1]);
// 				currentBuchlaSetting.noise3b = max(v[0], v[1]);
//
// 				fmAmpSpecs.put(6, ControlSpec(v[0], v[1], 'exponential'));
// 				synths[0].set(\amp7, fmAmpSpecs[6].map((currentValList[12])/200));
// 				crazyMode = false;
// 		}, [0,3000], false, layout:\vert));
// 		rangeSliders.add(EZSlider.new(win,Rect(570, 210, 80, 220), "vol", ControlSpec(0, 1, 'amp'),
// 			{|v|
// 				currentBuchlaSetting.volume = v.value;
//
// 				synths[0].set(\volume, v.value);
// 				crazyMode = false;
// 		}, 1, false, layout:\vert));
//
//
// 		filterButtons = List.new;
// 		filterButtons.add(Button.new(win,Rect(10, 10, 30, 30))
// 			.states_([ [ "", Color.blue, Color.blue ],  [ "", Color.red, Color.red ]  ])
// 			.action_{|v|
// 				crazyFilterSetting0 = buchlaFilters[0].trigger;
// 				crazyFilterSetting1 = buchlaFilters[1].trigger;
// 			}
// 		);
//
// 		currentBuchlaSetting = settingBank.currentBuchlaSetting;
//
// 		replaceSetting = false;
//
// 		3.do{arg i; filterButtons.add(Button.new(win,Rect(60+(40*i), 10, 30, 30))
// 			.states_([ [ "", Color.red, Color.red ], [ "", Color.blue, Color.blue ] ])
// 			.action_{|v|
// 				if(replaceSetting==false,{
// 					buchlaFilters[0].setVals(currentBuchlaSetting.setCurrentFilter0(i));
// 					buchlaFilters[1].setVals(currentBuchlaSetting.setCurrentFilter1(i));
// 					},{
// 						replaceSetting = false;
// 						currentBuchlaSetting.replaceSetting(i, crazyFilterSetting0, crazyFilterSetting1);
// 				});
// 				5.do{arg i2; filterButtons[i2+1].value_(0)};
// 				filterButtons[i+1].value_(1);
// 			}
// 		)};
// 		2.do{arg i; filterButtons.add(Button.new(win,Rect(80+(40*i), 50, 30, 30))
// 			.states_([ [ "", Color.red, Color.red ], [ "", Color.blue, Color.blue ] ])
// 			.action_{|v|
// 				if(replaceSetting==false,{
// 					buchlaFilters[0].setVals(currentBuchlaSetting.setCurrentFilter0(i+3));
// 					buchlaFilters[1].setVals(currentBuchlaSetting.setCurrentFilter1(i+3));
// 					},{
// 						replaceSetting = false;
// 						currentBuchlaSetting.replaceSetting(i+3, crazyFilterSetting0, crazyFilterSetting1);
// 				});
// 				5.do{arg i2; filterButtons[i2+1].value_(0)};
// 				filterButtons[i+4].value_(1);
// 			}
// 		)};
//
// 		settingButtons = List.new;
//
// 		4.do{arg i; settingButtons.add(Button(win,Rect(210+(40*i), 10, 30, 30))
// 			.states_([ [ "", Color.yellow, Color.yellow ], [ "", Color.blue, Color.blue ] ])
// 			.action_{|v|
// 				currentBuchlaSetting = settingBank.setLargeBank(i);
// 				this.setAllSlidersFilters;
// 				synths[0].set(\gate, 1);
// 				4.do{arg i2; settingButtons[i2].value = 0};
// 				settingButtons[i].value = 1;
// 			});
// 		};
// 		4.do{arg i; settingButtons.add(Button(win,Rect(190+(40*i), 50, 30, 30))
// 			.states_([ [ "", Color.yellow, Color.yellow ], [ "", Color.blue, Color.blue ] ])
// 			.action_{|v|
// 				currentBuchlaSetting = settingBank.setSettingNum(i);				this.setAllSlidersFilters;
// 				synths[0].set(\gate, 1);
// 				8.do{arg i2; settingButtons[i2+4].value = 0};
// 				settingButtons[i+4].value = 1;
// 			});
// 		};
// 		4.do{arg i; settingButtons.add(Button(win,Rect(210+(40*i), 90, 30, 30))
// 			.states_([ [ "", Color.yellow, Color.yellow ], [ "", Color.blue, Color.blue ] ])
// 			.action_{|v|
// 				currentBuchlaSetting = settingBank.setSettingNum(i+4);
// 				this.setAllSlidersFilters;
// 				synths[0].set(\gate, 1);
// 				8.do{arg i2; settingButtons[i2+4].value = 0};
// 				settingButtons[i+8].value = 1;
// 			});
//
// 		};
//
// 		sectionButton = Button(win, Rect(410, 110, 80, 40))
// 		.states_([["Section 1", Color.black, Color.red],["Section 2",Color.red,Color.black]])
// 		.action_{|v|
// 			this.changeSection(v.value)
// 		};
//
// 		currentBuchlaSetting = settingBank.setLargeBank(0);
// 		settingButtons[0].value = 1;
//
// 		exportButton = Button(win, Rect(210, 130, 50, 30))
// 		.states_([["Export", Color.green, Color.black], ["Export", Color.black, Color.green]])
// 		.action_{
// 			settingBank.postXML;
// 		};
// 		replaceButton = Button(win, Rect(270, 130, 50, 30))
// 		.states_([["Replace", Color.green, Color.black], ["Replace", Color.black, Color.green]])
// 		.action_{
// 			replaceSetting = true;
// 		};
//
// 		crazyModeButton = Button(win, Rect(210, 170, 50, 30))
// 		.states_([["Crazy", Color.green, Color.black], ["Crazy", Color.black, Color.green]])
// 		.action_{
// 			crazyMode = true;
// 			specs.put(0,ControlSpec(200, 1000, 'exponential'));
// 			specs.put(1,ControlSpec(500, 3000, 'exponential'));
// 			specs.put(2,ControlSpec(500, 3000, 'exponential'));
// 			specs.put(3,ControlSpec(200, 1000, 'exponential'));
// 			specs.put(4,ControlSpec(500, 3000, 'exponential'));
// 		};
// 		crazyModeButton.valueAction = 1;
//
// 		loadButton = Button(win, Rect(270, 170, 50, 30))
// 		.states_([["Load", Color.green, Color.black], ["Load", Color.black, Color.green]])
// 		.action_{
// 			CocoaDialog.openPanel({ arg paths;
// 				paths.do({ arg path;
// 					settingBank.load(path);
// 				})
// 				},{
// 			});
// 		};
// 		saveButton = Button(win, Rect(330, 170, 50, 30))
// 		.states_([["Save", Color.green, Color.black], ["Save", Color.black, Color.green]])
// 		.action_{
// 			CocoaDialog.savePanel({arg path;
// 				settingBank.save(path);
// 				},{
// 			});
// 		};
//
// 		synthGroup = Group.tail(group);
// 		filterGroup0 = Group.tail(group);
// 		filterGroup1 = Group.tail(group);
// 		transferBus0 = Bus.audio(group.server, 2);
// 		transferBus1 = Bus.audio(group.server, 2);
//
// 		synths = List.new;
// 		synths.add(Synth("buchlaFMPatchSave_mod", [\outBus0, transferBus0.index, \outBus1, transferBus1.index], synthGroup));
// 		synths.add(Synth("standingWavesSection2Synth", [\outBus0, transferBus0.index, \outBus1, transferBus1.index], synthGroup));
// 		buchlaFilters = List.new;
// 		buchlaFilters.add(BuchlaFiltersSynths_Mod(filterGroup0, transferBus0.index, outBus));
// 		buchlaFilters.add(BuchlaFiltersSynths_Mod(filterGroup1, transferBus1.index, outBus+2));
// 		//                 0  1  2  3  4  5   6   7  8  9  10 11   12 13 14  15 16 17  18 19 20   21   22 23 24
// 		//triggerButtons = [44,51,49,57,64,42,  77, 78,79,80, 72,73,  81,82,83, 74,75,76, 67,68,69,  59,  52,53,54];
// 		triggerButtons = [3,11, 9,18,26, 1,41, 42,43,44,35,36];
//
//
// 		//first the synthesis buttons, then the filter buttons, then the export, crazy, load, save
//
// 		//continuousButtons = [3,2,9,1,8,16,23,7,15,22,14,21,29];
// 		continuousButtons = [4,3,11,2, 10,19,27, 9,18,26, 17,25,34];
//
// 		mantaIsSet = false;
// 		midiIsSet = false;
// 		ampSpec = ControlSpec(0,1,'amp');
// 		fmAmpSpecs = List.new;
// 		7.do{fmAmpSpecs.add(ControlSpec(0,3000,'amp'))};
//
// 		currentValList = List.fill(13, 0);
// 		noteDownVol = 1;
// 	}
//
// 	changeSection {arg sectionNum;
// 		this.clearAllManta;
// 		if(sectionNum==0,{
// 			synths[1].set(\mainAmp, 0);
// 			this.setAllManta;
// 			},{
// 				synths[1].set(\mainAmp, 1);
// 				synths[0].set(\amp0, 0);
// 				this.setAllMantaSection2;
// 		});
// 	}
//
// 	setAllSlidersFilters {
// 		rangeSliders[0].valueAction_([currentBuchlaSetting.freq0a, currentBuchlaSetting.freq0b]);
// 		rangeSliders[1].valueAction_([currentBuchlaSetting.freq1a, currentBuchlaSetting.freq1b]);
// 		rangeSliders[2].valueAction_([currentBuchlaSetting.noise0a, currentBuchlaSetting.noise0b]);
// 		rangeSliders[3].valueAction_([currentBuchlaSetting.noise1a, currentBuchlaSetting.noise1b]);
// 		rangeSliders[4].valueAction_([currentBuchlaSetting.noise2a, currentBuchlaSetting.noise2b]);
// 		rangeSliders[5].valueAction_([currentBuchlaSetting.noise3a, currentBuchlaSetting.noise3b]);
// 		rangeSliders[6].valueAction_(currentBuchlaSetting.volume);
// 		buchlaFilters[0].setVals(currentBuchlaSetting.currentFilterSetting0);
// 		buchlaFilters[1].setVals(currentBuchlaSetting.currentFilterSetting1);
// 	}
//
// 	setMIDIKeyboard {
// 		midiResponders.add(
// 			NoteOnResponder({arg src, chan, num, val;
// 				currentDownNote = (num%12);
// 				bankTemp = (currentDownNote/8).floor.asInteger;
// 				settingTemp = currentDownNote%8;
//
// 				if(num<60,{noteDownVol = 1});
// 				if(num>=60,{noteDownVol = 0.25});
// 				if(num>=72,{noteDownVol = 0.1});
//
// 				currentBuchlaSetting = settingBank.setByKeyboard(bankTemp, settingTemp);
// 				{
// 					this.setAllSlidersFilters;
// 					4.do{arg i; settingButtons[i].value = 0};
// 					settingButtons[bankTemp].value = 1;
// 					8.do{arg i; settingButtons[i+4].value = 0};
// 					settingButtons[settingTemp+4].value = 1;
// 					synths[0].set(\gate, 1, \noteDownVol, noteDownVol);
// 				}.defer;
//
// 			},nil,nil,nil,nil)
// 		);
//
// 		midiResponders.add(
// 			NoteOffResponder({arg src, chan, num, val;
// 				num = num%12;
// 				if(currentDownNote == num,{synths[0].set(\gate, 0)});
// 			},nil,nil,nil,nil)
// 		);
// 		midiIsSet = true;
// 	}
//
// 	setAllManta {arg data;
//
// 		//all this needs to be done through midiHidControl
// 		/*			Manta.addNoteOn(triggerButtons[0], {
// 		if(crazyMode==true,{
// 		specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'));
// 		})
// 		});
// 		Manta.addNoteOn(triggerButtons[1], {
// 		if(crazyMode==true,{
// 		specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'));
// 		})
// 		});
// 		Manta.addNoteOn(triggerButtons[2], {
// 		if(crazyMode==true,{
// 		specs.put(2, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'));
// 		})
// 		});
// 		Manta.addNoteOn(triggerButtons[3], {
// 		if(crazyMode==true,{
// 		specs.put(3, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'));
// 		})
// 		});
// 		Manta.addNoteOn(triggerButtons[4], {
// 		if(crazyMode==true,{
// 		specs.put(4, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'));
// 		})
// 		});
//
// 		Manta.addNoteOn(triggerButtons[5], {synths[0].set(\filtTrig, 1)});
//
// 		Manta.addNoteOn(triggerButtons[6], {{filterButtons[0].valueAction = filterButtons[0].value+1}.defer});
//
// 		5.do{arg i;
// 		Manta.addNoteOn(triggerButtons[7+i], {{filterButtons[1+i].valueAction = filterButtons[1+i].value+1}.defer;});
// 		};
//
//
// 		Manta.addNoteOn(triggerButtons[21], {arg val; {{exportButton.valueAction = exportButton.value+1}.defer}});
//
// 		Manta.addPad(continuousButtons[0], {arg val;  currentValList.put(0, val); synths[0].set(\amp0, ampSpec.map((val)/200))});
//
// 		Manta.addPad(continuousButtons[2], {arg val;  currentValList.put(2, val); synths[0].set(\freq0, specs[1].map((val)/200))});
// 		Manta.addPad(continuousButtons[5], {arg val;  currentValList.put(5, val); synths[0].set(\amp3, fmAmpSpecs[2].map((val)/200))});
// 		Manta.addPad(continuousButtons[6], {arg val;  currentValList.put(6, val); synths[0].set(\amp4, fmAmpSpecs[3].map((val)/200))});
// 		Manta.addPad(continuousButtons[9], {arg val;  currentValList.put(9, val); synths[0].set(\freq1, specs[4].map(val/200))});
// 		Manta.addPad(continuousButtons[10], {arg val;  currentValList.put(10, val); synths[0].set(\amp5, fmAmpSpecs[4].map((val)/200))});
// 		Manta.addPad(continuousButtons[11], {arg val;  currentValList.put(11, val); synths[0].set(\amp6, fmAmpSpecs[5].map((val)/200))});
// 		Manta.addPad(continuousButtons[12], {arg val;  currentValList.put(12, val); synths[0].set(\amp7, fmAmpSpecs[6].map((val)/200))});*/
// 		mantaIsSet = true;
// 	}
//
// 	pause {
// 		buchlaFilters.do{|item| item.pause};
// 	}
//
// 	unpause {
// 		buchlaFilters.do{|item| item.unpause};
// 	}
//
// 	setAllMantaSection2 {
// 		//also needs to be done differently
//
// 		/*			4.do{arg i;  Manta.addPad(41+i, {arg val; synths[1].set(("amp"++i.asString).asSymbol, ampSpec.map((val)/200))})};
// 		4.do{arg i;  Manta.addPad(33+i, {arg val; synths[1].set(("amp"++(i+4).asString).asSymbol, ampSpec.map((val)/200))})};
// 		4.do{arg i;  Manta.addPad(25+i, {arg val; synths[1].set(("amp"++(i+8).asString).asSymbol, ampSpec.map((val)/200))})};
// 		4.do{arg i;  Manta.addPad(17+i, {arg val; synths[1].set(("amp"++(i+12).asString).asSymbol, ampSpec.map((val)/200))})};*/
// 	}
//
// 	clearAllMIDI {
// 		midiResponders.do{arg item; item.free};
// 	}
//
// 	clearAllManta {
// 		triggerButtons.do{arg num;
// 			Manta.removeNoteOn(num);
// 		};
// 		continuousButtons.do{arg num;
// 			Manta.removePad(num);
// 		};
// 		mantaIsSet = false;
// 	}
//
// 	save {arg xmlDoc;
// 		xmlSynth = xmlDoc.createElement(modName);
// 		xmlSynth.setAttribute("bounds", win.bounds.asString);
// 		xmlSynth.setAttribute("mantaIsSet", mantaIsSet.asString);
// 		xmlSynth.setAttribute("midiIsSet", midiIsSet.asString);
//
// 		^xmlSynth;
// 	}
//
// 	load {arg xmlSynth;
// 		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
// 		mantaIsSet = (xmlSynth.getAttribute("mantaIsSet")).contains("true");
// 		midiIsSet = (xmlSynth.getAttribute("midiIsSet")).contains("true");
// 		if(mantaIsSet,{assignButtons[0].valueAction_(1)});
// 		if(midiIsSet,{assignButtons[1].valueAction_(1)});
//
// 		win.front;
// 	}
//
// 	killMe {
// 		win.close;
// 		this.clearAllManta;
// 		this.clearAllMIDI;
// 		synths.do{arg item; item.free};
// 		buchlaFilters[0].killMe;
// 		buchlaFilters[1].killMe;
//
// 	}
// }