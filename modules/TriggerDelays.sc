TriggerDelays_Mod : Module_Mod {
	var delLow, delHi, rangeSliders, decayLo, decayHi;

	*initClass {
		StartUp.add {
			SynthDef("triggerDelays_mod", {arg input, outBus, trigDur, decayTime;
				var in, bigEnv, lilEnv, out, trig, shift;

				in = In.ar(input, 2);

				trig = Trig.kr(1, trigDur);
				lilEnv = EnvGen.kr(Env.asr(0.05,1,0.05), trig);

				bigEnv = EnvGen.kr(Env.new([0,4,4,0],[0.05, decayTime, 0.3]), doneAction:2);

				shift = (LFNoise2.kr(Rand(0.1, 0.4), 0.5)*Line.kr(0, 1, decayTime/4))+1;

				out = CombC.ar(in*lilEnv, 3, trigDur, decayTime);

				//out = PitchShift.ar(out, 0.2, shift, 0.01);

				Out.ar(outBus, out*bigEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("TriggerDelays",Rect(680, 421, 110, 207));

		rangeSliders = List.new;

		this.initControlsAndSynths(1);

		this.makeMixerToSynthBus(8);

		synths = List.newClear(4);

		rangeSliders.add(EZRanger.new(win,Rect(5, 5, 30, 180), "del", ControlSpec(0.05, 1.5, 'exponential'),
			{|v|
				v = v.value;
				delLow = v[0];
				delHi = v[1];
			}, [0.1,0.8], true, layout:\vert));
		rangeSliders.add(EZRanger.new(win,Rect(40, 5, 30, 180), "dec", ControlSpec(10, 30, 'linear'),
			{|v|
				v = v.value;
				decayLo = v[0];
				decayHi = v[1];
			}, [20,30], true, layout:\vert));
		controls.add(Button.new(win,Rect(75, 10, 30, 80))
			.states_([[ "T", Color.black, Color.red ], [ "T", Color.red, Color.black ]])
			.action_{|v|
				switch(numChannels,
					2,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					},
					4,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+2, \outBus, outBus.index+2, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					},
					8,{
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index, \outBus, outBus, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+2, \outBus, outBus.index+2, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+4, \outBus, outBus.index+4, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
						Synth("triggerDelays_mod", [\input, mixerToSynthBus.index+6, \outBus, outBus.index+6, \trigDur, rrand(delLow, delHi), \decayTime, rrand(decayLo, decayHi)], group);
					}
				)
			});
		this.addAssignButton(0,\onOff, Rect(75, 90, 30, 80));

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(10, 190, 60, 20))
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


	}

	pause {

	}

	unpause{

	}

/*	save {arg xmlDoc;
		xmlSynth = xmlDoc.createElement(modName);
		mantaData.do{arg item, i;
			xmlSynth.setAttribute("manta"++i.asString, item.asString);
		};
		midiData.do{arg item, i;
			xmlSynth.setAttribute("midi"++i.asString, item.asString);
		};
		xmlSynth.setAttribute("delLo", rangeSliders[0].lo.asString);
		xmlSynth.setAttribute("delHi", rangeSliders[0].hi.asString);
		xmlSynth.setAttribute("decLo", rangeSliders[1].lo.asString);
		xmlSynth.setAttribute("decHi", rangeSliders[1].hi.asString);
		xmlSynth.setAttribute("bounds", win.bounds.asString);
		xmlSynth.setAttribute("numChannels", numChannels.asString);

		^xmlSynth;
	}

	load {arg xmlSynth;
		this.loadControllers(xmlSynth);

		rangeSliders[0].lo_(xmlSynth.getAttribute("delLo").interpret);
		rangeSliders[0].hi_(xmlSynth.getAttribute("delHi").interpret);
		rangeSliders[1].lo_(xmlSynth.getAttribute("decLo").interpret);
		rangeSliders[1].hi_(xmlSynth.getAttribute("decHi").interpret);

		//well, it is a power of 2 right?
		controls[1].valueAction_(xmlSynth.getAttribute("numChannels").interpret.log2-1);

		win.bounds_(xmlSynth.getAttribute("bounds").interpret);
		win.front;
	}*/

}