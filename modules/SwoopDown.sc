SwoopDown_Mod : Module_Mod {
	var volBus, buffers, bufferStream;

	*initClass {
		StartUp.add {
			SynthDef(\swoopDown2_mod, {| inBus, outBus, volBus, bufnum, time, loopTime, bottom0, bottom1, bottom2, bottom3 |
				var trig, out, out0, out1, ampMod0, ampMod1, verb, env, bigEnv, vol;

				trig = Impulse.kr(1/loopTime);

				RecordBuf.ar(In.ar(inBus,2), bufnum,loop:0);

				out0 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom0, time)*BufRateScale.kr(bufnum), trig, 5000, 0);
				out1 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom1, time)*BufRateScale.kr(bufnum), trig, 5000, 0);

				out0 = Latch.ar(out0[0], Impulse.ar(XLine.kr(44100, bottom2, time)));
				out1 = Latch.ar(out1[1], Impulse.ar(XLine.kr(44100, bottom3,time)));

				ampMod0 = 1-Lag.ar(Trig1.ar(Dust.kr(XLine.kr(5,25,time)), 0.1), 0.01);
				ampMod1 = 1-Lag.ar(Trig1.ar(Dust.kr(XLine.kr(5,30,time)), 0.1), 0.01);

				env = EnvGen.kr(Env([1,1,0,0],[2*time/3, time/3, time/2], 'sine'), doneAction:2);

				out0 = out0*ampMod0*env;
				out1 = out1*ampMod1*env;

				verb = GVerb.ar(out1+out0, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));

				bigEnv = EnvGen.kr(Env([0,1,1,0],[time/6, 2**time, time/6],'welch'));

				vol = In.kr(volBus);

				Out.ar(outBus, ([out0,out1]+verb)*bigEnv*vol);
			}).writeDefFile;
			SynthDef(\swoopDown4_mod, {| inBus, outBus, volBus, bufnum, time, loopTime, bottom0, bottom1, bottom2, bottom3 |
				var trig, out, out0, out1, out2, out3, out4, ampMod0, ampMod1, verb0, verb1, env, bigEnv, vol;

				trig = Impulse.kr(1/loopTime);

				RecordBuf.ar(In.ar(inBus,2), bufnum,loop:0);

				out0 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom0, time)*BufRateScale.kr(bufnum), trig, 5000, 0);
				out1 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom1, time)*BufRateScale.kr(bufnum), trig, 5000, 0);

				out0 = Latch.ar(out0[0], Impulse.ar(XLine.kr(44100, bottom2, time)));
				out1 = Latch.ar(out1[1], Impulse.ar(XLine.kr(44100, bottom3,time)));

				ampMod0 = 1-Trig1.ar(Dust.kr(XLine.kr(5,25,time)), 0.1);
				ampMod1 = 1-Trig1.ar(Dust.kr(XLine.kr(5,30,time)), 0.1);

				env = EnvGen.kr(Env([1,1,0,0],[2*time/3, time/3, time/2], 'sine'), doneAction:2);

				out = [out0*ampMod0*env, out1*ampMod1*env];

				#out1, out3 = Pan2.ar(out[0], Line.kr(-1, 1, time));
				#out2, out4 = Pan2.ar(out[1], Line.kr(-1, 1, time));

				verb0 = GVerb.ar(out1+out2, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));
				verb1 = GVerb.ar(out3+out4, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));

				bigEnv = EnvGen.kr(Env([0,1,1,0],[time/6, 2**time, time/6],'welch'));

				vol = In.kr(volBus);

				Out.ar(outBus, [out1,out2,out3, out4]*bigEnv*vol);
				Out.ar(outBus, verb0);
				Out.ar(outBus+2, verb1);
			}).writeDefFile;
			SynthDef(\swoopDown8_mod, {| inBus, outBus, volBus, bufnum, time, loopTime, bottom0, bottom1, bottom2, bottom3 |
				var trig, out, out0, out1, out2, out3, out4, out5, out6, out7, out8, ampMod0, ampMod1, verb0, verb1, verb2, verb3, env, bigEnv, vol;

				trig = Impulse.kr(1/loopTime);

				RecordBuf.ar(In.ar(inBus,2), bufnum,loop:0);

				out0 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom0, time)*BufRateScale.kr(bufnum), trig, 5000, 0);
				out1 = PlayBuf.ar(2, bufnum, XLine.kr(1, bottom1, time)*BufRateScale.kr(bufnum), trig, 5000, 0);

				out0 = Latch.ar(out0[0], Impulse.ar(XLine.kr(44100, bottom2, time)));
				out1 = Latch.ar(out1[1], Impulse.ar(XLine.kr(44100, bottom3,time)));

				ampMod0 = 1-Trig1.ar(Dust.kr(XLine.kr(5,25,time)), 0.1);
				ampMod1 = 1-Trig1.ar(Dust.kr(XLine.kr(5,30,time)), 0.1);

				env = EnvGen.kr(Env([1,1,0,0],[2*time/3, time/3, time/2], 'sine'), doneAction:2);

				out = [out0*ampMod0*env, out1*ampMod1*env];

				#out1, out3, out5, out7 = PanAz.ar(4, out[0], Line.kr(0, 1.5, time), orientation:0);
				#out2, out4, out6, out8 = PanAz.ar(4, out[1], Line.kr(0, 1.5, time), orientation:0);

				verb0 = GVerb.ar(out1+out2, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));
				verb1 = GVerb.ar(out3+out4, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));
				verb2 = GVerb.ar(out5+out6, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));
				verb3 = GVerb.ar(out7+out8, 80, 4.85, 0.41, 0.19, 0, 0, 0)*EnvGen.kr(Env([0,0,1],[time/6, 5*time/6]));

				bigEnv = EnvGen.kr(Env([0,1,1,0],[time/6, 2**time, time/6],'welch'));

				vol = In.kr(volBus);

				Out.ar(outBus, [out1, out2, out3, out4, out5, out6, out7, out8]*bigEnv*vol);
				Out.ar(outBus, verb0);
				Out.ar(outBus+2, verb1);
				Out.ar(outBus+4, verb2);
				Out.ar(outBus+6, verb3);
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SwoopDown", Rect(717, 512, 265, 55));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(2);

		volBus = Bus.control(group.server);

		volBus.set(0);

		buffers = List.new;
		5.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate * 2.0, 2))};
		bufferStream = Pseq([0,1,2,3,4], inf).asStream;

		controls.add(QtEZSlider("Vol", ControlSpec(0.0, 4.0, \amp), {arg slider;
				volBus.set(slider.value);
			}, 0, false, \horz));
		this.addAssignButton(0, \continuous);

		controls.add(Button()
			.states_([
				["Go", Color.green, Color.black],
				["Go", Color.black, Color.green]
			])
			.action = {arg butt;
				switch(numChannels,
					2,{
						Synth("swoopDown2_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \bufnum, buffers[bufferStream.next].bufnum, \time, rrand(8, 12), \loopTime, rrand(0.1,0.2), \bottom0, rrand(0.125, 0.25), \bottom1, rrand(0.125, 0.25), \bottom2, rrand(50, 100), \bottom3, rrand(150, 200)], group);
					},
					4,{
						Synth("swoopDown4_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \bufnum, buffers[bufferStream.next].bufnum, \time, rrand(8, 12), \loopTime, rrand(0.1,0.2), \bottom0, rrand(0.125, 0.25), \bottom1, rrand(0.125, 0.25), \bottom2, rrand(50, 100), \bottom3, rrand(150, 200)], group);
					},
					8,{
						Synth("swoopDown8_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \volBus, volBus.index, \bufnum, buffers[bufferStream.next].bufnum, \time, rrand(8, 12), \loopTime, rrand(0.1,0.2), \bottom0, rrand(0.125, 0.25), \bottom1, rrand(0.125, 0.25), \bottom2, rrand(50, 100), \bottom3, rrand(150, 200)], group);
					}
				)
			});
		this.addAssignButton(1, \onOff);

		//multichannel button
		numChannels = 2;
		controls.add(Button(win,Rect(5, 255, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
					},
					1, {
						numChannels = 4;
					},
					2, {
						numChannels = 8;
					}
				)
			};
		);

		win.layout_(
			VLayout(
				HLayout(controls[0], assignButtons[0]),
				HLayout(controls[1], assignButtons[1], nil)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;

	}

	pause {

	}

	unpause {

	}

	killMeSpecial {
		volBus.free;
		buffers.do{arg item; item.free};
	}
}