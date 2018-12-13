Maths_Main {
	var <>synths, <>group, <>localBusses, <>outBus, <>controls, <>trigUpDown, <>layout;

	var controlsStartSize, synth, temp0, temp1, garbageBus, texts;

	*new {arg synths, group, localBusses, outBus, controls, trigUpDown, layout;
		^super.new.synths_(synths).group_(group).localBusses_(localBusses).outBus_(outBus).controls_(controls).trigUpDown_(trigUpDown).layout_(layout).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("mathsMain_analogMod", {arg
				signalBus, signalPlugged, attenuvertorVal,
				trigBus, trigBusPlugged, cycleOnButton = 0, cycleOnBus, cycleOnBusPlugged,
				riseBus, riseInVal=0, riseBusPlugged=0, fallBus, fallInVal=0, fallBusPlugged=0, linkedDurBus, linkedDurBusPlugged,
				shapeBus, shapeVal = 0.5,
				trigOutBus, trigUpDown=0, unityOutBus, outBus, gate=1, pauseGate = 1, localPauseGate = 1;

				var trigIn, trigger, shape,
				riseIn, upDur, fallIn, downDur, linkedDur,
				signalIn, control, slew, cycle, cycleOn;

				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				signalIn = InFeedback.ar(signalBus)*attenuvertorVal;

				riseIn = Select.ar(riseBusPlugged, [Lag.ar(K2A.ar(riseInVal)), InFeedback.ar(riseBus)]).fold(0,1);  //folding the input to 0 to 1, not sure if that is correct

				fallIn = Select.ar(fallBusPlugged, [Lag.ar(K2A.ar(fallInVal)), InFeedback.ar(fallBus)]).fold(0,1);

				linkedDur = InFeedback.ar(linkedDurBus).fold(0,1);

				upDur = Select.ar(linkedDurBusPlugged, [riseIn, linkedDur]).clip(0,1).linexp(0, 1, 0.0005, 25); //if the linked input is engaged it overrides the riseIn bus
				//upDur = riseIn.squared*100;

				downDur = Select.ar(linkedDurBusPlugged, [fallIn, linkedDur]).clip(0,1).linexp(0, 1, 0.0005, 25);
				//downDur = fallIn.squared*100;

				//if the cycle is on, cycle
				cycleOn = Select.ar(cycleOnBusPlugged, [K2A.ar(1), InFeedback.ar(cycleOnBus)]);
				cycleOn = Lag.ar((cycleOnButton*cycleOn).clip(0,1), 0.05);
				cycle = LFPulse.ar((1/(upDur+downDur)), 0, (upDur/(upDur+downDur)))*cycleOn;

				//check the trigger in for some trigger action
				trigIn = Select.ar(trigBusPlugged, [K2A.ar(0), InFeedback.ar(trigBus)]);

				trigger = Trig1.ar(trigIn, upDur);

				control = cycle + trigger + signalIn;

				slew = Slew.ar(control, 1/upDur, 1/downDur);

				//original Maths does not have signal in shape control
				//shape = (InFeedback.ar(shapeBus)+shapeVal).fold(0,1);
				slew = SignalBend.ar(slew, Lag.kr(shapeVal));

				control = Select.ar(trigUpDown, [control, 1/control]);

				Out.ar(unityOutBus, Select.ar(cycleOn, [Amplitude.ar(signalIn), slew]));
				Out.ar(trigOutBus, Trig1.ar(control, 0.01));
				Out.ar(outBus, slew*Select.kr(signalPlugged, [attenuvertorVal, 1]));//if there is a signal in, don't attenuvert the output

			}).writeDefFile;
		}
	}

	makePlugIn {arg synth, busName, pluggedName;
		/*controls.add(PopUpMenu().items_(["nil"].addAll((1..(localBusses.size-6)))).action_({arg menu;
			temp0 = menu.value;
			if(menu.value == 0, {temp1 = 0},{temp1 = 1});
			synth.set(busName, localBusses[temp0], pluggedName, temp1);
		}));*/
		controls.add(NumberBox().clipLo_(0).clipHi_(localBusses.size-6).action_({
			arg numb;
			temp0 = numb.value;
			if(numb.value == 0, {temp1 = 0},{temp1 = 1});
			synth.set(busName, localBusses[temp0], pluggedName, temp1);
			numb.alpha_(linlin(0,localBusses.size-6,1,0));
		}).maxWidth_(50).background_(Color.yellow));
	}

	makePlugOut {arg synth, busName;
		controls.add(PopUpMenu().items_(["nil"].addAll((1..(localBusses.size-2)))).action_({arg menu;
			temp0 = menu.value;
			if(temp0==0, {synth.set(busName, garbageBus)},{synth.set(busName, localBusses[temp0])});
		}));
	}

	init {
		//synths = List.newClear;
		garbageBus = localBusses[localBusses.size-1];
		controlsStartSize = controls.size;

		synth = Synth("mathsMain_analogMod", [\signalBus, localBusses[0], \signalPlugged, 0, \attenuvertorVal, 0,	\trigBus, localBusses[0], \trigBusPlugged, 0, \cycleOnButton, 0, \cycleOnBus, localBusses[0], \cycleOnBusPlugged, 0, \riseBus, localBusses[0], \fallBus, localBusses[0], \linkedDurBus, localBusses[0], \linkedDurBusPlugged, 0, \shapeBus, localBusses[0], \shapeVal, 0.5, \trigOutBus, garbageBus, \trigUpDown, trigUpDown, \unityOutBus, garbageBus, \outBus, outBus], group);

		synths.add(synth);

		texts = ["SignalIn", "TrigIn", "CycleIn", "Rise", "Fall", "Both", "TrigOut", "UnityOut", "Out"].collect{arg item; StaticText().string_(item)};

		this.makePlugIn(synth, \signalBus, \signalPlugged);

		this.makePlugIn(synth, \trigBus, \trigBusPlugged);

		controls.add(Button()
			.states_([["CycleOff", Color.black, Color.red], ["CycleOn", Color.black, Color.green]])
			.action_({arg butt;
				synth.set(\cycleOnButton, butt.value);
		}));

		this.makePlugIn(synth, \cycleOnBus, \cycleOnBusPlugged);

		controls.add(QtEZSlider("Rise", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synth.set(\riseInVal, slider.value);
		}, 0, true, \horz));

		this.makePlugIn(synth, \riseBus, \riseBusPlugged);

		controls.add(QtEZSlider("Fall", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synth.set(\fallInVal, slider.value);
		}, 0, true, \horz));

		this.makePlugIn(synth, \fallBus, \fallBusPlugged);
		this.makePlugIn(synth, \linkedDurBus, \linkedDurBusPlugged);

		controls.add(QtEZSlider("shape", ControlSpec(0, 1, 'lin', 0.001), {arg slider;
			synth.set(\shapeVal, slider.value);
		}, 0.5, true, \horz));

		controls.add(QtEZSlider("attenuverter", ControlSpec(0, 1, 'lin'), {arg slider;
			synth.set(\attenuvertorVal, slider.value);
		}, 0, true, \horz));

		this.makePlugOut(synth, \trigOutBus);
		this.makePlugOut(synth, \unityOutBus);
		this.makePlugOut(synth, \outBus);

		if (trigUpDown==0, {layout.add(HLayout(StaticText().string_("1 - Controls")))}, {layout.add(HLayout(StaticText().string_("4 - Controls")))});

		layout.add(HLayout(controls[controlsStartSize], texts[0], controls[controlsStartSize+1], texts[1]));
		layout.add(HLayout(controls[controlsStartSize+2], controls[controlsStartSize+3], texts[2]));
		layout.add(HLayout(controls[controlsStartSize+4].layout, controls[controlsStartSize+5], texts[3]));
		layout.add(HLayout(controls[controlsStartSize+6].layout, controls[controlsStartSize+7], texts[4]));
		layout.add(HLayout(controls[controlsStartSize+8], texts[5], controls[controlsStartSize+9].layout));
		layout.add(HLayout(controls[controlsStartSize+10].layout));
		layout.add(HLayout(controls[controlsStartSize+11], texts[6], controls[controlsStartSize+12], texts[7], controls[controlsStartSize+13], texts[8]));
	}
}


Maths_AnalogMod : AnalogModule_Mod {
	//in order to make this function with audio inputs, I probably need to inherit from SignalSwitcher_Mod
	var synthGroup, mixerGroup;
	var mathsMains, mainLayouts;
	var outBusses, controlsStartSize;
	var signal2Bus, signal3Bus, outBus2, outBus3, signal2BusEngaged, signal3BusEngaged;
	var orOutBus, sumOutBus, invOutBus;

	*initClass {
		StartUp.add {

			SynthDef("maths23_analogMod", {arg signalBus, signalPlugged, attenuvertorVal, voltageVal, outBus, mixerBus, mixerBusOn, gate=1, pauseGate = 1, localPauseGate = 1;
				var signal, out;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				signal = InFeedback.ar(signalBus)*attenuvertorVal;

				out = Select.ar(signalPlugged, [K2A.ar(attenuvertorVal/2+0.5).clip(0,1), signal]);

				Out.ar(outBus, out);
				//Out.ar(mixerBus, out*mixerBusOn);

			}).writeDefFile;

			SynthDef("mathsMixer_analogMod", {
				arg signal0Bus, signal1Bus, signal2Bus, signal3Bus,
				orOutBus, sumOutBus, invOutBus, gate=1, pauseGate = 1, localPauseGate = 1;

				var sig0, sig1, sig2, sig3, orOut, sumOut, invOut;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), localPauseGate, doneAction:1);

				sig0 = In.ar(signal0Bus);
				sig1 = In.ar(signal1Bus);
				sig2 = In.ar(signal2Bus);
				sig3 = In.ar(signal3Bus);

				orOut = max(sig0, max(sig1, max(sig2, sig3)));
				sumOut = sig0+sig1+sig2+sig3;

				Out.ar(orOutBus, orOut);
				Out.ar(sumOutBus, sumOut);
				Out.ar(invOutBus, sumOut.neg);
			}).writeDefFile;
		}
	}

	init {

		this.initAnalogBusses;

		this.makeWindow("Maths",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(44);

		synthGroup = Group.tail(group);
		mixerGroup = Group.tail(group);

		mainLayouts = Array.fill(2, {VLayout()});
		outBusses = Array.fill(4, {Bus.audio(group.server)});

		mathsMains = Array.fill(2, {arg i;
			Maths_Main(synths, synthGroup, localBusses, outBusses[[0,3].at(i)], controls, i, mainLayouts[i]);
		});

		2.do{arg i; synths.add(Synth("maths23_analogMod", [\signalBus, localBusses[0], \signalPlugged, 0, \attenuvertorVal, 0, \outBus, outBusses[i+1]], synthGroup))};

		synths.add(Synth("mathsMixer_analogMod", [\signal0Bus, outBusses[0], \signal1Bus, outBusses[1], \signal2Bus, outBusses[2], \signal3Bus, outBusses[3], 	\orOutBus, garbageBus, \sumOutBus, garbageBus, \invOutBus, garbageBus], mixerGroup));

		controlsStartSize = controls.size;

		texts = ["In2", "Out2", "In3", "Out3", "Or", "Sum", "Inv"].collect{arg item; StaticText().string_(item)};

		this.makePlugIn(synths[2], \signalBus, \signalPlugged);

		controls.add(QtEZSlider("att2", ControlSpec(0, 1, 'lin'), {arg slider;
			synths[2].set(\attenuvertorVal, slider.value);
		}, 0, true, \horz));

		controls.add(PopUpMenu().items_(["nil"].addAll((1..(localBusses.size-2)))).action_({arg menu;
			temp0 = menu.value;
			if(temp0==0, {synths[2].set(\outBus, outBusses[1])},{synths[2].set(\outBus, localBusses[temp0])});
		}));

		this.makePlugIn(synths[3], \signalBus, \signalPlugged);

		controls.add(QtEZSlider("att3", ControlSpec(0, 1, 'lin'), {arg slider;
			synths[3].set(\attenuvertorVal, slider.value);
		}, 0, true, \horz));

		controls.add(PopUpMenu().items_(["nil"].addAll((1..(localBusses.size-2)))).action_({arg menu;
			temp0 = menu.value;
			if(temp0==0, {synths[3].set(\outBus, outBusses[2])},{synths[3].set(\outBus, localBusses[temp0])});
		}));

		this.makePlugOut(synths[2], \orOutBus);
		this.makePlugOut(synths[2], \sumOutBus);
		this.makePlugOut(synths[2], \invOutBus);

		win.layout = VLayout(
			HLayout(*mainLayouts),
			HLayout(controls[controlsStartSize], texts[0], controls[controlsStartSize+1].layout, controls[controlsStartSize+2], texts[1],
				controls[controlsStartSize+3], texts[2], controls[controlsStartSize+4].layout, controls[controlsStartSize+5], texts[3]),
			HLayout(controls[controlsStartSize+6], texts[4], controls[controlsStartSize+7], texts[5], controls[controlsStartSize+8], texts[6])
		);
	}
}
