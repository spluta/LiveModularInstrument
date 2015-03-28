ResonDraw_Mod : Module_Mod {
	var transferBus, limitBus, group0, group1, group2, localRout, length, delayTime, outTemp;

	*initClass {
		StartUp.add {


			SynthDef("resonNoise2_mod", {arg inBus, outBus, transferBus, noiseVol=0, inOutVol=0, centerFreq, lfoFreq, rq = 0.1, onOff=1, gate=1, pauseGate=1;
				var env, in, noiseOut, inOut, pauseEnv, freq;

				in = In.ar(inBus, 1);

				freq = max(centerFreq+SinOsc.ar(lfoFreq, 0, centerFreq*0.8), 40);

				inOut = Resonz.ar(in, freq, rq);

				noiseOut = Resonz.ar(WhiteNoise.ar(1), freq, rq);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				noiseOut = Pan2.ar(noiseOut, LFNoise2.kr(lfoFreq+5/20));

				Out.ar(transferBus, inOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*inOutVol);

				Out.ar(outBus, noiseOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*noiseVol);
			}).writeDefFile;

			SynthDef("resonNoise4_mod", {arg inBus, outBus, transferBus, noiseVol=0, inOutVol=0, centerFreq, lfoFreq, rq = 0.1, onOff=1, gate=1, pauseGate=1;
				var env, in, noiseOut, inOut, pauseEnv, freq;

				in = In.ar(inBus, 1);

				freq = max(centerFreq+SinOsc.ar(lfoFreq, 0, centerFreq*0.8), 40);

				inOut = Resonz.ar(in, freq, rq);

				noiseOut = Resonz.ar(WhiteNoise.ar(1), freq, rq);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				noiseOut = PanAz.ar(4, noiseOut, LFNoise2.kr(lfoFreq+5/20));

				Out.ar(transferBus, inOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*inOutVol);

				Out.ar(outBus, noiseOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*noiseVol);
			}).writeDefFile;

			SynthDef("resonNoise8_mod", {arg inBus, outBus, transferBus, noiseVol=0, inOutVol=0, centerFreq, lfoFreq, rq = 0.1, onOff=1, gate=1, pauseGate=1;
				var env, in, noiseOut, inOut, pauseEnv, freq;

				in = In.ar(inBus, 1);

				freq = max(centerFreq+SinOsc.ar(lfoFreq, 0, centerFreq*0.8), 40);

				inOut = Resonz.ar(in, freq, rq);

				noiseOut = Resonz.ar(WhiteNoise.ar(1), freq, rq);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				noiseOut = PanAz.ar(8, noiseOut, LFNoise2.kr(lfoFreq+5/20));

				Out.ar(transferBus, inOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*inOutVol);

				Out.ar(outBus, noiseOut*LagUD.kr(onOff, 0.1, 1)*env*pauseEnv*noiseVol);
			}).writeDefFile;

			SynthDef("resonDelays2_mod", {arg inBusNum, outBus0, outBus1, delayTime, length;
				var in, in2, env, delayedSignal, buffer, out, bigEnv, volume, xStart, xEnd;

				in = In.ar(inBusNum, 1);

				env = Env.new([0.001,1,1,0.001], [1,length,1], 'linear');
				bigEnv = Env.new([0.001,1,1,0.001], [0.01,length+delayTime+1.98,0.01], 'linear');

				in2 = EnvGen.ar(env, doneAction: 0)*EnvGen.ar(bigEnv, doneAction: 2)*in;

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				delayedSignal = DelayL.ar(in2, 5, delayTime);
				out = Pan2.ar(delayedSignal, Line.kr(xStart, xEnd, length+2+delayTime));
				Out.ar(outBus0, out[0]);
				Out.ar(outBus1, out[1]);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("ResonDraw", Rect(500, 500, 350, 260));

		this.initControlsAndSynths(4);

		dontLoadControls = [0,1,2];

		group0 = Group.head(group);
		group1 = Group.tail(group);
		group2 = Group.tail(group);

		this.makeMixerToSynthBus;

		transferBus = Bus.audio(group.server);

		limitBus = Bus.audio(group.server, 8);

		synths.add(Synth("resonNoise2_mod", [\inBus, mixerToSynthBus, \outBus, limitBus, \transferBus, transferBus, \vol, 0, \centerFreq, 400, \lfoFreq, 0], group0));

		synths.add(Synth("limiter_mod", [\inBus, limitBus, \outBus, outBus, \limit, 0.9], group2));

		controls.add(QtEZSlider.new("noiseVol", ControlSpec(0,1,'amp'),
			{|v|
				synths[0].set(\noiseVol, v.value);
		}, 0, true, \vert));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("inOut", ControlSpec(0,4,'amp'),
			{|v|
				synths[0].set(\inOutVol, v.value);
		}, 0, true, \vert));
		this.addAssignButton(1,\continuous);


		controls.add(QtEZSlider2D.new(ControlSpec(0, 50), ControlSpec(80, 15080),
			{arg vals;
				synths[0].set(\centerFreq, vals[1], \lfoFreq, vals[0])
		}));
		this.addAssignButton(2,\slider2D);

		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						synths[0].set(\gate, 0);
						numChannels = 2;
						synths.put(0, Synth("resonNoise2_mod", [\inBus, mixerToSynthBus, \outBus, limitBus, \transferBus, transferBus, \vol, 0, \centerFreq, 400, \lfoFreq, 0], group0));
					},
					1, {
						synths[0].set(\gate, 0);
						numChannels = 4;
						synths.put(0, Synth("resonNoise4_mod", [\inBus, mixerToSynthBus, \outBus, limitBus, \transferBus, transferBus, \vol, 0, \centerFreq, 400, \lfoFreq, 0], group0));
					},
					2, {
						synths[0].set(\gate, 0);
						numChannels = 8;
						synths.put(0, Synth("resonNoise8_mod", [\inBus, mixerToSynthBus, \outBus, limitBus, \transferBus, transferBus, \vol, 0, \centerFreq, 400, \lfoFreq, 0], group0));
					}
				)
			};
		);

		controls[2].zAction = {|val|
			synths[0].set(\rq, rrand(0.1, 0.5));
		};

		localRout = Task.new({{
			delayTime = 2.5.rand+0.4;
			length = 3.0.rand + 5;

			switch(numChannels,
				2,{
					Synth("resonDelays2_mod", [\inBusNum, transferBus, \outBus0, limitBus.index, \outBus1, limitBus.index+1, \delayTime, delayTime, \length, length], group1);
				},
				4,{
					outTemp = limitBus.index+4.rand;
					Synth("resonDelays2_mod", [\inBusNum, transferBus, \outBus0, outTemp, \outBus1, (outTemp+(3.rand+1)).wrap(outTemp, outTemp+3), \delayTime, delayTime, \length, length], group1);
				},
				8,{
					outTemp = limitBus.index+8.rand;
					Synth("resonDelays2_mod", [\inBusNum, transferBus, \outBus0, outTemp, \outBus1, (outTemp+(7.rand+1)).wrap(outTemp, outTemp+7), \delayTime, delayTime, \length, length], group1);
				}
			);
			(0.5 + (0.15.rand)).wait;
		}.loop});
		localRout.start;

		win.layout_(
			VLayout(
				HLayout(
					VLayout(controls[0].layout,assignButtons[0].layout),
					VLayout(controls[1].layout,assignButtons[1].layout),
					VLayout(controls[2].layout,assignButtons[2].layout)
				),controls[3]
			)
		);
	}

	pause {
		//localRout.pause;
		synths[0].set(\pauseGate, 0);
	}

	unpause {
		//localRout.resume;
		synths[0].set(\pauseGate, 1);
		synths[0].run(true);
	}

	killMeSpecial {
		localRout.stop;
		transferBus.free;
	}
}