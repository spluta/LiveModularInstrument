GFNoiseMiniSky_Mod : Module_Mod {
	var group, outBus, midiHidControl, manta, activeSynth, activeState, grainObjects, muted, volBus;

	init {
		this.makeWindow("GFNoiseMiniSky", Rect(500, 400, 300, 100));
		this.initControlsAndSynths(5);

		dontLoadControls = Array.series(3);

		this.makeMixerToSynthBus;

		volBus = Bus.control(group.server);
		muted = true;

		grainObjects = List.new;
		grainObjects.add(GNFGrainObject(group, mixerToSynthBus.index, outBus, volBus.index, 0.02, 0.2));

		controls.add(Button.new()
			.states_([["mute", Color.blue, Color.black ],["mute", Color.black, Color.red ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				muted = true;
				if(activeSynth!=nil,{grainObjects[activeSynth].pause;});
				activeSynth = nil;
		});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["free", Color.blue, Color.black ],["free", Color.black, Color.blue ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				if(muted, {
					activeSynth = 0;
					muted = false;
					grainObjects[0].unfreeze2;
					},{
						grainObjects[0].unfreeze;
				});
				activeState = 0;
		});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["freeze", Color.blue, Color.black ],["freeze", Color.black, Color.blue ]])
			.action_{|v|
				3.do{arg i; controls[i].value = 0};
				v.value = 1;
				if(muted, {
					activeSynth = 0;
					muted = false;
					grainObjects[0].freeze;
					},{
						activeSynth = 0;
						grainObjects[0].freeze;
				});
				activeState = 1;
		});

		this.addAssignButton(2, \onOff);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,\amp),
			{arg val; volBus.set(val.value)}, 1, true, \horz));
		this.addAssignButton(3, \continuous);


		controls.add(QtEZSlider("grainSize", ControlSpec(0.02,0.3,\amp),
			{arg val;
				grainObjects.do{|item|
					item.durLow = val.value;
					item.durHigh = val.value+rand(val.value/4);
			}
		}, 1, true, \horz));
		this.addAssignButton(4, \continuous);


		//multichannel button
		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						grainObjects.do{|item| item.assignNumChannels(numChannels)};
					},
					1, {
						numChannels = 4;
						grainObjects.do{|item| item.assignNumChannels(numChannels)};
					},
					2, {
						numChannels = 8;
						grainObjects.do{|item| item.assignNumChannels(numChannels)};
					}
				)
			};
		);

		controls[0].valueAction = 1;

		win.layout_(
			VLayout(
				HLayout(controls[0], controls[1], controls[2]),
				HLayout(assignButtons[0], assignButtons[1], assignButtons[2]),
				HLayout(controls[3], assignButtons[3]),
				HLayout(controls[4], assignButtons[4]),
				HLayout(controls[5], nil);
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	pause {
		if(muted.not,{
			grainObjects[activeSynth].pause;
		});
	}

	unpause {
		if(muted.not,{
			if(activeState==0,{
				grainObjects[activeSynth].unfreeze2;
				},{
					grainObjects[activeSynth].freeze;
			})
		})
	}

	killMeSpecial {
		volBus.free;
		grainObjects.do{arg item; item.killMe};
		group.freeAllMsg;
	}
}

MuteSky_Mod : Module_Mod {
	var impulseOn, dustOn, pulseRate, zSlider, volSlider;

	*initClass {
		StartUp.add {
			SynthDef("muteSky_mod", {arg inBus, outBus, mute=1, ramp=0.01, gate=1, pauseGate=1;
				var env, out, pauseEnv, muteEnv;

				muteEnv = Lag.kr(mute, ramp);

				//muteEnv = EnvGen.kr(Env.asr(ramp, 1, ramp), mute, doneAction:0);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, In.ar(inBus, 8)*env*muteEnv*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("MuteSky", Rect(500, 500, 290, 75));

		this.initControlsAndSynths(3);

		this.makeMixerToSynthBus(8);

		synths.add(Synth("muteSky_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));

		impulseOn = false;
		dustOn = false;
		pulseRate = [11,17];

		zSlider = 0;
		volSlider = 0;

		controls.add(Button(win)
			.states_([["mute", Color.blue, Color.black],["on", Color.black, Color.blue]])
			.action_({arg butt;
				synths[0].set(\mute, butt.value)
			})
		);

		this.addAssignButton(0,\onOff);

		controls.add(QtEZSlider("ramp", ControlSpec(0.01, 0.25, 'linear'),
			{arg val;
				synths[0].set(\ramp, val.value);
			}, 0.01, true, 'horz'));


		controls.add(QtEZSlider("vol", ControlSpec(0, 1, 'amp'),
			{arg val;
				volSlider=val.value;
				synths[0].set(\mute, volSlider*zSlider);
		}, 0.01, true, 'horz'));
		this.addAssignButton(2,\continuous);

		controls[2].zAction = {|val|
			zSlider = val.value;
			synths[0].set(\mute, zSlider*volSlider);
		};

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1]),
				HLayout(controls[2], assignButtons[2]);
			)
		);
	}
}
