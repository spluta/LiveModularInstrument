ModularVolumeObject {
	var <inBus, <inputNum, outBus, synths, volIn, localResponder, peak, <rms;

	*new {arg inBus, inputNum;
		^super.newCopyArgs(inBus, inputNum).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("modularInput_mod", {arg inBus, outBus, vol=1, gate=1;
				var in, env;

				env = EnvGen.kr(Env.asr(0,1,0), gate);

				in = SoundIn.ar(inBus)*env;

				SendPeakRMS.kr(in, 10, 3, "/inVol", inBus);

				Out.ar(outBus, in*Lag.kr(vol,0.01));
			}).writeDefFile;

			SynthDef("directInputs_mod", {arg outBus;
				Out.ar(outBus, SoundIn.ar((0..21)));
			}).writeDefFile;
		};
	}

	init {
		volIn = 0;

		synths = List.newClear(0);

		ModularServers.servers.do{arg server;
			this.addServer(server);
		};

		rms = LevelIndicator();
	}

	addServer{arg server;
		synths.add(Synth("modularInput_mod", [\inBus, inBus, \outBus, server.inBusses[inputNum]], server.inGroup));
		synths.add(Synth("directInputs_mod", [\outBus, server.mixerDirectInBus], server.inGroup));

		localResponder.free;
		localResponder = OSCFunc({ |msg|

			if(msg[2]==inBus,{
				{
					rms.value = msg[4].ampdb.linlin(-40, 0, 0, 1);
					rms.peakLevel = msg[3].ampdb.linlin(-40, 0, 0, 1);
				}.defer
			});
		}, '/inVol', ModularServers.servers[0], nil, [synths[0].nodeID]);
	}

	setInputChannel {arg inBusIn;
		inBus = inBusIn;
		synths.do{arg item; item.set(\inBus, inBus)};
	}

	setVol {arg vol;
		synths.do{arg item; item.set(\vol, vol)};
	}

	mute {
		synths.do{arg synth; synth.set(\gate, 0)};
	}

	unmute {
		synths.do{arg synth; synth.set(\gate, 1)};
	}

	killMe {
		localResponder.free;
	}

}

ModularInputsArray : Module_Mod {
	//inherits from Module_Mod, but 'group' and 'outBus' are not used

	var dispArray, win, <>outBusses, run, layouts, chanInBoxes, numBusses;

	init {}

	init2 {arg inBusses;

		run = true;

		modName = "ModularVolumeRack";
		//"loadVolumeRack".postln;
		this.initControlsAndSynths(16);

		isGlobalController = true;

		synths = List.newClear(0);

		oscMsgs = List.newClear(ModularServers.servers['lmi1'].inBusses.size+5);
		//controls = List.newClear(0);
		chanInBoxes = List.newClear(0);
		//assignButtons = List.newClear(0);
		layouts = List.newClear;

		numBusses = ModularServers.servers['lmi1'].inBusses.size;

		numBusses.do{arg i;

			dispArray = dispArray.add(ModularVolumeObject(inBusses[i]-1, i));  //this is sending the inBus that SoundIn uses in the MVO

			chanInBoxes.add(NumberBox().clipLo_(1).clipHi_(22)
				.action_{arg num;
					dispArray[i].setInputChannel(num.value-1);
				}
				.value_(inBusses[i])
				.maxHeight_(15).font_("Helvetica",10)
			);

			controls.add(QtEZSlider(nil, ControlSpec(0.001, 2, \amp),
				{arg slider;
					dispArray[i].setVol(slider.value);
			}, 1, true, \vert));
			this.addAssignButton(i,\continuous);

			controls[i].value = 1;

		};

		numBusses.do{arg i;
			controls.add(TextField()
				.action_{arg text;
					Lemur_Mod.netAddrs.do{arg addr; addr.sendMsg(("/MixerLabel"++(i+1).asString).asSymbol, "@content", text.value)};
			});

			layouts.add(VLayout(
				controls[i+numBusses],
				chanInBoxes[i].maxWidth_(40),
				HLayout(dispArray[i].rms.maxWidth_(10),controls[i].maxWidth_(30)),
				assignButtons[i].layout.maxWidth_(40)
			).margins_(1!4).spacing_(1));
		};

		win = Window("Inputs").layout_(GridLayout.rows(layouts).margins_(1!4).spacing_(1));

		win.bounds_(Rect(786, 610, 412, 140));
		win.userCanClose_(false);
		win.front;
	}

	addServer{arg server;
		dispArray.do{arg item;
			item.addServer(server);
		};
		synths.add(Synth("modularOutput_mod", [\inBus, server.mixerTransferBus, \outBus0, outBusses[0], \outBus1, outBusses[1], \outBus2, outBusses[2], \outBus3, outBusses[3], \outBus4, outBusses[4], \outBus5, outBusses[5], \outBus6, outBusses[6], \outBus7, outBusses[7]], server.postMixerGroup));
	}


	killMeSpecial {
		group.free;
		run = false;
		win.close;
		dispArray.do{arg item;
			item.killMe;
		};
	}
}