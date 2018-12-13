LucerneVideo_Mod : SignalSwitcher_Mod {
	var fftBufs, volOSC, trigOSC, qcWin, qcView, squibblesPast, lastPlayerNumber;

	*initClass {
		StartUp.add {
			SynthDef("partALucerne_mod", {arg inBus0, inBus1, inBus2, inBus3, fftBuf0, fftBuf1, fftBuf2, fftBuf3, thresh;
				var amp, fft0, fft1, fft2, fft3, in0, in1, in2, in3, onsets0, onsets1, onsets2, onsets3;

				in0 = In.ar(inBus0);
				in1 = In.ar(inBus1);
				in2 = In.ar(inBus2);
				in3 = In.ar(inBus3);

				fft0 = FFT(fftBuf0, in0);
				fft1 = FFT(fftBuf1, in1);
				fft2 = FFT(fftBuf2, in2);
				fft3 = FFT(fftBuf3, in3);

				onsets0 = Onsets.kr(fft0, thresh);
				onsets1 = Onsets.kr(fft1, thresh);
				onsets2 = Onsets.kr(fft2, thresh);
				onsets3 = Onsets.kr(fft3, thresh);

				SendTrig.kr(onsets0, 0, 1);
				SendTrig.kr(onsets1, 1, 1);
				SendTrig.kr(onsets2, 2, 1);
				SendTrig.kr(onsets3, 3, 1);

				SendPeakRMS.kr(in0+in1+in2+in3, 20, 1, "/sigAmplitude");
			}).writeDefFile;
		}
	}

	init2 {
		[soundInBusses, stereoSoundInBusses, location, mainProcessingWindow];
		this.makeWindow("LucerneVideo", Rect(860, 200, 220, 150));
		this.initControlsAndSynths(4);

		mixerGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		localBusses = List.new;
		4.do{localBusses.add(Bus.audio(group.server, 8))};

		mixerStrips = List.new;
		4.do{arg i; mixerStrips.add(DiscreteInput_Mod(mixerGroup, localBusses[i], win, Point(5+(i*55), 0), nil))};

		fftBufs = List.newClear(0);
		4.do{fftBufs.add(Buffer.alloc(group.server, 512))};

		qcWin = SCWindow("poopButt",  Rect(0,0, 800, 610));
		qcView = SCQuartzComposerView(qcWin, Rect(0, 0, qcWin.bounds.width, qcWin.bounds.height)).maxFPS_(30);
		//qcView.path = Document.current.path.dirname ++ "/LucerneVideos/Images1.qtz";
		qcView.path = "/Users/sam/Library/Application\ Support/SuperCollider/Extensions/interface\ 3.6/modules/LucerneVideos/Images1.qtz";
		qcView.start;
		qcWin.front;

		qcWin.userCanClose_(false);

		qcWin.alwaysOnTop_(true);

		qcView.setInputValue("Enable0", true);
		qcView.setInputValue("Enable1", true);
		qcView.setInputValue("Enable2", true);
		qcView.setInputValue("Enable3", true);

		squibblesPast = Array.fill(4, {this.makeDaPlace.dup});

		rout = Routine({
			group.server.sync;
			0.4.wait;

			synths = List.newClear(3);
			synths.put(0, Synth("partALucerne_mod", [\inBus0, localBusses[0].index, \inBus1, localBusses[1].index, \inBus2, localBusses[2].index, \inBus3, localBusses[3].index, \fftBuf0, fftBufs[0], \fftBuf1, fftBufs[1], \fftBuf2, fftBufs[2], \fftBuf3, fftBufs[3], \thresh, 0.5], synthGroup));

			4.do{arg i;
				controls.add(Button.new(win,Rect(5+(i*55), 70, 55, 20))
					.states_([["", Color.black, Color.green],["", Color.black, Color.blue]])
					.action_({|v|
						this.changeDaPlace(i);
					}, 1.0, false, layout:\horz));
				this.addAssignButton(i,\onOff, Rect(5+(i*55), 90, 55, 20));
			};


			1.wait;

			volOSC = OSCFunc({ |msg|
				{
					qcView.setInputValue("Alpha0", msg[3]);
					qcView.setInputValue("Alpha1", msg[3]);
					qcView.setInputValue("Alpha2", msg[3]);
					qcView.setInputValue("Alpha3", msg[3]);
				}.defer;
			}, '/sigAmplitude');

			trigOSC = OSCFunc({ |msg|
				{
					this.changeDaPlace(msg[2]);
				}.defer;
			}, '/tr');

		});

		AppClock.play(rout);
	}

	makeDaPlace {
		var xLo, yLo, locArray;

		locArray = List.newClear(0);

		xLo = rrand(0.1, 1.6);
		yLo = rrand(0.1, 1.6);

		4.do{arg i;
			//xLo, xHi, yLo, yHi, periodLow, periodHigh, minWidth, maxWidth
			locArray.add([xLo, (xLo+rrand(0.15, 0.4)).fold(0,1.8), yLo, (yLo+rrand(0.15, 0.4)).fold(0,1.8),
				rrand(0.03, 0.04), rrand(0.08, 0.1), rrand(0.005, 0.02), rrand(0.025, 0.04)]);
		};
		^locArray
	}

	changeDaPlace {arg playerNumber;
		var array;

		if(lastPlayerNumber != playerNumber,{

			lastPlayerNumber = playerNumber;

			if(0.5.coin, {
				array = squibblesPast[playerNumber].choose;
			},{
				array = this.makeDaPlace;
				squibblesPast[playerNumber].put([0,1].choose, array);
			});

			4.do{arg i;
				qcView.setInputValue("xMin"++i.asString, array[i][0]);
				qcView.setInputValue("xMax"++i.asString, array[i][1]);
				qcView.setInputValue("yMin"++i.asString, array[i][2]);
				qcView.setInputValue("yMax"++i.asString, array[i][3]);
				qcView.setInputValue("periodLow"++i.asString, array[i][4]);
				qcView.setInputValue("periodHigh"++i.asString, array[i][5]);
				qcView.setInputValue("minWidth"++i.asString, array[i][6]);
				qcView.setInputValue("maxWidth"++i.asString, array[i][7]);
			};
			controls[playerNumber].value_(controls[playerNumber].value+1);
		})
	}

	killMeSpecial {
		volOSC.free;
		trigOSC.free;
		qcWin.close;
	}
}
