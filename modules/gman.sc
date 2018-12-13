GingerMan_Mod : Module_Mod {
	var synthGroup, filterGroup, buchlaFilters, transferBus, sinTemp0, sinTemp1, gmanTemp0, gmanTemp1, getSin, getGman, spec, noteOnFunctions, sliderFunctions, startTime, saveButton;

	*initClass {
		StartUp.add {

			SynthDef("gman",{arg sinFreq0, sinFreq1, gmanFreq0, gmanFreq1, dustFreq0, dustFreq1, dustFreq2, dustFreq3, dustFreq4, dustFreq5, selectFAM2=0, outBus, onOff = 0, vol = 0, gate=1, pauseGate = 1;
				var trig0, trig1, trig2, sinFreq, gmanFreq, out, fastAmpMod, slowAmpMod, fastAmpMod2, ampMod, pauseEnv, env;


				sinFreq = LFNoise0.kr(LFNoise0.kr(2).range(0.1, 8)).range(sinFreq0, sinFreq1);
				gmanFreq = LFNoise0.kr(LFNoise0.kr(5).range(0.1, 8)).range(gmanFreq0, gmanFreq1);

				trig0 = Dust.kr(LFNoise0.kr(2).range(dustFreq0, dustFreq1));
				fastAmpMod = Trig1.kr(trig0, TRand.kr(0.015,0.4,trig0));

				trig1 = Dust.kr(LFNoise0.kr(0.5).range(dustFreq2, dustFreq3));
				slowAmpMod = Lag.kr(1-Trig1.kr(trig1, TRand.kr(1.5/(dustFreq2+0.001*2), 1.5/(dustFreq3+0.001*2),trig1)), 0.001);

				trig2 = Dust.kr(LFNoise0.kr(0.5).range(dustFreq4, dustFreq5));
				fastAmpMod2 = Select.kr(selectFAM2, [1, Lag.kr(Trig1.kr(trig2, TRand.kr(0.01,0.05,trig2)), 0.001)]);

				ampMod = fastAmpMod*slowAmpMod*fastAmpMod2*Lag.kr(onOff, 0.001);

				out = SinOsc.ar(sinFreq, GbmanN.ar(gmanFreq) ,vol*ampMod).dup;

				//SendTrig.kr(Trig1.kr(ampMod, 0.01), 1, 1);
				//SendTrig.kr(Trig1.kr((1-ampMod).abs, 0.01), 0, 0);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				Out.ar(outBus, [out[0], Delay2.ar(out[1])].distort*pauseEnv*env);

				Out.ar(outBus+2, [Delay2.ar(out[0]), out[1]].distort*pauseEnv*env);
				Out.ar(outBus+4, [out[0], Delay2.ar(out[1])].distort*pauseEnv*env);
				Out.ar(outBus+6, [Delay2.ar(out[0]), out[1]].distort*pauseEnv*env);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GingerMan", Rect(600, 300, 200, 60));
		this.initControlsAndSynths(40);

		synthGroup = Group.tail(group);
		filterGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 8);

		synths.add(Synth("gman", [\sinFreq0, 20, \sinFreq1, 20000, \gmanFreq0, 20, \gmanFreq1, 20000, \dustFreq0, 35, \dustFreq1, 40, \dustFreq2, 0, \dustFreq3, 0, \outBus, transferBus.index], synthGroup));

		buchlaFilters = List.newClear(4);
		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));

		controls.add(Button()
			.states_([["Assign", Color.red, Color.black], ["Clear", Color.black, Color.red]])
			.action_{arg val;
				if(val.value==1,{
					this.setManta;
				},{
					this.clearMidiOsc;
				})
			}
		);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,'amp'),
			{|slider| synths[0].set(\vol, slider.value)}, 1, true, \horz)
		);
		this.addAssignButton(1,\continuous);

		win.layout_(VLayout(controls[0], HLayout(controls[1].layout), assignButtons[1].layout));
		win.layout.spacing = 0;

		noteOnFunctions = IdentityDictionary.new;
		sliderFunctions = IdentityDictionary.new;
		this.addFunctions;
	}

	setManta {
		var counter=0;

		noteOnFunctions.keys.do{arg key;
			var key2;

			key2 = "/SeaboardNote/"++(key).asString;
			oscMsgs.put(counter, key2);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], noteOnFunctions[key], setups);
			counter=counter+1;
		};

		oscMsgs.put(counter, "/SeaboardNote/31");
		MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter],
			{|val| if(val==1, {synths[0].set(\onOff, 0)})}, setups);
		counter=counter+1;

		oscMsgs.put(counter, "/SeaboardNote/30");
		MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter],
			{|val| if(val==1, {synths[0].set(\onOff, 1)})}, setups);

	}


	addFunctions {
		4.do{arg gmanCount;
			6.do{arg sinCount;
				noteOnFunctions.put(((gmanCount*8)+(sinCount)), {|val|
					if(val==1,{
						this.getSin(sinCount);
						this.getGman(gmanCount);
						synths[0].set(\sinFreq0, sinTemp0, \sinFreq1, sinTemp1, \gmanFreq0, gmanTemp0, \gmanFreq1, gmanTemp1);
						buchlaFilters.do{arg item; if(item!=nil,{item.trigger})};
					})
				})
			}
		};

		spec = ControlSpec(0,1,\amp);
	}

	getSin  {arg num;
		switch(num,
			0,{
				sinTemp0 = exprand(20, 80);
				sinTemp1 = sinTemp0+40.linrand;
			},
			1,{
				sinTemp0 = exprand(80, 320);
				sinTemp1 = sinTemp0+180.linrand;
			},
			2,{
				sinTemp0 = exprand(320, 1100);
				sinTemp1 = sinTemp0+579.linrand;
			},
			3,{
				sinTemp0 = exprand(1100, 4000);
				sinTemp1 = sinTemp0+2700.linrand;
			},
			4,{
				sinTemp0 = exprand(4000, 10000);
				sinTemp1 = sinTemp0+7777.linrand;
			},
			5,{
				sinTemp0 = exprand(10000, 20000);
				sinTemp1 = sinTemp0+5000.linrand;
			}
		)
	}

	getGman  {arg num;
		switch(num,
			0,{
				gmanTemp0 = exprand(20, 80);
				gmanTemp1 = gmanTemp0+40.linrand;
			},
			1,{
				gmanTemp0 = exprand(80, 2000);
				gmanTemp1 = gmanTemp0+180.linrand;
			},
			2,{
				gmanTemp0 = exprand(2000, 11000);
				gmanTemp1 = gmanTemp0+2700.linrand;
			},
			3,{
				gmanTemp0 = exprand(11000, 20000);
				gmanTemp1 = gmanTemp0+5000.linrand;
			},
			4,{
				gmanTemp0 = exprand(4000, 10000);
				gmanTemp1 = gmanTemp0+7777.linrand;
			},
			5,{
				gmanTemp0 = exprand(10000, 20000);
				gmanTemp1 = gmanTemp0+5000.linrand;
			}
		)
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);

		temp = List.newClear(0);
			temp.add(oscMsgs[1]);
		saveArray.add(temp);
		//this does not save or load the oscMsgs
		//it takes advantage that the setManta button is a control button
		//and it just saves the state of that button

		saveArray.add(win.bounds);
		^saveArray
	}

	load {arg loadArray;
		loadArray[1].do{arg controlLevel, i;
			if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
		};
/*		loadArray[2].do{arg msg, i;
			waitForSetNum = i;
			if(msg!=nil,{
				MidiOscControl.getFunctionNSetController(this, controls[i+1], msg, group.server, setups);
				assignButtons[i+1].instantButton.value_(1);
			})
		};
		win.bounds_(loadArray[3]);*/
	}

	killMeSpecial {
		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
		transferBus.free;
		group.freeAllMsg;
	}
}