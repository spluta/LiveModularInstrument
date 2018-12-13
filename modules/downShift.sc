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
			SynthDef("DSverb4_mod",{arg verbBus, verbVolBus, outBus, gate=1, pauseGate=1;
				var in, out, out2, pauseEnv, env, verbVol;

				verbVol = In.kr(verbVolBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				in = In.ar(verbBus,2);
				out = GVerb.ar(in[0], 80, 4.85, 0.41, 0.19, 15, 0, 0);
				out2 = GVerb.ar(in[1], 80, 4.85, 0.41, 0.19, 15, 0, 0);

				Out.ar(outBus, out*env*pauseEnv*verbVolBus);
				Out.ar(outBus+2, out2*env*pauseEnv*verbVolBus);
			}).writeDefFile;
			SynthDef("DSverb8_mod",{arg verbBus, verbVolBus, outBus, gate=1, pauseGate=1;
				var in, out, out2, out3, out4, pauseEnv, env, verbVol;

				verbVol = In.kr(verbVolBus);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				in = In.ar(verbBus,4);
				out = GVerb.ar(in[0], 80, 4.85, 0.41, 0.19, 15, 0, 0);
				out2 = GVerb.ar(in[1], 80, 4.85, 0.41, 0.19, 15, 0, 0);
				out3 = GVerb.ar(in[2], 80, 4.85, 0.41, 0.19, 15, 0, 0);
				out4 = GVerb.ar(in[3], 80, 4.85, 0.41, 0.19, 15, 0, 0);

				Out.ar(outBus, out*env*pauseEnv*verbVolBus);
				Out.ar(outBus+2, out2*env*pauseEnv*verbVolBus);
				Out.ar(outBus+4, out3*env*pauseEnv*verbVolBus);
				Out.ar(outBus+6, out4*env*pauseEnv*verbVolBus);
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

				//shift = [XLine.kr(Rand(1.95,2.05), Rand(0.25, 0.125), length),Line.kr(Rand(4,8), Rand(0.25, 0.125), length)].choose;

				out = PitchShift.ar(in, Rand(0.5, 1), shift, 0, 0.1);

				out = out*pauseEnv*vol;

				Out.ar(outBus, out*env);
				Out.ar(verbBus, Mix.new(out)*verbEnv);

			}).writeDefFile;


			SynthDef("downShift4_mod", {|inBus, outBus, volBus, verbBus, length=1, delayTime, start, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, env, pauseEnv, verbEnv, out1, out2, out3, out4;

				vol = In.kr(volBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.new([0,0,1,1,0], [delayTime, length/4,length/2,length/4]), gate, doneAction: 2);

				verbEnv = EnvGen.kr(Env.new([0,0,2], [delayTime, length]), gate, doneAction: 0);

				in = DelayC.ar(In.ar(inBus,1), delayTime, delayTime*[1,0.95]);

				out = PitchShift.ar(in, Rand(0.5, 1), [XLine.kr(Rand(1.95,2.05), Rand(0.25, 0.125), length),Line.kr(Rand(4,8), Rand(0.25, 0.125), length)].choose, 0, 0.1);

				out = out*pauseEnv*vol;

				#out1, out3 = Pan2.ar(out[0], Line.kr(start, start.neg, length));
				#out2, out4 = Pan2.ar(out[1], Line.kr(start, start.neg, length));

				Out.ar(outBus, [out1, out2, out3, out4]*env);
				Out.ar(verbBus, Pan2.ar(Mix.new(out)*verbEnv, Line.kr(start, start.neg, length)));

			}).writeDefFile;
			SynthDef("downShift8_mod", {|inBus, outBus, volBus, verbBus, length=1, delayTime, start, end, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, env, pauseEnv, verbEnv, out1, out2, out3, out4, out5, out6, out7, out8;

				vol = In.kr(volBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.new([0,0,1,1,0], [delayTime, length/4,length/2,length/4]), gate, doneAction: 2);

				verbEnv = EnvGen.kr(Env.new([0,0,2], [delayTime, length]), gate, doneAction: 0);

				in = DelayC.ar(In.ar(inBus,1), delayTime, delayTime*[1,0.95]);

				out = PitchShift.ar(in, Rand(0.5, 1), [XLine.kr(Rand(1.95,2.05), Rand(0.25, 0.125), length),Line.kr(Rand(4,8), Rand(0.25, 0.125), length)].choose, 0, 0.1);

				out = out*pauseEnv*vol;

				#out1, out3, out5, out7 = PanAz.ar(4, out[0], Line.kr(start, end, length), orientation:0);
				#out2, out4, out6, out8 = PanAz.ar(4, out[1], Line.kr(start, end, length), orientation:0);

				Out.ar(outBus, [out1, out2, out3, out4, out5, out6, out7, out8]*env);
				Out.ar(verbBus, PanAz.ar(4, Mix.new(out)*verbEnv, Line.kr(start, end, length), orientation:0));

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

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
			}, 1, true, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(75, 10, 60, 220), "verbVol", ControlSpec(0,2,'amp'),
			{|v|
				verbVolBus.set(v.value);
			}, 1, true, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(80, 230, 60, 20));

		controls.add(EZRanger.new(win,Rect(140, 10, 60, 220), "length", ControlSpec(3.0,20.0,'linear'),
			{|v|
				length = v.value;
			}, [4,5], true, layout:\vert));
		this.addAssignButton(2,\continuous, Rect(140, 230, 60, 20));

		controls.add(Button(win,Rect(10, 260, 60, 20))
			.states_([["Go",Color.black,Color.green], ["Go",Color.green,Color.black]])
			.action_{|v|
				switch(numChannels,
					2, {Synth("downShift2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \verbBus, verbBus.index, \verbVolBus, verbVolBus, \length, rrand(length[0], length[1]), \start, [1,-1].choose, \delayTime, 0], synthGroup)},
					4, {Synth("downShift4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \verbBus, verbBus.index, \verbVolBus, verbVolBus, \length, rrand(length[0], length[1]), \start, [1,-1].choose, \delayTime, 0], synthGroup)},
					8, {
						[{start=0; end=1.5;},{start=1.5; end=0;}].choose.value;
						Synth("downShift8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \verbBus, verbBus.index, \verbVolBus, verbVolBus, \length, rrand(length[0], length[1]), \start, start, \end, end, \delayTime, 0], synthGroup)
					}
				)
			});
		this.addAssignButton(3,\onOff, Rect(70, 260, 60, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 285, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("DSverb2_mod",[\verbBus, verbBus.index, \verbVol, 0, \outBus, outBus], verbGroup));
					},
					1, {
						numChannels = 4;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("DSverb4_mod",[\verbBus, verbBus.index, \verbVol, 0, \outBus, outBus], verbGroup));
					},
					2, {
						numChannels = 8;
						synths[0].set(\gate, 0);
						synths.put(0, Synth("DSverb8_mod",[\verbBus, verbBus.index, \verbVol, 0, \outBus, outBus], verbGroup));
					}
				)
			};
		);

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
			}, 1, true, layout:\vert));
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