RDVolumeDisplay_Mod {
	var <win, <bounds, top;

	*new {arg win, bounds;
		^super.newCopyArgs(win, bounds);
	}

	init {
	}

	update {arg val;
		Color.black.set;
		Pen.strokeRect(bounds);
		if(val>=1,{Color.red.set},{Color.green.set});
		top = bounds.top + (bounds.height-((val*bounds.height)));
		Pen.fillRect(Rect(bounds.left+2, top, bounds.width-4, val*bounds.height-4));
	}
}


Freeze_Mod : Module_Mod {
	var group, outBus, midiHidControl, manta, buffer, win, frozenAudioBus, fftBus, levelBus, updateDisplayRout, volDisplay, displayVol, volumeDisplay, onOff, rout, volBus, threshBus, onOffBus, muteGateBus, buffers;

	*initClass {
		StartUp.add {
			SynthDef("rdFreeze_mod", { arg audioInBus, audioOutBus, levelBus, t_keyTrig, volBus, threshBus, muteGateBus, onOffBus, buffer, gate = 1, pauseGate = 1;
				var audioIn, fftIn, chain, outSig, trig1, trig2, trig, amp, sin, peak, env, pauseEnv, muteEnv, vol, thresh, muteGate, onOff;

				vol = In.kr(volBus);
				thresh = In.kr(threshBus);
				muteGate = In.kr(muteGateBus);
				onOff = In.kr(onOffBus);

				audioIn = In.ar(audioInBus, 1)*EnvGen.kr(Env.dadsr(3,0,0,1,0), 1);

				amp = Amplitude.kr(audioIn)*EnvGen.kr(Env.asr(0.001, 1, 0.001), onOff);

				trig1 = Trig1.kr(Coyote.kr(audioIn, thresh: thresh, minDur: 0.1),0.1)*1.1;

				trig2 = Trig1.kr(t_keyTrig, 0.1)*1.1;

				trig = (trig1+trig2);

				chain = FFT(buffer, audioIn);

				chain = PV_Freeze(chain, ((1 - (trig+EnvGen.kr(Env.new([0,0,2,0], [0.5, 0.1,0.001]), 1)))*EnvGen.kr(Env.dadsr(3,0,0,1,0), 1)));
				outSig = IFFT(chain);

				peak = PeakFollower.ar(outSig, 0.99);
				Out.kr(levelBus, peak);

				sin = SinOsc.kr(Rand(0.09,1.1))*0.25;

				outSig = Compander.ar(outSig, outSig,
					thresh: 0.8,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				muteEnv = EnvGen.kr(Env.asr(0.1,1,0.1), muteGate);

				outSig = outSig*vol*env*pauseEnv*muteEnv;

				Out.ar(audioOutBus, outSig);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Freeze",Rect(946, 618, 130, 330));
		this.initControlsAndSynths(8);

		this.makeMixerToSynthBus(8);

		volBus = Bus.control(group.server);
		threshBus = Bus.control(group.server);
		onOffBus = Bus.control(group.server);
		muteGateBus = Bus.control(group.server);

		synths = List.newClear(8);

		buffers = List.new;
		2.do{buffers.add(Buffer.alloc(group.server, 2048, 1))};

		levelBus = Bus.control(group.server, 1);

		onOff = 0;

		controls.add(Button()
			.states_([
				["Locked", Color.red, Color.black],
				["Locked", Color.black, Color.green]
			])
			.action = {arg butt;
				onOff = 1;
				onOffBus.set(onOff);
				butt.value_(1);
				controls[1].value = 0;
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button()
			.states_([
				["Free", Color.red, Color.black],
				["Free", Color.black, Color.green]
			])
			.action = {arg butt;
				onOff = 1;
				onOffBus.set(onOff);
				butt.value_(1);
				controls[0].value = 0;
		});
		this.addAssignButton(1, \onOff);
		controls[0].valueAction_(1);

		controls.add(QtEZSlider("Amp", ControlSpec(0.001, 2, \amp),
			{arg slider;
				volBus.set(slider.value);
		}, 1, true, \vert));
		this.addAssignButton(2, \continuous);

		controls.add(QtEZSlider("Thresh", ControlSpec(0.0, 0.05, \cos), {arg slider;
			threshBus.set(slider.value);
		}, 0.01, true, \vert));
		this.addAssignButton(3, \continuous);

		controls[2].value = 1;

		controls.add(Button()
			.states_([
				["trig", Color.black, Color.red],
				["trig", Color.black, Color.blue]
			])
			.action_{arg butt;
				synths.do{arg item; item.set(\t_keyTrig, 1)};
		});

		this.addAssignButton(4, \onOff);

		controls.add(Button()
			.states_([
				["On", Color.red, Color.black],
				["On", Color.black, Color.green]
			])
			.action_{arg butt;
				muteGateBus.set(1);
				butt.value = 1;
				controls[6].value = 0;
		});
		this.addAssignButton(5,\onOff);

		controls.add(Button()
			.states_([
				["Off", Color.red, Color.black],
				["Off", Color.black, Color.green]
			])
			.action_{arg butt;
				muteGateBus.set(0);
				butt.value = 1;
				controls[5].value = 0;
		});
		this.addAssignButton(6,\onOff);

		controls[1].valueAction_(1);
		controls[5].valueAction_(1);

		//multichannel button
		numChannels = 2;

		rout = Routine({
			group.server.sync;
			1.8.wait;
			//buffers.do{arg item; item.zero};
			group.server.sync;
			1.0.wait;
			synths.add(Synth("rdFreeze_mod", [\audioInBus, mixerToSynthBus.index, \audioOutBus, outBus, \levelBus, levelBus.index, \onOffBus, onOffBus, \muteGateBus, muteGateBus, \volBus, volBus, \threshBus, threshBus, \buffer, buffers[0]], group));
			synths.add(Synth("rdFreeze_mod", [\audioInBus, mixerToSynthBus.index+1, \audioOutBus, outBus.index+1, \levelBus, levelBus.index, \onOffBus, onOffBus, \muteGateBus, muteGateBus, \volBus, volBus, \threshBus, threshBus, \buffer, buffers[1]], group));
		});
		AppClock.play(rout);

		win.layout_(
			VLayout(
				HLayout(
					VLayout(controls[0], controls[1]),
					VLayout(assignButtons[0], assignButtons[1])
				),
				HLayout(
					VLayout(controls[2], assignButtons[2]),
					VLayout(controls[3], assignButtons[3])
				),
				HLayout(
					VLayout(controls[4], controls[5], controls[6]),
				VLayout(assignButtons[4], assignButtons[5], assignButtons[6])
					)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;

		win.front;
	}


	load {arg loadArray;
		rout = Routine( {
			dontLoadControls = [0,1,4,5,6];
			loadArray[1].do{arg controlLevel, i;
				if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
					controls[i].valueAction_(controlLevel);
				});
			};
			loadArray[2].do{arg msg, i;
				waitForSetNum = i;
				if(msg!=nil,{
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
					assignButtons[i].instantButton.value_(1);
				})
			};
			win.bounds_(loadArray[3]);
			controls[1].valueAction_(1);
			controls[5].valueAction_(1);
			this.loadExtra(loadArray);
		});
		AppClock.play(rout);
	}

	killMeSpecial {
		buffers.do{arg item; item.free};

		volBus.free;
		threshBus.free;
		onOffBus.free;
		muteGateBus.free;
	}
}

TFreeze_Mod : Module_Mod {
	var group, outBus, midiHidControl, manta, buffers, win, frozenAudioBus, fftBus, levelBus, updateDisplayRout, volDisplay, displayVol, volumeDisplay, rout, trigRateBus, dustOnBus, volBus, modeBus;

	*initClass {
		StartUp.add {
			SynthDef("tFreeze2_mod", { arg inBus, outBus, modeBus, trigRateBus, dustOnBus, triggerOnce, volBus, gate = 1, pauseGate = 1, buffer0, buffer1;
				var audioIn, chain, outSig, trig, trig0, trig1, switch0, switch1, env, pauseEnv, trigRate, dustOn, vol, mode;

				audioIn = In.ar(inBus, 2)*EnvGen.kr(Env.dadsr(1,0,0,1,0), 1);

				dustOn = In.kr(dustOnBus);
				vol = In.kr(volBus);
				trigRate = In.kr(trigRateBus);
				mode = In.kr(modeBus);

				chain = FFT([buffer0,buffer1], audioIn);

				trig1 = 1-Trig1.kr(Dust.kr(trigRate, 0.1), 0.02);

				trig = triggerOnce+(trig1*dustOn);

				chain = PV_Freeze(chain, trig);

				outSig = IFFT(chain);

				outSig = Compander.ar(outSig, outSig, 0.5, 1, 0.5, 0.01, 0.01);

				switch0 = Lag.kr(Select.kr(mode, [1, 0]), 0.01);
				switch1 = Lag.kr(Select.kr(mode, [0, 1]), 0.01);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);

				Out.ar(outBus, ((audioIn*switch0)+(outSig*switch1*vol))*pauseEnv*env);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("TFreeze",Rect(946, 618, 130, 330));
		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus(8);

		dustOnBus = Bus.control(group.server);
		volBus = Bus.control(group.server);
		modeBus = Bus.control(group.server);
		trigRateBus = Bus.control(group.server);

		trigRateBus.set(1);
		dustOnBus.set(0);
		volBus.set(1);
		modeBus.set(0);

		synths = List.newClear(4);

		buffers = List.new;
		//8.do{buffers.add(Buffer.alloc(group.server, 1024, 1))};
		2.do{buffers.add(Buffer.alloc(group.server, 2048, 1))};
		levelBus = Bus.control(group.server, 1);

		controls.add(Button()
			.states_([
				["Pass", Color.red, Color.black],
				["Pass", Color.black, Color.green]
			])
			.action = {arg butt;
				modeBus.set(0);
				butt.value_(1);
				controls[1].value = 0;
				controls[2].value = 0;
				synths[0].set(\triggerOnce, -0.5);
		});
		this.addAssignButton(0,\onOff);

		controls.add(Button()
			.states_([
				["Trig", Color.red, Color.black],
				["Trig", Color.black, Color.green]
			])
			.action = {arg butt;
				modeBus.set(1);
				synths[0].set(\triggerOnce, 0.5);
				dustOnBus.set(0);
				butt.value_(1);
				controls[0].value = 0;
				controls[2].value = 0;
		});
		this.addAssignButton(1,\onOff);

		controls.add(Button()
			.states_([
				["Dust", Color.red, Color.black],
				["Dust", Color.black, Color.green]
			])
			.action = {arg butt;
				modeBus.set(1);
				dustOnBus.set(1);
				butt.value_(1);
				controls[0].value = 0;
				controls[1].value = 0;
				synths[0].set(\triggerOnce, -0.5);
		});
		this.addAssignButton(2,\onOff);

		controls.add(QtEZSlider("Amp", ControlSpec(0.001, 2, \amp),
			{arg slider;
				volBus.set(slider.value);
		}, 0, true, \vert));
		this.addAssignButton(3, \continuous, Rect(0, 260, 40, 20));

		controls.add(QtEZSlider("TrigRate", ControlSpec(0.5, 2.0), {arg slider;
			trigRateBus.set(slider.value);
		}, 1, true, \vert));
		this.addAssignButton(4, \continuous);

		controls.add(Button()
		.states_([["reset bufs", Color.black, Color.white]])
		.action_({|butt|
			buffers.do{arg item; item.zero};
		}));

		//multichannel button
		numChannels = 2;

		rout = Routine( {
			group.server.sync;
			2.0.wait;
			group.server.sync;
			0.5.wait;
			synths.put(0, Synth("tFreeze2_mod", [\inBus, mixerToSynthBus, \outBus, outBus,  \modeBus, modeBus, \trigRateBus, trigRateBus, \dustOnBus, dustOnBus, \volBus, volBus, \t_trig, 0, \buffer0, buffers[0].bufnum, \buffer1, buffers[1].bufnum], group));
			controls[0].valueAction_(1);
		});

		AppClock.play(rout);

		win.layout_(
			VLayout(
				HLayout(
					VLayout(controls[0], controls[1], controls[2]),
					VLayout(assignButtons[0], assignButtons[1], assignButtons[2])
				),
				HLayout(
					VLayout(controls[3], assignButtons[3]),
					VLayout(controls[4], assignButtons[4])
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;
	}

	killMeSpecial {
		buffers.do{arg item; item.free};
	}
}