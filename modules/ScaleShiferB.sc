ScaleShifterB_Mod : Module_Mod {
	var largeEnv, envGroup, synthGroup, largeEnvBus, nextTime, localRout, delayTime, length, filterStart, filterEnd, rqStart, rqEnd, inVolBus, outVolBus, delayVar, waitVar, numNotes, numNotesLocal, bigScale, localLength, smallLength, ratio0, outBusLocal, limitGroup, limitBus;

	*initClass {
		StartUp.add {

			SynthDef("scaleShifterLimiter_mod", {arg inBus, outBus, limit=0.9;
				Out.ar(outBus, Limiter.ar(In.ar(inBus), limit))
			}).writeDefFile;

			SynthDef("scaleShifterB2_mod", {arg inBusNum, outBus, inVolBus, outVolBus, largeEnvBusNum, length, gate0=0, ratio0=1, smallLength;
				var in, in2, env, env0, sig0, out, largeEnv, bigEnv, inVol, outVol, xStart, xEnd, delTime;

				inVol = In.kr(inVolBus);
				outVol = In.kr(outVolBus);

				env0 = EnvGen.kr(Env.sine(smallLength*5,1), 1);

				largeEnv = In.kr(largeEnvBusNum);
				in = (In.ar(inBusNum, 1)*env0*inVol)+(LocalIn.ar(1));

				env = EnvGen.kr(Env.new([0,1,1,0], [0.1,length,3], 'linear'), doneAction:2);

				xStart = Rand(-1,1);
				xEnd = Rand(-1,1);

				sig0 = PitchShift.ar(in, 0.1, ratio0, 0, 0.02);

				sig0 = LPF.ar(HPF.ar(sig0, 100), 15000);

				delTime = Rand(smallLength/3,smallLength/2);

				LocalOut.ar(DelayC.ar(sig0, delTime));

				out = Pan2.ar(sig0,
					Line.kr(xStart, xEnd, length))*largeEnv;
				Out.ar(outBus, out*outVol);
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("ScaleShifterB",Rect(318, 645, 150, 270));
		this.initControlsAndSynths(4);

		this.makeMixerToSynthBus;

		envGroup = Group.head(group);
		synthGroup = Group.tail(group);
		limitGroup = Group.tail(group);

		largeEnvBus = Bus.control(group.server);
		inVolBus = Bus.control(group.server);
		outVolBus = Bus.control(group.server);
		limitBus = Bus.audio(group.server, 2);


		inVolBus.set(0);outVolBus.set(0);

		synths = List.newClear(2);

		synths.put(0, Synth("largeEnvFilt_mod", [\outBusNum, largeEnvBus.index, \gate, 1.0], envGroup));

		synths.put(1, Synth("scaleShifterLimiter_mod", [\inBus, limitBus, \outBus, outBus, \limit, 0.9], limitGroup));

		bigScale = [4/5,7/8,8/9,10/11,8/11,7/9];

		localRout = Routine.new({{
			localLength = rrand(3*length/4, 3*length/2);
			numNotesLocal = max(2, rrand(numNotes-2, numNotes+2).round);


			smallLength = localLength/numNotesLocal;

			ratio0 = bigScale.choose;
			if(0.5.coin,{ratio0 = 1/ratio0});

			Synth("scaleShifterB2_mod", [\inBusNum, mixerToSynthBus.index, \outBus, limitBus, \inVolBus, inVolBus.index, \outVolBus, outVolBus.index, \largeEnvBusNum, largeEnvBus.index, \length, localLength, \smallLength, smallLength, \ratio0, ratio0], synthGroup);

			(rrand(localLength/5, localLength/3)).wait;
		}.loop});

		controls.add(QtEZSlider.new("invol", ControlSpec(0,8,'amp'),
			{|v|
				inVolBus.set(v.value);
			}, 0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("outvol", ControlSpec(0,8,'amp'),
			{|v|
				outVolBus.set(v.value);
			}, 0, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider.new("length", ControlSpec(1,7,'linear'),
			{|v|
				length = v.value;
			}, 0, true, \horz));
		this.addAssignButton(2,\continuous);

		controls.add(QtEZSlider.new("notes", ControlSpec(2,24,'linear',1),
			{|v|
				numNotes = v.value;
			}, 0, true, \horz));
		this.addAssignButton(3,\continuous);

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1]),
				HLayout(controls[2], assignButtons[2]),
				HLayout(controls[3], assignButtons[3])
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

		SystemClock.play(localRout);
	}


	pause {
		largeEnv.set(\pauseGate, 0);
	}

	unpause {
		largeEnv.set(\pauseGate, 1);
	}

	killMeSpecial {
		localRout.stop;
		largeEnvBus.free;
		inVolBus.free;
		outVolBus.free;
		limitBus.free;
	}
}
