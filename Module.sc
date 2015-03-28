Module_Mod {
	var <>group, <>outBus, <>setups, <>mixerToSynthBus, synths, xmlSynth, numChannels = 2, rout;
	var waitForSetNum, assignButtons, win, modName, <>controls, <>oscMsgs, zActions, dontLoadControls;

	*new {arg group, outBus, setups;
		^super.new.group_(group).outBus_(outBus).setups_(setups).init;
	}

	initControlsAndSynths {arg num;
		//oscMsgs holds the

		oscMsgs = List.newClear(num);
		controls = List.newClear(0);
		assignButtons = List.newClear(num);

		//put a number in this List if you don't want the object to initialize itself upon loading from a file
		dontLoadControls = List.newClear(0);

		synths = List.newClear(0);
	}


	addSetup {arg setup;
		//happens when the user hits the addSetup button on the MainWindow
		setups.postln;
		oscMsgs.postln;

		//setups.add(setup);
		oscMsgs.do{arg item, i;
			if(item!=nil, {
				MidiOscControl.addFuncToSetup(group.server, setup, item)
			})
		}
	}


	removeSetup {arg setup;
		//setups.remove(setup);
		//should just have to send the osc message to MidiOscControl to be deleted
		oscMsgs.do{arg item, i;
			if(item!=nil, {
				MidiOscControl.removeFuncFromSetup(group.server, setup, item)
			})
		}
	}

	clearMidiOsc {
		//should just have to send all osc message to MidiOscControl for deletion
		setups.do{arg item; this.removeSetup(item)};
		oscMsgs = List.newClear(oscMsgs.size);
	}

	setOscMsg {arg msg;
		oscMsgs.put(waitForSetNum, msg);
		oscMsgs.postln;
	}

	addAssignButton {|num, type, rect|
		var temp;
		//for RadioButtons, the "num" is an array
		//the first value is the control index, the second value is an array corresponding to the place in the RadioButtonArray

		if(rect!=nil, {temp = AssignButton.new(win, rect)},{temp = AssignButton.new()});

		assignButtons.put(num, temp
			.instantAction_{|butt|
				if(butt.value==1,{

					//module, controlObject, typeOfController, server, setupsIn
					waitForSetNum = num;
					MidiOscControl.requestInstantAssign(this, controls[num], type, group.server, setups);


				},{
						MidiOscControl.clearInstantAssign;
						MidiOscControl.clearController(group.server, oscMsgs[num]); //send a message to clear the OSC data from the MidiOscControl
						oscMsgs.put(num, nil);
				})
			});
	}

	makeWindow {arg name, rect;
		name.postln;
		win = Window.new(name, rect);
		win.userCanClose_(false);
		win.front;
		modName = name;
	}

	makeMixerToSynthBus {arg numChannels;
		if(numChannels==nil, {numChannels = 1});
		mixerToSynthBus = Bus.audio(group.server, numChannels);
	}

	getInternalBus {
		^mixerToSynthBus;
	}

	pause {
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
	}

	show {
		win.visible = true;
		win.front;
	}

	hide {
		win.visible = false;
	}

	numBusses {
		^mixerToSynthBus.numChannels;
	}

	saveExtra{}//does nothing unless the module overrides this method

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);  //controller messages
		saveArray.add(oscMsgs);

		saveArray.add(win.bounds);

		this.saveExtra(saveArray);
		^saveArray
	}

	loadExtra{}//does nothing unless the module overrides this method

	load {arg loadArray;
		loadArray.do{arg item; item.postln};
		loadArray[1].do{arg controlLevel, i;
			//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
			if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
				controls[i].valueAction_(controlLevel);
			});
		};
		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server, setups);
			})
		};
		win.bounds_(loadArray[3]);
		this.loadExtra(loadArray);
	}

	killMe {
		oscMsgs.do{arg item; MidiOscControl.clearController(group.server, item)};
		win.close;
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.set(\gate, 0)})};
		});
		mixerToSynthBus.free;
		this.killMeSpecial;
	}

	killMeSpecial {

	}
}