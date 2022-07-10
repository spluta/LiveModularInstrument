HarmonicDoubler_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, xStart, xEnd, volume, waitVar, pitchRatios, pitchRatio;

	*initClass {
		StartUp.add {
			SynthDef("largeEnvHD_mod",{arg outBusNum, attack, decay, gate=1, pauseGate=1;
				var env, out, pauseEnv;

				env = Env.asr(attack,1,decay);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate);

				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2)*pauseEnv);
			}).writeDefFile;

			SynthDef("harmonicDoubler_mod", {arg inBusNum, outBus, largeEnvBusNum, xStart, xEnd, pitchRatio, length, vol;
					var in, in2, env, delayedSignal, buffer, out, largeEnv, volume;

					volume = In.kr(vol);

					in = In.ar(inBusNum, 1);
					largeEnv = In.kr(largeEnvBusNum, 1);

					env = EnvGen.ar(Env.sine(length, 1), doneAction:2);

					out = PitchShift.ar(in, 0.1, pitchRatio, 0, 0.01);

					out = Pan2.ar(out, Line.kr(xStart, xEnd, length));
					Out.ar(outBus, out*volume*env);
				}).writeDefFile;
		}
	}

	init {
		this.makeWindow("HarmonicDoubler",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		volume = Bus.control(group.server);

		volume.set(0);

		largeEnv = Synth("largeEnvHD_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		//pitchRatios = [11/7, 10/7, 9/7, 8/7, 6/7, 5/7, 4/7];
		pitchRatios = [7/8, 3/4, 5/8, 1/2];

		localRout = Routine.new({{
			length = 3.0.rand + 5;

			xStart = 1.0.rand2;
			xEnd = 1.0.rand2;

			pitchRatio = pitchRatios.choose;

			Synth("harmonicDoubler_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd, \length, length, \pitchRatio, pitchRatio, \vol, volume.index], synthGroup);
			(waitVar + (0.35.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				volume.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(80, 10, 60, 220), "wait", ControlSpec(1.0,3.0,'linear'),
			{|v|
				waitVar = v.value;
			}, 1.5, true, layout:\vert));
		this.addAssignButton(1,\continuous,Rect(80, 230, 60, 20));

		SystemClock.play(localRout);
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}

HarmonicDoubler2_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, xStart, xEnd, volume, waitVar, pitchRatios, pitchRatio, addDelays, outBusNow;

	*initClass {
		StartUp.add {
			SynthDef("largeEnvHD_mod",{arg outBusNum, attack, decay, gate=1, pauseGate=1;
				var env, out, pauseEnv;

				env = Env.asr(attack,1,decay);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate);

				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2)*pauseEnv);
			}).writeDefFile;

			SynthDef("harmonicDoubler_mod", {arg inBusNum, outBus, largeEnvBusNum, xStart, xEnd, pitchRatio, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, volume;

				volume = In.kr(vol);

				in = In.ar(inBusNum, 1);
				largeEnv = In.kr(largeEnvBusNum, 1);

				env = EnvGen.ar(Env.sine(length, 1), doneAction:2);

				out = PitchShift.ar(in, 0.1, pitchRatio, 0, 0.01);

				out = Pan2.ar(out, Line.kr(xStart, xEnd, length));
				Out.ar(outBus, out*volume*env);
			}).writeDefFile;

			SynthDef("harmonicDoubler2_mod", {arg inBusNum, outBus, largeEnvBusNum, xStart, xEnd, pitchRatio, delayTime, decayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, volume, env2;

				volume = In.kr(vol);

				in = In.ar(inBusNum, 1);
				largeEnv = In.kr(largeEnvBusNum, 1);

				env = EnvGen.ar(Env.new([0,1,0,0], [length/2,length/2,decayTime], 'sine'), doneAction:2);

				env2 = EnvGen.ar(Env.new([0,0,1,0],[length/2,delayTime/2,delayTime/2], 'sine'), doneAction:0);

				out = PitchShift.ar(in, 0.1, pitchRatio, 0, 0.01);

				out = (out*env)+CombC.ar(out*env2, delayTime, delayTime, decayTime);

				out = Pan2.ar(out, Line.kr(xStart, xEnd, decayTime));

				Out.ar(outBus, out*volume*env);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("HarmonicDoubler2",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		volume = Bus.control(group.server);

		volume.set(0);

		largeEnv = Synth("largeEnvHD_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		//pitchRatios = [11/7, 10/7, 9/7, 8/7, 6/7, 5/7, 4/7];
		pitchRatios = [7/8, 3/4, 5/8, 1/2];

		localRout = Routine.new({{
			length = 3.0.rand + 5;

			xStart = 1.0.rand2;
			xEnd = 1.0.rand2;

			pitchRatio = pitchRatios.choose;

			switch(numChannels,
				2, {
					outBusNow = outBus.index+0;
				},
				4, {
					outBusNow = outBus.index+[0,2].choose;
				},
				8, {
					outBusNow = outBus.index+[0,2,4,6].choose;
				}
			);

			if(addDelays,{
				Synth("harmonicDoubler2_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBusNow, \largeEnvBusNum, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd, \delayTime, rrand(0.1,0.25), \decayTime, rrand(4,7), \length, length, \pitchRatio, pitchRatio, \vol, volume.index], synthGroup);
			},{
				Synth("harmonicDoubler_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBusNow, \largeEnvBusNum, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd, \length, length, \pitchRatio, pitchRatio, \vol, volume.index], synthGroup);
			});
			(waitVar + (0.35.rand)).wait;
		}.loop});

		controls.add(QtEZSlider.new("vol", ControlSpec(0,8,'amp'),
			{|v|
				volume.set(v.value);
			}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("wait", ControlSpec(1.0,3.0,'linear'),
			{|v|
				waitVar = v.value;
			}, 1.5, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(Button(win,Rect(80, 130, 60, 50))
			.states_([["Off",Color.black,Color.red],["On",Color.black,Color.green]])
			.action_{|v|
				if(v.value==1,{
					addDelays = true;
				},{
					addDelays = false;
				});
			});
		this.addAssignButton(2,\onOff);

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				controls[2], assignButtons[2]
			)
		);

		addDelays = false;

		SystemClock.play(localRout);
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}

HarmDoublerUp_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, xStart, xEnd, volume, waitVar, pitchRatios, pitchRatio, outBusNow;

	init {
		this.makeWindow("HarmDoublerUp",Rect(318, 645, 150, 270));		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		volume = Bus.control(group.server);

		volume.set(0);

		largeEnv = Synth("largeEnvHD_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		pitchRatios = [9/8, 21/16, 11/8, 13/8, 15/8];

		localRout = Routine.new({{
			length = 2.0.rand + 2;

			xStart = 1.0.rand2;
			xEnd = 1.0.rand2;

			pitchRatio = pitchRatios.choose;

			switch(numChannels,
				2, {
					outBusNow = outBus.index+0;
				},
				4, {
					outBusNow = outBus.index+[0,2].choose;
				},
				8, {
					outBusNow = outBus.index+[0,2,4,6].choose;
				}
			);

			Synth("harmonicDoubler_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBusNow, \largeEnvBusNum, largeEnvBus.index, \xStart, xStart, \xEnd, xEnd, \length, length, \pitchRatio, pitchRatio, \vol, volume.index], synthGroup);
			(waitVar + (0.15.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				volume.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(80, 10, 60, 220), "wait", ControlSpec(0.25,0.5,'linear'),
			{|v|
				waitVar = v.value;
			}, 1.5, true, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(80, 230, 60, 20));

			//multichannel button
		controls.add(Button(win,Rect(10, 255, 60, 20))
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
		numChannels = 2;

		SystemClock.play(localRout);
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}
