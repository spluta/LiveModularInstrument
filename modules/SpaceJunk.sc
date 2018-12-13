SpaceJunk_Mod : Module_Mod {
	var noteOnFunctions;

	*initClass {
		StartUp.add {

			SynthDef("spaceJunk_mod", {arg outBus, freq=10, vol=1, pan=0, t_trig;
				var dust, noise, sine, smallEnv, out0, out, verb;

				dust = t_trig;

				noise = WhiteNoise.ar(1);

				noise = BPF.ar(noise, TRand.kr(100, 10000, dust), TRand.kr(0.01, 0.1, dust));

				sine = SinOsc.ar(TRand.kr(1000, 2000, dust));

				smallEnv = Decay.kr(dust, LFNoise2.kr(4).range(0.01, 0.05));

				out0 = noise*sine*smallEnv;
				/*out = Pan2.ar(out0, TRand.kr(-1.0, 1.0, dust));*/

				out = Pan4.ar(out0, TRand.kr(-1.0, 1.0, dust), TRand.kr(-1.0, 1.0, dust));

				verb = GVerb.ar(out0*(TRand.kr(0.1, 0.7, dust)**2)*0.25, 80, 3, 0.41, 0.19, 0, -9.dbamp, -11.dbamp);

				out = ((out+([verb, verb].flatten))*16).softclip;

				Out.ar(outBus, Compander.ar(out, out, 0.8, 1, 0.5, 0.01, 0.01));
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("SpaceJunk", Rect(600, 300, 100, 60));
		this.initControlsAndSynths(16);

		synths.add(Synth("spaceJunk_mod", [\outBus, outBus], group));

		controls.add(Button(win, Rect(5, 0, 90, 16))
			.states_([["Assign", Color.red, Color.black], ["Clear", Color.black, Color.red]])
			.action_{arg val;
				if(val.value==1,{
					this.setManta;
				},{
					this.clearMidiOsc;
				})
			}
		);


		noteOnFunctions = IdentityDictionary.new;
		this.addFunctions;


		numChannels = 2;
//		controls.add(Button(win,Rect(10, 65, 60, 20))
//			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
//			.action_{|butt|
//				switch(butt.value,
//					0, {
//						numChannels = 2;
//						3.do{|i| buchlaFilters[i+1].killMe};
//					},
//					1, {
//						numChannels = 4;
//						buchlaFilters.put(1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+2, outBus.index+2));
//					},
//					2, {
//						if(numChannels==2,{
//							3.do{|i| buchlaFilters.put(i+1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+4, outBus.index+(2*(i+1))))};
//						},{
//							2.do{|i| buchlaFilters.put(i+2,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+6, outBus.index+(2*(i+2))))};
//						});
//						numChannels = 8;
//
//					}
//				)
//			};
//		);
	}

	setManta {
		var counter=0;

		noteOnFunctions.keys.do{arg key;
			oscMsgs.put(counter, "/manta/padOn/"++key.asString);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], noteOnFunctions[key], setups);
			counter=counter+1;
		};

	}

	addFunctions {
		16.do{arg i;
			noteOnFunctions.put(32+i,
					{
						synths[0].set(\t_trig, 1);
					});
		};
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);  //controller messages
		//this does not save or load the oscMsgs. that is why it is overridden

		saveArray.add(win.bounds);

		this.saveExtra(saveArray);
		^saveArray
	}

	load {arg loadArray;
		loadArray[1].do{arg controlLevel, i;
			if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
		};
		win.bounds_(loadArray[3]);
		this.loadExtra(loadArray);
	}

	killMeSpecial {
		group.freeAllMsg;
	}
}