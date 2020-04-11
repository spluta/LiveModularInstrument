(
a = SynthDef("FM7Saw_NNMod", {
	var ctls, mods, sig, filt, envs, onOff;

	ctls = 3.collect{|i|2.collect{|i2| LFSaw.ar(*["frC","mulC","addC"].collect{|name| NamedControl.kr((name++i++i2).asSymbol, 1.5.linrand+0.5, 0.1)}.insert(1,0))}.insert(1,0)}.addAll(0!3!3);

	mods = 3.collect{|i|3.collect{|i2| LFSaw.ar(*["frM","mulM","addM"].collect{|name| NamedControl.kr((name++i++i2).asSymbol, 1.5.linrand+0.5, 0.1)}.insert(1,0))}.addAll(0!3)}.addAll(0!6!3);

	sig = Splay.ar(FM7.ar(ctls, mods).slice([0,1]), 1)*0.5;

	envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	onOff = Lag.kr(In.kr(\onOffBus.kr), 0.01);

	filt = SelectX.ar( \filtMode.kr(0), [
		BMoog.ar(sig, \filtFrq.kr(500).clip(20, 20000), \filtQ.kr(0.2).clip(0.001, 0.999), 0, \filtSat.kr(0.95)),
		BBandPass.ar(sig, \filtFrq.kr, \filtQ.kr)]);

	sig = SelectX.ar(\filtMix.kr(0), [sig, filt]);

	sig = Normalizer.ar(sig, 0.9);

	sig = CrossoverDistortion.ar(sig, \distAmp.kr(1), \distSmooth.kr(0.5));

	sig = Limiter.ar(sig*envs*onOff*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1), 0.9);

	Out.ar(\outBus.kr(0), sig);

}).load(ModularServers.servers[\lmi1].server)
)


Lemur_Mod.sendOSC("/Container2/Container2/Fader/x", 1.0.rand)

{CrossoverDistortion.ar(SinOsc.ar([400, 404], 0, 0.2), MouseX.kr(0, 1), MouseY.kr(0, 1))}.scope


s.boot;
(
z = {
	SelectX.ar(
	MouseY.kr(0.0, 1.0, \linear),
    [BMoog.ar(
		WhiteNoise.ar(0.2),
        MouseX.kr(40,20000,\exponential), // cutoff freq.
			0.5, // q
			0, // mode - lowpass
        0.25), // mul
		BBandPass.ar(WhiteNoise.ar(0.2),
			MouseX.kr(40,20000,\exponential), // cutoff freq.
			0.5, // q
			0.5)
		]
	);
}.play)

(
z = {
	SelectX.ar(
	MouseY.kr(0.0, 2.0, \linear),
    RHPF.ar(
		WhiteNoise.ar(0.2),
        MouseX.kr(40,20000,\exponential), // cutoff freq.
			0.5, // q
        0.25); // mul
	);
}.play)
z.release;