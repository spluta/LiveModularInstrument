//with the roli

FeedbackSynth_Mod :  Module_Mod {
	var texts, functions;

	*initClass {
		StartUp.add {
			SynthDef("feedbackSynth_mod", {arg outBus, fbMult=1500, freezeGate = 1, xFreezeShift = 1, yFreezeShift = 1, straightNoiseFreq=100, noiseSelect=0, ampModOn = 0, ampModRate = 10, vol=0, onOff=1, zOnOff=0, onOff2=0, lpHPSelect=0, pulseOn=0, pulseFreqs=#[8,30], pulseWidths=#[0.1,0.5], gate=1, pauseGate=1;

				var fb;
				var noise, noiseFreq, out;
				var lastFilterFreq, lastRq, fbFilterFreq, straightNoise, ampMod, impulse, pulse, env, pauseEnv;

				noiseFreq = Gate.kr(LFNoise1.kr(1).range(500, 12000)!2, freezeGate);

				noise = LFNoise0.ar(
					Gate.kr(LFNoise1.kr(1).range(500, 12000)!2, freezeGate)*yFreezeShift
				);
				fb = LocalIn.ar(2);

				fbFilterFreq = Gate.kr((1000+(fb*fbMult)), freezeGate)*xFreezeShift;

				fb = RLPF.ar(noise, fbFilterFreq);
				LocalOut.ar(fb);


				lastFilterFreq = Gate.kr(LFNoise0.kr(LFNoise0.kr(0.5).range(0.5,10)).exprange(10,
					LFNoise0.kr(LFNoise0.kr(1).range(0.5,3)).range(40, 2000)), freezeGate)*yFreezeShift;

				lastRq = Gate.kr(LFNoise0.kr(10).range(0.5,2), freezeGate)*xFreezeShift;

				//freq of last RLPF - no control at all
				out = Select.ar(lpHPSelect, [RLPF.ar(
					fb,
					lastFilterFreq,
					lastRq
				),
				RHPF.ar(
					fb,
					lastFilterFreq,
					lastRq
				)]);

				straightNoise = MidEQ.ar([BrownNoise.ar(0.1)+WhiteNoise.ar(0.1)+LFClipNoise.ar(straightNoiseFreq,0.1), BrownNoise.ar(0.1)+ WhiteNoise.ar(0.1)+LFClipNoise.ar(straightNoiseFreq,0.1)], straightNoiseFreq, 0.3, 48 ).softclip*0.5;

				out = SelectX.ar(Lag.kr(noiseSelect,0.01), [Compander.ar(out, out).clip(-0.5,0.5)*Select.kr(zOnOff, [1, (onOff+onOff2).clip(0,1)]), straightNoise]);

				impulse = Impulse.kr(ampModRate);

				ampMod = Lag.kr(Select.kr(ampModOn, [1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.02);

				pulse = Pulse.ar(pulseFreqs*xFreezeShift, pulseWidths*yFreezeShift, 3).softclip;

				out = Select.ar(Lag.kr(pulseOn, 0.01), [out, pulse]);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*ampMod*vol*env*pauseEnv)
			}).writeDefFile
		}
	}

	init {
		this.makeWindow("FeedbackSynth",Rect(200, 230, 140, 280));

		this.initControlsAndSynths(13);

		synths.add(Synth("feedbackSynth_mod", [\outBus, outBus], group));

		texts = List.newClear(0);

		["fbMult", "onOff", "lpHPSelect", "xFreezeShift", "yFreezeShift", "freezeGate", "zOnOff", "noiseSelect", "straightNoiseFreq", "ampModOn", "ampModRate", "pulseOn", "vol"].collect({|item| texts.add(StaticText().string_(item))});

		functions = [

			{arg val; synths[0].set(\fbMult, val.linlin(0,1,1000,10000))},
			{arg val; synths[0].set(\onOff, val)},

			{arg val;  synths[0].set(\lpHPSelect, 1-val)},
			{arg val;  synths[0].set(\xFreezeShift, val.linlin(0,1,0.95,1.05))},
			{arg val;  synths[0].set(\yFreezeShift, val.linlin(0,1,0.95,1.05))},

			{arg val;  synths[0].set(\freezeGate, 1-val, \onOff2, val)},

			{arg val;  synths[0].set(\zOnOff, 1-val)},
			{arg val;  synths[0].set(\noiseSelect, val)},
			{arg val;  synths[0].set(\straightNoiseFreq, val.linexp(0,1,20,5000))},
			{arg val;  synths[0].set(\ampModOn, val)},
			{arg val;  synths[0].set(\ampModRate, val.linexp(0,1,4,30))},
			{arg val;  synths[0].set(\pulseOn, val, \pulseFreqs, Array.fill(2, {rrand(8,30)}), \pulseWidths, Array.fill(2, {rrand(0.1,0.5)}))},
			{arg val;  synths[0].set(\vol, val)}
		];

		13.do{arg i;
			oscMsgs.postln;
			controls.add(TextField()
				.action = {arg field;
					if(oscMsgs[i]!=nil,{
						MidiOscControl.clearController(group.server, oscMsgs[i]);
						TypeOSCFunc_Mod.removeResponder(oscMsgs[i]);
					});
					TypeOSCFunc_Mod.addResponder(field.value);
					oscMsgs.put(i, field.value.asString);
					MidiOscControl.setControllerNoGui(group.server, oscMsgs[i], functions[i], setups);
		})};

		win.layout_(
			HLayout(
				VLayout(*texts),
				VLayout(*controls)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	load {arg loadArray;

		loadArray[1].do{arg controlLevel, i;
			//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
			if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
				controls[i].valueAction_(controlLevel);
			});
		};

		if(win!=nil,{
			win.bounds_(loadArray[3]);
			win.visible_(false);
		});

		this.loadExtra(loadArray);
	}
}
