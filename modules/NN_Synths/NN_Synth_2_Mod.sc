/*NN_Synth_ID {
classvar <id=5000;
*initClass { id = 5000; }
*next  { ^id = id + 1; }
*path {this.filenameSymbol.postln}
}*/

NN_Synth_Mod : Module_Mod {
	classvar <>pythonPath = "/usr/local/Cellar/python/3.7.5/Frameworks/Python.framework/Versions/3.7/bin/python3.7";
	var pythonFilesPath;

	var numModels, <>sizeOfNN, ports, pythonAddrs, pythonFile, <>whichModel, <>multiBallList, xyz, valList, allValsList, nnVals, trainingList, parent, currentPoint, receivePort, sliderCount, loadedCount, loadedOSC;

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
		multiBallList = List[List[0,0,],List[0,0]];
		valList = List.fill(sizeOfNN, {0});
		this.setAllVals;

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
				//whichModel.postln;
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
							multiBallList = temp.asList.clump(2);
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
		[nameIn, parent, otherValsBusses, onOffBus, envOnOffBus].postln;
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

		win.layout = VLayout(
			*controls.collect({arg item, i;
				HLayout(item/*, assignButtons[i]*/)})
		);
		win.layout.spacing_(0).margins_(0!4);
	}

	changeModel {|i|
		allValsList.put(whichModel, valList.addAll(multiBallList.flatten));
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

		xyz.do{|item,i| multiBallList.put(i, item.copyRange(0,1))};

		if(parent.predictOnOff==1){pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(multiBallList.flatten))};
	}

	setXYZ {|i, vals|
		//[i,vals].postln;
		xyz[i].put(0, vals[0]);
		xyz[i].put(1, vals[1]);
		this.configure;
	}

	setSynth {|argument, i, val01, val|
		//"setSynth ".post; [argument, i, val01, val].postln;
		valList.put(i, val01);
		synths[0].set(argument, val);
	}

	setSlidersAndSynth2 {
		if(trainingList.size>0){
			this.setSlidersAndSynth(trainingList[currentPoint].copyRange(0,sizeOfNN-1));
		};
	}

	setGUISlider {|i, val|
		controls[i].valueAction_(controls[i].controlSpec.map(val));
	}

	setSlidersAndSynth {|vals, isPoint=false|
		//"setSlidersAndSynth".postln;
		//vals.postln;
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
			"reload ".post; whichModel.postln;
			//(pythonPath+path.quote++pythonFile+"--path"+path.quote+"--port"+ports[i].asString+"--sendPort"+receivePort.asString+"--num"+i.asString+"&").unixCmd;

			(pythonPath+pythonFilesPath.quote++pythonFile+"--path"+path.quote+"--port"+(ports[whichModel]).asString
				+"--sendPort"+receivePort.asString+"--num"+whichModel.asString+"&").postln.unixCmd;
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
			saveFile = (path++"trainingFile"++whichModel++".csv").postln;
			saveFile = File(saveFile, "w");
			//trainingList.postln;
			//if(trainingList.size<20,{trainingList.addAll(16-trainingList.size)});
			//trainingList.postln;
			trainingList.do{arg item;
				item.postln;
				(24-item.size).do{item = item.insert(sizeOfNN, 0)};
				item.postln;
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
			(pythonPath+pythonFilesPath.quote++"NN_Synth_1_Save.py"+"--numbersFile"+saveFile.quote+"--modelFile"+modelFile.quote+"--num"+whichModel.asString+"--sendPort"+receivePort.asString+"&").unixCmd.postln;
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
		trainingList.postln;
		currentPoint = 0;

		valList = trainingList[currentPoint].copyRange(0,trainingList[currentPoint].size-5);
		this.setSlidersAndSynth(valList, true);
		parent.setLemur(valList);
		parent.setMultiBallsNoAction(trainingList[currentPoint].copyRange(trainingList[currentPoint].size-4,trainingList[currentPoint].size-1));
	}

	addPoint {
		trainingList.add(valList.copyRange(0,sizeOfNN-1).addAll(multiBallList.flatten));
	}

	copyPoint {
		^valList.copyRange(0,sizeOfNN-1)
	}

	pastePoint {|point|
		trainingList.add(point.addAll(multiBallList.flatten));
	}

	removePoint {
		if(trainingList.size>0){
			trainingList.removeAt(currentPoint);
			currentPoint = max(currentPoint-1, 0);
		};
	}

	nextPoint {
		currentPoint = (currentPoint+1).wrap(0, trainingList.size-1);

		trainingList[currentPoint].postln;
		valList = trainingList[currentPoint].copyRange(0,trainingList[currentPoint].size-5);
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
}

NN_Synths_Mod : Module_Mod {

	var <>hasControl, <>predictOnOff, zValsOnOff, currentPoint, otherVals, numModels;
	var predictOnOff, currentPoint, envOnOff, <>updateSliders, nn_synths, currentSynth, nn_synthChoices, showHideButtons, nn_synths, currentSynth, otherValsBusses, loadedSynths, sliderControl, sliderControlButton, <>envChoice, <>onOffBus, <>envOnOffBus, copiedPoint, twoDCounter;

	init {

		this.makeWindow("NN_Synths");

		this.initControlsAndSynths(4+1+8+6+1+6+4+2);

		dontLoadControls = (0..29);

		updateSliders = false;

		loadedSynths = List.fill(4,{0});

		hasControl = Array.fill(30, {0});  //setting max size of NN to 30
		predictOnOff = 0;
		zValsOnOff = 0;
		currentPoint = 0;
		currentSynth = 0;
		envChoice = 0;
		nn_synths = List.fill(4, {nil});

		otherValsBusses = Array.fill(3, {Bus.control(group.server, 1)});

		onOffBus = Bus.control(group.server, 1);
		onOffBus.set(1);
		envOnOffBus = Bus.control(group.server, 1);
		envOnOffBus.set(0);

		//hard coding to 8 models
		numModels = 8;

		//synth switcher
		4.do{|i|
			controls.add(Button());
			this.addAssignButton(i, \onOff);
		};

		RadioButtons(controls,
			Array.fill(numModels, {|i| [[ "synth"++(i+1).asString, Color.red, Color.black ],[ "synth"++(i+1).asString, Color.black, Color.red ]]}),
			Array.fill(numModels, {|i|
				{
					this.setNNSynth(i);
				}
			}),
			0, false);

		nn_synthChoices = PathName(path).folders.collect{arg folder;
			folder.postln;
			folder.files.select{arg file;
				file.extension=="sc"
			}
		};
		nn_synthChoices = ["nil"].addAll(nn_synthChoices.flatten.collect{arg item; item.fileNameWithoutExtension});

		controls.add(PopUpMenu()
			.items_(nn_synthChoices)
			.action_{arg menu;
				this.makeNewSynth(menu.value)
			}
		);

		showHideButtons = Array.fill(4, {|i|
			Button()
			.states_([[ "show"++(i+1).asString, Color.red, Color.black ],[ "hide"++(i+1).asString, Color.green, Color.black ]])
			.action_({arg butt;
				if(butt.value == 1){
					if(nn_synths[i]!=nil){nn_synths[i].show}
				}{
					if(nn_synths[i]!=nil){nn_synths[i].hide}
				};
			})
		});

		numModels.do{|i|
			controls.add(Button());
			this.addAssignButton(i+5, \onOff);
		};

		RadioButtons(controls.copyRange(5,5+numModels-1),
			Array.fill(numModels, {|i| [[ "model"++(i+1).asString, Color.red, Color.black ],[ "model"++(i+1).asString, Color.black, Color.red ]]}),
			Array.fill(numModels, {|i|
				{
					if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].changeModel(i)};
				}
			}),
			0, false);

		controls.add(Button()
			.states_([["load", Color.green, Color.black],["load", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].loadPoints;
					//this.setSlidersAndMultis;
				};
		});

		controls.add(Button()
			.states_([["nextPoint", Color.green, Color.black],["nextPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].nextPoint;
					//this.setSlidersAndMultis;
				};
		});

		controls.add(Button()
			.states_([["removePoint", Color.green, Color.black],["removePoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].removePoint};
		});

		controls.add(Button()
			.states_([["addPoint", Color.green, Color.black],["addPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].addPoint};
		});

		controls.add(Button()
			.states_([["trainNN", Color.green, Color.black],["trainNN", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].trainNN};
		});

		controls.add(Button()
			.states_([["reloadNN", Color.green, Color.black],["reloadNN", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].reloadNN};
		});

		6.do{|i|
			this.addAssignButton(5+numModels+i, \onOff);
		};

		controls.add(Button()
			.states_([["predictOff", Color.red, Color.black],["predictOn", Color.green, Color.black]])
			.action_{|button|
				predictOnOff = button.value;
		});
		this.addAssignButton(5+numModels+6, \onOff);

		3.do{|item,i|
			controls.add(Button());
			this.addAssignButton(5+numModels+7+i, \onOff);
		};

		RadioButtons(controls.copyRange(5+numModels+7, 5+numModels+7+2),
			["noZ", "zActions", "env"].collect{|item| [[ item, Color.red, Color.black ],[ item, Color.black, Color.red ]]},
			Array.fill(3, {|i|
				{
					envChoice = i;
					if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].setEnv(i)};
				}
			}),
			0);

		otherVals = [
			[\vol, ControlSpec(0, 1, \amp)],
			[\envRise, ControlSpec(0.01, 0.4, \exp)],
			[\envFall, ControlSpec(0.01, 0.4, \exp)]
		];

		otherVals.do{|item, i|
			controls.add(QtEZSlider(item[0], item[1], {arg val;
				otherValsBusses[i].set(val.value);
			}, [0,0.1,0.1].at(i), true, \horz));
			this.addAssignButton(5+numModels+10+i, \continuous);
		};

		controls.add(Button()
			.states_([["slider update off", Color.red, Color.black],["slider update on", Color.green, Color.black]])
			.action_{|button|
				updateSliders = button.value.asBoolean;
		});
		this.addAssignButton(5+numModels+13, \onOff);

		controls.add(Button()
			.states_([["reload synth", Color.red, Color.black],["reload synth", Color.green, Color.black]])
			.action_{|button|
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].reloadSynth};
		});
		this.addAssignButton(5+numModels+14, \onOff);


		twoDCounter = 0;
		2.do{|i|
			controls.add(QtEZSlider2D.new(ControlSpec(0, 1), ControlSpec(0, 1),
				{|vals|
					if(nn_synths[currentSynth]!=nil){
						twoDCounter = twoDCounter+1;
						if(twoDCounter.odd){nn_synths[currentSynth].setXYZ(i, vals)}
					}
			}, [0, 0], false));
			this.addAssignButton(5+numModels+15+i,\slider2D);

			controls[5+numModels+15+i].zAction = {|val|
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].doTheZ(i, val.value)};
			};
		};

		sliderControl = NN_Synth_Control_NNMod(group, outBus);
		sliderControl.parent_(this);
		sliderControl.hide;

		sliderControlButton = Button()
		.states_([["viewGUICntrl", Color.red, Color.black],["hideGUICntrl", Color.green, Color.black]])
		.action_{|button|
			if(button.value == 1){sliderControl.show}{sliderControl.hide}
		};

		controls.add(Button()
			.states_([["copyPoint", Color.green, Color.black],["copyPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){copiedPoint = nn_synths[currentSynth].copyPoint}{copiedPoint = nil};
		});

		controls.add(Button()
			.states_([["pastePoint", Color.green, Color.black],["pastePoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil&&copiedPoint!=nil){nn_synths[currentSynth].pastePoint(copiedPoint)};

		});

		win.layout_(VLayout(
			HLayout(*(controls.copyRange(0, 4))),
			HLayout(*assignButtons.copyRange(0, 4).add(nil)),
			HLayout(*showHideButtons.add(nil)),
			nil,
			HLayout(*controls.copyRange(5, 5+numModels-1)),
			HLayout(*assignButtons.copyRange(5, 5+numModels-1)),
			nil,
			HLayout(*controls.copyRange(5+numModels, 5+numModels+6-1)
				.addAll(controls.copyRange(5+numModels+17, 5+numModels+18))),
			HLayout(*assignButtons.copyRange(5+numModels, 2+numModels+6-1)),
			nil,
			HLayout(controls[5+numModels+6], assignButtons[5+numModels+7-1]),
			nil,
			HLayout(*controls.copyRange(5+numModels+7, 5+numModels+10-1)),
			HLayout(*assignButtons.copyRange(5+numModels+7, 5+numModels+10-1)),

			HLayout(
				VLayout(*controls.copyRange(5+numModels+10, 5+numModels+13-1)),
				VLayout(*assignButtons.copyRange(5+numModels+10, 5+numModels+13-1))
			),

			HLayout(controls[5+numModels+13], assignButtons[5+numModels+13],
				controls[5+numModels+14], assignButtons[5+numModels+14]
			),
			HLayout(
				VLayout(controls[5+numModels+15], assignButtons[5+numModels+15]),
				VLayout(controls[5+numModels+16], assignButtons[5+numModels+16])),
			sliderControlButton
		)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	setLemur{|vals|
		sliderControl.setLemur(vals)
	}

	makeNewSynth {arg num;
		var newSynthName;
		if(nn_synths[currentSynth]!=nil,{
			nn_synths[currentSynth].killMe;
		});
		newSynthName = nn_synthChoices[num];
		newSynthName.postln;
		if (newSynthName!="nil", {
			nn_synths.put(currentSynth, ModularClassList.initNN_Synth(newSynthName++"_NNMod", group, outBus).postln);
			nn_synths[currentSynth].init2(newSynthName++"_NNMod", this, otherValsBusses, onOffBus, envOnOffBus);
		},{
			nn_synths.put(currentSynth, nil);
		});
		loadedSynths.put(currentSynth, num);
	}

	setNNSynth {|num|
		if(nn_synths[currentSynth]!=nil){
			nn_synths[currentSynth].pause;
			controls[5+nn_synths[currentSynth].whichModel].value_(0);
		};
		currentSynth = num;
		if(nn_synths[currentSynth]!=nil){
			nn_synths[currentSynth].unpause;
			nn_synths[currentSynth];
			this.setSlidersAndMultis;
			controls[5+nn_synths[currentSynth].whichModel].value_(1);
			Lemur_Mod.sendSwitchOSC(oscMsgs[5+nn_synths[currentSynth].whichModel].asString.postln);
			sliderControl.setLabels(nn_synths[currentSynth].getLabels);
		};
		controls[4].value_(loadedSynths[currentSynth]);

	}

	setSlidersAndMultis {
		if(nn_synths[currentSynth]!=nil){
			nn_synths[currentSynth].setSlidersAndSynth2;
			this.setMultiBalls(nn_synths[currentSynth].multiBallList.flatten);
		};
	}

	setMultiBalls {|vals|
		controls[5+numModels+15].valueAction_([vals[0],vals[1]]);
		controls[5+numModels+16].valueAction_([vals[2],vals[3]]);

		[[0,"/x"],[0,"/y"],[1,"/x"],[1,"/y"]].do{arg item, i;
			Lemur_Mod.sendOSC((oscMsgs[item[0]].asString.copyRange(0,oscMsgs[item[0]].asString.size-3)++item[1]).asSymbol, vals[i]);
		}
	}

	setMultiBallsNoAction {|vals|
		vals.postln;
		controls[5+numModels+15].value_([vals[0],vals[1]]);
		controls[5+numModels+16].value_([vals[2],vals[3]]);

		[[5+numModels+15,"/x"],[5+numModels+15,"/y"],[5+numModels+16,"/x"],[5+numModels+16,"/y"]].do{arg item, i;
			Lemur_Mod.sendOSC((oscMsgs[item[0]].asString.copyRange(0,oscMsgs[item[0]].asString.size-3)++item[1]).asSymbol, vals[i]);
		}
	}

	saveExtra {arg saveArray;
		var temp, temp2;

		temp = nn_synths.collect({|item| item.class.asString})
		.addAll(loadedSynths).add(sliderControl.save);

		temp2 = nn_synths.collect({|item, i|
			var answer;
			if((item!=nil)and:(item.class.asString=="NN_Prototype_NNMod")){
				answer = item.save;
			}{answer = nil};
			answer.postln;
		});
		temp.add(temp2.postln);
		saveArray.add(temp);
	}

	setGUISlider {|i, val|
		nn_synths[currentSynth].setGUISlider(i, val);
	}

	setGUIzVal{|i, val|
		hasControl.put(i, val);
	}

	loadExtra {arg loadArray;
		var loadProto;
		if(loadArray!=nil){
			loadArray.copyRange(0,3).do{arg item, i;
				[item,i].postln;
				if (item!="Nil", {
					[item,i].postln;
					nn_synths.put(i, ModularClassList.initNN_Synth(item, group, outBus).postln);
					nn_synths[i].init2(item, this, otherValsBusses, onOffBus, envOnOffBus);
				},{
					nn_synths.put(i, nil);
				});
			};
			loadedSynths = loadArray.copyRange(4,7);
			controls[0].valueAction=1;
			nn_synths.copyRange(1,3).do{|item| if(item!=nil){item.pause}};
			controls[4].value_(loadedSynths[0]);
			try {sliderControl.load(loadArray[8])};
			try {
				loadProto = loadArray[9];
				loadProto.do{|item,i|
					item.postln;
					if(item!=nil){nn_synths[i].load(item)};
				}
			}
		}
	}

	killMeSpecial {
		nn_synths.do{|item| if(item!=nil){item.killMe}};
	}

}

