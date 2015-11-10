ModularServerObject {
	var <>server, <>objectBusses, mainGroup, <>inGroup, <>mixerGroup, synthGroup, <>synthGroups, <>inBusses, <>inBusIndexes, <>stereoInBusses, <>stereoInBusIndexes, volumeInRack, setupSwitcher, modularObjects, dimensions, <>mainMixer, mainWindow, <>busMap;

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

			setupSwitcher = SetupSwitcher(modularObjects);

			mainWindow = MainProcessingWindow.new(mainGroup);

			this.addPanelsToWindow;

			mainMixer = MainMixer.new(mixerGroup);

			LiveModularInstrument.readyToRoll();
		})
	}

	saveSetup {arg path, setupNum;
		var saveSetup, temp;

		saveSetup = List.newClear(0);

		saveSetup.add(inBusIndexes);
		saveSetup.add(stereoInBusIndexes);

		temp = List.newClear(0);

		//saves

		objectBusses.flatten.flatten.do{arg item, i;
			if(i%setupNum==0, {
				temp.add(item.index); //add busses
			})
		};
		saveSetup.add(temp);

		temp = List.newClear(0);

		modularObjects.flatten.flatten.do{arg mop, i;
			if(i%setupNum==0, {
				temp.add(mop.save);
			})
		};

		saveSetup.add(temp);

		saveSetup = saveSetup.asCompileString;
		saveSetup.writeArchive(path);
	}

	loadSetup {arg path, setupNum;
		var loadArray, flatMOPs, mopData;

		loadArray = Object.readArchive(path);
		loadArray = loadArray.interpret;

		this.makeBusMap([loadArray[0], loadArray[1], loadArray[2]]);

		flatMOPs = modularObjects.flatten.flatten;
		mopData = loadArray[3];
		mopData.do{arg item, i; if((item.size>0)&&((i%setupNum)==0),
			{flatMOPs[i].load(item)})
		};
	}

	makeBusMap {arg loadArray;
		var inBusTemp, stereoInBusTemp, internalBusTemp, flatObjectBus;

		inBusTemp = loadArray[0];
		stereoInBusTemp = loadArray[1];
		internalBusTemp = loadArray[2];

		busMap = List.fill(3, {arg i; Dictionary.new});

		inBusTemp.do{arg item, i; busMap[0].add(item.asSymbol -> [inBusIndexes[i], i])};
		stereoInBusTemp.do{arg item, i; busMap[1].add(item.asSymbol -> [stereoInBusIndexes[i], i])};
		flatObjectBus = objectBusses.flatten.flatten;
		internalBusTemp.do{arg item, i; busMap[2].add(item.asSymbol -> flatObjectBus[i].index)};
	}


	save {
		var saveServer, temp;

		//save and load inputsArray

		saveServer = List.newClear(0);

		saveServer.add(mainWindow.save);

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
		^saveServer;
	}


	load {arg loadArray;
		var inBusTemp, stereoInBusTemp, internalBusTemp, flatObjectBus, flatMOPs, mopData;

		mainWindow.load(loadArray[0]);

		this.makeBusMap([loadArray[1], loadArray[2], loadArray[3]]);

		flatMOPs = modularObjects.flatten.flatten;
		mopData = loadArray[4];
		mopData.do{arg item, i; if(item.size>0, {flatMOPs[i].load(item)})};

		mainMixer.load(loadArray[5]);

		setupSwitcher.hideAll;
		//setupSwitcher.changeSetup(\setup0);
		mainWindow.hitButton(0);
		setupSwitcher.showCurrentSetup;
	}


	confirmValidBus {arg bus;
		var valid = false;

		inBusIndexes.do{arg item; if(item==bus,{valid = true})};
		stereoInBusIndexes.do{arg item; if(item==bus,{valid = true})};
		objectBusses.flatten.flatten.do{arg item; if(item.index==bus,{valid = true})};
		^valid
	}

	getCurrentSetup {
		^setupSwitcher.currentSetup;
	}

	changeSetup {|setup|
		setupSwitcher.changeSetup(setup);
		mainMixer.changeSetup(setup);
	}

	addPanelsToWindow {
		modularObjects.flatten.flatten.do{arg item, i;
			item.addToWindow(mainWindow.win, ((i%4)==0));
		}
	}

	setModularPanelToSetup{arg location, setupName;
		setupSwitcher.changeSetupMap(location, setupName);
	}

	showAndPlay {arg bool;
		if(bool,{
			mainWindow.show;
			mainMixer.unmute;
			mainMixer.unhide;
			setupSwitcher.showCurrentSetup;

			},{
				mainWindow.hide;
				mainMixer.hide;
				mainMixer.mute;
				setupSwitcher.hideCurrentSetup;
		});
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
	classvar <>servers, <>setups, <>modularInputsArray, <>serverSwitcher;

	*boot {arg numServersIn, inBusIn, outBusIn;
		numServers = numServersIn; inBus = inBusIn; outBus = outBusIn;
		servers = Dictionary.new(0);
		setups = List.fill(4, {arg i; ('setup'++i.asSymbol)});
		setupColors = [Color.blue.multiply(0.5).alpha_(0.25), Color.green.multiply(0.5).alpha_(0.25), Color.red.multiply(0.5).alpha_(0.25), Color.magenta.multiply(0.5).alpha_(0.25)];
		numServers.do{arg i;
			("adding a server: "++i.asString).postln;
			servers.add(("lmi"++i).asSymbol-> ModularServerObject.new(Server.new("lmi"++i.asString, NetAddr("localhost", 57111+i), Server.local.options)));
		};

	}

	*save {arg path, serverName;
		var saveServers, temp;

		saveServers = List.newClear(0);

		saveServers.add(modularInputsArray.save); //save the inputs array

		if(numServers>1,{
			"saveSwitcher".postln;
			saveServers.add(serverSwitcher.save);
			},{
				saveServers.add(nil)
		});

		temp = List.newClear(0);

		if(serverName==nil,{
			numServers.do{arg i;
				temp.add(servers[("lmi"++i).asSymbol].save)
			}; //save the servers
			},{
				temp.add(servers[serverName.asSymbol].save)
		});

		saveServers.add(temp);

		//saveServers = saveServers.asCompileString;
		saveServers.writeArchive(path); //write the archive
	}

	*load {arg path, serverName;
		var loadArray, numServersInFile, file;

		loadArray = Object.readArchive(path);

		modularInputsArray.load(loadArray[0]);
		numServersInFile = loadArray[2].size;

		if(serverName==nil,{
			min(numServersInFile, numServers).do{arg i;
				servers[("lmi"++i).asSymbol].load(loadArray[2][i])
			}
			}, {
				servers[serverName.asSymbol].load(loadArray[2][0])
		});

		//load the serverSwitcher last so that it can update the server windows
		if(loadArray[1]!=nil,{
			serverSwitcher.load(loadArray[1]);
		});

	}

	*addInputsArray {arg inBus;
		modularInputsArray = ModularInputsArray.new;
		modularInputsArray.init2(inBus);
	}

	*updateServerSwitcher {
		if(serverSwitcher!=nil,{
			serverSwitcher.reset;
			},{
				serverSwitcher = ServerSwitcher.new();
		});
	}

	*addServer{
		("adding a server: "++numServers.asString).postln;
		servers.add(("lmi"++numServers.asString).asSymbol-> ModularServerObject.new(Server.new(("lmi"++numServers.asString).asSymbol, NetAddr("localhost", 57111+numServers), Server.local.options)));
		numServers = numServers+1;
		this.updateServerSwitcher;
	}

	*changeSetup {|serverName, setupNum|
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