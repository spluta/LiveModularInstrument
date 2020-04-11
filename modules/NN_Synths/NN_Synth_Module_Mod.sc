NN_Synth_ID {
	classvar <id=5000;
	*initClass { id = 5000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

NN_Synth_Mod : Module_Mod {
	classvar <>pythonPath = "/usr/local/Cellar/python/3.7.5/Frameworks/Python.framework/Versions/3.7/bin/python3.7";
	var pythonFilesPath;

	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, nnInputVals, valList, allValsList, nnVals, trainingList, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC, <>modelFolder, <>onOff0, <>onOff1;

	init_window {|parentIn|

		pythonFile = "NN_Synth_1_Predict.py";

		pythonFilesPath = PathName(path);

		pythonFilesPath = pythonFilesPath.fullPath.copyRange(0, pythonFilesPath.colonIndices[pythonFilesPath.colonIndices.size-2]);

		modelFolder = nil;

		parent = parentIn;

		sliderCount = 0;

		onOff0 = 0;
		onOff1 = 0;

		receivePort = NN_Synth_ID.next;

		ports = List.fill(numModels, {|i| NN_Synth_ID.next});

		pythonAddrs = List.fill(numModels, {|i| NetAddr("127.0.0.1", ports[i])});

		trainingList = List.newClear(0);
		controlValsList = List.newClear(0);
		valList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(8, {List.fill(sizeOfNN+4, {0})});

		nnInputVals = (0!parent.inputControl.numActiveControls).postln;

		//set the system to receive messages from each python instance
		OSCFunc.new({arg ...msg;
			this.setSlidersAndSynth(msg[0].copyRange(2,sizeOfNN+1));
			parent.setLemur(msg[0].copyRange(2,sizeOfNN+1));
		}, '/nnOutputs', nil, receivePort);

		pythonAddrs.do{arg item, i;
			OSCFunc.new({arg ...msg;
				allValsList.put(whichModel, msg[0].copyRange(1,msg[0].size-1).addAll([0,0,0,0]));
			}, '/prime', nil, receivePort);
		};

		//prime the pump
		//numModels.do{arg modelNum;
		loadedCount = 0;
		loadedOSC = OSCFunc({arg ...msg;
			loadedCount = loadedCount+1;
			if(loadedCount == numModels){
				{
					"loaded".postln;
					0.11.wait;
					numModels.do{|modelNum|
						whichModel = modelNum;
						5.do{|i|
							var temp;
							temp = Array.fill(parent.inputControl.numActiveControls, {1.0.rand});
							controlValsList = temp.asList;
							pythonAddrs[modelNum].sendMsg(*['/predict'].addAll(temp));
							0.01.wait
						};
						pythonAddrs[modelNum].sendMsg(*['/prime'].addAll(nnInputVals));
						0.1.wait;
					};
					whichModel = 0;
					//loadedOSC.free;
				}.fork;
			}
		}, '/loaded', nil, receivePort);
		whichModel = 0;

		this.createWindow;
	}

	clearTraining {
		modelFolder = nil;
		this.killThePythons;
	}

	killThePythons {
		"kill the pythons".postln;
		pythonAddrs.do{|item|item.sendMsg('/close')}
	}

	loadTraining {|modelFolderIn|
		{
			modelFolder = modelFolderIn;
			this.killThePythons;
			1.wait;
			loadedCount = 0;
			numModels.do{|i|
				(pythonPath+pythonFilesPath.quote++pythonFile+"--path"+(modelFolder++"/").quote+"--port"+ports[i].asString
					+"--sendPort"+receivePort.asString+"--num"+i.asString+"&").postln.unixCmd;
				ServerQuit.add({NetAddr("127.0.0.1", ports[i]).sendMsg('/close')})
			};
		}.fork;
	}

	init2 {arg nameIn, parent, volBus, onOff0, onOff1;
		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1], group));
		this.init_window(parent);
	}

	createWindow {
		nnVals.do{arg item, i;
			controls.add(QtEZSlider(item[0], item[1], {arg val;
				synths[0].set(item[0], val.value);
				{valList.put(i, val.slider.value)}.defer;
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

	}

	changeModel {|i|
		allValsList.put(whichModel, valList.addAll(controlValsList));
		whichModel = i;
		valList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valList, true);
		//parent.setMultiBalls(allValsList[whichModel].copyRange(sizeOfNN,sizeOfNN+4));
	}

	configure {
		if(parent.predictOnOff==1){pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(controlValsList))};
	}

	setNNInputVals {|vals|
		controlValsList = vals;
		this.configure;
	}

	setSynth {|argument, i, val01, val|
		valList.put(i, val01);
		synths[0].set(argument, val);
	}

	setSlidersAndSynth2 {
		if(trainingList.size>0){
			this.setSlidersAndSynth(trainingList[currentPoint].copyRange(0,sizeOfNN-1));
		};
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

	reloadNN {
		{
			"close".postln;
			pythonAddrs[whichModel].sendMsg('/close');
			1.wait;
			"reload ".post;

			(pythonPath+pythonFilesPath.quote++pythonFile+"--path"+(modelFolder++"/").quote+"--port"+(ports[whichModel]).asString
				+"--sendPort"+receivePort.asString+"--num"+whichModel.asString+"&").unixCmd;
			OSCFunc.new({arg ...msg;
				"reloaded".postln;
				{
					10.do{
						pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(Array.fill(parent.inputControl.numActiveControls, {1.0.rand})));
						0.01.wait;
					}
				}.fork
			}, '/loaded', nil, receivePort).oneShot;
		}.fork;
	}

	trainNN {
		var saveFile, modelFile;
		{
			saveFile = (modelFolder++"/"++"trainingFile"++whichModel++".csv");
			saveFile = File(saveFile, "w");
			[[valList.size, controlValsList.size]].addAll(trainingList).do{arg item;
				item.do{|item2, i|
					if(i!=0){saveFile.write(", ")};
					item2 = item2.asString;
					saveFile.write(item2);
				};
				saveFile.write("\n");
			};
			saveFile.close;
			1.wait;

			saveFile = modelFolder++"/"++"trainingFile"++whichModel++".csv";
			modelFile = modelFolder++"/"++"modelFile"++whichModel++".h5";
			(pythonPath+pythonFilesPath.quote++"NN_Synth_1_Save.py"+"--numbersFile"+saveFile.quote+"--modelFile"+modelFile.quote+"--sendPort"+receivePort.asString+"&").postln.unixCmd;
		}.fork;

		OSCFunc({
			this.reloadNN;
			"trained".postln;
		}, '/trained', nil, receivePort).oneShot;
	}

	loadPoints {
		var tempA, tempB, fileInfo;

		if(modelFolder!=nil){

			trainingList = CSVFileReader.read(modelFolder++"/"++"trainingFile"++whichModel++".csv");
			fileInfo = trainingList[0].collect{|item| item.asInteger};
			trainingList = trainingList.copyRange(1, trainingList.size-1).collect({arg item; item.collect({arg item2; item2.asFloat})}).asList;
			//trainingList.addAll(sizeOfNN-trainingList.size!0); //not sure what this would do
			trainingList.size;
			currentPoint = 0;

			valList = trainingList[currentPoint].copyRange(0,fileInfo[0]-1);
			valList.postln;

			this.setSlidersAndSynth(valList, true);
			parent.setLemur(valList);
			controlValsList = trainingList[currentPoint].copyRange(fileInfo[0], fileInfo[0]+fileInfo[1]-1);
			parent.setControlPointsNoAction(controlValsList);
		}{"no model".postln}
	}

	newPointsList {
		trainingList = List.newClear(0);
		currentPoint = 0;
	}

	addPoint {
		[valList.size, controlValsList.size].postln;
		trainingList.add(valList.copyRange(0,sizeOfNN-1).addAll(controlValsList));

	}

	copyPoint {
		^valList.copyRange(0,sizeOfNN-1)
	}

	pastePoint {|point|
		trainingList.add(point.addAll(controlValsList));
	}

	removePoint {
		if(trainingList.size>0){
			trainingList.removeAt(currentPoint);
			currentPoint = max(currentPoint-1, 0);
		};
	}

	nextPoint {
		if(trainingList.size>0){
			currentPoint = (currentPoint+1).wrap(0, trainingList.size-1);

			valList = trainingList[currentPoint].copyRange(0,trainingList[currentPoint].size-(controlValsList.size+1));
			valList = valList.addAll((sizeOfNN-valList.size)!0); //fill with zeroes if it isn't large enough

			this.setSlidersAndSynth(valList, true);
			parent.setLemur(valList);

			controlValsList = trainingList[currentPoint].copyRange(valList.size,trainingList[currentPoint].size-1);
			controlValsList.postln;
			valList.postln;
			parent.setControlPointsNoAction(controlValsList);
		}
	}

	reloadSynth {
		"should be overwritten by the synth".postln;
	}

	killMeSpecial {
		this.killThePythons;
	}

	getLabels {
		^nnVals.collect{arg item; item[0].asString};
	}

	//loadExtra {win.visible = false}
}

