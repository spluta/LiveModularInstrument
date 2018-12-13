ScaleShifter_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, filterStart, filterEnd, rqStart, rqEnd, volBus, delayVar, waitVar, numNotes, bigScale, localLength;

	*initClass {
		StartUp.add {

			SynthDef("scaleShifter2_mod", {arg inBusNum, outBus, volBus, largeEnvBusNum, length, gate0=0, gate1=0, gate2=0, gate3=0, ratio0=1, ratio1=1, ratio2=1, ratio3=1, smallLength;
				var in, in2, env, env0, env1, env2, env3, sig, sig0, sig1, sig2, sig3, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd;

				volume = In.kr(volBus);

				largeEnv = In.kr(largeEnvBusNum);
				in = In.ar(inBusNum, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,smallLength], 'linear');

				in2 = EnvGen.kr(env, doneAction: 2)*in*largeEnv;

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				env0 = EnvGen.kr(Env([0,1,1,0], [smallLength/3,smallLength/3,smallLength/3]), gate0);
				env1 = EnvGen.kr(Env([0,1,1,0], [smallLength/3,smallLength/3,smallLength/3]), gate1);
				env2 = EnvGen.kr(Env([0,1,1,0], [smallLength/3,smallLength/3,smallLength/3]), gate2);
				env3 = EnvGen.kr(Env([0,1,1,0], [smallLength/3,smallLength/3,smallLength/3]), gate3);

				sig0 = PitchShift.ar(in2, 0.1, ratio0, 0, 0.1)*env0;
				sig1 = PitchShift.ar(in2, 0.1, ratio1, 0, 0.1)*env1;
				sig2 = PitchShift.ar(in2, 0.1, ratio2, 0, 0.1)*env2;
				sig3 = PitchShift.ar(in2, 0.1, ratio3, 0, 0.1)*env3;

				out = Pan2.ar(sig0+sig1+sig2+sig3,
					Line.kr(xStart, xEnd, length));
				Out.ar(outBus, out*volume);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("ScaleShifter",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		volBus = Bus.control(group.server);

		volBus.set(0);

		largeEnv = Synth("largeEnvFilt_mod", [\outBusNum, largeEnvBus.index, \gate, 1.0], envGroup);

		bigScale = [3/2,5/4,7/4,9/8,11/8,13/8,15/8,17/16,19/16,21/16,23/16,25/16,27/16,29/16,31/16].sort;

		localRout = Routine.new({{
			localLength = rrand(7*length/8, 9*length/8);
			this.makeANote(localLength);
			(localLength/4).wait;
		}.loop});

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,8,'amp'),
			{|v|
				volBus.set(v.value);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "length", ControlSpec(2,10,'linear'),
			{|v|
				length = v.value;
			}, 7, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 130, 60, 100), "notes", ControlSpec(2,12,'linear',1),
			{|v|
				numNotes = v.value;
			}, 2, true));
		this.addAssignButton(2,\continuous, Rect(80, 230, 60, 20));


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


		SystemClock.play(localRout);
	}

	makeANote {arg localLength2;
		var synth, scale, step, changeNext;

		Routine{
			var changeNumStream, smallLength, copySize;

//			if (0.5.coin,{
//				scale = bigScale.copyRange(0, min(numNotes+4, bigScale.size-1).floor.asInteger).copyRange(0, rrand(numNotes-1, numNotes+3).floor.asInteger).sort;
//			},{
//				scale = bigScale.reverse.copyRange(0, min(numNotes+4, bigScale.size-1).floor.asInteger).copyRange(0, rrand(numNotes-1, numNotes+3).floor.asInteger).sort.reverse;
//			});

			scale = (bigScale/2).scramble.copyRange(0, rrand(numNotes-1, numNotes+3).floor.asInteger).sort.reverse;

			smallLength = localLength2/((scale.size+1)/2);

			switch(numChannels,
				2,{
					synth = Synth("scaleShifter2_mod", [\inBusNum, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \largeEnvBusNum, largeEnvBus.index, \length, localLength2, \smallLength, smallLength], synthGroup);
				},
				4,{
//					synth = Synth("scaleShifter4_mod", [\inBusNum, mixerToSynthBus, \outBus, outBus, \volBus, volBus.index, \largeEnvBusNum, largeEnvBus.index, \length, localLength2, \vol, volBus.index, \gate0, 1, \gate1, 0, \gate2, 0, \gate3, 0, \ratio0, scale[0], \ratio1, scale[1] \ratio2, scale[2], \ratio3, scale[3], \smallLength, smallLength], synthGroup);
				},
				8,{
//					synth = Synth("scaleShifter8_mod", [\inBusNum, mixerToSynthBus, \outBus, outBus, \volBus, volBus.index, \largeEnvBusNum, largeEnvBus.index, \length, localLength2, \vol, volBus.index, \gate0, 1, \gate1, 0, \gate2, 0, \gate3, 0, \ratio0, scale[0], \ratio1, scale[1] \ratio2, scale[2], \ratio3, scale[3], \smallLength, smallLength], synthGroup);
				}
			);

			changeNumStream = Pseq(#[0,1,2,3], inf).asStream;

			(scale.size).do{|i|
				changeNext = changeNumStream.next;

				switch(changeNext,
					0, {synth.set(\gate0, 1, \ratio0, scale[i])},
					1, {synth.set(\gate1, 1, \ratio1, scale[i])},
					2, {synth.set(\gate2, 1, \ratio2, scale[i])},
					3, {synth.set(\gate3, 1, \ratio3, scale[i])}
				);
				(smallLength/2).wait;
			};

			synth.set(\gate, 0);

		}.play;

	}

	pause {
		largeEnv.set(\pauseGate, 0);
	}

	unpause {
		largeEnv.set(\pauseGate, 1);
	}

	killMeSpecial {
		largeEnv.set(\gate, 0);
		localRout.stop;
		largeEnvBus.free;
	}
}
