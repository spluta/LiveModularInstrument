UpDownSines_Mod : Module_Mod {
	var  envGroup, synthGroup, volBus, synths, sines, currentSine, nextSine, time, startEnd, <>volBus, upDown, lowHi;

	*initClass {
		StartUp.add {
			SynthDef("sineUpDown2_mod", {arg outBus, volBus, startFreq, endFreq, time, gate=1, pauseGate = 1;
				var out, pauseEnv;

				out = SinOsc.ar(XLine.kr(startFreq, endFreq, time), Rand(0,2), 0.1);
				out = Pan2.ar(out, Line.kr(Rand(-1,1), Rand(-1, 1), time));

				out = out*EnvGen.kr(Env.new([0, 1, 1, 0],[time/6, (time/3), time/3], 'sine'), gate, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*In.kr(volBus)*pauseEnv);
			}).writeDefFile;

			SynthDef("sineUpDown4_mod", {arg outBus, volBus, startFreq, endFreq, time, gate=1, pauseGate = 1;
				var out, pauseEnv, lfo;

				out = SinOsc.ar(XLine.kr(startFreq, endFreq, time), Rand(0,2), 0.1);

				lfo = LFSaw.kr(Rand(0.1, 0.3), 2.0.rand);
				out = PanAz.ar(4, out, lfo);

				out = out*EnvGen.kr(Env.new([0, 1, 1, 0],[time/6, (time/3), time/3], 'sine'), gate, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*In.kr(volBus)*pauseEnv);
			}).writeDefFile;

			SynthDef("sineUpDown8_mod", {arg outBus, volBus, startFreq, endFreq, time, gate=1, pauseGate = 1;
				var out, pauseEnv, lfo;

				out = SinOsc.ar(XLine.kr(startFreq, endFreq, time), Rand(0,2), 0.1);

				lfo = LFSaw.kr(Rand(0.1, 0.3), 2.0.rand);
				out = PanAz.ar(8, out, lfo);

				out = out*EnvGen.kr(Env.new([0, 1, 1, 0],[time/6, (time/3), time/3], 'sine'), gate, doneAction:2);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*In.kr(volBus)*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("UpDownSines",Rect(318, 645, 210, 270));
		this.initControlsAndSynths(6);
		this.makeMixerToSynthBus;

		dontLoadControls = ([1]);

		volBus = Bus.control(group.server);

		volBus.set(0);

		synths = Array.newClear(2);
		nextSine = Pseq(#[0,1], inf).asStream;
		currentSine = nextSine.next;

		upDown = 0;
		lowHi = [100, 12500];

		controls.add(QtEZSlider.new("vol", ControlSpec(0,2,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, orientation:\vert));
		this.addAssignButton(0,\continuous);

		controls.add(Button()
			.states_([["Go", Color.black, Color.green],["Go", Color.green, Color.black]])
			.action_({this.sinesOn}));
		this.addAssignButton(1,\onOff);

		controls.add(Button()
			.states_([["Off", Color.black, Color.red],["Off", Color.red, Color.black]])
			.action_({this.sinesOff}));
		this.addAssignButton(2,\onOff);

		controls.add(Button()
			.states_([["Up", Color.black, Color.green],["Down", Color.black, Color.red],["Both", Color.black, Color.blue]])
			.action_({|butt| upDown = butt.value}));
		this.addAssignButton(3,\onOff);

		controls.add(QtEZRanger("range", ControlSpec(100, 12500, 'exponential'),
			{|val|
				lowHi = val.value;
		}, [100,12500], true, orientation:\vert));

		//multichannel button
		numChannels = 2;
		controls.add(Button()
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
		win.layout_(
			VLayout(
				HLayout(
					VLayout(
						controls[0].layout, assignButtons[0].layout
					),
					VLayout(
						controls[1], assignButtons[1].layout, controls[2], assignButtons[2].layout, controls[3], assignButtons[3].layout
					),
					VLayout(
						controls[4].layout
					)
				),
				controls[5]
			)
		);
		win.layout.spacing = 0;
		win.bounds_(Rect(613, 441, 222, 251));
	}

	sinesOn {
		this.sinesOff;
		startEnd = [rrand(lowHi[0]*0.6, lowHi[0]*1.3), rrand(lowHi[1]*0.6, lowHi[1]*1.3)];
		switch(upDown,
			1, {startEnd = startEnd.rotate(1)},
			2, {if(0.5.coin, {startEnd = startEnd.rotate(1)})}
		);

		time = rrand(25, 45);
		sines = List.new;
		switch(numChannels,
			2, { rrand(4, 6).do{sines.add(Synth("sineUpDown2_mod", [\outBus, outBus, \volBus, volBus.index, \startFreq, startEnd[0], \endFreq, startEnd[1]+(startEnd[1]/2).rand2, \time, time, \gate, 1], group))}},
			4, {	rrand(4, 6).do{sines.add(Synth("sineUpDown4_mod", [\outBus, outBus, \volBus, volBus.index, \startFreq, startEnd[0], \endFreq, startEnd[1]+(startEnd[1]/2).rand2, \time, time, \gate, 1], group))}},
			8, {	rrand(4, 6).do{sines.add(Synth("sineUpDown8_mod", [\outBus, outBus, \volBus, volBus.index, \startFreq, startEnd[0], \endFreq, startEnd[1]+(startEnd[1]/2).rand2, \time, time, \gate, 1], group))}}
		);

		synths.put(currentSine, sines);
	}

	sinesOff {
		synths[currentSine].do{arg item; item.set(\gate, rrand(-9.5, -5.5))};
		currentSine = nextSine.next;
	}

	pause {
		synths.do{|item0| item0.do{|item| if(item!=nil, item.set(\pauseGate, 0))}};
	}

	killMe {
		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, setups, item)};
		win.close;
		if(synths!=nil,{
			synths.do{|item0| item0.do{|item| if(item!=nil, item.set(\gate, 0))}};
		});
		mixerToSynthBus.free;
		this.killMeSpecial;
	}

	killMeSpecial {
		volBus.free;
	}

	unpause {
		synths.do{|item0| item0.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})}};
	}
}