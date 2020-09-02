SamplePlayer0_NNMod : NN_SampleSynth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("SamplePlayer0_NNMod", {|buffer, treeInBus, treeOutBus, treeInBuffer, treeOutBuffer, grainEnv|
				var envs, out, lilEnvs, players, trig, trigs, onOffSwitch, startFrameFreeze, endFrameFreeze, durFrameFreeze, bufTrig, bufTrig1, realDur, selector, indices;

				 var treeTrig, point;




				onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(1), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

				trig = onOffSwitch;

				bufTrig1 = ImpulseB.kr(1/\dur.kr(0.1, 0.01), trig);

				bufTrig = Select.kr(\selector.kr(1, 0.01), [bufTrig1, trig]);


				treeTrig = Impulse.kr(MouseX.kr(0, ControlRate.ir/2));//Impulse.kr((ControlRate.ir/10));

				Out.kr(treeInBus, treeTrig);

				indices = BufRd.kr(1,treeOutBuffer,Array.iota(2));

				startFrameFreeze = Latch.kr(indices[0], bufTrig);
				endFrameFreeze = Latch.kr(indices[0], bufTrig);
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