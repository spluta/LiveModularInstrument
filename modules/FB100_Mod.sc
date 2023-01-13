FB100_Mod : ProtoType_Mod {

	*initClass {
		StartUp.add {
			SynthDef(\fb100_mod,{arg inBus, controlsBus, outBus;
				var snd, num, trig0, trig1, trig, locIn, verb, env, controls, vol;
				num = 4;
				snd = Impulse.ar(0)!num;

				//trig = MouseButton.kr(lag:0);
				controls = In.kr(controlsBus, 10);

				trig0 = controls[0];
				trig1 = controls[1];
				vol = controls[2];

				env = (trig0+trig1).clip(0,1).lag(0.01,0.01);

				trig = Trig1.kr(trig0, 0.001)+Trig1.kr(trig1, 0.001);

				locIn = LocalIn.ar(num)*0.1;

				locIn = SelectX.ar(TWChoose.kr(trig, [0,1], [0.6,0.4].normalizeSum).lag(0.01),
		[locIn, BitCrusher.ar(locIn, {(TRand.ar(1,32, trig!num))}, {TRand.ar(200,40000,trig!num)})]
	);

	locIn = BHiShelf.ar(locIn, TExpRand.ar(1000,10000,trig), 1, TRand.ar(0, -40, trig));

				snd = snd + locIn.sum;

				snd = Resonz.ar(snd, {TExpRand.kr(0.0001, 10000, trig!num) }, {TRand.kr(0.01, 0.3, trig!num) });
				snd = Integrator.ar(snd, {TRand.kr(0.97, 0.99, trig!num) });

				snd = snd * ({ { LFNoise1.kr(TRand.kr(0.001, 0.1, trig)).range(TRand.kr(-500,500, trig)) } ! num } ! num);
				snd = snd.sum;
				snd = LeakDC.ar(snd);

				LocalOut.ar(DelayC.ar(snd.clip2(TRand.ar(100,1000,trig)).(LPF.ar(_, 30000)),512/SampleRate.ir,(({TRand.ar(0,1.0,trig!num)}**8)*512/SampleRate.ir)));

				snd = snd.softclip.select{|item, i| i<4};

				snd = snd.(LPF.ar(_, 20000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

				Out.ar(outBus, snd*vol*env/2);
			}).writeDefFile;

			/*SynthDef(\fb100_mod,{arg inBus, controlsBus, outBus;
				var snd, num, trig0, trig1, trig, locIn, verb, env, controls, vol;
				num = 10;
				snd = Impulse.ar(0)!num;

				//trig = MouseButton.kr(lag:0);
				controls = In.kr(controlsBus, 10);

				trig0 = controls[0];
				trig1 = controls[1];
				vol = controls[2];

				env = (trig0+trig1).clip(0,1).lag(0.01,0.01);

				trig = Trig1.kr(trig0, 0.001)+Trig1.kr(trig1, 0.001);

				locIn = LocalIn.ar(num)+TChoose.kr(trig, Array.fill(9, {|i| (1!(i+2)).addAll(0!(8-i))}));
				snd = snd + locIn.sum;

				snd = Resonz.ar(snd, {TExpRand.kr(0.0001, 10000, trig!num) }, {TRand.kr(0.01, 5, trig!num) });
				Integrator.ar(snd, {TRand.kr(0.97, 0.99, trig!num) });
				snd = snd * ({ { LFNoise1.kr(TRand.kr(0.001, 0.1, trig)).range(TRand.kr(-500,500, trig)) } ! num } ! num);
				snd = snd.sum;
				snd = LeakDC.ar(snd);

				LocalOut.ar(DelayC.ar(snd.clip2(100).(LPF.ar(_, 30000)),512/SampleRate.ir,((TRand.ar(0,1.0,trig)**8)*512/SampleRate.ir)));

				snd = snd.softclip.select{|item, i| i<2};

				snd = snd.(LPF.ar(_, 25000)).(HPF.ar(_, 20)).(BLowShelf.ar(_, 1200, 1, 5));

				Out.ar(outBus, snd*vol*env/2);
			}).writeDefFile;*/

		}
	}

	loadExtra {
	}

	init {
		numControls = 10;
		textList = Array.fill(numControls, {"text"});
		withTextView = false;
		this.init2;
	}

	init3 {
		synths.add(Synth("fb100_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
		synths.add(Synth("fb100_mod", ['inBus', mixerToSynthBus, 'controlsBus', controlsBus, 'outBus', outBus], group));
	}
}