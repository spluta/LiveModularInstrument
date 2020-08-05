//created after moving the EQ from the main out to before the main mixer

EQ_Mod : Module_Mod {

	*initClass {
		StartUp.add {


			SynthDef("EQ_mod", {arg inBus, outBus, lowDB = 0, midDB = 0, hiDB = 0, vol=1, gate=1, pauseGate = 1;
				var in, env, pauseEnv;

				in = In.ar(inBus,2);

				in = BLowShelf.ar(in, 80, 1, Lag.kr(lowDB));
				in = MidEQ.ar(in, 2500, 1, Lag.kr(midDB));
				in = BHiShelf.ar(in, 12500, 1, Lag.kr(hiDB));

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				Out.ar(outBus, in*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("EQ", Rect(150, 100, 160, 115));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("EQ_mod", [\inBus, mixerToSynthBus, \outBus, outBus], group));

		controls.add(QtEZSlider("Low", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\lowDB, slider.value)}
		}, 0, true));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider("Mid", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\midDB, slider.value)}
		}, 0, true));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider("Hi", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\hiDB, slider.value)}
		}, 0, true));
		this.addAssignButton(2,\continuous);

		controls.add(Button().maxWidth_(40)
			.states_([["reset", Color.blue, Color.black]],[["reset", Color.blue, Color.black]])
			.action_{
				3.do{|i| controls[i].valueAction_(0)};
				this.sendGUIVals;
			}
		);
		this.addAssignButton(3,\onOff);

		win.layout_(
			VLayout(
				HLayout(controls[0], controls[1],controls[2],controls[3]),
				HLayout(assignButtons[0].maxWidth_(40), assignButtons[1].maxWidth_(40), assignButtons[2].maxWidth_(40), assignButtons[3].maxWidth_(40))
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}
}

// EQFilter_Mod {
// 	var inBus, outBus, panel, point, group, filterType, specRanges, controls, controlCounter, mainModule, filter, text, numChannels, freqBus, rqBus, dbBus;
//
// 	*new {arg inBus, outBus, panel, point, group, filterType, specRanges, controls, controlCounter, mainModule;
// 		^super.newCopyArgs(inBus, outBus, panel, point, group, filterType, specRanges, controls, controlCounter, mainModule).init;
// 	}
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("eqLowFilter_mod", {arg inBus, freqBus, rqBus, dbBus, lagTime=0, gate = 1, pauseGate = 1;
// 				var in, out, env, pauseEnv, freq, rq, db;
//
// 				freq = In.kr(freqBus);
// 				rq = In.kr(rqBus);
// 				db = In.kr(dbBus);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				in  = In.ar(inBus, 8);
//
// 				out = BLowShelf.ar(in,freq,rq,Lag.kr(db, lagTime)+LFNoise1.kr(0.1));
//
// 				ReplaceOut.ar(inBus, out*env*pauseEnv);
// 			}).writeDefFile;
// 			SynthDef("eqMidFilter_mod", {arg inBus, freqBus, rqBus, dbBus, lagTime=0, gate = 1, pauseGate = 1;
// 				var in, out, env, pauseEnv, freq, rq, db;
//
// 				freq = In.kr(freqBus);
// 				rq = In.kr(rqBus);
// 				db = In.kr(dbBus);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				in  = In.ar(inBus, 8);
//
// 				out = MidEQ.ar(in,freq, Lag.kr(rq, lagTime), Lag.kr(db, lagTime)+LFNoise1.kr(0.1));
//
// 				ReplaceOut.ar(inBus, out*env*pauseEnv);
// 			}).writeDefFile;
// 			SynthDef("eqHighFilter_mod", {arg inBus, freqBus, rqBus, dbBus, lagTime=0, gate = 1, pauseGate = 1;
// 				var in, out, env, pauseEnv, freq, rq, db;
//
// 				freq = In.kr(freqBus);
// 				rq = In.kr(rqBus);
// 				db = In.kr(dbBus);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				in  = In.ar(inBus, 8);
//
// 				out = BHiShelf.ar(in,freq, rq,Lag.kr(db, lagTime)+LFNoise1.kr(0.1));
//
// 				ReplaceOut.ar(inBus, out*env*pauseEnv);
// 			}).writeDefFile;
// 			SynthDef("eqOut_mod", {arg inBus, outBus, gate = 1, pauseGate = 1;
// 				var in, out, env, pauseEnv;
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				in  = In.ar(inBus, 8);
//
// 				Out.ar(outBus, in*env*pauseEnv);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		text = StaticText(panel, Rect(point.x, point.y, 60, 20));
//
// 		numChannels = 8;
//
// 		freqBus = Bus.control(group.server);
// 		rqBus = Bus.control(group.server);
// 		dbBus = Bus.control(group.server);
//
// 		this.makeFilter;
//
// 		if(filterType!=3,{
// 			controls.add(EZKnob.new(panel, Rect(point.x, point.y+20, 60, 100), "rq", ControlSpec(0.001, 1, 'linear'),
// 				{arg val;
// 					rqBus.set(val.value);
// 				}, 0.5, true
// 			));
// 			mainModule.addAssignButton(controlCounter*3,\continuous, Rect(point.x, point.y+120, 60, 20));
//
// 			controls.add(EZKnob.new(panel, Rect(point.x, point.y+140, 60, 100), "Freq", ControlSpec(specRanges[0], specRanges[1], 'exponential'),
// 				{arg val;
// 					freqBus.set(val.value);
// 				}, specRanges[1]/2, true
// 			));
// 			mainModule.addAssignButton(controlCounter*3+1,\continuous, Rect(point.x, point.y+240, 60, 20));
//
// 			controls.add(EZKnob.new(panel, Rect(point.x, point.y+260, 60, 100), "db", ControlSpec(-15, 15, 'linear'),
// 				{arg val;
// 					dbBus.set(val.value);
// 				}, 0, true
// 			));
// 			mainModule.addAssignButton(controlCounter*3+2,\continuous, Rect(point.x, point.y+360, 60, 20));
// 		})
// 	}
//
// 	makeFilter {
// 		switch(filterType,
// 			0,{
// 				filter = Synth("eqLowFilter_mod",[\inBus, inBus, \freqBus, freqBus, \rqBus, rqBus, \dbBus, dbBus], group, \addToTail);
// 				text.string = "LowSh";
// 			},
// 			1,{
// 				filter = Synth("eqMidFilter_mod",[\inBus, inBus, \freqBus, freqBus, \rqBus, rqBus, \dbBus, dbBus], group, \addToTail);
// 				text.string = "Mid";
// 			},
// 			2,{
// 				filter = Synth("eqHighFilter_mod",[\inBus, inBus, \freqBus, freqBus, \rqBus, rqBus, \dbBus, dbBus], group, \addToTail);
// 				text.string = "HiSh";
// 			},
// 			3,{
// 				filter = Synth("eqOut_mod",[\inBus, inBus, \outBus, outBus], group, \addToTail);
// 			}
// 		);
// 	}
//
// 	pause {
// 		filter.set(\pauseGate, 0);
// 	}
//
// 	unpause {
// 		if(filter!=nil, {
// 			filter.set(\pauseGate, 1);
// 			filter.run(true);
// 		});
// 	}
//
// 	killMe {
// 		if(filter!=nil, {filter.set(\gate, 0)});
// 	}
// }
//
// EQ_Mod : Module_Mod {
// 	var filters, transferBus, rout;
//
// 	init {
// 		this.makeWindow("EQ", Rect(700, 500, 360, 420));
// 		this.makeMixerToSynthBus(8);
//
// 		this.initControlsAndSynths(18);
//
// 		controls = List.new;
// 		filters = List.new;
// 		//rout = Routine.new({
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 0@0, group, 0, [20, 400], controls, 0, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 60@0, group, 1, [20, 400], controls, 1, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 120@0, group, 1, [300, 600], controls, 2, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 180@0, group, 1, [500, 2500], controls, 3, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 240@0, group, 1, [1500, 5000], controls, 4, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 300@0, group, 2, [2000, 10000], controls, 5, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 360@0, group, 3, [0, 0], controls, 6, this));
// 		//});
// 		//AppClock.play(rout);
//
// 	}
//
// 	pause {
// 		filters.do{|item| item.pause};
// 	}
//
// 	unpause {
// 		filters.do{|item| item.unpause};
// 	}
//
// 	killMeSpecial {
// 		filters.do{|item| item.killMe};
// 	}
// }
//
// EQmini_Mod : Module_Mod {
// 	var filters, transferBus;
//
// 	init {
// 		this.makeWindow("EQmini", Rect(700, 500, 180, 420));
// 		this.makeMixerToSynthBus(8);
//
// 		this.initControlsAndSynths(9);
//
// 		controls = List.new;
// 		filters = List.new;
// 		//rout = Routine.new({
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 0@0, group, 0, [20, 400], controls, 0, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 60@0, group, 1, [100, 5000], controls, 1, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 120@0, group, 2, [2000, 10000], controls, 2, this));
// 		//	0.1.wait;
// 		filters.add(EQFilter_Mod(mixerToSynthBus.index, outBus.index, win, 0@0, group, 3, [0, 0], controls, 3, this));
// 		//});
// 		//AppClock.play(rout);
//
// 	}
//
// 	pause {
// 		filters.do{|item| item.pause};
// 	}
//
// 	unpause {
// 		filters.do{|item| item.unpause};
// 	}
//
// 	killMeSpecial {
// 		filters.do{|item| item.killMe};
// 	}
// }
