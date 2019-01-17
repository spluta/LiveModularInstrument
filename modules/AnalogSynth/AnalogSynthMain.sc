AnalogSynth_Mod : Module_Mod {
	//in order to make this function with audio inputs, I probably need to inherit from SignalSwitcher_Mod
	var mixerInGroup, synthGroup, mixerOutGroup, localBusses;
	var popUps, visibleButtons, analogSynths, volumeFaders, layerButtons;
	var usedCables, views, currentLayer, stack;
	var <>usedCables, <>inOutMenus, cablesText, <>lastCableAttached;

	*initClass {
		StartUp.add {
			SynthDef("analogMainMixer_analogMod", {
				arg inBus0, inBus1, inBus2, inBus3, vol0=0, vol1=0, vol2=0, vol3=0, pan0 = 0, pan1 = 0, pan2 = 0, pan3 = 0, outBus, gate=1, pauseGate = 1, localPauseGate = 1;
				var env, pauseEnv, localPauseEnv;

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.05,1,0.01), pauseGate, doneAction:1);
				localPauseEnv = EnvGen.kr(Env.asr(0.05,1,0.05), pauseGate, doneAction:1);

				Out.ar(outBus, Pan2.ar(LPF.ar(LeakDC.ar(In.ar(inBus0))*vol0, SampleRate.ir/2), pan0)*env*pauseEnv*localPauseEnv);
				Out.ar(outBus, Pan2.ar(LPF.ar(LeakDC.ar(In.ar(inBus1))*vol1, SampleRate.ir/2), pan1)*env*pauseEnv*localPauseEnv);
				Out.ar(outBus, Pan2.ar(LPF.ar(LeakDC.ar(In.ar(inBus2))*vol2, SampleRate.ir/2), pan2)*env*pauseEnv*localPauseEnv);
				Out.ar(outBus, Pan2.ar(LPF.ar(LeakDC.ar(In.ar(inBus3))*vol3, SampleRate.ir/2), pan3)*env*pauseEnv*localPauseEnv);
			}).writeDefFile;
		}

	}

	init {

		this.makeWindow("AnalogSynth",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(440);

		usedCables = Bag.new(0);
		inOutMenus = List.newClear(0);
		lastCableAttached = 0;

		mixerInGroup = Group.tail(group);
		synthGroup = Group.tail(group);
		mixerOutGroup = Group.tail(group);

		localBusses = List.new;

		//busses 61-64 are the out busses, bus 0 is a null bus that all busses are linked to when they are not active, bus 65 is a garbage out bus where all data goes that is not being used
		66.do{localBusses.add(Bus.audio(group.server, 1))};

		analogSynths = Array.fill(10, {List.newClear(20)});
		popUps = Array.fill(10, {List.newClear(0)});
		visibleButtons = Array.fill(10, {List.newClear(0)});
		views = List.newClear(0);
		volumeFaders = Array.fill(10, {List.newClear(0)});
		layerButtons = List.newClear(0);

		currentLayer = 0;

		10.do{arg layerNum;

			synths.add(Synth("analogMainMixer_analogMod", [\inBus0, localBusses[61], \inBus1, localBusses[62],\inBus2, localBusses[63], \inBus3, localBusses[64], \outBus, outBus], mixerOutGroup));

			layerButtons.add(Button().maxWidth_(30)
				.states_([["", Color.black, Color.black],["",Color.yellow, Color.yellow]])
				.action_({arg butt;
					layerButtons.do{arg item; item.value=0};
					layerButtons[layerNum].value=1;
					if(currentLayer!=layerNum, {
						synths[currentLayer].set(\localPauseGate, 0);
						synths[layerNum].set(\localPauseGate, 1);

						analogSynths[layerNum].do{arg item; if (item!=nil, {item.localPlayPause(1)})};
						analogSynths[currentLayer].do{arg item; if(item!=nil, {item.localPlayPause(0)})};

						layerButtons[currentLayer].value_(0);
						currentLayer = layerNum;
						stack.index = layerNum;
					});
				})
			);

			20.do{arg i;
				var temp;

				temp = PopUpMenu();
				temp.items = ["nil", "Maths", "RandoCalrissian", "ComplexOscillator", "DualADSR", "VCA", "VCF", "Sequencer"];
				temp.action = {arg menu;
					this.makeNewSynth(layerNum, i, menu.item);
				};
				popUps[layerNum].add(temp);

				temp = Button();
				temp.states = [["visible", Color.black, Color.green], ["hidden", Color.black, Color.red]];
				temp.action = {arg button;
					if(button.value==1, {analogSynths[layerNum][i].hide},{analogSynths[layerNum][i].show})
				};
				visibleButtons[layerNum].add(temp);
			};


			4.do{arg i;
				volumeFaders[layerNum].add(QtEZSlider((61+i).asString, ControlSpec(0,1,'amp'), {arg val; synths[0].set(("vol"++i.asString).asSymbol, val.value)}, 0, true, 'horz'));
				volumeFaders[layerNum].add(QtEZSlider("pan", ControlSpec(-1,1,'lin'), {arg val; synths[0].set(("pan"++i.asString).asSymbol, val.value)}, 0, true, 'horz'));
			};
			volumeFaders[layerNum].do{arg item;
				item.slider.minWidth_(100);
				item.numBox.minWidth_(40);
			};

			controls.addAll(popUps[layerNum]).addAll(visibleButtons[layerNum]).addAll(volumeFaders[layerNum]);

			views.add(View().layout_(HLayout(
				VLayout(*popUps[layerNum].select({arg item, i; i<10})),
				VLayout(*visibleButtons[layerNum].select({arg item, i; i<10})),
				VLayout(*popUps[layerNum].select({arg item, i; i>=10})),
				VLayout(*visibleButtons[layerNum].select({arg item, i; i>=10})),
				VLayout(*volumeFaders[layerNum].collect({arg item; item.layout}))
			)));
		};

		stack = StackLayout(*views);
		stack.index = 0;

		cablesText = StaticText();

		win.layout = VLayout(HLayout(*layerButtons), stack, cablesText);
		win.front;
	}

	attachCable {arg num;
		if(num!=0, {
			usedCables.add(num);
			lastCableAttached = num;
			cablesText.string = usedCables.contents.keys.asArray.sort;
			analogSynths.flatten.do{arg item;
				if(item!=nil,{
					if(item.plugs!=nil,{
						item.plugs.do{arg plug;
							if(plug.currentValue==num, {
								var color;
								color = plug.background;
								plug.background = Color.red;
								AppClock.sched(2.0,{plug.background = plug.color});
				})}})})
			};
		});
	}

	getNextCable {
		var counter, cable;

		counter = 1;

		while (
			{(cable==nil)&&(counter<(localBusses.size-6))},
			{
				if(usedCables.includes(counter),{
					counter = counter+1;
				},{
					cable = counter;
				})
			}
		);
		^cable
	}

	detachCable {arg num;
		usedCables.remove(num);
		cablesText.string = usedCables.contents.keys.asArray.sort;
	}

	makeNewSynth {arg layerNum, analogSynthIndex, newSynthName;
		if(analogSynths[layerNum][analogSynthIndex]!=nil,{
			analogSynths[layerNum][analogSynthIndex].killMe;
		});
		if (newSynthName!="nil", {
			analogSynths[layerNum].put(analogSynthIndex, ModularClassList.initAnalogSynthModule(newSynthName, synthGroup, localBusses));
			analogSynths[layerNum][analogSynthIndex].parent_(this);
		},{
			analogSynths[layerNum].put(analogSynthIndex, nil);
		});
	}

	saveExtra {arg saveArray;
		var tempArray;

		tempArray = Array.fill(10, {List.newClear});
		analogSynths.do{arg synthLayer, bigI;

			synthLayer.do{arg item, i;
				if (item.value!=nil, {
					tempArray[bigI].add(item.save);
				},{
					tempArray[bigI].add(nil)
				});
			}
		};
		saveArray.add(tempArray);
	}

	loadExtra {arg loadArray;
		loadArray[4].do{arg synthLayer, bigI;
			synthLayer.do{arg item, i;
				if(item!=nil,{
					analogSynths[bigI][i].load(item);
					if(visibleButtons[bigI][i].value==0, {analogSynths[bigI][i].show});
				});
			}
		}
	}

	killMeSpecial {
		analogSynths.do{arg synthLayer;
			synthLayer.do{arg item; if(item!=nil,{item.killMe})}
		}
	}
}

AnalogModule_Mod : Module_Mod {
	var <>localBusses, <>garbageBus, temp0, temp1, texts, <>parent, <>plugs;

	localPlayPause {arg val;
		if(val==0,{
			synths.do{arg item; item.set(\localPauseGate, val)};
			this.hide;
		},{
			synths.do{arg item; item.set(\localPauseGate, val); item.run(true)};
			this.show;
		});
	}

	initAnalogBusses {
		localBusses = outBus;
		garbageBus = localBusses[localBusses.size-1]; //the last bus is the garbageBus
	}

	makePlugIn {arg synth, busName, pluggedName;
		var box;

		if(plugs==nil,{plugs = List.newClear(0)});

		box = NumberBox_Mod().clipLo_(-2).clipHi_(localBusses.size-6).action_({arg numb;
			var temp;

			if(numb.value==(-1), {
				temp = box.valueAction_(parent.getNextCable)
			},{
				if(numb.value==(-2), {
					temp = box.valueAction_(parent.lastCableAttached)
				},{
					temp0 = numb.value;
					if(numb.value == 0, {temp1 = 0},{temp1 = 1});
					synth.set(busName, localBusses[temp0], pluggedName, temp1);
					numb.alpha_(linlin(0,localBusses.size-6,1,0));

					parent.detachCable(numb.currentValue);
					numb.currentValue = numb.value;
					parent.attachCable(numb.value);
				})
			})
		}).maxWidth_(50).minWidth_(30).background_(Color.new255(50, 205, 50)).color_(Color.new255(50, 205, 50)).step_(1);

		controls.add(box);
		plugs.add(box);
	}

	makePlugOut {arg synth, busName;
		if(plugs==nil,{plugs = List.newClear(0)});

		controls.add(NumberBox_Mod().clipLo_(0).clipHi_(localBusses.size-2).action_({arg numb;
			temp0 = numb.value;
			if(temp0==0, {synth.set(busName, garbageBus)},{synth.set(busName, localBusses[temp0])});
			numb.alpha_(linlin(0,localBusses.size-6,1,0));

			parent.detachCable(numb.currentValue);
			numb.currentValue = numb.value;
			parent.attachCable(numb.value);
		}).maxWidth_(50).minWidth_(30).background_(Color.yellow)/*.color_(Color.yellow)*/.step_(1));
	}

}

NumberBox_Mod : NumberBox {
	var <>currentValue=0, <>color;
}
