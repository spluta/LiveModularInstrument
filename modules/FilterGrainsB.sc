FilterGrainObjectB {

	var <>inBus, <>outBus, <>ratioBus, <>dispersionBus, <>envGroup, <>synthGroup, <>largeEnvBus, <>filterVol, <>swoopLength, <>numChannels, start, end, delayTime, length, filterStart, filterEnd, rqStart, rqEnd, filterVol, delayVar, waitVar, localRout, ratioBus, dispersionBus;

		*new {arg inBus, outBus, ratioBus, dispersionBus, envGroup, synthGroup, largeEnvBus, filterVol, swoopLength, numChannels;
		^super.new.inBus_(inBus).outBus_(outBus).ratioBus_(ratioBus).dispersionBus_(dispersionBus).envGroup_(envGroup).synthGroup_(synthGroup).largeEnvBus_(largeEnvBus).filterVol_(filterVol).swoopLength_(swoopLength).numChannels_(numChannels).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("gestureLines_mod", {arg ratioBus, dispersionBus, start, end, length;
				Out.kr(ratioBus, XLine.kr(start, end, length+(length/4), doneAction:2));

				Out.kr(dispersionBus, Line.kr(Rand(0,1.0), Rand(0,1.0), length+(length/4)));

			}).writeDefFile;


				SynthDef("filterGrainsB2_mod", {arg inBusNum, outBus, largeEnvBusNum, ratioBus, dispersionBus, filterStart, filterEnd, rqStart, rqEnd, delayTime, length, vol;
					var in, in2, env, delayedSignal, buffer, out, largeEnv, bigEnv, volume, xStart, xEnd, ratio, grains, dispersion,  winSize, filt;

					volume = In.kr(vol);

					largeEnv = In.kr(largeEnvBusNum, 1);
					in = In.ar(inBusNum, 1)*largeEnv;

					env = Env.new([0.001,1,1,0.001], [1,length,1]);
					bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01]);

					in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

					xStart = Rand(-1,1);
					xEnd = Rand(-1,1);

					delayedSignal = DelayL.ar(in2, 5, delayTime);

					ratio = In.kr(ratioBus);

					//filt = BPF.ar(delayedSignal, ratio+LFNoise1.kr(0.3,100, 200), Line.kr(rqStart, rqEnd, length+delayTime+2));

					filt = BPF.ar(delayedSignal, Line.kr(filterStart, filterEnd, length+delayTime+2)+LFNoise1.kr(Rand(0.25, 0.35),100, 200), Line.kr(rqStart, rqEnd, length+delayTime+2));


					dispersion = In.kr(dispersionBus);
					winSize = Rand(0.1, 0.2);

					grains = PitchShift.ar(filt, winSize, ratio, dispersion*winSize, 0.2);
					//grains = PitchShift.ar(filt, winSize, 1, 0, winSize);

					out = Pan2.ar(grains,
						Line.kr(xStart, xEnd, length+2+delayTime));
					Out.ar(outBus, out*volume);
				}).writeDefFile;
		}
	}

	makeStartEnd {
		start = rrand(0.25, 0.9);
		end = rrand(1.2, 4);
	}

	init {
		this.makeStartEnd;

		if(0.5.coin,{
			Synth("gestureLines_mod", [\ratioBus, ratioBus.index, \dispersionBus, dispersionBus.index, \start, start, \end, end, \length, swoopLength], envGroup)
		},{
			Synth("gestureLines_mod", [\ratioBus, ratioBus.index, \dispersionBus, dispersionBus.index, \start, end, \end, start, \length, swoopLength], envGroup)
		});

		localRout = Routine.new({{
			delayTime = rrand(0.05, 1.2);
			length = 2.0.rand + 5;

			filterStart = 200+20000.rand;
			filterEnd = 200+20000.rand;

			rqStart = 0.05+0.1.rand;
			rqEnd = 0.05+0.1.rand;

			this.makeSynth;

			(0.1 + (0.35.rand)).wait;
		}.loop});

		SystemClock.play(localRout);

		SystemClock.sched(swoopLength, {
			localRout.stop;
		});
	}

	makeSynth {
		switch(numChannels,
				2,{
					Synth("filterGrainsB2_mod", [\inBusNum, inBus, \outBus, outBus, \largeEnvBusNum, largeEnvBus.index, \ratioBus, ratioBus.index,\dispersionBus, dispersionBus, \filterStart, filterStart, \filterEnd, filterEnd, \rqStart, rqStart, \rqEnd, rqEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				},
				4,{
					Synth("filterGrainsB2_mod", [\inBusNum, inBus, \outBus, outBus.index+[0,2].choose, \largeEnvBusNum, largeEnvBus.index, \ratioBus, ratioBus.index,\dispersionBus, dispersionBus, \filterStart, filterStart, \filterEnd, filterEnd, \rqStart, rqStart, \rqEnd, rqEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				},
				8,{
					Synth("filterGrainsB2_mod", [\inBusNum, inBus, \outBus, outBus.index+[0,2,4,6].choose, \largeEnvBusNum, largeEnvBus.index, \ratioBus, ratioBus.index,\dispersionBus, dispersionBus, \filterStart, filterStart, \filterEnd, filterEnd, \rqStart, rqStart, \rqEnd, rqEnd, \delayTime, delayTime, \length, length, \vol, filterVol.index], synthGroup);
				}
			);
	}

}

FilterGrainsB_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, swoopLength, filterVol, ratioBusses, ratioBusStream, dispersionBusses, dispersionBusStream;


	init {
		this.makeWindow("FilterGrainsB",Rect(318, 645, 150, 270));

		localRout = Routine({{
			FilterGrainObjectB.new(mixerToSynthBus.index, outBus, ratioBusStream.next, dispersionBusStream.next, envGroup, synthGroup, largeEnvBus, filterVol, rrand(swoopLength*0.8, swoopLength*1.2), numChannels);
			(swoopLength*3/4).wait;
		}.loop});

		this.init2;
	}

	init2 {

		ratioBusses = List.newClear(0);
		10.do{ratioBusses.add(Bus.control(group.server))};
		ratioBusStream = Pseq(ratioBusses, inf).asStream;

		dispersionBusses = List.newClear(0);
		10.do{dispersionBusses.add(Bus.control(group.server))};
		dispersionBusStream = Pseq(dispersionBusses, inf).asStream;

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		filterVol = Bus.control(group.server);

		filterVol.set(0);

		largeEnv = Synth("largeEnvFilt_mod", [\outBusNum, largeEnvBus.index, \attack, 4, \decay, 10, \gate, 1.0], envGroup);

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "vol", ControlSpec(0,1,'amp'),
			{|v|
				filterVol.set(v.value*12);
			}, 0, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZKnob.new(win,Rect(80, 10, 60, 100), "length", ControlSpec(1,5,\lin),
			{|v|
				swoopLength = v.value;
			}, 3.0, true));
		this.addAssignButton(1,\continuous, Rect(80, 110, 60, 20));

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

	pause {
		largeEnv.set(\pauseGate, 0);
	}

	unpause {
		largeEnv.set(\pauseGate, 1);
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
		ratioBusses.do{|item| item.free};
		dispersionBusses.do{|item| item.free};
	}
}