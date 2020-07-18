SamplePlayer0_NNMod : NN_SampleSynth_Mod {
	*initClass {
		StartUp.add {
			//{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2)).dup}.play;
			SynthDef("SamplePlayer0_NNMod", {|buffer, startFrame=0, endFrame=3000, grainEnv|
				var envs, out, lilEnvs, players, trig, trigs, onOffSwitch, startFrameFreeze, endFrameFreeze, durFrameFreeze, bufTrig, bufTrig1, realDur, selector;

				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(1), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				trig = onOffSwitch;

				//lilEnvs = EnvGen.ar(Env([0,1,1,0], [0.001, dur-0.002, 0.001]), trig);

				//players = PlayBuf.ar(1, buffer, 1, trig, startFrameFreeze).dup*lilEnvs;

				bufTrig1 = ImpulseB.kr(1/\dur.kr(0.1, 0.01), trig);

				bufTrig = Select.kr(\selector.kr(1, 0.01), [bufTrig1, trig]);

				startFrameFreeze = Latch.kr(startFrame, bufTrig);
				endFrameFreeze = Latch.kr(endFrame, bufTrig);
				durFrameFreeze = endFrameFreeze-startFrameFreeze / SampleRate.ir;

				realDur =Select.kr(\selector.kr, [\dur.kr, durFrameFreeze]);

				players = GrainBuf.ar(2, bufTrig, realDur, buffer, 1, startFrameFreeze/BufFrames.kr(buffer), 2, 0, grainEnv);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				out = players*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				Out.ar(\outBus.kr, out*envs);
			}).writeDefFile;
			}
		}

		init {

			this.makeWindow("SamplePlayer0", Rect(0, 0, 200, 40));

			numModels = 8;
			sizeOfNN = numModels*3;

			this.initControlsAndSynths(sizeOfNN);

			dontLoadControls = (0..(sizeOfNN-1));

			nnVals = List.fill(8, {|i| [["xRange"++i, ControlSpec(0, 1)],
			["yRange"++i, ControlSpec(0, 1)],
			["dur"++i, ControlSpec(0.01, 2)]]}).flatten

		}


	}