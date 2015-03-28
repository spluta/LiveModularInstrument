InputSelectXY_Mod : Module_Mod {
	var volsBus, points, temp, tempOrder;

	*initClass {
		StartUp.add {


			SynthDef("inputSelectXY_mod", {arg inBus, outBus, volsBus, filterBoost=10, filterSpeed=0.1, rq = 0.1, onOff=1, gate=1, pauseGate=1;
				var env, in, vols, pauseEnv, freq, chain, out;

				in = Normalizer.ar(In.ar(inBus));

				vols = Lag2.kr(In.kr(volsBus, 8), 1);

				freq = SinOsc.kr(filterSpeed, 0).range(30, 10000);
				in = MidEQ.ar(in, freq, rq, filterBoost);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in = in*env*pauseEnv;

				chain = FFT(LocalBuf(512), in);

				out = IFFT(PV_BrickWall(chain, -0.5));

				Out.ar(outBus, [LPF.ar(LPF.ar(out, 8000), 8000), LPF.ar(LPF.ar(out, 8000), 8000), in]);

			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("InputSelectXY", Rect(500, 500, 600, 420));

		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		volsBus = Bus.control(group.server, 8);

		synths.add(Synth("inputSelectXY_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volsBus, volsBus.index], group));

		points = 8.collect{|i| [0.2*(i%4)+0.2, 0.33*((i/4).floor)+0.33, i]};

		controls.add(Slider2D.new(win,Rect(0, 0, 400, 400)));		controls[0].action_({|sl|
			temp = points.collect{|item, i|
				(((item[0]-sl.x)**2)+((item[1]-sl.y)**2)).sqrt;
			};

			temp = temp.expexp(1e-6, 1, 2.0, 1e-9).normalizeSum;

			tempOrder = temp.order;
			(0..4).do{|i| temp.put(tempOrder[i], 0)};

			volsBus.setn(temp);
		});

		this.addAssignButton(0,\slider2D, Rect(0, 400, 60, 20));


		controls.add(EZSlider.new(win,Rect(405, 5, 60, 220), "filterBoost", ControlSpec(-10,20),
			{|v|
				synths[0].set(\filterBoost, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(405, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(465, 5, 60, 220), "filterSpeed", ControlSpec(0, 0.01, 'lin'),
			{|v|
				synths[0].set(\filterSpeed, v.value);
			}, 0.005, layout:\vert));
		this.addAssignButton(2,\continuous, Rect(465, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(525, 5, 60, 220), "rq", ControlSpec(0.1, 1, 'exponential'),
			{|v|
				synths[0].set(\rq, v.value);
			}, 0, layout:\vert));
		this.addAssignButton(3,\continuous, Rect(525, 230, 60, 20));


	}
}