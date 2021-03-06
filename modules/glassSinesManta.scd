// GlassSineObject : Module_Mod {
// 	var win, sineWaves, midiNoteNum, functions, interval, availableIntervals, win, availableIntervalsList, mantaRow, <>layout;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("glassSine2", {arg freq, volBus, outBus, vol=0, gate = 1, pauseGate = 1;
// 				var sine, env, pauseEnv, mainVol;
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
//
// 				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);
//
// 				mainVol = Lag.kr(In.kr(volBus), 0.1);
//
// 				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv*mainVol, Rand(-1, 1)));
// 			}).writeDefFile;
//
// 			SynthDef("glassSine4", {arg freq, volBus, outBus, vol=0, gate = 1, pauseGate = 1;
// 				var sine, env, pauseEnv, mainVol;
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
//
// 				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);
//
// 				mainVol = Lag.kr(In.kr(volBus), 0.1);
//
// 				Out.ar(outBus, PanAz.ar(4, sine*AmpComp.kr(freq)*env*pauseEnv*mainVol, Rand(-1, 1)));
// 			}).writeDefFile;
//
// 			SynthDef("glassSine8", {arg freq, volBus, outBus, vol=0, gate = 1, pauseGate = 1;
// 				var sine, env, pauseEnv, mainVol;
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
//
// 				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);
//
// 				mainVol = Lag.kr(In.kr(volBus), 0.1);
//
// 				Out.ar(outBus, PanAz.ar(8, sine*AmpComp.kr(freq)*env*pauseEnv*mainVol, Rand(-1, 1)));
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {}
//
// 	setWinPoint {arg rowNum, volBus;
//
// 		this.initControlsAndSynths(4);
// 		oscMsgs = List.newClear(8);
//
// 		//if(win.isNil, {win = winIn});
//
// 		midiNoteNum = 70;
//
// 		availableIntervals = ["11/10","11/9","9/7","13/7"];
// 		interval = availableIntervals[0].interpret;
//
// 		sineWaves = List.new;
// 		4.do{arg i;
// 			sineWaves.add(Synth("glassSine2", [\freq, midiNoteNum.midicps-(i*5), \volBus, volBus, \outBus, outBus], group));
// 		};
// 		4.do{arg i;
// 			sineWaves.add(Synth("glassSine2", [\freq, (midiNoteNum.midicps*interval)-((i+4)*5), \volBus, volBus, \outBus, outBus], group));
// 		};
//
// 		controls.add(QtEZSlider(
// 			"Cutoff", // label
// 			ControlSpec(20, 124, \linear, 0.5), // control spec
// 			{|ez|
// 				midiNoteNum=ez.value;
// 				this.setFreq;
// 			},// action
// 			midiNoteNum, // initVal
// 			true, \horz
// 		));
//
// 		controls.add(PopUpMenu()
// 			.action_{|v|
// 				interval = availableIntervals[v.value].interpret;
//
// 				this.setFreq;
//
// 		});
//
// 		availableIntervalsList = List.new;
// 		availableIntervals.do{arg item; availableIntervalsList.add(item.replace("/", "-").asString)};
// 		controls[1].items = availableIntervalsList.asArray;
//
// 		controls.add(PopUpMenu()
// 			.action_{|v|
// 				mantaRow = v.value;
// 		});
// 		controls[2].items = Array.series(6);
// 		controls[2].valueAction = rowNum;
//
// 		controls.add(Button()
// 			.states_([ [ "A", Color.red, Color.black ] ,[ "C", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					this.setManta;
// 					}, {
// 						this.clearManta;
// 				})
// 		});
//
// 		layout = HLayout(controls[0], controls[1], controls[2], controls[3]);
//
// 		functions = List.fill(8, {|i| {|val| sineWaves[i].set(\vol, val/200)}});
// 	}
//
// 	setFreq {
// 		4.do{arg i; sineWaves[i].set(\freq, midiNoteNum.midicps-(i*5))};
// 		(4..7).do{arg i; sineWaves[i].set(\freq, midiNoteNum.midicps*interval-(i*5))};
// 	}
//
// 	pause {
// 		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol, 0)}}
// 	}
//
// 	unpause {
// 		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};
//
// 	}
//
// 	setManta {
// 		var key;
//
// 		8.do{|i|
// 			key = "/manta/pad/"++(mantaRow*8+i).asString;
// 			oscMsgs.put(i, key);
// 			MidiOscControl.setControllerNoGui(group.server, oscMsgs[i], functions[i], setups);
// 		};
// 	}
//
// 	/*	setManta {
// 	var key;
//
// 	8.do{|i|
// 	key = "/manta/pad/"++(mantaRow*8+i).asString;
// 	oscMsgs.put(i, key);
// 	MidiOscControl.setControllerNoGui(group.server, oscMsgs[i], functions[i], setups);
// 	};
// 	}*/
//
// 	clearManta {
// 		8.do{|i|
// 			MidiOscControl.clearController(group.server, oscMsgs[i]);
// 			oscMsgs.put(i, nil);
// 		};
// 	}
//
// 	save {
// 		var saveArray, temp;
//
// 		saveArray = List.newClear(0);
//
// 		temp = List.newClear(0);
// 		controls.do{arg item;
// 			temp.add(item.value);
// 		};
// 		saveArray.add(temp);
// 		// saveArray.add(oscMsgs);
//
// 		^saveArray
// 	}
//
// 	load {arg loadArray;
//
// 		loadArray[0].do{arg controlLevel, i;
// 			if((controls[i].value!= controlLevel) && (dontLoadControls.includes(i).not),{
// 				controls[i].valueAction_(controlLevel);
// 			});
// 		};
// 	}
//
// 	killMe {
// 		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, item)};
// 		if(synths!=nil,{
// 			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
// 		});
// 	}
//
// }
//
// GlassSines_Mod : Module_Mod {
// 	var glassSineObjects, volBus;
//
// 	init {
// 		this.makeWindow("GlassSines", Rect(20, 400, 485, 100));
//
// 		this.initControlsAndSynths(1);
// 		this.makeMixerToSynthBus;
//
// 		volBus = Bus.control(group.server);
//
// 		glassSineObjects = List.new;
//
// 		4.do{arg i;
// 			glassSineObjects.add(GlassSineObject(group, outBus, setups));
// 			glassSineObjects[i].setWinPoint(i, volBus);
// 		};
//
// 		controls.add(QtEZSlider("vol", ControlSpec(0,2,'amp'),
// 			{|slider| volBus.set(slider.value)}, 1, true, \horz)
// 		);
// 		this.addAssignButton(0,\continuous);
//
// 		win.layout_(VLayout(
// 			glassSineObjects[3],
// 			glassSineObjects[2],
// 			glassSineObjects[1],
// 			glassSineObjects[0],
// 			HLayout(controls[0], assignButtons[0])
// 			)
// 		);
// 		win.layout.spacing = 0;
// 	}
//
// 	pause {
// 		glassSineObjects.do{|item| item.pause};
// 	}
//
// 	unpause {
// 		glassSineObjects.do{|item| item.unpause};
// 	}
//
// 	setStoredMIDI {
//
// 	}
//
// 	show {
// 		win.visible = true;
// 	}
//
// 	hide {
// 		win.visible = false;
// 	}
//
// 	getInternalBusses {
// 		^[];
// 	}
//
// 	addSetup {|setup|
// 		glassSineObjects.do{|item| item.addSetup(setup)}
// 	}
//
// 	removeSetup {|setup|
// 		glassSineObjects.do{|item| item.removeSetup(setup)}
// 	}
//
// 	killMe {
// 		glassSineObjects.do{arg item; item.killMe};
// 		win.close;
// 	}
//
// 	saveExtra {arg saveArray;
// 		var temp;
//
// 		temp = List.newClear(0); //controller settings
// 		glassSineObjects.do{arg item, i;
// 			temp.add(item.save);
// 		};
//
// 		saveArray.add(temp);  //controller messages
//
// 		^saveArray
// 	}
//
// 	loadExtra {arg loadArray;
//
// 		loadArray[4].do{arg item, i;
// 			glassSineObjects[i].load(item);
// 		};
// 	}
// }
