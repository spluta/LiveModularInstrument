Record_Mod : Module_Mod {
	var buffer, recordNode, path;

	*initClass {
		StartUp.add {
			SynthDef("record2_mod", { arg bufnum, inBus;
				var in;

				in = In.ar(inBus, 2);
				DiskOut.ar(bufnum, in*0.5)
			}).writeDefFile;
			SynthDef("record4_mod", { arg bufnum, inBus;
				var in;

				in = In.ar(inBus, 4);
				DiskOut.ar(bufnum, in*0.5)
			}).writeDefFile;
			SynthDef("record8_mod", { arg bufnum, inBus;
				var in;

				in = In.ar(inBus, 8);
				DiskOut.ar(bufnum, in*0.5)
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("Record", Rect(618, 645, 160, 50));
		this.initControlsAndSynths(2);

		this.makeMixerToSynthBus(2);

		synths = List.newClear(1);

		controls.add(Button(win,Rect(5, 5, 150, 20))
			.states_([ [ "prepare", Color.green, Color.black ], [ "record", Color.green, Color.black ], [ "stop", Color.red, Color.black ]])
			.action_{|v|
				switch(v.value,
					1, {
						path = thisProcess.platform.recordingsDir +/+ "SC_" ++ Date.localtime.stamp ++ ".aif";
						buffer = Buffer.alloc(group.server, 65536, numChannels);
						buffer.write(path, "aiff", "int24", 0, 0, true);
					},
					2, {
						switch(numChannels,
							2, {recordNode = Synth("record2_mod", [\bufnum, buffer.bufnum, \inBus, mixerToSynthBus], group)},
							4, {recordNode = Synth("record4_mod", [\bufnum, buffer.bufnum, \inBus, mixerToSynthBus], group)},
							8, {recordNode = Synth("record8_mod", [\bufnum, buffer.bufnum, \inBus, mixerToSynthBus], group)}
						)
					},
					0, {
						recordNode.notNil.if({
							recordNode.free;
							recordNode = nil;
							buffer.close({ arg buf; buf.free; });
							buffer = nil;
						});
					}
				)
			});
		this.addAssignButton(1,\onOff,Rect(5, 25, 150, 20));

		//multichannel button
		controls.add(Button(win,Rect(5, 50, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				synths[0].set(\gate, 0);
				switch(butt.value,
					0, {
						numChannels = 2;
					},
					1, {
						numChannels = 4;
					},
					2, {
						numChannels = 8;
					}
				)
			};
		)
	}

	killMeSpecial {
		buffer.free;
	}
}