//with the roli

FeedbackSynth_Mod :  Module_Mod {

	*initClass {
		StartUp.add {
			SynthDef("feedbackSynth_mod", {arg fbMult=1500, freezeGate = 1, xFreezeShift = 1, yFreezeShift = 1, straightNoiseFreq=100, noiseSelect=0, ampModOn = 0, ampModRate = 10, vol=1, onOff=1, zOnOff=0, lpHPSelect=0, pulseOn=0, pulseFreqs=#[8,30], pulseWidths=#[0.1,0.5];

				var fb;
				var noise, noiseFreq, out;
				var lastFilterFreq, lastRq, fbFilterFreq, straightNoise, ampMod, impulse, pulse;

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

				out = SelectX.ar(Lag.kr(noiseSelect,0.01), [Compander.ar(out, out).clip(-0.5,0.5)*Select.kr(zOnOff, [1, onOff]), straightNoise]);

				impulse = Impulse.kr(ampModRate);

				ampMod = Lag.kr(Select.kr(ampModOn, [1, Stepper.kr(impulse, 0, 0, 1, 1, 0)]), 0.02);

				pulse = Pulse.ar(pulseFreqs, pulseWidths, 3).softclip;

				out = Select.ar(Lag.kr(pulseOn, 0.01), [out, pulse]);

				Out.ar(0, out*ampMod*vol)
			}).writeDefFile
		}
	}

	init {
		this.makeWindow("FeedbackSynth",Rect(200, 230, 140, 280));

		pitchBus = Bus.control(group.server);

		this.initControlsAndSynths(12);



		win.layout_(
			VLayout(
				HLayout(controls[0].layout, controls[1].layout),
				HLayout(controls[2], controls[3])
			)
		);
	}
}
