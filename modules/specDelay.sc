SpecDelay_Mod : Module_Mod {
	var dels0, dels1, dels2, dels3, fb0, fb1, dels0Buf, dels1Buf, fft0Buf, fft1Buf, fb0Buf, fb1Buf, cond, rout, setupRout, maxDelay, maxFeedBack;

	*initClass {
		StartUp.add {


			SynthDef("specDelay_mod", { arg inBus,outBus,inVol=0,outVol=0,bufnum=0, dels0Buf=0, dels1Buf =0, fft0Buf=0, fft1Buf =0,fb0Buf=0, gate=1, pauseGate=1;
				var in, out, chain, chain1, chain2, chain3, env, pauseEnv;

				in = In.ar(inBus)*inVol;

				in = LPF.ar(in, 3000);
				in = HPF.ar(in, 150);

				chain = FFT(fft0Buf, in, 0.25);

				chain = PV_PartialSynthF(chain, 5, 6, 0);

				chain1 = PV_Copy(chain, fft1Buf);

				chain = PV_BinDelay(chain, 5, dels0Buf, fb0Buf, 0.25);
				chain1 = PV_BinDelay(chain1, 5, dels1Buf, fb0Buf, 0.25);

				env = EnvGen.kr(Env.asr(0.1,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = [IFFT(chain), IFFT(chain1)];

				Out.ar(outBus, out*env*pauseEnv*outVol );
			}).writeDefFile;

		}
	}

	init {
		this.makeWindow("SpecDelay", Rect(833, 400, 381, 138));
		this.initControlsAndSynths(5);

		this.makeMixerToSynthBus;

		setupRout = Routine ({

			fft0Buf = Buffer.alloc(group.server, 2048, 1);
			fft1Buf = Buffer.alloc(group.server, 2048, 1);

			dels0Buf = Buffer.alloc(group.server, 1024, 1);
			dels1Buf = Buffer.alloc(group.server, 1024, 1);

			fb0Buf = Buffer.alloc(group.server, 1024, 1);

			group.server.sync;
			1.4.wait;

			dels0Buf.setn(0, Array.fill(1024, { rrand(0.5,1.5) }));
			group.server.sync;
			0.4.wait;
			dels1Buf.setn(0, Array.fill(1024, { rrand(0.5,1.5) }));
			group.server.sync;
			0.4.wait;
			fb0Buf.setn(0, Array.fill(1024, { rrand(0.2,0.6) }));

			group.server.sync;
			0.4.wait;

			synths.add(Synth("specDelay_mod", [\inBus, mixerToSynthBus.index.postln, \outBus, outBus, \inVol, 0, \outVol, 1.5, \partialThresh, 5, \lowPassFreq, 4000, \dels0Buf, dels0Buf, \dels1Buf, dels1Buf, \fft0Buf, fft0Buf, \fft1Buf, fft1Buf, \fb0Buf, fb0Buf, \maxDelay, 3, \maxFeedBack, 0.4], group));

			controls.add(QtEZSlider.new("inVol", ControlSpec(0,1,'amp'),
				{|v|
					synths[0].set(\inVol, v.value);
			}, 0, false, \horz));
			this.addAssignButton(0,\continuous);

			controls.add(QtEZSlider.new("outVol", ControlSpec(0, 2,'linear'),
				{|v|
					synths[0].set(\outVol, v.value);
			}, 1.5, false, \horz));
			this.addAssignButton(1,\continuous);

			controls.add(QtEZSlider.new("maxDelay", ControlSpec(0,5,'linear'),
				{|v|
					synths[0].set(\maxDelay, v.value);
			}, 0.5, false, \horz));
			this.addAssignButton(2,\continuous);

			controls.add(QtEZSlider.new("maxFeedback", ControlSpec(0,0.8,'linear'),
				{|v|
					synths[0].set(\maxFeedback, v.value);
			}, 0.5, false, \horz));
			this.addAssignButton(3,\continuous);

			this.startChangeRoutine;

			//multichannel button
			numChannels = 2;
			//		controls.add(Button(win,Rect(10, 255, 60, 20))
			//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			//			.action_{|butt|
			//				switch(butt.value,
			//					0, {
			//						numChannels = 2;
			//						if(synths.size>4,{
			//							synths[4..].do{arg item; item.set(\gate, 0)};
			//						});
			//						synths.do{arg item, i; item.set(\outBus, outBus.index+(i%2))};
			//					},
			//					1, {
			//						numChannels = 4;
			//						synths.do{arg item, i; item.set(\outBus, outBus.index+i)};
			//					},
			//					2, {
			//						numChannels = 8;
			//						synths.do{arg item, i; item.set(\outBus, outBus.index+i)};
			//						4.do{|i|synths.add(Synth("delayFeedback_mod",[\inBus, outBus.index+(i%2), \outBus, outBus.index+i, \volBus, volBus.index], synthGroup))};
			//					}
			//				)
			//			};
			//		);

			win.layout_(VLayout(
				HLayout(controls[0].layout, assignButtons[0].layout),
				HLayout(controls[1].layout, assignButtons[1].layout),
				HLayout(controls[2].layout, assignButtons[2].layout),
				HLayout(controls[3].layout, assignButtons[3].layout))
			);
			win.layout.spacing = 0;
			win.layout.margins = [0,0,0,0];
			win.front;

		});
		setupRout.play(AppClock);


	}

	startChangeRoutine {
		rout = Routine({{

			dels0Buf.setn(1000.rand, Array.series(rrand(10,20), rrand(0.4, 1.2), rrand(0.02, 0.06)));
			0.1.wait;
			dels1Buf.setn(1000.rand, Array.series(rrand(10,20), rrand(0.4, 1.2), rrand(0.02, 0.06)));
			0.1.wait;

			fb0Buf.setn(1000.rand, Array.series(rrand(10,20), rrand(0.1, 0.4), rrand(0.005, 0.01)));
			0.3.wait;
		}.loop});
		SystemClock.play(rout);
	}

	stopChangeRoutine {
		rout.stop;
	}

	resumeChangeRoutine {
		rout.reset;
		rout.play;
	}

	pause {
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
		this.stopChangeRoutine;
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
		this.resumeChangeRoutine;
	}

	killMeSpecial {
		this.stopChangeRoutine;
		fft0Buf.free;
		fft1Buf.free;
		dels0Buf.free;
		dels1Buf.free;
		fb0Buf.free;
	}
}