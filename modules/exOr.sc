//Drum_Mod {
//	var <>server, <>group, <>arduino, <>audioInBus, <>audioOutBus, <>arduinoOutChan, transferBus, verbGroup, buchlaGroup, buchlaFilters, synth, arduinoSeq, arduinoMessage, audioInSynth, audioInGroup, verbSynth;
//	var spec0, spec1, spec2, spec3, midVal, triggerVals;
//
//	*new {arg server, group, arduino, audioInBus, audioOutBus, arduinoOutChan;
//		^super.newCopyArgs(server, group, arduino, audioInBus, audioOutBus, arduinoOutChan).init;
//	}
//	
//	*initClass{
//		StartUp.add {
//			SynthDef(\audioInStraight_mod, {arg inBus, outBus;
//				var in;
//				
//				in = In.ar(inBus);
//				
//				Out.ar(outBus, in)
//			}).writeDefFile;
//			
//			SynthDef(\drumVerb_mod, {arg inBus, outBus, roomsize, revtime, damping, inputbw, spread = 15, dryLevel, verbLevel, earlylevel, taillevel, pitchShift, bigVol;
//				var in, verb;
//				
//				in = In.ar(inBus);
//				
//				verb = in+PitchShift.ar(in, 0.02, 0.5, 0.0);
//				
//				verb = GVerb.ar(
//					in,
//					roomsize, 
//					revtime, 
//					damping, 
//					inputbw, 
//					spread, 
//					dryLevel,
//					earlylevel.dbamp, 
//					taillevel.dbamp,
//					roomsize, 0.3) + in;
//				
//				verb = PitchShift.ar(verb, 0.02, pitchShift, 0.0);
//				
//				ReplaceOut.ar(outBus, (Pan2.ar((in*dryLevel), Rand(-1,1))+(verb*verbLevel))*bigVol)
//			}).writeDefFile;
//		}
//	}
//	
//	init {
//		arduinoSeq = Pseq([[arduinoOutChan.value,0],[arduinoOutChan.value,1]],inf).asStream;
//	
//		audioOutBus.postln;
//	
//		transferBus = Bus.audio(server);
//
//		audioInGroup = Group.tail(group);
//		verbGroup = Group.tail(group);
//		buchlaGroup = Group.tail(group);
//
//		audioInSynth = Synth("audioInStraight", [\inBus, audioInBus, \outBus, transferBus.index], audioInGroup);
//		verbSynth = Synth(\drumVerb, [\inBus, transferBus.index, \outBus, transferBus.index, \roomsize, 300, \revtime, 5, \damping, 0.43, \inputbw, 0.51, \drylevel, 0, \earlylevel, -26, \taillevel, -20, \pitchShift, 0.5, \verbLevel, 0, \bigVol, 1], verbGroup);
//		buchlaFilters = BuchlaFilters(nil, buchlaGroup, transferBus.index, [audioOutBus]);
//	}
//	
//	triggerHighDrum {
//		arduinoMessage = arduinoSeq.next;
//		arduino.send($w, $d, arduinoMessage[0], arduinoMessage[1]);
//		buchlaFilters.triggerRange(3, 2, -12);
//		buchlaFilters.triggerRange(0, 0, -12);
//		buchlaFilters.triggerRange(12, 2, 12);
//		buchlaFilters.triggerRange(14, 0, 12);
//		buchlaFilters.triggerRangeNormal([6,7,8,9], 5);
//	}
//	
//	triggerMidDrum {
//		arduinoMessage = arduinoSeq.next;
//		arduino.send($w, $d, arduinoMessage[0], arduinoMessage[1]);
//		buchlaFilters.triggerRangeNormal([0,1,2,3,], 5);
//		buchlaFilters.triggerRange(5, 2, 12);
//		buchlaFilters.triggerRange(0, 0, 12);
//		buchlaFilters.triggerRange(12, 2, 12);
//		buchlaFilters.triggerRange(14, 0, 12);
//	}
//	
//	triggerLowDrum {
//		arduinoMessage = arduinoSeq.next;
//		arduino.send($w, $d, arduinoMessage[0], arduinoMessage[1]);
//		buchlaFilters.triggerRange(3, 2, 12);
//		buchlaFilters.triggerRange(0, 0, 12);
//		buchlaFilters.triggerRange(12, 2, -12);
//		buchlaFilters.triggerRange(14, 0, -12);
//		buchlaFilters.triggerRangeNormal([6,7,8,9], 5);
//	}
//	
//	triggerRandomDrum {
//		arduinoMessage = arduinoSeq.next;
//		arduino.send($w, $d, arduinoMessage[0], arduinoMessage[1]);
//		buchlaFilters.trigger;
//	}
//	
//	triggerPeak {arg num;  //num is a value between 0 and 1, 1 being high, 0 being low
//		
//		"triggerPeak".postln;
//		num.postln;
//		
//		spec0 = ControlSpec(0,14, 'linear');
//		
//		midVal = spec0.map(num);
//		
//		num.postln;
//		
//		spec1 = ControlSpec(0,midVal, 'linear');
//		spec2 = ControlSpec(midVal,14, 'linear');		
//		spec3 = ControlSpec(-11, 7, 'linear');
//		
//		triggerVals = List.new;
//		
//		15.do{arg i;
//			if(i<midVal,{
//				triggerVals.add(spec3.map(spec1.unmap(i)+1.rand2));
//			},{
//				triggerVals.add(spec3.map(spec2.unmap(i)+1.rand2));
//			});
//		};
//		verbSynth.set(\dryLevel, num, \verbLevel, 1-num);
//		buchlaFilters.triggerPeak(triggerVals);
//		
//		triggerVals.postln;
//		arduinoMessage = arduinoSeq.next;
//		arduino.send($w, $d, arduinoMessage[0], arduinoMessage[1]);
//	}
//	
//	setVerbLevels {arg dryLevel, verbLevel;
//		"setVerbLevels".postln;
//		verbSynth.set(\dryLevel, dryLevel, \verbLevel, verbLevel)
//	}
//	
//	setBigVol {arg vol;
//		verbSynth.set(\bigVol, vol);
//	}
//}
//
//DrumModule_Mod {
//	var <>server, <>drums, drumCounter, playCounter, playCountTo, playList, wetDrumStream, dryDrumStream, num;
//	
//	*new {arg server, drums;
//		^super.newCopyArgs(server, drums).init;
//	}
//
//	init {
//		wetDrumStream = Prand(#[0,1], inf).asStream;
//		dryDrumStream = Prand(#[2,3], inf).asStream;
//		playList = List.new;
//		rrand(4,11).do{
//			if(0.75.coin,{
//				playList.add([\high,\mid,\low].choose);
//			},{
//				playList.add(nil);
//			})
//		};
//		drumCounter = 0;
//		playCounter = 0;
//		playCountTo = rrand(3,5);
//	}
//
//	reset {
//		playList.postln;
//		if(playCounter>playCountTo,{
//			if(playList.size>10,{
//				playList.removeAt(playList.size.rand);
//			},{
//				if(playList.size>3,{
//					[{
//						num = playList.size.rand;
//						playList.rotate(num);
//						playList.add([\high,\mid,\low].choose);
//						playList.rotate(num.neg);},
//					{
//						playList.removeAt(playList.size.rand);
//					},
//					{
//						playList.put(playList.size.rand, [\high,\mid,\low,nil].choose)
//					}].choose.value;
//				},{
//					num = playList.size.rand;
//					playList.rotate(num);
//					playList.add([\high,\mid,\low].choose);
//					playList.rotate(num.neg);
//				})
//			});
//			playCounter = 0;
//			playCountTo = rrand(3,5);
//		},{
//			playCounter = playCounter+1;
//		});
//	}
//
//	playNext {//add augmentation
//		if(drumCounter<=playList.size,{
//			switch(playList[drumCounter],
//				\high,{drums[dryDrumStream.next].triggerHighDrum},
//				\mid,{drums[dryDrumStream.next].triggerMidDrum},
//				\low,{drums[wetDrumStream.next].triggerLowDrum},
//				nil,{}
//			);
//			if(0.75.coin,{
//				drumCounter = drumCounter+1;
//			});
//			^true;
//		},{	
//			drumCounter = 0;
//			this.reset;
//			^false;
//		})
//			
//	}
//}
//
//DrumSpiralPlayer_Mod {
//	var <>server, <>drums, playCounter, playCountTo, drumStream, routs, routStream, routNum;
//	
//	*new {arg server, drums;
//		^super.newCopyArgs(server, drums).init;
//	}
//
//	init {
//		drums.postln;
//		drumStream = Prand(#[0,1,2,3], inf).asStream;
//		this.reset;
//		routs = List.new;
//		4.do{	
//			routs.add(Routine({loop{
//				"play".postln;
//				this.playNext;
//				((1/20)+((playCounter/playCountTo)/10)).wait;
//			}}));
//		};
//		routStream = Pseq(#[0,1,2,3], inf).asStream;
//		routNum = routStream.next;
//	}
//	
//	stop {
//		routs[routNum].stop;
//		routNum = routStream.next;
//	}
//	
//	start {
//		routs[routNum].reset;
//		routs[routNum].play;
//	}
//	
//	reset {
//		playCounter = 0;
//		playCountTo = rrand(50,100);
//	}
//
//	playNext {
//		"playNext".postln;
//		if(playCounter<playCountTo,{
//			playCounter = playCounter+1;
//			playCounter.postln;
//			drums[drumStream.next].triggerPeak(1-(playCounter/playCountTo));
//		},{	
//			this.reset;
//		});	
//	}
//}
//
//DrumModulePlayer_Mod {
//	var <>server, <>drums, <>waitFunc, drumModules, currentModule, routs, routStream, routNum;
//	
//	*new {arg server, drums, waitFunc;
//		^super.newCopyArgs(server, drums, waitFunc).init;
//	}
//	
//	init {
//		drumModules = List.new;
//		3.do{drumModules.add(DrumModule(server,drums))};
//		currentModule = drumModules.choose;
//		routs = List.new;
//		4.do{	
//			routs.add(Routine({loop{
//				if(currentModule.playNext.not,{
//					currentModule = drumModules.choose;
//				});
//				waitFunc.value.wait;
//			}}));
//		};
//		routStream = Pseq(#[0,1,2,3], inf).asStream;
//		routNum = routStream.next;
//	}
//	
//	stop {
//		routs[routNum].stop;
//		routNum = routStream.next;
//	}
//	
//	start {
//		routs[routNum].reset;
//		routs[routNum].play;
//	}
//}
//
//DrumTriggerPanel_Mod {
//	var <>server, <>group, <>drumBus0, <>drumBus1, <>win, <>point, <>arduino, <>sampleTriggerPanel, <>drums, <>func, panel, label, <>button, <>timeSlider, <>regularitySlider, <>skipSlider, buttonState, routs, routStream, rout, routNum, knobWait, regularityValue, skipChance, waitTime, drums, dryDrumStream, wetDrumStream, funcs, <>funcNum, drumModulePlayer, spiralButton, spiralPlayer;
//
//	*new {arg server, group, drumBus0, drumBus1, win, point, arduino, sampleTriggerPanel;
//		^super.newCopyArgs(server, group, drumBus0, drumBus1, win, point, arduino, sampleTriggerPanel).init;
//	}
//
//	init {
//		
//		panel = CompositeView(win, Rect(point.x, point.y, 250, 110)).relativeOrigin_(true);
//		
//		button = Button.new(panel,Rect(0, 20, 250, 20))
//			.states_([ [ "pulse", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ], [ "free", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ],  [ "modules", Color.black, Color.white ],["nothing", Color.white, Color.black]])
//			.action_{|v|
//				routs[routNum].stop;
//				drumModulePlayer.stop;
//				spiralPlayer.stop;
//				
//				this.resetDrumVerb;
//				
//				if(v.value<2,{
//					routNum = routStream.next;
//					routs[routNum].reset;
//					buttonState = v.value;
//					routs[routNum].play;
//				},{
//					switch(v.value,
//						2,{
//							drumModulePlayer.start;
//						},
//						3,{
//							"stop".postln;
//						}
//					)
//				})
//			};
//		buttonState = 0;
//		
//		spiralButton = Button.new(panel,Rect(0, 0, 250, 20))
//			.states_([ [ "spiral", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
//			.action_{|v|
//				routs[routNum].stop;
//				drumModulePlayer.stop;
//				spiralPlayer.reset;
//				spiralPlayer.start;
//			};
//		
//		label = StaticText.new(panel,Rect(0, 0, 250, 20))
//			.string_("DrumTriggerPanel");
//		timeSlider = EZSlider2.new(panel,Rect(0, 40, 250, 20),'waitTime', ControlSpec(0.05, 1.0),
//		{|v| 
//			waitTime = v.value;
//			drumModulePlayer.waitFunc = {rrand(v.value*0.9,v.value*1.1)};
//		}, waitTime = 0.25);
//		regularitySlider = EZSlider2.new(panel,Rect(0, 60, 250, 20), 'regularity', ControlSpec(0, 0.25),{|v| regularityValue = v.value}, regularityValue = 0);
//		skipSlider = EZSlider2.new(panel,Rect(0, 80, 250, 20), 'skipChance', ControlSpec(0,0.75),{|v| skipChance = v.value}, skipChance = 0);
//		
//		drums = List.new;
//		drums.add(Drum_Mod(server, group, arduino, 0, drumBus0.index, 2));
//		drums.add(Drum_Mod(server, group, arduino, 1, drumBus1.index+1, 3));
//		drums.add(Drum_Mod(server, group, arduino, 2, drumBus0.index+1, 4));
//		drums.add(Drum_Mod(server, group, arduino, 3, drumBus1.index, 5));
//		
//		this.resetDrumVerb;
//		
//		drumModulePlayer = DrumModulePlayer(server, drums, {rrand(0.09,0.11)});
//		
//		spiralPlayer = DrumSpiralPlayer(server, drums);
//		
//		wetDrumStream = Prand(#[0,1], inf).asStream;
//		dryDrumStream = Prand(#[2,3], inf).asStream;
//		
//		funcs = List.new;
//		funcs.add({0.5.wait});
//		funcs.add({
//			//counter = 0;
//			rrand(2,3).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(3.0, 4.5).wait;
//			};
//		}); 	
//		funcs.add({
//			//counter = 0;
//			sampleTriggerPanel.trigger;
//			rrand(2,3).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(0.9, 1.1).wait;
//			};
//			rrand(11,19).do{
//				if(0.5.coin, {
//					drums[dryDrumStream.next].triggerHighDrum;
//				},{
//					drums[dryDrumStream.next].triggerRandomDrum;
//				});
//				0.12.wait;
//			};
//	
//			rrand(0.0,3.0).wait;
//		}); 	
//		funcs.add({
//			//counter = 0;
//			sampleTriggerPanel.trigger;
//			rrand(1,2).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(0.9, 1.1).wait;
//			};
//			rrand(7,13).do{
//				if(0.5.coin, {
//					drums[dryDrumStream.next].triggerHighDrum;
//				},{
//					drums[dryDrumStream.next].triggerRandomDrum;
//				});
//				0.12.wait;
//			};
//			sampleTriggerPanel.trigger;
//			rrand(1,2).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(1.3, 1.5).wait;
//			};
//			rrand(9,15).do{
//				if(0.5.coin, {
//					drums[dryDrumStream.next].triggerHighDrum;
//				},{
//					drums[dryDrumStream.next].triggerRandomDrum;
//				});
//				0.12.wait;
//			};
//			rrand(0.0,3.0).wait;
//		});
//		funcs.add({
//			//counter = 0;
//			sampleTriggerPanel.trigger;
//			rrand(1,2).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(1.1, 1.5).wait;
//			};
//			rrand(0.0,2.0).wait;
//			rrand(5,11).do{
//				if(0.5.coin, {
//					drums[dryDrumStream.next].triggerHighDrum;
//				},{
//					drums[dryDrumStream.next].triggerRandomDrum;
//				});
//				0.12.wait;
//			};
//			rrand(0.0,2.0).wait;
//			sampleTriggerPanel.trigger;
//			rrand(1,2).do{
//				drums[wetDrumStream.next].triggerLowDrum;
//				rrand(1.7, 1.9).wait;
//			};
//			rrand(7,13).do{
//				if(0.5.coin, {
//					drums[dryDrumStream.next].triggerHighDrum;
//				},{
//					drums[dryDrumStream.next].triggerRandomDrum;
//				});
//				0.12.wait;
//			};
//			rrand(0.0,3.0).wait;
//		});
//		
//		funcNum = 0;
//		
//		routs = List.new;
//		4.do{	
//			routs.add(Routine({loop{
//				if(buttonState == 0,{
//					funcs[funcNum].value;
//				},{
//					if(skipChance.coin.not,{
//						drums[dryDrumStream.next].triggerRandomDrum;
//					});
//					knobWait = waitTime*(1+(regularityValue.rand2));
//					knobWait.wait;
//				})
//			}}));
//		};
//		routStream = Pseq(#[0,1,2,3], inf).asStream;
//		routNum = routStream.next;
//		routs[routNum].play;
//	}
//	
//	resetDrumVerb {
//		drums[0].setVerbLevels(0, 0.9);
//		drums[1].setVerbLevels(0, 0.9);
//		drums[2].setVerbLevels(0.9, 0);
//		drums[3].setVerbLevels(0.9, 0);
//	}
//	
//	changeFunc {arg funcNumIn;
//		routs[routNum].stop;
//		funcNum = funcNumIn;
//		routNum = routStream.next;
//		routs[routNum].reset;
//		routs[routNum].play;
//	}
//}
//
//FakeArduino {
//	*new {
//		^super.newCopyArgs().init;
//	}
//	
//	init {
//	}
//	
//	send {arg a,b,c,d;
//	
//	}
//}
//
//ExOrWin_Mod {
//	var <>server, <>win, <>arduino, <>synth0, <>synth1, text, sectionButton, buchlaSlider, synthGroup, drumSlider0, drumSlider1, sampleSlider0, sampleSlider1, jeffTriggerPanel, meapTriggerPanel, drumTriggerPanel, deviceList, buchlaModel, buchlaBus, tempBus, mixer, mixerGroup, drumBus0, drumBus1, sampleBus0, sampleBus1, sampleTriggerPanel, last5Val;
//	
//	*new {arg server, win, arduino;
//		^super.newCopyArgs(server, win, arduino).init;
//	}
//	
//	*initClass {
//		StartUp.add {
//			SynthDef("exOrLiveMixer_mod", {arg buchlaBus, buchlaVol, drumBus0, drumVol0, drumBus1, drumVol1, sampleBus0, sampleVol0, sampleBus1, sampleVol1, outBus0, outBus1;
//				var buchla, drum0, drum1, sample0, sample1, out0, out1, out2, out3;
//				
//				drum0 = In.ar(drumBus0,2)*drumVol0;
//				drum1 = In.ar(drumBus1,2)*drumVol1;
//				
//				out0 = buchla+drum0+drum1+[sample0, sample0]+sample1;
//				
//				out2 = (buchla*3)+drum0+([sample0, sample0]*2);
//				out3 = drum1+(sample1*4);
//				
//				out2 = Compander.ar(out2, out2,
//					thresh: 0.9,
//					slopeBelow: 1,
//					slopeAbove: 0.5,
//					clampTime: 0.01,
//					relaxTime: 0.01
//				);
//				
//				out3 = Compander.ar(out3, out3,
//					thresh: 0.9,
//					slopeBelow: 1,
//					slopeAbove: 0.5,
//					clampTime: 0.01,
//					relaxTime: 0.01
//				);
//				
//				Out.ar(0, out0);
//				Out.ar(4, out2);
//				Out.ar(6, out3);
//				
//			}).writeDefFile
//		}
//	}
//	
//	init {	
//		text = StaticText.new(win,Rect(20, 14, 796, 64)).string_("exclusiveOr").font(Font("AmericanTypewriter-Light", 36));
//
//		buchlaBus = Bus.audio(server, 2);
//		tempBus =  Bus.audio(server, 2);
//		drumBus0 = Bus.audio(server, 2);
//		drumBus1 = Bus.audio(server, 2);
//		sampleBus0 = Bus.audio(server, 2);
//		sampleBus1 = Bus.audio(server, 2);
//		
//		synthGroup = Group.tail(server);
//		mixerGroup = Group.tail(server);
//		
//		mixer = Synth("exOrLiveMixer", [\buchlaBus, buchlaBus.index, \buchlaVol, 0, \drumBus0, drumBus0.index, \drumVol0, 0, \drumBus1, drumBus1.index, \drumVol1, 0, \sampleBus0, sampleBus0.index, \sampleVol0, 0, \sampleBus1, sampleBus1.index, \sampleVol1, 0, \outBus0, 0, \outBus1, 2], mixerGroup);
//		
//		sectionButton = Button.new(win,Rect(24, 104, 172, 60))
//			.states_([ [ "Start", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
//				[ "Section 1", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
//				[ "Section 2", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
//				[ "Section 3", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
//				[ "Section 4", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
//			])
//			.action_{|butt| 
//				this.goToSection(butt.value);
//			};
//			
//		buchlaSlider = EZSlider2.new(win,Rect(20, 214, 90, 220),"buchlaVol", ControlSpec(0, 1.0, \amp),
//			{|val| 
//				mixer.set(\buchlaVol, val.value);
//			}, 0);
//		drumSlider0 = EZSlider2.new(win,Rect(130, 214, 90, 220),"DrumVol0", ControlSpec(0, 1.0, \amp),
//			{|val| 
//				mixer.set(\drumVol0, val.value);
//			}, 0);
//		drumSlider1 = EZSlider2.new(win,Rect(240, 214, 90, 220),"DrumVol1", ControlSpec(0, 1.0, \amp),
//			{|val| 
//				mixer.set(\drumVol1, val.value);
//			}, 0);
//		sampleSlider0 = EZSlider2.new(win,Rect(350, 214, 90, 220),"Samples", ControlSpec(0, 1.0, \amp),
//			{|val| 
//				mixer.set(\sampleVol0, val.value);
//			}, 0);
//		sampleSlider1 = EZSlider2.new(win,Rect(460, 214, 90, 220),"LongSamps", ControlSpec(0, 1.0, \amp),
//			{|val| 
//				mixer.set(\sampleVol1, val.value);
//			}, 0);
//		jeffTriggerPanel = JeffTriggerPanel(win, Point(570, 104), arduino);
//		sampleTriggerPanel = SampleTriggerPanel(server, synthGroup, sampleBus0, sampleBus1, win, Point(570, 214));
//		drumTriggerPanel = DrumTriggerPanel(server, synthGroup, drumBus0, drumBus1, win, Point(570, 324), arduino, sampleTriggerPanel);
//		
//		buchlaModel = BuchlaModel(server, synthGroup, buchlaBus, jeffTriggerPanel, sampleTriggerPanel, drumTriggerPanel, sectionButton);
//		
//		last5Val = 0;
//		
//		MIDIClient.init(3,3);			// explicitly intialize the client
//		3.do({ arg i; 
//			MIDIIn.connect(i, MIDIClient.sources.at(i));
//		});
//		MIDIIn.control = { arg src, chan, num, val;
//			src.postln;
//			{switch(src,
//				1266907545,{	
//					switch(num,
//						7, {
//							switch(chan,
//								0,{
//									buchlaSlider.value_(val/127);
//								},
//								1,{
//									drumSlider0.value_(val/127);
//								},
//								2,{
//									drumSlider1.value_(val/127);
//								},
//								3,{
//									sampleSlider0.value_(val/127);
//								},
//								4,{
//									sampleSlider1.value_(val/127);
//								}
//							)
//						},
//						13, {
//							switch(chan,
//								0,{
//									jeffTriggerPanel.timeSlider.value_(jeffTriggerPanel.timeSlider.controlSpec.map(val/127));
//								},
//								1,{
//									sampleTriggerPanel.timeSlider.value_(sampleTriggerPanel.timeSlider.controlSpec.map(val/127));
//								},
//								2,{
//									drumTriggerPanel.timeSlider.value_(drumTriggerPanel.timeSlider.controlSpec.map(val/127));
//								}
//							)
//						},
//						12, {												switch(chan,
//								0,{
//									jeffTriggerPanel.regularitySlider.value_(jeffTriggerPanel.regularitySlider.controlSpec.map(val/127));
//								},
//								1,{
//									sampleTriggerPanel.regularitySlider.value_(sampleTriggerPanel.regularitySlider.controlSpec.map(val/127));
//								},
//								2,{
//									drumTriggerPanel.regularitySlider.value_(drumTriggerPanel.regularitySlider.controlSpec.map(val/127));
//								}
//							)
//						},
//						10, {
//							switch(chan,
//								0,{
//									jeffTriggerPanel.skipSlider.value_(jeffTriggerPanel.skipSlider.controlSpec.map(val/127));
//								},
//								1,{
//									sampleTriggerPanel.skipSlider.value_(sampleTriggerPanel.skipSlider.controlSpec.map(val/127));
//								},
//								2,{
//									drumTriggerPanel.skipSlider.value_(drumTriggerPanel.skipSlider.controlSpec.map(val/127));
//								}
//							)
//						},
//						18, {
//							sectionButton.valueAction = sectionButton.value+1;
//						},
//						19, {
//							jeffTriggerPanel.button.valueAction = jeffTriggerPanel.button.value+1;
//						},
//						20, {
//							sampleTriggerPanel.button.valueAction = sampleTriggerPanel.button.value+1;
//						},
//						21, {
//							drumTriggerPanel.button.valueAction = 0;
//						},
//						24, {
//							drumTriggerPanel.button.valueAction = 1;
//						},
//						27, {
//							drumTriggerPanel.button.valueAction = 2;
//						},
//						26, {
//							drumTriggerPanel.button.valueAction = 3;
//						}
//					)
//				}
//			)}.defer;
//		};
//		MIDIIn.noteOn = { arg src, chan, num, val;
//			{switch(num,
//				0,{drumTriggerPanel.button.valueAction_(1)},
//				1,{jeffTriggerPanel.button.valueAction_(1)},
//				2,{drumTriggerPanel.button.valueAction_(2)}
//			)}.defer
//		};
//		MIDIIn.noteOff = { arg src, chan, num, val;
//			{switch(num,
//				0,{drumTriggerPanel.button.valueAction_(0)},
//				1,{jeffTriggerPanel.button.valueAction_(0)}
//			)}.defer
//		};
//		
//	}
//
//	goToSection {arg sectionNum;
//		switch(sectionNum, 
//			1,{
//				drumTriggerPanel.changeFunc(1);
//				jeffTriggerPanel.changeFunc(1);
//			},
//			2,{
//				drumTriggerPanel.changeFunc(2);
//				jeffTriggerPanel.changeFunc(2);
//			},
//			3,{
//				sampleTriggerPanel.playLongSample(0);
//				drumTriggerPanel.changeFunc(3);
//				//jeffTriggerPanel.changeFunc(3);
//			},
//			4,{
//				drumTriggerPanel.changeFunc(4);
//				//jeffTriggerPanel.changeFunc(4);
//			}
//		)
//		
//	}
//
//}