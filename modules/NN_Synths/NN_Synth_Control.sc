NN_Synth_Control_NNMod :  Module_Mod {
	var texts, functions, zFunctions, <>parent, labels, numControls, button0, button1, button2, loBox, hiBox;

	init {

		this.makeWindow("NN_Synth_Control");

		this.initControlsAndSynths(160);

		texts = List.newClear(0);

		numControls = 80;

		texts = Array.fill(numControls, {|i| "slider"+(i+1).asString}).addAll(Array.fill(numControls, {|i| "zAction"+(i+1).asString}));

		functions = Array.fill(numControls, {|i|
			{arg val; parent.setGUISlider(i, val)}
		});
		zFunctions = Array.fill(numControls, {|i|
			{arg val; parent.setGUIzVal(i, val)}
		});

		//would be better to add zActions to the above
		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false, true, i+functions.size, zFunctions[i]));
		};

		labels = Array.fill(numControls, {|i|
			var field;
			field = TextField().font_(Font("Helvetica", 10)).maxHeight_(15);
			field.string_("/nn_faders/label"++(i+1))
		});

		button0 = Button()
		.states_([["set sliders from 1 (no num)"]])
		.action_({arg butt;
			var temp, num, first, divs, text;

			temp = controls[0].textField.value.asString;
			temp = temp.copyRange(0,temp.size-4);
			numControls.do{|item, i|
				text = temp++(i+1)++"/x";
				controls[i].valueAction_(text);
			};
		});

		button1 = Button()
		.states_([["set texts from 1 (no num)"]])
		.action_({arg butt;
			var temp, num, first, divs, text;

			temp = labels[0].value.asString;
			temp = temp.copyRange(0,temp.size-2);

			numControls.do{|item, i|
				text = temp++(i+1);
				labels[i].valueAction_(text);
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
		});

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
		win.visible_(false);

	}

	setLabels {arg labelsIn;
		labelsIn.do{arg item, i;
			//Lemur_Mod.netAddrs.do{arg addr; addr.sendMsg(labels[i].string.asSymbol, "@content", item)};
			OSCReceiver_Mod.netAddrs.do{arg addr;
				addr.sendMsg(labels[i].string.asSymbol, item);

			};
		};
	}

	setLemurRange{|i, val|
		OSCReceiver_Mod.sendOSC(oscMsgs[i].asString.copyRange(0, oscMsgs[i].size-3), val);
		//Lemur_Mod.sendOSC(oscMsgs[i], val);
	}

	setLemur{|vals|
		//currently disabled - too cpu inefficient
/*		vals.do{|item,i|
			//OSCReceiver_Mod.sendOSC(oscMsgs[i].asString.copyRange(0, oscMsgs[i].size-3), item);
			//Lemur_Mod.sendOSC(oscMsgs[i], item);
		};*/
	}

	saveExtra {|saveArray|
		saveArray.add(labels.collect({|item| item.string}));
	}

	loadExtra {|loadArray|
		loadArray.do{|item, i| labels[i].string = item.asString};

		//I should not have to do this, but for some reason this is not running on load
		controls.do{|item, i|
			var temp = controls[i].textField.value.asString;
			item.textField.valueAction_(temp);
		};
		win.visible_(false);
	}

}