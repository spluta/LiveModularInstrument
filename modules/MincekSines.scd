MincekSineObject : Module_Mod {
	var sineWaves, freqList, freqNum;

	// *new {arg group, outBus, midiHidControl, manta, lemur, bcf2000, setups;
	// 	^super.new.group_(group).outBus_(outBus).midiHidControl_(midiHidControl).manta_(manta).lemur_(lemur).bcf2000_(bcf2000).setups_(setups).init;
	// }

	*initClass {
		StartUp.add {
			SynthDef("mincekSine", {arg freq, lagTime, outBus, vol=0, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);



				sine = SinOsc.ar(Lag.kr(freq, lagTime), 0, LagUD.kr(vol, LFNoise2.kr(0.1, 1.25, 1.5), LFNoise2.kr(0.1, 2.25, 3.5))*0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("ShifterFeedback",Rect(490, 510, 300, 250));

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(6);

		freqList = [[70, 63, 60, 53, 0], [54, 73, 65, 47, 130], [62, 69, 58, 51, 100], [55, 75, 65, 45, 50], [60, 62, 59, 58, 110]];

		freqNum = 0;

		sineWaves = List.new;

		4.do{arg i;
			sineWaves.add(Synth("mincekSine", [\freq, freqList[0][i].midicps, \lagTime, 1, \outBus, outBus], group));
		};

		4.do{arg i;
			controls.add(EZSlider(win, Rect(60*i+5, 5, 60, 200), "sine", ControlSpec(0, 1, 'amp'),
				{|v|
					sineWaves[i].set(\vol, v.value);
				}, 0, layout:\vert)
			);
			this.addAssignButton(i,\continuous, Rect(60*i+5, 225, 60, 20));
		};

		controls.add(Button(win, Rect(240, 5, 60, 20))
			.states_([ [ "prev", Color.green, Color.black ], [ "prev", Color.black, Color.green ] ])
			.action_({arg but;
				if(freqNum!=0,{
					freqNum = freqNum-1;
					4.do{|i| sineWaves[i].set(\freq, freqList[freqNum][i], \lagTime, 2)};
				});
			})
		);
		this.addAssignButton(4,\onOff, Rect(60*i+5, 225, 60, 20));

		controls.add(Button(win, Rect(240, 5, 60, 20))
			.states_([ [ "next", Color.green, Color.black ], [ "next", Color.black, Color.green ] ])
			.action_({arg but;
				if(freqNum!=4,{
					freqNum = freqNum+1;
					4.do{|i| sineWaves[i].set(\freq, freqList[freqNum][i], \lagTime, freqList[freqNum][4])};
				},{

				});
			})
		);
		this.addAssignButton(5,\onOff, Rect(60*i+5, 225, 60, 20));


		text = StaticText(win, Rect(240, 55, 40, 40);
		text.string = "0";
	}

	pause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol, 0)}}
	}

	unpause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};

	}


	killMe {
		sineWaves.do{arg item; item.set(\gate, 0)};
	}


}

