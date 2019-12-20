/*SetupSwitcherObject {var modularObjects;
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
}*/

// SetupSwitcher {
// 	var <>modularObjects, currentLayer, nextLayer, xmlModules, color, setupTemp, setupsList, <>currentSetup;
//
// 	*new {arg modularObjects;
// 		^super.newCopyArgs(modularObjects).init;
// 	}
//
// 	init {
//
// 		//creates a nXn of SetupSwitcherObjects that control which object brought to the front when the setup is changed
// 		setupsList = List.fill(modularObjects.size*modularObjects[0].size, {arg i;
// 			SetupSwitcherObject.new(modularObjects[i%5][(i/5).floor]);
// 		});
//
// 		setupsList = setupsList.clump(modularObjects.size);
// 		currentSetup = 'setup0';
// 	}
//
// 	changeSetupMap {arg location, setup;
// 		setupsList[location[0]][location[1]].changeSetupMap('setup'++location[2].asSymbol, setup);
// 	}
//
// 	changeSetup {arg changeToSetup;
//
// 		currentSetup = changeToSetup.asSymbol;
//
// 		setupsList.flatten.do{arg item, i;
// 			item.changeSetup(changeToSetup);
// 		};
// 	}
//
// 	hideCurrentSetup {
// 		setupsList.flatten.do{arg item, i;
// 			item.hideCurrentSetup;
// 		};
// 	}
//
// 	hideAll {
// 		setupsList.flatten.do{arg item;
// 			item.hideAll;
// 		}
// 	}
//
// 	showCurrentSetup {
// 		setupsList.flatten.do{arg item, i;
// 			item.showCurrentSetup;
// 		};
// 	}
// }

ServerSwitcher : MidiOscObject {
	var numServers, numButtons, controlTexts, actions, controlGrid, assignGrid, grid, <>currentServers, hideServerButtons;

	*new {
		^super.new.init;
	}

	init {
		numServers = ModularServers.servers.size;
		currentServers = ModularServers.servers.keys.asList.sort;
		modName = "ServerSwitcher";

		//dontLoadControls = Array.fill(16, {arg i; i});

		if(numServers>1, {

			numButtons = 2*numServers;
			this.initControlsAndSynths(numButtons*2+1);

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

			hideServerButtons = List.newClear(0);
			numServers.do{arg i;
				hideServerButtons.add(Button()
					.states_([["Show",Color.black, Color.blue], ["Hide", Color.black, Color.red]])
					.action_({arg butt;
						if(butt.value==1,{
							ModularServers.servers[("lmi"++i.asString).asSymbol].makeVisible(false);
						},{
							ModularServers.servers[("lmi"++i.asString).asSymbol].makeVisible(true);
						})
				}))
			};

			win.layout = VLayout(
				VLayout(*grid.collect { |row| HLayout(*row) }),
				HLayout(*hideServerButtons)
			);

			win.front;
		});
	}

/*	load {arg loadArray;
		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				if(i<controls.size,{
					MidiOscControl.getFunctionNSetController(this, controls[i], msg, \global, nil);
					assignButtons[i].instantButton.value_(1);
				});
			})
		};
	}*/

	reset {
		this.clearMidiOsc;
		if(win!=nil, {win.close; win = nil});
		this.init;
	}
}

ServerSwitcher2 : MidiOscObject {
	var numServers, numButtons, numIPads, controlTexts, actions, controlGrid, assignGrid, grid, <>currentServers, radioButtons, muteLayer2, server2MuteGrid;

	*new {
		^super.new.init;
	}

	init {
		numServers = ModularServers.servers.size;
		currentServers = ModularServers.servers.keys.asList.sort[0].asSymbol.dup;
		this.updateCurrentServers;

		modName = "ServerSwitcher2";
		isGlobalController = true;

		numIPads = Lemur_Mod.netAddrs.size;

		numButtons = 2*numServers;
		this.initControlsAndSynths(numButtons);
		oscMsgs = List.newClear(numButtons*2+1);

		radioButtons = List.newClear(0);

		controls = List.fill(numButtons*2, {Button.new().maxWidth_(60).maxHeight_(15)});
		controlGrid = controls.clump(numServers);
		(controlGrid.size/2).do{arg i;
			controlTexts = List.fill(numServers, {arg i2;
				[["LMI "++(i2+1).asString, Color.red, Color.black ], [ "LMI "++(i2+1).asString, Color.green, Color.black ]]});

			actions = List.fill(numServers, {arg i2;
				{arg button;
					//button.value.postln;
					//(i2+1).postln;
					currentServers.put(i, radioButtons[i].onButtons.collect({|item| ("lmi"++(item+1).asString).asSymbol}));
					ModularServers.servers[("lmi"++(i2+1).asString).asSymbol].showAndPlay(true);
					this.updateCurrentServers;
			}});

			radioButtons.add(RadioButtons(controlGrid[i], controlTexts, actions, 0, false));
		};

		numButtons.do{arg i;
			this.addAssignButton(i,\onOff);
		};

		(numServers).do{arg i;
			controls[numButtons+i]
			.states_([["Rotate", Color.red, Color.black ], [ "Keep Open", Color.green, Color.black ]])
			.action_{|butt|
				if(butt.value==0,{
					radioButtons[0].activateButton(controls[i]);
					controls[i].value_(0);
				},{
					radioButtons[0].deactivateButton(controls[i]);
					controls[i].valueAction_(1);
				})
			}
		};

		assignGrid = assignButtons.collect{|button| button.layout}.clump(numServers);

		numIPads.do{|i|
			controls[numServers*i].valueAction=1;
		};

		grid = List.newClear(0);
		numServers.do{arg i;
			grid.add(controlGrid[i]);
			grid.add(assignGrid[i]);
			if(i==0,{
				grid.add(controlGrid[i+numButtons]);
			});
		};

		muteLayer2 = 0;
		assignButtons = assignButtons.addAll(List.fill(numButtons+1, {nil}));
		controls.add(Button()
			.states_([["Pass Layer 2", Color.black, Color.red], ["Mute Layer 2", Color.black, Color.green]])
			.action_{arg butt;
				muteLayer2 = butt.value;
				if(muteLayer2==0,{
					ModularServers.servers[currentServers[1][0].asSymbol].showAndPlay(true);
				});
				this.updateCurrentServers;
		});
		//assignButtons.add(nil);
		this.addAssignButton(numButtons*2, \onOff);
		server2MuteGrid = [controls[numButtons*2], assignButtons.last.layout];
		grid.add(server2MuteGrid);

		win = Window("Server Switcher");

		win.layout = VLayout(
			VLayout(*grid.collect { |row| HLayout(*row) })
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.view.maxWidth_(numServers*60).maxHeight_(numIPads*3*15+15);

		win.front
	}

	updateCurrentServers {
		var tempCurrent;
		if(muteLayer2==1, {tempCurrent=currentServers.first}, {tempCurrent=currentServers});
		ModularServers.servers.keys.asList.do{arg key;
			if(tempCurrent.flatten.indexOf(key.asSymbol)==nil, {
				//"hide me ".post; key.postln;
				ModularServers.servers[key.asSymbol].showAndPlay(false)
			})
		}
	}

	reset {
		this.clearMidiOsc;
		if(win!=nil, {win.close; win = nil});
		this.init;
	}
}
