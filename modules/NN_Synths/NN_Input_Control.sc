NN_Input_Control_NNMod :  TypeOSCModule_Mod {
	var texts, functions, onOffFunctions, <>parent, labels, numControls, button0, button1, button2, loBox, hiBox, sliderVals, sliderOns, freezeButton, modControls, controlsCount, changed, changeRout;

	init {

		this.makeWindow("NN_Input_Control");

		numControls = 10;

		this.initControlsAndSynths(numControls*3+1);

		sliderVals = (0!numControls).asList;
		sliderOns = (0!numControls).asList;

		texts = Array.fill(numControls, {|i| "slider"+(i+1).asString}).addAll(numControls, {|i| "onOff"+(i+1).asString});

		modControls = 1;
		controlsCount = 0;
		changed = false;

		functions = Array.fill(numControls, {|i|
			{arg val;
				//controlsCount = (controlsCount+1).wrap(0,10);
				//if(controlsCount%modControls==0){
				sliderVals.put(i, val);
				changed = true;
			}
		});

		changeRout = Routine({inf.do{
			if(changed){
				parent.setInputSliders(sliderVals.select{|item, i| sliderOns[i]==1});
				changed = false;
			};
			0.01.wait;
		}}).play;

		onOffFunctions = Array.fill(numControls, {|i|
			{arg val; parent.setInputButton(i+1, val)}
		});

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true));
		};

		numControls.do{|i|
			controls.add(Button().maxHeight_(15).maxWidth_(20)
				.states_([["Off", Color.black, Color.red], ["On", Color.black, Color.green]])
				.action_({|button|
					sliderOns.put(i, button.value);
			}));
		};

		onOffFunctions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i+(2*numControls), "onOff "++(i+1), func, true));
		};

/*		freezeButton = Button().maxHeight_(15)
		.states_([["Input Through", Color.black, Color.green], ["Input Off", Color.black, Color.red]])
		.action_{|button|
			numControls.do{|i| controls[i].frozen=button.value.asBoolean}
		};*/

		controls.add(QtEZSlider("skip inputs", ControlSpec(1,4,'lin',1), {|slider| modControls = slider.value}, 1, false, 'horz'));

		win.layout_(
			VLayout(
				HLayout(
					VLayout(*controls.copyRange(0, numControls-1)),
					VLayout(*controls.copyRange(numControls, 2*numControls-1)),
					VLayout(*controls.copyRange(2*numControls, 3*numControls-1))
				),
					controls[3*numControls]
			)
		);
		win.layout.spacing_(1).margins_(1!4);
		win.view.maxHeight_(numControls*17);
		win.visible_(false);

	}

	numActiveControls {
		^sliderOns.sum
	}

}