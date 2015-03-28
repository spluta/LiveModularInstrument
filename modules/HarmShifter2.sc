HarmonicShifter2_Mod {
	var <>inBus, <>shiftBus, <>audioGateBus, <>shiftByArray, <>shiftWeightArray, loopLength, <>group, <>delayTime, randomPitch;
	var playID, shiftRout, length, delayTime, windowSize, pitchRatio, pitchDisp, volume;
	var largeEnv, largeEnvBus, largeEnvBusNum, transBus, transBusNum, playRout, resp, bussesOut1, bussesOut2;
	var outBus1, outBus2, outBus3, outBus4;
	var volGroup, recordGroup, playGroup, synthGroup;
	var playBufSynth, writeBufSynth, delaySynth;

	*new {arg inBus, shiftBus, audioGateBus, shiftByArray, shiftWeightArray, group, delayTime;
		^super.new.group_(group).inBus_(inBus).shiftBus_(shiftBus).audioGateBus_(audioGateBus).shiftByArray_(shiftByArray).shiftWeightArray_(shiftWeightArray).delayTime_(delayTime).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("largeEnvShifter_mod",{arg outBusNum, attack, decay, gate;
				var env, out;

				env = Env.asr(attack,1,decay);
				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2));
			}).writeDefFile;
			SynthDef(\audioDelay_mod, {arg inBus, outBus, delayTime, audioGateBus;
				var in;
					//
				in = DelayC.ar(In.ar(inBus)*Lag.kr(In.kr(audioGateBus), 0.01), delayTime, delayTime);

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

		delaySynth = Synth(\audioDelay_mod, [\inBus, inBus, \outBus, transBus, \delayTime, delayTime, \audioGateBus, audioGateBus], volGroup);

		bussesOut1 = Pxrand(#[0,2,4,6], inf).asStream;
		bussesOut2 = Pxrand(#[1,3,5,7], inf).asStream;

		randomPitch = false;

		shiftRout = Routine.new({{
			length = 5.0.rand + 5;
			delayTime = 0.05+0.25.rand;
			windowSize = 0.5+2.0.rand;
			pitchRatio = shiftByArray.wchoose(shiftWeightArray.normalizeSum);
			if(pitchRatio == 2, {pitchRatio = shiftByArray.choose*[2,4].choose});
			if(randomPitch, {pitchRatio = pitchRatio*(rrand(0.75,1.25))});
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
	var shiftBus, shiftGroup, tapeGroup, mixGroup, topButtons, sideButtons, randomPitch, shiftSlide, shiftWeightArray, shiftByArray, harmonicShifters, bombVol;
	var distortBus, feedBackLooper, shiftButtons, bufferArray, bombVol, mainVol, rout, audioGateBus;

	init {
		this.initControlsAndSynths(5);
		this.makeWindow("ShifterX2",Rect(490, 510, 170, 490));

		this.makeMixerToSynthBus;

		audioGateBus = Bus.control(group.server);
		audioGateBus.set(0);

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

		topButtons.add(Button.new(win,Rect(10, 10, 70, 20))
			.states_([ [ "randOff", Color.red, Color.black ], [ "randOn", Color.black, Color.green ] ])
			.action_{|v|
				if(v.value==1,{randomPitch = true},{randomPitch = false});
				harmonicShifters.do{arg item; item.randomPitch = randomPitch};
			});
		topButtons.add(Button.new(win,Rect(80, 10, 70, 20))
			.states_([ [ "slideOff", Color.red, Color.black ], [ "slideOn", Color.black, Color.green ] ])
			.action_{|v| if(v.value==1,{shiftSlide = true},{shiftSlide = false})});
		topButtons.add(Button.new(win,Rect(10, 30, 70, 20))
			.states_([ [ "noiseOff", Color.red, Color.black ], [ "noiseOn", Color.black, Color.green ] ])
			.action_{|v|

				v.enabled_(false);
				topButtons[3].enabled_(true);
			});
		topButtons.add(Button.new(win,Rect(80, 30, 70, 20))
			.states_([ [ "slideOff", Color.red, Color.black ], [ "slideOn", Color.black, Color.green ] ])
			.action_{|v|
				if(v.value==1,{feedBackLooper.setDeviation(true)},{feedBackLooper.setDeviation(false)});
			});
		topButtons[3].enabled_(true);
		topButtons[2].enabled_(false);

		shiftButtons = List.new;

		shiftButtons.add(Button.new(win,Rect(10, 60, 50, 20))
			.states_([ [ "1-4", Color.red, Color.black ], [ "1-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(0, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 90, 50, 20))
			.states_([ [ "1-2", Color.red, Color.black ], [ "1-2", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(1, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 120, 50, 20))
			.states_([ [ "1-1", Color.red, Color.black ], [ "1-1", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(2, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 150, 50, 20))
			.states_([ [ "5-4", Color.red, Color.black ], [ "5-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(3, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 180, 50, 20))
			.states_([ [ "3-2", Color.red, Color.black ], [ "3-2", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(4, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 210, 50, 20))
			.states_([ [ "7-4", Color.red, Color.black ], [ "7-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(5, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new(win,Rect(10, 240, 50, 20))
			.states_([ [ "2to4", Color.red, Color.black ], [ "2to4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(6, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});

		shiftWeightArray = [0,0,0,0,0,0,0];
		shiftByArray = [0.25, 0.5, 1.0, 1.25, 1.5, 1.75, 2];

		controls.add(EZKnob.new(win,Rect(90, 70, 60, 100), "fade", ControlSpec(-1,1,'linear'),
			{|v|
				synths[0].set(\fade, v.value)
			},-1, true));
		this.addAssignButton(0,\continuous, Rect(90, 170, 60, 20));

		controls.add(EZSlider.new(win,Rect(90, 190, 60, 160), "vol", ControlSpec(0,1,'amp'),
			{|v|
				mainVol.set(v.value)
			}, 0, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(90, 350, 60, 20));

		harmonicShifters = List.new;
		3.do{|i|harmonicShifters.add(HarmonicShifter2_Mod(mixerToSynthBus, shiftBus, audioGateBus, shiftByArray, shiftWeightArray, shiftGroup, i*1.5))};

		controls.add(Button(win, Rect(10, 370, 160, 20))
			.states_([ [ "Off", Color.green, Color.black ], [ "On", Color.black, Color.green ]])
			.action_({arg but;
				audioGateBus.set(but.value);
			}));
		this.addAssignButton(2,\onOff,Rect(10, 390, 160, 20));

		//multichannel button
		controls.add(Button(win,Rect(10, 315, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				synths[0].set(\gate, 0);
				switch(butt.value,
					0, {
						numChannels = 2;
						synths.put(0, Synth("busToOuts2_mod", [\outBus, outBus, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));
					},
					1, {
						numChannels = 4;
						synths.put(0, Synth("busToOuts4_mod", [\outBus, outBus, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));
					},
					2, {
						numChannels = 8;
						synths.put(0, Synth("busToOuts8_mod", [\outBus, outBus, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));
					}
				)
			};
		);

		synths.add(Synth("busToOuts2_mod", [\outBus, outBus.postln, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));
	}

	killMeSpecial {
		harmonicShifters.do{arg item; item.killMe;};
		if(feedBackLooper!=nil,{feedBackLooper.killMe});
	}
}