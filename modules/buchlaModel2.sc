//with the roli

BuchlaModelSolo_Mod :  Module_Mod {
	var filterList, rqMax, freqs, filtNums, outGroup, specs, synthGroup, filterGroup, transferBus, buchlaFilters, straightPitch, straightPitchBool, triggerButtons, continuousButtons, ampSpec, fmAmpSpec, padFunctions, noteOnFunctions, noise1Select, noise2Select, ampModNoiseSelect, trigVal, baseVol, pitchBus;

	var arpRout, arpRoutA, arpArray, arpCounter = 0, arpNumber = 12, arpWait = 1, arpWaitSpec, deviation = 0, arpFreq = 400, arpeggioSwitch = false;

	*initClass {
		StartUp.add {
			SynthDef("buchlaFMPatchSolo_mod", {arg inBus, outBus, pitchBus, amp0 = 0, amp1=0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0, amp6 = 0, amp7 = 0, freq0 = 300, freq1=200, arpFreq = 400, ampModFreq=0, volume = 0, onOff = 1, noise1Select=0, noise2Select = 0, ampModNoiseSelect=0, t_trig, lagTime = 0.1, zamp1=0, zamp2=0, zamp3=0, zamp4=0, zamp5=0, zamp6=0, zamp7=0, gate = 1, pauseGate = 1;
				var layer2, layer1, layer0, ampMod, seq, env, pauseEnv, layer1Noise, in, freq, hasFreq;

				in = In.ar(inBus);



				layer2 = WhiteNoise.ar(Lag.kr(amp5*zamp5))+PinkNoise.ar(Lag.kr(amp6*zamp6))+BrownNoise.ar(Lag.kr(amp7*zamp7));

				layer2 = Select.ar(noise2Select, [layer2, Latch.ar(layer2, Dust.ar(TRand.ar(200, 1000, noise2Select)))]);

				layer1Noise = BrownNoise.ar(Lag.kr(amp4*zamp4));
				layer1Noise = Select.ar(noise1Select, [layer1Noise, Latch.ar(layer1Noise, Dust.ar(TRand.ar(200, 1000, noise1Select)))]);


				freq1 = Lag.kr(freq1, 0.05);
				layer1 = SinOsc.ar(freq1+layer2,0,Lag.kr(amp2*zamp2))+LFSaw.ar(freq1+layer2,0,Lag.kr(amp3*zamp3))+layer1Noise;

				//ampMod = SinOsc.ar(ampModFreq+(Select.kr(ampModNoiseSelect, [0, LFNoise2.kr(TRand.kr(0.5, 2, ampModNoiseSelect)).range(0, 30)])), 0, ampModVol/2, ampModVol/2);

				layer0 = SinOsc.ar(Lag.kr(freq0,0.05)+layer1, 0, Lag.kr(volume*onOff));
				//-(ampMod*Lag.kr((ampModFreq>1), 1/ampModFreq)));

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				Out.ar(outBus, [layer0,layer0]*env*pauseEnv)
			}).writeDefFile
		}
	}

	init {
		this.makeWindow("BuchlaModelSolo",Rect(200, 230, 140, 280));

		pitchBus = Bus.control(group.server);

		this.initControlsAndSynths(48);
		this.makeMixerToSynthBus;

		baseVol = 0;
		controls.add(QtEZSlider.new("baseVol", ControlSpec(0,1,'linear'),
			{|v|
				baseVol = v.value;
				synths[0].set(\volume, ampSpec.map(baseVol));
		}, 0));

		straightPitch = 60;
		straightPitchBool = false;


		controls.add(QtEZSlider.new("trigVal", ControlSpec(1,12,'lin',1),
			{|v|
				trigVal = v.value;
		}, 6));

		controls.add(Button.new()
			.states_([ [ "A-Seaboard", Color.red, Color.black ] ,[ "C-Seaboard", Color.black, Color.red ] ])
			.action_{|v|
				if(v.value==1,{
					this.setSeaboard;
				},{
					this.clearMidiOsc;
				})
		});


		win.layout_(
			VLayout(
				HLayout(controls[0].layout, controls[1].layout),
				HLayout(controls[2])
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

		specs = List.new;
		specs.add(ControlSpec(200, 1000, 'exponential'));
		specs.add(ControlSpec(500, 3000, 'exponential'));
		specs.add(ControlSpec(500, 3000, 'exponential'));
		specs.add(ControlSpec(500, 3000, 'exponential'));
		specs.add(ControlSpec(200, 1000, 'exponential'));
		specs.add(ControlSpec(500, 3000, 'exponential'));
		specs.add(ControlSpec(0.0001, 100, 'exponential'));

		synthGroup = Group.tail(group);
		filterGroup = Group.tail(group);
		transferBus = Bus.audio(group.server, 2);

		synths = List.new;
		synths.add(Synth("buchlaFMPatchSolo_mod", [\inBus, mixerToSynthBus, \outBus, transferBus.index, \pitchBus, pitchBus], synthGroup));

		buchlaFilters = List.newClear(4);
		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));
		//[2, 6, 7, 5, 9, 10, 12, 14]
		triggerButtons = [3, 2, 6, 7,  1, 5, 9, 10,  12, 13, 14, 15]; //[ 2, 10, 19, 8, 17, 25, 40, 0, 1, 27, 34, 12 ];
		//[3, 2, 6, 7, 5, 9, 10, 4, 8, 14]
		continuousButtons = [3,  2, 6, 7,  1, 5, 9, 10,  0,4,8,  13,14,15]; //[ 3, 2, 10, 19, 0, 1, 9, 18, 26, 8, 17, 25, 16, 24, 33, 11, 3 ];

		ampSpec = ControlSpec(0,1,'linear');
		fmAmpSpec = ControlSpec(0,3000,'amp');

		this.addFunctions;
	}

	pause {
		buchlaFilters.do{arg item; if(item!=nil,{item.pause})};
		synths.do{|item| item.set(\pauseGate, 0)};
	}

	unpause {
		buchlaFilters.do{arg item; if(item!=nil,{item.unpause})};
		synths.do{|item| item.set(\pauseGate, 1); item.run(true)};
	}

	setSeaboard {
		var counter=0;

		padFunctions.keys.do{arg key;
			oscMsgs.put(counter, "/SeaboardPressure/"++key.asString);
			//oscMsgs.postln;
			MidiOscControl.setControllerNoGui(oscMsgs[counter], padFunctions[key], group.server);
			counter=counter+1;
		};
		noteOnFunctions.keys.do{arg key;
			oscMsgs.put(counter, "/SeaboardNote/"++key.asString);
			MidiOscControl.setControllerNoGui(oscMsgs[counter], noteOnFunctions[key], group.server);
			counter=counter+1;
		};

	}

	addFunctions {
		var base;

		noteOnFunctions = IdentityDictionary.new;

		//the notes

		base = Seaboard.lowNote+Seaboard.keyboardWidth-1;

		noteOnFunctions.put(3, {arg val;
			synths[0].set(\onOff, val);
		});

		noteOnFunctions.put(2, {arg val;
			if(val==1,{specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'))});
		});
		noteOnFunctions.put(10, {arg val;
			if(val==1,{specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
		});
		noteOnFunctions.put(11, {arg val;
			if(val==1,{specs.put(2, ControlSpec(rrand(600,950), rrand(5000, 15000), 'exponential'))});
		});


		noteOnFunctions.put(1, {arg val;
			if(val==1,{specs.put(3, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'))});
			synths[0].set(\zamp2, val);
		});
		noteOnFunctions.put(9, {arg val;
			if(val==1,{specs.put(4, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
			synths[0].set(\zamp3, val);
		});
		noteOnFunctions.put(17, {arg val;
			if(val==1,{specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))});
			"zamp4".post;val.postln;
			synths[0].set(\zamp4, val);
		});

		noteOnFunctions.put(25, {arg val;
			synths[0].set(\noise2Select, 2.rand);
			synths[0].set(\zamp5, val);
			"zamp5".post;val.postln;
		});

		noteOnFunctions.put(24, {arg val; buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}});

		noise1Select = Pseq([0,1],inf).asStream;
		noise2Select = Pseq([0,1],inf).asStream;
		ampModNoiseSelect = Pseq([0,1],inf).asStream;

		padFunctions = IdentityDictionary.new;

		padFunctions.put(3, {arg val; val = val+baseVol; synths[0].set(\volume, ampSpec.map(max(val-0.1,0)))});

		padFunctions.put(2, {arg val;
				synths[0].set(\freq0, specs[0].map(val))});
		padFunctions.put(10, {arg val;  synths[0].set(\freq0, specs[1].map(val))});
		padFunctions.put(11, {arg val;  synths[0].set(\freq0, specs[2].map(val))});


		padFunctions.put(1, {arg val;  synths[0].set(\amp2, fmAmpSpec.map(val))});
		padFunctions.put(9, {arg val;  synths[0].set(\amp3, fmAmpSpec.map(val))});

		padFunctions.put(0, {arg val;  synths[0].set(\freq1, specs[3].map(val))});

		padFunctions.put(8, {arg val;  synths[0].set(\freq1, specs[4].map(val))});

		padFunctions.put(17, {arg val;
			"amp4".post;val.postln;
			synths[0].set(\amp4, fmAmpSpec.map(val))});

		padFunctions.put(25, {arg val;
			"amp5".post;val.postln;
			synths[0].set(\amp5, fmAmpSpec.map(val))});


	}

	trigger {
		synths[0].set(\t_trig, 1);
	}

	killMeSpecial {
		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
		arpRout.stop;
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);  //controller messages
		//this does not save or load the oscMsgs

		saveArray.add(win.bounds);

		this.saveExtra(saveArray);
		^saveArray
	}

	load {arg loadArray;
		loadArray[1].do{arg controlLevel, i;
			if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
		};
		win.bounds_(loadArray[3]);
		this.loadExtra(loadArray);
	}

}


/*base = Seaboard.lowNote+Seaboard.keyboardWidth

		noteOnFunctions.put(3, {arg val;
			//if(val==0,{synths[0].set(\volume, 0)});
		});

		noteOnFunctions.put(2, {arg val;
			if(val==1,{specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'))});
		});
		noteOnFunctions.put(6, {arg val;
			if(val==1,{specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
		});
		noteOnFunctions.put(7, {arg val;
			if(val==1,{specs.put(2, ControlSpec(rrand(600,950), rrand(5000, 15000), 'exponential'))});
		});


		noteOnFunctions.put(1, {arg val;
			if(val==1,{specs.put(3, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'))});
			synths[0].set(\zamp2, val);
		});
		noteOnFunctions.put(5, {arg val;
			if(val==1,{specs.put(4, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
			synths[0].set(\zamp3, val);
		});
		noteOnFunctions.put(14, {arg val;
			if(val==1,{specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))});
			synths[0].set(\zamp4, val);
		});
		/*noteOnFunctions.put(triggerButtons[7], {arg val; specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))});*/

		noteOnFunctions.put(12, {arg val; buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}});

		/*		noteOnFunctions.put(triggerButtons[7], {arg val; arpeggioSwitch = true; arpNumber = [3,5,7,11].choose});
		noteOnFunctions.put(triggerButtons[8], {arg val; arpeggioSwitch = true; arpWaitSpec = ControlSpec(rrand(0.05, 0.1), rrand(0.2, 0.4), 'exponential');});*/

		noise1Select = Pseq([0,1],inf).asStream;
		noise2Select = Pseq([0,1],inf).asStream;
		ampModNoiseSelect = Pseq([0,1],inf).asStream;
/*		noteOnFunctions.put(13, {arg val;
			if(val==1,{synths[0].set(\noise1Select, noise1Select.next)});
			synths[0].set(\zamp5, 1);
		});
		noteOnFunctions.put(14, {arg val;
			if(val==1,{synths[0].set(\noise2Select, noise2Select.next)});
			synths[0].set(\zamp6, 1);
		});
		noteOnFunctions.put(15, {arg val;
			if(val==1,{synths[0].set(\noise2Select, noise2Select.next)});
			synths[0].set(\zamp7, 1);
		});*/
		/*noteOnFunctions.put(triggerButtons[11], {arg val; synths[0].set(\ampModNoiseSelect, ampModNoiseSelect.next)});*/

		padFunctions = IdentityDictionary.new;

		//continuousButtons = [3,  2, 6, 7,  1, 5, 9, 10,  0,4,8,  13,14,15]

		padFunctions.put(3, {arg val; val.postln; val = val+baseVol; synths[0].set(\volume, ampSpec.map(max(val-0.1,0)))});

		padFunctions.put(2, {arg val;
				synths[0].set(\freq0, specs[0].map(val))});
		padFunctions.put(6, {arg val;  synths[0].set(\freq0, specs[1].map(val))});
		padFunctions.put(7, {arg val;  synths[0].set(\freq0, specs[2].map(val))});


		padFunctions.put(1, {arg val;  synths[0].set(\amp2, fmAmpSpec.map(max(val-0.05,0)))});
		padFunctions.put(5, {arg val;  synths[0].set(\amp3, fmAmpSpec.map(max(val-0.05,0)))});

		padFunctions.put(0, {arg val;  synths[0].set(\freq1, specs[3].map(max(val-0.05,0)))});

		padFunctions.put(4, {arg val;  synths[0].set(\freq1, specs[4].map(max(val-0.05,0)))});
		/*padFunctions.put(continuousButtons[10], {arg val;  synths[0].set(\freq1, specs[5].map(max(val-0.05,0)))});*/

		padFunctions.put(14, {arg val;  synths[0].set(\amp4, fmAmpSpec.map(max(val-0.05,0)))});
/*		padFunctions.put(continuousButtons[12], {arg val;  synths[0].set(\amp6, fmAmpSpec.map(max(val-0.05,0)))});
		padFunctions.put(continuousButtons[13], {arg val;  synths[0].set(\amp7, fmAmpSpec.map(max(val-0.05,0)))});*/

		/*padFunctions.put(continuousButtons[14], {arg val;  synths[0].set(\ampModFreq, specs[6].map(max(val-0.05,0)))});*/
*/