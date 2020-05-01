(
SynthDef("TVFeedback_NNMod", {
	var in, sig, audioOut, onOffSwitch, envs;

	sig = SoundIn.ar([0,1,4,5,6,7], (6.collect{|i| NamedControl.kr(("\inGain"++i).asSymbol, 4, 0.1)}));

	sig = 6.collect{|i|
/*BLowShelf.ar(sig[i], NamedControl.kr(("\lowFrq"++i).asSymbol, 80, 0.1), 1, NamedControl.kr(("\lowDB"++i).asSymbol, 0, 0.1))*/
	BHiShelf.ar(
			MidEQ.ar(
				MidEQ.ar(
					BLowShelf.ar(sig[i], NamedControl.kr(("\lowFrq"++i).asSymbol, 80, 0.1), 1, NamedControl.kr(("\lowDB"++i).asSymbol, 0, 0.1)),
					NamedControl.kr(("\midFrqA"++i).asSymbol, 1200, 0.1), 4, NamedControl.kr(("\midDBA"++i).asSymbol, 0, 0.1)),
				NamedControl.kr(("\midFrqB"++i).asSymbol, 3500, 0.1), 4, NamedControl.kr(("\midDBB"++i).asSymbol, 0, 0.1)),
			NamedControl.kr(("\hiFrq"++i).asSymbol, 12500, 0.1), 1, NamedControl.kr(("\hiDB"++i).asSymbol, 0, 0.1)
		);
	};

	envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	sig = Normalizer.ar(Mix(sig))*envs;

	onOffSwitch = (\onOff0.kr(0, 0.01)+\onOff1.kr(0, 0.01)).clip(0,1);

	onOffSwitch = Select.kr(\switchState.kr(0), [\isCurrent.kr(0, 0.01), \isCurrent.kr*onOffSwitch, onOffSwitch]);

	sig = sig*onOffSwitch;

	audioOut = LPF.ar(LPF.ar(IFFT(PV_BrickWall(FFT(LocalBuf(1024), sig), -0.5)), 10000), 10000);

	Out.ar(\outBus.kr(0), [sig*\tvVol.kr(0, 0.1),audioOut*Lag.kr(In.kr(\volBus.kr), 0.1)]);
}).load(ModularServers.servers[\lmi1].server))



t.set(\lowDB0, 25)
t.set(\hiDB, 15)


(
a = {
	var in;
	in = WhiteNoise.ar(0.1);

	in = BLowShelf.ar(in, \lowFrq.kr(200, 0.1), 1, \lowDB.kr(0, 0.1));
	in = MidEQ.ar(in, \midFrqa.kr(800, 0.1), 4, \midDBa.kr(0, 0.1));
	in = MidEQ.ar(in, \midFrqb.kr(2250, 0.1), 4, \midDBb.kr(0, 0.1));
	in = MidEQ.ar(in, \midFrqc.kr(5500, 0.1), 4, \midDBc.kr(0, 0.1));
	in = BHiShelf.ar(in, \hiFrq.kr(12500, 0.1), 1, \hiDB.kr(0, 0.1));

	Out.ar(0, in.dup);
}.play)


a.set(\lowDB, -25)

a.set(\midDBa, -25)
a.set(\midFrqa, 1100)