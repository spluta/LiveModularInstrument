NN_Synth_ID {
	classvar <id=5000;
	*initClass { id = 5000; }
	*next  { ^id = id + 1; }
	*path {this.filenameSymbol.postln}
}

NN_Synth : Module_Mod {
	classvar <>pythonPath = "/usr/local/Cellar/python/3.7.5/Frameworks/Python.framework/Versions/3.7/bin/python3.7";
	var group, outBus, numModels, sizeOfNN, ports, pythonAddrs, hasControl, predictOnOff, zValsOnOff, currentPoint, pythonFile, whichModel, multiBallList, xyz, synth, synthVals, valList, allValsList, nnVals, otherVals, trainingList;
	var saveFile, modelFile, predictOnOff, currentPoint, envOnOff, envChoice, loadedCount, updateSliders, receivePort, sliderCount;

	init {
		"no init".postln;
	}

	initNN_Synth {arg numModelsIn, sizeOfNNIn;

		pythonFile = "NN_Synth_1_Predict.py";

		numModels = numModelsIn;
		sizeOfNN = sizeOfNNIn;

		updateSliders = false;

		receivePort = NN_Synth_ID.next;

		ports = List.fill(numModels, {|i| NN_Synth_ID.next});

		pythonAddrs = List.fill(numModels, {|i| NetAddr("127.0.0.1", ports[i])});

		hasControl = Array.fill(sizeOfNN, {0});
		predictOnOff = 0;
		zValsOnOff = 0;
		currentPoint = 0;
		sliderCount = 0;

		trainingList = List.newClear(0);
		multiBallList = List[List[0,0,],List[0,0]];
		valList = List.fill(sizeOfNN, {0});
		this.setAllVals;

		numModels.do{|i|
			(pythonPath+path.quote++pythonFile+"--path"+path.quote+"--port"+ports[i].asString
				+"--sendPort"+receivePort.asString+"--num"+i.asString+"&").unixCmd;
			ServerQuit.add({NetAddr("127.0.0.1", ports[i]).sendMsg('/close')})
		};

		//set the system to receive messages from each python instance
		OSCFunc.new({arg ...msg;
			//msg.postln;
			this.setSlidersAndSynth(msg[0].copyRange(2,sizeOfNN+1));
			//valList = msg[0].copyRange(1,sizeOfNN).asList;
		}, '/nnOutputs', nil, receivePort);

		//prime the pump
		//numModels.do{arg modelNum;
		loadedCount = 0;
			OSCFunc({arg ...msg;
			loadedCount = loadedCount+1;
				if(loadedCount == numModels){
					{
						"loaded".postln;
						1.wait;
						(40).do{|i|
							var temp;
							temp = Array.fill(4, {1.0.rand});
							multiBallList = temp.asList.clump(2);
							pythonAddrs[i%numModels].sendMsg(*['/predict'].addAll(temp));
							0.01.wait
						};
						//pythonAddrs[modelNum].sendMsg(*['/prime'].addAll(Array.fill(4, {0})));
					}.fork;
				}
			}, '/loaded', nil, receivePort);
		//};
		whichModel = 0;

		xyz = List.fill(2, {List.fill(3, {0})});

		2.do{|i|
			controls.add(QtEZSlider2D.new(ControlSpec(0, 1), ControlSpec(0, 1),
				{|vals|
					xyz[i].put(0, vals[0]);
					xyz[i].put(1, vals[1]);
					this.configure;
			}, [0, 0], false));
			this.addAssignButton(i,\slider2D);

			controls[i].zAction = {|val|
				xyz[i].put(2, val.value);
				this.doTheZ;
			};
		};

		numModels.do{|i|
			controls.add(Button());
			this.addAssignButton(i+2, \onOff);
		};
		RadioButtons(controls.copyRange(2, 2+numModels),
			Array.fill(numModels, {|i| [[ "model"++(i+1).asString, Color.red, Color.black ],[ "model"++(i+1).asString, Color.black, Color.red ]]}),
			Array.fill(numModels, {|i|
				{
					allValsList.put(whichModel, valList.addAll(multiBallList.flatten));
					whichModel = i;
					valList = allValsList[whichModel].copyRange(0,sizeOfNN-1);
					this.setSlidersAndSynth(valList);
					this.setMultiBalls(allValsList[whichModel].copyRange(sizeOfNN,sizeOfNN+4));
				}
			}),
			0, false);

		nnVals.do{arg item, i;
			controls.add(QtEZSlider(item[0], item[1], {arg val;
				//this.setSynth(item[0], i, val.slider.value, val.value);
				synths[0].set(item[0], val.value);
				{valList.put(i, val.slider.value)}.defer;
			}, allValsList[0][i], true, \horz));
			this.addAssignButton(2+numModels+i, \continuous);

			controls[2+numModels+i].zAction = {|val|
				hasControl.put(i, val.value);
			};
		};

		controls.add(Button()
			.states_([["load", Color.green, Color.black],["load", Color.green, Color.black]])
			.action_{
				trainingList = CSVFileReader.read(path++"trainingFile"++whichModel++".csv");
				trainingList = trainingList.collect({arg item; item.collect({arg item2; item2.asFloat})}).asList;
				currentPoint = 0;
				this.setSlidersAndMultis;
		});

		controls.add(Button()
			.states_([["nextPoint", Color.green, Color.black],["nextPoint", Color.green, Color.black]])
			.action_{
				currentPoint = (currentPoint+1).wrap(0, trainingList.size-1);
				this.setSlidersAndMultis;
		});

		controls.add(Button()
			.states_([["removePoint", Color.green, Color.black],["removePoint", Color.green, Color.black]])
			.action_{
				if(trainingList.size>0){
					trainingList.removeAt(currentPoint);
					currentPoint = max(currentPoint-1, 0);
				};
		});

		controls.add(Button()
			.states_([["addPoint", Color.green, Color.black],["addPoint", Color.green, Color.black]])
			.action_{
				trainingList.add(valList.copyRange(0,sizeOfNN-1).addAll(multiBallList.flatten));
		});

		controls.add(Button()
			.states_([["trainNN", Color.green, Color.black],["trainNN", Color.green, Color.black]])
			.action_{
				{
					saveFile = File(path++"trainingFile"++whichModel++".csv", "w");

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
					(pythonPath+path.quote++"NN_Synth_1_Save.py"+"--numbersFile"+saveFile.quote+"--modelFile"+modelFile.quote+"--num"+whichModel.asString+"&").unixCmd;
				}.fork;
				OSCFunc({
					this.reloadNN(whichModel);
					"trained".postln;
				}, '/trained', nil, receivePort).oneShot;
		});

		controls.add(Button()
			.states_([["reloadNN", Color.green, Color.black],["reloadNN", Color.green, Color.black]])
			.action_{
				this.reloadNN(whichModel);
		});

		6.do{|i|
			this.addAssignButton(2+numModels+sizeOfNN+i, \onOff);
		};

		controls.add(Button()
			.states_([["predictOff", Color.red, Color.black],["predictOn", Color.green, Color.black]])
			.action_{|button|
				predictOnOff = button.value;
		});
		this.addAssignButton(2+numModels+sizeOfNN+6, \onOff);

		envChoice = 0;

		3.do{|item,i|
			controls.add(Button());
			this.addAssignButton(2+numModels+sizeOfNN+7+i, \onOff);
		};

		RadioButtons(controls.copyRange(2+numModels+sizeOfNN+7, 2+numModels+sizeOfNN+7+2),
			["noZ", "zActions", "env"].collect{|item| [[ item, Color.red, Color.black ],[ item, Color.black, Color.red ]]},
			[
				{envChoice = 0; synth.set(\onOff, 1, \envOnOff, 0);},
				{
					envChoice = 1;
					synth.set(\onOff, xyz[[xyz[0][2],xyz[1][2]].maxIndex][2], \envOnOff, 0);
				},
				{
					envChoice = 2;
					synth.set(\onOff, xyz[[xyz[0][2],xyz[1][2]].maxIndex][2], \envOnOff, 1)}],
			0);

		otherVals = [
			[\vol, ControlSpec(0, 0.2, \amp)],
			[\envRise, ControlSpec(0.01, 0.4, \exp)],
			[\envFall, ControlSpec(0.01, 0.4, \exp)]
		];

		otherVals.do{|item, i|
			controls.add(QtEZSlider(item[0], item[1], {arg val;
				synths[0].set(item[0], val.value)
			}, allValsList[0][i+sizeOfNN], true, \horz));
			this.addAssignButton(2+numModels+sizeOfNN+10+i, \continuous);
		};

		controls.add(Button()
			.states_([["slider update off", Color.red, Color.black],["slider update on", Color.green, Color.black]])
			.action_{|button|
				updateSliders = button.value.asBoolean;
		});
		this.addAssignButton(2+numModels+sizeOfNN+13, \onOff);


		controls.add(Button()
			.states_([["copyPoint", Color.green, Color.black],["copyPoint", Color.green, Color.black]])
			.action_{

		});

		controls.add(Button()
			.states_([["pastePoint", Color.green, Color.black],["pastePoint", Color.green, Color.black]])
			.action_{

		});

		win.layout_(VLayout(
			HLayout(
				VLayout(controls[0], assignButtons[0]),
				VLayout(controls[1], assignButtons[1])),
			HLayout(*controls.copyRange(2, 2+numModels-1)),
			HLayout(*assignButtons.copyRange(2, 2+numModels-1)),
			HLayout(
				VLayout(*controls.copyRange(2+numModels, 2+numModels+sizeOfNN-1)
					.addAll(controls.copyRange(2+numModels+sizeOfNN+14, 2+numModels+sizeOfNN+15))),
				VLayout(*assignButtons.copyRange(2+numModels, 2+numModels+sizeOfNN-1)
					.addAll(assignButtons.copyRange(2+numModels+sizeOfNN+14, 2+numModels+sizeOfNN+15))),
			),

			HLayout(*controls.copyRange(2+numModels+sizeOfNN, 2+numModels+sizeOfNN+3-1)),
			HLayout(*assignButtons.copyRange(2+numModels+sizeOfNN, 2+numModels+sizeOfNN+3-1)),
			HLayout(*controls.copyRange(2+numModels+sizeOfNN+3, 2+numModels+sizeOfNN+6-1)),
			HLayout(*assignButtons.copyRange(2+numModels+sizeOfNN+3, 2+numModels+sizeOfNN+6-1)),

			HLayout(controls[2+numModels+sizeOfNN+6], assignButtons[2+numModels+sizeOfNN+6]),

			HLayout(*controls.copyRange(2+numModels+sizeOfNN+7, 2+numModels+sizeOfNN+10-1)),
			HLayout(*assignButtons.copyRange(2+numModels+sizeOfNN+7, 2+numModels+sizeOfNN+10-1)),

			HLayout(
				VLayout(*controls.copyRange(2+numModels+sizeOfNN+10, 2+numModels+sizeOfNN+13-1)),
				VLayout(*assignButtons.copyRange(2+numModels+sizeOfNN+10, 2+numModels+sizeOfNN+13-1))
			),
			HLayout(controls[2+numModels+sizeOfNN+13], assignButtons[2+numModels+sizeOfNN+13])
		));
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];

	}

	setSynth {|argument, i, val01, val|
		//"setSynth ".post;[argument, i, val].postln;
		valList.put(i, val01);
		synths[0].set(argument, val);
	}

	reloadNN {arg num;
		{
			"close".postln;
			pythonAddrs[num].sendMsg('/close');
			1.wait;
			"reload ".post; num.postln;
			(pythonPath+path.quote++pythonFile+"--path"+path.quote+"--port"+(ports[num]).asString
				+"--sendPort"+receivePort.asString+"--num"+num.asString+"&").postln.unixCmd;
			1.wait;
			5.do{
				pythonAddrs[num].sendMsg(*['/predict'].addAll(Array.fill(4, {1.0.rand})));
				0.01.wait
			};
		}.fork;
	}

	doTheZ {
		switch(envChoice,
			0, {synths[0].set(\onOff, 1, \envOnOff, 0)},
			1, {synths[0].set(\onOff, xyz[[xyz[0][2],xyz[1][2]].maxIndex][2], \envOnOff, 0)},
			2, {synths[0].set(\onOff, xyz[[xyz[0][2],xyz[1][2]].maxIndex][2], \envOnOff, 1)}
		);
	}

	configure {

		xyz.do{|item,i| multiBallList.put(i, item.copyRange(0,1))};

		if(predictOnOff==1){pythonAddrs[whichModel].sendMsg(*['/predict'].addAll(multiBallList.flatten))};
	}

	setSlidersAndSynth {|vals|
		sliderCount = sliderCount+1;
		//sliderCount.postln;
		vals.do{|item, i|
			if(i<sizeOfNN,{
				if(hasControl[i]==0,{
					this.setSynth(nnVals[i][0], i, vals[i], controls[2+numModels+i].controlSpec.map(item));
					//synths[0].set(nnVals[i][0], controls[2+numModels+i].controlSpec.map(item));
					//{controls[2+numModels+i].valueAction_(controls[2+numModels+i].controlSpec.map(item))}.defer;
					Lemur_Mod.sendOSC(oscMsgs[2+numModels+i], item);
				});
			});
		};
		if(updateSliders==true){
			if(sliderCount%50==0){
				vals.do{|item, i|
					{controls[2+numModels+i].value_(controls[2+numModels+i].controlSpec.map(item))}.defer;
				}
			}
		};
	}

	setMultiBalls {|vals|
		vals.postln;
		controls[0].valueAction_([vals[0],vals[1]]);
		controls[1].valueAction_([vals[2],vals[3]]);

		[[0,"/x"],[0,"/y"],[1,"/x"],[1,"/y"]].do{arg item, i;
			Lemur_Mod.sendOSC((oscMsgs[item[0]].asString.copyRange(0,oscMsgs[item[0]].asString.size-3)++item[1]).asSymbol, vals[i]);
		}
	}

	setAllVals{}

	setSlidersAndMultis {
		if(trainingList.size>0){
			this.setSlidersAndSynth(trainingList[currentPoint].copyRange(0,sizeOfNN-1));
			this.setMultiBalls(trainingList[currentPoint].copyRange(sizeOfNN,sizeOfNN+4));
		};
	}

	killMeSpecial {
		"kill the pythons".postln;
		pythonAddrs.do{|i|i.sendMsg('/close')};
	}

}

