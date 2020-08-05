SimpleSound_Mod : Module_Mod {
	var volBus, freqBus, attackBus, releaseBus, name;

	init {
		name = this.class.asString.copyRange(0, this.class.asString.size-5);
		this.makeWindow(name,Rect(490, 510, 300, 250));

		this.initControlsAndSynths(5);
		synths.add(nil);

		volBus = Bus.control(group.server);
		freqBus = Bus.control(group.server);
		attackBus = Bus.control(group.server);
		releaseBus = Bus.control(group.server);

		volBus.set(0);
		freqBus.set(100);
		attackBus.set(1);
		releaseBus.set(1);

		controls.add(Button()
			.states_([["go", Color.blue, Color.black ],["stop", Color.black, Color.red ]])
			.action_{|v|
				if(v.value==1,{
					this.makeSynth;
				},{
					synths[0].set(\gate, 0);
				})
		});
		this.addAssignButton(0,\onOff);

		controls.add(QtEZSlider("freq", ControlSpec(20, 20000, 'exp'),
			{|v|
				freqBus.set(v.value);
		}, 100, true, \horz));
		this.addAssignButton(1,\continuous);
		controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, true, \horz));
		this.addAssignButton(2,\continuous);
		controls.add(QtEZSlider("attack", ControlSpec(0.001, 3, 'exp'),
			{|v|
				attackBus.set(v.value);
		}, 1, true, \horz));
		this.addAssignButton(3,\continuous);
		controls.add(QtEZSlider("release", ControlSpec(0.001, 3, 'exp'),
			{|v|
				releaseBus.set(v.value);
		}, 1, true, \horz));
		this.addAssignButton(4,\continuous);

		win.layout_(VLayout(
			HLayout(controls[0], assignButtons[0]),
			HLayout(controls[1], assignButtons[1]),
			HLayout(controls[2], assignButtons[2]),
			HLayout(controls[3], assignButtons[3]),
			HLayout(controls[4], assignButtons[4])
		))
	}

}

SinOsc_Mod : SimpleSound_Mod {

	*initClass {
		StartUp.add {

			SynthDef("sineWave_mod", {arg outBus, freqBus, volBus, gate=1, pauseGate=1, attackBus, releaseBus;
				var env, pauseEnv, sound, vol, freq, attack, release;

				vol = In.kr(volBus);
				freq = In.kr(freqBus);
				attack = In.kr(attackBus);
				release = In.kr(releaseBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(attack, 1, release), gate, doneAction:2);
				sound = SinOsc.ar(freq, 0, vol);
				Out.ar(outBus, sound.dup*env*pauseEnv);
			}).writeDefFile;

		}
	}

	makeSynth {
		synths.put(0, Synth("sineWave_mod", [\outBus, outBus, \freqBus, freqBus, \volBus, volBus, \attackBus, attackBus, \releaseBus, releaseBus], group))
	}
}


PinkNoise_Mod : SimpleSound_Mod {

	*initClass {
		StartUp.add {

			SynthDef("pinkNoise_mod", {arg outBus, freqBus, volBus, gate=1, pauseGate=1, attackBus, releaseBus;
				var env, pauseEnv, sound, vol, freq, attack, release;

				vol = In.kr(volBus);
				freq = In.kr(freqBus);
				attack = In.kr(attackBus);
				release = In.kr(releaseBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(attack, 1, release), gate, doneAction:2);
				sound = PinkNoise.ar(0.5*vol);
				Out.ar(outBus, sound*env*pauseEnv);
			}).writeDefFile;

		}
	}

	makeSynth {
		synths.put(0, Synth("pinkNoise_mod", [\outBus, outBus, \freqBus, freqBus, \volBus, volBus, \attackBus, attackBus, \releaseBus, releaseBus], group))
	}
}


//
// MincekSine_Mod : Module_Mod {
// 	var sineWaves, freqList, lagList, volList, freqNum, text;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("mincekSine_mod", {arg freq, lagTime, outBus, vol=0, lilVol = 1, gate = 1, pauseGate = 1;
// 				var sine, env, pauseEnv;
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,6), pauseGate, doneAction:1);
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
//
// 				sine = SinOsc.ar(VarLag.kr(freq, lagTime), 0, Lag.kr(vol, 0.01)*Lag.kr(lilVol, 0.1)*0.1);
//
// 				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("MincekSine",Rect(490, 510, 300, 250));
//
// 		this.makeMixerToSynthBus;
//
// 		this.initControlsAndSynths(23);
//
// 		dontLoadControls = (1..22);
//
// 		/*freqList = [[71,69,60,60], [75.7,64,52.3,60], [87.3,50.7,60,60], [76.3,61.7,60,60], [83,52,60,60], [93.3,45,60,60], [70.7,69.3,60,60], [86,75,49,38], [79,68,54,43], [84,71,49,36], [62,61,59,58]];
//
// 		lagList = [0, 0, 0, 60, 45, 0, 55, 0, 120, 120, 240];
//
// 		volList = [[1,1,0,0], [1,1,1,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,0,0], [1,1,1,1], [1,1,1,1], [1,1,1,1], [1,1,1,1]];*/
//
// 		freqList = [[71,69,60,60], [70,64,52.3,60], [87.3,50.7,60,60],
// 			[75.7,64,52.3,60], [80,57,60,60], [70.7,69.3,60,60],
// 			[86,75,49,38], [79,68,54,43], [84,71,49,36],
// 		[62,61,59,58], [62,61,59,58]];
//
// 		lagList = [0, 0, 0,
// 			0, 0, 64,
// 			0, 27, 36,
// 		82, 240];
//
// 		volList = [[1,0,0,0], [1,0,0,0], [1,1,0,0],
// 			[1,1,1,0], [1,1,0,0], [1,1,0,0],
// 			[1,1,1,1], [1,1,1,1], [1,1,1,1],
// 		[1,1,1,1], [1,1,1,1]];
//
// 		freqNum = 0;
//
// 		sineWaves = List.new;
//
// 		4.do{arg i;
// 			sineWaves.add(Synth("mincekSine_mod", [\freq, freqList[0][i].midicps, \lagTime, 1, \outBus, outBus], group));
// 		};
//
//
// 		controls.add(QtEZSlider.new("volume", ControlSpec(0, 1, 'amp'),
// 			{|v|
// 				sineWaves.do{arg item; item.set(\vol, v.value)};
// 			}, 0, true, \horz)
// 		);
// 		this.addAssignButton(0,\continuous);
//
//
// 		freqList.size.do{arg i;
//
// 			controls.add(Button()
// 				.states_([ [ "chord"+(i+1).asString, Color.green, Color.black ], [ "chord"+(i+1).asString, Color.black, Color.green ] ])
// 				.action_({arg but;
//
// 					4.do{|i2| sineWaves[i2].set(\freq, freqList[i][i2].midicps, \lagTime, lagList[i], \lilVol, volList[i][i2])};
// 					//text.string = freqNum.asString;
// 				})
// 			);
// 			this.addAssignButton(i+1,\onOff);
// 		};
//
// 		freqList.size.do{arg i;
// 			controls.add(Button()
// 				.states_([ [ "chord"+i.asString, Color.green, Color.black ], [ "chord"+i.asString, Color.black, Color.green ] ])
// 				.action_({arg but;
//
// 					4.do{|i2| sineWaves[i2].set(\freq, freqList[i][i2].midicps, \lagTime, 1, \lilVol, volList[i][i2])};
// 					//text.string = freqNum.asString;
// 				})
// 			);
// 			this.addAssignButton(i+12,\onOff);
// 		};
//
// 		win.layout_(VLayout(
// 			HLayout(controls[0],assignButtons[0]),
// 			HLayout(controls[1],controls[2],controls[3],controls[4],controls[5],controls[6],controls[7],controls[8],controls[9],controls[10],controls[11]),
//
// 			HLayout(assignButtons[1],assignButtons[2],assignButtons[3],assignButtons[4],assignButtons[5],assignButtons[6],assignButtons[7],assignButtons[8],assignButtons[9],assignButtons[10],assignButtons[11]),
// 			HLayout(controls[12],controls[13],controls[14],controls[15],controls[16],controls[17],controls[18],controls[19],controls[20],controls[21],controls[22]),
// 			HLayout(assignButtons[12],assignButtons[13],assignButtons[14],assignButtons[15],assignButtons[16],assignButtons[17],assignButtons[18],assignButtons[19],assignButtons[20],assignButtons[21],assignButtons[22])
// 		));
// 	}
//
// 	pause {
// 		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 0, \vol)}}
// 	}
//
// 	unpause {
// 		sineWaves.do{|item| item.do{|item| item.set(\pauseGate, 1); item.run(true)}};
//
// 	}
//
// 	killMeSpecial {
// 		sineWaves.do{arg item; item.set(\gate, 0)};
// 	}
//
//
// }

