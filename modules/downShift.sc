DownShift_Mod : Module_Mod {
	var verbGroup, synthGroup, delayTime, length, volBus, verbBus, verbVolBus, start, end;

	*initClass {
		StartUp.add {
			SynthDef("DSverb2_mod",{arg verbBus, verbVolBus, outBus, gate=1, pauseGate=1;
				var in, out, pauseEnv, env, verbVol;

				verbVol = In.kr(verbVolBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				in = In.ar(verbBus);
				out = GVerb.ar(in, 80, 4.85, 0.41, 0.19, 15, 0, 0);

				Out.ar(outBus, out*env*pauseEnv*verbVolBus);
			}).writeDefFile;

			SynthDef("downShift2_mod", {|inBus, outBus, volBus, verbBus, length=1, delayTime, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, env, pauseEnv, verbEnv, shift, start0, start1;

				vol = In.kr(volBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.new([0,0,1,1,0], [delayTime, length/4,length/2,length/4]), gate, doneAction: 2);

				verbEnv = EnvGen.kr(Env.new([0,0,2], [delayTime, length]), gate, doneAction: 0);

				in = DelayC.ar(In.ar(inBus,1), delayTime, delayTime*[1,0.95]);

				start0 = Rand(1.95,2.05);
				start1 = Rand(4,8);

				shift = Select.kr(IRand(0,1), [
					EnvGen.kr(Env.new([start0, start0, Rand(0.25, 0.125)], [delayTime+(length/4), 3*length/4], \lin), 1),
					EnvGen.kr(Env.new([start1, start1, Rand(0.25, 0.125)], [delayTime+(length/4), 3*length/4], \lin), 1)
				]);

				out = PitchShift.ar(in, Rand(0.5, 1), shift, 0, 0.1);

				out = out*pauseEnv*vol;

				Out.ar(outBus, out*env);
				Out.ar(verbBus, Mix.new(out)*verbEnv);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("DownShift",Rect(718, 645, 210, 300));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		synthGroup = Group.tail(group);
		verbGroup = Group.tail(group);

		volBus = Bus.control(group.server);
		verbBus = Bus.audio(group.server, 4);
		verbVolBus = Bus.control(group.server);

		synths = List.newClear(1);
		synths.put(0, Synth("DSverb2_mod",[\verbBus, verbBus.index, \verbVol, 0, \outBus, outBus], verbGroup));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
			}, 1, true, orientation:\horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("verbVol", ControlSpec(0,2,'amp'),
			{|v|
				verbVolBus.set(v.value);
			}, 1, true, orientation:\horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZRanger.new("length", ControlSpec(3.0,20.0,'linear'),
			{|v|
				length = v.value;
			}, [4,5], true, orientation:\horz));
		this.addAssignButton(2,\continuous);

		controls.add(Button()
			.states_([["Go",Color.black,Color.green], ["Go",Color.green,Color.black]])
			.action_{|v|
				Synth("downShift2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \verbBus, verbBus.index, \verbVolBus, verbVolBus, \length, rrand(length[0], length[1]), \start, [1,-1].choose, \delayTime, 0], synthGroup)
			});
		this.addAssignButton(3,\onOff);

		win.layout_(VLayout(
			HLayout(controls[0], assignButtons[0]),
			HLayout(controls[1], assignButtons[1]),
			HLayout(controls[2], assignButtons[2]),
			HLayout(controls[3], assignButtons[3])
		));
		win.layout.spacing = 0;
		win.layout.margins = 0!4;

	}

	killMeSpecial {
		volBus.free;
		verbBus.free;
	}
}

FloatShifter_Mod : Module_Mod {
	var verbGroup, synthGroup, delayTime, length, volBus, verbBus;

	*initClass {
		StartUp.add {
			SynthDef("floatShifter_mod", {|inBus, outBus, volBus, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, env, pauseEnv, verbEnv;

				vol = In.kr(volBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0.1,1,2), gate, doneAction: 2);

				in = In.ar(inBus,2);

				out = PitchShift.ar(in, Rand(0.5, 1), TChoose.kr(Dust.kr(2), [1,0])+Rand(0.95, 1.05), Rand(0,0.05), Rand(0.1,0.7));

				out = out*pauseEnv*vol;

				Out.ar(outBus, out*env);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("FloatShifter",Rect(718, 645, 210, 300));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(2);

		volBus = Bus.control(group.server);

		synths = List.newClear(1);

		controls.add(EZSlider.new(win,Rect(5, 5, 60, 220), "vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
			}, 1, true, layout:\horz));
		this.addAssignButton(0,\continuous, Rect(5, 225, 60, 20));

		controls.add(Button(win,Rect(5, 250, 60, 20))
			.states_([["Off",Color.black,Color.green], ["On",Color.green,Color.black]])
			.action_{|v|
				if(v.value==1,{
					synths.put(0,Synth("floatShifter_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index], group));
				},{
					synths[0].set(\gate, 0);
				})
			});
		this.addAssignButton(1,\onOff, Rect(5, 270, 60, 20));
	}

	killMeSpecial {
		volBus.free;
	}
}