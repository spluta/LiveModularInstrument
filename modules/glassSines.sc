GSObject2 : GlassSineObject {
	var setValBox, boxFreqs, changeFreqs, layer2Sines, sinWavesSeqs, currentWaves, currentWavesWaiting, parent, analysisTexts, parent, onOffVals, mode;



	setWinPoint {arg parentIn, rowNum, volBus;

		mode = 0;
		parent = parentIn;

		blocksRow = rowNum;

		this.initControlsAndSynths(9);
		oscMsgs = List.newClear(16);

		sineWaves = List.fill(8, {List.fill(4, {Synth("glassSine2", [\freq, 440, \volBus, volBus, \outBus, outBus], group)})});

		sinWavesSeqs = List.fill(8, {Pseq((0..3), inf).asStream});
		currentWaves = List.fill(8, {3});
		currentWavesWaiting = List.fill(8, {3});
		onOffVals = List.fill(8, {0});

		boxFreqs = List.fill(8, {440});
		changeFreqs = List.fill(8, {440});
		analysisTexts = List.fill(8, {StaticText().maxHeight_(15).maxWidth_(30).font_(Font("Helvetica", 8)).string_("440")});

		8.do{arg i;
			controls.add(NumberBox()
				.action_{arg val;
					boxFreqs.put(i, val.value);
					sineWaves[i].do{|wave| wave.set(\freq, val.value)};
				}
				.maxHeight_(15).maxWidth_(30).font_(Font("Helvetica", 8))
				.valueAction_(440)
			);
		};

		controls.add(Button()
			.states_([ [ "A", Color.red, Color.black ] ,[ "C", Color.black, Color.red ] ])
			.action_{|v|
				if(v.value==1,{
					this.setBlocks;
				}, {
					this.clearBlocks;
				})
			}.maxHeight_(15).maxWidth_(60)
		);

		setValBox = TextField().maxHeight_(15).maxWidth_(30).font_(Font("Helvetica", 8))
		.action = {arg field;
			var temp;
			try {
				temp = field.value.split($ );
				temp = temp.collect{|item| item.asInteger};
				switch (temp.size)
				{1} {8.do{|i| controls[i].valueAction_(temp[0]+(i*5))}}
				{2} {8.do{|i| controls[i].valueAction_(temp[0]+(i*temp[1]))}}
				{3} {
					4.do{|i| controls[i].valueAction_(temp[0]+(i*temp[1]))};
					4.do{|i| controls[i+4].valueAction_(temp[2]+(i*5))};
				}
				{
					4.do{|i| controls[i].valueAction_(temp[0]+(i*temp[1]))};
					4.do{|i| controls[i+4].valueAction_(temp[2]+(i*temp[3]))};
				};
			}{
				setValBox.string = "440";
			};
		};

		layout = HLayout(setValBox,
			VLayout(controls[0], analysisTexts[0]),
			VLayout(controls[1], analysisTexts[1]),
			VLayout(controls[2], analysisTexts[2]),
			VLayout(controls[3], analysisTexts[3]),
			VLayout(controls[4], analysisTexts[4]),
			VLayout(controls[5], analysisTexts[5]),
			VLayout(controls[6], analysisTexts[6]),
			VLayout(controls[7], analysisTexts[7]),
			controls[8]
		);


	}

	switchMode {|val|
		mode = val;
		switch (val)
		{0} {
			controls.copyRange(0,7).do{|box, i| boxFreqs.put(i, box.value)};
			boxFreqs.do{|freq, i| sineWaves[i].do{|wave| wave.set(\freq, freq)}};
		};
		/*		{1} {if(mode==0){freqs.do{|freq, i| sineWaves[i].do{|wave| wave.set(\freq, freq)}}}}
		{2} {freqs.do{|freq, i| sineWaves[i].do{|wave| wave.set(\freq, freq)}}}*/

	}

	pause {
		sineWaves.do{|ocho| ocho.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol, 0)}}}
	}

	unpause {
		sineWaves.do{|ocho| ocho.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}}}
	}

	setBlocks {
		var key;

		8.do{|i|
			key = "/SeaboardPressure/"++((blocksRow*8)+i).asString;

			oscMsgs.put(i, key);
			MidiOscControl.setControllerNoGui(oscMsgs[i], {|val|
				sineWaves[i][currentWaves[i]].set(\vol, val)
			}, group.server);

			key = "/SeaboardNote/"++((blocksRow*8)+i).asString;

			oscMsgs.put(i, key);  //this seems messed up

			MidiOscControl.setControllerNoGui(oscMsgs[i], {|val|
				onOffVals.put(i, val);
				if(mode==1){parent.getFreqGrid(blocksRow, onOffVals.sum)};
				if(val==0){
					sineWaves[i].do{|wave| wave.set(\zvol, val)};
					currentWaves.put(i, sinWavesSeqs[i].next);
				}{
					sineWaves[i][currentWaves[i]].set(\zvol, val);
				};
				if(mode==0){
					sineWaves[i][currentWaves[i]].set(\freq, boxFreqs[i])
				}{sineWaves[i][currentWaves[i]].set(\freq, changeFreqs[i])}

			}, group.server);

		};
	}

	setLayerFreqs {|layerFreqs|
		changeFreqs = layerFreqs;
		changeFreqs.do{|item, i|
			{analysisTexts[i].string_(item.asString)}.defer;
			if(onOffVals[i]==0){
				currentWaves.put(i, sinWavesSeqs[i].next);
				sineWaves[i][currentWaves[i]].set(\freq, item)
			};
		};
	}


	clearBlocks {
		8.do{|i|
			MidiOscControl.clearController(group.server, oscMsgs[i]);
			oscMsgs.put(i, nil);
		};
	}

	killMe {
		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, item)};
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
	}

}



GlassSines_Mod : Module_Mod {
	var glassSineObjects, volBus, gridOnOff, analysisGroup, sinesGroup, analysisSynth, analysisBus, accumRout, accumPitches;

	*initClass {
		StartUp.add {
			SynthDef("analysisGS_mod", {
				var pitch, conf, onset, loudness, in, last;

				in = In.ar(\inBus.kr, 1);

				#pitch, conf = FluidPitch.kr(in);
				loudness = FluidLoudness.kr(in);
				pitch = Gate.kr(pitch, (conf>0.4)+(loudness>(-50)))[0];

				last = LastValue.kr(pitch.cpsmidi, 0.25);

				Out.kr(\analysisBus.kr, last);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GlassSines", Rect(20, 400, 485, 100));

		this.initControlsAndSynths(4);
		this.makeMixerToSynthBus;

		analysisGroup = Group.tail(group);
		sinesGroup = Group.tail(group);

		volBus = Bus.control(group.server);
		analysisBus = Bus.control(group.server);
		analysisBus.set(60);

		glassSineObjects = List.new;

		gridOnOff = List.fill(4, {0});

		synths.add(Synth("analysisGS_mod", [\inBus, mixerToSynthBus.index, \analysisBus, analysisBus], analysisGroup));

		accumPitches = List.fill(5, {440.cpsmidi});

		accumRout = Routine({inf.do{
			analysisBus.get({|val|
				if((val==nil)||(val<10)){val = rrand(50.0,72.0)};
				val = val.round(0.125);
				if(accumPitches[0]!=val){accumPitches.addFirst(val).pop};
			});
			0.1.wait;
		}}).play;


		4.do{arg i;
			glassSineObjects.add(GSObject2(sinesGroup, outBus));
			glassSineObjects[i].setWinPoint(this, i, volBus);
		};

		3.do{controls.add(Button.new())};

		RadioButtons(controls,
			[[["presets", Color.blue, Color.black ],["presets", Color.black, Color.red ]], [["analysis", Color.blue, Color.black ],["analysis", Color.black, Color.red ]], [["lock", Color.blue, Color.black ],["lock", Color.black, Color.red ]]],
			Array.fill(3, {|i| {glassSineObjects.do{|item| item.switchMode(i)}}}),
			0, true);

		this.addAssignButton(0, \onOff);
		this.addAssignButton(1, \onOff);
		this.addAssignButton(2, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,'amp'),
			{|slider| volBus.set(slider.value)}, 1, true, \horz)
		);
		this.addAssignButton(3,\continuous);

		win.layout_(VLayout(
			glassSineObjects[3],
			glassSineObjects[2],
			glassSineObjects[1],
			glassSineObjects[0],
			HLayout(controls[0], controls[1], controls[2]),
			HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),
			HLayout(controls[3], assignButtons[3])
		)
		);
		win.layout.spacing = 0;
	}

	straight8 {|val|
		^List.fill(8, {|i| val.midicps+(i*5)})
	}

	aboveRoot {|temp, root, octaves|

		temp = (root+((temp-root)%12));
		temp = temp.midicps*(octaves.choose);
		^List.fill(8, {|i| temp+(i*5)});
	}

	belowRoot {|temp, root, octaves|

		temp = (root+((temp-root)%12)-12);
		temp = temp.midicps/(octaves.choose);
		^List.fill(8, {|i| temp+(i*5)});
	}

	getFreqGrid {|rowNum, val|
		var temp, root, sum = gridOnOff.sum;
		if(sum==0){
			var pitchGrid;
			switch(rowNum)
			{3} {
				temp = accumPitches.copyRange(0,3).sort;
				pitchGrid = List.fill(4, {|i| this.straight8(temp[i])});
			}
			{2} {
				pitchGrid = List.fill(4, {|i|
					switch(i)
					{3} {
						this.aboveRoot(accumPitches[1], accumPitches[0], [1,2]);
					}
					{2} {
						this.straight8(accumPitches[0]);
					}
					{1} {
						this.belowRoot(max(accumPitches[2], accumPitches[3]), accumPitches[0], [1]);
					}
					{0} {
						this.belowRoot(min(accumPitches[2], accumPitches[3]), accumPitches[0], [1,2]);
					}
				});
			}
			{1} {
				temp = (accumPitches.copyRange(2,3)+([0,[12,0].choose].scramble)).sort;
				pitchGrid = List.fill(4, {|i|
					switch(i)
					{3} {
						this.straight8(temp[1]);
					}
					{2} {
						this.straight8(temp[0]);
					}
					{1} {
						this.straight8(accumPitches[0]);
					}
					{0} {
						this.belowRoot(accumPitches[1], accumPitches[0], [1,2]);
					}
				});
			}
			{0} {
				root = (accumPitches[0]-24)%12+24;
				temp = ([5,7,9,11,13,15]*(root.midicps)).cpsmidi.scramble;
				pitchGrid = List.fill(4, {|i|
					switch(i)
					{3} {
						this.aboveRoot(temp[2], root, [2,3]);
					}
					{2} {
						this.aboveRoot(temp[1], root, [1,2]);
					}
					{1} {
						this.aboveRoot(temp[0], root, [1,2]);
					}
					{0} {
						this.straight8(root);
					}
				});
			};
			pitchGrid = pitchGrid.collect{|grid|
				grid.clump(2).flop.collect{|item,i| if(i==0){item.reverse}{item}}.flatten
			};
			glassSineObjects.do{|gs, i| gs.setLayerFreqs(pitchGrid[i])};
		};
		gridOnOff.put(rowNum, val);
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

	killMeSpecial {
		glassSineObjects.do{arg item; item.killMe};
		win.close;
	}

	saveExtra {arg saveArray;
		var temp;


		temp = List.newClear(0); //controller settings
		glassSineObjects.do{arg item, i;
			temp.add(item.save);
		};

		saveArray.add(temp);  //controller messages

		^saveArray
	}

	loadExtra {arg loadArray;


		loadArray.do{arg item, i;

			glassSineObjects[i].load(item);
		};
	}
}

GlassSineObject : Module_Mod {
	var win, sineWaves, sineWavesB, midiNoteNum, volFunctions, interval, availableIntervals, win, availableIntervalsList, blocksRow, <>layout;

	*initClass {
		StartUp.add {
			SynthDef("glassSine2", {arg freq, volBus, outBus, vol=0, zvol=1, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv, mainVol;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(freq+LFNoise2.kr(0.1, 5), 0, LagUD.kr(min(vol, zvol), LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);

				mainVol = Lag.kr(In.kr(volBus), 0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv*mainVol, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {}

	setWinPoint {arg rowNum, volBus;
		this.initControlsAndSynths(4);
		oscMsgs = List.newClear(16);

		midiNoteNum = 70;

		availableIntervals = ["11/10","11/9","9/7","13/7"];
		interval = availableIntervals[0].interpret;

		sineWaves = List.new;
		4.do{arg i;
			sineWaves.add(Synth("glassSine2", [\freq, midiNoteNum.midicps-(i*5), \volBus, volBus, \outBus, outBus], group));
		};
		4.do{arg i;
			sineWaves.add(Synth("glassSine2", [\freq, (midiNoteNum.midicps*interval)-((i+4)*5), \volBus, volBus, \outBus, outBus], group));
		};

		controls.add(QtEZSlider(
			"Cutoff", // label
			ControlSpec(20, 124, \linear, 0.5), // control spec
			{|ez|
				midiNoteNum=ez.value;
				this.setFreq;
			},
			midiNoteNum,
			true, \horz
		));

		controls.add(PopUpMenu()
			.action_{|v|
				interval = availableIntervals[v.value].interpret;
				this.setFreq;
		});

		availableIntervalsList = List.new;
		availableIntervals.do{arg item; availableIntervalsList.add(item.replace("/", "-").asString)};
		controls[1].items = availableIntervalsList.asArray;

		controls.add(PopUpMenu()
			.action_{|v|
				blocksRow = v.value;
		});
		controls[2].items = Array.series(6);
		controls[2].valueAction = rowNum;

		controls.add(Button()
			.states_([ [ "A", Color.red, Color.black ] ,[ "C", Color.black, Color.red ] ])
			.action_{|v|
				if(v.value==1,{
					this.setBlocks;
				}, {
					this.clearBlocks;
				})
		});

		layout = HLayout(controls[0], controls[1], controls[2], controls[3]);

	}

	asView {^layout}

	setFreq {
		4.do{arg i; sineWaves[i].set(\freq, midiNoteNum.midicps-(i*5))};
		(4..7).do{arg i; sineWaves[i].set(\freq, midiNoteNum.midicps*interval-(i*5))};
	}

	pause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol, 0)}}
	}

	unpause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};

	}



	setBlocks {
		var key;

		8.do{|i|
			key = "/SeaboardPressure/"++((blocksRow*8)+i).asString;

			oscMsgs.put(i, key);
			MidiOscControl.setControllerNoGui(oscMsgs[i], {|val|
				sineWaves[i].set(\vol, val)}, group.server);

			key = "/SeaboardNote/"++((blocksRow*8)+i).asString;
			oscMsgs.put(i, key);
			MidiOscControl.setControllerNoGui(oscMsgs[i], {|val|
				sineWaves[i].set(\zvol, val)}, group.server);
		};
	}


	clearBlocks {
		8.do{|i|
			MidiOscControl.clearController(group.server, oscMsgs[i]);
			oscMsgs.put(i, nil);
		};
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		controls.do{arg item;
			saveArray.add(item.value);
		};

		^saveArray
	}

	load {arg loadArray;

		loadArray.do{arg controlLevel, i;
			try {
				controls[i].valueAction_(controlLevel);
			} {"nope".postln};
		};
	}

	killMe {
		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, item)};
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
	}

}
