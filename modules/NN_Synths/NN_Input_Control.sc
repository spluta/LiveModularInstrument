NN_Input_Control_NNMod :  TypeOSCModule_Mod {
	var texts, functions, onOffFunctions, <>parent, labels, numControls, button0, button1, button2, loBox, hiBox, sliderVals;

	init {

		this.makeWindow("NN_Synth_Control");

		this.initControlsAndSynths(160);

		texts = List.newClear(0);

		numControls = 10;

		sliderVals = (0!numControls).asList;

		texts = Array.fill(numControls, {|i| "slider"+(i+1).asString}).addAll(numControls, {|i| "onOff"+(i+1).asString});

		functions = Array.fill(numControls, {|i|
			{arg val;
				sliderVals.put(i, val)
				parent.setInputSliders(s)}
		});

		onOffFunctions = Array.fill(numControls, {|i|
			{arg val; parent.setInputButton(i, val)}
		});

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false, true, i+functions.size));
		};

		onOffFunctions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false, true, i+onOffFunctions.size));
		};

		/*labels = Array.fill(numControls, {|i|
			var field;
			field = TextField().font_(Font("Helvetica", 10)).maxHeight_(15);
			if(i==0){field.string_("/Container2/Container2/Text")}{field.string_("/Container2/Container2/Text"++(i+1).asString)}
		});

		button0 = Button()
		.states_([["set sliders from 1"]])
		.action_({arg butt;
			var temp, num, first, divs;

			temp = controls[0].textField.value.asString;
			divs = temp.findAll("/");
			if(divs[divs.size-1]-divs[divs.size-2]==6){first = 1};
			if(divs.last-divs[divs.size-2]==7){first = temp[divs.last-1].asString.asInteger};
			if(divs.last-divs[divs.size-2]==8){first = temp.copyRange(divs.last-2, divs.last-1).asInteger};
			(1..numControls-1).do{|i|
				temp = temp.copyRange(0, divs[divs.size-2]+5)++(first+i)++"/x";
				controls[i].textField.valueAction_(temp);
			};
		});

		button1 = Button()
		.states_([["set texts from 1"]])
		.action_({arg butt;
			var temp, num, first, divs;

			temp = labels[0].value.asString;
			divs = temp.findAll("/");

			if(temp.size-divs.last==5){first = 1};
			if(temp.size-divs.last==6){first = temp.last.asString.asInteger};
			if(temp.size-divs.last==7){first = temp.copyRange(temp.size-2, temp.size-1).asInteger};

			(1..numControls-1).do{|i|
				temp = temp.copyRange(0, divs.last+4)++(first+i);
				labels[i].valueAction_(temp);
			};
		});

		loBox = NumberBox().value_(1);
		hiBox = NumberBox().value_(1);

		button2 = Button()
		.states_([["set random"]])
		.action_({arg butt;
			(loBox.value.asInteger..hiBox.value.asInteger).do{|val|
				var rando;
				rando = 1.0.rand;
				parent.setGUISlider(val-1, rando);
				this.setLemurRange(val-1, rando);
			};
		});*/

		win.layout_(
			VLayout(
				HLayout(
					VLayout(*controls.copyRange(0, (numControls/2-1).asInteger)),
					VLayout(*labels.copyRange(0, (numControls/2-1).asInteger)),
					VLayout(*controls.copyRange((numControls/2).asInteger, (numControls-1).asInteger)),
					VLayout(*labels.copyRange((numControls/2).asInteger, (numControls-1).asInteger))
				),
				HLayout(button0, button1, loBox, hiBox, button2)
			)
		);
		win.layout.spacing_(1).margins_(1!4);
		win.view.maxHeight_(numControls/2*17);
		//win.front;
		win.visible_(false);

	}

	setLabels {arg labelsIn;
		labelsIn.do{arg item, i;
			Lemur_Mod.netAddrs.do{arg addr; addr.sendMsg(labels[i].string.asSymbol, "@content", item)};
		};
	}

	setLemurRange{|i, val|
		Lemur_Mod.sendOSC(oscMsgs[i], val);
	}

	setLemur{|vals|
		vals.do{|item,i|
			Lemur_Mod.sendOSC(oscMsgs[i], item);
		};
	}

	saveExtra {|saveArray|
		saveArray.add(labels.collect({|item| item.string}));
	}

	loadExtra {|loadArray|
		loadArray.do{|item, i| labels[i].string = item.asString};
		win.visible_(false);
	}

}