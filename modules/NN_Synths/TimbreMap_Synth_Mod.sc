TimbreMap_Synth_Mod : NN_Synth_Mod {
	var trees, synthData, norm, stand, pca, buf, dest0, dest1, dest2, dest3, readyToGo, constrainSpecs, folder, listeningSynth;

	init_window {|parentIn|
		var hiddenArray;

		modelFolder = nil;

		parent = parentIn;

		currentPoint = 0;
		sliderCount = 0;
		numPoints = 0;
		keys = Set[];

		onOff0 = 0;
		onOff1 = 0;

		controlValsList = List.newClear(sizeOfNN);
		valsList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(8, {List.fill(sizeOfNN+4, {0})});

		nnInputVals = (0!parent.inputControl.numActiveControls);

		whichModel = 0;
		readyToPredict = false;


		setLemurSpeedLimit = SpeedLimit({|array| parent.setLemur(array)}, 0.05);
		this.createWindow;
	}

	clearTraining {
		"clear training".postln;
		trees.do{|item| item.free};
		synthData.do{|item| item.free};
		norm.free;
		stand.free;
		pca.free;
		buf.free;
		dest0.free;
		dest1.free;
		dest2.free;
		dest3.free;
		listeningSynth.free;
	}

	clearMLPs {

	}

	loadTraining {|modelFolderIn|
		"loading training".postln;
		modelFolder = modelFolderIn.postln;
		{
			trees = List.fill(100, {|i| FluidKDTree(group.server, 5)});
			synthData = List.fill(100, {|i| FluidDataSet(group.server, "ds_synth_"++i)});
			1.wait;
			group.server.sync;

			100.do{|i|
				trees[i].read(modelFolder++"/trees/tree_"++i++".json");
				group.server.sync;
			};
			100.do{|i|
				synthData[i].read(modelFolder++"/synthData/dset_"++i++".json");
				group.server.sync;
			};

			trees[100.rand].size;
			"make norm and stand".postln;
			norm = FluidNormalize(group.server);
			stand = FluidStandardize(group.server);
			pca = FluidPCA(group.server);
			group.server.sync;
			"read jsons".postln;
			stand.read(modelFolder++"/stand0.json");
			pca.read(modelFolder++"/pca0.json");
			norm.read(modelFolder++"/norm0.json");

			buf = Buffer.alloc(group.server, 19);

			this.makeListeningSynth;

			dest0 = Buffer.new(group.server);
			dest1 = Buffer.new(group.server);
			dest2 = Buffer.new(group.server);
			dest3 = Buffer.new(group.server);

			constrainSpecs = List.fill(7, {ControlSpec(0,1)});

			"done loading timbre map".postln;
			readyToPredict = true;
		}.fork;
	}

	init2 {arg nameIn, parent, volBus, onOff0, onOff1, chanVolBus;
		synthArgs = [nameIn, parent, volBus, onOff0, onOff1, chanVolBus];
		"nn_synth group: ".post;
		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1, \chanVolBus, chanVolBus], group));
		this.init_window(parent);
	}

	reloadSynth {
		{
			synths.do{|item| item.free};
			group.server.sync;
			synths.put(0, Synth(synthArgs[0], [\outBus, outBus, \volBus, synthArgs[2].index, \onOff0, synthArgs[3]-1, \onOff1, synthArgs[4]-1, \chanVolBus, synthArgs[5]], synthGroup));
		}.fork;
	}

	createWindow {
		{
			nnVals.postln;
			nnVals.do{arg item, i;
				controls.add(QtEZSlider(item[0], item[1], {arg val;
					synths[0].set(item[0], val.value);
					{valsList.put(i, val.slider.value)}.defer;
				}, allValsList[0][i], true, \horz));
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

	changeModel {|i|
		/*		"change model".postln;
		allValsList.put(whichModel, valsList.addAll(controlValsList));
		whichModel = i;
		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valsList, true);*/
	}

	configure {
		setLemurSpeedLimit.value(controlValsList);
		this.setSlidersAndSynth(controlValsList);
		setLemurSpeedLimit.value(controlValsList);
	}

	setNNInputVals {|vals|
		if(readyToPredict){
			controlValsList = vals.collect{|val,i| constrainSpecs[i].map(val)};
			this.configure;
		}
	}

	trigger {|num, val|
		if(num==0){
			if(readyToPredict&&(val==1)){
				"triggerOn".postln;
				//buf.loadToFloatArray(0, 19, {|array| array.postln});
				stand.transformPoint(buf, dest0, {
					pca.transformPoint(dest0, dest1, {
						norm.transformPoint(dest1, dest2, {
							var num = 100.rand;
							trees[num].kNearest(dest2, {|ids|
								synthData[num].getPoint(ids[0], dest3, {|buf| buf.getn(0, 7, {|array|
									//array.postln;
									array.do{|item, i|
										if(i<2){
											constrainSpecs[i].minval_(item*0.975);
											constrainSpecs[i].maxval_(item*1.025);
											item = item*rrand(0.975, 1.025); //probably should set to current x or y vals
										}{
											constrainSpecs[i].minval_(item*0.9);
											constrainSpecs[i].maxval_(item*1.1);
											item = item*rrand(0.9, 1.1)
										};
										readyToPredict=true;
									};
									this.setSlidersAndSynth(array);
									synths[0].set(\onOff0, val);
				})})})})})});
			}{
				"triggerOff".postln;
				synths[0].set(\onOff0, val);
			}
		}
	}

	setSynth {|argument, i, val01, val|
		valsList.put(i, val01);
		synths[0].set(argument, val);
	}

	setSlidersAndSynth2 {
		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valsList);
	}

	setGUISlider {|i, val|
		if(i<sizeOfNN){controls[i].valueAction_(controls[i].controlSpec.map(val))}
	}

	setSlidersAndSynth {|vals, isPoint=false|
		sliderCount = sliderCount+1;

		vals.do{|item, i|
			if(i<sizeOfNN,{
				if((parent.hasControl[i]==0)&&(nnVals[i][0].asString!="nil"))
				{
					this.setSynth(nnVals[i][0], i, vals[i], controls[i].controlSpec.map(item));
				};
			});
		};

		if(parent.updateSliders==true){
			if((sliderCount%50==0)||isPoint){
				vals.do{|item, i|
					if(parent.hasControl[i]==0){
						{controls[i].value_(controls[i].controlSpec.map(item))}.defer;
					}
				}
			}
		};
	}

	makeInOutBufs {

	}

	reloadNN {

	}

	trainNN {
	}

	loadPoints {
	}

	newPointsList {
	}

	addPoint {
	}

	copyPoint {
	}

	pastePoint {|point|
	}

	removePoint {
	}

	nextPoint {
	}


	getLabels {
		^nnVals.collect{arg item; item[0].asString};
	}

	killMeSpecial {
		this.clearTraining;
	}

}