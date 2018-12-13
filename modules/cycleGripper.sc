CycleGripper_Mod : Module_Mod {
	var lastValue, seqs, repeatSeq, numPlays, countToPlays, tempList;

	*initClass {
		StartUp.add {
			SynthDef("cycleGripper_mod", {arg inBus, outBus, trigRateDust=0, trigRateImpulse=0, mode=0, inDelay = 0.02, t_trig = 0, gate = 1, pauseGate = 1;				var trig, div0, div1, switch0, switch1, source, local, delay, delayTime;
				var triga, div0a, div1a, switch0a, switch1a, env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0.01,1,0.01), pauseGate, doneAction:1);

				trig = Dust.kr(trigRateDust) + Impulse.kr(trigRateImpulse);

				div0 = PulseDivider.kr(trig, 2, 0);
				div1 = PulseDivider.kr(trig, 2, 1);
				switch0 = SetResetFF.kr(div0,div1);
				switch1 = SetResetFF.kr(div1,div0);

				div0a = Trig.kr(t_trig, 0.01);
				div1a = Trig.kr(TDelay.kr(t_trig, inDelay), 0.01);
				switch0a = SetResetFF.kr(div0a,div1a);
				switch1a = SetResetFF.kr(div1a,div0a);

				switch0 = Select.kr(mode, [switch0, switch0a, 1]);
				switch1 = Select.kr(mode, [switch1, switch1a, 0]);

				source = In.ar(inBus, 2);

				delayTime = Select.kr(mode, [TRand.kr(64/44100, 1024/44100, trig), inDelay, inDelay]);

				delay = DelayN.ar(LocalIn.ar(2), 8192/44100, delayTime);

				delay = Compander.ar((switch1*delay), (switch1*delay), 1, 1, 0.5, 0.01, 0.01).distort.clip2(0.8);
				//delay = (delay+PitchShift.ar(delay, 0.02, TRand.kr(0.9, 1.1, switch1), 0.01, 0));

				local = Mix.new([(switch0*source),delay]);

				LocalOut.ar(local.reverse*1.2);

				Out.ar(outBus, local*env*pauseEnv);
			}).writeDefFile;
		}
	}

	getDelay {arg num;
		numPlays.put(num, numPlays[num]+1);
		if(numPlays[num]>countToPlays[num], {
			numPlays.put(num, 0);
			countToPlays.put(num, rrand(12,17));
			tempList = List.newClear;
			rrand(3,5).do{tempList.add(rrand(2**(9+num), 2**(10+num))/44100)};
			seqs.put(num, Pseq.new(tempList, inf).asStream);
		});
		lastValue.put(num, seqs[num].next);
		^lastValue[num];
	}

	init {
		this.makeWindow("CycleGripper", Rect(10,10,10,10));
		this.initControlsAndSynths(9);

		this.makeMixerToSynthBus(8);

		synths = List.newClear(4);
		synths.put(0, Synth("cycleGripper_mod", [\inBus, mixerToSynthBus.index, \outBus, outBus, \trigRateDust, 0, \trigRateImpulse, 0, \mode, 2], group));

		seqs = List.newClear(0);
		5.do{arg i; seqs.add(Pseq.new(#[0.1], inf).asStream)};
		lastValue = List[seqs[0].next, seqs[1].next, seqs[2].next, seqs[3].next, seqs[4].next];
		numPlays = List[0,0,0,0,0];
		countToPlays = List[0,0,0,0,0];
		repeatSeq = 0;

		controls.add(Button.new()
			.states_([["allOff", Color.red, Color.black ], ["allOff", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(0);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 2);
					})
				}
			});
		this.addAssignButton(0, \onOff);

		controls.add(Button.new()
			.states_([["man0", Color.red, Color.black ], ["man0", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(1);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 1);
						item.set(\inDelay, this.getDelay(0));
						item.set(\t_trig, 1);
					})
				}
			});
		this.addAssignButton(1, \onOff);

		controls.add(Button.new()
			.states_([["man1", Color.red, Color.black ], ["man1", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(2);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 1);
						item.set(\inDelay, this.getDelay(1));
						item.set(\t_trig, 1);
					})
				}
			});
		this.addAssignButton(2, \onOff);

		controls.add(Button.new()
			.states_([["man2", Color.red, Color.black ], ["man2", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(3);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 1);
						item.set(\inDelay, this.getDelay(2));
						item.set(\t_trig, 1);
					})
				}
			});
		this.addAssignButton(3, \onOff);

		controls.add(Button.new()
			.states_([["man3", Color.red, Color.black ], ["man3", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(4);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 1);
						item.set(\inDelay, this.getDelay(3));
						item.set(\t_trig, 1);
					})
				}
			});
		this.addAssignButton(4, \onOff);

		controls.add(Button.new()
			.states_([["man4", Color.red, Color.black ], ["man4", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(5);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateDust, 0);
						item.set(\trigRateImpulse, 0);
						item.set(\mode, 1);
						item.set(\inDelay, this.getDelay(4));
						item.set(\t_trig, 1);
					})
				}
			});
		this.addAssignButton(5, \onOff);

		controls.add(Button.new()
			.states_([["Dust", Color.red, Color.black ], ["Dust", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(6);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateImpulse, rrand(5,12));
						item.set(\trigRateDust,rrand(5,25));
						item.set(\mode, 0);
					})
				}
			});
		this.addAssignButton(6, \onOff);

		controls.add(Button.new()
			.states_([["Impulse", Color.red, Color.black ], ["Impulse", Color.black, Color.blue ]])
			.action_{|v|
				this.setDaButtons(7);
				synths.do{arg item;
					if(item!=nil,{
						item.set(\trigRateImpulse, rrand(5,25));
						item.set(\trigRateDust, 0);
						item.set(\mode, 0);
					})
				}
			});
		this.addAssignButton(7, \onOff);

		//multichannel button
		numChannels = 2;
		controls.add(Button()
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| synths[i+1].set(\gate, 0)};
					},
					1, {
						synths.put(1, Synth("cycleGripper_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \trigRateDust, 0, \trigRateImpulse, 0, \mode, 2], group));
						numChannels = 4;
					},
					2, {
						if(numChannels==2,{
							synths.put(1, Synth("cycleGripper_mod", [\inBus, mixerToSynthBus.index+2, \outBus, outBus.index+2, \trigRateDust, 0, \trigRateImpulse, 0, \mode, 2], group));
						});
						synths.put(2, Synth("cycleGripper_mod", [\inBus, mixerToSynthBus.index+4, \outBus, outBus.index+4, \trigRateDust, 0, \trigRateImpulse, 0, \mode, 2], group));
						synths.put(3, Synth("cycleGripper_mod", [\inBus, mixerToSynthBus.index+6, \outBus, outBus.index+6, \trigRateDust, 0, \trigRateImpulse, 0, \mode, 2], group));
						numChannels = 8;
					}
				)
			};
		);

		win.layout_(
				VLayout(
					HLayout(controls[0], controls[1], controls[2], controls[3], controls[4], controls[5], controls[6], controls[7], controls[8]),
					HLayout(assignButtons[0].layout, assignButtons[1].layout, assignButtons[2].layout, assignButtons[3].layout, assignButtons[4].layout, assignButtons[5].layout, assignButtons[6].layout, assignButtons[7].layout, nil)
				)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
		win.bounds = win.bounds.size_(win.minSizeHint);
		win.front;
	}

	setDaButtons {arg int;
		8.do{arg i;
			if(int==i,{
				controls[i].value = 1;
			},{
				controls[i].value = 0;
			});
		}
	}

}

