MantaToMidi_Mod :  Module_Mod {
	var noteOnFunctions, noteOffFunctions, destinations, midiOut, triggerButtons;


	init {
		this.makeWindow("MantaToMidi",Rect(200, 230, 140, 280));

		this.initControlsAndSynths(96);

		destinations = MIDIClient.destinations.collect{arg item; item.device.asString};

		midiOut = MIDIOut(0);

		controls.add(PopUpMenu().items_(destinations).action_({
			|pu|
			midiOut = MIDIOut(pu.value);
		}));

		controls.add(Button.new()
			.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
			.action_{|v|
				if(v.value==1,{
					this.setManta;
				},{
					this.clearMidiOsc;
				})
			});

		win.layout_(
			VLayout(controls[0], controls[1])
		);

	}

	setManta {
		var counter=0;

		48.do{arg key;
			oscMsgs.put(key, "/manta/padOn/"++((key).asString));
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[key],
				{
					midiOut.noteOn(0, key+36, 127)
				}, setups);
		};
		48.do{arg key;
			oscMsgs.put(key+48, "/manta/padOff/"++((key).asString));
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[key+48],
				{
					midiOut.noteOff(0, key+36, 0)
				}, setups);
		};

	}

/*	addFunctions {
		noteOnFunctions = IdentityDictionary.new;
		noteOffFunctions = IdentityDictionary.new;

		triggerButtons = (1..48);

		//the notes
		triggerButtons.do{arg i;
		noteOnFunctions.put(i, {arg val;
			specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'));
			arpeggioSwitch = false;
		});
	}*/


	/*save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);  //controller messages
		//this does not save or load the oscMsgs

		saveArray.add(win.bounds);

		this.saveExtra(saveArray);
		^saveArray
	}*/

	// load {arg loadArray;
	// 	loadArray[1].do{arg controlLevel, i;
	// 		if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
	// 	};
	// 	win.bounds_(loadArray[3]);
	// 	this.loadExtra(loadArray);
	// }

}
