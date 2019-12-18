FluidKDTree : UGen {
	var <>server, <>dataSetObject, <>maxK, <>action, <>nodeID, dimensions, <>nearestBus, synth, instance;

/*	*new {|server, dataSetObject, maxK, action|
		^super.newCopyArgs(server, dataSetObject, maxK, action).init;
		//^super.new.init(server, dataSet);
	}*/

	*new {|server, dataSetObject, maxK, action|
		^super.new.server_(server).dataSetObject_(dataSetObject).maxK_(maxK).action_(action).init;
	}

	init {
		server.postln;
		//Owen's bullshit server stuff
		//nearestBus = Bus.control(maxK);
		//this.createNodeID(server);
		//this.indexDataSet(action);

	}

	*createNodeID {|server|
		var instance, synth;
		"kill me".postln;
		synth = {instance = FluidKDTree.kr()}.play(server);
		instance.nodeID = synth.nodeID;
		instance.nodeID.postln;
	}

/*	maxK_ {|num|
		maxK = num;
		nearestBus.free;
		nearestBus = Bus.kr(maxK);
	}*/

	indexDataSet {|action|
		server.sendMsg('/u_cmd', nodeID, this.synthIndex, 'index', dataSetObject.asUGenInput);

		OSCFunc({|msg|
			//messageResponse
			if(action.notNil){action.value};
		}, '/index').oneShot;
	}


	setDataSetObject {arg dataSetIn;
		dataSetObject = dataSetIn;
		this.indexDataSet;
	}

	kNearest {|buffer, num, action|
		server.sendMsg('/u_cmd', nodeID, this.synthIndex, 'kNearest', dataSetObject.asUGenInput);

		OSCFunc({|msg|
			//messageResponse
			//
			if(action.notNil){action.value};
		}, '/kNearest').oneShot;
	}


	*kr { /*source, startFrame = 0, numFrames = -1, startChan = 0, numChans = -1, gain = 1, destination, destStartFrame = 0, destStartChan = 0, destGain = 0, */|doneAction = 0|

        ^this.multiNew('control');
	}

	getKDTree {
		//make the kdtree buf
		//return kdtree buf
	}

	read {|file| }

	write {|file| }

}

