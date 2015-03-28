Filters_Mod : Module_Mod {
	var rangerSpecs;

	*initClass {
		StartUp.add {
			SynthDef("filters_mod", {arg inBus, outBus, sweepTime0=10, sweepTime1=10, top=22000, bottom=0, upDown=0, whichOne=0;
				var saw, q, sweepTime, sig, noise0, noise2, in, ampIn, ampSig, out, impulse;

				q = LFNoise2.kr(0.87).range(0.025, 0.075);

				in = In.ar(inBus,8);

				ampIn = Amplitude.ar(in);

				sweepTime = LFNoise2.kr(1).range(sweepTime0, sweepTime1);

				impulse = Impulse.kr(1/sweepTime);

				saw = Phasor.ar(impulse, 1/(sweepTime*SampleRate.ir));

				saw = Select.kr(upDown, [1-saw, saw]);

				saw = LinLin.kr(saw, 0,1, TRand.kr(bottom, bottom+((top-bottom)/8), impulse), TRand.kr(top, top-((top-bottom)/8), impulse));

				noise0 = TRand.kr(bottom, top, impulse);

				noise2 = Lag3.kr(noise0, sweepTime);

				sig = LPF.ar(in, Lag3.kr(Select.kr(whichOne, [20000, saw+(saw*q/2), noise0+(noise0*q/2), noise2+(noise2*q/2)]), 0.1));
				sig = HPF.ar(sig, Lag3.kr(Select.kr(whichOne, [20, saw-(saw*q/2), noise0-(noise0*q/2), noise2-(noise2*q/2)]), 0.1));

				ampSig = Amplitude.ar(sig);

				sig = sig*(ampIn/(ampSig+0.001));

				Out.ar(outBus, sig);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Filters", Rect(900, 500, 200, 220));
		this.initControlsAndSynths(7);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("filters_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		rangerSpecs = List.newClear;
		rangerSpecs.add(Env.new([0,0,0,0.33,0,0.33,1,0.5,1],[1,1,1,1,1,1,1,1].normalizeSum));
		rangerSpecs.add(Env.new([0,0.5,1,0.66,1,0.66,1,1,1],[1,1,1,1,1,1,1,1].normalizeSum));

		controls.add(EZRanger(win, Rect(0, 0, 60, 200), "range", ControlSpec(20,10000, \exponential),
			{arg val; synths[0].set(\bottom, val.value[0], \top, val.value[1])}, [20,10000], true, layout:\vert));
		this.addAssignButton(0, \range, Rect(0, 200, 60, 20));

		controls.add(EZRanger(win, Rect(60, 0, 60, 200), "time", ControlSpec(0.1,5),
			{arg val; synths[0].set(\sweepTime0, val.value[0], \sweepTime1, val.value[1])}, [0.1,5], true, layout:\vert));
		this.addAssignButton(1, \range, Rect(60, 200, 60, 20));

		controls.add(Button.new(win,Rect(140, 0, 60, 20))
			.states_([["Nothin", Color.blue, Color.black ],["Nothin", Color.black, Color.red ]])
			.action_{|v|
				5.do{arg i; controls[i+2].value = 0};
				v.value = 1;
				synths[0].set(\whichOne, 0);
			});
		this.addAssignButton(2, \onOff, Rect(140, 20, 60, 20));

		controls.add(Button.new(win,Rect(140, 40, 60, 20))
			.states_([["BPUp", Color.blue, Color.black ],["BPUp", Color.black, Color.red ]])
			.action_{|v|
				5.do{arg i; controls[i+2].value = 0};
				v.value = 1;
				synths[0].set(\whichOne, 1, \upDown, 1);
			});
		this.addAssignButton(3, \onOff, Rect(140, 60, 60, 20));

		controls.add(Button.new(win,Rect(140, 80, 60, 20))
			.states_([["BPDown", Color.blue, Color.black ],["BPDown", Color.black, Color.red ]])
			.action_{|v|
				5.do{arg i; controls[i+2].value = 0};
				v.value = 1;
				synths[0].set(\whichOne, 1, \upDown, -1);
			});
		this.addAssignButton(4, \onOff, Rect(140, 100, 60, 20));

		controls.add(Button.new(win,Rect(140, 120, 60, 20))
			.states_([["BPNoise0", Color.blue, Color.black ],["BPNoise0", Color.black, Color.red ]])
			.action_{|v|
				5.do{arg i; controls[i+2].value = 0};
				v.value = 1;
				synths[0].set(\whichOne, 2);
			});
		this.addAssignButton(5, \onOff, Rect(140, 140, 60, 20));

		controls.add(Button.new(win,Rect(140, 160, 60, 20))
			.states_([["BPNoise2", Color.blue, Color.black ],["BPNoise2", Color.black, Color.red ]])
			.action_{|v|
				5.do{arg i; controls[i+2].value = 0};
				v.value = 1;
				synths[0].set(\whichOne, 2);
			});
		this.addAssignButton(6, \onOff, Rect(140, 180, 60, 20));
	}

	//i have to override this function because i have to deal with the 1 slider for the EZRanger issue
	//this won't work anymore
/*	setMantaForSetup {arg setup, item, i;
		[setup, item, i].postln;
		if(item!=nil,{
			switch(item[1],
				0,{
					Manta.addNoteOnSetup(setup.asSymbol, item[0], {|val| {controls[i].valueAction_(controls[i].value+1)}.defer});
				},
				1,{
					Manta.addPadSetup(setup.asSymbol, item[0], {|val| {controls[i].valueAction_(controls[i].controlSpec.map(val/180))}.defer});
				},
				2,{
					Manta.addSliderSetup(setup.asSymbol, item[0], {|val|
						{controls[i].valueAction_([controls[i].controlSpec.map(rangerSpecs[0][val/4096]), controls[i].controlSpec.map(rangerSpecs[1][val/4096])])}.defer
					});
				},
				3,{
					Manta.addNoteOnSetup(setup.asSymbol, item[0], {|val| {controls[i].valueAction_(1)}.defer});
					Manta.addNoteOffSetup(setup.asSymbol, item[0], {|val| {controls[i].valueAction_(0)}.defer});
				}
			);
		});
	}*/
}