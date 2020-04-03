NN_Synth_ID {
	classvar <id=5000;
	*initClass { id = 5000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

NN_Synth_Mod : Module_Mod {
	classvar <>pythonPath = "/usr/local/Cellar/python/3.7.5/Frameworks/Python.framework/Versions/3.7/bin/python3.7";
	var pythonFilesPath;

	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, xyz, valList, allValsList, nnVals, trainingList, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC;

	init_window {|parentIn|

		pythonFile = "NN_Synth_1_Predict.py";

		pythonFilesPath = PathName(path);

		pythonFilesPath = pythonFilesPath.fullPath.copyRange(0, pythonFilesPath.colonIndices[pythonFilesPath.colonIndices.size-2]);

		parent = parentIn;

		sliderCount = 0;

		receivePort = NN_Synth_ID.next;

		ports = List.fill(numModels, {|i| NN_Synth_ID.next});

		pythonAddrs = List.fill(numModels, {|i| NetAddr("127.0.0.1", ports[i])});

		trainingList = List.newClear(0);
		controlValsList = List[];
		valList = List.fill(sizeOfNN, {0});
		//this.setAllVals;
		allValsList = List.fill(8, {List.fill(sizeOfNN+4, {0})});

		xyz = List.fill(2, {List.fill(3, {0})});

		numModels.do{|i|
			(pythonPath+pythonFilesPath.quote++pythonFile+"--path"+path.quote+"--port"+ports[i].asString
				+"--sendPort"+receivePort.asString+"--num"+i.asString+"&").unixCmd;
			ServerQuit.add({NetAddr("127.0.0.1", ports[i]).sendMsg('/close')})
		};

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
							temp = Array.fill(4, {1.0.rand});
							controlValsList = temp.asList;
							pythonAddrs[modelNum].sendMsg(*['/predict'].addAll(temp));
							0.01.wait
						};
						pythonAddrs[modelNum].sendMsg(*['/prime'].addAll([0,0,0,0]));
						0.1.wait;
					};
					whichModel = 0;
					loadedOSC.free;
				}.fork;
			}
		}, '/loaded', nil, receivePort);
		whichModel = 0;

		this.createWindow;
	}

	init2 {arg nameIn, parent, otherValsBusses, onOffBus, envOnOffBus;
		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, otherValsBusses[0].index, \envRiseBus, otherValsBusses[1].index, \envFallBus, otherValsBusses[2].index, \onOffBus, onOffBus, \envOnOffBus, envOnOffBus], group));
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
		allValsList.put(whichModel, valList.addAll(controlValsList.flatten));
		whichModel = i;
		valList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valList, true);
		//parent.setMultiBalls(allValsList[whichModel].copyRange(sizeOfNN,sizeOfNN+4));
	}

	doTheZ {|i, val|
		xyz[i].put(2, val.value);
		switch(parent.envChoice,
			0, {parent.onOffBus.set(1); parent.envOnOffBus.set(0)},
			1, {parent.onOffBus.set(xyz[[xyz[0][2],xyz[1][2]].maxIndex][2]); parent.envOnOffBus.set(0)},
			2, {parent.onOffBus.set(xyz[[xyz[0][2],xyz[1][2]].maxIndex][2]); parent.envOnOffBus.set(1)}
		);
	}

	setEnv {|num|
		switch(num,
			0, {parent.onOffBus.set(1); parent.envOnOffBus.set(0)},
			1, {parent.onOffBus.set(xyz[[xyz[0][2],xyz[1][2]].maxIndex][2]); parent.envOnOffBus.set(0)},
			2, {parent.onOffBus.set(xyz[[xyz[0][2],xyz[1][2]].maxIndex][2]); parent.envOnOffBus.set(1)}
		)
	}

	configure {

		xyz.do{|item,i| controlValsList.put(i, item.copyRange(0,1))};

		if(parent.predictOnOff==1){pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(controlValsList.flatten))};
	}

	setXYZ {|i, vals|
		//xyz[i].put(0, vals[0]);
		//xyz[i].put(1, vals[1]);
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

			(pythonPath+pythonFilesPath.quote++pythonFile+"--path"+path.quote+"--port"+(ports[whichModel]).asString
				+"--sendPort"+receivePort.asString+"--num"+whichModel.asString+"&").unixCmd;
			OSCFunc.new({arg ...msg;
				"reloaded".postln;
				{
					10.do{

						pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(Array.fill(4, {1.0.rand})));
						0.01.wait;
					}
				}.fork
			}, '/loaded', nil, receivePort).oneShot;
		}.fork;
	}

	trainNN {
		var saveFile, modelFile;
		{
			saveFile = (path++"trainingFile"++whichModel++".csv");
			saveFile = File(saveFile, "w");
			trainingList.do{arg item;
				item.do{|item2, i|
					if(i!=0){saveFile.write(", ")};
					item2 = item2.asString;
					saveFile.write(item2);

				};
				saveFile.write("\n");
			};
			saveFile.close;
			1.wait;

			saveFile = path++"trainingFile"++whichModel++".csv";
			modelFile = path++"modelFile"++whichModel++".h5";
			(pythonPath+pythonFilesPath.quote++"NN_Synth_1_Save.py"+"--numbersFile"+saveFile.quote+"--modelFile"+modelFile.quote+"--sendPort"+receivePort.asString+"--sizeOfNN"+sizeOfNN.asString+"--sizeOfControl"+4.asString+"&").unixCmd;
		}.fork;
		OSCFunc({
			this.reloadNN;
			"trained".postln;
		}, '/trained', nil, receivePort).oneShot;
	}

	loadPoints {
		var tempA, tempB;
		trainingList = CSVFileReader.read(path++"trainingFile"++whichModel++".csv");
		trainingList = trainingList.collect({arg item; item.collect({arg item2; item2.asFloat})}).asList;
		trainingList.addAll(sizeOfNN-trainingList.size!0);
		trainingList.size;
		currentPoint = 0;

		valList = trainingList[currentPoint].copyRange(0,trainingList[currentPoint].size-5);
		this.setSlidersAndSynth(valList, true);
		parent.setLemur(valList);
		parent.setMultiBallsNoAction(trainingList[currentPoint].copyRange(trainingList[currentPoint].size-4,trainingList[currentPoint].size-1));
	}

	newPointsList {
		trainingList = List.newClear(0);
		currentPoint = 0;
	}

	addPoint {
		trainingList.add(valList.copyRange(0,sizeOfNN-1).addAll(controlValsList.flatten));

	}

	copyPoint {
		^valList.copyRange(0,sizeOfNN-1)
	}

	pastePoint {|point|
		trainingList.add(point.addAll(controlValsList.flatten));
	}

	removePoint {
		if(trainingList.size>0){
			trainingList.removeAt(currentPoint);
			currentPoint = max(currentPoint-1, 0);
		};
	}

	nextPoint {
		currentPoint = (currentPoint+1).wrap(0, trainingList.size-1);

		valList = trainingList[currentPoint].copyRange(0,trainingList[currentPoint].size-5);
		valList = valList.addAll((sizeOfNN-valList.size)!0);
		this.setSlidersAndSynth(valList, true);
		parent.setLemur(valList);
		parent.setMultiBallsNoAction(trainingList[currentPoint].copyRange(trainingList[currentPoint].size-4,trainingList[currentPoint].size-1));
	}

	reloadSynth {
		"should be overwritten by the synth".postln;
	}

	getMultiBallPoints {
		^trainingList[currentPoint].copyRange(sizeOfNN,sizeOfNN+3);
	}

	killMeSpecial {
		"kill the pythons".postln;
		pythonAddrs.do{|i|i.sendMsg('/close')};
	}

	getLabels {
		^nnVals.collect{arg item; item[0].asString};
	}

	//loadExtra {win.visible = false}
}


