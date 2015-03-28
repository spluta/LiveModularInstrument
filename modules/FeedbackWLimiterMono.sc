//
// FeedbackDetectionMono {
// 	var <>group, <>inBus, <>outBus, <>size, <>multis, thresholdShort=0.5, thresholdLong = 0.5, temp, mulBus0, mulBus1, synth, task, getArray, finalVals, tot, sepArray, buffer, inc, waitTime=0.5, mulArray0, mulArray1;
//
// 	*new {arg group, inBus, outBus, multis;
// 		^super.new.group_(group).inBus_(inBus).outBus_(outBus).multis_(multis).init;
// 	}
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("spectralLimiterMono_mod", {arg inBus, outBus, bufnum, threshold, mulBus0, mulBus1, pauseGate = 1, gate=1;
// 				var mulArray0, mulArray1, chain0, chain1, env, pauseEnv, in, fftSize, clip, outSig;
//
// 				fftSize = 4096;
//
// 				in = In.ar(inBus);
//
// 				Amplitude.kr(in);
//
// 				mulArray0 = In.kr(mulBus0, 560);
// 				mulArray1 = In.kr(mulBus1, 560);
//
// 				chain0 = FFT(bufnum, in);
//
// 				chain1 = PV_Copy(chain0, LocalBuf(fftSize));
//
// 				chain1 = chain1.pvcollect(fftSize, {|mag, phase, index|
// 					min(mag*mulArray0[index]*mulArray1[index], 6.0)
// 					//mag*mulArray0[index]*mulArray1[index]
// 				}, frombin: 0, tobin: 559, zeroothers: 0);
//
// 				env = EnvGen.kr(Env.asr(2,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				outSig = IFFT(chain1)*env*pauseEnv;
//
// 				Out.ar(outBus, outSig);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		size = 560;
//
// 		mulBus0 = Bus.control(group.server, size);
// 		mulBus1 = Bus.control(group.server, size);
//
// 		mulArray0 = MulArray(size, mulBus0, multis[0], multis[1], multis[4], 25, 0);
// 		mulArray1 = MulArray(size, mulBus1, multis[2], multis[3], multis[4], 250, 1);
//
// 		{
// 			buffer = Buffer.alloc(group.server, 4096, 1);
// 			group.server.sync;
// 			0.4.wait;
//
// 			synth = Synth("spectralLimiterMono_mod", [\inBus, inBus, \outBus, outBus, \bufnum, buffer.bufnum, \threshold, 0.5, \mulBus0, mulBus0.index, \mulBus1, mulBus1.index], group);
//
// 			task = Task({ { buffer.getn(2, size*2, { arg buf;
// 				var z, x;
// 				z = buf.clump(2).flop;
// 				z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
// 				x = Complex(z[0], z[1]);
// 				getArray = (Array.newFrom(x.magnitude)* 0.05).squared;
// 				mulArray0.addVolArray(getArray);
// 				mulArray1.addVolArray(getArray);
// 			}); waitTime.wait;}.loop });
// 			task.start;
// 		}.fork;
// 	}
//
// 	setThresholdShort {arg in;
// 		mulArray0.threshold_(in);
// 	}
//
// 	setThresholdLong {arg in;
// 		mulArray1.threshold_(in);
// 	}
//
// 	setNumBinsLong {arg in;
// 		mulArray1.numBinsLong_(in);
// 	}
//
// 	setOverallThresh {arg thresh;
// 		synth.set(\threshold, thresh);
// 	}
//
// 	pause {
// 		if (task!=nil,{
// 			task.pause;
// 			synth.set(\pauseGate, 0);
// 		});
// 	}
//
// 	unpause {
// 		task.resume;
// 		synth.set(\pauseGate, 1);
// 		synth.run(true);
// 	}
//
// 	setAnalSec {arg analSex;
// 		waitTime = 1/analSex;
// 	}
//
// 	turnDispOn {
// 		mulArray0.turnDispOn;
// 		mulArray1.turnDispOn;
// 	}
//
// 	turnDispOff {
// 		mulArray0.turnDispOff;
// 		mulArray1.turnDispOff;
// 	}
//
// 	killMe {
// 		mulArray0.killMe;
// 		mulArray1.killMe;
// 		task.stop;
// 		synth.set(\gate, 0);
// 	}
// }
//
// FeedbackControlMono_Mod : Module_Mod {
// 	var volBus, feedbackDetection, synthGroup, limiterGroup, multis, tempArray, transferBus;
//
// 	*initClass {
// 		StartUp.add {
//
// 			SynthDef("delayFeedbackMono_mod", {arg inBus, outBus, volBus, delay = 0.02158, varAmount=0.5, varFreq=0.02, pauseGate = 1, gate = 1;
// 				var in, convolveAudio, volume, env, pauseEnv, out0, out1;
//
// 				volume = In.kr(volBus);
//
// 				in  = LPF.ar(In.ar(inBus), 3000);
//
// 				in = Compander.ar(in, in,
// 					thresh: 0.5,
// 					slopeBelow: 1,
// 					slopeAbove: 0.5,
// 					clampTime: 0.01,
// 					relaxTime: 0.01
// 				);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				in = DelayC.ar(in, 0.5, (LFNoise2.kr(varFreq).range(0, varAmount)+delay));
//
// 				out0 = Limiter.ar(in, 0.1, 0.03)*volume;
// 				out1 = Normalizer.ar(in, 0.9, 0.03);
//
//
// 				Out.ar(outBus, [out0, out1]);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("DelayFeedbackMono", Rect(318, 645, 345, 250));
// 		this.initControlsAndSynths(3);
//
// 		this.createMultis;
//
// 		this.makeMixerToSynthBus;
//
// 		transferBus = Bus.audio(group.server);
//
// 		volBus = Bus.control(group.server);
//
// 		limiterGroup = Group.tail(group);
// 		synthGroup = Group.tail(group);
//
// 		feedbackDetection = FeedbackDetectionMono(limiterGroup, mixerToSynthBus.index, transferBus, multis);
//
// 		4.do{|i|synths.add(Synth("delayFeedbackMono_mod",[\inBus, transferBus.index, \outBus, outBus.index, \volBus, volBus.index, \delay, 0.025+(i*0.1234)], synthGroup))};
//
// 		controls.add(EZSlider.new(win,Rect(5, 5, 80, 220), "vol", ControlSpec(0,1,'amp'),
// 			{|v|
// 				volBus.set(v.value);
// 		}, 0, layout:\vert));
// 		this.addAssignButton(0,\continuous, Rect(5, 230, 80, 20));
//
// 		controls.add(EZSlider.new(win,Rect(90, 5, 80, 220), "maxDel", ControlSpec(0.02, 0.1,'linear'),
// 			{|v|
// 				synths.do{arg item; item.set(\varFreq, v.value)};
// 		}, 0.02, layout:\vert));
// 		this.addAssignButton(1,\continuous, Rect(90, 230, 80, 20));
//
// 		controls.add(EZSlider.new(win,Rect(175, 5, 80, 220), "threshShort", ControlSpec(0,1,'linear'),
// 			{|v|
// 				feedbackDetection.setThresholdShort(v.value);
// 		}, 0.5, layout:\vert));
// 		this.addAssignButton(2,\continuous, Rect(175, 230, 80, 20));
//
// 		controls.add(EZSlider.new(win,Rect(260, 5, 80, 220), "threshLong", ControlSpec(0,1,'linear'),
// 			{|v|
// 				feedbackDetection.setThresholdLong(v.value);
// 		}, 0.5, layout:\vert));
// 		this.addAssignButton(3,\continuous, Rect(260, 230, 80, 20));
//
// 		controls.add(EZSlider.new(win,Rect(345, 5, 80, 220), "numBinsLong", ControlSpec(2,15,'linear',1),
// 			{|v|
// 				feedbackDetection.setNumBinsLong(v.value);
// 		}, 5, layout:\vert));
// 		this.addAssignButton(4,\continuous, Rect(345, 230, 80, 20));
//
// 		controls.add(EZSlider.new(win,Rect(430, 5, 80, 220), "anal/Sec", ControlSpec(2,15,'linear',1),
// 			{|v|
// 				feedbackDetection.setAnalSec(v.value);
// 		}, 2, layout:\vert));
// 		this.addAssignButton(5,\continuous, Rect(430, 230, 80, 20));
//
// 		controls.add(Button(win, Rect(5, 260, 90, 20))
// 			.states_([["dispOff", Color.blue, Color.black],["dispOn", Color.black, Color.blue]])
// 			.action_({arg butt;
// 				if(butt.value==0,{
// 					feedbackDetection.turnDispOff
// 					},{
// 						feedbackDetection.turnDispOn
// 				})
// 			})
// 		);
//
//
// 		//multichannel button
// 		numChannels = 2;
//
// 	}
//
// 	createMultis {arg size = 560;
//
// 		multis = List.newClear(0);
//
// 		multis.add(MultiSliderView(win, Rect(515, 5, size, 350)));
// 		tempArray = Array.new;
// 		size.do({arg i;
// 			tempArray = tempArray.add(i/size);
// 		});
// 		multis[0].value_(tempArray);
// 		multis[0].readOnly = false;
// 		multis[0].xOffset_(1);
// 		multis[0].thumbSize_(1);
// 		multis[0].strokeColor_(Color.black);
// 		multis[0].drawLines_(true);
// 		multis[0].drawRects_(false);
// 		multis[0].indexThumbSize_(0.5);
// 		multis[0].valueThumbSize_(0.1);
// 		multis[0].isFilled_(false);
// 		multis[0].gap_(1);
//
//
// 		multis.add(MultiSliderView(win, Rect(515+size, 5, size, 350)));
// 		multis[1].value_(tempArray);
// 		multis[1].readOnly = false;
// 		multis[1].xOffset_(1);
// 		multis[1].thumbSize_(1);
// 		multis[1].strokeColor_(Color.black);
// 		multis[1].drawLines_(true);
// 		multis[1].drawRects_(false);
// 		multis[1].indexThumbSize_(0.5);
// 		multis[1].valueThumbSize_(0.1);
// 		multis[1].isFilled_(false);
// 		multis[1].gap_(1);
//
// 		multis.add(MultiSliderView(win, Rect(515, 355, size, 350)));
// 		multis[2].value_(tempArray);
// 		multis[2].readOnly = false;
// 		multis[2].xOffset_(1);
// 		multis[2].thumbSize_(1);
// 		multis[2].strokeColor_(Color.black);
// 		multis[2].drawLines_(true);
// 		multis[2].drawRects_(false);
// 		multis[2].indexThumbSize_(0.5);
// 		multis[2].valueThumbSize_(0.1);
// 		multis[2].isFilled_(false);
// 		multis[2].gap_(1);
//
// 		multis.add(MultiSliderView(win, Rect(515+size, 355, size, 350)));
// 		multis[3].value_(tempArray);
// 		multis[3].readOnly = false;
// 		multis[3].xOffset_(1);
// 		multis[3].thumbSize_(1);
// 		multis[3].strokeColor_(Color.black);
// 		multis[3].drawLines_(true);
// 		multis[3].drawRects_(false);
// 		multis[3].indexThumbSize_(0.5);
// 		multis[3].valueThumbSize_(0.1);
// 		multis[3].isFilled_(false);
// 		multis[3].gap_(1);
//
// 		multis.add(MultiSliderView(win, Rect(5, 355, size, 350)));
// 		tempArray = Array.newClear(0);
// 		size.do{|i|tempArray = tempArray.add(1-(i/(size*2)))};
// 		multis[4].value_(tempArray);
// 		multis[4].readOnly = false;
// 		multis[4].xOffset_(1);
// 		multis[4].thumbSize_(1);
// 		multis[4].strokeColor_(Color.black);
// 		multis[4].drawLines_(true);
// 		multis[4].drawRects_(false);
// 		multis[4].indexThumbSize_(0.5);
// 		multis[4].valueThumbSize_(0.1);
// 		multis[4].isFilled_(false);
// 		multis[4].gap_(1);
// 	}
//
// 	pause {
// 		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
// 		feedbackDetection.pause;
// 	}
//
// 	unpause {
// 		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
// 		feedbackDetection.unpause;
// 	}
//
// 	killMeSpecial {
// 		feedbackDetection.killMe;
// 	}
// }