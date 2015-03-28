PulsatingDelays_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, pulseStart, pulseEnd, pulseLow, pulseHigh, xStart, xEnd, filterVol, delayVar, waitVar;

	*initClass {
		StartUp.add {
			SynthDef("largeEnvPulse_mod",{arg outBusNum, attack, decay, gate;
				var env, out;

				env = Env.asr(attack,1,decay);
				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2));
			}).writeDefFile;

			SynthDef("pulsatingDelays2_mod", {arg inBusNum, outBus, largeEnvBusNum, pulseStart, pulseEnd, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				volume = In.kr(vol);

				in = In.ar(inBusNum, 1);
				largeEnv = In.kr(largeEnvBusNum, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = Pan2.ar(delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				out = out*SinOsc.kr(Line.kr(pulseStart, pulseEnd, length+2+delayTime), 1, 0.5);
				Out.ar(outBus, out*volume);
			}).writeDefFile;

			SynthDef("pulsatingDelays4_mod", {arg inBusNum, outBus, largeEnvBusNum, pulseStart, pulseEnd, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				volume = In.kr(vol);

				in = In.ar(inBusNum, 1);
				largeEnv = In.kr(largeEnvBusNum, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				xStart = Rand(-0.75,0.75);
				xEnd = Rand(-0.75,0.75);

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = PanAz.ar(4, delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				out = out*SinOsc.kr(Line.kr(pulseStart, pulseEnd, length+2+delayTime), 1, 0.5);
				Out.ar(outBus, [out[0],out[1],out[3],out[2]]*volume);
			}).writeDefFile;

			SynthDef("pulsatingDelays8_mod", {arg inBusNum, outBus, largeEnvBusNum, pulseStart, pulseEnd, delayTime, length, vol;
				var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				volume = In.kr(vol);

				in = In.ar(inBusNum, 1);
				largeEnv = In.kr(largeEnvBusNum, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				xStart = Rand(-0.6,0.6);
				xEnd = Rand(-0.6,0.6);

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = PanAz.ar(8, delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				out = out*SinOsc.kr(Line.kr(pulseStart, pulseEnd, length+2+delayTime), 1, 0.5);
				Out.ar(outBus, [out[0],out[1],out[7],out[2],out[6],out[3],out[5],out[4]]*volume);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("PulsatingDelays",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		filterVol = Bus.control(group.server);

		filterVol.set(0);

		largeEnv = Synth("largeEnvPulse_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		localRout = Routine.new({{
			delayTime = rrand(2.0,4.0);
			length = 2.0.rand + 5;

			pulseStart = rrand(pulseLow, pulseHigh);
			pulseEnd = rrand(pulseLow, pulseHigh);

			switch(numChannels,
				2,{
					Synth("pulsatingDelays2_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \pulseStart, pulseStart, \pulseEnd, pulseEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				},
				4,{
					Synth("pulsatingDelays4_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \pulseStart, pulseStart, \pulseEnd, pulseEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				},
				8,{
					Synth("pulsatingDelays8_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \pulseStart, pulseStart, \pulseEnd, pulseEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				}
			);
			(1.25 + (0.35.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,2,'amp'),
			{|v|
				filterVol.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "pulseLow", ControlSpec(2,20,'linear'),
			{|v|
				pulseLow = v.value;
			}, 2, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 130, 60, 100), "pulseHigh", ControlSpec(2,20,'linear'),
			{|v|
				pulseHigh = v.value;
			}, 20, true));
		this.addAssignButton(2,\continuous, Rect(80, 230, 60, 20));

		//multichannel button
		numChannels =2;
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

		SystemClock.play(localRout);
	}

	pause {
		localRout.stop;
	}

	unpause {
		localRout.reset;
		localRout.play;
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
		envGroup.free;
		synthGroup.free;
	}
}
