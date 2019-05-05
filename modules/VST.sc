VST_Mod : Module_Mod {
	var loadButton, savePresetButton, loadPresetButton, presetFile, vstPath, vstName, vst, guiButton;

	*initClass {
		StartUp.add {
			SynthDef("vst_mod", {arg inBus, outBus, vol=0, bypass=0, gate=1, pauseGate=1;
				var env, out, pauseEnv;

				out = VSTPlugin.ar(In.ar(inBus, 2), 2, bypass);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv*vol);
			}).writeDefFile;
		}
	}

	init {

		this.initControlsAndSynths(1);

		this.makeMixerToSynthBus(2);

		synths.add(Synth("vst_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus], group));
		vst = VSTPluginController.new(synths[0]);

		vstName = StaticText();

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\vol, v.value);
			}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		loadButton = Button()
		.states_([["load VST", Color.green, Color.black]])
		.action_({arg butt;
			Dialog.openPanel({arg path;
				try {
					vst.open(path);
					vstPath = path;
					fork {
						2.wait;
						{vstName.string = vst.info.name}.defer;
					}
				} {"oops".postln;}
			})
		});

		savePresetButton = Button()
		.states_([["save preset", Color.green, Color.black]])
		.action_({arg butt;
			Dialog.savePanel({arg path;
				try {
					vst.writeProgram(path);
					presetFile = path;
				} {"oops".postln;}
			})
		});

		loadPresetButton = Button()
		.states_([["load preset", Color.blue, Color.black]])
		.action_({arg butt;
			Dialog.loadPanel({arg path;
				try {
					vst.readProgram(path);
					presetFile = path;
				} {"oops".postln;}
			})
		});

		guiButton = Button()
		.states_([["GUI", Color.yellow, Color.black]])
		.action_({arg butt;
			try{vst.gui}
		});

		this.makeWindow("VST", Rect(500, 500, 240, 50));

		win.layout_(VLayout(
			vstName,
			HLayout(controls[0].layout, assignButtons[0].layout),
			HLayout(loadButton, savePresetButton, loadPresetButton, guiButton)
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		//win.front;
	}

	saveExtra {arg saveArray;
		var temp;


		temp = List.newClear(0); //controller settings
		temp.add(vstPath);
		temp.add(presetFile);

		saveArray.add(temp);  //controller messages

		^saveArray
	}

	loadExtra {arg loadArray;

		loadArray.postln;

		if(loadArray[0]!=nil, {
			vst.open(loadArray[0]);
			fork {
				"waiting".postln;
				2.wait;
				"loading".postln;
				{vstName.string = vst.info.name}.defer;
				if(loadArray[1]!=nil, {vst.readProgram(loadArray[1])});
			}
		});

	}
}
