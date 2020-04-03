NN_Synths_Mod : Module_Mod {

	var <>hasControl, <>predictOnOff, zValsOnOff, currentPoint, otherVals, numModels;
	var predictOnOff, currentPoint, envOnOff, <>updateSliders, nn_synths, currentSynth, nn_synthChoices, showHideButtons, nn_synths, currentSynth, otherValsBusses, loadedSynths, sliderControl, sliderControlButton, <>envChoice, <>onOffBus, <>envOnOffBus, copiedPoint, synthControlCounter, inputControl, inputControlButton;

	init {

		this.makeWindow("NN_Synths");

		this.initControlsAndSynths(4+1+8+6+1+6+4+3);

		dontLoadControls = (0..32);

		updateSliders = false;

		loadedSynths = List.fill(4,{0});

		hasControl = Array.fill(100, {0});  //setting max size of NN to 30
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

		synthControlCounter = 0;

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

		inputControl = NN_Input_Control_NNMod(group, outBus);
		inputControl.parent_(this);
		inputControl.hide;

		inputControlButton = Button()
		.states_([["viewGUICntrl", Color.red, Color.black],["hideGUICntrl", Color.green, Color.black]])
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

		controls.add(Button()
			.states_([["newPoints", Color.green, Color.black],["newPoints", Color.green, Color.black]])
			.action_{
				if(nn_synths[currentSynth]!=nil){
					nn_synths[currentSynth].newPointsList
				};

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
				.addAll(controls.copyRange(5+numModels+15, 5+numModels+17))),
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
			/*HLayout(
				VLayout(controls[5+numModels+15], assignButtons[5+numModels+15]),
				VLayout(controls[5+numModels+16], assignButtons[5+numModels+16])),*/
			HLayout(inputControlButton, sliderControlButton)
		)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	/*
		2.do{|i|
			controls.add(QtEZSlider2D.new(ControlSpec(0, 1), ControlSpec(0, 1),
				{|vals|
					if(nn_synths[currentSynth]!=nil){ //filters out half the messages
						twoDCounter = twoDCounter+1;
						if(twoDCounter.odd){nn_synths[currentSynth].setXYZ(i, vals)}
					}
			}, [0, 0], false));
			this.addAssignButton(5+numModels+15+i,\slider2D);

			controls[5+numModels+15+i].zAction = {|val|
				if(nn_synths[currentSynth]!=nil){nn_synths[currentSynth].doTheZ(i, val.value)};
			};
		};*/


	setInputSlider {|vals|

		if(nn_synths[currentSynth]!=nil){
			synthControlCounter = synthControlCounter+1;
			if(synthControlCounter.odd){nn_synths[currentSynth].setXYZ(i, vals)}//filters out half the messages
		}
	}

	setInputButton {|val|

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
		if (newSynthName!="nil", {
			nn_synths.put(currentSynth, ModularClassList.initNN_Synth(newSynthName++"_NNMod", group, outBus));
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
			Lemur_Mod.sendSwitchOSC(oscMsgs[5+nn_synths[currentSynth].whichModel].asString);
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
			loadArray.copyRange(0,3).do{arg item, i;
				if (item!="Nil", {
					nn_synths.put(i, ModularClassList.initNN_Synth(item, group, outBus));
					nn_synths[i].init2(item, this, otherValsBusses, onOffBus, envOnOffBus);
				},{
					nn_synths.put(i, nil);
				});
			};
			try {
				loadProto = loadArray[9];
				loadProto.do{|item,i|
					if(item!=nil){nn_synths[i].load(item)};
				}
			};
			loadedSynths = loadArray.copyRange(4,7);
			controls[0].valueAction=1;
			nn_synths.copyRange(1,3).do{|item| if(item!=nil){item.pause}};
			controls[4].value_(loadedSynths[0]);
			try {sliderControl.load(loadArray[8])};
		};
		AppClock.sched(2, {
			"hide hide hide!".postln;
			nn_synths.do{|item| if(item!=nil){item.hide}};
			sliderControl.hide;
		});
	}

	killMeSpecial {
		nn_synths.do{|item| if(item!=nil){item.killMe}};
	}

}
