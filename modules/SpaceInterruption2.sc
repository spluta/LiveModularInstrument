SpaceInterruption_Mod : SignalSwitcher_Mod {
	//modified from Alik Rustamoff's implementation of the Lexicon reverb

	var volBus, djBus, synthSeq, currentSynth, buttonVal, decayRange;

	*initClass {
		StartUp.add {
			SynthDef("spaceInterruption2_mod", { arg
				predelay = 0.0,
				input_diff_1 = 0.78512552440328,
				input_diff_2 = 0.4613229053115,
				bandwidth = 0.96,		// input bandwidth - randomize
				decay = 0.9,	// tank decay - 0.8 to 1ish
				decay_diff_1 = 0.4,
				decay_diff_2 = 0.8,
				damping = 0.57; // tank bandwidth - random 0.2 to 0.8


				var src, in0, in1, input, local;
				var input_diff = [ input_diff_1, input_diff_2 ];
				var dltimes;
				var tank0, tank1, tankdelays0, tankdelays1, outdelaysL, outdelaysR;
				var n_out_0, n_out_1, n_out_2, n_out_3, n_out_4, n_out_5, n_out_6;
				var djSlider, sound, vol, impulse;


				var decayVal = Lag.kr(decay, \decayLag.kr(0.6));

				djSlider = In.kr(\djBus.kr);

				in0 = In.ar(\inBus0.kr, 2);
				in1 = In.ar(\inBus1.kr, 2);

				src = (in0*(1-djSlider)+(in1*djSlider))*\inVol.kr(1, 0.01);

				input = Integrator.ar(
					DelayC.ar(src.mean * bandwidth, 0.08, predelay),
					coef: 1 - bandwidth
				);

				dltimes = [ 0.0047713450488895, 0.0035953092974026, 0.012734787137529, 0.0093074829474816 ];

				dltimes.do { |it i|
					input = AllpassN.ar(input, it, it, decaytime: input_diff[ i.trunc(2) / 2 ]);
				};
				///////////////////////////////////Tank///////////

				tankdelays0 = [ 0.022579886428547, 0.1496253486106, 0.060481838647895, 0.12499579987232 ];

				tankdelays1 = [ 0.030509727495716, 0.14169550754343, 0.089244313027116, 0.10628003091294 ];

				local = LocalIn.ar(2);

				////////////////////////// 0 //////////////////

				n_out_1 = AllpassL.ar(input + local[1], 0.4, (tankdelays0[0] + LFNoise2.ar(0.7,mul:0.00025)), decaytime: decay_diff_1);

				tank0 = DelayC.ar(n_out_1,tankdelays0[1],tankdelays0[1]);

				// n_out_2 = OnePole.ar(tank0, 1 - damping);
				n_out_2	= Integrator.ar(tank0 * ( 1 - damping ), damping) * decayVal;

				n_out_3 = AllpassL.ar(n_out_2, tankdelays0[2], tankdelays0[2], decaytime: decay_diff_2);

				tank0 = DelayC.ar(n_out_3, tankdelays0[3],tankdelays0[3] - ControlDur.ir) * decayVal;

				////////////////////////////// 1 ///////////////

				n_out_4 = AllpassL.ar(input + local[0],0.4,(tankdelays1[0] + LFNoise2.kr(0.71, mul:0.00018)),decaytime: decay_diff_1);

				tank1 = DelayC.ar(n_out_4,tankdelays1[1],tankdelays1[1]);

				n_out_5	= Integrator.ar(tank1 * ( 1 - damping ), damping) * decayVal;

				n_out_6 = AllpassL.ar(n_out_5, tankdelays1[2], tankdelays1[2], decaytime: decay_diff_2);

				tank1 = DelayC.ar(n_out_6, tankdelays1[3], tankdelays1[3] - ControlDur.ir) * decayVal;

				LocalOut.ar([tank0 * -1, tank1 * -1]);

				outdelaysL = [ 0.0089378717113, 0.099929437854911, 0.064278754074124, 0.067067638856221, 0.066866032727395, 0.0062833910150869, 0.035818688888142 ];

				outdelaysR = [ 0.011861160579282, 0.12187090487551, 0.041262054366453, 0.089815530392124, 0.070931756325392, 0.011256342192803, 0.0040657235979974 ];


				sound = [
						Mix(DelayN.ar([n_out_4, n_out_4, n_out_5, n_out_6, n_out_1, n_out_2, n_out_3], outdelaysL, 0.2, [1,1,-1,1,-1,-1,-1])),
						Mix(DelayN.ar([n_out_1, n_out_1, n_out_2, n_out_3, n_out_4, n_out_5, n_out_6], outdelaysR, 0.2, [1,1,-1,1,-1,-1,-1]))
				];


				vol = In.kr(\volBus.kr);

				impulse = SelectX.kr(\outSelect.kr(0, 0.01), [0, SelectX.kr(\impulseOn.kr(0), [1, 1-(PulseCount.kr(ImpulseB.kr(\impulseSpeed.kr(1), \impulseOn.kr), \impulseOn.kr)%2)])]);

				sound = ((sound*impulse*vol)+(in0*(1-impulse)));

				Out.ar(\outBus.kr, sound*\isCurrent.kr(0, 0.1));
				//Out.ar(outBus, sound);

			}).writeDefFile;
		}
	}

	init3 {
		"make me".postln;
		synthName = "SpaceInterruption2";

		win.name = "SpaceInterruption2"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
		this.initControlsAndSynths(6);

		volBus = Bus.control(group.server);
		djBus = Bus.control(group.server);

		decayRange = [0.8,0.9];

		4.do{synths.add(Synth("spaceInterruption2_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus, \volBus, volBus, \djBus, djBus, \input_diff_1, rrand(0.4,0.9), \input_diff_2, rrand(0.4,0.9)], outGroup))};

		synthSeq = Pseq((0..3), inf).asStream;
		currentSynth = synthSeq.next;

		3.do{controls.add(Button.new())};
		buttonVal = -1;
		RadioButtons(controls,
			[[["pass", Color.blue, Color.black ],["pass", Color.black, Color.red ]], [["on", Color.blue, Color.black ],["on", Color.black, Color.red ]], [["impulse", Color.blue, Color.black ],["impulse", Color.black, Color.red ]]],
			Array.fill(3, {|i| {this.doAction(i)}}),
			0, true);

		synths[currentSynth].set(\isCurrent, 1);

		this.addAssignButton(0, \onOff);
		this.addAssignButton(1, \onOff);
		this.addAssignButton(2, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'amp'), {|val| volBus.set(val.value)}, 0, true, 'horz'));
		this.addAssignButton(3, \continuous);

		//start me in the off position
		controls[0].value = 1;

		controls.add(QtEZSlider(nil, ControlSpec(0,1), {|val| djBus.set(val.value)}, 0, true, 'horz', false));
		this.addAssignButton(4, \continuous);

		controls.add(QtEZRanger(\decay, ControlSpec(0,1.5), {|val|
			decayRange = val.value;
		}, [0.8, 0.9], true, 'horz'));

		win.layout_(
			HLayout(
				VLayout(
					HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
					HLayout(controls[4], assignButtons[4].maxWidth_(40)),
					HLayout(controls[0].maxHeight_(15), controls[1].maxHeight_(15), controls[2].maxHeight_(15)),
					HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),
					HLayout(controls[3],assignButtons[3]),
					controls[5];
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	doAction {|num|
		var decay = rrand(decayRange[0], decayRange[1]);
		"doAction".postln; num.postln;
		switch(num,
			0, {
				var temp;

				if(buttonVal!=0){
					temp = currentSynth;
					synths[temp].set(\outSelect, 0, \inVol, 1, \delaysMul, 0, \delaysMulLag, 0, \impulseOn, 0, \decay, 0.1, \damping, rrand(0.1,0.7));
					SystemClock.sched(1, {synths[temp].set(\decayLag, 0.2)});
				};
				buttonVal = 0;
			},
			1, {
				if(buttonVal!=1){
					synths[currentSynth].set(\inVol, 1, \isCurrent, 0);
					currentSynth = synthSeq.next;
					synths[currentSynth].set(\outSelect, 1, \inVol, 0, \decay, decay, \decayLag, 0.6, \impulseOn, 0, \isCurrent, 1);
				};
				buttonVal = 1;
			},
			2, {
				if(buttonVal!=2){
					synths[currentSynth].set(\inVol, 1, \isCurrent, 0);
					currentSynth = synthSeq.next;
					synths[currentSynth].set(\outSelect, 1, \inVol, 0, \decay, decay, \decayLag, 0.6, \impulseOn, 1, \impulseSpeed, rrand(5,20), \isCurrent, 1);
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