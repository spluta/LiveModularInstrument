CrackleSynth_Mod : Module_Mod {
	var spec0, spec1;

	var valsArray;

	*initClass {
		StartUp.add {


			SynthDef("crackleSynth2_mod", { |outBus, vol, whichSawFreq = 0, sawFreq = 20, crackleMult = 20, crackleAdd=0.5, hpfFreq = 40, envRate=5, justOn = 1, whichOscil = 1, gate = 1, pauseGate = 1|
				var randSawFreq, synth, noise0, noise1, env, pauseEnv;

				randSawFreq = LFNoise0.kr(LFNoise0.kr(1, 5, 5)).range(20, 2020);

				synth = SelectX.ar(whichOscil, [SinOsc.ar(LFSaw.ar(Select.kr(whichSawFreq, [sawFreq,randSawFreq]), 0, 2000, 0)+Crackle.ar([1.93, 1.97], crackleMult, LFNoise0.kr(LFNoise2.kr(1, 60, 100), crackleAdd*5, crackleAdd*5000)), 0, 0.8),
					SinOsc.ar(LFSaw.ar(Select.kr(whichSawFreq, [sawFreq,randSawFreq]), 0, 2000, 0)+Crackle.ar([1.93, 1.97], crackleMult, LFNoise0.kr(LFNoise2.kr(1, 60, 100), crackleAdd*5, crackleAdd*5000)), 0, 0.8),
					Pulse.ar(Crackle.ar([1.93, 1.97], crackleMult, LFNoise0.kr(LFNoise2.kr(1, 60, 100), crackleAdd*5, crackleAdd*100)), 0.5,1.5)]);

				synth = HPF.ar(synth, hpfFreq).clip2(0.2);

				noise0 = LFNoise2.kr(0.75);

				noise1 = LFNoise2.kr(0.5, 20, 10);

				synth = ((Pan2.ar(synth[0], (noise0-(LFNoise2.kr(2, 0.1).abs)).clip2(1))*(1-Trig1.ar(Dust.kr(noise1), 1/100))) + (Pan2.ar(synth[1], (noise0+(LFNoise2.kr(2, 0.1).abs)).clip2(1))*(1-Trig1.ar(Dust.kr(noise1), 1/100))));

				synth = Normalizer.ar(synth, 0.5, 0.05);

				env = EnvGen.kr(Env.asr(0, 1, 5), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, (max(LFSaw.kr(envRate, 0, 1, 0.5), Lag.kr(justOn, 0.01)))*synth*vol*env*pauseEnv);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("CrackleSynth", Rect(500, 500, 350, 260));

		this.initControlsAndSynths(6);

		valsArray = Array.newClear(7);

		synths.add(Synth("crackleSynth2_mod", [\outBus, outBus, \vol, 0, \whichSawFreq, 0, \sawFreq, 20, \crackleMult, 20, \crackleAdd, 0.5, \hpfFreq, 40, \envRate, 5, \justOn, 1, \whichOscil, 1], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
		}, 0, true, 'vert'));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("whichOscil", ControlSpec(0,3,'linear'),
			{|v|
				synths[0].set(\whichOscil, v.value);

				valsArray.put(0, v.value);
				//valsArray.postln;

		}, 0, true, \vert));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider.new("ampMod", ControlSpec(0,100,'linear'),
			{|v|
				if(v.value>90, {valsArray.put(2, 1); synths[0].set(\justOn, 1)},{valsArray.put(2, 0); synths[0].set(\envRate, v.value, \justOn, 0)});

				valsArray.put(1, v.value);
		}, 0, true, \vert));
		this.addAssignButton(2,\continuous);

		controls.add(Button()
			.states_([["manual", Color.red, Color.black],["auto", Color.black, Color.green]])
			.action_{arg butt;
				butt.value;
				synths[0].set(\whichSawFreq, butt.value);
		});
		this.addAssignButton(3,\onOff);

		controls.add(QtEZSlider2D.new(ControlSpec(20, 15000, 'exp'), ControlSpec(20, 10000, 'exp'),
			{arg vals;
				synths[0].set(\sawFreq, vals[0], \hpfFreq, vals[1]);

				valsArray.put(3, vals[0]);
				valsArray.put(4, vals[1]);
		}));
		this.addAssignButton(4,\slider2D);

		controls.add(QtEZSlider2D.new(ControlSpec(20, 15000, 'exp'), ControlSpec(0, 1),
			{arg vals;
				vals = vals.value;
				synths[0].set(\crackleMult, vals[0], \crackleAdd, vals[1]);

				valsArray.put(5, vals[0]);
				valsArray.put(6, vals[1]);
		}));
		this.addAssignButton(5,\slider2D);

		win.layout_(
			HLayout(
				VLayout(
					HLayout(
						VLayout(controls[0].layout,assignButtons[0].layout),
						VLayout(controls[1].layout,assignButtons[1].layout),
						VLayout(controls[2].layout,assignButtons[2].layout)
					),
					controls[3], assignButtons[3].layout
				),
				VLayout(controls[4].layout, assignButtons[4].layout, controls[5].layout, assignButtons[5].layout)
			)
		);
	}

	pause {
		synths[0].set(\pauseGate, 0);
	}

	unpause {
		synths[0].set(\pauseGate, 1);
		synths[0].run(true);
	}

}