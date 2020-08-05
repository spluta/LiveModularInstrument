LargeArcLoops_Mod : Module_Mod {
	var phasorBus, volBus, transferBus, recordGroup, playGroup, verbGroup, verbGroup, buffer, lengthRange, impulseRange, panStart, panEnd, side, nodeStream;

	*initClass {
		StartUp.add {

			SynthDef("largeArcLoopsRec_mod", {arg inBus, outBus, phasorBus, bufnum, t_trig=0, gate=1, pauseGate=1;
				var in, phasor, phaseStart, env, pauseEnv, resetTrig;

				in = In.ar(inBus,1);

				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				//phaseStart = Latch.kr(phasor, t_trig);

				env = EnvGen.kr(Env.asr(0, 1, 5), gate, doneAction:2);

				Out.kr(phasorBus, phasor);

				BufWr.ar(in, bufnum, phasor, loop:1);
			}).writeDefFile;

			SynthDef("largeArcLoopsPlay_mod", {arg outBus, transferBus, volBus, phasorBus, bufnum, impulseRate, arcDur, panStart, panEnd, gate=1, pauseGate=1;
				var trig, phaseStart, env, smallEnv, pauseEnv, durs, dur, vol, out, verbLine, rate;

				phaseStart = Latch.kr(In.kr(phasorBus),Line.kr(-1, 1, 0.1))-(Rand(22000, 44000));

				trig  = Impulse.ar(Line.kr(impulseRate, impulseRate+Rand(-0.25, 0.25, arcDur)));

				verbLine = EnvGen.kr(Env.new([1, Rand(0.2, 0.5), 1], [arcDur/2,arcDur/2]), 1);

				rate = XLine.kr(1, ExpRand(0.5, 2), arcDur);

				out = TGrains.ar(2, trig, bufnum, rate, (phaseStart/BufFrames.kr(bufnum)*30)+Line.kr(0, Rand(1,3), arcDur), 2/impulseRate, Line.kr(panStart, panEnd, arcDur));

				env = EnvGen.kr(Env.new([0,1,0],[arcDur/3, 2*arcDur/3], \sine), doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				vol = In.kr(volBus);

				out = out*env*pauseEnv*vol;

				Out.ar(transferBus, out*verbLine);

				Out.ar(outBus, out);
			}).writeDefFile;

			SynthDef("largeArcVerb_mod", {arg transferBus, outBus, gate=1, pauseGate=1;
				var in, out, env, pauseEnv;

				in = In.ar(transferBus, 2);

				out = FreeVerb2.ar(in[0], in[1], 1, 0.7, 1, 0.8)+FreeVerb2.ar(in[0], in[1], 1, 0.9, 1, 0.08);

				env = EnvGen.kr(Env.asr(0, 1, 5), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, out*env*pauseEnv);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("LargeArcLoops", Rect(600, 549, 330, 94));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus;

		buffer = Buffer.alloc(group.server, group.server.sampleRate*30, 1);

		phasorBus = Bus.control(group.server);
		volBus = Bus.control(group.server);

		synths = List.newClear(12);

		recordGroup = Group.head(group);
		playGroup = Group.tail(group);
		verbGroup = Group.tail(group);

		transferBus = Bus.audio(group.server,2);

		synths.put(0, Synth("largeArcLoopsRec_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \phasorBus, phasorBus, \bufnum, buffer.bufnum], recordGroup));
		synths.put(1, Synth("largeArcVerb_mod", [\transferBus, transferBus, \outBus, outBus], verbGroup));

		nodeStream = Pseq((2..11), inf).asStream;


		controls.add(QtEZSlider("vol", ControlSpec(0.0,8.0,\amp),
			{|v|
				volBus.set(v.value);
		}, 0, true, \horz));
		this.addAssignButton(0, \continuous);


		controls.add(Button()
			.states_([["go", Color.green, Color.black],["go", Color.black, Color.green]])
			.action_{arg butt;
				side = [1,-1].choose;
				panStart = rrand(0.5, 1)*side;
				panEnd = rrand(0.5, 1)*(side.neg);
				synths.put(nodeStream.next, Synth("largeArcLoopsPlay_mod", [\outBus, outBus, \transferBus, transferBus, \volBus, volBus, \phasorBus, phasorBus, \bufnum, buffer.bufnum, \impulseRate, rrand(impulseRange[0], impulseRange[1]), \arcDur, rrand(lengthRange[0], lengthRange[1]), \panStart, panStart, \panEnd, panEnd], playGroup));
		});
		this.addAssignButton(1, \onOff);


		controls.add(QtEZRanger("len", ControlSpec(5, 20),
			{arg vals;
				lengthRange = vals.value;
		}, [5,10], true, \horz));


		controls.add(QtEZRanger("impulseRate", ControlSpec(0.5, 6),
			{arg vals;
				impulseRange = vals.value;
		}, [1,2], true, \horz));

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				controls[2], controls[3]
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;
	}

	killMeSpecial {
		phasorBus.free;
		transferBus.free;
		volBus.free;
		buffer.free;
	}
}
