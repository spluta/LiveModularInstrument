SpaceInterruption_Mod : SignalSwitcher_Mod {
	var volBus, djBus, synthSeq, currentSynth, buttonVal;

	*initClass {
		StartUp.add {
			SynthDef("spaceInterruption_mod", {
				var in0, in1, soundIn, sound, er, allPass, delays, ins, impulse, vol, djSlider;

				vol = In.kr(\volBus.kr);
				djSlider = In.kr(\djBus.kr);

				in0 = In.ar(\inBus0.kr, 2);
				in1 = In.ar(\inBus1.kr, 2);

				soundIn = (in0*(1-djSlider)+(in1*djSlider))*\inVol.kr(1, 0.01);

				delays = [ 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691 ].linlin(503, 691, 0.01, 0.05).scramble.copyRange(0,15);

				er = Array.fill(16, {|i| CombC.ar(soundIn[(i>7).asInteger], 1, delays[i], Rand(0.05,0.1), LFNoise2.kr(LFNoise2.kr(0.5).range(0.1, 0.2)).range(0.5,1))});

				er = BHiShelf.ar(Splay.ar(er.copyRange(0,7), 0.5, 1, -0.5)+Splay.ar(er.copyRange(8,15), 0.5, 1, 0.5), 3000, 1, -12);

				sound = soundIn+(er);

				sound = AllpassC.ar(AllpassC.ar(AllpassC.ar(AllpassC.ar(sound, 0.15, {(0.1*rrand(0.93,1.07))}!2, 2), 0.05, {(0.1/3*rrand(0.93,1.07))}!2, 2/3), 0.05, {(0.1/9*rrand(0.93,1.07))}!2, 2/9), 0.1, {(0.1/27*rrand(0.93,1.07))}!2, 2/27);

				delays = [ 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953 ].linlin(751, 953, 0.02, 0.5).scramble.copyRange(0,19);

				sound = Array.fill(delays.size, {|i| CombC.ar(sound[i%2], 1, delays[i], \delaysMul.kr(1, \delaysMulLag.kr(0.1))*rrand(10,20), LFNoise2.kr(LFNoise2.kr(0.5).range(0.1, 0.2)).range(0.5,1) )}).clump(delays.size/2);

				impulse = SelectX.kr(\outSelect.kr(0, 0.01), [0, SelectX.kr(\impulseOn.kr, [1, 1-(PulseCount.kr(ImpulseB.kr(\impulseSpeed.kr(1), \impulseOn.kr(0)), \impulseOn.kr)%2)])]);

				sound = (sound*impulse*vol)+(in0*(1-impulse));

				Out.ar(\outBus.kr, sound*\isCurrent.kr(0));
			}).writeDefFile;
		}
	}

	init3 {
		synthName = "SpaceInterruption";

		win.name = "SpaceInterruption"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
		this.initControlsAndSynths(5);

		volBus = Bus.control(group.server);
		djBus = Bus.control(group.server);

		4.do{synths.add(Synth("spaceInterruption_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus, \volBus, volBus, \djBus, djBus], outGroup))};

		synthSeq = Pseq((0..3), inf).asStream;
		currentSynth = synthSeq.next;

		3.do{controls.add(Button.new())};
		buttonVal = -1;
		RadioButtons(controls,
			[[["pass", Color.blue, Color.black ],["pass", Color.black, Color.red ]], [["on", Color.blue, Color.black ],["on", Color.black, Color.red ]], [["impulse", Color.blue, Color.black ],["impulse", Color.black, Color.red ]]],
			Array.fill(3, {|i| {this.doAction(i)}}),
			0, true);

		this.addAssignButton(0, \onOff);
		this.addAssignButton(1, \onOff);
		this.addAssignButton(2, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'amp'), {|val| volBus.set(val.value)}, 0, true, 'horz'));
		this.addAssignButton(3, \continuous);

		//start me in the off position
		controls[0].value = 1;

		controls.add(QtEZSlider(nil, ControlSpec(0,1), {|val| djBus.set(val.value)}, 0, true, 'horz', false));
		this.addAssignButton(4, \continuous);

		win.layout_(
			HLayout(
				VLayout(
					HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
					HLayout(controls[4], assignButtons[4].maxWidth_(40)),
					HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15), controls[2].maxHeight_(15)),
					HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),
					HLayout(controls[3],assignButtons[3])
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	doAction {|num|
		switch(num,
			0, {
				var temp;
				if(buttonVal!=0){
					temp = currentSynth;
					synths[temp].set(\outSelect, 0, \inVol, 1, \delaysMul, 0, \delaysMulLag, 0, \impulseOn, 0);
					SystemClock.sched(1, {synths[temp].set(\delaysMul, 0.2)});
				};
				buttonVal = 0;
			},
			1, {
				if(buttonVal!=1){
					synths[currentSynth].set(\inVol, 1, \isCurrent, 0);
					currentSynth = synthSeq.next;
					synths[currentSynth].set(\outSelect, 1, \inVol, 0, \delaysMul, 1, \delaysMulLag, 0.6, \impulseOn, 0, \isCurrent, 1);
				};
				buttonVal = 1;
			},
			2, {
				if(buttonVal!=2){
					synths[currentSynth].set(\inVol, 1, \isCurrent, 0);
					currentSynth = synthSeq.next;
					synths[currentSynth].set(\outSelect, 1, \inVol, 0, \delaysMul, 1, \delaysMulLag, 0.6, \impulseOn, 1, \impulseSpeed, rrand(5,20), \isCurrent, 1);
				};
				buttonVal = 2;
			}
		)
	}

	killMeSpecial {
		volBus.free;
		djBus.free;
	}
}