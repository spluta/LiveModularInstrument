PulseBP_Mod : Module_Mod {
	var largeEnv, ampSynth, ampBus, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, freq, rq, bpVol, delayVar, waitVar, play;

	*initClass {
		StartUp.add {
			SynthDef("largeEnvPBP_mod",{arg outBusNum, attack, decay, gate=1, pauseGate=1;
				var env, out, pauseEnv;

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:0);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
				Out.kr(outBusNum, env*pauseEnv);
			}).writeDefFile;

			SynthDef("amplitudePBP_mod", {|inBus, ampBus|
				var amp;

				amp = Amplitude.kr(In.ar(inBus));

				Out.kr(ampBus, amp);
			}).writeDefFile;

			SynthDef("pulsingBandPass2_mod", {|inBus, outBus, ampBus, volBus, largeEnvBus, freq, rq, delayTime, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, largeEnv, env, pauseEnv;

				vol = In.kr(volBus);

				largeEnv = In.kr(largeEnvBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				amp = In.kr(ampBus);

				decayTime = Latch.kr(amp, 1)*10+5;

				in = In.ar(inBus)*EnvGen.kr(Env.sine(delayTime), 1);

				out = Pan2.ar(CombC.ar(BPF.ar(in, freq, rq), delayTime, delayTime, decayTime), Rand(-1.0, 1.0));

				//Out.ar(outBus, in);
				Out.ar(outBus, env*pauseEnv*vol*largeEnv*out*EnvGen.kr(Env.new([0,1,1,0],[0.01, decayTime, 1]), 1, doneAction:2));
			}).writeDefFile;

			SynthDef("pulsingBandPass4_mod", {|inBus, outBus, ampBus, volBus, largeEnvBus, freq, rq, delayTime, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, largeEnv, env, pauseEnv;

				vol = In.kr(volBus);

				largeEnv = In.kr(largeEnvBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				amp = In.kr(ampBus);

				decayTime = Latch.kr(amp, 1)*10+5;

				in = In.ar(inBus)*EnvGen.kr(Env.sine(delayTime), 1);

				out = PanAz.ar(4, CombC.ar(BPF.ar(in, freq, rq), delayTime, delayTime, decayTime), Rand(-0.75, 0.75));

				//Out.ar(outBus, in);
				Out.ar(outBus, env*pauseEnv*vol*largeEnv*[out[0],out[1],out[3],out[2]]*EnvGen.kr(Env.new([0,1,1,0],[0.01, decayTime, 1]), 1, doneAction:2));
			}).writeDefFile;

			SynthDef("pulsingBandPass8_mod", {|inBus, outBus, ampBus, volBus, largeEnvBus, freq, rq, delayTime, gate = 1, pauseGate = 1|
				var in, out, amp, decayTime, vol, largeEnv, env, pauseEnv;

				vol = In.kr(volBus);

				largeEnv = In.kr(largeEnvBus);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

				amp = In.kr(ampBus);

				decayTime = Latch.kr(amp, 1)*10+5;

				in = In.ar(inBus)*EnvGen.kr(Env.sine(delayTime), 1);

				out = PanAz.ar(8, CombC.ar(BPF.ar(in, freq, rq), delayTime, delayTime, decayTime), Rand(-0.6, 0.6));

				//Out.ar(outBus, in);
				Out.ar(outBus, env*pauseEnv*vol*largeEnv*[out[0],out[1],out[7],out[2],out[6],out[3],out[5],out[4]]*EnvGen.kr(Env.new([0,1,1,0],[0.01, decayTime, 1]), 1, doneAction:2));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("PulseBP",Rect(518, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		ampBus = Bus.control(group.server);
		largeEnvBus = Bus.control(group.server);
		bpVol = Bus.control(group.server);

		bpVol.set(0);

		largeEnv = Synth("largeEnvPBP_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);
		ampSynth = Synth("amplitudePBP_mod", [\inBus, mixerToSynthBus.index, \ampBus, ampBus.index], envGroup);

		play = false;

		localRout = Routine.new({{
			if(play,{
				switch(numChannels,
					2,{
						(3.rand+1).do{Synth("pulsingBandPass2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \ampBus, ampBus.index, \volBus, bpVol.index, \largeEnvBus, largeEnvBus.index, \freq, rrand(200, 15000), \rq, 0.2, \delayTime, rrand(0.05, 0.1), \decayTime, rrand(0.5, 4.9)], synthGroup)};
					},
					4,{
						(3.rand+1).do{Synth("pulsingBandPass4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \ampBus, ampBus.index, \volBus, bpVol.index, \largeEnvBus, largeEnvBus.index, \freq, rrand(200, 15000), \rq, 0.2, \delayTime, rrand(0.05, 0.1), \decayTime, rrand(0.5, 4.9)], synthGroup)};
					},
					8,{
						(3.rand+1).do{Synth("pulsingBandPass8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \ampBus, ampBus.index, \volBus, bpVol.index, \largeEnvBus, largeEnvBus.index, \freq, rrand(200, 15000), \rq, 0.2, \delayTime, rrand(0.05, 0.1), \decayTime, rrand(0.5, 4.9)], synthGroup)};
					}
				)
			});
			(waitVar + (0.1.rand)).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				bpVol.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "wait", ControlSpec(0.1,0.2,'linear'),
			{|v|
				waitVar = v.value;
			}, 0.55, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

		controls.add(Button(win,Rect(80, 130, 60, 50))
			.states_([["Off",Color.black,Color.red],["On",Color.black,Color.green]])
			.action_{|v|
				if(v.value==1,{
					play = true;
					localRout.reset;
					localRout.play;
				},{
					localRout.stop;
					play = false;
				});
			});
		this.addAssignButton(2,\onOff, Rect(80, 180, 60, 50));

		//multichannel button
		numChannels =2;
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
	}

	pause {
		largeEnv.set(\pauseGate, 0);
		localRout.stop;
	}

	unpause {
		largeEnv.set(\pauseGate, 1);
		localRout.reset;
		localRout.play;
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
		ampSynth.free;
	}
}
