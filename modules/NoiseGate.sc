Compander_Mod : Module_Mod {
	var buttons, threshBus, slopeBelowBus, slopeAboveBus, attackBus, releaseBus;

	*initClass {
		StartUp.add {
			SynthDef("compander_mod", {arg threshBus, slopeBelowBus, slopeAboveBus, attackBus, releaseBus, inBus, outBus, gate = 1, pauseGate = 1;
				var in, out, env, pauseEnv, thresh, slopeBelow, slopeAbove, attack, release;

				thresh = In.kr(threshBus);
				slopeBelow = In.kr(slopeBelowBus);
				slopeAbove = In.kr(slopeAboveBus);
				attack = In.kr(attackBus);
				release = In.kr(releaseBus);

				in = In.ar(inBus, 2);

				out = Compander.ar(in, in,
					thresh: thresh,
					slopeBelow: slopeBelow,
					slopeAbove: slopeAbove,
					clampTime: attack,
					relaxTime: release
				);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Compander", Rect(946, 618, 420, 240));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus(2);

		threshBus = Bus.control(group.server);
		slopeBelowBus = Bus.control(group.server);
		slopeAboveBus = Bus.control(group.server);
		attackBus = Bus.control(group.server);
		releaseBus = Bus.control(group.server);

		synths = List.newClear(4);

		synths = synths.put(0, Synth("compander_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \threshBus, threshBus, \slopeBelowBus, slopeBelowBus, \slopeAboveBus, slopeAboveBus, \attackBus, attackBus, \releaseBus, releaseBus], group));

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "thresh", ControlSpec(0.001,0.25,'linear'),
			{|v|
				threshBus.set(v.value);
			}, 0.1, true, layout:\vert));
		controls.add(EZSlider.new(win,Rect(70, 10, 60, 220), "slBelow", ControlSpec(0,10,'linear'),
			{|v|
				slopeBelowBus.set(v.value);
			}, 1, true, layout:\vert));
		controls.add(EZSlider.new(win,Rect(130, 10, 60, 220), "slAbove", ControlSpec(0,5,'linear'),
			{|v|
				slopeAboveBus.set(v.value);
			}, 1, true, layout:\vert));
		controls.add(EZSlider.new(win,Rect(190, 10, 60, 220), "att", ControlSpec(0.001,1,'exponential'),
			{|v|
				attackBus.set(v.value);
			}, 0.01, true, layout:\vert));
		controls.add(EZSlider.new(win,Rect(250, 10, 60, 220), "rel", ControlSpec(0.001,1,'exponential'),
			{|v|
				releaseBus.set(v.value);
			}, 0.01, true, layout:\vert));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(0, 325, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("compander_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \threshBus, threshBus, \slopeBelowBus, slopeBelowBus, \slopeAboveBus, slopeAboveBus, \attackBus, attackBus, \releaseBus, releaseBus], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("compander_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \threshBus, threshBus, \slopeBelowBus, slopeBelowBus, \slopeAboveBus, slopeAboveBus, \attackBus, attackBus, \releaseBus, releaseBus], group));
						});
						2.do{|i| synths.put(i+2, Synth("compander_mod", [\inBus, mixerToSynthBus.index+4+(i*2), \outBus, outBus.index+4+(i*2), \threshBus, threshBus, \slopeBelowBus, slopeBelowBus, \slopeAboveBus, slopeAboveBus, \attackBus, attackBus, \releaseBus, releaseBus], group))};
						numChannels = 8;
					}
				)
			};
		);

		buttons.add(Button(win, Rect(330, 10, 80, 20))
			.states_([["noizGate", Color.black, Color.red]])
			.action_({arg butt;
				controls[1].valueAction_(10);
				controls[2].valueAction_(1);
				controls[3].valueAction_(0.01);
				controls[4].valueAction_(0.01);
			}));
		buttons.add(Button(win, Rect(330, 30, 80, 20))
			.states_([["compressor", Color.black, Color.red]])
			.action_({arg butt;
				controls[1].valueAction_(1);
				controls[2].valueAction_(0.5);
				controls[3].valueAction_(0.01);
				controls[4].valueAction_(0.01);
			}));
		buttons.add(Button(win, Rect(330, 50, 80, 20))
			.states_([["limiter", Color.black, Color.red]])
			.action_({arg butt;
				controls[1].valueAction_(1);
				controls[2].valueAction_(0.1);
				controls[3].valueAction_(0.01);
				controls[4].valueAction_(0.01);
			}));
		buttons.add(Button(win, Rect(330, 70, 80, 20))
			.states_([["sustainer", Color.black, Color.red]])
			.action_({arg butt;
				controls[1].valueAction_(0.1);
				controls[2].valueAction_(1);
				controls[3].valueAction_(0.01);
				controls[4].valueAction_(0.01);
			}));
	}
}