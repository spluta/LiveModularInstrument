EnvGen_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate, attackRelease, curve;

	*initClass {
		StartUp.add {
			SynthDef("envGen_mod", {arg inBus, outBus, pulseRate0=0, onBypass=0, attack=0.01, release=1, curve=(-4), whichOscil=0, oscilFreq, oscilMult, gate = 1, pauseGate = 1;
				var env, localEnv, out, impulse, dust, mod, pauseEnv, oscil;

				oscil = Select.kr(whichOscil, [0, LFSaw.kr(oscilFreq, 0, oscilMult, oscilMult/2), LFTri.kr(oscilFreq, 0, oscilMult, oscilMult/2)]);

				impulse = Impulse.kr(pulseRate0+oscil);

				localEnv = EnvGen.kr(Env.perc(attack, release, 1, curve), impulse);

				mod = Lag.kr(Select.kr(onBypass, [1, localEnv]), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 8)*env*mod*pauseEnv*mod);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("EnvGen", Rect(500, 500, 385, 95));

		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("envGen_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;

		controls.add(Button(win, Rect(5, 5, 90, 20))
			.states_([["fast", Color.blue, Color.black],["fast", Color.black, Color.blue]])
			.action_({arg butt;
				this.setAttackRelease;
				pulseRate = rrand(5.0, 10);
				synths[0].set(\pulseRate0, pulseRate, \onBypass, 1, \attack, attackRelease[0], \release, attackRelease[1], \curve, curve);
				this.setControls([1,0,0,0]);
			})
		);

		controls.add(Button(win, Rect(100, 5, 90, 20))
			.states_([["med", Color.blue, Color.black],["med", Color.black, Color.blue]])
			.action_({arg butt;
				this.setAttackRelease;
				pulseRate = rrand(2.5, 5);
				synths[0].set(\pulseRate0, pulseRate, \onBypass, 1, \attack, attackRelease[0], \release, attackRelease[1], \curve, curve);
				this.setControls([0,1,0,0]);
			})
		);

		controls.add(Button(win, Rect(195, 5, 90, 20))
			.states_([["slow", Color.blue, Color.black],["slow", Color.black, Color.blue]])
			.action_({arg butt;
				this.setAttackRelease;
				pulseRate = rrand(1, 2.5);
				synths[0].set(\pulseRate0, pulseRate, \onBypass, 1, \attack, attackRelease[0], \release, attackRelease[1], \curve, curve);
				this.setControls([0,0,1,0]);
			})
		);

		controls.add(Button(win, Rect(290, 5, 90, 20))
			.states_([["off", Color.blue, Color.black],["off", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\onBypass, 0);
				this.setControls([0,0,0,1]);
			})
		);

		this.addAssignButton(0,\onOff, Rect(5, 25, 90, 20));
		this.addAssignButton(1,\onOff, Rect(100, 25, 90, 20));
		this.addAssignButton(2,\onOff, Rect(195, 25, 90, 20));
		this.addAssignButton(3,\onOff, Rect(290, 25, 90, 20));

		controls.add(Button(win, Rect(5, 50, 90, 20))
			.states_([["Saw", Color.blue, Color.black],["Saw", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\whichOscil, 1, \oscilFreq, rrand(0.05, 0.3), \oscilMult, rrand(1,6.0));
				this.setControls2([1,0,0]);
			})
		);

		controls.add(Button(win, Rect(100, 50, 90, 20))
			.states_([["Tri", Color.blue, Color.black],["Tri", Color.black, Color.blue]])
			.action_({arg butt;
				this.setAttackRelease;
				synths[0].set(\whichOscil, 2, \oscilFreq, rrand(0.05, 0.3), \oscilMult, rrand(1,6.0));
				this.setControls2([0,1,0]);
			})
		);

		controls.add(Button(win, Rect(195, 50, 90, 20))
			.states_([["none", Color.blue, Color.black],["none", Color.black, Color.blue]])
			.action_({arg butt;
				this.setAttackRelease;
				synths[0].set(\whichOscil, 0);
				this.setControls2([0,0,1]);
			})
		);

		this.addAssignButton(4,\onOff, Rect(5, 70, 90, 20));
		this.addAssignButton(5,\onOff, Rect(100, 70, 90, 20));
		this.addAssignButton(6,\onOff, Rect(195, 70, 90, 20));

		this.setControls([0,0,0,1]);
		this.setControls2([0,0,1]);

	}

	setControls {|vals|
		controls.copyRange(0,3).do{arg item, i; item.value_(vals[i])};
	}

	setControls2 {|vals|
		controls.copyRange(4,6).do{arg item, i; item.value_(vals[i])};
	}

	setAttackRelease {|vals|
		attackRelease = [rrand(0.05, 0.001), 1];
		if(0.1.coin,{attackRelease = attackRelease.rotate(1)});
		curve = rrand(-4,-8);
	}

}