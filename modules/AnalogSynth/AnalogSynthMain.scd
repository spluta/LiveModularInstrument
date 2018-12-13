AnalogSynth_Mod : Module_Mod {
	//in order to make this function with audio inputs, I probably need to inherit from SignalSwitcher_Mod

	var popUps, analogSynths;

	*initClass {
		StartUp.add {

		}
	}

	init {

		this.makeWindow("AnalogSynth",Rect(500, 500, 180, 150));
		this.initControlsAndSynths(44);

		mixerInGroup = Group.tail(group);
		synthGroup = Group.tail(group);
		mixerOutGroup = Group.tail(group);

		localBusses = List.new;
		64.do{localBusses.add(Bus.audio(group.server, 1))};

		/*		mixerStrips = List.new;
		4.do{arg i;
		mixerStrips.add(ModularMixerStrip(mixerGroup, localBusses[i], setups));
		mixerStrips[i].init2(win, Point(5+(i*55), 0), nil);
		};*/

		synths.add(Synth("analogMainMixer_analogMod", [\inBus0, localBusses[60], \inBus1, localBusses[61],\inBus2, localBusses[62], \inBus3, localBusses[63], \outBus, outBus], mixerOutGroup));

		analogSynths = List.newClear(0);
		popUps = List.newClear(0);
		visibleButtons = List.newClear(0);

		20.do{arg i;
			var temp;

			analogSynths.add(nil);

			temp = PopUpMenu();
			temp.items = ["nil", "Maths", "Oscillator", "ResonantFilter", "RingMod"];
			temp.action = {arg menu;
				this.makeNewSynth(i, menu.item);
			};
			popUps.add(temp);

			temp = Button();
			temp.states = [["visible", Color.black, Color.green], ["hidden", Color.black, Color.red]];
			temp.action = {arg button;
				if(button.value==1, {analogSynths[i].setVisible(false)},{analogSynths[i].setVisible(true)})
			};
			visibleButtons.add(temp);
		};

		//make the volume faders

		controls.addAll(popUps).addAll(visibleButtons).addAll(volumeFaders);

		win.layout = HLayout(
			VLayout(*popUps), VLayout(*visibleButtons)
		)
		win.front;
	}

	killCurrentSynth {arg newSynthName;
	}

	makeNewSynth {arg analogSynthIndex, newSynthName;
		if(analogSynths[analogSynthIndex]!=nil,{
			analogSynths[analogSynthIndex].killMe;
		});
		if (newSynthName!="nil", {
			analogSynths.put(analogSynthIndex, ModularClassList.initAnalogSynthModule(synthName, synthGroup, outBus, setups));
		},{
			analogSynths.put(analogSynthIndex, nil);
		});
	}
}

}