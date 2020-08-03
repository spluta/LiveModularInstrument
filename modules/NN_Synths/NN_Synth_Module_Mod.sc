NN_Synth_ID {
	classvar <id=5000;
	*initClass { id = 5000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

NN_Synth_DataSetID {
	classvar <id=5000;
	*initClass { id = 5000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

Kill_The_Pythons {
	*kill {
		100.do{|i|
			NetAddr("127.0.0.1", 5000+i).do{|i|i.sendMsg('/close')};
		}
	}
}

NN_Synth_Mod : Module_Mod {
	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, nnInputVals, valsList, allValsList, nnVals, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC, <>modelFolder, <>onOff0, <>onOff1, mlpInBuf, mlpOutBuf, mlps, inDataSet, outDataSet, inBuf, outBuf, copyInBuf, copyOutBuf, numPoints, keys;

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

		inDataSet = FluidDataSet(group.server,("indata"++NN_Synth_DataSetID.next));
		outDataSet = FluidDataSet(group.server,("outdata"++NN_Synth_DataSetID.next));
		inBuf = Buffer(group.server);
		outBuf = Buffer(group.server);

		copyInBuf = Buffer(group.server);
		copyOutBuf = Buffer(group.server);

		controlValsList = List.newClear(0);
		valsList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(8, {List.fill(sizeOfNN+4, {0})});

		nnInputVals = (0!parent.inputControl.numActiveControls);

		whichModel = 0;

		//hard coding this to 3, which should be fine...will probably find this in 5 years and wonder wtf
		hiddenArray = (3, 3+(valsList.size/5)..valsList.size).floor.asInteger.copyRange(1,3);

		mlps = List.fill(8, {FluidMLPRegressor(group.server,hiddenArray,2,1,0,1000,0.1,0,1,0)});

		this.createWindow;
	}

	clearTraining {
		modelFolder = nil;
		this.clearMLPs;
	}

	clearMLPs {
		"clear MLPs".postln;
		//pythonAddrs.do{|item|item.sendMsg('/close')}
		mlps = mlps.do{|item| item.clear};
		mlps.postln;
	}

	loadTraining {|modelFolderIn|
		"loading training".postln;
		modelFolder = modelFolderIn.postln;
		loadedCount = 0;

		controlValsList = List.fill(File.readAllString(modelFolder++"/inDataSet0.json").parseYAML["cols"].asInteger.postln, {0});

		this.makeInOutBufs;

		numModels.do{|i|
			this.reloadNN(i);
		};
	}

	init2 {arg nameIn, parent, volBus, onOff0, onOff1, chanVolBus;
		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1, \chanVolBus, chanVolBus], group));
		this.init_window(parent);
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
		"change model".postln;
		allValsList.put(whichModel, valsList.addAll(controlValsList));
		whichModel = i;
		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valsList, true);
	}

	configure {//"configure".postln;
		if(parent.predictOnOff==1){
			//[mlpInBuf,mlpOutBuf].postln;
			//whichModel.postln;
			mlps[whichModel].predictPoint(mlpInBuf,mlpOutBuf,{
				mlpOutBuf.loadToFloatArray(action:{|array|
					array = array.asArray;
					this.setSlidersAndSynth(array);
					parent.setLemur(array);
				})
			});
		};
	}

	setNNInputVals {|vals|
		controlValsList = vals;
		mlpInBuf.setn(0, vals);
		this.configure;
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
		"makeInOutBufs".postln;
		if(mlpInBuf != nil){mlpInBuf.free};
		if(mlpOutBuf != nil){mlpOutBuf.free};

		mlpInBuf = Buffer.alloc(group.server,controlValsList.size);
		mlpOutBuf = Buffer.alloc(group.server,valsList.size);
	}

	reloadNN {arg reloadWhich;
		var fileInfo, hiddenArray, modelFile;
		if(reloadWhich==nil){reloadWhich = whichModel};
		"clear".postln;
		mlps[reloadWhich].clear({
			"reload ".post;
			modelFile = modelFolder++"/"++"modelFile"++reloadWhich++".json";
			try{
				File.readAllString(modelFile).parseYAML;
				mlps[reloadWhich].read(modelFile, {("loaded"+modelFile)});
			}{"no json file".postln}
		})
	}

	trainNN {
		var saveFile, modelFile, hiddenArray, controlValsDict, valDict;

		this.makeInOutBufs;

		mlps[whichModel].fit(inDataSet,outDataSet,{|x|
			"trainy trainy trainy".postln;
			x.postln;
		});

		mlps[whichModel].write(modelFolder++"/"++"modelFile"++whichModel++".json");
		inDataSet.write(modelFolder++"/"++"inDataSet"++whichModel++".json");
		outDataSet.write(modelFolder++"/"++"outDataSet"++whichModel++".json");
	}

	loadPoints {
		var tempA, tempB, fileInfo;
		currentPoint = 0;

		if(modelFolder!=nil){
			inDataSet.read(modelFolder++"/"++"inDataSet"++whichModel++".json", {
				outDataSet.read(modelFolder++"/"++"outDataSet"++whichModel++".json", {

					//inDataSet.size({|val| numPoints = val.asInteger});

					inDataSet.print;
					outDataSet.print;

					outDataSet.dump({|vals|
						var max=0, newPoint;

						keys = vals["data"].keys.asList;

						//vals["data"].keys.do{|item| if(item.asInteger.postln>max){max=item}};

						inDataSet.getPoint(keys[currentPoint], inBuf, {inBuf.postln; inBuf.loadToFloatArray(action:{|array|
							array.postln;
							controlValsList = array.asList;
							{parent.setControlPointsNoAction(controlValsList)}.defer;
						})});

						outDataSet.getPoint(keys[currentPoint], outBuf, {outBuf.loadToFloatArray(action:{|array|
							valsList = array.asList.postln;
							this.setSlidersAndSynth(valsList, true);
							{parent.setLemur(valsList)}.defer;
						})});
					})
				})
			})

		}{"no model".postln}
	}

	newPointsList {
		inDataSet.clear;
		outDataSet.clear;
		keys = List[];

		currentPoint = 0;
	}

	addPoint {
		[valsList.size, controlValsList.size].postln;
		outDataSet.dump({|vals|
			var max=0, newPoint;
			vals.postln;

			if(vals["data"]!=nil){
				vals["data"].keys.do{|item| if(item.asInteger.postln>max){max=item}};
				max.postln;
				newPoint = max.asInteger+1;
			}{newPoint = 0};

			Buffer.loadCollection(group.server, controlValsList.asArray, 1, {|array|
				inDataSet.addPoint(newPoint.asString, array);
				inDataSet.print;
				Buffer.loadCollection(group.server, valsList.asArray, 1, {|array|
					outDataSet.addPoint(newPoint.asString, array);
					outDataSet.print;
					numPoints = numPoints+1;
					keys.add(newPoint.asString);
				});
			});
		})

	}

	copyPoint {
		"copy point".postln;
		copyInBuf.copyData(inBuf);
		copyOutBuf.copyData(outBuf);
	}

	pastePoint {|point|
		outDataSet.dump({|vals|
			var max=0, newPoint;
			vals["data"].keys.do{|item| if(item.asInteger.postln>max){max=item}};
			max.postln;
			newPoint = max+1;
			inDataSet.addPoint(newPoint.asString, copyInBuf);
			outDataSet.addPoint(newPoint.asString, copyOutBuf);
		});
	}

	removePoint {
		var cp = keys[currentPoint];

		inDataSet.print;
		outDataSet.print;

		inDataSet.deletePoint(cp, {
			outDataSet.deletePoint(cp, {
				keys.removeAt(currentPoint);
				keys.postln;
				currentPoint = currentPoint.wrap(0, keys.size-1);

		})});
	}

	nextPoint {
		var key;

		if(keys.size>0){
			currentPoint = (currentPoint+1).wrap(0, keys.size-1);
			currentPoint.postln;
			key = keys[currentPoint];

			inDataSet.getPoint(key, inBuf, {
				inBuf.loadToFloatArray(action:{|array|
					array.postln;

					controlValsList = array;
					controlValsList.postln;
					{parent.setControlPointsNoAction(controlValsList)}.defer;
				})
			});

			outDataSet.getPoint(key, outBuf, {
				outBuf.loadToFloatArray(action:{|array|
					array.postln;
					valsList = array;

					{this.setSlidersAndSynth(valsList, true)}.defer;
					parent.setLemur(valsList);
					valsList.postln;
				})
			});
		}{"no points loaded".postln}
	}

	reloadSynth {
		"should be overwritten by the synth".postln;
	}

	killMeSpecial {
		this.clearMLPs;
		mlpInBuf.free;
		mlpOutBuf.free;
	}

	getLabels {
		^nnVals.collect{arg item; item[0].asString};
	}

	//loadExtra {win.visible = false}
}


