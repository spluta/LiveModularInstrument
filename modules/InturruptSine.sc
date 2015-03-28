InterruptSine_Mod : Module_Mod {
	var volBus, pulseRate, impulseOn, dustOn;

	*initClass {
		StartUp.add {
			SynthDef("interruptSine_mod", {arg inBus, outBus, volBus, pulseRate, whichSig = 0, t_shortTrigger=0, t_freqTrigger=0, gate = 1, pauseGate = 1;
				var in, sine, out, freq, switch, dust, impulseRate, pauseEnv, env, muteEnv, vol, trig;

				vol = In.kr(volBus);

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				dust = Dust.kr(pulseRate);

				switch = Select.kr(whichSig, [0, Trig1.kr(Decay.kr(t_shortTrigger,0.1), TRand.kr(0.3, 0.7)), Trig1.kr(Impulse.kr(pulseRate), 1/(2*pulseRate)), Trig1.kr(dust, TRand.kr(0.05, 0.15, dust))]);

				in = In.ar(inBus, 8);

				trig = Trig1.kr(Decay.kr(t_freqTrigger, 0.02), 0.1);

				freq = TRand.kr(60, 10000, Select.kr(TChoose.kr(trig, [0,1]), [trig, switch]));

				//sine	= SinOsc.ar([freq, freq+Rand(-1,1), freq+Rand(-1,1), freq+Rand(-1,1), freq+Rand(-1,1), freq+Rand(-1,1), freq+Rand(-1,1), freq+Rand(-1,1)]);

				sine = SinOsc.ar([freq, freq+Rand(-1,1)]);

				out = (Lag.kr(1-switch, 0.05)*in)+(Lag.kr(switch, 0.05)*sine*vol);

				Out.ar(outBus, out);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("InterruptSine", Rect(500,100,280,130));
		this.initControlsAndSynths(6);

		this.makeMixerToSynthBus(8);

		volBus = Bus.control(group.server);

		volBus.set(0);

		synths = List.newClear(4);

		synths.put(0, Synth("interruptSine_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		controls.add(Button(win, Rect(5, 5, 70, 20))
			.states_([["bypass", Color.blue, Color.black],["bypass", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				dustOn = false;
				synths[0].set(\whichSig, 0);
				butt.value_(1);
				controls[1].value_(0);
				controls[2].value_(0);
				controls[3].value_(0);
			})
		);

		controls.add(Button(win, Rect(75, 5, 70, 20))
			.states_([["short", Color.blue, Color.black],["short", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				dustOn = false;
				synths[0].set(\whichSig, 1, \t_freqTrigger, 1, \t_shortTrigger, 1);
				butt.value_(1);
				controls[0].value_(0);
				controls[2].value_(0);
				controls[3].value_(0);
			})
		);

		controls.add(Button(win, Rect(145, 5, 70, 20))
			.states_([["impulse", Color.blue, Color.black],["impulse", Color.black, Color.blue]])
			.action_({arg butt;
				dustOn = false;
				if(impulseOn,{
					impulseOn = false;
					synths[0].set(\pulseRate, rrand(pulseRate[0], pulseRate[1]), \whichSig, 0);
					controls[0].value_(1);
					butt.value_(0);
				},{
					impulseOn = true;
					synths[0].set(\pulseRate, rrand(pulseRate[0], pulseRate[1]), \whichSig, 2, \t_freqTrigger, 1);
					butt.value_(1);
					controls[0].value_(0);
					controls[1].value_(0);
					controls[3].value_(0);
				})
			})
		);

		controls.add(Button(win, Rect(215, 5, 70, 20))
			.states_([["dust", Color.blue, Color.black],["dust", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				if(dustOn,{
					dustOn = false;
					synths[0].set(\pulseRate, rrand(pulseRate[0], pulseRate[1]), \whichSig, 0);
					controls[0].value_(1);
					butt.value_(0);
				},{
					dustOn = true;
					synths[0].set(\pulseRate, rrand(pulseRate[0], pulseRate[1]), \whichSig, 3, \t_freqTrigger, 1);
					butt.value_(1);
					controls[0].value_(0);
					controls[1].value_(0);
					controls[2].value_(0);
				})
			})
		);

		this.addAssignButton(0,\onOff, Rect(5, 25, 70, 20));
		this.addAssignButton(1,\onOff, Rect(75, 25, 70, 20));
		this.addAssignButton(2,\onOff, Rect(145, 25, 70, 20));
		this.addAssignButton(3,\onOff, Rect(215, 25, 70, 20));

		controls.add(EZRanger(win, Rect(5, 50, 280, 30), "speed", ControlSpec(0.25, 4, 'exp'),
			{arg val;
				pulseRate = val.value;
				synths[0].set(\pulseRate, rrand(pulseRate[0], pulseRate[1]));
			}, [4, 7], true));

		controls[0].valueAction_(1);

		controls.add(EZSlider.new(win,Rect(5, 85, 280, 20), "vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
			}, 1, true, layout:\horz));
		this.addAssignButton(0,\continuous, Rect(10, 105, 280, 20));
	}
}