HarmonicShifter_Mod {
	var buffer, shiftBus, shiftByArray, <>shiftWeightArray, loopLength, inBus, group, <>randomPitch;
	var playID, shiftRout, length, delayTime, windowSize, pitchRatio, pitchDisp, volume;
	var largeEnv, largeEnvBus, largeEnvBusNum, transBus, transBusNum, playRout, resp, bussesOut1, bussesOut2;
	var outBus1, outBus2, outBus3, outBus4;
	var volGroup, recordGroup, playGroup, synthGroup;
	var playBufSynth, writeBufSynth;

	*new {arg buffer, shiftBus, shiftByArray, shiftWeightArray, loopLength, inBus, group;
		^super.newCopyArgs(buffer, shiftBus, shiftByArray, shiftWeightArray, loopLength, inBus, group).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("largeEnvShifter_mod",{arg outBusNum, attack, decay, gate;
				var env, out;

				env = Env.asr(attack,1,decay);
				Out.kr(outBusNum, EnvGen.kr(env, gate, doneAction: 2));
			}).writeDefFile;
			SynthDef(\playBufShifter_mod, {arg bufNum, outBusNum, length, gate = 1.0;
				var out, env;

				env = Env.new([0.001, 1, 1, 0.001], [3, length-6.1, 3], 'sine');
				out = EnvGen.kr(env, gate, doneAction: 2)*PlayBuf.ar(1, bufNum);
				Out.ar(outBusNum, out);
			}).writeDefFile;
			SynthDef(\writeBufShifter_mod, {arg inBusNum=0, bufNum, rate=1, length;
				var in, out, env;
				in = In.ar(inBusNum);
				env = Env.new([0,1,1,0],[0.1, length-0.2, 0.1], 'linear');
				out =  BufWr.ar(EnvGen.ar(env,1.0,doneAction: 2)*in, bufNum, Phasor.ar(0, BufRateScale.kr(bufNum) * rate, 0, BufFrames.kr(bufNum)), 0);
			}).writeDefFile;
			SynthDef(\shifterX_mod, {arg inBusNum, outBus1, outBus2, outBus3, outBus4, length, delayTime, windowSize, pitchRatio, 				pitchDisp, xStart, xEnd, yStart, yEnd, largeEnvBusNum;
				var in, in2, out1, out2, out3, out4, addToSlope, env, bigEnv, largeEnv;

				addToSlope = length/4;
				env = Env.new([0.001,1,1,0.001], [addToSlope+1,length-(2+(2*addToSlope)),1+addToSlope], 'linear');
				bigEnv = Env.new([0.001, 1, 1, 0.001], [0.001, length + addToSlope + delayTime +2, 0.001], 'linear');

				largeEnv = In.kr(largeEnvBusNum, 1);

				in = In.ar(inBusNum, 1);
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
		recordGroup = Group.tail(group);
		playGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server, 1);
		largeEnv = Synth("largeEnvShifter_mod", [\outBusNum, largeEnvBus.index, \attack, 10, \decay, 10, \gate, 1.0], volGroup);

		transBus = Bus.audio(group.server, 1);

		writeBufSynth = Synth("writeBufShifter_mod", [\inBusNum, inBus, \bufNum, buffer.bufnum, \length, loopLength], recordGroup);

		playRout = Routine.new({{
			playBufSynth = Synth("playBufShifter_mod", [\bufNum, buffer.bufnum, \outBusNum, transBus.index, \length, loopLength, \gate, 1.0], playGroup);
			loopLength.wait;
		}.loop});

		SystemClock.play(playRout);

		bussesOut1 = Pxrand(#[0,2,4,6], inf).asStream;
		bussesOut2 = Pxrand(#[1,3,5,7], inf).asStream;

		randomPitch = false;
		shiftRout = Routine.new({{
			length = 5.0.rand + 5;
			delayTime = 0.05+0.25.rand;
			windowSize = 0.5+2.0.rand;
			if(shiftWeightArray.sum>0,{
				pitchRatio = shiftByArray.wchoose(shiftWeightArray.normalizeSum);
			},{
				pitchRatio = shiftByArray.choose
			});
			if(pitchRatio == 2, {pitchRatio = shiftByArray.choose*[2,4].choose});
			if(randomPitch, {pitchRatio = pitchRatio*(rrand(0.75,1.25))});
			pitchDisp = 0.025.rand;
			volume = 0.5.rand+0.5;

			outBus1 = shiftBus.index+bussesOut1.next;
			outBus2 = shiftBus.index+bussesOut2.next;
			outBus3 = shiftBus.index+bussesOut1.next;
			outBus4 = shiftBus.index+bussesOut2.next;


			Synth("shifterX_mod", [\inBusNum, transBus.index, \outBus1, outBus1, \outBus2, outBus2, \outBus3, outBus3, \outBus4, outBus4, \length, length, \delayTime, delayTime, \windowSize, windowSize, \pitchRatio, pitchRatio, \pitchDisp, pitchDisp, \xStart, 1.0.rand2, \xEnd, 1.0.rand2, \yStart, 1.0.rand2, \yEnd, 1.0.rand2, \largeEnvBusNum, largeEnvBus.index], synthGroup);
			(1.5 + (3.5.rand)).wait;
		}.loop});

		SystemClock.play(shiftRout);
	}

	killMe {
		shiftRout.stop;
		SystemClock.sched(15,
		{
			playRout.stop;
			playBufSynth.set(\gate, 0);
			largeEnvBus.free;
			nil
		});
	}
}

FeedBackLooper_Mod {
	var shiftBus, distortBus, group;
	var busOut, busOutStream, playRout, waitLine1, waitLine2, durLine1, durLine2, startLine, endLine, lineDeviation, grainyMouse, resp, resp2;

	*new {arg shiftBus, distortBus, group;
		^super.newCopyArgs(shiftBus, distortBus, group).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("tapeFeedback_mod", {arg inBus, outBus, length, startLine, endLine;
				var local, in, amp, initEnv, envLength, delayTime, env, vol;

				in = In.ar(inBus);

				//Out.ar(0, in);

				in = Compander.ar(in, in, thresh: 0.5, slopeBelow: 1, slopeAbove: 0.5, clampTime: 0.01,relaxTime: 0.01);

				amp = Amplitude.kr(in);
				//in = in * (amp > 0.02); // noise gate

				local = LocalIn.ar(1);

				local = DelayN.ar(local, 0.5, Line.kr(startLine, endLine, length));

				local = LeakDC.ar(local);
				local = ((local + in) * 1.25).softclip;

				LocalOut.ar(local);

				env = EnvGen.kr(Env.sine(length), doneAction:2);

				Out.ar(outBus, local * 0.1 * env);
			}).writeDefFile;
		}
	}

	init {

		lineDeviation = 0;

		busOutStream = Pxrand(#[0,1,2,3,4,5,6,7], inf).asStream;

		playRout = Routine.new({{
			startLine = rrand(0.15,0.5);
			endLine = startLine+(([(startLine-0.15).rand.neg, (startLine-0.5).neg.rand].choose)*lineDeviation.rand.value);

			Synth("tapeFeedback_mod", [\inBus, shiftBus.index+(8.rand), \outBus, distortBus.index+busOutStream.next, \length, rrand(6, 12), \startLine, startLine, \endLine, endLine], group);
			rrand(0.3, 2).wait;
		}.loop});

		SystemClock.play(playRout);
	}

	setDeviation {arg bool;
		if (bool,{
			lineDeviation = 0.75;
		},{
			lineDeviation = 0;
		})
	}

	killMe {
		playRout.stop;
	}
}

ShifterFeedback_Mod : Module_Mod {
	var shiftBus, shiftGroup, tapeGroup, mixGroup, topButtons, sideButtons, randomPitch, shiftSlide, shiftWeightArray, shiftByArray, harmonicShifters, bombVol;
	var distortBus, feedBackLooper, shiftButtons, bufferArray, bombVol, mainVol, rout;

	*initClass {
		StartUp.add {
			SynthDef("busToOuts2_mod", {arg outBus, bus1, bus2, bus3, bus4, bus5, bus6, bus7, bus8, bus1a, bus2a, bus3a, bus4a, bus5a, bus6a, bus7a, bus8a, fade, volBus, gate=1, pauseGate = 1;
				var out, out1, out2, out3, out4, out5, out6, out7, out8, fade2, pauseEnv, env, vol;

				vol = In.kr(volBus);

				fade2 = Lag.kr(fade, 0.05);

				out1 = XFade2.ar(In.ar(bus1), In.ar(bus1a), fade2);
				out2 = XFade2.ar(In.ar(bus2), In.ar(bus2a), fade2);
				out3 = XFade2.ar(In.ar(bus3), In.ar(bus3a), fade2);
				out4 = XFade2.ar(In.ar(bus4), In.ar(bus4a), fade2);
				out5 = XFade2.ar(In.ar(bus5), In.ar(bus5a), fade2);
				out6 = XFade2.ar(In.ar(bus6), In.ar(bus6a), fade2);
				out7 = XFade2.ar(In.ar(bus7), In.ar(bus7a), fade2);
				out8 = XFade2.ar(In.ar(bus8), In.ar(bus8a), fade2);

				out = Pan2.ar(out1,1,0.5)+ Pan2.ar(out2,-0.75,0.5)+ Pan2.ar(out3,0.75,0.5)+ Pan2.ar(out4,-0.6,0.5)+ Pan2.ar(out5,0.6,0.5)+ Pan2.ar(out6,-0.2,0.5)+ Pan2.ar(out7,0.2,0.5)+Pan2.ar(out8,-1,0.5);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.2,1,0.2), gate, doneAction:2);

				Out.ar(outBus, pauseEnv*env*LeakDC.ar(out*Lag.kr(vol, 0.05)));
			}).writeDefFile;

			SynthDef("delayBomb2_mod", {arg buf, outBus, volBus;
				var out, env, env1, dur, vol;

				vol = In.kr(volBus);

				env = EnvGen.kr(Env.sine(Rand(0.5, 0.9), 1));

				out = PlayBuf.ar(1, buf, startPos: MouseX.kr*BufFrames.kr(buf))*env;

				dur = Rand(7.0, 11.0);

				env1 = EnvGen.kr(Env.new([0,1,0],[0.1, dur]), doneAction:2);

				Out.ar(outBus, Pan2.ar(CombC.ar(out, 0.3, Rand(0.1, 0.3), dur*4)*env1, Rand(-1,1), vol));

			}).writeDefFile;
		}
	}

	init {

		this.makeMixerToSynthBus;

		this.initControlsAndSynths(5);

		synths = List.new;

		bufferArray = Array.with(
			Buffer.alloc(group.server, 44100*30, 1),
			Buffer.alloc(group.server, 44100*30, 1),
			Buffer.alloc(group.server, 44100*30, 1),
			Buffer.alloc(group.server, 44100*30, 1)
		);

		bombVol = Bus.control(group.server);
		mainVol = Bus.control(group.server);

		topButtons = List.new;

		randomPitch = false;
		shiftSlide = false;

		shiftBus = Bus.audio(group.server, 8);
		distortBus = Bus.audio(group.server, 8);

		shiftGroup = Group.tail(group);
		tapeGroup = Group.tail(group);
		mixGroup = Group.tail(group);

		feedBackLooper = FeedBackLooper_Mod(shiftBus, distortBus, tapeGroup);

		topButtons.add(Button.new()
			.states_([ [ "randOff", Color.red, Color.black ], [ "randOn", Color.black, Color.green ] ])
			.action_{|v|
				if(v.value==1,{randomPitch = true},{randomPitch = false});
				harmonicShifters.do{arg item; item.randomPitch = randomPitch};
			});
		topButtons.add(Button.new()
			.states_([ [ "slideOff", Color.red, Color.black ], [ "slideOn", Color.black, Color.green ] ])
			.action_{|v| if(v.value==1,{shiftSlide = true},{shiftSlide = false})});
		topButtons.add(Button.new()
			.states_([ [ "noiseOff", Color.red, Color.black ], [ "noiseOn", Color.black, Color.green ] ])
			.action_{|v|

				v.enabled_(false);

			});
		topButtons.add(Button.new()
			.states_([ [ "slideOff", Color.red, Color.black ], [ "slideOn", Color.black, Color.green ] ])
			.action_{|v|
				if(v.value==1,{feedBackLooper.setDeviation(true)},{feedBackLooper.setDeviation(false)});
			});
		//topButtons[3].enabled_(false);
		topButtons[2].enabled_(true);
		topButtons[3].enabled_(true);

		shiftButtons = List.new;

		shiftButtons.add(Button.new()
			.states_([ [ "1-4", Color.red, Color.black ], [ "1-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(0, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "1-2", Color.red, Color.black ], [ "1-2", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(1, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "1-1", Color.red, Color.black ], [ "1-1", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(2, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "5-4", Color.red, Color.black ], [ "5-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(3, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "3-2", Color.red, Color.black ], [ "3-2", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(4, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "7-4", Color.red, Color.black ], [ "7-4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(5, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});
		shiftButtons.add(Button.new()
			.states_([ [ "2to4", Color.red, Color.black ], [ "2to4", Color.black, Color.green ] ])
			.action_{|v|
				shiftWeightArray.put(6, v.value);
				harmonicShifters.do{arg item; item.shiftWeightArray = shiftWeightArray};
			});

		shiftWeightArray = [0,0,0,0,0,0,0];
		shiftByArray = [0.25, 0.5, 1.0, 1.25, 1.5, 1.75, 2];

		controls.add(QtEZSlider.new("fade", ControlSpec(-1,1,'linear'),
			{|v|
				synths[0].set(\fade, v.value);
				this.sendSliderOsc(0, v.value);
			},-1, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				mainVol.set(v.value);
				this.sendSliderOsc(1, v.value);
			}, 0, true, \horz));
		this.addAssignButton(1,\continuous);

		harmonicShifters = List.new;
		 controls.add(Button()
			.states_([ [ "Go0", Color.green, Color.black ], [ "Go1", Color.black, Color.green ], [ "Go2", Color.green, Color.black ], [ "Go3", Color.black, Color.green ], [ "Done", Color.black, Color.red ] ])
			.action_({arg but;
				if(but.value<4, {
					/*(mixerToSynthBus, shiftBus, audioGateBus, shiftByArray, shiftWeightArray, shiftGroup, i*1.5)*/

					harmonicShifters.add(HarmonicShifter_Mod(bufferArray[but.value-1], shiftBus, shiftByArray, shiftWeightArray, 30, mixerToSynthBus.index, shiftGroup));
				},{
					but.enabled_(false);
				});
				this.sendButtonOsc(2, but.value);
			}));
		this.addAssignButton(2,\onOff);

		controls.add(Button()
			.states_([ [ "delayBomb", Color.green, Color.black ], [ "delayBomb", Color.black, Color.green ] ])
			.action_({arg but;
				4.do{arg i; Synth("delayBomb2_mod", [\buf, bufferArray[i].bufnum, \outBus, outBus, \volBus, bombVol.index], group)};
				this.sendButtonOsc(3, but.value);
			}));

		this.addAssignButton(3,\onOff);

		controls.add(QtEZSlider.new("bVol", ControlSpec(0,4,'amp'),
			{|v|
				bombVol.set(v.value);

				this.sendSliderOsc(4, v.value);
			}, 4, true, \horz));
		this.addAssignButton(4,\continuous);


		this.makeWindow("ShifterFeedback",Rect(1018, 644, 315, 212));

		win.layout_(VLayout(
			HLayout(topButtons[0], topButtons[1], topButtons[2], topButtons[3]),
			HLayout(shiftButtons[0], shiftButtons[1], shiftButtons[2], shiftButtons[3]),
			HLayout(shiftButtons[4], shiftButtons[5], shiftButtons[6]),
			HLayout(controls[1].layout,assignButtons[1].layout),
			HLayout(controls[0].layout,assignButtons[0].layout),
			HLayout(controls[2], assignButtons[2].layout, controls[3], assignButtons[3].layout),
			HLayout(controls[4].layout,	assignButtons[4].layout)
		));
		win.layout.spacing = 2;
		win.layout.margins = [0,0,0,0];


		synths.add(Synth("busToOuts2_mod", [\outBus, outBus, \bus1, shiftBus.index, \bus2, shiftBus.index+1, \bus3, shiftBus.index+2, \bus4, shiftBus.index+3, \bus5, shiftBus.index+4, \bus6, shiftBus.index+5, \bus7, shiftBus.index+6, \bus8, shiftBus.index+7, \bus1a, distortBus.index, \bus2a, distortBus.index+1, \bus3a, distortBus.index+2, \bus4a, distortBus.index+3, \bus5a, distortBus.index+4, \bus6a, distortBus.index+5, \bus7a, distortBus.index+6, \bus8a, distortBus.index+7, \vol, 0, \fade, -1, \volBus, mainVol.index], mixGroup));
	}

	killMeSpecial {
		harmonicShifters.do{arg item; item.killMe;};
		if(feedBackLooper!=nil,{feedBackLooper.killMe});
	}
}


BitCrusher_Mod : Module_Mod {
	var sr1Bus, sr2Bus, distVolBus, sineVolBus;

	*initClass {
		StartUp.add {
			SynthDef("bitCrusher2_Mod",{ arg inbus, outbus, sr1Bus, sr2Bus, distVolBus, sineVolBus, gate = 1, pauseGate = 1;
				var input, fx1, fx2, sines, sine0, sine1, sine2, sine3, sine4, fund, skip, freq, hasFreq, env, pauseEnv;
				var sr1, sr2, distVol, sineVol;

				sr1 = In.kr(sr1Bus);
				sr2 = In.kr(sr2Bus);
				distVol = In.kr(distVolBus);
				sineVol = In.kr(sineVolBus);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				input=In.ar(inbus, 1);
				fx1=Latch.ar(input.round(0.125),Impulse.ar(sr1));
				fx2=Latch.ar(input.round(0.1),Impulse.ar(sr2));

				#freq, hasFreq = Pitch.kr(fx1, ampThreshold: 0.02, median: 7);

				fund = TIRand.kr(7, 50, hasFreq);
				skip = TIRand.kr(1, 5, hasFreq);

				sine0 = SinOsc.ar(freq, 0, hasFreq);
				sine1 = SinOsc.ar(freq+(fund*skip), 0, hasFreq);
				sine2 = SinOsc.ar(freq+(2*fund*skip), 0, hasFreq);
				sine3 = SinOsc.ar(freq+(3*fund*skip), 0, hasFreq);
				sine4 = SinOsc.ar(freq+(4*fund*skip), 0, hasFreq);

				sines = Pan2.ar(sine0, TRand.kr(-1,1,hasFreq)) + Pan2.ar(sine1, TRand.kr(-1,1,hasFreq)) + Pan2.ar(sine2, TRand.kr(-1,1,hasFreq)) + Pan2.ar(sine3, TRand.kr(-1,1,hasFreq)) + Pan2.ar(sine4, TRand.kr(-1,1,hasFreq));

				Out.ar(outbus, sines*0.25*sineVol*env*pauseEnv);
				Out.ar(outbus,([fx1,fx2])*distVol*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {

		this.makeMixerToSynthBus;

		sr1Bus = Bus.control(group.server);
		sr2Bus = Bus.control(group.server);
		distVolBus = Bus.control(group.server);
		sineVolBus = Bus.control(group.server);

		this.initControlsAndSynths(3);

		synths = List.new;
		synths.add(Synth("bitCrusher2_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \sineVolBus, sineVolBus], group));

		controls.add(QtEZSlider.new("blip", ControlSpec(0,1,'amp'),
			{|v|
				sineVolBus.set(v.value);
				this.sendSliderOsc(0, v.value);
		}, 0, true));
		this.addAssignButton(0, \continuous);


		controls.add(QtEZSlider.new("bit", ControlSpec(0,1,'amp'),
			{|v|
				distVolBus.set(v.value);
				this.sendSliderOsc(1, v.value);
			}, 0, true));

		this.addAssignButton(1, \continuous);

		controls.add(QtEZSlider.new("sr", ControlSpec(0,127,'linear'),
			{|v|
				sr1Bus.set(v.value*40+400);
				sr2Bus.set(v.value*40+300);
				this.sendSliderOsc(2, v.value);
			}, 0));
		this.addAssignButton(2,\continuous);

		this.makeWindow("BitCrusher", Rect(0, 0, 200, 200));

		win.layout_(VLayout(
			HLayout(controls[0].layout, controls[1].layout, controls[2].layout),
			HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout)
		).spacing_(0).margins_(0!4));
		win.front;

	}

}

BitInterrupter_Mod : Module_Mod {
	var sr1Bus, sr2Bus, distVolBus, sineVolBus;

	*initClass {
		StartUp.add {
			SynthDef("bitInterrupter2_Mod",{ arg inbus, outbus, sr1Bus, sr2Bus, distVolBus, distortSwitch, gate = 1, pauseGate = 1;
				var in, fx1, fx2, env, pauseEnv, out;
				var sr1, sr2, distVol;

				sr1 = In.kr(sr1Bus);
				sr2 = In.kr(sr2Bus);
				distVol = In.kr(distVolBus);

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in=In.ar(inbus, 2);

				fx1=Latch.ar(in[0].round(0.125),Impulse.ar(sr1));
				fx2=Latch.ar(in[1].round(0.1),Impulse.ar(sr2));

				out = ([fx1,fx2])*distVol*env*pauseEnv;

				out = (Lag.kr(1-distortSwitch, 0.05)*in)+(Lag.kr(distortSwitch, 0.05)*out);

				Out.ar(outbus,out);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BitInterrupter",Rect(718, 645, 135, 270));

		this.makeMixerToSynthBus(8);

		sr1Bus = Bus.control(group.server);
		sr2Bus = Bus.control(group.server);
		distVolBus = Bus.control(group.server);
		sineVolBus = Bus.control(group.server);

		this.initControlsAndSynths(4);

		synths = List.new;
		synths.add(Synth("bitInterrupter2_Mod", [\inbus, mixerToSynthBus.index, \outbus, outBus, \sr1Bus, sr1Bus, \sr2Bus, sr2Bus, \distVolBus, distVolBus, \distortSwitch, 0], group));

		controls.add(Button(win,Rect(5, 0, 60, 100))
			.states_([["Off", Color.black, Color.red],["On", Color.black, Color.green]])
			.action_{|butt|
				if(butt.value==1,{
					synths[0].set(\distortSwitch, 1);
				},{
					synths[0].set(\distortSwitch, 0);
				});
				this.sendButtonOsc(0, butt.value);
			});
		this.addAssignButton(0,\onOff, Rect(5, 100, 60, 20));

		controls.add(EZSlider.new(win,Rect(5, 120, 60, 120), "bitVol", ControlSpec(0,1,'amp'),
			{|v|
				distVolBus.set(v.value);
				this.sendSliderOsc(1, v.value);
			}, 0, layout:\vert));

		this.addAssignButton(1,\continuous, Rect(5, 240, 60, 20));

		controls.add(EZKnob.new(win,Rect(70, 0, 60, 100), "sr", ControlSpec(300,5500,'linear'),
			{|v|
				sr1Bus.set(v.value+100);
				sr2Bus.set(v.value);
				this.sendSliderOsc(2, v.value);
			}, 0));
		this.addAssignButton(2,\continuous, Rect(70, 100, 60, 20));

		controls.add(EZSlider.new(win,Rect(70, 120, 60, 120), "sr", ControlSpec(300,5500,'linear'),
			{|v|
				sr1Bus.set(v.value+100);
				sr2Bus.set(v.value);
				this.sendSliderOsc(3, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(3,\continuous, Rect(70, 240, 60, 20));
	}
}

