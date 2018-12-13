Envs {

	*kr { | muteGate=1, pauseGate=1, gate=1 |
		var slewMod;

		env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
		pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.1), pauseGate, doneAction:1);
		muteEnv = EnvGen.kr(Env.asr(0.01,1,0.1), muteGate, doneAction:1);

		^env*pauseEnv*muteEnv
	}

}