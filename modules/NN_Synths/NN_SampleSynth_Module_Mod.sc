FluidFolder {
	var <>path, <>buffer, <>index;

}

FluidName_ID {
	classvar <id=1000;
	*initClass { id = 1000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

NN_SampleSynth_Mod : NN_Synth_Mod {
	var loader, ds, tree, folder,dataBuf, dataBufs, task, friends, loader, index, dur, grainEnv, bufIsLoaded=false, ranges, controlVals, uGotTreed, volBus, synthName, chanVolBus, treeInBus, treeOutBus, treeInBuffer, treeOutBuffer, addRandomVal;

	killMeSpecial {
		tree.free;
		ds.free;
		loader.free;
	}

	pause {
		//task.pause;
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
		bigSynthGroup.set(\pauseGate, 0);bigSynthGroup.run(false);
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
		bigSynthGroup.run(true); bigSynthGroup.set(\pauseGate, 1);
		//task.start;
	}

	configure {
		var startPoint, rando;
		valsList = controlValsList;

		startPoint = whichModel*3;

		2.do{|i| valsList.put(i, nnVals[startPoint+i][1].map(valsList[i]+addRandomVal))};

		treeInBuffer.setn(0, valsList.copyRange(0,2).asArray);

		if(synths[0]!=nil){synths[0].set(\dur, nnVals[startPoint+2][1].map(valsList[2]))};
	}

	setSynth {|argument, i, val01, val|
		valsList.put(i, val01);
	}

	changeModel {|i|
		whichModel = i;
		this.configure;
	}

	trainNN {  //using this to save the model settings into a json file
		var array;

		array = (nnVals.collect{|item| item; [item[1].minval, item[1].maxval]}).add(addRandomVal);
		array.writeArchive(modelFolder++"controlVals");
	}

	reloadNN {//calls this when it

	}
	loadPoints {}
	newPointsList {}
	pastePoint {}
	copyPoint {}
	removePoint {}
	nextPoint {}

	clearTraining {
		modelFolder = nil;
		if(synths[0]!=nil){synths[0].free};
		uGotTreed=false;
		tree.free;
		ds.free;
		loader.free;
	}

	loadTraining {|modelFolderIn|

		ds = FluidDataSet(group.server, FluidName_ID.next.asString);

		tree = FluidKDTree(group.server, 1, lookupDataSet:ds);
		loader = FluidFolder();

		modelFolder = modelFolderIn;
		loadedCount = 0;

		if(synths[0]!=nil){synths[0].free};

		loader.index = Object.readArchive(modelFolder++"/index");

		controlVals = Object.readArchive(modelFolder++"/controlVals");

		controlVals.copyRange(0, controlVals.size-2).do{|val, i| controls[i].valueAction_(val)};

		controls.last.valueAction_(controlVals.last);

		ds.read(modelFolder++"/indices.json", {
			tree.read(modelFolder++"/datasetTree.json", {
				tree.inBus_(treeInBus).outBus_(treeOutBus).inBuffer_(treeInBuffer).outBuffer_(treeOutBuffer);
				loader.buffer = Buffer.read(group.server, modelFolder++"/buffer.wav",action:{|buffer|
					bufIsLoaded = true;

					synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, volBus.index, \treeInBus, treeInBus.index, \treeOutBus, treeOutBus.index, \treeInBuffer, treeInBuffer, \treeOutBuffer, treeOutBuffer, \onOff0, 0, \onOff1, 0, \buffer, buffer, \grainEnv, grainEnv, \chanVolBus, chanVolBus], group));

				});
			});
		});

	}

	createWindow {
		{
			ranges = [[0,1],[0,1],[0.01, 0.2]];
			8.do{|i|
				3.do{|i2|
					controls.add(QtEZRanger(nnVals[i*3+i2][0], ControlSpec(ranges[i2][0], ranges[i2][1]), {arg val;
						//nnVals[i].put(1, ControlSpec(val.value[0], val.value[1]))
						nnVals[i*3+i2][1].minval_(val.value[0]).maxval_(val.value[1]);
					}, [ranges[i2][0], ranges[i2][1]], true, 'horz'))
				};
			};
			controls.add(QtEZSlider("add random", ControlSpec(0, 0.01), {|val| addRandomVal=val.value}, 0, true, 'horz'));
			if(nnVals.size<41){
				win.layout = VLayout(
					*controls.collect({arg item, i;
						HLayout(item)})
				);
			}{
				win.layout = HLayout(
					VLayout(
						*controls.copyRange(0,39).collect({arg item, i;
							HLayout(item)})
					),
					VLayout(
						*controls.copyRange(40,controls.size-1).collect({arg item, i;
							HLayout(item)})
					)
				)
			};
			win.layout.spacing_(0).margins_(0!4);

			win.visible_(false);
		}.defer;
	}


	init2 {arg nameIn, parent, volBusIn, onOff0In, onOff1In, chanVolBusIn;
		volBus = volBusIn;
		synthName = nameIn;
		chanVolBus = chanVolBusIn;

		treeInBus = Bus.control(group.server);
		treeOutBus = Bus.control(group.server);
		treeInBuffer = Buffer.alloc(group.server,2);
		treeOutBuffer = Buffer.alloc(group.server,2);

		addRandomVal = 0;

		grainEnv = Buffer.sendCollection(group.server, Env([0, 1, 1, 0], [0.001, 0.998, 0.001]).discretize, 1);

		folder = PathName(this.class.filenameSymbol.asString).pathOnly;//++"model0/";

		synths = List.newClear(1);

		uGotTreed = false;

		dataBuf = Buffer.new(group.server);

		this.init_window(parent);
	}
}