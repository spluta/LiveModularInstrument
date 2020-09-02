CF1_TimbreMap_NNMod : TimbreMap_Synth_Mod {
	*initClass {
		StartUp.add {
			SynthDef("CF1_Listener_NNMod",{|inBus, buf|
					var source, mfcc;

					source = In.ar(inBus);
					mfcc = FluidMFCC.kr(source, 20);

					mfcc.copyRange(1,19).collect{|point, i| BufWr.kr(point, buf, i)};

			}).writeDefFile;

			SynthDef("CF1_TimbreMap_NNMod",{
				var localIn, noise1, osc1, osc1a, osc1b, osc2, out, foldNum, dust, trigEnv, filtMod, filterFreq, envs, onOffSwitch;

				localIn = LocalIn.ar(1);

				osc1 = SinOscFB.ar(\freq1.kr(300, 0.01).clip(2, 10000)+(localIn*\modVol1.kr(1).clip(0, 3000)), \freq1.kr.linlin(100, 300, 2, 0.0));

				osc1 = SelectX.ar(\freq1.kr.linlin(15.0, 25.0, 0.0, 1.0), [osc1.linlin(-1.0,1.0, 0.0, 1.0), osc1]);

				osc2 = LFTri.ar(\freq2.kr(500, 0.01).clip(2, 10000)+(osc1*\modVol2.kr(1).clip(0, 3000)));

				osc2 = LeakDC.ar(osc2);

				LocalOut.ar(osc2);

				foldNum = \fold.kr(0.5).clip(0.1,1);

				out = osc2.fold2;

				filterFreq = \outFilterFreq.kr(10000, 0.01).clip(20, 20000);

				out = RLPF.ar(out, filterFreq, \outFilterRQ.kr(0.5, 0.01).clip(0.1, 1));


				onOffSwitch = (\onOff0.kr(0, 0.01)/*+\onOff1.kr(0, 0.01)*/).clip(0,1);

				onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);
				onOffSwitch.poll(1);

				out = out*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*onOffSwitch*Lag.kr(In.kr(\chanVolBus.kr), 0.05).clip(0,1);

				envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

				Out.ar(\outBus.kr, Pan2.ar(out, 0)*envs);
			}).writeDefFile;
			}
		}

		makeListeningSynth {
		[parent.mixerToSynthBus, buf].postln;
			listeningSynth = Synth("CF1_Listener_NNMod",[\inBus, parent.mixerToSynthBus, \buf, buf], group);
		}

		init {

			this.makeWindow("CF1_TimbreMap", Rect(0, 0, 200, 40));

			numModels = 8;
			sizeOfNN = 7;

			//this.makeMixerToSynthBus;

			this.initControlsAndSynths(sizeOfNN);

			dontLoadControls = (0..(sizeOfNN-1));

			nnVals = [[\freq1, ControlSpec(20, 10000, \exp)],
				[\freq2, ControlSpec(20, 10000, \exp)],
				[\modVol1, ControlSpec(0, 3000)],
				[\modVol2, ControlSpec(0, 3000)],
				[\fold, ControlSpec(0.1, 1)],
				[\outFilterFreq, ControlSpec(300, 20000, \exp)],
				[\outFilterRQ, ControlSpec(0.1, 2, \exp)]
			];

			/*		nnVals = [[\freq1, ControlSpec(1, 10000, \exp)],
			[\freq2, ControlSpec(5, 10000, \exp)],
			[\modVol1, ControlSpec(0, 3000)],
			[\modVol2, ControlSpec(0, 3000)],
			[\noiseVol, ControlSpec(0, 3000)],
			[\impulse, ControlSpec(100, 20000, \exp)],
			[\filterFreq, ControlSpec(100, 20000, \exp)],
			[\rq, ControlSpec(0.1, 2)],
			[\fold, ControlSpec(0.1, 1)],
			[\dustRate, ControlSpec(1000, 1)],
			[\attack, ControlSpec(0.001, 0.01, \exp)],
			[\release, ControlSpec(0.001, 0.01, \exp)],
			[\outFilterFreq, ControlSpec(20, 20000, \exp)],
			[\outFilterRQ, ControlSpec(0.1, 2, \exp)],
			[\filtModFreq, ControlSpec(0, 30, \lin)],
			[\filtModAmp, ControlSpec(0, 1, \amp)]

			];*/

			"initNN_Synth".postln;

		}

	}