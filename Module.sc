MidiOscObject {
	var <>group, <>synthGroup, <>bigSynthGroup, <>win, <>oscMsgs, <>controls, assignButtons/*, <>needsSequentialMixer=false*/;
	var waitForSetNum, modName, dontLoadControls, <>synths, visibleArray, isGlobalController;

	initControlsAndSynths {arg num;
		//oscMsgs holds the

		oscMsgs = List.newClear(num);
		controls = List.newClear(0);
		assignButtons = List.newClear(num);

		//put a number in this List if you don't want the object to initialize itself upon loading from a file
		dontLoadControls = List.newClear(0);

		synths = List.newClear(0);
		bigSynthGroup = Group.new(group);  //this is only in the sampler...not sure why

	}

	sendGUIVals {
		var vals;
		controls.do{arg item, i;
			vals = item.value;
			vals.postln;
			if(vals.size<2,{
				if(vals!=nil,{
					this.sendOSC(i, vals)
				});
			},{
				try {this.sendOSC(i, [item.x, item.y])}
			})
		};
		if(this.isKindOf(MainMixer),{
			this.sendGUIMixer;
		});
	}

	//why is it calling this
	sendOSC {|num, val|
		var unmapped;
		[num, val].postln;
		if(oscMsgs[num]!=nil, {
			oscMsgs[num].postln;
			if(oscMsgs[num].asString.contains("/Switches"), {
				if(controls[num].value==1,{
					Lemur_Mod.sendSwitchOSC(oscMsgs[num].asString)
				});
			},{

				if(val.size<2,{
					try {unmapped = controls[num].controlSpec.unmap(val)} {unmapped = val};
					Lemur_Mod.sendOSC(oscMsgs[num], unmapped);
				},{
					Lemur_Mod.sendOSC(oscMsgs[num][0].postln, controls[num].x);
					Lemur_Mod.sendOSC(oscMsgs[num][1], controls[num].y);
				});
			});
		})
	}

	sendXYOsc {|num, val|
		if(oscMsgs[num]!=nil, {
			TouchOSC_Mod.sendOSC(oscMsgs[num], controls[num].controlSpec.unmap(val));
		})
	}

	setOscMsg {arg msg;
		oscMsgs.postln;
		[waitForSetNum, msg].postln;
		oscMsgs.put(waitForSetNum, msg);
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
					oscMsg.postln;

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

		//assignButtons.put(num, temp);
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

		"loadArray1".postln;
		loadArray[1].do{arg controlLevel, i;
			var control;
			[controlLevel, i].postln;
			try { control=controls[i] } { control = nil; };
			if(control!=nil,{
				//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
				if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
					controls[i].valueAction_(controlLevel);
				});
			});
		};

		"loadArray2".postln;
		loadArray[2].do{arg msg, i;
			var control;
			[msg, i].postln;
			waitForSetNum = i;
			try { control=controls[i] } { control = nil;};
			if((msg!=nil)&&(control!=nil),{
				if(isGlobalController==true,{
					//this is only true for the server switcher and Modular Inputs Array
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, 'global');
				},{
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
				});
				assignButtons[i].instantButton.value_(1);
			})
		};

		if(win!=nil,{
			win.bounds_(loadArray[3]);
			win.visible_(false);
		});

		this.loadExtra(loadArray[4]);

		this.sendGUIVals;
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
		//win.front;
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
		bigSynthGroup.set(\pauseGate, 0);bigSynthGroup.run(false);
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
		bigSynthGroup.run(true); bigSynthGroup.set(\pauseGate, 1);
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
	//really need to fix this
	//this makes them not send the data back to the controller, which isn't ideal

	sendGUIVals {
		/*		var vals;
		controls.do{arg item, i;
		vals = item.value;
		if(vals.size<2,{
		if(vals!=nil,{
		this.sendOSC(i, vals)
		});
		},{
		if((vals[0]!=nil)&&(vals[1]!=nil),{
		this.sendOSC(i, vals)
		});
		})
		}*/
	}

	load {arg loadArray;

		//only load the values in the textFields

		"LoadingTypeOSC".postln;

		loadArray[1].do{arg controlLevel, i;
			[controlLevel, i].postln;
			if(((controls[i]!=nil) and: controls[i].value!=controlLevel) and:(dontLoadControls.includes(i).not),{
				controls[i].valueAction_(controlLevel);
				oscMsgs.postln;

			});
		};

		if(win!=nil,{
			win.bounds_(loadArray[3]);
			win.visible_(false);
		});

		this.loadExtra(loadArray[4]);

	}
}
