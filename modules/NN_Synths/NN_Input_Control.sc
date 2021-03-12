NN_Input_Control_NNMod :  TypeOSCModule_Mod {
	var texts, functions, onOffFunctions, <>parent, <>msgsPerSec=100, <>ctrlSteps=128, labels, numControls, button0, button1, button2, loBox, hiBox, sliderVals, sliderOns, freezeButton, changed, changeRout, lastVals, counter = 0, sliderVals2;

	init {

		this.makeWindow("NN_Input_Control");

		numControls = 10;

		this.initControlsAndSynths(numControls*3+2);

		sliderVals = (0!numControls).asList;
		sliderOns = (0!numControls).asList;

		lastVals = (0!numControls).asList;

		texts = Array.fill(numControls, {|i| "slider"+(i+1).asString}).addAll(numControls, {|i| "onOff"+(i+1).asString});

		changed = false;

		functions = Array.fill(numControls, {|i|
			{arg val;
				if(ctrlSteps<=4096) {
					if(lastVals[i]<val)
					{val = (val-(1/(ctrlSteps)).rand).clip(0.0,1.0);}
					{val = (val+(1/(ctrlSteps)).rand).clip(0.0,1.0);}
				};
				lastVals.put(i, val);
				sliderVals.put(i, val);

				sliderVals2 = sliderVals.select{|item, i| sliderOns[i]==1};
				if(sliderVals2.size>0){parent.setInputSliders(sliderVals2)};

				//changed = true;
			}
		});

/*		changeRout = Routine({inf.do{
			if(changed){
				sliderVals2 = sliderVals.select{|item, i| sliderOns[i]==1};
				if(sliderVals2.size>0){parent.setInputSliders(sliderVals2)};
				changed = false;
			};
			(1/msgsPerSec).wait;
		}}).play;*/

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

		controls.add(QtEZSlider("messages per sec", ControlSpec(20,200,'lin',1), {|slider| msgsPerSec = slider.value}, 100, true, 'horz'));

		controls.add(QtEZSlider("controller steps", ControlSpec(128, 4096+128,'lin',128), {|slider| ctrlSteps = slider.value}, 100, true, 'horz'));

		win.layout_(
			VLayout(
				HLayout(
					VLayout(*controls.copyRange(0, numControls-1)),
					VLayout(*controls.copyRange(numControls, 2*numControls-1)),
					VLayout(*controls.copyRange(2*numControls, 3*numControls-1))
				),
					controls[3*numControls],controls[3*numControls+1]
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