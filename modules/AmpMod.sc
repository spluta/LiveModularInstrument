AmpMod_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("ampMod_mod", {arg inBus, outBus, pulseRate0 = 1, pulseRate1 = 1, onBypass=0, gate=1, pauseGate=1;
				var env, out, impulse, dust, mod, pauseEnv;

				impulse = Impulse.kr(pulseRate0);
				//dust = Dust.kr(pulseRate1);

				mod = Lag.kr(Select.kr(onBypass, [1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.02);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 2)*env*mod*pauseEnv);
			}).writeDefFile;
		}
	}

	init {

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("ampMod_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		controls.add(Button()
			.states_([["impulse", Color.blue, Color.black],["impulse", Color.black, Color.blue]])
			.action_({arg butt;
				controls[1].value_(0);
				synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \onBypass, 1);
				//this.sendOSC(0, butt.value);
			})
		);

		controls.add(Button()
			.states_([["bypass", Color.blue, Color.black],["bypass", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\pulseRate0, 0, \pulseRate1, 0, \onBypass, 0);
				controls[0].value_(0);
				//this.sendOSC(1, butt.value);
			})
		);

		this.addAssignButton(0,\onOff);
		this.addAssignButton(1,\onOff);
		//this.addAssignButton(2,\onOff);

		controls.add(QtEZRanger("speed", ControlSpec(0.25, 30, 'linear'),
			{arg val;
				pulseRate = val.value;
				if(impulseOn&&dustOn,{
					synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \pulseRate1, rrand(pulseRate[0], pulseRate[1]));
				},{
					if(impulseOn,{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2);
					},{
						if(dustOn,{
							synths[0].set(\pulseRate1, rrand(pulseRate[0], pulseRate[1])*2);
						})
					})
				})
			}, [4, 7], true, \horz));

		controls[1].valueAction_(1);

		this.makeWindow("AmpMod", Rect(0, 0, 200, 40));

		win.layout_(VLayout(
			HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15)),
			HLayout(assignButtons[0], assignButtons[1]),
			controls[2]
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		//win.front;
	}
}

TestSine_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("testSine_mod", {arg outBus, gate=1, pauseGate=1;
				var env, out, impulse, dust, mod, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = SinOsc.ar(LFNoise2.kr(0.1).range(35, 50))+SinOsc.ar(LFNoise2.kr(0.1).range(50, 90))+SinOsc.ar(LFNoise2.kr(0.1).range(90, 12))+SinOsc.ar(LFNoise2.kr(0.1).range(120, 200))+SinOsc.ar(LFNoise2.kr(0.1).range(200, 500))+SinOsc.ar(LFNoise2.kr(0.1).range(500, 550));

				Out.ar(outBus, out*0.05*env*pauseEnv);

				//Out.ar(outBus, SinOsc.ar(MouseX.kr(100, 3000).poll*0.1*env*pauseEnv));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("TestSine", Rect(500, 500, 290, 80));

		synths = List.new;

		synths.add(Synth("testSine_mod", [\outBus, outBus], group));

	}
}