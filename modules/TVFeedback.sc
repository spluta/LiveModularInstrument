TVFeedback_Mod : ModularMainMixer {
	var volsBus, points, temp, tempOrder, tvBus;

	*initClass {
		StartUp.add {
			SynthDef("tvFeedback_mod", {arg inBus0, inBus1, outBus, filterBoost=7, filterSpeed=0.005, rq = 0.75, delaytime = 0.1, gate=1, pauseGate=1;
				var env, in0, in1, pauseEnv, freq, chain, out, amp, tvOut;

				in0 = Normalizer.ar(In.ar(inBus0));

				in1 = In.ar(inBus1)*10;

				amp = Amplitude.ar(in1);

				freq = SinOsc.kr(filterSpeed, 0).range(30, 10000);
				in0 = MidEQ.ar(in0, freq, rq, filterBoost);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = in0*env*pauseEnv;

				chain = FFT(LocalBuf(512), in0);

				out = IFFT(PV_BrickWall(chain, -0.5));
				tvOut = (in0*(1-min(amp*5, 1)))+(in1*EnvGen.kr(Env.asr(0.01, 1, 0.2), amp>0.1).poll);

				//tvOut = (in0*(1-min(amp*5, 1)))+in1;

				Out.ar(outBus, [LPF.ar(LPF.ar(out, 8000), 8000), tvOut]);

			}).writeDefFile;
		}
	}


	init3 {
		synthName = "TVFeedback";

		win.name = "TVFeedback"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		synths.add(Synth("tvFeedback_mod", [\inBus0, localBusses[0].index, \inBus1, localBusses[1].index, \outBus, outBus], outGroup));

		//tvBus = Bus.audio(group.server, 2);

		//channelOutBox = ChannelOutBox(tvBus.index+1);

/*		controls.add(EZSlider.new(win,Rect(10, 70, 200, 20), "filterBoost", ControlSpec(-10,20),
			{|v|
				synths[0].set(\filterBoost, v.value);
			}, 0, true));

		controls.add(EZSlider.new(win,Rect(10, 90, 200, 20), "filterSpeed", ControlSpec(0, 0.01, 'lin'),
			{|v|
				synths[0].set(\filterSpeed, v.value);
			}, 0.005, true));

		controls.add(EZSlider.new(win,Rect(10, 110, 200, 20), "rq", ControlSpec(0.1, 1, 'exponential'),
			{|v|
				synths[0].set(\rq, v.value);
			}, 0, true));

		controls.add(EZSlider.new(win,Rect(10, 130, 200, 20), "delTime", ControlSpec(0.0002, 0.2),
			{|v|
				synths[0].set(\delaytime, v.value);
			}, 0.1, true));*/

		win.layout_(HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0));
		win.front;
	}

	killMeSpecial {
		localBusses.do{arg item; item.free};
		mixerGroup.free;
		synthGroup.free;
	}
}
