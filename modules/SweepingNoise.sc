SweepingNoise_Mod : Module_Mod {
	var volBus, distortBus;

	*initClass {
		StartUp.add {
			SynthDef("sweepingNoise_mod", {arg outBus, volBus, lowRange, highRange, gate = 1, pauseGate = 1, localGate = 0;
				var noise, localEnv, pauseEnv, env, freq, volume;

				volume = In.kr(volBus);

				freq = LFTri.kr(0.03).exprange(lowRange, highRange);

				noise = MidEQ.ar([BrownNoise.ar(0.1)+WhiteNoise.ar(0.1)+LFClipNoise.ar(freq,0.1), BrownNoise.ar(0.1)+ WhiteNoise.ar(0.1)+LFClipNoise.ar(freq,0.1)], freq, 0.3, 48 ).softclip*0.5;

				localEnv = EnvGen.kr(Env.asr(0,1,0), localGate, doneAction:0);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				Out.ar(outBus, noise*localEnv*pauseEnv*env*volume);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SweepingNoise", Rect(500,100,140,280));
		this.initControlsAndSynths(4);

		synths = List.newClear(1);

		volBus = Bus.control(group.server);
		volBus.set(0);

		synths.put(0, Synth("sweepingNoise_mod", [\outBus, outBus, \volBus, volBus.index, \lowRange, 30, \highRange, 3000], group));

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 200), "vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 210, 60, 20));

		controls.add(EZRanger.new(win,Rect(70, 10, 60, 200), "range", ControlSpec(30,3000,'exponential'),
			{|v|
				synths[0].set(\lowRange, v.value[0], \highRange, v.value[1]);
			}, [30,3000], true, layout:\vert));
		//this.addAssignButton(1,\continuous, Rect(10, 210, 60, 20));

		controls.add(Button.new(win,Rect(10, 230, 120, 20))
			.states_([ [ "Off", Color.red, Color.black ], [ "On", Color.black, Color.green ]])
			.action_({|v|
				synths.do{|item| if(item!=nil, {item.set(\localGate, v.value)})};
			}));
		this.addAssignButton(2,\onOff, Rect(10, 250, 120, 20));

		//multichannel button
		numChannels = 2;
//		controls.add(Button(win,Rect(10, 275, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						3.do{|i| synths[i+1].set(\gate, 0)};
//					},
//					1, {
//						synths.put(1, Synth("sweepingNoise_mod", [\outBus, outBus+2, \volBus, volBus.index], group));
//						numChannels = 4;
//					},
//					2, {
//						if(numChannels==2,{
//							synths.put(1, Synth("sweepingNoise_mod", [\outBus, outBus+2, \volBus, volBus.index], group));
//						});
//						synths.put(2, Synth("sweepingNoise_mod", [\outBus, outBus+4, \volBus, volBus.index], group));
//						synths.put(3, Synth("sweepingNoise_mod", [\outBus, outBus+6, \volBus, volBus.index], group));
//						numChannels = 8;
//					}
//				)
//			};
//		);
	}

	killMeSpecial {
		volBus.free;
	}

/*	loadSettings {arg xmlSynth;
			rout = Routine({
			group.server.sync;
			2.do{arg i;
				midiHidTemp = xmlSynth.getAttribute("controls"++i.asString);
				if(midiHidTemp!=nil,{
					controls[i].valueAction_(midiHidTemp.interpret);
				});
			};
		});

		AppClock.play(rout);
	}*/
}