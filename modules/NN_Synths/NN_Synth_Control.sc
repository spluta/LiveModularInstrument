NN_Synth_Control_NNMod :  TypeOSCModule_Mod {
	var texts, functions, zFunctions, <>parent, labels;

	init {

		this.makeWindow("NN_Synth_Control");

		this.initControlsAndSynths(40);

		texts = List.newClear(0);

		texts = Array.fill(20, {|i| "slider"+(i+1).asString}).addAll(Array.fill(20, {|i| "zAction"+(i+1).asString}));

		functions = Array.fill(20, {|i|
			{arg val; parent.setGUISlider(i, val)}
		});
		zFunctions = Array.fill(20, {|i|
			{arg val; parent.setGUIzVal(i, val)}
		});

		//would be better to add zActions to the above
		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false, true, i+functions.size, zFunctions[i]));
		};

		labels = Array.fill(20, {|i|
			var field;
			field = TextField().font_(Font("Helvetica", 10)).maxHeight_(15);
			if(i==0){field.string_("/Container2/Container2/Text")}{field.string_("/Container2/Container2/Text"++(i+1).asString)}
		});

		win.layout_(
 			HLayout(
 				//VLayout(*controls.collect({arg item; item.view}))
				VLayout(*controls.copyRange(0,19)),
				VLayout(*labels)/*,
				StaticText().string_("zActions"),
				VLayout(*controls.copyRange(20,39))*/
 			)
 		);
 		win.layout.spacing_(1).margins_(1!4);
 		win.view.maxHeight_(20*17);
 		win.front;

	}

	setLabels {arg labelsIn;
		"setLabels ".post; labelsIn.postln;
		labelsIn.do{arg item, i;
			//Lemur_Mod.sendOSC(labels[i], item);
			Lemur_Mod.netAddrs.do{arg addr; addr.sendMsg(labels[i].string.asSymbol, "@content", item)};
		};
	}

	setLemur{|vals|
		vals.do{|item,i|
			Lemur_Mod.sendOSC(oscMsgs[i], item);
		};
	}
}