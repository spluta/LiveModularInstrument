StraightDelays_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, filterStart, filterEnd, rqStart, rqEnd, xStart, xEnd, filterVol, delayVar, waitVar;

	*initClass {
		StartUp.add {
			SynthDef("largeEnvStraightDels_mod",{arg outBusNum, attack, decay, gate=1, pauseGate=1;
				var env, out, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0.5,1,0), pauseGate, doneAction:0);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
				Out.kr(outBusNum, env*pauseEnv);
			}).writeDefFile;

			SynthDef("straightDelays2_mod", {arg inBusNum, outBus, largeEnvBusNum, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				volume = In.kr(vol);

				largeEnv = In.kr(largeEnvBusNum, 1);
				in = In.ar(inBusNum, 1)*largeEnv;

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = Pan2.ar(delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				Out.ar(outBus, out*volume);
			}).writeDefFile;

			SynthDef("straightDelays4_mod", {arg inBusNum, outBus, largeEnvBusNum, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				xStart = Rand(-0.75,0.75);
				xEnd = Rand(-0.75,0.75);

				volume = In.kr(vol);

				largeEnv = In.kr(largeEnvBusNum, 1);
				in = In.ar(inBusNum, 1)*largeEnv;

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = PanAz.ar(4, delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				Out.ar(outBus, [out[0],out[1],out[3],out[2]]*volume);
			}).writeDefFile;

			SynthDef("straightDelays8_mod", {arg inBusNum, outBus, largeEnvBusNum, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				xStart = Rand(-0.6,0.6);
				xEnd = Rand(-0.6,0.6);

				volume = In.kr(vol);

				largeEnv = In.kr(largeEnvBusNum, 1);
				in = In.ar(inBusNum, 1)*largeEnv;

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = PanAz.ar(8, delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				Out.ar(outBus, [out[0],out[1],out[7],out[2],out[6],out[3],out[5],out[4]]*volume);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("StraightDelays",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		filterVol = Bus.control(group.server);

		filterVol.set(0);

		largeEnv = Synth("largeEnvStraightDels_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		localRout = Task.new({{
			delayTime = delayVar.rand+(delayVar/6);
			length = 2.0.rand + 5;

			switch(numChannels,
				2,{Synth("straightDelays2_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);},
				4,{Synth("straightDelays4_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);},
				8,{Synth("straightDelays8_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);}
			);
			(waitVar + (0.35.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				filterVol.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "delay", ControlSpec(1,5,'linear'),
			{|v|
				delayVar = v.value;
			}, 3.0, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 130, 60, 100), "wait", ControlSpec(0.3,1.0,'linear'),
			{|v|
				waitVar = v.value;
			}, 0.55, true));
		this.addAssignButton(2,\continuous, Rect(80, 230, 60, 20));

		this.addAssignButton(10,\continuous, Rect(10, 250, 130, 20));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 275, 60, 20))
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

		localRout.start;
	}

	pause {
		localRout.pause;
		largeEnv.set(\pauseGate, 0);
		//synthGroup.run(false);
	}

	unpause {
		localRout.resume;
		SystemClock.sched(1.0, {largeEnv.set(\pauseGate, 1)});
		//synthGroup.run(true);
	}

	setSpecialMidi {arg data, dataType, controlsIndex;
		3.do{arg i;
			this.setMidi([data[0],[7,12,10].at(i)], 0, i);
		}
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}
