TriggerDelays_Mod : Module_Mod {
	var delLow, delHi, rangeSliders, decayLo, decayHi, volBus;

	*initClass {
		StartUp.add {
			SynthDef("triggerDelays_mod", {arg input, outBus, volBus, trigDur, decayTime;
				var in, bigEnv, lilEnv, out, trig, shift, vol;

				in = In.ar(input);

				vol = In.kr(volBus);

				trig = Trig.kr(1, trigDur);
				lilEnv = EnvGen.kr(Env.asr(0.05,1,0.05), trig);

				bigEnv = EnvGen.kr(Env.new([0,4,4,0],[0.05, decayTime, 0.3]), doneAction:2);

				shift = (LFNoise2.kr(Rand(0.1, 0.4), 0.5)*Line.kr(0, 1, decayTime/4))+1;

				out = CombC.ar(in*lilEnv, 3, trigDur, decayTime);

				out = PitchShift.ar(out, 0.2, shift, 0.01);
				out = Pan2.ar(out, Line.kr(Rand(-1,1), Rand(-1, 1), decayTime+0.4));

				Out.ar(outBus, out*bigEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("TriggerDelays",Rect(680, 421, 300, 100));

		rangeSliders = List.new;

		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus(1);

		synths = List.newClear(4);

		volBus = Bus.control(group.server);
		volBus.set(0);

		controls.add(QtEZRanger.new("del", ControlSpec(0.05, 1.5, 'exponential'),
			{|v|
				v = v.value;
				delLow = v[0];
				delHi = v[1];
			}, [0.1,0.8], true, \horz));
		this.addAssignButton(0,\range);

		controls.add(QtEZRanger.new("dec", ControlSpec(10, 30, 'linear'),
			{|v|
				v = v.value;
				decayLo = v[0];
				decayHi = v[1];
			}, [20,30], true,\horz));
		this.addAssignButton(1,\range);

		controls.add(QtEZSlider.new("vol", ControlSpec(0, 1, 'amp'),
			{|v|
				volBus.set(v.value)
			}, 0, true,\horz));
		this.addAssignButton(2,\continuous);

		controls.add(Button.new()
			.states_([[ "T", Color.black, Color.red ], [ "T", Color.red, Color.black ]])
			.action_{|v|
				switch(numChannels,
					2,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					},
					4,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+2, \outBus, outBus.index+2, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					},
					8,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+2, \outBus, outBus.index+2, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+4, \outBus, outBus.index+4, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+6, \outBus, outBus.index+6, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					}
				)
			});
		this.addAssignButton(3,\onOff);

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 190, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
					},
					1, {
						numChannels = 4;
					},
					2, {
						numChannels = 8;
					}
				)
			};
		);

		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout),
				HLayout(controls[2].layout, assignButtons[2].layout),
				HLayout(controls[3], assignButtons[3].layout),
				HLayout(controls[4], nil)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	pause {

	}

	unpause{

	}

}