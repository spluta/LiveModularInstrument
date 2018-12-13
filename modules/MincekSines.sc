MincekSine_Mod : Module_Mod {
	var sineWaves, freqList, lagList, volList, freqNum, text;

	// *new {arg group, outBus, midiHidControl, manta, lemur, bcf2000, setups;
	// 	^super.new.group_(group).outBus_(outBus).midiHidControl_(midiHidControl).manta_(manta).lemur_(lemur).bcf2000_(bcf2000).setups_(setups).init;
	// }

	*initClass {
		StartUp.add {
			SynthDef("mincekSine_mod", {arg freq, lagTime, outBus, vol=0, lilVol = 1, gate = 1, pauseGate = 1;
				var sine, env, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				sine = SinOsc.ar(VarLag.kr(freq, lagTime), 0, Lag.kr(vol, 0.01)*Lag.kr(lilVol, 0.1)*0.1);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("MincekSine",Rect(490, 510, 300, 250));

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(23);

		dontLoadControls = (1..22);

		/*freqList = [[71,69,60,60], [75.7,64,52.3,60], [87.3,50.7,60,60], [76.3,61.7,60,60], [83,52,60,60], [93.3,45,60,60], [70.7,69.3,60,60], [86,75,49,38], [79,68,54,43], [84,71,49,36], [62,61,59,58]];

		lagList = [0, 0, 0, 60, 45, 0, 55, 0, 120, 120, 240];

		volList = [[1,1,0,0], [1,1,1,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,1,1], [1,1,1,1], [1,1,1,1], [1,1,1,1]];*/

		freqList = [[71,69,60,60], [70,64,52.3,60], [87.3,50.7,60,60],
			[75.7,64,52.3,60], [80,57,60,60], [70.7,69.3,60,60],
			[86,75,49,38], [79,68,54,43], [84,71,49,36],
			[62,61,59,58], [62,61,59,58]];

		lagList = [0, 0, 0,
			0, 0, 64,
			0, 27, 36,
			82, 240];

		volList = [[1,0,0,0], [1,0,0,0], [1,1,0,0],
			[1,1,1,0], [1,1,0,0], [1,1,0,0],
			[1,1,1,1], [1,1,1,1], [1,1,1,1],
			[1,1,1,1], [1,1,1,1]];

		freqNum = 0;

		sineWaves = List.new;

		4.do{arg i;
			sineWaves.add(Synth("mincekSine_mod", [\freq, freqList[0][i].midicps, \lagTime, 1, \outBus, outBus], group));
		};


		controls.add(QtEZSlider.new("volume", ControlSpec(0, 1, 'amp'),
			{|v|
				sineWaves.do{arg item; item.set(\vol, v.value)};
			}, 0, true, \horz)
		);
		this.addAssignButton(0,\continuous);


		freqList.size.do{arg i;

			controls.add(Button()
				.states_([ [ "chord"+(i+1).asString, Color.green, Color.black ], [ "chord"+(i+1).asString, Color.black, Color.green ] ])
				.action_({arg but;

					4.do{|i2| sineWaves[i2].set(\freq, freqList[i][i2].midicps, \lagTime, lagList[i], \lilVol, volList[i][i2])};
					//text.string = freqNum.asString;
				})
			);
			this.addAssignButton(i+1,\onOff);
		};

		freqList.size.do{arg i;
			controls.add(Button()
				.states_([ [ "chord"+i.asString, Color.green, Color.black ], [ "chord"+i.asString, Color.black, Color.green ] ])
				.action_({arg but;

					4.do{|i2| sineWaves[i2].set(\freq, freqList[i][i2].midicps, \lagTime, 1, \lilVol, volList[i][i2])};
					//text.string = freqNum.asString;
				})
			);
			this.addAssignButton(i+12,\onOff);
		};

		win.layout_(VLayout(
			HLayout(controls[0].layout,assignButtons[0].layout),
		HLayout(controls[1],controls[2],controls[3],controls[4],controls[5],controls[6],controls[7],controls[8],controls[9],controls[10],controls[11]),

HLayout(assignButtons[1].layout,assignButtons[2].layout,assignButtons[3].layout,assignButtons[4].layout,assignButtons[5].layout,assignButtons[6].layout,assignButtons[7].layout,assignButtons[8].layout,assignButtons[9].layout,assignButtons[10].layout,assignButtons[11].layout),
			HLayout(controls[12],controls[13],controls[14],controls[15],controls[16],controls[17],controls[18],controls[19],controls[20],controls[21],controls[22]),
	HLayout(assignButtons[12].layout,assignButtons[13].layout,assignButtons[14].layout,assignButtons[15].layout,assignButtons[16].layout,assignButtons[17].layout,assignButtons[18].layout,assignButtons[19].layout,assignButtons[20].layout,assignButtons[21].layout,assignButtons[22].layout)
		));
	}

	pause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol)}}
	}

	unpause {
		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};

	}

	killMeSpecial {
		sineWaves.do{arg item; item.set(\gate, 0)};
	}


}

