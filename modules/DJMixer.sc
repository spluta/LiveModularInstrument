DJMixer_Mod : SignalSwitcher_Mod {

	*initClass {
		StartUp.add {
			SynthDef("djMixer_mod", {arg inBus0, inBus1, outBus, gate = 1, pauseGate = 1;
				var in0, in1, env, out, pauseEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				in0 = In.ar(inBus0, 2);
				in1 = In.ar(inBus1, 2);

				out = (in0*(1-\djSlider.kr(0))+(in1*\djSlider.kr));

				Out.ar(outBus, out, 0);
			}).writeDefFile;
		}
	}

	init3 {
		synthName = "DJMixer";

		win.name = "DJMixer"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		this.initControlsAndSynths(1);

		synths.add(Synth("djMixer_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \outBus, outBus], outGroup));

		controls.add(QtEZSlider(nil, ControlSpec(0,1), {|val| synths[0].set(\djSlider, val.value)}, 0, true, 'horz', false));
		this.addAssignButton(0, \continuous);

		win.layout_(
			VLayout(
				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
				HLayout(controls[0], assignButtons[0].maxWidth_(40))
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.front;
	}
}