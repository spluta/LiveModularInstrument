// NN_Synth_ID {
// 	classvar <id=5000;
// 	*initClass { id = 5000; }
// 	*next  { ^id = id + 1; }
// 	*path {this.filenameSymbol.postln}
// }
//
// NN_Synth_DataSetID {
// 	classvar <id=5000;
// 	*initClass { id = 5000; }
// 	*next  { ^id = id + 1; }
// 	*path {this.filenameSymbol.postln}
// }
//
// Kill_The_Pythons {
// 	*kill {
// 		100.do{|i|
// 			NetAddr("127.0.0.1", 5000+i).do{|i|i.sendMsg('/close')};
// 		}
// 	}
// }
//
// NN_Synth_Mod : Module_Mod {
// 	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, nnInputVals, valsList, allValsList, nnVals, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC, <>modelFolder, <>onOff0, <>onOff1, mlpInBuf, mlpOutBuf, mlps, inDataSet, outDataSet, numPoints;
//
// 	init_window {|parentIn|
// 		var hiddenArray;
//
// 		modelFolder = nil;
//
// 		parent = parentIn;
//
// 		currentPoint = 0;
// 		sliderCount = 0;
// 		numPoints = 0;
//
// 		onOff0 = 0;
// 		onOff1 = 0;
//
// 		inDataSet = FluidDataSet(group.server,("indata"++NN_Synth_DataSetID.next));
// 		outDataSet = FluidDataSet(group.server,("outdata"++NN_Synth_DataSetID.next));
//
// 		controlValsList = List.newClear(0);
// 		valsList = List.fill(sizeOfNN, {0});
// 		allValsList = List.fill(8, {List.fill(sizeOfNN+4, {0})});
//
// 		nnInputVals = (0!parent.inputControl.numActiveControls);
//
// 		whichModel = 0;
//
// 		//hard coding this to 3, which should be fine...will probably find this in 5 years and wonder wtf
// 		hiddenArray = (3, 3+(valsList.size/5)..valsList.size).floor.asInteger.copyRange(1,3);
//
// 		mlps = List.fill(8, {FluidMLPRegressor(group.server,hiddenArray.postln,2,1,0,1000,0.1,0,1,0)});
//
// 		this.createWindow;
// 	}
//
// 	clearTraining {
// 		modelFolder = nil;
// 		this.clearMLPs;
// 	}
//
// 	clearMLPs {
// 		"clear MLPs".postln;
// 		mlps = mlps.do{|item| item.clear};
// 		mlps.postln;
// 	}
//
// 	loadTraining {|modelFolderIn|
// 		"loading training".postln;
// 		modelFolder = modelFolderIn;
// 		loadedCount = 0;
//
// 		controlValsList = List.fill(File.readAllString(modelFolder++"/inDataSet0.json").parseYAML["cols"].asInteger.postln, {0});
//
// 		this.makeInOutBufs;
//
// 		//{
// 		numModels.do{|i|
// 			this.reloadNN(i);
// 			//0.2.wait;
// 		};
// 		//}.fork;
// 	}
//
// 	init2 {arg nameIn, parent, volBus, onOff0, onOff1, chanVolBus;
// 		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1, \chanVolBus, chanVolBus], group));
// 		this.init_window(parent);
// 	}
//
// 	createWindow {
// 		{
// 			nnVals.postln;
// 			nnVals.do{arg item, i;
// 				controls.add(QtEZSlider(item[0], item[1], {arg val;
// 					synths[0].set(item[0], val.value);
// 					{valsList.put(i, val.slider.value)}.defer;
// 				}, allValsList[0][i], true, \horz));
// 			};
//
// 			if(nnVals.size<41){
// 				win.layout = VLayout(
// 					*controls.collect({arg item, i;
// 					HLayout(item)})
// 				);
// 			}{
// 				win.layout = HLayout(
// 					VLayout(
// 						*controls.copyRange(0,39).collect({arg item, i;
// 						HLayout(item)})
// 					),
// 					VLayout(
// 						*controls.copyRange(40,controls.size-1).collect({arg item, i;
// 						HLayout(item)})
// 					)
// 				)
// 			};
// 			win.layout.spacing_(0).margins_(0!4);
//
// 			win.visible_(false);
// 		}.defer;
// 	}
//
// 	changeModel {|i|
// 		"change model".postln;
// 		allValsList.put(whichModel, valsList.addAll(controlValsList));
// 		whichModel = i;
// 		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
// 		this.setSlidersAndSynth(valsList, true);
// 	}
//
// 	configure {//"configure".postln;
// 		if(parent.predictOnOff==1){
// 			//[mlpInBuf,mlpOutBuf].postln;
// 			//whichModel.postln;
// 			mlps[whichModel].predictPoint(mlpInBuf,mlpOutBuf,{
// 				mlpOutBuf.loadToFloatArray(action:{|array|
// 					array = array.asArray;
// 					this.setSlidersAndSynth(array);
// 					parent.setLemur(array);
// 				})
// 			});
// 		};
// 	}
//
// 	setNNInputVals {|vals|
// 		controlValsList = vals;
// 		mlpInBuf.setn(0, vals);
// 		this.configure;
// 	}
//
// 	setSynth {|argument, i, val01, val|
// 		valsList.put(i, val01);
// 		synths[0].set(argument, val);
// 	}
//
// 	setSlidersAndSynth2 {
// 		//valsList.postln;
// 		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
// 		this.setSlidersAndSynth(valsList);
// 	}
//
// 	setGUISlider {|i, val|
// 		if(i<sizeOfNN){controls[i].valueAction_(controls[i].controlSpec.map(val))}
// 	}
//
// 	setSlidersAndSynth {|vals, isPoint=false|
// 		//vals.postln;
// 		sliderCount = sliderCount+1;
//
// 		vals.do{|item, i|
// 			if(i<sizeOfNN,{
// 				if((parent.hasControl[i]==0)&&(nnVals[i][0].asString!="nil"))
// 				{
// 					this.setSynth(nnVals[i][0], i, vals[i], controls[i].controlSpec.map(item));
// 				};
// 			});
// 		};
// 		if(parent.updateSliders==true){
// 			if((sliderCount%50==0)||isPoint){
// 				vals.do{|item, i|
// 					if(parent.hasControl[i]==0){
// 						{controls[i].value_(controls[i].controlSpec.map(item))}.defer;
// 					}
// 				}
// 			}
// 		};
// 	}
//
// 	makeInOutBufs {
// 		"makeInOutBufs".postln;
// 		if(mlpInBuf != nil){mlpInBuf.free};
// 		if(mlpOutBuf != nil){mlpOutBuf.free};
//
// 		controlValsList.postln;
//
// 		mlpInBuf = Buffer.alloc(group.server,controlValsList.size);
// 		mlpOutBuf = Buffer.alloc(group.server,valsList.size);
// 	}
//
// 	reloadNN {arg reloadWhich;
// 		var fileInfo, hiddenArray, modelFile;
// 		if(reloadWhich==nil){reloadWhich = whichModel};
// 		reloadWhich.postln;
// 		"clear".postln;
// 		mlps.postln;
// 		mlps[reloadWhich].clear({
// 			"reload ".post;
// 			modelFile = modelFolder++"/"++"modelFile"++reloadWhich++".json";
// 			try{
// 				File.readAllString(modelFile).parseYAML;
// 				mlps[reloadWhich].read(modelFile, {("loaded"+modelFile).postln});
// 			}{"no json file".postln}
// 		})
// 	}
//
// 	trainNN {
// 		var saveFile, modelFile, hiddenArray, controlValsDict, valDict;
//
// 		this.makeInOutBufs;
//
// 		mlps[whichModel].fit(inDataSet,outDataSet,{|x|
// 			x.postln;
// 		});
//
// 		mlps[whichModel].write(modelFolder++"/"++"modelFile"++whichModel++".json");
// 		inDataSet.write(modelFolder++"/"++"inDataSet"++whichModel++".json");
// 		outDataSet.write(modelFolder++"/"++"outDataSet"++whichModel++".json");
// 	}
//
// 	loadPoints {
// 		var tempA, tempB, fileInfo;
//
// 		if(modelFolder!=nil){
// 			inDataSet.read(modelFolder++"/"++"inDataSet"++whichModel++".json", {
// 				outDataSet.read(modelFolder++"/"++"outDataSet"++whichModel++".json", {
// 					var buf = Buffer(group.server);
// 					var buf2 = Buffer(group.server);
//
// 					numPoints = outDataSet.size.postln;
//
// 					inDataSet.getPoint(currentPoint, buf, {|buffy| buffy.loadToFloatArray(action:{|array|
// 						controlValsList = array.asList;
// 						parent.setControlPointsNoAction(controlValsList);
// 					})});
//
// 					outDataSet.getPoint(currentPoint, buf2, {|buffy| buffy.loadToFloatArray(action:{|array|
// 						valsList = array.asList;
// 						this.setSlidersAndSynth(valsList, true);
// 						parent.setLemur(valsList);
// 					})});
// 				})
// 			})
//
// 		}{"no model".postln}
// 	}
//
// 	newPointsList {
// 		inDataSet.clear;
// 		outDataSet.clear;
//
// 		currentPoint = 0;
// 		numPoints = 0;
// 	}
//
// 	addPoint {
// 		[valsList.size, controlValsList.size].postln;
// 		Buffer.loadCollection(group.server, controlValsList.asArray, 1, {|array|
// 			inDataSet.addPoint(numPoints.asString, array);
// 			inDataSet.print;
// 		});
// 		Buffer.loadCollection(group.server, valsList.asArray, 1, {|array|
// 			outDataSet.addPoint(numPoints.asString, array);
// 			outDataSet.print;
// 		});
// 		numPoints = numPoints+1;
//
// 	}
//
// 	copyPoint {
// 		"copy point".postln;
// 		^valsList.copyRange(0,sizeOfNN-1).postln
// 	}
//
// 	pastePoint {|point|
// 		valsList = point;
// 		this.addPoint;
// 		//trainingList.add(point.addAll(controlValsList));
// 	}
//
// 	removePoint {
// 		//this needs to be some hairy shit
// 		"not working".postln;
// 	}
//
// 	nextPoint {
// 		var point0 = Buffer.alloc(group.server);
// 		var point1 = Buffer.alloc(group.server);
//
// 		if(numPoints>0){
// 			currentPoint = (currentPoint+1).wrap(0, numPoints);
//
// 			inDataSet.getPoint(currentPoint.asSymbol, point0, {
// 				point0.loadToFloatArray(action:{|array|
//
// 					controlValsList = array;
// 					controlValsList.postln;
//
// 					parent.setControlPointsNoAction(controlValsList);
// 					point0.free;
// 				})
// 			});
//
// 			outDataSet.getPoint(currentPoint.asSymbol, point1, {
// 				point1.loadToFloatArray(action:{|array|
// 					array.postln;
// 					valsList = array;
//
// 					//valsList = valsList.addAll((sizeOfNN-valsList.size)!0); //fill with zeroes if it isn't large enough
//
// 					this.setSlidersAndSynth(valsList, true);
// 					parent.setLemur(valsList);
//
// 					valsList.postln;
// 					point1.free;
// 				})
// 			});
//
// 		}
// 	}
//
// 	reloadSynth {
// 		"should be overwritten by the synth".postln;
// 	}
//
// 	killMeSpecial {
// 		this.killThePythons;
// 	}
//
// 	getLabels {
// 		^nnVals.collect{arg item; item[0].asString};
// 	}
//
// 	//loadExtra {win.visible = false}
// }
//
//
