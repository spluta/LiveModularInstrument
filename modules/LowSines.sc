
LowSines_Mod : Module_Mod {
	var midiNoteNum, detune, localRout,synthSpotStream,synthSpot, dur, freqs;

	*initClass {
		StartUp.add {
			SynthDef("lowSine", {arg freq0, freq1, dur, outBus, vol=0, gate = 1, pauseGate = 1, localGate = 0;
				var sine, env, pauseEnv, localEnv, freq;

				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.05), pauseGate, doneAction:1);
				env = EnvGen.kr(Env([0,1,1,0],[0.5, dur-1, 0.5]), gate, doneAction:2);

				freq = XLine.kr(freq0, freq1, dur);

				sine = SinOsc.ar(freq, 0, 0.2);

				Out.ar(outBus, Pan2.ar(sine*AmpComp.kr(freq)*env*pauseEnv, Rand(-1, 1)));
			}).writeDefFile;
		}
	}

	init {
		"running wub sine init".postln;
		this.makeWindow("LowSines", Rect(500,100,210,250));

		synths = List.newClear(3);

		synthSpotStream = Pseq([0,1,2], inf).asStream;

		localRout = Routine.new({{
			freqs = [rrand(30,50),rrand(120,150)];
			if(0.5.coin,{freqs = freqs.rotate(1)});

			freqs.postln;

			dur = rrand(5.0, 8.0);

			synthSpot = synthSpotStream.next;

			synths[synthSpot].set(\gate, 0);

			synths.put(synthSpot, Synth("lowSine", [\freq0, freqs[0], \freq1, freqs[1], \dur, dur, \outBus, outBus], group));

			(dur-0.5).wait;
		}.loop});

		SystemClock.play(localRout);
	}

	pause {
		localRout.stop;
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
	}

	unpause {
		localRout.reset;
		SystemClock.play(localRout);
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
	}

	killMeSpecial {
		localRout.stop;
	}
}