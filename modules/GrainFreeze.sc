GrainFreezeDrums_Mod : Module_Mod {
	var volBus, recordGroup, playGroup, buffer, fftBuf, phaseBus, trigButton, getTrig, serverNum;

	*initClass {
		{
			SynthDef(\gfdRecord_mod, { arg inBus, phaseBus, bufnum = 0, gate = 1, pauseGate = 1;
				var sound, phasor, env, pauseEnv;

				sound = In.ar(inBus);
				phasor = Phasor.ar(0, BufRateScale.kr(bufnum), 0, BufFrames.kr(bufnum));

				Out.kr(phaseBus, phasor);

				BufWr.ar(sound, bufnum, phasor, 0, BufFrames.kr(bufnum));

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

			}).writeDefFile;

			SynthDef(\gfdPlay2_mod, { arg inBus, phaseBus, outBus = 0, bufnum = 0, volBus, thresh = 0.1, trigDiv = 2, addPulse = 2, whichTrig=0, useOnOff = 0, offDensity = 0, impDust=0, t_trigShifter=0, t_trigShiftStay=0, shiftTime = 3, triggerVal = 0, lockGate = 1, muteGate = 1, gate = 1, pauseGate = 1;
				var sound, impulse, trig, trig1, trig2, phase, impRate, out, latchPhase = 0, amp, toggleEnv, shiftTrig, shiftEnv, shiftStay, shift, vol, env, pauseEnv, muteEnv, onOffSwitch, phaseOffset, buf, chain, grainDur;

				sound = In.ar(inBus);

				phase = In.kr(phaseBus);


				trig = Trig1.kr((Coyote.kr(sound, thresh: thresh)*EnvGen.kr(Env.asr(0.001, 1, 0.001), lockGate)),0.01);

				//this needs to be more specific
				SendTrig.kr(trig, triggerVal, 1);

				shiftTrig = Trig1.kr(t_trigShifter, shiftTime);

				shift = TExpRand.kr(0.25,4,t_trigShifter+t_trigShiftStay)-1;
				shiftEnv = EnvGen.kr(Env.new([0, shift, shift, 0],[0, shiftTime, 0]), shiftTrig);
				shiftStay = EnvGen.kr(Env.asr(0, shift, 0), ToggleFF.kr(t_trigShiftStay));

				//trig = Select.kr(shiftTrig, [trig, 0]);

				trig1 = PulseDivider.kr(trig, trigDiv, 0);
				trig2 = PulseDivider.kr(trig, trigDiv, addPulse);

				latchPhase = Latch.kr(phase, trig);

				trig = Select.kr(whichTrig, [trig, trig1]);

				toggleEnv = Select.kr(whichTrig, [1, Lag.kr(SetResetFF.kr(trig1, trig2), 0.01)]);

				impRate = TExpRand.kr(5, 100, trig);

				impulse = Impulse.kr(impRate);

				impulse = Select.kr(impDust, [impulse, TChoose.kr(impulse, [impulse, 0], [0.95, 0.05])]);

				onOffSwitch = Select.kr(useOnOff, [1, Lag.kr(TChoose.kr(trig, [1,0], [1-offDensity, offDensity].normalizeSum), 0.01)]);

				//phaseOffset = (LagUD.kr(1-trig, TRand.kr(2, 4, trig), 0)*TExpRand.kr(0.001, 0.025, trig));

				phaseOffset = TRand.kr(0.011, 0.02);

				grainDur = TRand.kr(2/impRate, 4/impRate, trig);
				//changed SampleRate from 44100
				out = TGrains.ar(2, impulse, bufnum,1, (latchPhase/SampleRate.ir-(1/impRate)).clip(1/impRate, BufDur.kr(bufnum)-(1/impRate)), TRand.kr(2/impRate, 4/impRate, trig), TRand.kr(-1, 1, trig), 4);

				vol = In.kr(volBus);

				env = EnvGen.kr(Env.asr(0.02,1,0.02), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);

				Out.ar(outBus, out*0.1*toggleEnv*env*pauseEnv*muteEnv*vol*onOffSwitch);

			}).writeDefFile;

		}.defer(1);
	}

	init {
		this.initControlsAndSynths(9);

		this.makeMixerToSynthBus;

		serverNum = group.server.name.asString;
		serverNum = serverNum.copyRange(3, serverNum.size).asInteger;

		buffer = Buffer.alloc(group.server, group.server.sampleRate*120, 1);

		volBus = Bus.control(group.server);
		phaseBus = Bus.control(group.server);

		synths = List.newClear(2);
		recordGroup = Group.head(group);
		playGroup = Group.tail(group);

		synths.put(0, Synth("gfdRecord_mod", [\inBus, mixerToSynthBus.index, \phaseBus, phaseBus.index, \bufnum, buffer.bufnum], recordGroup));
		synths.put(1, Synth("gfdPlay2_mod", [\inBus, mixerToSynthBus.index, \phaseBus, phaseBus.index, \outBus, outBus, \bufnum, buffer, \volBus, volBus.index, \triggerVal, serverNum], playGroup));

		controls.add(QtEZSlider.new("vol", ControlSpec(0.0,4.0,\amp),
			{|v|
				volBus.set(v.value);
		}, 1.0, true, \horz));
		this.addAssignButton(0,\continuous);

		controls.add(QtEZSlider.new("thresh", ControlSpec(0.2,0.9),
			{|v|
				synths[1].set(\thresh, v.value);
		}, 0.1, true, \horz));
		this.addAssignButton(1,\continuous);

		controls.add(QtEZSlider.new("offDens", ControlSpec(0.0,0.1),
			{|v|
				synths[1].set(\offDensity, v.value);
		}, 0.1, true, \horz));
		this.addAssignButton(2,\continuous);

		controls.add(Button.new()
			.states_([["mute", Color.green, Color.black],["play", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\muteGate, 1);
					},{
						synths[1].set(\muteGate, 0);
				})
		});
		this.addAssignButton(3,\onOff);

		controls.add(Button.new()
			.states_([["free", Color.green, Color.black],["lock", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\lockGate, 0);
					},{
						synths[1].set(\lockGate, 1);
				})
		});
		this.addAssignButton(4,\onOff);

		controls.add(Button.new()
			.states_([["on", Color.green, Color.black],["on/off", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\useOnOff, 1);
					},{
						synths[1].set(\useOnOff, 0);
				})
		});
		this.addAssignButton(5,\onOff);

		controls.add(Button.new()
			.states_([["impulse", Color.green, Color.black],["dust", Color.red, Color.black]])
			.action_{arg butt;
				if(butt.value==1,{
					synths[1].set(\impDust, 1);
					},{
						synths[1].set(\impDust, 0);
				})
		});
		this.addAssignButton(6,\onOff);

		controls.add(Button.new()
			.states_([["shift", Color.yellow, Color.black]])
			.action_{arg butt;
				synths[1].set(\t_trigShifter, 1, \shiftTime, rrand(0.2, 1));
		});
		this.addAssignButton(7,\onOff);

		controls.add(Button.new()
			.states_([["noShift", Color.red, Color.black],["shStay", Color.blue, Color.black]])
			.action_{arg butt;
				synths[1].set(\t_trigShiftStay, butt.value);
		});
		this.addAssignButton(8,\onOff);

		trigButton = Button.new()
		.states_([["trig", Color.black, Color.red],["trig", Color.black, Color.blue]]);

		this.makeWindow("GrainFreezeDrums",Rect(718, 758, 380, 98));

		getTrig = OSCFunc({|msg, time|
			if(msg[1]==synths[1].nodeID&&msg[2]==serverNum, {
				{trigButton.value = (trigButton.value+1).wrap(0,1)}.defer
		})}, '/tr');

		win.layout_(VLayout(
			HLayout(controls[0],assignButtons[0]),
			HLayout(controls[1],assignButtons[1]),
			HLayout(controls[2], assignButtons[2]),
			HLayout(controls[3], assignButtons[3],
				controls[4], assignButtons[4],
				controls[5], assignButtons[5],
				controls[6], assignButtons[6]),
			HLayout(controls[7], assignButtons[7],
				controls[8], assignButtons[8],
				trigButton, controls[9])
			)
		);
}

killMeSpecial {
	getTrig.stop;
}
}