SinArray {

	var <>group, <>outBus, <>volBus, synths, sinFreqs, sinVols, pans;

	*new {arg group, outBus, volBus;
		^super.new.group_(group).outBus_(outBus).volBus_(volBus).init;
	}

	init {
		sinFreqs = List.newClear(5);
		sinVols = List.newClear(5);
		pans = List.newClear(5);
		synths = List.newClear(5);
		5.do{|i|
			synths.put(i, Synth("sinArray_mod", [\outBus, outBus, \volBus, volBus.index], group));
		};
		this.changeSins;
	}

	changeSins {
		5.do{|i|
			sinFreqs.put(i, rrand(100, 2000));
			sinVols.put(i, rrand(0.05, 0.2));
			pans.put(i, rrand(-1.0,1.0));
			synths[i].set(\freq, sinFreqs[i], \vol, sinVols[i], \pan, pans[i]);
		}
	}

	turnOff {
		5.do{|i|
			synths[i].set(\localGate, 0);
		}
	}

	turnOn {
		5.do{|i|
			synths[i].set(\freq, sinFreqs[i], \vol, sinVols[i], \pan, pans[i], \localGate, 1);
			synths[i].run(true);
		}
	}
}

SinArray_Mod : Module_Mod {
	var sinArray, currentOn, volBus;

	*initClass {
		StartUp.add {
			SynthDef("sinArray_mod", { |outBus, freq, vol, pan, volBus, gate = 1, localGate = 0, pauseGate = 1|
				var sin, inVol, env, pauseEnv, localEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				localEnv = EnvGen.kr(Env.asr(0,1,0), localGate, doneAction:1);

				inVol = In.kr(volBus);

				sin = SinOsc.ar(freq, 0, vol)*inVol;

				Out.ar(outBus, Pan2.ar(sin, pan)*env*pauseEnv*localEnv);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SinArray",Rect(946, 618, 60*6, 80));
		this.initControlsAndSynths(12);

		volBus = Bus.control(group.server);

		sinArray = List.newClear(0);
		5.do{|i| sinArray.add(SinArray(group, outBus, volBus))};

		currentOn = 0;

		controls.add(Button.new(win,Rect(0, 0, 60, 20))
			.states_([["allOff", Color.red, Color.black ]])
			.action_{|v|
					sinArray[currentOn].turnOff;
					controls[currentOn+1].value_(0);
			});
		this.addAssignButton( 0, \onOff, Rect(0, 20, 60, 20));

		5.do{arg i;
			controls.add(Button.new(win,Rect(60+(60*i), 0, 60, 20))
				.states_([["current"+i, Color.red, Color.black ], ["current"+i, Color.blue, Color.black ]])
				.action_{|v|
					this.setCurrentOn(i);
				});
			this.addAssignButton(i+1, \onOff, Rect(60+(60*i), 20, 60, 20), );
		};

		5.do{arg i;
			controls.add(Button.new(win,Rect(60+(60*i), 40, 60, 20))
				.states_([["current"+i, Color.red, Color.black ], ["current"+i, Color.red, Color.black ]])
				.action_{|v|
					sinArray[i].changeSins;
					//this.setCurrentOn(i);
				});
			this.addAssignButton(i+6, \onOff, Rect(60+(60*i), 60, 60, 20));		};

		controls.add(EZSlider(win, Rect(0, 80, 240, 20), "vol", ControlSpec(0,1,\amp),
			{arg val; volBus.set(val.value)}, 1, true));
		this.addAssignButton(10, \continuous, Rect(240, 80, 60, 20));

		//multichannel button
//		numChannels = 2;
//		controls.add(Button(win,Rect(10, 25, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//					},
//					1, {
//						numChannels = 4;
//					},
//					2, {
//						numChannels = 8;
//					}
//				)
//			};
//		);
	}

	setCurrentOn {arg value;
		if(currentOn!=value, {
			sinArray[currentOn].turnOff;
			controls[currentOn+1].value_(0);
		});
		controls[value+1].value_(1);
		sinArray[value].turnOn;
		currentOn = value;
	}

	killMe {
		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, setups, item)};
		win.close;
		volBus.free;
		group.set(\gate, 0);
	}

/*	loadSettings {arg xmlSynth;
		midiHidTemp = xmlSynth.getAttribute("controls11");
		if(midiHidTemp!=nil,{
			controls[11].valueAction_(midiHidTemp.interpret);
		});
	}*/
}