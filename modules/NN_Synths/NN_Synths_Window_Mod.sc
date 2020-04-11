NN_Synths_Mod : Module_Mod {

	var <>hasControl, <>predictOnOff, zValsOnOff, currentPoint, otherVals, numModels;
	var predictOnOff, currentPoint, envMode, <>updateSliders, nn_synths, currentSynth, nn_synthChoices, showHideButtons, nn_synths, currentSynth, loadedSynths, sliderControl, sliderControlButton, <>onOffBus, copiedPoint, synthControlCounter, <>inputControl, inputControlButton, modelChoices, chosenModels, nn_synthFolders, <>buttonBusses, trainingButtons, sliderUpdateButton, volBus, numSynths, onOffSwitches, zMode, zModeButtons;

	init {

		this.makeWindow("NN_Synths");

		this.initControlsAndSynths(27);

		dontLoadControls = (0..26);

		updateSliders = false;

		loadedSynths = List.fill(4,{0});

		hasControl = Array.fill(100, {0});  //setting max size of NN to 30
		predictOnOff = 0;
		zValsOnOff = 0;
		currentPoint = 0;
		currentSynth = 0;
		envMode = 0;
		nn_synths = List.fill(4, {nil});
		chosenModels = List.fill(4, {0});

		volBus = Bus.control(group.server, 1);

		buttonBusses = Array.fill(10, {Bus.control(group.server, 1)});
		buttonBusses.do{|item| item.set(1)};

		synthControlCounter = 0;

		//hard coding to 8 models
		numSynths = 4;
		numModels = 8;

		//synth switcher
		numSynths.do{|i|
			controls.add(Button());
			this.addAssignButton(i, \onOff);
		};

		RadioButtons(controls,
			Array.fill(numSynths, {|i| [[ "synth"++(i+1).asString, Color.red, Color.black ],[ "synth"++(i+1).asString, Color.black, Color.red ]]}),
			Array.fill(numSynths, {|i|
				{
					this.setNNSynth(i);
				}
			}),
			0, false);

		//knowledge of the models is all based on the directory structure

		nn_synthChoices = PathName(path).folders.collect{arg folder;
			folder.files.select{arg file;
				file.extension=="sc"
			};
		};
		nn_synthFolders = nn_synthChoices.flatten.collect{arg item; item.pathOnly};
		nn_synthChoices = ["nil"].addAll(nn_synthChoices.flatten.collect{arg item; item.fileNameWithoutExtension});

		controls.add(PopUpMenu()
			.items_(nn_synthChoices)
			.action_{arg menu;
				this.makeNewSynth(menu.value)
			}
		);

		modelChoices = List[nil];
		nn_synthFolders.collect{arg folder;
			var temp;
			temp = List["nil"];
			PathName(folder).folders.do{arg folder;
				if(folder.folderName.contains("model")){
					temp.add(folder.folderName)
				}
			};
			modelChoices.add(temp);
		};

		controls.add(PopUpMenu()
			.items_(["nil"])
			.action_{arg menu;
				chosenModels.put(currentSynth, menu.value);
				if(menu.value!=0){
					this.loadTrainingFolder(PathName(nn_synthFolders[controls[4].value-1]++menu.item))
				}{nn_synths[currentSynth].clearTraining}
			}
		);

		onOffSwitches = List[List.fill(4,{0}),List.fill(4,{0})];
		2.do{|i|
			numSynths.do{|i2|
				controls.add(NumberBox().clipLo_(1).clipHi_(8)
					.action_{|num|
						onOffSwitches[i].put(i2, num.value);
					}
					.valueAction_(1)
				)
			}
		};

		showHideButtons = Array.fill(numSynths, {|i|
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
			this.addAssignButton(14+i, \onOff);
		};

		RadioButtons(controls.copyRange(14,14+numModels-1),
			Array.fill(numModels, {|i| [[ "model"++(i+1).asString, Color.red, Color.black ],[ "model"++(i+1).asString, Color.black, Color.red ]]}),
			Array.fill(numModels, {|i|
				{
					if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].changeModel(i)};
				}
			}),
			0, false);

		trainingButtons = List.newClear(0);

		trainingButtons.add(Button()
			.states_([["newPoints", Color.green, Color.black],["newPoints", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].newPointsList
				};
			}
		);

		trainingButtons.add(Button()
			.states_([["loadPoints", Color.green, Color.black],["loadPoints", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].loadPoints;
					//this.setSlidersAndMultis;
				};
		});

		trainingButtons.add(Button()
			.states_([["nextPoint", Color.green, Color.black],["nextPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].nextPoint;
					//this.setSlidersAndMultis;
				};
		});

		trainingButtons.add(Button()
			.states_([["removePoint", Color.green, Color.black],["removePoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].removePoint};
		});

		trainingButtons.add(Button()
			.states_([["addPoint", Color.green, Color.black],["addPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].addPoint};
		});

		trainingButtons.add(Button()
			.states_([["trainNN", Color.green, Color.black],["trainNN", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].trainNN};
		});

		trainingButtons.add(Button()
			.states_([["reloadNN", Color.green, Color.black],["reloadNN", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].reloadNN};
		});

		trainingButtons.add(Button()
			.states_([["copyPoint", Color.green, Color.black],["copyPoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){copiedPoint = nn_synths[currentSynth].copyPoint}{copiedPoint = nil};
		});

		trainingButtons.add(Button()
			.states_([["pastePoint", Color.green, Color.black],["pastePoint", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil&&copiedPoint!=nil){nn_synths[currentSynth].pastePoint(copiedPoint)};

		});

		controls.add(QtEZSlider("vol", ControlSpec(0, 1, \amp), {arg val;
			volBus.set(val.value);
		}, 0, true, \horz));
		this.addAssignButton(14+numModels, \continuous);

		controls.add(Button()
			.states_([["predictOff", Color.red, Color.black],["predictOn", Color.green, Color.black]])
			.action_{|button|
				predictOnOff = button.value;
		});
		this.addAssignButton(14+numModels+1, \onOff);



		3.do{|item,i|
			controls.add(Button());
			this.addAssignButton(14+numModels+2+i, \onOff);
		};

		RadioButtons(controls.copyRange(14+numModels+2, 14+numModels+4),
			["on", "mono", "poly"].collect{|item| [[ item, Color.red, Color.black ],[ item, Color.black, Color.red ]]},
			Array.fill(3, {|i|
				{
					envMode = i;

					nn_synths.do{|synth| if(synth!=nil){synth.synths[0].set(\switchState, envMode)}};
				}
			}),
		0);


		zModeButtons = Array.fill(2, {Button()});
		zMode = 0;
		RadioButtons(zModeButtons,
			["stay mode", "switch mode"].collect{|item| [[ item, Color.red, Color.black ],[ item, Color.black, Color.red ]]},
			Array.fill(2, {|i|
				{
					zMode = i;
				}
			}),
		0);

		sliderUpdateButton = Button()
		.states_([["slider update off", Color.red, Color.black],["slider update on", Color.green, Color.black]])
		.action_{|button|
			updateSliders = button.value.asBoolean;
		};

		inputControl = NN_Input_Control_NNMod(group, outBus);
		inputControl.parent_(this);
		inputControl.hide;

		inputControlButton = Button()
		.states_([["viewInputCntrl", Color.red, Color.black],["hideInputCntrl", Color.green, Color.black]])
		.action_{|button|
			if(button.value == 1){inputControl.show}{inputControl.hide}
		};

		sliderControl = NN_Synth_Control_NNMod(group, outBus);
		sliderControl.parent_(this);
		sliderControl.hide;

		sliderControlButton = Button()
		.states_([["viewGUICntrl", Color.red, Color.black],["hideGUICntrl", Color.green, Color.black]])
		.action_{|button|
			if(button.value == 1){sliderControl.show}{sliderControl.hide}
		};

		win.layout_(VLayout(
			HLayout(*(controls.copyRange(0, 5))),
			HLayout(*assignButtons.copyRange(0, 3).addAll([nil, nil])),

			//number boxes
			HLayout(*(controls.copyRange(6, 9))),
			HLayout(*(controls.copyRange(10, 13))),

			HLayout(*showHideButtons.add(nil)),
			nil,
			HLayout(*controls.copyRange(14, 14+numModels-1)),
			HLayout(*assignButtons.copyRange(14, 14+numModels-1)),
			nil,
			HLayout(*trainingButtons),
			nil,
			HLayout(controls[14+numModels], assignButtons[14+numModels], controls[14+numModels+1], assignButtons[14+numModels+1]), //vol slider and predict on
			nil,
			HLayout(*controls.copyRange(14+numModels+2, 14+numModels+4)), //on mono poly
			HLayout(*zModeButtons),
			HLayout(*assignButtons.copyRange(14+numModels+2, 14+numModels+4)),

			HLayout(sliderUpdateButton, inputControlButton, sliderControlButton)
		)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	setInputSliders {|vals|
		//vals.postln;
		if(envMode<2){
			if(nn_synths[currentSynth]!=nil){
				synthControlCounter = synthControlCounter+1;
				if(synthControlCounter.odd){nn_synths[currentSynth].setNNInputVals(vals)}//filters out half the messages
			}
		}{
			synthControlCounter = synthControlCounter+1;
			nn_synths.do{|synth, i|
				if((synth!=nil).and{synth.onOff0+synth.onOff1>0}){
					if(synthControlCounter.odd){
						synth.setNNInputVals(vals)
					}
				}
			}
		}
	}

	setInputButton {|num, val|
		//a button has been pressed
		var done = false;

		nn_synths.do{|synth, i|
			if(synth!=nil){
				if((zMode==1).and{done.not})
				{
					if(val==1){
						if((num==onOffSwitches[0][i]).or{num==onOffSwitches[1][i]}){
							done=true;
							{controls[i].valueAction_(1)}.defer
						}
					}
				};
				if(num==onOffSwitches[0][i]){
					synth.synths[0].set(\onOff0, val);
					synth.onOff0 = val;
				};
				if(num==onOffSwitches[1][i]){
					synth.onOff1 = val;
					synth.synths[0].set(\onOff1, val)
				};
			}
		}
	}

	setLemur{|vals|
		sliderControl.setLemur(vals)
	}

	loadTrainingFolder {arg folder;
		"load training folder ".post;
		folder.postln;
		nn_synths[currentSynth].loadTraining(folder.fullPath);
	}

	makeNewSynth {arg num;
		var newSynthName;

		if(nn_synths[currentSynth]!=nil,{
			nn_synths[currentSynth].killMe;
		});
		newSynthName = nn_synthChoices[num];
		if (newSynthName!="nil", {
			controls[5].items_(modelChoices[num].asArray);
			nn_synths.put(currentSynth, ModularClassList.initNN_Synth(newSynthName++"_NNMod", group, outBus));
			nn_synths[currentSynth].init2(newSynthName++"_NNMod", this, volBus, onOffSwitches[0][currentSynth], onOffSwitches[1][currentSynth]);
			//set the model trainings
		},{
			controls[5].items_(modelChoices[0].asArray);
			nn_synths.put(currentSynth, nil);
		});
		loadedSynths.put(currentSynth, num);
	}

	setNNSynth {|num|
		if(nn_synths[currentSynth]!=nil){
			nn_synths[currentSynth].synths[0].set(\isCurrent, 0);
			controls[14+nn_synths[currentSynth].whichModel].value_(0);
		};
		currentSynth = num;
		if(nn_synths[currentSynth]!=nil){
			//nn_synths[currentSynth].unpause;
			//nn_synths[currentSynth];
			nn_synths[currentSynth].synths[0].set(\isCurrent, 1);
			this.setSlidersAndMultis;
			controls[14+nn_synths[currentSynth].whichModel].value_(1);
			Lemur_Mod.sendSwitchOSC(oscMsgs[14+nn_synths[currentSynth].whichModel].asString);
			sliderControl.setLabels(nn_synths[currentSynth].getLabels);
		};
		controls[4].value_(loadedSynths[currentSynth]);
		if(nn_synths[currentSynth]!=nil){
			controls[5].items_(modelChoices[currentSynth+1].asArray);
			controls[5].value_(chosenModels[currentSynth]);
		}{controls[5].items_(["nil"])}

	}

	setSlidersAndMultis {
		if(nn_synths[currentSynth]!=nil){
			nn_synths[currentSynth].setSlidersAndSynth2;
		};
	}

	/*	setMultiBalls {|vals|
	controls[6+numModels+15].valueAction_([vals[0],vals[1]]);
	controls[6+numModels+16].valueAction_([vals[2],vals[3]]);

	[[0,"/x"],[0,"/y"],[1,"/x"],[1,"/y"]].do{arg item, i;
	Lemur_Mod.sendOSC((oscMsgs[item[0]].asString.copyRange(0,oscMsgs[item[0]].asString.size-3)++item[1]).asSymbol, vals[i]);
	}
	}*/

	setControlPointsNoAction {|vals|
		/*controls[5+numModels+15].value_([vals[0],vals[1]]);
		controls[5+numModels+16].value_([vals[2],vals[3]]);*/

		vals.do{|item, i|
			inputControl.controls[i].setExternal_(item);
			Lemur_Mod.sendOSC(inputControl.oscMsgs[i].asSymbol, item);
		};

		/*[[5+numModels+15,"/x"],[5+numModels+15,"/y"],[5+numModels+16,"/x"],[5+numModels+16,"/y"]].do{arg item, i;
		Lemur_Mod.sendOSC((oscMsgs[item[0]].asString.copyRange(0,oscMsgs[item[0]].asString.size-3)++item[1]).asSymbol, vals[i]);
		}*/
	}

	saveExtra {arg saveArray;
		var temp, temp2;

		temp = nn_synths.collect({|item| item.class.asString})
		.addAll(loadedSynths).addAll(chosenModels).addAll(onOffSwitches.flatten)
		.add(sliderControl.save).add(inputControl.save);

		temp2 = nn_synths.collect({|item, i|
			var answer;
			if((item!=nil)and:(item.class.asString=="NN_Prototype_NNMod")){
				answer = item.save;
			}{answer = nil};
		});
		temp.add(temp2);
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

			loadedSynths = loadArray.copyRange(4,7);
			controls[4].value_(loadedSynths[0]);
			chosenModels = loadArray.copyRange(8,11);
			controls.copyRange(6, 13).do{|item, i| item.valueAction_(loadArray[12+i])};
			loadArray.copyRange(0,3).do{arg item, i;
				if (item!="Nil", {
					nn_synths.put(i, ModularClassList.initNN_Synth(item, group, outBus));
					nn_synths[i].init2(item, this, volBus, onOffSwitches[0][i], onOffSwitches[1][i]);
					//here I need to load the models that are saved
/*					"here ".post; i.postln;
					loadedSynths.postln;
					modelChoices.postln;
					chosenModels.postln;
					nn_synthFolders.postln;*/
					if(chosenModels[i]!=0){nn_synths[i].loadTraining((nn_synthFolders[loadedSynths[i]-1]++modelChoices[i+1][chosenModels[i]]).postln)}
				},{
					"isNil".postln;
					nn_synths.put(i, nil);
				});
			};
			try {
				loadProto = loadArray[22];
				loadProto.do{|item,i|
					if(item!=nil){nn_synths[i].load(item)};
				}
			};
			controls[0].valueAction=1;
			//nn_synths.copyRange(1,3).do{|item| if(item!=nil){item.pause}};

			controls[5].items_(modelChoices[loadedSynths[0]].asArray);
			controls[5].value_(chosenModels[0]);
			try {sliderControl.load(loadArray[20])};
			try {inputControl.load(loadArray[21])};
		};
		AppClock.sched(2, {
			nn_synths.do{|item| if(item!=nil){item.hide}};
			sliderControl.hide;
		});
	}

	killMeSpecial {
		nn_synths.do{|item| if(item!=nil){item.killMe}};
	}

}