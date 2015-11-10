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

				SendPeakRMS.kr(in, 10, 3, "/vol", inBus);

				Out.ar(outBus, in*Lag.kr(vol,0.01));
				//Out.ar(stereoOutBus, in*Lag.kr(vol,0.01));
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
		}, '/vol');
	}

	addServer{arg server;
		synths.add(Synth("modularInput", [\inBus, inBus.post, \outBus, server.inBusses[inputNum].postln], server.inGroup));
		if(inputNum.even,{
			synths.add(Synth("stereoModularInput", [\inBus, inBus, \outBus, server.stereoInBusses[inputNum].postln], server.inGroup));
		});
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

	var dispArray, win, <>outBusses, run, layouts;

	init {}

	init2 {arg inBusOffset;

		run = true;

		modName = "ModularVolumeRack";
		//this.initControlLists(outBus[0].size);

		oscMsgs = List.newClear(ModularServers.servers['lmi0'].inBusses.size);
		controls = List.newClear(0);
		assignButtons = List.newClear(0);
		layouts = List.newClear;

		setups = ModularServers.setups;

		ModularServers.servers['lmi0'].inBusses.size.do{arg i;

			dispArray = dispArray.add(ModularVolumeObject(i+inBusOffset, i));  //this is sending the inBus that SoundIn uses in the MVO

			controls.add(QtEZSlider("Amp", ControlSpec(0.001, 2, \amp),
				{arg slider;
					dispArray[i].setVol(slider.value);
			}, 1, true, \vert));
			this.addAssignButton(i,\continuous);

			controls[i].value = 1;

			layouts.add(VLayout(dispArray[i].rms.maxHeight_(60),
				controls[i].maxHeight_(80).layout,
				assignButtons[i].layout.maxWidth_(40)
			))
		};

		win = Window("Inputs").layout_(GridLayout.rows(layouts));

		win.bounds_(Rect(786, 610, 412, 246));
		win.userCanClose_(false);
		win.front;
	}

	addAssignButton {|num, type|
		//I am overriding the addAssignButton method because it needs to work on all servers

		assignButtons.add(AssignButton.new()
			.instantAction_{|butt|
				if(butt.value==1,{

					//module, controlObject, typeOfController, server, setupsIn
					waitForSetNum = num;
					ModularServers.servers.do{arg server;
						MidiOscControl.requestInstantAssign(this, controls[num], type, server.name, setups);
					};


					},{
						MidiOscControl.clearInstantAssign;
						ModularServers.servers.do{arg server;
							MidiOscControl.clearController(server.name, oscMsgs[num]); //send a message to clear the OSC data from the MidiOscControl on all servers
						};
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
		}
	}


	killMe {
		group.free;
		run = false;
		win.close;
		dispArray.do{arg item;
			item.killMe;
		};
	}
}