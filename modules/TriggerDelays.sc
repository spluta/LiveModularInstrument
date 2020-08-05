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
				Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
			});
		this.addAssignButton(3,\onOff);

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				HLayout(controls[2], assignButtons[2]),
				HLayout(controls[3], assignButtons[3])
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