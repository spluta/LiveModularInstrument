HarmonicShifter2_Mod {
	var <>inBus, <>shiftBus, <>shiftByArray, loopLength, <>group, <>delayTime, randomPitch;
	var playID, shiftRout, length, delayTime, windowSize, pitchRatio, pitchDisp, volume;
	var largeEnv, largeEnvBus, largeEnvBusNum, transBus, transBusNum, playRout, resp, bussesOut1, bussesOut2;
	var outBus1, outBus2, outBus3, outBus4;
	var volGroup, recordGroup, playGroup, synthGroup;
	var playBufSynth, writeBufSynth, delaySynth;

	*new {arg inBus, shiftBus, shiftByArray, group, delayTime;
		^super.new.group_(group).inBus_(inBus).shiftBus_(shiftBus).shiftByArray_(shiftByArray).delayTime_(delayTime).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("largeEnvShifter_mod",{arg outBusNum, attack, decay, gate;
				var env, out;

				env = Env.asr(attack,1,decay);
				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2));
			}).writeDefFile;
			SynthDef(\audioDelay_mod, {arg inBus, outBus, delayTime;
				var in;

				in = DelayC.ar(In.ar(inBus), delayTime, delayTime);

				Out.ar(outBus, in);
			}).writeDefFile;

			SynthDef(\shifterX2_mod, {arg inBus, outBus1, outBus2, outBus3, outBus4, length, delayTime, windowSize, pitchRatio, 				pitchDisp, xStart, xEnd, yStart, yEnd, largeEnvBusNum;
				var in, in2, out1, out2, out3, out4, addToSlope, env, bigEnv, largeEnv;

				addToSlope = length/4;
				env = Env.new([0.001,1,1,0.001], [addToSlope+1,length-(2+(2*addToSlope)),1+addToSlope], 'linear');
				bigEnv = Env.new([0.001, 1, 1, 0.001], [0.001, length + addToSlope + delayTime +2, 0.001], 'linear');

				largeEnv = In.kr(largeEnvBusNum, 1);

				in = In.ar(inBus, 1);

				in2 =  in*EnvGen.kr(bigEnv, doneAction: 2)*EnvGen.kr(env, doneAction: 0)*largeEnv;

				# out1, out2, out3, out4 = Pan4.ar(
					PitchShift.ar(DelayL.ar(in2, 0.5, delayTime), windowSize, pitchRatio, pitchDisp),					Line.kr(xStart, xEnd, length+2.1+delayTime),					Line.kr(yStart, yEnd, length+2.1+delayTime)
				);

				Out.ar(outBus1, out1);
				Out.ar(outBus2, out2);
				Out.ar(outBus3, out3);
				Out.ar(outBus4, out4);
			}).writeDefFile;
		}
	}

	init {
		volGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		transBus = Bus.audio(group.server, 1);

		largeEnvBus = Bus.control(group.server, 1);
		largeEnv = Synth("largeEnvShifter_mod", [\outBusNum, largeEnvBus.index, \attack, 10, \decay, 10, \gate, 1.0], volGroup);

		delaySynth = Synth(\audioDelay_mod, [\inBus, inBus, \outBus, transBus, \delayTime, delayTime], volGroup);

		bussesOut1 = Pxrand(#[0,2,4,6], inf).asStream;
		bussesOut2 = Pxrand(#[1,3,5,7], inf).asStream;

		randomPitch = false;

		shiftRout = Routine.new({{
			length = 5.0.rand + 5;
			delayTime = 0.05+0.25.rand;
			windowSize = 0.5+2.0.rand;
			pitchRatio = shiftByArray.choose;
			/*
			if(pitchRatio == 2, {pitchRatio = shiftByArray.choose*[2,4].choose});
			if(randomPitch, {pitchRatio = pitchRatio*(rrand(0.75,1.25))});*/
			pitchDisp = 0.025.rand;
			volume = 0.5.rand+0.5;

			outBus1 = shiftBus.index+bussesOut1.next;
			outBus2 = shiftBus.index+bussesOut2.next;
			outBus3 = shiftBus.index+bussesOut1.next;
			outBus4 = shiftBus.index+bussesOut2.next;


			Synth("shifterX2_mod", [\inBus, transBus, \outBus1, outBus1, \outBus2, outBus2, \outBus3, outBus3, \outBus4, outBus4, \length, length, \delayTime, delayTime, \windowSize, windowSize, \pitchRatio, pitchRatio, \pitchDisp, pitchDisp, \xStart, 1.0.rand2, \xEnd, 1.0.rand2, \yStart, 1.0.rand2, \yEnd, 1.0.rand2, \largeEnvBusNum, largeEnvBus.index], synthGroup);
			(1.5 + (3.5.rand)).wait;
		}.loop});

		SystemClock.play(shiftRout);
	}

	killMe {
		shiftRout.stop;
	}
}

ShifterX2_Mod : Module_Mod {
	var shiftBus, shiftGroup, tapeGroup, mixGroup, topButtons, sideButtons, randomPitch, shiftSlide, shiftByArray, harmonicShifters, bombVol;
	var distortBus, feedBackLooper, shiftButtons, bufferArray, bombVol, mainVol, rout;

	init {
		this.initControlsAndSynths(3);
		this.makeWindow("ShifterX2",Rect(490, 510, 300, 100));

		this.makeMixerToSynthBus;

		shiftGroup = Group.tail(group);
		tapeGroup = Group.tail(group);
		mixGroup = Group.tail(group);

		shiftBus = Bus.audio(group.server, 8);
		distortBus = Bus.audio(group.server, 8);

		mainVol = Bus.control(group.server);

		topButtons = List.new;

		randomPitch = false;
		shiftSlide = false;

		feedBackLooper = FeedBackLooper_Mod(shiftBus, distortBus, tapeGroup);

		shiftByArray = [0.25, 0.5, 1.0, 1.25, 1.5, 1.75, 2, 2.25, 2.5, 2.75, 3, 3.25, 3.5, 3.75, 4];

		controls.add(QtEZSlider.new("fade", ControlSpec(-1,1,'linear'),
			{|v|
				synths[0].set(\fade, v.value)
		},-1, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				mainVol.set(v.value)
		}, 0, true, \horz));
		this.addAssignButton(1,\continuous);

		harmonicShifters = List.new;
		3.do{|i|harmonicShifters.add(HarmonicShifter2_Mod(mixerToSynthBus, shiftBus, shiftByArray, shiftGroup, i*1.5))};


		synths.add(Synth("busToOuts2_mod", [\outBus, outBus, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));

		win.layout_(
			VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout)
			)
		);
		win.layout.spacing = 0;

	}

	killMeSpecial {
		harmonicShifters.do{arg item; item.killMe;};
		if(feedBackLooper!=nil,{feedBackLooper.killMe});
	}
}