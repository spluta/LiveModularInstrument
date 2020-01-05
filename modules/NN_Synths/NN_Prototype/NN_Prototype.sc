PrototypeElement {
	var <>action, <>msgBox, <>lowBox, <>hiBox, <>warpBox, <>onOffButton, valBox, container, <>controlSpec, <>msg, <>low, <>high, <>warp, <value, layout, <>nnVals, <>location, <>parent;

	*new {

		^super.new.init;
	}

	init {

		controlSpec = ControlSpec();

		msgBox = TextField();
		msgBox.string = 'msg';
		msgBox.maxHeight_(15).maxWidth_(120).font_(Font("Helvetica", 10));
		msgBox.action = {arg val;
			msg = val.value.asSymbol;
			nnVals.put(location, [msg.asString, nil]);
		};

		lowBox = NumberBox().decimals_(3);
		lowBox.value_(0);
		lowBox.maxHeight_(15).maxWidth_(60).font_(Font("Helvetica", 10));
		lowBox.action = {arg val;
			try {
				controlSpec.minval_(val.value);
				low = val.value;
			}
		};

		hiBox = NumberBox().decimals_(3);
		hiBox.value_(1);
		hiBox.maxHeight_(15).maxWidth_(60).font_(Font("Helvetica", 10));
		hiBox.action = {arg val;
			try {
				controlSpec.maxval_(val.value);
				high = val.value;
			}
		};

		warpBox = PopUpMenu().maxHeight_(15).maxWidth_(60).font_(Font("Helvetica", 10))
		.items_(["lin", "exp", "amp"])
		.action_{|pop|
			warp = pop.value;
			switch(pop.value,
				0, {controlSpec.warp_('lin')},
				1, {controlSpec.warp_('exp')},
				2, {controlSpec.warp_('amp')});
		};

		valBox = NumberBox().maxHeight_(15).maxWidth_(120).font_(Font("Helvetica", 10))
		.value_(0);
		value = 0;

		onOffButton = Button().maxHeight_(15).maxWidth_(20)
		.states_([["Off", Color.black, Color.red], ["On", Color.black, Color.green]])
		.action_({|button|
			if(button.value==1){parent.increaseNNSize}{parent.decreaseNNSize};
		});

		layout = HLayout(msgBox, lowBox, hiBox, warpBox, valBox, onOffButton);

	}

	asView {^layout}

	exportModel {

	}

	value_ { arg val;
		value = controlSpec.constrain(val);
		{
			valBox.value = value.round(0.001);
		}.defer;
	}

	doAction { action.value(this) }

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}
}

NN_Prototype_NNMod : NN_Synth_Mod {
	var synthNameBox, reloadSynthButton, synthName;
	var otherValsBusses, onOffBus, envOnOffBus, numControls, exportButton;

	setAllVals {

		allValsList = List.fill(8, {List.fill(20, {0})});
	}

	init {

		this.makeWindow("NN_Prototype", Rect(0, 0, 200, 40));

		numModels = 8;
		numControls = 20;
		sizeOfNN = 0;

		this.initControlsAndSynths(numControls);

		nnVals = List.fill(numControls, {|i| [nil, nil]});

		dontLoadControls = (0..(numControls-1));
	}

	init2 {arg parentIn, otherValsBussesIn, onOffBusIn, envOnOffBusIn;
		synths.add(nil);
		otherValsBusses = otherValsBussesIn;
		onOffBus = onOffBusIn;
		envOnOffBus = envOnOffBusIn;
		this.init_window(parentIn);
	}

	createWindow {
		synthNameBox = TextField().maxHeight_(15).font_(Font("Helvetica", 10))
		.string_("NotAName")
		.action_{arg field;
			synthName = field.value;
		};
		synthName = "NotAName";

		exportButton = Button()
		.states_([["export", Color.black, Color.yellow]])
		.action_{
			controls.collect{|item|
				[item.msg, item.controlSpec]}.asCompileString.postln;
		};

		reloadSynthButton = Button()
		.states_([["reloadSynth", Color.black, Color.yellow]])
		.action_{
			if(synths[0]!=nil, {synths[0].set(\gate, 0)});

			synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, otherValsBusses[0].index, \envRiseBus, otherValsBusses[1].index, \envFallBus, otherValsBusses[2].index, \onOffBus, onOffBus.postln, \envOnOffBus, envOnOffBus], group));
		};

		numControls.do{arg item, i;
			controls.add(
				PrototypeElement()
				.nnVals_(nnVals).location_(i).parent_(this)
				.action_{arg proto;
					synths[0].set(nnVals[i][0].asSymbol, proto.value);
					{valList.put(i, proto.controlSpec.unmap(proto.value))}.defer;
				}
			)
		};

		win.layout = VLayout(
			HLayout(synthNameBox, reloadSynthButton),
			VLayout(*controls),
			exportButton
		);
		win.layout.spacing_(0).margins_(0!4);
	}

	increaseNNSize {
		sizeOfNN = sizeOfNN+1;
		valList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
	}

	decreaseNNSize {
		sizeOfNN = sizeOfNN-1;
		valList = List.fill(sizeOfNN, {0});
		allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
	}


	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(synthName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add([item.msg, item.low, item.high, item.warp, item.onOffButton.value])
		};

		saveArray.add(temp);

		^saveArray
	}

	load {|loadArray|
		var tempNNSize=0;
		"loadArray".postln;
		loadArray.postln;
		if(loadArray!=nil,{
			synthNameBox.valueAction_(loadArray[0].asString);
			synths.put(0, Synth(synthName, [\outBus, outBus, \volBus, otherValsBusses[0].index, \envRiseBus, otherValsBusses[1].index, \envFallBus, otherValsBusses[2].index, \onOffBus, onOffBus, \envOnOffBus, envOnOffBus], group));
			loadArray[1].do{|item, i|
				item.postln;
				if(item[0]!=nil){controls[i].msgBox.valueAction_(item[0].asString)};
				if(item[1]!=nil){controls[i].lowBox.valueAction_(item[1].asFloat)};
				if(item[2]!=nil){controls[i].hiBox.valueAction_(item[2].asFloat)};
				try {
					if(item[3]!=nil){controls[i].warpBox.valueAction_(item[3].asInteger)};
				};
				try {
					if(item[4]!=nil){
						controls[i].onOffButton.value_(item[4].asInteger);
						if(item[4]==1, {tempNNSize=tempNNSize+1});
					};
				}
			};
			sizeOfNN = tempNNSize;
			valList = List.fill(sizeOfNN, {0});
			allValsList = List.fill(numModels, List.fill(sizeOfNN, {0}));
		})
	}
}