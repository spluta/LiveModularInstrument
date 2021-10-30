ProtoType_Mod :  Module_Mod {
	var goButton, controlsBus, exportButton, withTextView = true, numControls = 10;

	init {
		this.init2;
	}

	init2 {
		this.initControlsAndSynths(numControls+2);
		this.makeMixerToSynthBus(2);

		controlsBus = Bus.control(group.server, numControls);

		goButton = Button()
		.states_([["go", Color.grey, Color.white]])
		.action_{arg butt;
			group.freeAll;
			try {
				var def, synth, synthMsg;
				def = ("{arg inBus, controlsBus, outBus;"
					++controls[0].string++"}"
				).interpret
				.asSynthDef(
					fadeTime: 0.02,
					name: SystemSynthDefs.generateTempName
				);
				synth = Synth.basicNew(def.name, group.server);
				synthMsg = synth.newMsg(group.asTarget, [\inBus, mixerToSynthBus, \controlsBus, controlsBus, \outBus, outBus]);
				def.doSend(group.server, synthMsg);
			}
		};

		if(withTextView){
			controls.add(MyTextView().string_(""));
		};

		numControls.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i+1, "text",
				{arg val; controlsBus.setAt(i, val)},
				true, false));
		};

		exportName = TextField();

		exportButton = Button()
		.states_([["export", Color.blue, Color.white]])
		.action_{arg butt;
			var text = exportName.value++" : ProtoType_Mod {

*initClass {
StartUp.add {
SynthDef("++($\\)++exportName.copyRange(0,exportName.size-5)",{arg inBus, controlsBus, outBus;"
			++controls[0].string++"}).writeDefFile;


init {
withTextView = false;
this.init2;
}
"
			var file = File.new(Document.current.dir++exportName++".sc", "w");
			file.write(text);
			file.close;
		};

		this.makeWindow2;
	}

	makeWindow2 {
		var temp;

		temp = this.class.asString;
		this.makeWindow(temp.copyRange(0, temp.size-5));

		if(withTextView){

		win.layout_(
			VLayout(
				goButton,controls[0],
				VLayout(*controls.copyRange(1,numControls).collect({arg item; item})),
				exportButton
			)
		);
			win.layout.spacing_(1).margins_(1!4);
		win.view.resizeTo(10*17,numControls+15*17);
		}{
			win.layout_(
				VLayout(
					VLayout(*controls.copyRange(0,numControls-1).collect({arg item; item}))
				)
			);
			win.layout.spacing_(1).margins_(1!4);
			win.view.resizeTo(10*17,numControls*17);
		};


		win.front;
	}

	loadExtra {
		goButton.valueAction_(1);
	}
}