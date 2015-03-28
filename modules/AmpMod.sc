AmpMod_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("ampMod_mod", {arg inBus, outBus, pulseRate0 = 1, pulseRate1 = 1, onBypass=0, gate=1, pauseGate=1;
				var env, out, impulse, dust, mod, pauseEnv;

				impulse = Impulse.kr(pulseRate0);
				dust = Dust.kr(pulseRate1);

				mod = Lag.kr(Select.kr(onBypass, [1, Stepper.kr(impulse+dust, 0, 0, 1, 1, 0)]), 0.01);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 8)*env*mod*pauseEnv);
			}).writeDefFile;
		}
	}

	init {

		"outBus ".post; outBus.postln;

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("ampMod_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		controls.add(Button()
			.states_([["impulse", Color.blue, Color.black],["impulse", Color.black, Color.blue]])
			.action_({arg butt;
				if(impulseOn,{
					impulseOn = false;
					if(dustOn,{
						synths[0].set(\pulseRate0, 0, \pulseRate1, rrand(pulseRate[0], pulseRate[1])*2, \onBypass, 1);
						controls[2].value_(0);
					},{
						synths[0].set(\pulseRate0, 0, \pulseRate1, 0, \onBypass, 0);
						controls[2].value_(1);
					});
					butt.value_(0);
				},{
					impulseOn = true;
					if(dustOn,{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \pulseRate1, rrand(pulseRate[0], pulseRate[1]), \onBypass, 1);
					},{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2, \pulseRate1, 0, \onBypass, 1);
					});
					butt.value_(1);
					controls[2].value_(0);
				})
			})
		);

		controls.add(Button()
			.states_([["dust", Color.blue, Color.black],["dust", Color.black, Color.blue]])
			.action_({arg butt;
				if(dustOn,{
					dustOn = false;
					if(impulseOn,{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2, \pulseRate1, 0, \onBypass, 1);
						controls[2].value_(0);
					},{
						synths[0].set(\pulseRate0, 0, \pulseRate1, 0, \onBypass, 0);
						controls[2].value_(1);
					});
					butt.value_(0);
				},{
					dustOn = true;
					if(impulseOn,{
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \pulseRate1, rrand(pulseRate[0], pulseRate[1]), \onBypass, 1);
					},{
						synths[0].set(\pulseRate0, 0, \pulseRate1, rrand(pulseRate[0], pulseRate[1])*2, \onBypass, 1);
					});
					butt.value_(1);
					controls[2].value_(0);
				})
			})
		);

		controls.add(Button()
			.states_([["bypass", Color.blue, Color.black],["bypass", Color.black, Color.blue]])
			.action_({arg butt;
				impulseOn = false;
				dustOn = false;
				synths[0].set(\pulseRate0, 0, \pulseRate1, 0, \onBypass, 0);
				butt.value_(1);
				controls[0].value_(0);
				controls[1].value_(0);
			})
		);

		this.addAssignButton(0,\onOff);
		this.addAssignButton(1,\onOff);
		this.addAssignButton(2,\onOff);

		controls.add(QtEZRanger("speed", ControlSpec(0.25, 30, 'linear'),
			{arg val;
				pulseRate = val.value;
				if(impulseOn&&dustOn,{
					"both".postln;
					synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1]), \pulseRate1, rrand(pulseRate[0], pulseRate[1]));
				},{
					if(impulseOn,{
						"impulse".postln;
						synths[0].set(\pulseRate0, rrand(pulseRate[0], pulseRate[1])*2);
					},{
						if(dustOn,{
							"dust".postln;
							synths[0].set(\pulseRate1, rrand(pulseRate[0], pulseRate[1])*2);
						})
					})
				})
			}, [4, 7], true, \horz));

		controls[2].valueAction_(1);

		this.makeWindow("AmpMod", Rect(832, 531, 233, 74));

		win.layout_(VLayout(
			HLayout(controls[0], controls[1], controls[2]),
			HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout),
			controls[3].layout
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		//win.drawFunc_{arg win; win.bounds.postln;};

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