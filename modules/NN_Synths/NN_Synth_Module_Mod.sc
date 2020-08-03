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
	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, nnInputVals, valsList, allValsList, nnVals, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC, <>modelFolder, <>onOff0, <>onOff1, mlpInBuf, mlpOutBuf, mlps, inDataSet, outDataSet, inBuf, outBuf, copyInBuf, copyOutBuf, numPoints;

	init_window {|parentIn|
		var hiddenArray;

		modelFolder = nil;

		parent = parentIn;

		currentPoint = 0;
		sliderCount = 0;
		numPoints = 0;

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

		//{
		numModels.do{|i|
			this.reloadNN(i);
			//0.2.wait;
		};
		//}.fork;
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

					inDataSet.size({|val| numPoints = val.asInteger});

					inDataSet.print;
					outDataSet.print;

					inDataSet.getPoint(currentPoint.asString, inBuf, {inBuf.postln; inBuf.loadToFloatArray(action:{|array|
						array.postln;
						controlValsList = array.asList;
						{parent.setControlPointsNoAction(controlValsList)}.defer;
					})});

					outDataSet.getPoint(currentPoint.asString, outBuf, {outBuf.loadToFloatArray(action:{|array|
						valsList = array.asList.postln;
						this.setSlidersAndSynth(valsList, true);
						{parent.setLemur(valsList)}.defer;
					})});
				})
			})

		}{"no model".postln}
	}

	newPointsList {
		inDataSet.clear;
		outDataSet.clear;

		currentPoint = 0;
		numPoints = 0;
	}

	addPoint {
		[valsList.size, controlValsList.size].postln;
		Buffer.loadCollection(group.server, controlValsList.asArray, 1, {|array|
			inDataSet.addPoint(numPoints.asString, array);
			inDataSet.print;
			Buffer.loadCollection(group.server, valsList.asArray, 1, {|array|
				outDataSet.addPoint(numPoints.asString, array);
				outDataSet.print;
				numPoints = numPoints+1;
			});
		});



	}

	copyPoint {
		"copy point".postln;
		copyInBuf.copyData(inBuf);
		copyOutBuf.copyData(outBuf);
	}

	pastePoint {|point|
		inDataSet.addPoint(numPoints.asString, copyInBuf);
		outDataSet.addPoint(numPoints.asString, copyOutBuf);
		numPoints = numPoints+1;
	}

	consoldatePoints {
		var changed;
		changed = false;

		numPoints.do{}
	}

	removePoint {
		var cp = currentPoint;

		inDataSet.print;
		outDataSet.print;


		inDataSet.deletePoint(cp.asString, {
			outDataSet.deletePoint(cp.asString, {
				if(cp==(numPoints-1))
				{"last point removed".postln}
				{
					inDataSet.dump({|vals|
						if(vals["data"][cp.asString]==nil){
							cp.postln;

							((cp+1)..(numPoints-1)).do{|i2|
								var temp;
								i2.postln;
								vals.postln;
								temp = vals["data"][i2.asString];
								vals["data"].removeAt(i2.asString);
								vals["data"].add((i2-1).asString-> temp);
						}};
						inDataSet.load(vals);
						outDataSet.dump({|vals2|
							if(vals2["data"][cp.asString]==nil){
								cp.postln;

								((cp+1)..(numPoints-1)).do{|i2|
									var temp;
									i2.postln;
									vals2.postln;
									temp = vals2["data"][i2.asString];
									vals2["data"].removeAt(i2.asString);
									vals2["data"].add((i2-1).asString-> temp);
							}};
							outDataSet.load(vals2);
							vals["data"].postln;
							vals2["data"].postln;
							numPoints = numPoints-1;
							numPoints.postln;
						});
					});

				}
		})});
	}

	nextPoint {
		[currentPoint, numPoints].postln;
		if(numPoints>0){
			currentPoint = (currentPoint+1).wrap(0, numPoints-1);
			currentPoint.postln;
			inDataSet.getPoint(currentPoint.asSymbol, inBuf, {
				inBuf.loadToFloatArray(action:{|array|
					array.postln;

					controlValsList = array;
					controlValsList.postln;

					{parent.setControlPointsNoAction(controlValsList)}.defer;
				})
			});

			outDataSet.getPoint(currentPoint.asSymbol, outBuf, {
				outBuf.loadToFloatArray(action:{|array|
					array.postln;
					valsList = array;

					{this.setSlidersAndSynth(valsList, true)}.defer;
					parent.setLemur(valsList);

					valsList.postln;
				})
			});

		}
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


