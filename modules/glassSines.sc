GlassSineObject : Module_Mod {
	var win, sineWaves, midiNoteNum, func, interval, availableIntervals, win, slider, intervalDisp, assignButtons, mantaData, ccResponder, availableIntervalsList, waitForSet, waitForType;

	*initClass {
		StartUp.add {
			SynthDef("glassSine2", {arg freq, outBus, vol=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;

			SynthDef("glassSine4", {arg freq, outBus, vol=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);

				Out.ar(outBus, PanAz.ar(4, sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;

			SynthDef("glassSine8", {arg freq, outBus, vol=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);

				Out.ar(outBus, PanAz.ar(8, sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {}

	setWinPoint {arg winIn, point;

		if(win.isNil, {win = winIn});

		midiNoteNum = 90;

		mantaData = List.newClear(8);

		availableIntervals = ["11/10","11/9","9/7","13/7"];
		interval = availableIntervals[0].interpret;

		sineWaves = List.new;

		2.do{sineWaves.add(List.new)};
		4.do{arg i;
			sineWaves[0].add(Synth("glassSine2", [\freq, midiNoteNum.midicps-(i*5), \outBus, outBus], group));
			sineWaves[1].add(Synth("glassSine2", [\freq, (midiNoteNum.midicps*interval)-(i*5), \outBus, outBus], group));
		};

		slider = EZSlider(win, // window
			Rect(point.x, point.y, 300, 20), // dimensions
			"Cutoff", // label
			ControlSpec(20, 100, \linear, 0.5), // control spec
			{|ez|
				midiNoteNum=ez.value;
				sineWaves[0].do{arg item, i; item.set(\freq, midiNoteNum.midicps-(i*5))};
				sineWaves[1].do{arg item, i; item.set(\freq, (midiNoteNum.midicps*interval)-(i*5))};
			},// action
			midiNoteNum // initVal
		);

		intervalDisp = PopUpMenu.new(win,Rect(point.x+305, point.y, 60, 16))
			.action_{|v|
				interval = availableIntervals[v.value].interpret;
				sineWaves[0].do{arg item, i; item.set(\freq, midiNoteNum.midicps-(i*5))};
				sineWaves[1].do{arg item, i; item.set(\freq, (midiNoteNum.midicps*interval)-(i*5))};
			};
		availableIntervalsList = List.new;
		availableIntervals.do{arg item; availableIntervalsList.add(item.replace("/", "-").asString)};
		intervalDisp.items = availableIntervalsList.asArray;
		this.addAssignButton(0,\continuous, Rect(point.x+370, point.y, 60, 20));
	}

	pause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol, 0)}}
	}

	unpause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};

	}

	//all this control stuff below needs to be restructured
/*
	setMidi {arg data;
	}

	addSetup {arg setup;
		setups.add(setup);
		mantaData.do{arg item, i;
			if(item.isNil.not,{
				this.setMantaForSetup(setup, item[0], {arg val; sineWaves[(i/4).floor][i].set(\vol, val/200)});
			})
		};
	}

	setMantaForSetup {arg setup, item, function;
		Manta.addPadSetup(setup.asSymbol, item, function);
	}

	setManta {arg buttonNum, buttonType, controlsIndex;
		if(manta!=nil,{
			4.do{arg i;
				mantaData.put(i, [buttonNum.deepCopy+i, buttonType.deepCopy]);
				setups.do{arg setup;
					this.setMantaForSetup(setup, mantaData[i][0], {arg val; sineWaves[0][i].set(\vol, val/200)})
				}
			};
			4.do{arg i;
				mantaData.put(i+4, [buttonNum.deepCopy+i+4, buttonType.deepCopy]);
				setups.do{arg setup;
					this.setMantaForSetup(setup, mantaData[i+4][0], {arg val; sineWaves[1][i].set(\vol, val/200)})
				}
			};
		})
	}

	clearMidiHid {
		if((manta!=nil)and:(mantaData!=nil),{
			setups.do{|setup| 8.do{arg i;
				if(mantaData[i]!=nil,{
					Manta.removePadSetup(setup.asSymbol, mantaData[i][0])
				})
			}}
		});
	}

	setDefaultMidi {

	}

	killMe {
		sineWaves[0].do{arg item; item.set(\gate, 0)};
		sineWaves[1].do{arg item; item.set(\gate, 0)};
	}

	save {arg xmlSynth, i;
		xmlSynth.setAttribute("manta"++i.asString, mantaData[0].asString);
		xmlSynth.setAttribute("midiNoteNum"++i.asString, midiNoteNum.asString);
		xmlSynth.setAttribute("interval"++i.asString, intervalDisp.value.asString);
	}

	load {arg xmlSynth, i;
		midiHidTemp = xmlSynth.getAttribute("manta"++i.asString).interpret;
		if(midiHidTemp!=nil,{
			this.setManta(xmlSynth.getAttribute("manta"++i.asString).interpret[0], 1);
		});
		slider.valueAction_(xmlSynth.getAttribute("midiNoteNum"++i.asString).interpret);
		intervalDisp.valueAction_(xmlSynth.getAttribute("interval"++i.asString).interpret);
	}*/
}

GlassSines_Mod {
	var <>group, <>outBus, <>midiHidControl, <>manta, <>lemur, <>bcf2000, <>setups, glassSineObjects, <>win, xmlSynth;

	*new {arg group, outBus, midiHidControl, manta, lemur, bcf2000, setups;
		^super.new.group_(group).outBus_(outBus).midiHidControl_(midiHidControl).manta_(manta).lemur_(lemur).bcf2000_(bcf2000).setups_(setups).init;
	}

	makeWindow {arg name, rect;
		win = Window.new(name, rect);
		win.userCanClose_(false);
		win.front;
		win.alwaysOnTop_(true);
	}

	init {
		this.makeWindow("GlassSines", Rect(20, 400, 415, 100));

		glassSineObjects = List.new;

		4.do{arg i;
			glassSineObjects.add(GlassSineObject(group, outBus, midiHidControl, manta, lemur, bcf2000, setups));
			glassSineObjects[i].setWinPoint(win, Point(0, i*25));
		};
	}

	pause {
		glassSineObjects.do{|item| item.pause};
	}

	unpause {
		glassSineObjects.do{|item| item.unpause};
	}

	setStoredMIDI {

	}

	show {
		win.visible = true;
	}

	hide {
		win.visible = false;
	}

	getInternalBusses {
		^[];
	}

	addSetup {|setup|
		glassSineObjects.do{|item| item.addSetup(setup)}
	}

	removeSetup {|setup|
		glassSineObjects.do{|item| item.removeSetup(setup)}
	}

	killMe {
		glassSineObjects.do{arg item; item.clearMidiHid; item.killMe};
		win.close;
	}

	save {arg xmlDoc;
		xmlSynth = xmlDoc.createElement("GlassSines");
		glassSineObjects.do{arg item, i;
			item.save(xmlSynth, i);
		};
		xmlSynth.setAttribute("bounds", win.bounds.asString);
		^xmlSynth;
	}

	load {arg xmlSynth;
		glassSineObjects.do{arg item, i;
			item.load(xmlSynth, i);
		};
		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
	}
}
