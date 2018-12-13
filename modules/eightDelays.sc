EightDelays2_Mod : Module_Mod {
	var delayGroup, synthGroup, maxDelay, goButton, delayBusses, counter, rout, length, volBus;

	*initClass {
		StartUp.add {
			SynthDef("eightDelays2Line_mod", {arg start, end, time, length, delayBus, pauseGate=1;
				ReplaceOut.kr(delayBus, Line.kr(start, end, time, doneAction: 2));
				EnvGen.kr(Env.new([0,1],[length]), doneAction:2);
				EnvGen.kr(Env.asr(0.001, 1, 0.001), pauseGate);
			}).writeDefFile;

			SynthDef("eightDelays2L2_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(1/8)), -1);
				out1 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(2/8)), -0.75);
				out2 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(3/8)), -0.5);
				out3 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(4/8)), -0.25);
				out4 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(5/8)), 0.25);
				out5 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(6/8)), 0.5);
				out6 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(7/8)), 0.75);
				out7 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(8/8)), 1);

				Out.ar(outBus, (out0+out1+out2+out3+out4+out5+out6+out7)*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("eightDelays2R2_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(1/8)), 1);
				out1 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(2/8)), 0.75);
				out2 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(3/8)), 0.5);
				out3 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(4/8)), 0.25);
				out4 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(5/8)), -0.25);
				out5 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(6/8)), -0.5);
				out6 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(7/8)), -0.75);
				out7 = Pan2.ar(DelayC.ar(in, maxDelay, delayIn*(8/8)), -1);

				Out.ar(outBus, (out0+out1+out2+out3+out4+out5+out6+out7)*env*vol*pauseEnv);
			}).writeDefFile;

			//4 channel

			SynthDef("eightDelays2L4_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(1/8)), -1);
				out1 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(2/8)), -0.75);
				out2 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(3/8)), -0.5);
				out3 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(4/8)), -0.25);
				out4 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(5/8)), 0.25);
				out5 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(6/8)), 0.5);
				out6 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(7/8)), 0.75);
				out7 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(8/8)), 1);

				Out.ar(outBus, (out0+out1+out2+out3+out4+out5+out6+out7)*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("eightDelays2R4_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(1/8)), 1);
				out1 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(2/8)), 0.75);
				out2 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(3/8)), 0.5);
				out3 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(4/8)), 0.25);
				out4 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(5/8)), -0.25);
				out5 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(6/8)), -0.5);
				out6 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(7/8)), -0.75);
				out7 = PanAz.ar(4, DelayC.ar(in, maxDelay, delayIn*(8/8)), -1);

				#out1, out2, out3, out4 = out0+out1+out2+out3+out4+out5+out6+out7;

				Out.ar(outBus, [out1, out2, out4, out3]*env*vol*pauseEnv);
			}).writeDefFile;

			//8 channel

			SynthDef("eightDelays2L8_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = DelayC.ar(in, maxDelay, delayIn*(1/8));
				out1 = DelayC.ar(in, maxDelay, delayIn*(2/8));
				out2 = DelayC.ar(in, maxDelay, delayIn*(3/8));
				out3 = DelayC.ar(in, maxDelay, delayIn*(4/8));
				out4 = DelayC.ar(in, maxDelay, delayIn*(5/8));
				out5 = DelayC.ar(in, maxDelay, delayIn*(6/8));
				out6 = DelayC.ar(in, maxDelay, delayIn*(7/8));
				out7 = DelayC.ar(in, maxDelay, delayIn*(8/8));


				Out.ar(outBus, [out3,out4,out2,out5,out1,out6,out0,out7]*env*vol*pauseEnv);
			}).writeDefFile;

			SynthDef("eightDelays2R8_mod", {arg inBus, outBus, delayBus, maxDelay, volBus, length, pauseGate = 1;
				var in, out, env, phasor, delayIn, out0, out1, out2, out3, out4, out5, out6, out7, pauseEnv, vol;

				vol = In.kr(volBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0,1,1,0],[length/4, length/4, length/2]), 1, doneAction: 2);
				in = In.ar(inBus);

				delayIn = In.kr(delayBus);

				out0 = DelayC.ar(in, maxDelay, delayIn*(1/8));
				out1 = DelayC.ar(in, maxDelay, delayIn*(2/8));
				out2 = DelayC.ar(in, maxDelay, delayIn*(3/8));
				out3 = DelayC.ar(in, maxDelay, delayIn*(4/8));
				out4 = DelayC.ar(in, maxDelay, delayIn*(5/8));
				out5 = DelayC.ar(in, maxDelay, delayIn*(6/8));
				out6 = DelayC.ar(in, maxDelay, delayIn*(7/8));
				out7 = DelayC.ar(in, maxDelay, delayIn*(8/8));

				Out.ar(outBus, [out4,out3,out5,out2,out6,out1,out7,out0]*env*vol*pauseEnv);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("EightDelays2", Rect(500,100,400,40));

		modName = "EightDelays2";
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		delayBusses = Bus.control(group.server, 4);
		delayBusses.do{|item| item.set(0)};

		volBus = Bus.control(group.server);

		delayGroup = Group.head(group);
		synthGroup = Group.tail(group);

		maxDelay = 10;

		counter = 0;

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		//multichannel button
		numChannels = 2;
		controls.add(Button()
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
			HLayout(
				controls[0].layout, assignButtons[0].layout, controls[1]
			)
		);
		win.layout.spacing = 0;

		rout = Routine({{
			length = rrand(2.0, 4.0);
			switch(numChannels,
				2,{
					if(counter.even,{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2L2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);
					},{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2R2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);

					})
				},
				4,{
					if(counter.even,{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2L4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);
					},{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2R4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);

					})
				},
				8,{
					if(counter.even,{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2L8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);
					},{
						Synth("eightDelays2Line_mod", [\start, 0, \end, maxDelay, \time, 30, \length, length, \delayBus, delayBusses.index+counter], delayGroup);
						Synth("eightDelays2R8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \delayBus, delayBusses.index+counter, \maxDelay, maxDelay, \length, length, \volBus, volBus.index], synthGroup);

					})
				}
			);
			counter = counter+1;
			if(counter>3,{counter=0});
			(length/2).wait;
		}.loop});

		SystemClock.play(rout);
	}

	killMeSpecial {
		rout.stop;
		delayBusses.free;
		delayGroup.free;
		synthGroup.free;
	}
}

Melter_Mod : Module_Mod {
	var rout, volBus, length, ratio0a, ratio0b, ratio1a, ratio1b, currentShift, ratio0, ratio1;

	*initClass {
		StartUp.add {
			SynthDef("melter2_mod", {arg inBus, outBus, delayStart, delayEnd, ratio0, ratio1, length, volBus, gate = 1, pauseGate = 1;
				var in, out0, out1, env, phasor, delayTime, pauseEnv, vol, ratio, pan;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0, 1, 1, 0, 0],[length/8, 3*length/4, length/8, 1]), 1, doneAction: 2);

				in = In.ar(inBus)*env;

				vol = In.kr(volBus);

				ratio = Line.kr(ratio0, ratio1, length);

				out0 = PitchShift.ar(in, Rand(0.05,0.025), ratio, 0, 0.01);
				out1 = DelayC.ar(PitchShift.ar(in, Rand(0.025,0.05), ratio, 0, 0.01), 0.01, Rand(0.001, 0.01));

				pan = Rand(-0.9,0.9);

				Out.ar(outBus, Pan2.ar(out0*vol*pauseEnv, pan)+Pan2.ar(out1*vol*pauseEnv, pan+Rand(-0.07, 0.07)));
			//	Out.ar(outBus, Pan2.ar(out0*vol*pauseEnv, pan));
			}).writeDefFile;

			SynthDef("melter4_mod", {arg inBus, outBus, delayStart, delayEnd, ratio0, ratio1, length, volBus, gate = 1, pauseGate = 1;
				var in, out0, out1, out2, out3, out4, env, phasor, delayTime, pauseEnv, vol, ratio, pan;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0, 1, 1, 0, 0],[length/8, 3*length/4, length/8, 1]), 1, doneAction: 2);

				in = In.ar(inBus)*env;

				vol = In.kr(volBus);

				ratio = Line.kr(ratio0, ratio1, length);

				out0 = PitchShift.ar(in, Rand(0.05,0.025), ratio, 0, 0.01);
				out1 = DelayC.ar(PitchShift.ar(in, Rand(0.025,0.05), ratio, 0, 0.01), 0.01, Rand(0.001, 0.01));

				pan = Rand(-0.9,0.9);

				#out1, out2, out3, out4 = PanAz.ar(4, out0*vol*pauseEnv, pan)+PanAz.ar(4, out1*vol*pauseEnv, pan.neg);

				Out.ar(outBus, [out1, out2, out4, out3]);
			}).writeDefFile;

			SynthDef("melter8_mod", {arg inBus, outBus, delayStart, delayEnd, ratio0, ratio1, length, volBus, gate = 1, pauseGate = 1;
				var in, out0, out1, out2, out3, out4, out5, out6, out7, out8, env, phasor, delayTime, pauseEnv, vol, ratio, pan;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.new([0, 1, 1, 0, 0],[length/8, 3*length/4, length/8, 1]), 1, doneAction: 2);

				in = In.ar(inBus)*env;

				vol = In.kr(volBus);

				ratio = Line.kr(ratio0, ratio1, length);

				out0 = PitchShift.ar(in, Rand(0.05,0.025), ratio, 0, 0.01);
				out1 = DelayC.ar(PitchShift.ar(in, Rand(0.025,0.05), ratio, 0, 0.01), 0.01, Rand(0.001, 0.01));

				pan = Rand(-0.9,0.9);

				#out1, out2, out3, out4, out5, out6, out7, out8 = PanAz.ar(8, out0*vol*pauseEnv, pan)+PanAz.ar(8, out1*vol*pauseEnv, pan.neg);

				Out.ar(outBus, [out1, out2, out8, out3, out7, out4, out6, out5]);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Melter", Rect(600, 549, 330, 94));

		modName = "Melter";
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);

		ratio0a = Env([3, 3, 0.75], [1,1], 'welch').asSignal(128).asArray;
		ratio0b = Env([2, 0.5], [1], 'welch').asSignal(128).asArray;
		ratio1a = Env([2, 0.5], [1], 'welch').asSignal(128).asArray;
		ratio1b = Env([1.5, 0.25, 0.25], [1,1], 'welch').asSignal(128).asArray;

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		currentShift = 64;

		controls.add(QtEZSlider.new("range", ControlSpec(127,0),
			{|v|
				currentShift = v.value.asInteger;
			}, 64, true, \horz));
		this.addAssignButton(1,\continuous);

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(0, 225, 60, 20))
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

		rout = Routine({{
			length = rrand(0.25,0.5);
			ratio0 = rrand(ratio0a[currentShift], ratio0b[currentShift]);
			ratio1 = rrand(ratio1a[currentShift], ratio1b[currentShift]);
			switch(numChannels,
				2,{
					Synth("melter2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \length, length, \ratio0, ratio0, \ratio1, ratio1, \volBus, volBus.index], group);
				},
				4,{
					Synth("melter4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \length, length, \ratio0, ratio0, \ratio1, ratio1, \volBus, volBus.index], group);
				},
				8,{
					Synth("melter8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \length, length, \ratio0, ratio0, \ratio1, ratio1, \volBus, volBus.index], group);
				}
			);
			rrand(0.05,0.16).wait;
		}.loop});
		SystemClock.play(rout);

		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout),
				HLayout(controls[2], nil)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	killMeSpecial {
		rout.stop;
	}
}



LongDelay_Mod : Module_Mod {
	var delayGroup, synthGroup, maxDelay, goButton, volBus, delBus, decBus, panBus;

	*initClass {
		StartUp.add {
			SynthDef("longDelay2_mod", {arg inBus, outBus, volBus, delBus, decBus, panBus, gate = 1, pauseGate = 1;
				var in, out, env, phasor, out0, pauseEnv, delayTime=4, decayTime=20, vol=0, pan = 0;

				delayTime = In.kr(delBus);
				decayTime = In.kr(decBus);
				vol = In.kr(volBus);
				pan = In.kr(panBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1), gate, doneAction: 2);

				in = In.ar(inBus)*vol;
				out0 = Pan2.ar(AllpassC.ar(in, 17, delayTime, decayTime), pan);

				Out.ar(outBus, (out0)*env*pauseEnv);
			}).writeDefFile;

			SynthDef("longDelay4_mod", {arg inBus, outBus, volBus, delBus, decBus, panBus, gate = 1, pauseGate = 1;
				var in, out, env, phasor, out0, pauseEnv, delayTime=4, decayTime=20, vol=0, pan = 0;

				delayTime = In.kr(delBus);
				decayTime = In.kr(decBus);
				vol = In.kr(volBus);
				pan = In.kr(panBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1), gate, doneAction: 2);

				in = In.ar(inBus)*vol;
				out0 = PanAz.ar(4, AllpassC.ar(in, 17, delayTime, decayTime), pan);

				Out.ar(outBus, [out0[0], out0[1], out0[3], out0[2]]*env*pauseEnv);
			}).writeDefFile;

			SynthDef("longDelay8_mod", {arg inBus, outBus, volBus, delBus, decBus, panBus, gate = 1, pauseGate = 1;
				var in, out, env, phasor, out0, pauseEnv, delayTime=4, decayTime=20, vol=0, pan = 0;

				delayTime = In.kr(delBus);
				decayTime = In.kr(decBus);
				vol = In.kr(volBus);
				pan = In.kr(panBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.ar(Env.asr(0.1, 1, 0.1), gate, doneAction: 2);

				in = In.ar(inBus)*vol;
				out0 = PanAz.ar(8, AllpassC.ar(in, 17, delayTime, decayTime), pan);

				Out.ar(outBus, [out0[0], out0[1], out0[7], out0[2], out0[6], out0[3], out0[5], out0[4]]*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LongDelay", Rect(843, 708, 311, 97));

		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		delBus = Bus.control(group.server);
		decBus = Bus.control(group.server);
		panBus = Bus.control(group.server);

		synths.add(Synth("longDelay2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 0, \volBus, volBus, \delBus, delBus, \decBus, decBus, \panBus, panBus], group));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("del", ControlSpec(0,17),
			{|v|
				delBus.set(v.value);
			}, 4, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider.new("decay", ControlSpec(0,100),
			{|v|
				decBus.set(v.value);
			}, 20, true, \horz));
		this.addAssignButton(2,\continuous);

		controls.add(QtEZSlider.new("pan", ControlSpec(-1,1),
			{|v|
				panBus.set(v.value)
			}, 0, true, \horz));
		this.addAssignButton(3, \continuous);

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 185, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("longDelay2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 0, \volBus, volBus, \delBus, delBus, \decBus, decBus, \panBus, panBus], group));
					},
					1, {
						numChannels = 4;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("longDelay4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 0, \volBus, volBus, \delBus, delBus, \decBus, decBus, \panBus, panBus], group));
					},
					2, {
						numChannels = 8;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("longDelay8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \vol, 0, \volBus, volBus, \delBus, delBus, \decBus, decBus, \panBus, panBus], group));
					}
				)
			};
		);

		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout),
				HLayout(controls[2].layout, assignButtons[2].layout),
				HLayout(controls[3].layout, assignButtons[3].layout),
				HLayout(controls[4], nil),
			)
		);

		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;
	}
}
