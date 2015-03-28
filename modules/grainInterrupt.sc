GrainInterrupt_Mod : Module_Mod {
	var compThreshBus, gainBus, limitBus, shiftSpec, shiftUp, shiftDown, shiftOn, ratio, xFadeSig, mult, add;

	*initClass {
		StartUp.add {
			SynthDef("grainInterrupt_mod", {arg inBus, outBus, xFadeVal, ratio=1, lfoSwitch = 0, lfoFreq=0.2, mult=1, add=0.25, negMult=1, gate = 1, pauseGate = 1;
				var out, in, shift, shiftL, shiftR, pauseEnv, env, lfo, internalRatio, pDisp=0, tDisp=0;

				in = In.ar(inBus, 2);

				lfo = LFSaw.kr(lfoFreq, 0, mult*negMult, mult/2+add+0.25);

				internalRatio = Select.kr(lfoSwitch, [ratio, lfo]);

				pDisp = LFNoise2.kr(0.5, 0.12, 0.13).abs;
				tDisp = LFNoise2.kr(0.4, 0.08, 0.09).abs;

				#shiftL, shiftR = PitchShift.ar(in, 0.02, internalRatio, pDisp, tDisp);

				shiftL = Pan2.ar(shiftL, Lag.kr(TRand.kr(-1,0,Dust.kr(0.1)), 0.05));
				shiftR = Pan2.ar(shiftR, Lag.kr(TRand.kr(0,1,Dust.kr(0.1)), 0.05));

				shift = shiftL+shiftR;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				out = SelectX.ar(Lag.kr(xFadeVal,0.02), [in, shift*1.5]);

				Out.ar(outBus, out*pauseEnv*env);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GrainInterrupt", Rect(800, 800, 190, 250));
		this.initControlsAndSynths(6);

		this.makeMixerToSynthBus(8);

		synths = List.newClear(4);

		xFadeSig = Env.new([0,1,1], [1,1]).asSignal(200);
		shiftUp = 0;
		shiftDown = 0;
		shiftSpec = ControlSpec(0.25, 4, \exponential);
		shiftOn = false;

		synths.put(0, Synth("grainInterrupt_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \xFadeVal, 0, \ratio, 1, \pDisp, 0, \tDisp, 0], group));

		controls.add(Button(win, Rect(5, 0, 60, 20))
			.states_([["OFF",Color.red, Color.black],["ON",Color.black,Color.green]])
			.action_({|butt|
				if(butt.value==1,{
					shiftOn = true;
				},{
					shiftOn = false;
				});
				controls[1].value_(0);
				synths.do{arg item; item.set(\xFadeVal, butt.value, \lfoSwitch, 0, \ratio, 1, \pDisp, rrand(0.1, 0.25), \tDisp, rrand(0.02, 0.1))};
			})
		);
		this.addAssignButton(0,\onOff, Rect(65, 0, 20, 20));

		controls.add(Button(win, Rect(85, 0, 60, 20))
			.states_([["LFOoff",Color.red, Color.black],["LFOon",Color.black,Color.green]])
			.action_({|butt|
				if(butt.value==1,{
					shiftOn = true;
				},{
					shiftOn = false;
				});
				controls[0].value_(0);
				mult = rrand(0.125, 2);
				add = rrand(0.25, 0.75);
				if((mult*2+add+0.25)>4,{add=4-(mult*2)-0.25});
				mult.postln;
				add.postln;
				synths.do{arg item; item.set(\xFadeVal, butt.value, \lfoSwitch, butt.value, \negMult, [1,-1].choose, \lfoFreq, rrand(0.25, 0.75), \mult, mult, \add, add,\pDisp, rrand(0.01, 0.25), \tDisp, rrand(0.002, 0.1))};
			})
		);
		this.addAssignButton(1,\onOff, Rect(145, 0, 20, 20));

		controls.add(EZSlider(win, Rect(5, 20, 60, 200), "xFade", ControlSpec(0,199,\linear),
			{|val|
				if(shiftOn.not,{
					synths.do{arg item; item.set(\xFadeVal, xFadeSig[val.value], \pDisp, val.value/400, \tDisp, val.value/800)}
				},
				{
					synths.do{arg item; item.set(\pDisp, val.value/400, \tDisp, val.value/800)}
				});
			}, 1, true, layout:\vert)
		);
		this.addAssignButton(2,\continuous, Rect(5, 220, 60, 20));

		controls.add(EZSlider(win, Rect(65, 20, 60, 200), "ShiftUp", ControlSpec(0,0.5,\linear),
			{|val|
				shiftUp = val.value;
				ratio = shiftSpec.map(0.5+shiftUp-shiftDown);
				synths.do{arg item; item.set(\ratio, ratio)};
			}, 0.5, true, layout:\vert)
		);
		this.addAssignButton(3,\continuous, Rect(65, 220, 60, 20));

		controls.add(EZSlider(win, Rect(125, 20, 60, 200), "ShiftDown", ControlSpec(0,0.5,\linear),
			{|val|
				shiftDown = val.value;
				ratio = shiftSpec.map(0.5+shiftUp-shiftDown);
				synths.do{arg item; item.set(\ratio, ratio)};
			}, 0.5, true, layout:\vert)
		);
		this.addAssignButton(4,\continuous, Rect(125, 220, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(5, 245, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("grainInterrupt_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \xFadeVal, 0, \ratio, 1, \pDisp, 0, \tDisp, 0], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("grainInterrupt_mod",[\inBus, mixerToSynthBus.index+2, \outBus, outBus, \xFadeVal, 0, \ratio, 1, \pDisp, 0, \tDisp, 0], group));
						});
						synths.put(2, Synth("grainInterrupt_mod",[\inBus, mixerToSynthBus.index+4, \outBus, outBus, \xFadeVal, 0, \ratio, 1, \pDisp, 0, \tDisp, 0], group));
						synths.put(3, Synth("grainInterrupt_mod",[\inBus, mixerToSynthBus.index+6, \outBus, outBus, \xFadeVal, 0, \ratio, 1, \pDisp, 0, \tDisp, 0], group));
						numChannels = 8;
					}
				)
			};
		);
	}
}