SetupSwitcherObject {var modularObjects;
	var currentSetup, setupMap, setupsTemp, objectsDict;

	*new {arg modularObjects;
		^super.newCopyArgs(modularObjects).init;
	}

	init {
		setupsTemp = ModularServers.setups.deepCopy;

		//at the beginning the setupMap points to the correct object
		setupMap = Dictionary.new();

		//(setup0->setup0, setup1->setup1...etc)
		setupsTemp.do{arg item, i; setupMap.add(item.asSymbol->item.asSymbol)};


		//start on setup0
		currentSetup = 'setup0';


		objectsDict = Dictionary.new;
		modularObjects.do{arg item, i;
			//(setup0->ModularObjectPanel0, setup1->ModularObjectPanel1...etc)
			objectsDict.add(setupsTemp[i].asSymbol->item);
		};
	}

	changeSetupMap {arg setupPointsTo, setupIs;
		setupMap.put(setupIs.asSymbol, setupPointsTo.asSymbol);
	}

	changeSetup {arg changeToSetup;
		if(currentSetup!=setupMap[changeToSetup.asSymbol], {
			//pause the current setup and resume the next
			objectsDict[currentSetup.asSymbol].pause;
			currentSetup = setupMap[changeToSetup.asSymbol].asSymbol;
			objectsDict[currentSetup.asSymbol].resume;
		});
	}

	hideAll {
		modularObjects.do{arg item;
			item.pause;
		}
	}

	hideCurrentSetup {
		objectsDict[currentSetup.asSymbol].pause;
	}

	showCurrentSetup {
		objectsDict[currentSetup.asSymbol].resume;
	}
}

SetupSwitcher {
	var <>modularObjects, currentLayer, nextLayer, xmlModules, color, setupTemp, setupsList, <>currentSetup;

	*new {arg modularObjects;
		^super.newCopyArgs(modularObjects).init;
	}

	init {

		//creates a nXn of SetupSwitcherObjects that control which object brought to the front when the setup is changed
		setupsList = List.fill(modularObjects.size*modularObjects[0].size, {arg i;
			SetupSwitcherObject.new(modularObjects[i%5][(i/5).floor]);
		});

		setupsList = setupsList.clump(modularObjects.size);
		currentSetup = 'setup0';
	}

	changeSetupMap {arg location, setup;
		setupsList[location[0]][location[1]].changeSetupMap('setup'++location[2].asSymbol, setup);
	}

	changeSetup {arg changeToSetup;

		currentSetup = changeToSetup.asSymbol;

		setupsList.flatten.do{arg item, i;
			item.changeSetup(changeToSetup);
		};
	}

	hideCurrentSetup {
		setupsList.flatten.do{arg item, i;
			item.hideCurrentSetup;
		};
	}

	hideAll {
		setupsList.flatten.do{arg item;
			item.hideAll;
		}
	}

	showCurrentSetup {
		setupsList.flatten.do{arg item, i;
			item.showCurrentSetup;
		};
	}
}

ServerSwitcher : MidiOscObject {
	var numServers, numButtons, controlTexts, actions, controlGrid, assignGrid, grid, <>currentServers;

	*new {
		^super.new.init;
	}

	init {
		numServers = ModularServers.servers.size;
		currentServers = ModularServers.servers.keys.asList.sort;
		modName = "ServerSwitcher";
		dontLoadControls = Array.fill(16, {arg i; i});
		if(numServers>1, {

			numButtons = 2**numServers;
			this.initControlsAndSynths(numButtons);

			controls = List.fill(numButtons, {Button.new()});

			win = Window("Server Switcher", Rect(Window.screenBounds.width/2, Window.screenBounds.height*2, 200, 100));

			controlTexts = List.fill(numButtons, {arg i;
				[[ i.asInteger.asBinaryString(numServers), Color.red, Color.black ], [ i.asInteger.asBinaryString(numServers), Color.green, Color.black ]]});

			actions = List.fill(numButtons, {arg i;{
				currentServers = List.newClear;
				i.asInteger.asBinaryDigits(numServers).do{arg item, serverNum;
					if(item==1, {currentServers.add("lmi"++serverNum.asString)});
					ModularServers.servers[("lmi"++serverNum.asString).asSymbol].showAndPlay(item==1)
				}}
			});

			RadioButtons(controls, controlTexts, actions, 0, false);

			numButtons.do{arg i;
				this.addAssignButton(i,\onOff);
			};

			controlGrid = controls.clump(4);
			assignGrid = assignButtons.collect{|button| button.layout}.clump(4);

			grid = List.newClear(0);
			controlGrid.size.do{arg i;
				grid.add(controlGrid[i]);
				grid.add(assignGrid[i]);
			};

			win.layout = VLayout(*grid.collect { |row| HLayout(*row) });

			win.front;
		});
	}

	addAssignButton {|num, type, rect|
		var temp;

		if(rect!=nil, {temp = AssignButton.new(win, rect)},{temp = AssignButton.new()});

		assignButtons.put(num, temp
			.instantAction_{|butt|
				if(butt.value==1,{

					//the main thing here is that the messages go into the \global key in the MidiOscControl
					waitForSetNum = num;
					MidiOscControl.requestInstantAssign(this, controls[num], type, \global, nil);
				},{
						MidiOscControl.clearInstantAssign;
						MidiOscControl.clearController(\global, oscMsgs[num]); //send a message to clear the OSC data from the MidiOscControl
						oscMsgs.put(num, nil);
				})
			});
	}

	load {arg loadArray;
		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				if(i<controls.size,{
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, \global, nil);
					assignButtons[i].instantButton.value_(1);
				});
			})
		};
	}

	reset {
		this.clearMidiOsc;
		if(win!=nil, {win.close; win = nil});
		this.init;
	}
}
