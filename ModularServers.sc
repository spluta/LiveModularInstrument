ModularServer_ID {
	classvar <id=57100;
	*initClass { id = 57100; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

ModularServerObject {
	var <>serverName, <>blockSize, <>server, <>objectBusses, mainGroup, <>inGroup, <>mixerGroup, <>postMixerGroup, <>mixerDirectInBus, synthGroup, <>synthGroups, <>inBusses, <>stereoInBusIndexes, volumeInRack, modularObjects, dimensions, <>mainMixer, mainWindow, <>busMap, <>isVisible, id, directIns;

	*new {|serverName, blockSize|
		^super.new.serverName_(serverName).blockSize_(blockSize).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("directInputs_mod", {arg outBus;
				Out.ar(outBus, SoundIn.ar((0..21)));
			}).writeDefFile;
		};
	}

	init {
		id = ModularServer_ID.next;

		while( {("lsof -i:"++id).unixCmdGetStdOut.size > 0},{id = ModularServer_ID.next});

		blockSize ?? {"set blockSize".postln; blockSize = 128};

		Server.local.options.blockSize_(blockSize);

		Server.local.options.blockSize.postln;
		Server.local.options.device.postln;

		server = Server.new(serverName, NetAddr("localhost", id), Server.local.options);
		server.waitForBoot({
			//set up groups
			server.sync;
			mainGroup = Group.tail(server);
			server.sync;
			1.wait;
			inGroup = Group.tail(mainGroup);
			synthGroup = Group.tail(mainGroup);
			mixerGroup = Group.tail(mainGroup);
			postMixerGroup = Group.tail(mainGroup);

			mixerDirectInBus = Bus.audio(server, 22);
			directIns = Synth("directInputs_mod", [\outBus, mixerDirectInBus], inGroup);

			//set up the inputs and outputs
			inBusses = List.new;
			8.do{arg i;  //right now the number of input channels is hardcoded to 8
				inBusses.add(Bus.audio(server,1));
			};

			//create objectBusses and groups in the shape of the dimensions array

			objectBusses = List.new;

			dimensions = [5,5]; //for now this is hard-coded


			objectBusses = List.fill((dimensions[0]*dimensions[1]), {Bus.audio(server,2)});
			server.sync;
			synthGroups = List.fill((dimensions[0]*dimensions[1]), {Group.tail(synthGroup)});
			server.sync;
			0.1.wait;
			//create the Array of ModularObjects

			modularObjects = List.new;

			isVisible = true;

			(dimensions[0]*dimensions[1]).do{arg i;
				modularObjects.add(
					ModularObjectPanel(server, synthGroups[i], i)
				);
			};

			mainWindow = MainProcessingWindow.new(mainGroup, modularObjects);

			mainMixer = MainMixer.new(mixerGroup, 0).init2(4, true);

			LiveModularInstrument.readyToRoll();
		})
	}

	sendGUIVals {
		modularObjects.do{arg item; item.sendGUIVals};
		mainMixer.sendGUIVals;
	}

	makeBusMap {arg loadArray;

		busMap = Array.fill(3, {Dictionary.new});

		11.do{arg i; busMap.add((loadArray[0].asInteger+(i*2)).asSymbol -> (i*2))};

		loadArray[1].do{arg item, i; busMap[1].add(item.asSymbol -> i)};

		loadArray[2].do{arg item, i; busMap[2].add(item.asSymbol -> objectBusses[i].index)};
	}

	getBusFromMap {arg busIn;
		var temp;

		temp = busMap[0][busIn];
		if(temp!=nil,{
			temp = "D"++temp.asString;
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

		mainWindow.load(loadArray[0]);

		this.makeBusMap(loadArray.copyRange(1,3));
		//{
		loadArray[4].do{arg item, i;
			if(item.size>0){
				modularObjects[i].load(item);
			};
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
				modularObjects.do{|item| item.resume};
			});

			mainMixer.unmute;

		},{
			modularObjects.do{|item| item.pause};
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
		directIns.free;
	}
}

ModularServers {
	classvar <>numServers, <>inBusses;
	classvar <>servers, <>modularInputsArray, <>serverSwitcher, <>device;

	*boot {arg numServersIn, inBussesIn, blockSizesIn;
		numServers = numServersIn; inBusses = inBussesIn;
		servers = Dictionary.new(0);
		{
			numServers.do{arg i;
				servers.add(("lmi"++(i+1)).asSymbol-> ModularServerObject.new("lmi"++(i+1), blockSizesIn[i]));
				0.5.wait;
			}
		}.fork(AppClock);

	}

	*save {arg path, serverName;
		var saveServers, temp;

		saveServers = List.newClear(0);

		saveServers.add(modularInputsArray.save); //save the inputs array

		if(numServers>1,{
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

			{
				1.wait;
				min(numServersInFile, numServers).do{arg i;
					servers[("lmi"++(i+1)).asSymbol].load(loadArray[2][i]);
					0.5.wait;
				};
				//load the serverSwitcher last so that it can update the server windows
				if(loadArray[1]!=nil,{
					serverSwitcher.load(loadArray[1]);
				});
				//Window.allWindows.do{arg item; item.front};
			}.fork(AppClock);

		}, {
			AppClock.sched(rrand(1.0,3), {servers[serverName.asSymbol].load(loadArray[2][0])});
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
			serverSwitcher = ServerSwitcher2.new();
		});
	}

	*addServer{
		var num;
		num = numServers+1;
		servers.add(("lmi"++num.asString).asSymbol-> ModularServerObject.new(Server.new(("lmi"++num.asString).asSymbol, NetAddr("localhost", 57111+numServers), Server.local.options)));
		numServers = numServers+1;
		this.updateServerSwitcher;
	}

	*getSoundInBusses {arg serverName;
		^servers[serverName.asSymbol].inBusses.collect({arg item; item.index});
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