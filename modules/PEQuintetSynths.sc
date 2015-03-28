PESequencer_Mod : Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("peSequencer_mod", {arg outBus, volume, fund, offOn, speed, gate = 1, pauseGate = 1;
				var a, freq, trig, trig2, smallEnv, out0, out1, seq, verb, out, smallEnv2, noise, env, pauseEnv;
				seq =
					Dswitch1([
						Dswitch1([
							Dseq([9, 13, 11, 7, 9, 13, 5, 7, 11, 17/2, 15/2, 13/2]/2, inf),
							Dseq([9, 13, 11, 7, 9, 13, 5, 7, 11, 17/2, 15/2, 13/2]*2, inf),
							Dseq([9, 13, 11, 7, 9, 13, 5, 7, 11, 17/2, 15/2, 13/2]*4, inf)
						], Dwhite(0, 2, inf)),
						Dseq([5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25]*2, inf)
					], Dwhite(0, 1, inf));

				trig = Impulse.kr(LFNoise2.kr(1).range(110/60, 110/60*8))*offOn;
				trig2 = Impulse.kr(110/60*16)*offOn;

				trig = Select.kr(speed, [trig, trig2]);

				trig = Trig1.kr(trig, TRand.kr(0.001, Select.kr(speed, [0.05, 0.001]), trig));

				freq = Demand.kr(trig, 0, seq) ;

				smallEnv = EnvGen.ar(Env.perc(0.01, 0.1), trig);

				smallEnv2 = EnvGen.ar(Env.perc(0.001, 0.01), trig);

				noise = LPF.ar(BrownNoise.ar(0.2)*smallEnv2, TRand.kr(200, 1000, trig));

				out0 = SelectX.ar(LFNoise2.kr(2).range(0, 2).sqrt, [
					SinOsc.ar((freq* fund)+SinOsc.ar((freq-TIRand.kr(1, 4))* fund, 0, SinOsc.kr(0.3).range(1,200))) * 0.1,
					Saw.ar((freq* fund)+SinOsc.ar((freq-TIRand.kr(1, 4))* fund, 0, SinOsc.kr(0.3).range(1,200))) * 0.3]) ;

				out1 = SinOsc.ar((freq* fund*2)+SinOsc.ar((freq-TIRand.kr(1, 4))* fund*2, 0, SinOsc.kr(0.3).range(1,200))) * 0.3 ;

				verb = GVerb.ar(out0+out1, 40, 2, 0.1, 0.1, 15, 0, 0.1, 0.5, 40, 0.1);

				out = Pan2.ar(out0+noise, SinOsc.kr(LFNoise2.kr(0.5).range(0.2, 0.8)))+Pan2.ar(out1, SinOsc.kr(LFNoise2.kr(0.5).range(0.2, 0.8)));

				env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.1,1,0.1), pauseGate, doneAction:1);

				Out.ar(outBus, (verb+out)* smallEnv*pauseEnv*env*volume);

			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("PESequencer", Rect(900, 400, 300, 115));
		this.initControlsAndSynths(4);

		synths = List.newClear(0);

		synths.add(Synth("peSequencer_mod", [\outBus, outBus, \volume, 0, \fund, 40.midicps, \offOn, 0, \speed, 0], group));

		controls.add(Button.new(win,Rect(0, 0, 120, 20))
			.states_([["Off", Color.blue, Color.black ],["On", Color.black, Color.red ]])
			.action_{|v|
				synths[0].set(\offOn, v.value);
			});
		this.addAssignButton(0, \onOff, Rect(0, 20, 120, 20));

		controls.add(Button.new(win,Rect(120, 0, 120, 20))
			.states_([["Slow", Color.blue, Color.black ],["Fast", Color.black, Color.red ]])
			.action_{|v|
				synths[0].set(\speed, v.value);
			});
		this.addAssignButton(1, \onOff, Rect(120, 20, 120, 20));

		controls.add(EZSlider(win, Rect(0, 50, 240, 20), "vol", ControlSpec(0,1,\amp),
			{arg val; synths[0].set(\volume, val.value);}, 0, true));
		this.addAssignButton(2, \continuous, Rect(240, 50, 60, 20));

		controls.add(EZSlider(win, Rect(0, 70, 240, 20), "fund", ControlSpec(30,60,\linear,1),
			{arg val; synths[0].set(\fund, val.value)}, 30, true));
		this.addAssignButton(3, \continuous, Rect(240, 50, 60, 20));


//		//multichannel button
//		numChannels = 2;
//		controls.add(Button(win,Rect(10, 120, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{};
//		);
	}

/*	loadSettings {arg xmlSynth;
		rout = Routine({
			group.server.sync;
			(2..3).do{arg i;
				midiHidTemp = xmlSynth.getAttribute("controls"++i.asString);
				if(midiHidTemp!=nil,{
					controls[i].valueAction_(midiHidTemp.interpret);
				});
			};
		});
		AppClock.play(rout);
	}*/
}

PESynthTone_Mod : Module_Mod {
	var fund, freq, volBus;

	*initClass {
		StartUp.add {
			SynthDef("peSynthTone_mod", {arg outBus, volBus, freq;
				var start, end, attack, sustain, release, env, out, volume;

				volume = In.kr(volBus);

				start = Rand(5, 20);
				end = Rand(40, 200);

				attack = Rand(4, 7);
				sustain = Rand(2, 4);
				release = Rand(3, 7);

				env = EnvGen.kr(Env.new([0,1,1,0], [attack, sustain, release]), 1, doneAction:2);

				out = SinOsc.ar(freq, SinOsc.ar(XLine.kr(start, end, attack+sustain+release), 0, 2pi), 0.3)*env;

				out = Pan2.ar(out, Line.kr(Rand(-1, 1), Rand(-1, 1), attack+sustain+release));

				Out.ar(outBus, out* env*volume);

			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("PESynthTone", Rect(900, 400, 300, 115));
		this.initControlsAndSynths(3);

		volBus = Bus.control(group.server);

		fund = 30;

		controls.add(Button.new(win,Rect(0, 0, 240, 20))
			.states_([["Go", Color.blue, Color.black ],["Go", Color.black, Color.blue ]])
			.action_{|v|
				freq = fund.midicps*([5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25].choose)*[1,2].choose;
				Synth("peSynthTone_mod",[\outBus, outBus, \volBus, volBus.index, \freq, freq]);
			});
		this.addAssignButton(0, \onOff, Rect(0, 20, 120, 20));

		controls.add(EZSlider(win, Rect(0, 50, 240, 20), "vol", ControlSpec(0,1,\amp),
			{arg val; volBus.set(val.value);}, 0, true));
		this.addAssignButton(1, \continuous, Rect(240, 50, 60, 20));

		controls.add(EZSlider(win, Rect(0, 70, 240, 20), "fund", ControlSpec(30,60,\linear,1),
			{arg val; fund=val.value}, 30, true));
		this.addAssignButton(2, \continuous, Rect(240, 70, 60, 20));

//		//multichannel button
//		numChannels = 2;
//		controls.add(Button(win,Rect(10, 120, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{};
//		);

		//start me in the off position
		controls[0].value = 1;
	}

	killMeSpecial {
		volBus.free;
	}

/*	load {arg xmlSynth;
		this.loadMidiData(xmlSynth);
		mantaData.size.do{arg i;
			midiHidTemp = xmlSynth.getAttribute("manta"++i.asString);
			if(midiHidTemp!=nil,{
				midiHidTemp = midiHidTemp.interpret;
				if(midiHidTemp!=nil,{
					this.setManta(midiHidTemp[0], midiHidTemp[1], i);
				});
			});
		};

		rout = Routine({
			group.server.sync;
			(1..2).do{arg i;
				midiHidTemp = xmlSynth.getAttribute("controls"++i.asString);
				if(midiHidTemp!=nil,{
					controls[i].valueAction_(midiHidTemp.interpret);
				});
			};
		});
		AppClock.play(rout);
		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
		win.front;
	}*/
}