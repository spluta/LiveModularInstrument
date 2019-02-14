ModularServerObject {
	var <>server, <>objectBusses, mainGroup, <>inGroup, <>mixerGroup, <>postMixerGroup, <>mixerTransferBus, <>mixerDirectInBus, synthGroup, <>synthGroups, <>inBusses, <>stereoInBusIndexes, volumeInRack, modularObjects, dimensions, <>mainMixer, mainWindow, <>busMap, <>isVisible;

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
			postMixerGroup = Group.tail(mainGroup);

			mixerDirectInBus = Bus.audio(server, 22);
			mixerTransferBus = Bus.audio(server, 22);

			//set up the inputs and outputs
			inBusses = List.new;
			8.do{arg i;  //right now the number of input channels is hardcoded to 8
				inBusses.add(Bus.audio(server,1));
			};


			//create objectBusses and groups in the shape of the dimensions array

			objectBusses = List.new;

			dimensions = [4,4]; //for now this is hard-coded


			objectBusses = List.fill((dimensions[0]*dimensions[1]), {Bus.audio(server,2)});

			synthGroups = List.fill((dimensions[0]*dimensions[1]), {Group.tail(synthGroup)});

			//create the Array of ModularObjects

			modularObjects = List.new;

			isVisible = true;

			16.do{arg i;
				modularObjects.add(
					ModularObjectPanel(server, synthGroups[i], i)
				);
			};

			mainWindow = MainProcessingWindow.new(mainGroup, modularObjects);

			mainMixer = MainMixer.new(mixerGroup, mixerTransferBus).init2(4, true);

			LiveModularInstrument.readyToRoll();
		})
	}

	makeBusMap {arg loadArray;

		loadArray.postln;

		busMap = Array.fill(3, {Dictionary.new});

		11.do{arg i; busMap.add((loadArray[0].asInteger+(i*2)).asSymbol -> (i*2))};

		loadArray[1].do{arg item, i; busMap[1].add(item.asSymbol -> i)};

		loadArray[2].do{arg item, i; busMap[2].add(item.asSymbol -> objectBusses[i].index)};

		busMap.postln;
	}

	getBusFromMap {arg busIn;
		var temp;

		temp = busMap[0][busIn];
		if(temp!=nil,{
			temp = "D"++temp.asString;
			temp.postln;
		},{
			temp = busMap[1][busIn];
			if(temp!=nil,{
				temp = "S"++temp.asString

			},{
				temp = busMap[2][busIn];
			})
		});
		^temp
	}

	save {
		var saveServer, temp;

		//save and load inputsArray

		saveServer = List.newClear(0);

		saveServer.add(mainWindow.save);

		saveServer.add(mixerDirectInBus.index);

		temp = List.newClear(0);
		inBusses.do{arg item;
			temp.add(item.index); //add busses
		};
		saveServer.add(temp);

		temp = List.newClear(0);
		objectBusses.do{arg item;
			temp.add(item.index); //add busses
		};
		saveServer.add(temp);

		temp = List.newClear(0);
		modularObjects.do{arg mop, i;
			temp.add(mop.save);
		};
		saveServer.add(temp);

		saveServer.add(mainMixer.save);

		^saveServer;
	}


	load {arg loadArray;
		var inBusTemp, stereoInBusTemp, internalBusTemp, flatObjectBus, flatMOPs, mopData;
		loadArray.do{arg item; item.postln};

		mainWindow.load(loadArray[0]);

		this.makeBusMap(loadArray.copyRange(1,3));

		loadArray[4].do{arg item, i;

			if(item.size>0, {modularObjects[i].load(item)})
		};

		mainMixer.load(loadArray[5]);
	}



	makeVisible {arg val;
		isVisible = val;

		if(val==true,{
			mainWindow.show;
			mainMixer.unhide;
		},{
			mainWindow.hide;
			mainMixer.hide;
		});
	}

	showAndPlay {arg bool;
		if(bool,{

			if(isVisible,{
				mainWindow.show;
				mainMixer.unhide;
			});

			mainMixer.unmute;

		},{
			mainWindow.hide;
			mainMixer.hide;
			mainMixer.mute;
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
	classvar <>numServers, <>inBusses;
	classvar <>servers, <>modularInputsArray, <>serverSwitcher;

	*boot {arg numServersIn, inBussesIn;
		numServers = numServersIn; inBusses = inBussesIn;
		servers = Dictionary.new(0);
		numServers.do{arg i;
			("adding a server: "++(i+1).asString).postln;
			servers.add(("lmi"++(i+1)).asSymbol-> ModularServerObject.new(Server.new("lmi"++(i+1).asString, NetAddr("localhost", 57111+i), Server.local.options)));
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
				temp.add(servers[("lmi"++(i+1)).asSymbol].save)
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


		if(serverName==nil,{
			modularInputsArray.load(loadArray[0]);
			numServersInFile = loadArray[2].size;


			min(numServersInFile, numServers).do{arg i;
				servers[("lmi"++(i+1)).asSymbol].load(loadArray[2][i])
			};
			//load the serverSwitcher last so that it can update the server windows
			if(loadArray[1]!=nil,{
				serverSwitcher.load(loadArray[1]);
			});

		}, {
			servers[serverName.asSymbol].load(loadArray[2][0])
		});



	}

	*addInputsArray {arg inBusses;
		modularInputsArray = ModularInputsArray.new;
		modularInputsArray.init2(inBusses);
	}

	*updateServerSwitcher {
		if(serverSwitcher!=nil,{
			serverSwitcher.reset;
		},{
			serverSwitcher = ServerSwitcher.new();
		});
	}

	*addServer{
		var num;
		num = numServers+1;
		("adding a server: "++num.asString).postln;
		servers.add(("lmi"++num.asString).asSymbol-> ModularServerObject.new(Server.new(("lmi"++num.asString).asSymbol, NetAddr("localhost", 57111+numServers), Server.local.options)));
		numServers = numServers+1;
		this.updateServerSwitcher;
	}

	*getSoundInBusses {arg serverName;
		^servers[serverName.asSymbol].inBusses.collect({arg item; item.index}).postln;
	}

	*getDirectInBus {arg serverName;
		^servers[serverName.asSymbol].mixerDirectInBus.index;
	}
	*getObjectBusses {arg serverName;
		^servers[serverName.asSymbol].objectBusses;
	}

	/*	*getSynthGroup {arg serverName, location;
	^servers[serverName.asSymbol].synthGroups[location];
	}

	*getMixerGroup {arg serverName;
	^servers[serverName.asSymbol].mixerGroup;
	}*/


}