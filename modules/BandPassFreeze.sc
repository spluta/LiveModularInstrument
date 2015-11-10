BandPassFreeze_Mod : Module_Mod {
	var win, frozenAudioBus, fftBus, levelBus, updateDisplayRout, volDisplay, displayVol, volumeDisplay, onOff, rout, volBus, threshBus, onOffBus, buffers, soundGroup, panGroup, transferBus, getTrig, trigButton;

	*initClass {
		StartUp.add {
			SynthDef("bandPassFreeze2_mod", { arg audioInBus, transferBus, buffer, buf0, buf1, buf2, thresh, low = 0.1, hi = 0.5, t_trig=0, gate = 1, pauseGate = 1;
				var audioIn, fftIn, chain, chain0, chain1, chain2, outSig, trig0, trig1, trig2, trig, amp, pan0, pan1, pan2, env, env0, env1, env2, outSig0, outSig1, outSig2, pauseEnv, muteEnv;

				audioIn = In.ar(audioInBus)*EnvGen.kr(Env.dadsr(1,0,0,1,0), 1);

				amp = Amplitude.kr(audioIn);

				//trig = Trig1.ar(amp-thresh,0.01);

				trig = Coyote.kr(audioIn, thresh: thresh, minDur: 0.1)+Trig1.kr(Decay.kr(t_trig, 0.1)-0.1, 0.1);

				SendTrig.kr(trig, 10, 1);

				//PulseCount.kr(trig);

				trig0 = Trig1.ar(PulseDivider.kr(trig, 3, 0), 0.02);
				trig1 = Trig1.ar(PulseDivider.kr(trig, 3, 1), 0.02);
				trig2 = Trig1.ar(PulseDivider.kr(trig, 3, 2), 0.02);

				chain = FFT(buffer, audioIn);

				chain0 = PV_Copy(chain, buf0);
				chain1 = PV_Copy(chain, buf1);
				chain2 = PV_Copy(chain, buf2);

				chain0 = PV_Freeze(chain0, 1 - (trig0+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));
				chain1 = PV_Freeze(chain1, 1 - (trig1+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));
				chain2 = PV_Freeze(chain2, 1 - (trig2+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)));

				chain0 = PV_BrickWall(chain0, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));
				chain1 = PV_BrickWall(chain1, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));
				chain2 = PV_BrickWall(chain2, SinOsc.ar(LFNoise2.ar(1).range(low, hi)));

				outSig0 = IFFT(chain0);
				outSig1 = IFFT(chain1);
				outSig2 = IFFT(chain2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(1.1,1,0.1), gate, doneAction:2);

				Out.ar(transferBus, [outSig0, outSig1, outSig2]);
			}).writeDefFile;

			SynthDef("bpfPan2_mod", {arg transferBus, audioOutBus, onOff = 1, vol=0, pauseGate = 1, gate = 1;
				var in, pan0, pan1, pan2, outSig0, outSig1, outSig2, outSig, pauseEnv, muteEnv, env;

				in = In.ar(transferBus, 3);

				pan0 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				pan1 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				pan2 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1));
				outSig0 = Pan2.ar(in[0], pan0, 1);
				outSig1 = Pan2.ar(in[1], pan1, 1);
				outSig2 = Pan2.ar(in[2], pan2, 1);

				outSig = outSig0+outSig1+outSig2;

				outSig = Compander.ar(outSig, outSig,
					thresh: 0.8,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				muteEnv = EnvGen.kr(Env.asr(0.001, 1, 0.001), onOff);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				outSig = outSig*vol*env*pauseEnv*muteEnv;

				Out.ar(audioOutBus, outSig);

			}).writeDefFile;

			SynthDef("bpfPan4_mod", {arg transferBus, audioOutBus, onOff = 1, vol=0, pauseGate = 1, gate = 1;
				var in, pan0, pan1, pan2, outSig0, outSig1, outSig2, outSig, pauseEnv, muteEnv, env;

				in = In.ar(transferBus, 3);

				pan0 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				pan1 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				pan2 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				outSig0 = PanAz.ar(4, in[0], pan0, 1);
				outSig1 = PanAz.ar(4, in[1], pan1, 1);
				outSig2 = PanAz.ar(4, in[2], pan2, 1);

				outSig = outSig0+outSig1+outSig2;

				outSig = Compander.ar(outSig, outSig,
					thresh: 0.8,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				muteEnv = EnvGen.kr(Env.asr(0.001, 1, 0.001), onOff);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				outSig = outSig*vol*env*pauseEnv*muteEnv;

				Out.ar(audioOutBus, outSig);

			}).writeDefFile;

			SynthDef("bpfPan8_mod", {arg transferBus, audioOutBus, onOff = 1, vol=0, pauseGate = 1, gate = 1;
				var in, pan0, pan1, pan2, outSig0, outSig1, outSig2, outSig, pauseEnv, muteEnv, env;

				in = In.ar(transferBus, 3);

				pan0 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				pan1 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				pan2 = SinOsc.ar(LFNoise2.kr(0.1).range(0.2, 1))+SinOsc.ar(LFNoise2.kr(0.1).range(0.05, 0.1), Rand(0, 2));
				outSig0 = PanAz.ar(8, in[0], pan0, 1);
				outSig1 = PanAz.ar(8, in[1], pan1, 1);
				outSig2 = PanAz.ar(8, in[2], pan2, 1);

				outSig = outSig0+outSig1+outSig2;

				outSig = Compander.ar(outSig, outSig,
					thresh: 0.8,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				muteEnv = EnvGen.kr(Env.asr(0.001, 1, 0.001), onOff);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				outSig = outSig*vol*env*pauseEnv*muteEnv;

				Out.ar(audioOutBus, outSig);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("BandPassFreeze",Rect(946, 618, 130, 350));
		this.initControlsAndSynths(9);

		soundGroup = Group.head(group);
		panGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 3);

		this.makeMixerToSynthBus;

		synths = List.newClear(2);

		buffers = List.newClear(0);
		4.do{buffers.add(Buffer.alloc(group.server, 2048))};

		levelBus = Bus.control(group.server, 1);

		onOff = 0;

		rout = Routine({
			group.server.sync;
			1.0.wait;
			buffers.do{arg item; item.zero};
			group.server.sync;
			1.0.wait;
			synths.put(0, Synth("bandPassFreeze2_mod", [\audioInBus, mixerToSynthBus.index, \transferBus, transferBus, \buffer, buffers[0].bufnum, \buf0, buffers[1].bufnum, \buf1, buffers[2].bufnum, \buf2, buffers[3].bufnum, \thresh, 1], soundGroup));

			synths.put(1, Synth("bpfPan2_mod", [\transferBus, transferBus, \audioOutBus, outBus, \vol, 0], panGroup));
		});

		AppClock.play(rout);

		controls.add(EZSlider(win, Rect(0, 40, 40, 200), "Amp", ControlSpec(0.001, 2, \amp),
			{arg slider;
				synths[1].set(\vol, slider.value);
			}, 0, true, layout:\vert));
		this.addAssignButton(0, \continuous, Rect(0, 240, 40, 20));

		controls.add(EZSlider(win, Rect(45, 40, 40, 200), "Thresh", ControlSpec(0.0, 1.0, \cos), {arg slider;
				synths[0].set(\thresh, slider.value);
			}, 0.1, true, layout:\vert));
		this.addAssignButton(1, \continuous, Rect(45, 240, 40, 20));

		controls.add(EZSlider(win, Rect(90, 40, 40, 200), "Speed", ControlSpec(0.025, 12.0, \linear), {arg slider;
				synths[0].set(\low, slider.value, \hi, slider.value+(2*(slider.value.sqrt)));
			}, 0.1, true, layout:\vert));
		this.addAssignButton(2, \continuous, Rect(90, 240, 40, 20));

		controls.add(Button(win, Rect(0, 280, 65, 20))
			.states_([
				["On", Color.green, Color.black],
				["Off", Color.black, Color.red]
			])
			.action_{arg butt;
				synths[1].set(\onOff, (butt.value+1).wrap(0,1))
			});
		this.addAssignButton(3,\onOff, Rect(65, 280, 65, 20));

		controls.add(Button(win, Rect(0, 305, 65, 20))
			.states_([
				["manTrig", Color.red, Color.blue],
				["manTrig", Color.blue, Color.red]
			])
			.action_{arg butt;
				synths[0].set(\t_trig, 1)
			});
		this.addAssignButton(4,\onOff, Rect(65, 305, 65, 20));

		Button(win, Rect(5, 330, 60, 20))
			.states_([["0 Bufs", Color.black, Color.white]])
			.action_{
				buffers.do{arg item; item.zero};
			};

		trigButton = Button(win, Rect(70, 330, 60, 20))
		.states_([["trig", Color.red, Color.blue],["trig", Color.blue, Color.red]]);

		getTrig = OSCFunc({|msg, time|
			if(msg[2]==10, {
				{trigButton.value = (trigButton.value+1).wrap(0,1)}.defer
	})}, '/tr');

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(5, 355, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				AppClock.sched(1.0, {
					"change channels".postln;
					switch(butt.value,
						0, {
							synths[1].set(\gate, 0);
							numChannels = 2;
							synths.put(1, Synth("bpfPan2_mod", [\transferBus, transferBus, \audioOutBus, outBus, \vol, 0], panGroup));
						},
						1, {
							synths[1].set(\gate, 0);
							numChannels = 4;
							synths.put(1, Synth("bpfPan4_mod", [\transferBus, transferBus, \audioOutBus, outBus, \vol, 0], panGroup));
						},
						2, {
							synths[1].set(\gate, 0);
							numChannels = 8;
							synths.put(1, Synth("bpfPan8_mod", [\transferBus, transferBus, \audioOutBus, outBus, \vol, 0], panGroup));
						}
					);
					nil
				});
			};
		);


		win.front;
	}


/*	loadSettings {arg xmlSynth;
		var routB;

		routB = Routine({
			group.server.sync;
			2.0.wait;
			controls.do{arg item, i;
				midiHidTemp = xmlSynth.getAttribute("controls"++i.asString);
				if(midiHidTemp!=nil,{
					("controls"++i.asString+" ").post;
					if(controls[i].value!=midiHidTemp.interpret,{
						controls[i].valueAction_(midiHidTemp.interpret.postln);
					});
				});
			};
		});
		AppClock.play(routB);
	}*/

	killMeSpecial {
		getTrig.stop;
	}


}
