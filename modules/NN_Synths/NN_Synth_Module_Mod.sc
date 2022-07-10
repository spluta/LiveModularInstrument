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
	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>controlValsList, nnInputVals, valsList, allValsList, nnVals, parent, currentPoint, receivePort, loadedCount, loadedOSC, <>modelFolder, <>onOff0, <>onOff1, mlpInBuf, mlpOutBuf, mlps, inDataSet, outDataSet, inBuf, outBuf, copyInBuf, copyOutBuf, numPoints, keys, readyToPredict=true, synthArgs, setSlidersSpeedLimit, setLemurSpeedLimit, analysisGroup, mlpInBusSpeedLimit, mlpInBusses, mlpOutBusses, <>mlpSynths, mlpGroup, controlGroup, <>controlSwitchSynths, controlBusses, updateSlidersRout, <>isCurrentUpdateLemur=0;

	init_window {|parentIn|
		var hiddenArray;

		modelFolder = nil;

		parent = parentIn;

		currentPoint = 0;
		numPoints = 0;
		keys = Set[];

		onOff0 = 0;
		onOff1 = 0;

		inDataSet = FluidDataSet(group.server);
		outDataSet = FluidDataSet(group.server);
		inBuf = Buffer(group.server);
		outBuf = Buffer(group.server);

		copyInBuf = Buffer(group.server);
		copyOutBuf = Buffer(group.server);

		controlValsList = List.newClear(0);
		valsList = List.fill(sizeOfNN, {1.0.rand});
		allValsList = List.fill(8, {|i| List.fill(sizeOfNN+4, {1.0.rand})});

		nnInputVals = (0!parent.inputControl.numActiveControls);

		if(sizeOfNN>0){
			hiddenArray = (3, 3+(valsList.size/5)..valsList.size).floor.asInteger.copyRange(1,3);

			mlps = List.fill(8, {FluidMLPRegressor(group.server,hiddenArray,2,1,0,-1,1000,0.1,0.1,1,0)});
		}{
			mlps = List.fill(8, {FluidMLPRegressor(group.server,[3,3],2,1,0,-1,1000,0.1,0.1,1,0)});
		};

		setLemurSpeedLimit = SpeedLimit({|array| parent.setLemur(array)}, 0.05);
		setSlidersSpeedLimit = SpeedLimit({|array| this.setSliders(array)}, 0.2);
		mlpInBusSpeedLimit = SpeedLimit({|array| this.setMLPInBusses(array)}, 0.01);

		updateSlidersRout = Routine({inf.do{
			if(isCurrentUpdateLemur==1){
			if(parent.predictOnOff==1){
				mlpOutBusses[whichModel].getn(sizeOfNN, {|array|
					valsList = array;
					if(parent.updateSliders==true){
						setSlidersSpeedLimit.value(array);
						setLemurSpeedLimit.value(array);
					};
				});
			}{
				controlBusses[whichModel].getn(sizeOfNN, {|array|
					valsList = array;

					setLemurSpeedLimit.value(array);
					if(parent.updateSliders==true){
						setSlidersSpeedLimit.value(array);
					};
				});
			}
			};
			0.05.wait;
		}}).play;

		this.initWindow2;

		this.createWindow;
	}

	initWindow2 {}

	clearTraining {
		modelFolder = nil;
		this.clearMLPs;
	}

	clearMLPs {
		mlps = mlps.do{|item| item.clear};
	}

	loadTraining {|modelFolderIn|
		modelFolder = modelFolderIn;
		loadedCount = 0;

		controlValsList = List.fill(File.readAllString(modelFolder++"/inDataSet0.json").parseYAML["cols"].asInteger, {0});
		valsList = List.fill(File.readAllString(modelFolder++"/outDataSet0.json").parseYAML["cols"].asInteger, {0});

		this.makeInOutBufs;

		numModels.do{|i|
			this.reloadNN(i);
		};
	}

	init2 {arg nameIn, parent, volBus, onOff0, onOff1, chanVolBus;
		analysisGroup = Group.tail(group);
		mlpGroup = Group.tail(group);
		controlGroup = Group.tail(group);
		synthGroup = Group.tail(group);
		bigSynthGroup = Group.tail(group);  //this is only in the sampler...not sure why

		mlpSynths = List.newClear(8);
		controlSwitchSynths = List.newClear(8);

		//hardcoded to 10 and 80, but this could easily change
		mlpInBusses = List.fill(8, {Bus.control(group.server, 10).set({1.0.rand}!10)});
		mlpOutBusses = List.fill(8, {Bus.control(group.server, 80).set({1.0.rand}!80)});
		controlBusses = List.fill(8, {Bus.control(group.server, 80).set({1.0.rand}!80)});

		whichModel = 0;

		synths.add(Synth(nameIn, [\outBus, outBus, \volBus, volBus.index, \onOff0, onOff0-1, \onOff1, onOff1-1, \chanVolBus, chanVolBus, \dataInBus, mlpOutBusses[whichModel]], synthGroup));
		this.init_window(parent);
	}

	reloadSynth {
		{
			synths.do{|item| item.free};
			group.server.sync;
			synths.put(0, Synth(synthArgs[0], [\outBus, outBus, \volBus, synthArgs[2].index, \onOff0, synthArgs[3]-1, \onOff1, synthArgs[4]-1, \chanVolBus, synthArgs[5], \dataInBus, mlpOutBusses[whichModel]], synthGroup));
		}.fork;
	}

	createWindow {
		{
			nnVals.do{arg item, i;
				controls.add(QtEZSlider(item[0], ControlSpec(), {arg val;
					controlBusses[whichModel].setAt(i, [val.value]);

					valsList.put(i, val.value);
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
		allValsList.put(whichModel, valsList.addAll(controlValsList));
		mlpSynths[whichModel].set(\mlpOnOff, 0);
		mlpSynths[whichModel].run(false);
		whichModel = i;
		mlpSynths[whichModel].set(\mlpOnOff, 1);
		mlpSynths[whichModel].run(true);
		synths[0].set(\dataInBus, mlpOutBusses[whichModel]);
		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valsList);
	}

	trigger {|num, val|

		if(num==0){
			synths[0].set(\onOff0, val);
			onOff0 = val;
		}{
			synths[0].set(\onOff1, val);
			onOff1 = val;
		};
	}

	setMLPInBusses {|vals|
		mlpInBusses[whichModel].setn(vals);
	}

	setNNInputVals {|vals|
		controlValsList = vals;
		//mlpInBuf.setn(0, vals);
		//this.configure;
		if(parent.predictOnOff==1){
			mlpInBusSpeedLimit.value(vals);
		}
	}

	setSynth {|i, val|


	}

	setSlidersAndSynth2 {
		valsList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
		this.setSlidersAndSynth(valsList);
	}

	setGUISlider {|i, val|
		if(i<sizeOfNN){controls[i].valueAction_(val)}
	}

	setSliders {|vals|
		vals.do{|item, i|
			if(parent.hasControl[i]==0){
				{controls[i].value_(item)}.defer;
			}
		}
	}

	setSlidersAndSynth {|vals|
		vals.do{|item, i|
			if(i<sizeOfNN,{
				if((parent.hasControl[i]==0))
				{
					//this.setSynth(i, vals[i]);
					valsList.put(i, item);
					controlBusses[whichModel].setAt(i, item);
				};
			});
		};

		if(parent.updateSliders==true){
			setSlidersSpeedLimit.value(vals);
		};
	}

	makeMLPSynth {|makeWhich|
		^{
			var output, input = In.kr(mlpInBusses[makeWhich], controlValsList.size);
			var trig = Impulse.kr(50)*\mlpOnOff.kr(0);
			var inputPoint = (0!controlValsList.size).as(LocalBuf);
			var outputPoint = (0!valsList.size).as(LocalBuf);


			input.collect{|p, i| BufWr.kr([p],inputPoint,i)};
			mlps[makeWhich].kr(trig,inputPoint,outputPoint,0,-1);
			output = valsList.size.collect{|i| BufRd.kr(1,outputPoint,i)};


			EnvGen.kr(Env.asr, \gate.kr(1), doneAction:2);
			Out.kr(mlpOutBusses[makeWhich], output);
		}.play(mlpGroup);
	}

	makeControlSwitcher{|makeWhich|
		^{
			var switch, mlpIn = In.kr(mlpOutBusses[makeWhich], valsList.size);
			var controlIn = In.kr(controlBusses[makeWhich], valsList.size);
			\switches.kr({1}!valsList.size, 0);
			\switches2.kr({0}!valsList.size, 0);

			switch = \switches.kr-\switches2.kr;

			EnvGen.kr(Env.asr, \gate.kr(1), doneAction:2);
			valsList.size.do{|i|
				ReplaceOut.kr(mlpOutBusses[makeWhich].index+i, Select.kr(switch[i].clip(0,1), [controlIn[i], mlpIn[i]]).zap);//zap any NaNs that come out of the NN
			};
		}.play(controlGroup);
	}

	switchPredict {|predictVal|
		if(predictVal==0){
			mlpOutBusses[whichModel].getn(valsList.size, {|array|
				//controlBusses[whichModel].set(array);
				array.do{|val, i| controls[i].valueAction_(val)};
				controlSwitchSynths.do{|cSS| cSS.set(\switches, predictVal!valsList.size)}
			});
		}{
			/*controlBusses[whichModel].getn(valsList.size, {|array|
				mlpOutBusses[whichModel].set(array);*/
				controlSwitchSynths.do{|cSS| cSS.set(\switches, predictVal!valsList.size)}
			//});
		}
	}

	reloadNN {arg reloadWhich;
		var fileInfo, hiddenArray, modelFile;
		if(reloadWhich==nil){reloadWhich = whichModel};
		mlps[reloadWhich].clear({
			if(sizeOfNN>0){
				hiddenArray = (3, 3+(valsList.size/5)..valsList.size).floor.asInteger.copyRange(1,3);

				mlps[reloadWhich].hidden = hiddenArray;
			}{
				mlps[reloadWhich].hidden = [3,3];
			};

			modelFile = modelFolder++"/"++"modelFile"++reloadWhich++".json";
			try{
				File.readAllString(modelFile).parseYAML;
				mlps[reloadWhich].read(modelFile, {
					{
						if(mlpSynths[reloadWhich]!=nil){mlpSynths[reloadWhich].set(\gate, 0)};
						if(controlSwitchSynths[reloadWhich]!=nil){controlSwitchSynths[reloadWhich].set(\gate, 0)};
						group.server.sync;
						mlpSynths.put(reloadWhich, this.makeMLPSynth(reloadWhich));
						controlSwitchSynths.put(reloadWhich, this.makeControlSwitcher(reloadWhich));
						if(reloadWhich==whichModel){mlpSynths[whichModel].set(\mlpOnOff, 1, \isCurrent, 1)};
					}.fork
				});
			}{"no json file".postln}
		})
	}

	makeInOutBufs {
		var mlpSynth;

		if(mlpInBuf != nil){mlpInBuf.free};
		if(mlpOutBuf != nil){mlpOutBuf.free};

		mlpInBuf = Buffer.alloc(group.server,controlValsList.size);
		mlpOutBuf = Buffer.alloc(group.server,valsList.size);
	}

	trainNN {
		var saveFile, modelFile, hiddenArray, controlValsDict, valDict;

		this.makeInOutBufs;



		mlps[whichModel].fit(inDataSet,outDataSet,{|x|
			"trainy trainy trainy".postln;
			(modelFolder++"/"++"modelFile"++whichModel++".json").postln;
			mlps[whichModel].write(modelFolder++"/"++"modelFile"++whichModel++".json");
			inDataSet.write(modelFolder++"/"++"inDataSet"++whichModel++".json");
			outDataSet.write(modelFolder++"/"++"outDataSet"++whichModel++".json");

			this.reloadNN(whichModel);
		});


	}

	loadPoints {
		var tempA, tempB, fileInfo;
		currentPoint = 0;

		if(modelFolder!=nil){
			inDataSet.read(modelFolder++"/"++"inDataSet"++whichModel++".json", {
				outDataSet.read(modelFolder++"/"++"outDataSet"++whichModel++".json", {

					inDataSet.print;
					outDataSet.print;

					outDataSet.dump({|vals|
						var max=0, newPoint;

						keys = vals["data"].keys.asList;

						inDataSet.getPoint(keys[currentPoint], inBuf, {inBuf.loadToFloatArray(action:{|array|

							controlValsList = array.asList;
							{parent.setControlPointsNoAction(controlValsList)}.defer;
						})});

						outDataSet.getPoint(keys[currentPoint], outBuf, {outBuf.loadToFloatArray(action:{|array|
							controlBusses[whichModel].setn(array);
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
		outDataSet.dump({|vals|
			var max=0, newPoint;

			if(vals["data"]!=nil){
				vals["data"].keys.do{|item| if(item.asInteger>max){max=item}};
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
			vals["data"].keys.do{|item| if(item.asInteger>max){max=item}};
			newPoint = max+1;
			inDataSet.addPoint(newPoint.asString, copyInBuf);
			outDataSet.addPoint(newPoint.asString, copyOutBuf);
		});
	}

	removePoint {
		var cp = keys[currentPoint];

		inDataSet.print;
		outDataSet.print;

		inDataSet.deletePoint(cp, nil, {
			inDataSet.print;
			outDataSet.deletePoint(cp, nil, {
				outDataSet.print;
				keys.removeAt(currentPoint);
				currentPoint = currentPoint.wrap(0, keys.size-1);
		})});
	}

	nextPoint {
		var key;

		if(keys.size>0){
			currentPoint = (currentPoint+1).wrap(0, keys.size-1);
			key = keys[currentPoint];

			inDataSet.getPoint(key, inBuf, {
				inBuf.loadToFloatArray(action:{|array|

					controlValsList = array;
					{parent.setControlPointsNoAction(controlValsList)}.defer;
				})
			});

			outDataSet.getPoint(key, outBuf, {
				outBuf.loadToFloatArray(action:{|array|
					"getPoint".postln;
					array.postln;
					//valsList = array;

					//array.do{|val, i| valsList.put(i, val)};
					controlBusses[whichModel].setn(array);
					//{parent.setLemur(valsList)}.defer;
				})
			});

		}{"no points loaded".postln}
	}


	killMeSpecial {
		{
			mlpSynths.do{|item| item.set(\gate, 0)};
			controlSwitchSynths.do{|item| item.set(\gate, 0)};

			updateSlidersRout.stop;
			group.server.sync;
			1.wait;

			mlpInBuf.free;
			mlpOutBuf.free;
			inDataSet.free;
			outDataSet.free;

			mlpInBusses.do{|item| item.free};
			mlpOutBusses.do{|item| item.free};
			controlBusses.do{|item| item.free};

			mlps.do{|item| item.free};
		}.fork;

	}

	getLabels {
		^nnVals.collect{arg item; item[0].asString};
	}

	//loadExtra {win.visible = false}
}


