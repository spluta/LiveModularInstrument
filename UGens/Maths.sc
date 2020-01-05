Maths {

	*ar { | freq=10, width=0.5, logExp=0.3, loopOn = 0, trig = 0 |
		var slewMod, trigger, oscil, env;

		loopOn = loopOn.ceil;

		trigger = Select.kr(loopOn, [trig, ImpulseB.kr(freq, loopOn)]);

		env = Select.ar(loopOn, [EnvGen.ar(Env.new([0,1,1,0], [0.001,0.989,0.01]), trigger, 1, 0, 1/freq), K2A.ar(1)]);

		oscil = EnvGen.ar(Env.new([0,1,1,0,0], [0, width, 0, 1-width]), trigger, 1, 0, 1/freq);
		//oscil = LFPulse.ar(freq*loopOn,0, width);

		slewMod = Slew.ar(oscil, (1/width)*freq, (1/(1-width))*freq);

		^[env, trigger, SelectX.kr(logExp*3, [slewMod.explin(0.001, 1, 0, 1), slewMod, slewMod.linexp(0, 1, 0.001, 1)])];
	}

}