PedalRouter_Mod : Module_Mod {
	var volBus, buffers, bufferStream;

	*initClass {
		StartUp.add {
			SynthDef(\pedalRouter_mod, {| inBus, outBus, pedalOutBus, pedalInBus, outVol, inVol, gate = 1, muteGate = 1, pauseGate = 1 |
				var in, out, env, pauseEnv, muteEnv;

				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				in = In.ar(inBus)*inVol;
				Out.ar(pedalOutBus, in);

				out = SoundIn.ar(pedalInBus)*outVol*muteEnv;

				Out.ar(outBus, [out,out]*env*pauseEnv);
			}).writeDefFile;
		}
	}

init {
		this.makeWindow("PedalRouter",Rect(718, 645, 145, 310));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus;

		synths = List.new;
		synths.add(Synth("pedalRouter_mod",[\inBus, mixerToSynthBus.index, \outBus, outBus, \pedalOutBus, 2, \pedalInBus, 2, \outVol, 1, \inVol, 1], group));

		controls.add(EZSlider.new(win,Rect(10, 10, 60, 220), "inVol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\inVol, v.value);
			}, 1, true, layout:\vert));
		this.addAssignButton(0,\continuous, Rect(10, 230, 60, 20));

		controls.add(EZSlider.new(win,Rect(75, 10, 60, 220), "outVol", ControlSpec(0,2,'amp'),
			{|v|
				synths[0].set(\outVol, v.value);
			}, 1, true, layout:\vert));
		this.addAssignButton(1,\continuous, Rect(75, 230, 60, 20));

		controls.add(EZPopUpMenu.new(win,Rect(10, 260, 60, 20), "",
			[
				\2 ->{ synths[0].set(\pedalOutBus, 2)},
				\3 ->{ synths[0].set(\pedalOutBus, 3)},
				\4 ->{ synths[0].set(\pedalOutBus, 4)},
				\5 ->{ synths[0].set(\pedalOutBus, 5)},
				\6 ->{ synths[0].set(\pedalOutBus, 6)},
				\7 ->{ synths[0].set(\pedalOutBus, 7)}
			], {}, 0, false, 0, 0, \horz));
		controls.add(EZPopUpMenu.new(win,Rect(75, 260, 60, 20), "",
			[
				\2 ->{ synths[0].set(\pedalInBus, 2)},
				\3 ->{ synths[0].set(\pedalInBus, 3)},
				\4 ->{ synths[0].set(\pedalInBus, 4)},
				\5 ->{ synths[0].set(\pedalInBus, 5)},
				\6 ->{ synths[0].set(\pedalInBus, 6)},
				\7 ->{ synths[0].set(\pedalInBus, 7)}
			], {}, 0, false, 0, 0, \horz));

		controls.add(Button(win,Rect(10, 285, 60, 20))
			.states_([["Rout",Color.green,Color.black], ["Mute",Color.red,Color.black]])
			.action_{|v|
				if(v.value==0,{
					synths[0].set(\muteGate, 1);
				},{
					synths[0].set(\muteGate, 0);
				});
			});
		this.addAssignButton(4,\noIdea, Rect(70, 285, 60, 20));
	}
}
