Envs {

	*kr { | muteGate=1, pauseGate=1, gate=1 |
		var env, pauseEnv, muteEnv;

		env = EnvGen.kr(Env.asr(0.1,1,0.1), gate, doneAction:2);
		pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.1), pauseGate, doneAction:1);
		muteEnv = EnvGen.kr(Env.asr(0.01,1,0.1), muteGate, doneAction:0);

		^env*pauseEnv*muteEnv
	}

}

OnOffStuff {

	*ar {|in, onOffSwitch=0, switchState=0, isCurrent=0, vol=0, chanVol=0|

		var out, switch;

		switch = Select.kr(switchState, [isCurrent, isCurrent*onOffSwitch, onOffSwitch]);
		out = in*switch*vol*chanVol;

		^out
	}
}