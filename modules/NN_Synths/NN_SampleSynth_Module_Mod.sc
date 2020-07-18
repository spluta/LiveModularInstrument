FluidFolder {
	var <>path, <>buffer, <>index;

}

NN_SampleSynth_Mod : NN_Synth_Mod {
	var loader, tree, folder,dataBuf, dataBufs, task, friends, loader, index, dur, grainEnv, bufIsLoaded=false, ranges, controlVals, uGotTreed, volBus, synthName, chanVolBus;

	killMeSpecial {
		this.killThePythons;
		task.stop;
		tree.free;
	}

	pause {
		task.pause;
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
		bigSynthGroup.set(\pauseGate, 0);bigSynthGroup.run(false);
	}

	unpause {
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
		bigSynthGroup.run(true); bigSynthGroup.set(\pauseGate, 1);
		task.start;
	}

	configure {
		var startPoint;
		valList = controlValsList;

		startPoint = whichModel*3;

		2.do{|i| valList.put(i, nnVals[startPoint+i][1].map(valList[i]))};
		if(synths[0]!=nil){synths[0].set(\dur, nnVals[startPoint+2][1].map(valList[2]))};
	}

	setSynth {|argument, i, val01, val|
		valList.put(i, val01);
	}

	changeModel {|i|
		whichModel = i;
		this.configure;
	}

	trainNN {  //using this to save the model settings into a json file
		var array;

		array = nnVals.collect{|item| item.postln; [item[1].minval, item[1].maxval]};
		array.writeArchive(folder++"controlVals");
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
	}

	loadTraining {|modelFolderIn|
		"loading tree".postln;
		modelFolderIn.postln;
		modelFolder = modelFolderIn;
		loadedCount = 0;

		if(synths[0]!=nil){synths[0].free};

		loader.index = Object.readArchive(modelFolder++"/index");

		controlVals = Object.readArchive(modelFolder++"/controlVals");

		controlVals.do{|val, i| controls[i].valueAction_(val)};

		loader.index.postln;

		tree.read(modelFolder++"/datasetTree.json", {uGotTreed=true});

		loader.buffer = Buffer.read(group.server.postln, modelFolder++"/buffer.wav",action:{|buffer|
			"Loaded".postln;
			bufIsLoaded = true;

			synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, volBus.index, \onOff0, 0, \onOff1, 0, \buffer, buffer, \grainEnv, grainEnv, \chanVolBus, chanVolBus], group));

		});

	}

	createWindow {
		{
			nnVals.postln;
			ranges = [[0,1],[0,1],[0.01, 0.2]];
			8.do{|i|
				3.do{|i2|
					controls.add(QtEZRanger(nnVals[i*3+i2][0], ControlSpec(ranges[i2][0], ranges[i2][1]), {arg val;
						//nnVals[i].put(1, ControlSpec(val.value[0], val.value[1]))
						nnVals[i*3+i2][1].minval_(val.value[0]).maxval_(val.value[1]);
					}, [ranges[i2][0], ranges[i2][1]], true, 'horz'))
				};
			};
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

		grainEnv = Buffer.sendCollection(group.server, Env([0, 1, 1, 0], [0.001, 0.998, 0.001]).discretize, 1);

		folder = PathName(this.class.filenameSymbol.asString).pathOnly;//++"model0/";
		folder.postln;

		tree = FluidKDTree(group.server);
		loader = FluidFolder();

		synths = List.newClear(1);

		uGotTreed = false;

		dataBuf = Buffer.new(group.server);
		task = Task{
			inf.do{
				//tree.postln;
				if(uGotTreed){
					dataBuf.free;
					dataBuf = Buffer.loadCollection(group.server, valList.copyRange(0,1), 1, {|buf|
						tree.kNearest(buf,1,{|x|
							var v;
							friends = x;
							if(bufIsLoaded){
								v = loader.index[friends.asSymbol];
								if(synths[0]!=nil){synths[0].set(\startFrame, v[\bounds][0], \endFrame, v[\bounds][1])};
							}
						})
					});
				};
				0.05.wait;
			}
		}.play;


			this.init_window(parent);
		}
	}