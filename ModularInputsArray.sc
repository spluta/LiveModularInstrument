ModularVolumeObject {
	var <inBus, <inputNum, outBus, synths, volIn, localResponder, peak, <rms;

	*new {arg inBus, inputNum;
		^super.newCopyArgs(inBus, inputNum).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("modularInput", {arg inBus, outBus, vol=1, gate=1;
				var in, env;

				env = EnvGen.kr(Env.asr(0,1,0), gate);

				in = SoundIn.ar(inBus)*env;

				SendPeakRMS.kr(in, 10, 3, "/inVol", inBus);

				Out.ar(outBus, in*Lag.kr(vol,0.01));
			}).writeDefFile;

			SynthDef("stereoModularInput", {arg inBus, outBus, vol=1, gate=1;
				var in, env;

				env = EnvGen.kr(Env.asr(0,1,0), gate);

				in = SoundIn.ar([inBus, inBus+1])*env;

				Out.ar(outBus, in*Lag.kr(vol,0.01));
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

		localResponder = OSCFunc({ |msg|

			if(msg[2]==inBus,{
				{
					rms.value = msg[4].ampdb.linlin(-40, 0, 0, 1);
					rms.peakLevel = msg[3].ampdb.linlin(-40, 0, 0, 1);
				}.defer
			});
		}, '/inVol', ModularServers.servers[0], nil, []);
	}

	addServer{arg server;
		synths.add(Synth("modularInput", [\inBus, inBus, \outBus, server.inBusses[inputNum]], server.inGroup));
		//stereo input busses is broken
		/*		if(inputNum.even,{
		synths.add(Synth("stereoModularInput", [\inBus, inBus, \outBus, server.stereoInBusses[inputNum/2]], server.inGroup));
		});*/
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

	var dispArray, win, <>outBusses, run, layouts, chanInBoxes;

	*initClass {
		StartUp.add {
			SynthDef("modularOutput", {arg inBus, outBus0, outBus1, outBus2, outBus3, outBus4, outBus5, outBus6, outBus7, lowDB = 0, midDB = 0, hiDB = 0, vol=1, gate=1, muteGate = 1;
				var in, env, muteEnv;

				env = EnvGen.kr(Env.asr(0,1,0), gate);
				muteEnv = EnvGen.kr(Env.asr(0.01,1,0.01), muteGate);

				in = In.ar(inBus,8);

				in = BLowShelf.ar(in, 80, 1, Lag.kr(lowDB));
				in = MidEQ.ar(in, 2500, 1, Lag.kr(midDB));
				in = BHiShelf.ar(in, 12500, 1, Lag.kr(hiDB));

				Out.ar(outBus0, in[0]*muteEnv*vol);
				Out.ar(outBus1, in[1]*muteEnv*vol);
				Out.ar(outBus2, in[2]*muteEnv*vol);
				Out.ar(outBus3, in[3]*muteEnv*vol);
				Out.ar(outBus4, in[4]*muteEnv*vol);
				Out.ar(outBus5, in[5]*muteEnv*vol);
				Out.ar(outBus6, in[6]*muteEnv*vol);
				Out.ar(outBus7, in[7]*muteEnv*vol);

				//Out.ar(outBus, in*Lag.kr(vol,0.01));
			}).writeDefFile;
		}
	}

	init {}

	init2 {arg inBusses;

		run = true;

		modName = "ModularVolumeRack";
		//this.initControlsAndSynths(2);

		synths = List.newClear(0);

		oscMsgs = List.newClear(ModularServers.servers['lmi0'].inBusses.size+5);
		controls = List.newClear(0);
		chanInBoxes = List.newClear(0);
		assignButtons = List.newClear(0);
		layouts = List.newClear;

		setups = ModularServers.setups;

		ModularServers.servers['lmi0'].inBusses.size.do{arg i;

			dispArray = dispArray.add(ModularVolumeObject(inBusses[i], i));  //this is sending the inBus that SoundIn uses in the MVO

			chanInBoxes.add(NumberBox().clipLo_(0).clipHi_(22)
				.action_{arg num;
					dispArray[i].setInputChannel(num.value);
				}
				.value_(inBusses[i]));

			controls.add(QtEZSlider("Amp", ControlSpec(0.001, 2, \amp),
				{arg slider;
					dispArray[i].setVol(slider.value);
			}, 1, true, \vert));
			this.addAssignButton(i,\continuous);

			controls[i].value = 1;

			layouts.add(VLayout(
				chanInBoxes[i].maxWidth_(40),
				dispArray[i].rms.maxHeight_(60),
				controls[i].maxHeight_(80).layout,
				assignButtons[i].layout.maxWidth_(40)
			))
		};

		ModularServers.servers.do{arg server;
			synths.add(Synth("modularOutput", [\inBus, server.mixerTransferBus, \outBus0, ModularServers.outBusses[0], \outBus1, ModularServers.outBusses[1], \outBus2, ModularServers.outBusses[2], \outBus3, ModularServers.outBusses[3], \outBus4, ModularServers.outBusses[4], \outBus5, ModularServers.outBusses[5], \outBus6, ModularServers.outBusses[6], \outBus7, ModularServers.outBusses[7]], server.postMixerGroup));
		};

		controls.add(QtEZSlider("LowShelf", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\lowDB, slider.value)}
		}, 0, true));
		this.addAssignButton(controls.size-1,\continuous);
		controls.add(QtEZSlider("MidEQ", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\midDB, slider.value)}
		}, 0, true));
		this.addAssignButton(controls.size-1,\continuous);
		controls.add(QtEZSlider("HiShelf", ControlSpec(-15,15),
			{arg slider;
				synths.do{arg item; item.set(\hiDB, slider.value)}
		}, 0, true));
		this.addAssignButton(controls.size-1,\continuous);


		controls.add(QtEZSlider("OutVol", ControlSpec(0,1,\amp),
			{arg slider;
				synths.do{arg item; item.set(\vol, slider.value)}
		}));
		this.addAssignButton(controls.size-1,\continuous);

		controls.add(Button()
			.states_([["through", Color.black, Color.green],["mute", Color.black, Color.red]])
			.action_{arg butt; synths.do{arg item; item.set(\muteGate, 1-butt.value)}};
		);
		this.addAssignButton(controls.size-1,\onOff);


		win = Window("Inputs/EQ/Output").layout_(
			HLayout(
				GridLayout.rows(layouts),
				VLayout(
					StaticText().string_("Equalization"),
					HLayout(controls[controls.size-5].layout, controls[controls.size-4].layout,controls[controls.size-3].layout),
					HLayout(assignButtons[assignButtons.size-5].layout,assignButtons[assignButtons.size-4].layout,assignButtons[assignButtons.size-3].layout)),
				VLayout(controls[controls.size-2].layout, assignButtons[assignButtons.size-2].layout, controls[controls.size-1], assignButtons[assignButtons.size-1].layout)
		));

		win.bounds_(Rect(786, 610, 412, 246));
		win.userCanClose_(false);
		win.front;
	}

	addAssignButton {|num, type|
		//I am overriding the addAssignButton method because it needs to work on all servers

		assignButtons.add(AssignButton.new()
			.instantAction_{|butt|
				if(butt.value==1,{
					waitForSetNum = num;
					MidiOscControl.requestInstantAssign(this, controls[num], type, 'global', setups);
				},{
					MidiOscControl.clearInstantAssign;

					MidiOscControl.clearController('global', oscMsgs[num]);
					oscMsgs.put(num, nil);
				})
		});
	}

	load {arg loadArray;
		//I am overriding the load method because it needs to work on all servers

		loadArray[1].do{arg controlLevel, i; controls[i].valueAction_(controlLevel)};
		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				ModularServers.servers.do{arg server;
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, server.name, setups);
				};
				assignButtons[i].instantButton.value_(1);
			});
		};
		win.bounds_(loadArray[3]);
		this.loadExtra(loadArray);
	}

	addServer{arg server;
		dispArray.do{arg item;
			item.addServer(server);
		};
		synths.add(Synth("modularOutput", [\inBus, server.mixerTransferBus, \outBus0, ModularServers.outBusses[0], \outBus1, ModularServers.outBusses[1], \outBus2, ModularServers.outBusses[2], \outBus3, ModularServers.outBusses[3], \outBus4, ModularServers.outBusses[4], \outBus5, ModularServers.outBusses[5], \outBus6, ModularServers.outBusses[6], \outBus7, ModularServers.outBusses[7]], server.postMixerGroup));
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