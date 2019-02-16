MidiOscObject {var <>group, <>synthGroup, <>bigSynthGroup, <>win, <>oscMsgs, <>controls, assignButtons;
	var waitForSetNum, modName, dontLoadControls, <>synths, visibleArray;

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

	sendOSC {|num, val|
		var unmapped;

		if(oscMsgs[num]!=nil, {
			if(val.size<2,{
				try {unmapped = controls[num].controlSpec.unmap(val)} {unmapped = val};
				TouchOSC_Mod.sendOSC(oscMsgs[num], unmapped);
			},{
				unmapped = [controls[num].controlSpecX.unmap(val[0]), controls[num].controlSpecY.unmap(val[1])];
				TouchOSC_Mod.sendOSCxy(oscMsgs[num], unmapped);
			});
		})
	}

/*
	sendButtonOsc {|num, val|
		var name, nums, string;

		if(oscMsgs[num]!=nil, {
			TouchOSC_Mod.sendOSC(oscMsgs[num], val);
			//string = oscMsgs[num].asString;
			//if(string.contains("multitoggle"),{

				//name = string.findRegexp("/Switches.blahBlahx").at(0);

				//nums = ("["++(string.copyRange(name[0]+name[1].size+2, string.size-1))).interpret;

				//name = Array.with(name[1]).addAll(nums);

				//Lemur_Mod.sendOSCBundle(name);
			//},{
				//Lemur_Mod.sendOSC(oscMsgs[num], val);
			//});
		})
	}

	sendXYOsc {|num, val|
		if(oscMsgs[num]!=nil, {
			TouchOSC_Mod.sendOSC(oscMsgs[num], controls[num].controlSpec.unmap(val));
		})
	}

	sendSliderOsc {|num, val|
		if(oscMsgs[num]!=nil, {
			TouchOSC_Mod.sendOSC(oscMsgs[num], controls[num].controlSpec.unmap(val));
			//I should probably go through the controllers here
			//Lemur_Mod.sendOSC(oscMsgs[num]++"/x", controls[num].controlSpec.unmap(val));
		})
	}*/

	setOscMsg {arg msg;
		oscMsgs.put(waitForSetNum, msg);
	}

	clearMidiOsc {
		//should just have to send all osc message to MidiOscControl for deletion
		oscMsgs = List.newClear(oscMsgs.size);
	}

	addAssignButton {|num, type, rect|
		var temp;

		if(rect!=nil, {temp = AssignButton.new(win, rect)},{temp = AssignButton.new()});

		assignButtons.put(num, temp
			.instantAction_{|butt|
				if(butt.value==1,{

					waitForSetNum = num;
					MidiOscControl.requestInstantAssign(this, controls[num], type, group.server);
				},{
					MidiOscControl.clearInstantAssign;
					MidiOscControl.clearController(group.server, oscMsgs[num]); //send a message to clear the OSC data from the MidiOscControl
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
				tempString.postln;
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

		loadArray[1].do{arg controlLevel, i;
			//it will not load the value if the value is already correct (because Button seems messed up) or if dontLoadControls contains the number of the controller
			if((controls[i].value!=controlLevel)&&(dontLoadControls.includes(i).not),{
				controls[i].valueAction_(controlLevel);
			});
		};

		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				MidiOscControl.getFunctionNSetController(this, controls[i], msg, group.server);
				assignButtons[i].instantButton.value_(1);
			})
		};

		if(win!=nil,{
			win.bounds_(loadArray[3]);
			win.visible_(false);
		});

		this.loadExtra(loadArray[4]);
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

