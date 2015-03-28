NoisePulse_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate;

	*initClass {
		StartUp.add {
			SynthDef("noisePulse_mod", {arg outBus, vol, pulseRate = 1, panSpeed = 1, onOff=0, gate=1, pauseGate=1;
				var env, out, pauseEnv;
				var freqs, numSines = 50, onOffEnv;

				freqs = MouseY.kr(Array.rand(10, 270, 330), Array.rand(10, 7000, 9000));

				out = DynKlang.ar(`[
					SinOsc.kr(Array.rand(numSines, 0.7, 1.3), Array.rand(numSines, 0, 2), freqs, freqs+20),
					SinOsc.kr(Array.rand(numSines, 0.2, 1), 0, 0.1),
					[pi,pi,pi]
				])*Trig1.kr(Impulse.kr(MouseX.kr(4,15)), 0.05);
				onOffEnv = EnvGen.kr(Env.asr(0,1,0), onOff, doneAction:0);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, Pan2.ar(out, SinOsc.kr(panSpeed))*env*onOffEnv*pauseEnv*vol);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("NoisePulse", Rect(500, 500, 100, 210));

		this.initControlsAndSynths(2);

		synths = List.new;

		synths.add(Synth("noisePulse_mod", [\outBus, outBus], group));

		controls.add(EZKnob.new(win,Rect(0, 0, 100, 150), "vol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\vol, v.value)
			},0, true));
		this.addAssignButton(0,\continuous, Rect(0, 150, 100, 20));

		controls.add(Button(win, Rect(0, 170, 100, 20))
			.states_([["off", Color.blue, Color.black],["on", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\onOff, butt.value);
			})
		);
		this.addAssignButton(1,\onOff, Rect(0, 190, 100, 20));
	}
}