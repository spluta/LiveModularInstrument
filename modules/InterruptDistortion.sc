InterruptDistortion_Mod : Module_Mod {
	var volBus, distortBus;

	*initClass {
		StartUp.add {
			SynthDef("interruptDistortion_mod", {arg inBus, outBus, sinRate=1, volBus, distortBus, distortSwitch=0, gate = 1, pauseGate = 1;
				var in, out, chan1, chan2, delayTime, pan, pauseEnv, env, muteEnv, vol;

				vol = In.kr(volBus);
				//distortSwitch = In.kr(distortBus);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = In.ar(inBus, 2);

				chan1 = Mix.new(in);

				delayTime = (chan1 * LFNoise2.kr(2, 0.05, 0.01)) + 0.15;
				chan1 = DelayC.ar(chan1, 0.25, delayTime).distort;
				pan = SinOsc.ar(sinRate);
				chan2 = DelayC.ar(chan1, 0.025, 0.025).neg.softclip;
				out = Pan2.ar(chan1, pan) + Pan2.ar(chan2, pan.neg);


				out = (Lag.kr(1-distortSwitch, 0.05)*in)+(Lag.kr(distortSwitch, 0.05)*out*vol);

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("InterruptDistortion", Rect(500,100,200,40));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(8);

		volBus = Bus.control(group.server);
		//distortBus = Bus.control(group.server);

		//distortBus.set(0);
		volBus.set(0);

		synths = List.newClear(4);

		synths.put(0, Synth("interruptDistortion_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \sinRate, 3, \volBus, volBus, \distortBus, distortBus], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, true, orientation:\horz));
		this.addAssignButton(0,\continuous);

		controls.add(Button.new()
			.states_([ [ "Pass", Color.green, Color.black ], [ "Distort", Color.black, Color.green ]])
			.action_({|v|
				//distortBus.set(v.value);
				synths.do{|item| if(item!=nil, {item.set(\distortSwitch, v.value, \sinRate, rrand(0.5, 3))})};
			}));
		this.addAssignButton(1,\onOff);

		//multichannel button
		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("interruptDistortion_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \sinRate, 3, \volBus, volBus, \distortBus, distortBus], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("interruptDistortion_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \sinRate, 3, \volBus, volBus, \distortBus, distortBus], group));
						});
						synths.put(2, Synth("interruptDistortion_mod", [\inBus, mixerToSynthBus.index+4, \outBus, outBus.index+4, \sinRate, 3, \volBus, volBus, \distortBus, distortBus], group));
						synths.put(3, Synth("interruptDistortion_mod", [\inBus, mixerToSynthBus.index+6, \outBus, outBus.index+6, \sinRate, 3, \volBus, volBus, \distortBus, distortBus], group));
						numChannels = 8;
					}
				)
			};
		);

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1], controls[2])
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;

	}

	killMeSpecial {
		volBus.free;
	}
}