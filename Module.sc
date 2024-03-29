MidiOscObject {
	var <>group, <>synthGroup, <>bigSynthGroup, <>win, <>oscMsgs, <>controls, assignButtons, paused=false;
	var waitForSetNum, modName, dontLoadControls, <>synths, visibleArray, isGlobalController, path, waitToLoad = false;

	initControlsAndSynths {arg num;
		//oscMsgs holds the

		oscMsgs = List.newClear(num);
		controls = List.newClear(0);
		assignButtons = List.newClear(num);

		//put a number in this List if you don't want the object to initialize itself upon loading from a file
		dontLoadControls = List.newClear(0);

		synths = List.newClear(0);

	}

	sendGUIVals {
		var vals;
		controls.do{arg item, i;

			vals = item.value;
			if(vals.size<2,{
				if(vals!=nil,{
					this.sendOSC(i, vals)
				});
			},{
				//this means it is a TypeOSCFuncObject
				try {this.sendOSC(i, vals[1])}
			})
		};
		if(this.isKindOf(MainMixer),{
			this.sendGUIMixer;
		});
	}

	sendOSC {|num, val|
		var unmapped, temp;

		if(oscMsgs[num]!=nil) {
			/*			if(oscMsgs[num].asString.contains("/Switches")) {
			if(controls[num].value==1,{
			Lemur_Mod.sendSwitchOSC(oscMsgs[num].asString)
			});
			}{*/

			if(val.size<2){
				try {unmapped = controls[num].controlSpec.unmap(val)} {unmapped = val};
				//Lemur_Mod.sendOSC(oscMsgs[num], unmapped);
				temp = oscMsgs[num].asString;
				OSCReceiver_Mod.sendOSC(temp.copyRange(0, temp.size-3), unmapped);
			}
			//}
		}
	}

	setOscMsg {arg msg;
		msg = msg.asString;
		if(msg.contains("/z")){
			msg = msg.replace("/z", "/x");
		};
		oscMsgs.put(waitForSetNum, msg.asSymbol);
	}

	clearMidiOsc {
		//should just have to send all osc message to MidiOscControl for deletion
		oscMsgs.do{|msg| MidiOscControl.clearController(group.server, msg)};
		oscMsgs = List.newClear(oscMsgs.size);
	}

	addAssignButton {|num, type, rect|
		var temp, oscMsg;

		if(rect!=nil, {temp = AssignButton.new(win, rect)},{temp = AssignButton.new()});

		assignButtons.put(num, temp
			.instantAction_{|butt|
				if(butt.value==1,{

					waitForSetNum = num;
					if(isGlobalController==true,{
						MidiOscControl.requestInstantAssign(this, controls[num], type, \global, nil);
					},{
						MidiOscControl.requestInstantAssign(this, controls[num], type, group.server);
					});
				},{
					MidiOscControl.clearInstantAssign;

					//because MultiBall has to clear 3 functions

					oscMsg = oscMsgs[num].asString;

					if(oscMsg.contains("MultiBall"), {
						oscMsg = ["x","y","z"].collect{arg item; oscMsg.copyRange(0, oscMsg.size-2)++item};
					},{
						oscMsg = [oscMsg]
					});

					oscMsg.do{|msg|
						if(isGlobalController==true,{
							MidiOscControl.clearController(\global, msg);
						},{
							MidiOscControl.clearController(group.server, msg); //send a message to clear the OSC data from the MidiOscControl
						});
					};
					oscMsgs.put(num, nil);
				})
		});
	}

	addTypeOSCAssignButton {|num|
		var temp, tempString;

		temp = TypeOSCAssignButton.new()
		.instantAction_{|butt|
			if(butt.value==1,{
				MidiOscControl.requestInstantTypeAssign(controls[num].textField, group.server);
			},{
				MidiOscControl.clearInstantAssign;
				tempString = controls[num].textField.string;
				controls[num].textField.valueAction_(controls[num].textField.string);
			})
		};

		^temp
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

		if(win!=nil,{
			saveArray.add(win.bounds);
		},{
			saveArray.add(nil);
		});

		this.saveExtra(saveArray);
		^saveArray
	}

	loadExtra{}//does nothing unless the module overrides this method

	load {arg loadArray;


		Routine({
			while{waitToLoad==true}{"waiting".postln;0.2.wait};

			loadArray[1].do{arg controlLevel, i;
				var control;
				try { control=controls[i] } { control = nil };

				if(control!=nil,{
					//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
					//controls[i].valueAction_(controlLevel);
					if(dontLoadControls.includes(i).not){
						try {
							if(controls[i].value!=controlLevel)
							{
								controls[i].valueAction_(controlLevel);
							}
						}{}
					}{
						if(controls[i].class==TypeOSCFuncObject)
						{
							//if we aren't loading the value of a TypeOSCFuncObject, still load the label
							controls[i].valueAction_(controlLevel[0]);
						}
					}
				});
			};

			loadArray[2].do{arg msg, i;
				var control;
				waitForSetNum = i;
				try { control=controls[i] } { control = nil;};
				if((msg!=nil)&&(control!=nil)&&(control.class!=TypeOSCFuncObject),{
					if(isGlobalController==true,{
						//this is only true for the server switcher and Modular Inputs Array
						MidiOscControl.getFunctionNSetController(this, controls[i], msg, 'global');
					},{
						MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
					});
					if(assignButtons[i]!=nil){
						{assignButtons[i].instantButton.value_(1)}.defer;
					}
				})
			};

			if(win!=nil,{
				win.bounds_(loadArray[3]);
				win.visible_(false);
			});

			this.loadExtra(loadArray[4]);

			this.sendGUIVals;
		}).play(AppClock)


	}
}

	Module_Mod : MidiOscObject {
		var <>outBus, <>mixerToSynthBus, xmlSynth, numChannels = 2, rout;


		*new {arg group, outBus;
			^super.new.group_(group).outBus_(outBus).init;
		}

		makeWindow {arg name, rect;
			if(rect!=nil, {win = Window.new(name, rect)},{win = Window.new(name)});
			win.userCanClose_(false);
			path = PathName(this.class.filenameSymbol.asString).pathOnly;
			modName = name;
		}

		makeMixerToSynthBus {arg numChannels;
			numChannels ?? {numChannels = 1};
			mixerToSynthBus = Bus.audio(group.server, numChannels);
		}

		getInternalBus {
			^mixerToSynthBus;
		}

		pause {
			synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
			if(bigSynthGroup!=nil){bigSynthGroup.set(\pauseGate, 0);bigSynthGroup.run(false)};
		paused = true;
		}

		unpause {
			synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
			if(bigSynthGroup!=nil){bigSynthGroup.run(true); bigSynthGroup.set(\pauseGate, 1)};
		paused = false;
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


	TypeOSCModule_Mod : Module_Mod {
		load {arg loadArray;

			loadArray[1].do{arg controlLevel, i;
				var control;
				try { control=controls[i] } { control = nil; };
				if(control!=nil,{
					//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
					if(dontLoadControls.includes(i).not){
						if(controls[i].value!=controlLevel)
						{
							controls[i].valueAction_(controlLevel);
						}
					}{
						if(controls[i].class==TypeOSCFuncObject)
						{
							//if we aren't loading the value of a TypeOSCFuncObject, still load the label
							controls[i].valueAction_(controlLevel[0]);
						}
					}
				});
			};

			if(win!=nil,{
				win.bounds_(loadArray[3]);
				win.visible_(false);
			});

			this.loadExtra(loadArray[4]);

			this.sendGUIVals;
		}
	}