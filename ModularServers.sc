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
		objectsDict.postln;
	}

	changeSetupMap {arg setupPointsTo, setupIs;
		[setupIs, setupPointsTo].postln;
		setupMap.put(setupIs.asSymbol, setupPointsTo.asSymbol);
		setupMap.postln;
	}

	changeSetup {arg changeToSetup;
		//changeToSetup.postln;
		//objectsDict.postln;
		if(currentSetup!=setupMap[changeToSetup.asSymbol], {
			//pause the current setup and resume the next
			objectsDict[currentSetup.asSymbol].pause;
			currentSetup = setupMap[changeToSetup.asSymbol].asSymbol;
			currentSetup.postln;
			objectsDict[currentSetup.asSymbol].resume;
		});
	}

}

SetupSwitchers {
	var <>modularObjects, currentLayer, nextLayer, xmlModules, color, setupTemp, setupsList, <>currentSetup;

	*new {arg modularObjects;
		^super.newCopyArgs(modularObjects).init;
	}

	init {

		//creates a nXn of SetupSwitcherObjects that control which object brought to the front when the setup is changed
		setupsList = List.fill(modularObjects.size*modularObjects[0].size, {arg i;
			i.postln;
			SetupSwitcherObject.new(modularObjects[i%5][(i/5).floor]);
		});

		setupsList = setupsList.clump(modularObjects.size);
		currentSetup = 'setup0';
	}

	changeSetupMap {arg location, setup;
		[location, setup].postln;
		setupsList[location[0]][location[1]].changeSetupMap('setup'++location[2].asSymbol, setup);
	}

	changeSetup {arg changeToSetup;

		currentSetup = changeToSetup.asSymbol;

		setupsList.flatten.do{arg item, i;
			item.postln;
			item.changeSetup(changeToSetup);
		};
	}

}

ModularServerObject {
	var <>server, <>objectBusses, mainGroup, <>inGroup, <>mixerGroup, synthGroup, <>synthGroups, <>inBusses, <>inBusIndexes, <>stereoInBusses, <>stereoInBusIndexes, volumeInRack, setupSwitchers, modularObjects, dimensions, <>mainMixer, mainWindow, <>busMap;

	*new {arg server;
		^super.new.server_(server).init;
	}

	init {
		server.waitForBoot({
			//set up groups
			mainGroup = Group.tail(server);
			inGroup = Group.tail(mainGroup);
			synthGroup = Group.tail(mainGroup);
			mixerGroup = Group.tail(mainGroup);

			//set up the inputs and outputs
			inBusses = List.new;
			inBusIndexes = List.new;
			8.do{arg i;  //right now the number of input channels is hardcoded to 8
				inBusses.add(Bus.audio(server,1));
				inBusIndexes.add(inBusses[i].index);
			};
			stereoInBusses = List.new;
			stereoInBusIndexes = List.new;
			4.do{arg i;  //half of the number of input channels
				stereoInBusses.add(Bus.audio(server,2));
				stereoInBusIndexes.add(stereoInBusses[i].index);
			};


			//create objectBusses and groups in the shape of the dimensions array

			objectBusses = List.new;

			dimensions = [5,5,4]; //for now this is hard-coded

/*			dimensions[0].do{arg i;
				objectBusses.add(List.new);
				dimensions[1].do{arg i2;
					objectBusses[i].add(List.new);
					dimensions[2].do{arg i3;
						objectBusses[i][i2].add(Bus.audio(server,8));
					}
				}
			};*/

			objectBusses = List.fill((dimensions[0]*dimensions[1]*dimensions[2]), {Bus.audio(server,8)});

			objectBusses = objectBusses.clump(25).flop.clump(5);

			synthGroups = List.fill((dimensions[0]*dimensions[1]*dimensions[2]), {Group.tail(synthGroup)});

			synthGroups = synthGroups.clump(dimensions[2]).clump(dimensions[0]);

			//create the Array of ModularObjects

			modularObjects = List.new;



			dimensions[0].do{arg i;
				modularObjects.add(List.new);
				dimensions[1].do{arg i2;
					modularObjects[i].add(List.new);
					dimensions[2].do{arg i3;
						modularObjects[i][i2].add(
							ModularObjectPanel(server, synthGroups[i][i2][i3], objectBusses[i][i2][i3], [i2,i,i3])
						);
					}
				}
			};

			setupSwitchers = SetupSwitchers(modularObjects);


			mainWindow = MainProcessingWindow.new(server.name);

			this.addPanelsToWindow;

			mainMixer = MainMixer.new(mixerGroup);

			LiveModularInstrument.readyToRoll();
		})
	}

	save {
		var saveServer, temp;

		//save and load inputsArray

		saveServer = List.newClear(0);

		saveServer.add(mainWindow.mainSwitchSet);

		saveServer.add(mainWindow.win.bounds);

		saveServer.add(inBusIndexes);
		saveServer.add(stereoInBusIndexes);

		temp = List.newClear(0);
		objectBusses.flatten.flatten.do{arg item;
			temp.add(item.index); //add busses
		};
		saveServer.add(temp);

		temp = List.newClear(0);
		modularObjects.flatten.flatten.do{arg mop, i;
			temp.add(mop.save);
		};

		saveServer.add(temp);
		saveServer.add(mainMixer.save);
		"server ".post; saveServer.postln;
		^saveServer;
	}

	load {arg loadArray, inBusTemp, stereoInBusTemp, internalBusTemp, flatObjectBus, flatMOPs, mopData;

		loadArray.postln;

		if(loadArray[0], {mainWindow.assignMantaButton.valueAction_(1)});
		mainWindow.win.bounds_(loadArray[1]);

		inBusTemp = loadArray[2];
		stereoInBusTemp = loadArray[3];
		internalBusTemp = loadArray[4];

		busMap = List.fill(3, {arg i; Dictionary.new});

		inBusTemp.do{arg item, i; busMap[0].add(item.asSymbol -> [inBusIndexes[i], i])};
		stereoInBusTemp.do{arg item, i; busMap[1].add(item.asSymbol -> [stereoInBusIndexes[i], i])};
		flatObjectBus = objectBusses.flatten.flatten;
		internalBusTemp.do{arg item, i; busMap[2].add(item.asSymbol -> flatObjectBus[i].index)};

		busMap.postln;

		flatMOPs = modularObjects.flatten.flatten;
		mopData = loadArray[5];
		mopData.do{arg item, i; if(item.size>0, {flatMOPs[i].load(item)})};

		mainMixer.load(loadArray[6]);
	}

	confirmValidBus {arg bus;
		var valid = false;

		inBusIndexes.do{arg item; if(item==bus,{valid = true})};
		stereoInBusIndexes.do{arg item; if(item==bus,{valid = true})};
		objectBusses.flatten.flatten.do{arg item; if(item.index==bus,{valid = true})};
		^valid
	}

	getCurrentSetup {
		^setupSwitchers.currentSetup;
	}

	changeSetup {|setup|
		"changeSetup: ".post; [server, setup].postln;
		setupSwitchers.changeSetup(setup);
		mainMixer.changeSetup(setup);
	}

	addPanelsToWindow {
		modularObjects.flatten.flatten.do{arg item, i;
			item.addToWindow(mainWindow.win, ((i%4)==0));
		}
	}

	setModularPanelToSetup{arg location, setupName;
		setupSwitchers.changeSetupMap(location, setupName);
	}

	name {
		^server.name
	}

	killMe {
		mainGroup.free;
		inBusses.free;
	}
}

ModularServers {
	classvar <>numServers, <>inBus, <>outBus, <>setupColors;
	classvar <>servers, <>setups, <>modularInputsArray;

	*boot {arg numServersIn, inBusIn, outBusIn;
		numServers = numServersIn; inBus = inBusIn; outBus = outBusIn;
		//[numServers, inBus, outBus].postln;
		servers = Dictionary.new(0);
		setups = List.fill(4, {arg i; ('setup'++i.asSymbol)});
		setupColors = [Color.blue.multiply(0.5).alpha_(0.25), Color.green.multiply(0.5).alpha_(0.25), Color.red.multiply(0.5).alpha_(0.25), Color.magenta.multiply(0.5).alpha_(0.25)];
		numServers.do{arg i;
			("adding a server: "++i.asString).postln;
			servers.add(("lmi"++i).asSymbol-> ModularServerObject.new(Server.new("lmi"++i.asString, NetAddr("localhost", 57111+i), Server.local.options)));
		};
	}

	*save {arg path;
		var saveServers, temp;

		saveServers = List.newClear(0);

		saveServers.add(modularInputsArray.save); //save the inputs array

		temp = List.newClear(0);
		numServers.do{arg i; temp.add(servers[("lmi"++i).asSymbol].save)}; //save the servers
		saveServers.add(temp);

		saveServers = saveServers.asCompileString;
		saveServers.writeArchive(path); //write the archive
	}

	*load {arg path;
		var loadArray, numServersInFile;

		loadArray = Object.readArchive(path);

		loadArray = loadArray.interpret;

		loadArray.postln;

		modularInputsArray.load(loadArray[0]);

		numServersInFile = loadArray[0].size-1;

		//right now it is limited to the number of servers open or the number in the file. basically, for this to work, they have to be the same. i can change this.
		min(numServersInFile, numServers).do{arg i;
			servers[("lmi"++i).asSymbol].load(loadArray[1][i])
		}
	}

	*addInputsArray {arg inBus;
		modularInputsArray = ModularInputsArray.new;
		modularInputsArray.init2(inBus);
	}

	*addServer{
		("adding a server: "++numServers.asString).postln;
		servers.add(("lmi"++numServers.asString).asSymbol-> ModularServerObject.new(Server.new(("lmi"++numServers.asString).asSymbol, NetAddr("localhost", 57111+numServers), Server.local.options)));
		numServers = numServers+1;
	}

	*changeSetup {|serverName, setupNum|
		[serverName, setupNum].postln;
		servers[serverName.asSymbol].changeSetup(setups[setupNum])
	}

	*setModularPanelToSetup{arg serverName, location, setupName;
		servers[serverName.asSymbol].setModularPanelToSetup(location, setupName);
	}

	*getSynthGroup {arg serverName, location;
		^servers[serverName.asSymbol].synthGroups[location];
	}

	*getMixerGroup {arg serverName;
		^servers[serverName.asSymbol].mixerGroup;
	}

	*getSoundInBusses {arg serverName;
		^servers[serverName.asSymbol].inBusIndexes;
	}

	*getStereoSoundInBusses {arg serverName;
		^servers[serverName.asSymbol].stereoInBusIndexes;
	}

	*getSetups {arg serverName;
		^setups;
	}

	*getObjectBusses {arg serverName;
		^servers[serverName.asSymbol].objectBusses;
	}
}