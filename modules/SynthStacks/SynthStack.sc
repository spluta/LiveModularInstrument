SynthStackSynth {
	var <>group, <>outBus, <>synthName, <>midiStore, <>currentMidi, currentButtonVals, synth, onTime;

	*new {arg group, outBus, synthName;
		^super.new.group_(group).outBus_(outBus).synthName_(synthName).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("Drone0_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.1, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0, inButt0=1, inButt1=0, inButt2=0, inButt3=0|
				var sound, lfo, lfoMod, env;

				sound = LFSaw.ar(in0.linlin(0,1,20,100)+LFNoise2.kr([0.2,0.18], in1.linlin(0,1,0,3)));

				sound = MoogFF.ar(sound, in2.linlin(0,1,20,2000), in3.linlin(0,1,0,3.8), 0, 5).softclip;

				lfoMod = in4.linlin(0,1,0,100).clip(0,0.5);
				lfo = Lag.ar(SinOsc.ar(in4.linexp(0,1,0.1,10), 0, lfoMod, 1-lfoMod));

				env = Envs.kr(muteGate, pauseGate, gate);


				Out.ar(outBus, sound*env*lfo*Lag.kr(inButt0, 0.02)*(in7));
			}).writeDefFile;

			SynthDef("Gliss0_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0, inButt0=0, inButt1=0, inButt2=0, inButt3=0|
				var mathsA, mathsB, modFreq, modSynth, carrier, cFreq, cFeedback, sig;
				var env, mathsEnv, temp, temp0;

				#temp, temp0, mathsA = Maths.ar(in1.linexp(0, 1, 0.1, 20), 0.5, 0.6, 1);
				#mathsEnv, temp0, mathsB = Maths.ar(in2.linexp(0,1,0.02, 20), in4, 0.5, inButt0, inButt1);

				modFreq = 0.1+mathsB.linexp(0,1,3, 50);
				modSynth = [LFTri.ar(modFreq, 0), LFPulse.ar(modFreq, 0), LFSaw.ar(modFreq, 0)];

				cFreq = ((in0.linexp(0,1,0.01,1)+(mathsB*in3)).linexp(0,1,15,5000, \min)+((Lag.ar(modSynth[2], 0.02)*in5+in5)*(3000*(1-(mathsA*0.5))))).clip(-20000, 20000);
				cFeedback = 0.1+modSynth[0].linlin(-1,1,0,0.7);
				carrier = Mix(SinOscFB.ar([cFreq+2,cFreq-2], cFeedback));

				env = Envs.kr(muteGate, pauseGate, gate);

				sig = Pan2.ar(LPF.ar(LowShelf.ar(SoftClipAmp8.ar(carrier, 2), 400, 1, 3), 16000), 0, 0.9*in7);

				Out.ar(outBus, sig*env*mathsEnv);
			}).writeDefFile;

			SynthDef("Noise0_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0, inButt0=0, inButt1=1, inButt2=0|
				var mod, freq, sig, mouse, slewFreq, slewMod, width, env, temp, temp0;

				slewFreq = in4.linlin(0,1,0.05, 4);
				width = in6;

				#temp, temp0, slewMod = Maths.ar(slewFreq, width, in5, 1);

				freq = (in0+slewMod).linexp(0,1,100,15000);
				mod = Lag.ar(BMoog.ar(
					PinkNoise.ar*0.5,
					freq,
					in0.linlin(0,1,0.1,0.9),
					2), 0.01);

				sig = BMoog.ar([BrownNoise.ar,BrownNoise.ar],
					(freq+mod.linlin(-0.2,0.2,in0.linlin(0,1,100, 8000).neg,in0.linlin(0,1,100, 8000))).clip(100, 15000),
					in3.linlin(0,1,0.1,0.9), 2, 0.95);

				sig = sig*Lag.ar(Select.ar(inButt0, [LFPulse.ar(in1.linlin(0,1,0.2,30),0, in2.linlin(0,1,0.1,0.9)), K2A.ar(1)]*(inButt1)), 0.01)*0.8;

				env = Envs.kr(muteGate, pauseGate, gate);

				sig = HPF.ar(sig, 20)*in7*env;

				Out.ar(outBus, sig);
			}).writeDefFile;

			SynthDef("Perc0_Stack", {|outBus, muteGate=1, pauseGate=1, gate=1, in0=0.5, in1=0.5, in2=0.5, in3=0.5, in4=0.5, in5=0.5, in6=0, in7=0, inButt0=0, inButt1=0, inButt2=0, inButt3=0|

				var perc, mathsEnv, mathsTrig, maths, osc, cFreq, trig, impulseFreq, impulse, filtEnv, env;

				impulseFreq = in2.linlin(0,1,0.2, 30);
				impulse = Select.kr(inButt0, [inButt1, Impulse.kr(impulseFreq)]);
				//impulse = Impulse.kr(impulseFreq);

				#mathsEnv, mathsTrig, maths = Maths.ar(in0.linexp(0,1,0.1, 20), 0.05, 0.7, 0, impulse);

				filtEnv = EnvGen.ar(Env.perc, mathsTrig, 1, 0, 1/impulseFreq);

				perc = Splay.ar(WhiteNoise.ar([1,1])*EnvGen.ar(Env.perc(0.01, 0.5), mathsTrig, 1, 0, in1.linlin(0,1,0.01,0.5)), 0.5);
				//perc = RLPF.ar(perc, (filtEnv*10000+TRand.kr(4000,6000,impulse)), 0.2).distort.distort;

				cFreq = maths.linexp(0,1,15,/*MouseX.kr(15, 5000)*/in3.linlin(0,1,400, 10000));

				//osc = Splay.ar(SinOscFB.ar([cFreq+2,cFreq-2], maths.linlin(0,1,0.5,0.7)).distort, 0.5)*mathsEnv;

				osc = Splay.ar(LFTri.ar([cFreq+2,cFreq-2]).distort, 0.5)*mathsEnv;

				env = Envs.kr(muteGate, pauseGate, gate);

				osc = ((perc+osc)*1.4).softclip;

				Out.ar(outBus, osc*env*in7);

				/*impulseFreq = in2.linlin(0,1,0.2, 30);
				impulse = Select.kr(inButt0, [inButt1, Impulse.kr(impulseFreq)]);
				//impulse = Impulse.kr(impulseFreq);

				#mathsEnv, mathsTrig, maths = Maths.ar(in0.linexp(0,1,0.1, 20), 0.05, 0.7, 0, impulse);

				filtEnv = EnvGen.ar(Env.perc, mathsTrig, 1, 0, 1/impulseFreq);

				perc = Splay.ar(WhiteNoise.ar([1,1])*EnvGen.ar(Env.perc(0.01, 0.5), mathsTrig, 1, 0, in1.linlin(0,1,0.01,1)), 0.5);
				//perc = RLPF.ar(perc, (filtEnv*10000+TRand.kr(4000,6000,impulse)), 0.2).distort.distort;

				cFreq = maths.linexp(0,1,15,in3.linlin(0,1,400, 7000));

				osc = Splay.ar(SinOscFB.ar([cFreq+2,cFreq-2], maths.linlin(0,1,0.5,0.7)).distort, 0.5)*mathsEnv;

				env = Envs.kr(muteGate, pauseGate, gate);

				Out.ar(outBus, (perc+osc)*env*in7);*/
			}).writeDefFile;
		}
	}

	init {
		synthName.postln;
		synth = Synth(synthName++"_Stack", [\outBus, outBus, \muteGate, 1], group);
		midiStore = List.fill(4, {List.fill(12, 0)});
		currentMidi = List.fill(12, 0);

	}

	storeMidi {arg num;
		midiStore.put(num, currentMidi.deepCopy);
		//midiStore.postln;
	}

	restoreMidi {arg num, midiNumStart, midiController;
		midiStore[num].postln;
		midiStore[num].do{|item, i|
			if(i<7,{
				synth.set(("in"++i).asString, item);
				SynthStack_Mod.netAddr.sendMsg('/SynthFader'++(midiNumStart+i).asString++'/x', item);
				if(i.even, {
					SynthStack_Mod.netAddr.sendMsg('/SynthMulti'++(midiNumStart/2+(i/2)).asString++'/x', item)
				},{
					SynthStack_Mod.netAddr.sendMsg('/SynthMulti'++(midiNumStart/2+(i-1/2)).asString++'/y', item)
				});
			},{
				if([8,9,10,11].includes(i),{
					synth.set(("inButt"++(i-8)).asSymbol, item);
					SynthStack_Mod.netAddr.sendMsg('/SynthButton'++(midiNumStart+(i-8)).asString++'/x', item);
				});
			});
		};
		currentMidi = midiStore[num].deepCopy;
		^midiStore[num];
	}

	midiIn {arg num, val;
		//[num, val].postln;
		currentMidi.put(num, val);
		//currentMidi.postln;
		synth.set(("in"++num).asSymbol, val);
	}

	buttonIn {arg midiNumStart, num, val;
		var index, temp;

		//"buttonIn".post; num.postln;
		if([4,5,6,7].includes(num), {
			if(val==1,{
				onTime=Main.elapsedTime;
			},{
				index = [4,5,6,7].indexOf(num);
				if(Main.elapsedTime-onTime>0.5,{
					this.storeMidi(index);
				},{
					this.restoreMidi(index, midiNumStart);
				})
			})
		},{
			//if(val==1,{
			//index = [2,3,6].indexOf(num);
			//temp = (currentMidi[num+8]+1).wrap(0,1);
			synth.set(("inButt"++num.asString).asSymbol.post, val);
			currentMidi.put(num+8, val);
			//})
		})
	}

	mute {
		synth.set(\muteGate, 0);
	}

	unmute {
		synth.set(\muteGate, 1);
	}

	pause {
		synth.set(\pauseGate, 0);
	}

	unpause {
		synth.set(\pauseGate, 1); synth.run(true);
	}

	killMe {
		synth.set(\gate, 0);
	}

	save {
		^midiStore
	}

	load {arg store;
		midiStore = store.deepCopy;
	}
}


SynthStack_Mod : Module_Mod {
	var midiDests, midiFuncs, midiSwitches, currentSynth, midiController, storeButtons, synthGroup, eqGroup;
	classvar <>netAddr;

	*initClass {
		StartUp.add {
			SynthDef("EQ_Stack", {|outBus|
				ReplaceOut.ar(outBus, BLowShelf.ar(In.ar(outBus,8), 80, 1, 5));
			}).writeDefFile
		}
	}

	init {
		this.makeWindow("SynthStack", Rect(900, 400, 240, 115));
		this.initControlsAndSynths(17);

		synths = List.newClear(16);
		currentSynth = 0;
		netAddr = NetAddr("127.0.0.1", 8000);

		synthGroup = Group.tail(group);
		eqGroup = Group.tail(group);

		Synth("EQ_Stack",[\outBus, outBus], eqGroup);

		/*midiDests = MIDIClient.destinations.collect({arg item; item.name});
		controls.add(PopUpMenu()
		.items_(midiDests)
		.action_{arg menu;
		midiController = MIDIOut(menu.value);
		};
		);
		if(midiDests.includes("Midi Fighter Twister"), {controls.valueAction_(midiDests.indexOf("Midi Fighter Twister"))});*/

		//midiController = NetAddr("127.0.0.1

		storeButtons = List.newClear(0);
		16.do{arg i;
			controls.add(PopUpMenu()
				.items_(["nil", "Noise0", "Gliss0", "Perc0","Drone0"])
				.action_{arg menu;
					if(synths[i]!=nil, {synths[i].killMe});
					if(menu.value==0, {
						synths.put(i,nil)
					},{
						synths.put(i, SynthStackSynth(group, outBus, menu.item));
					});
					//synths.postln;
			});
			storeButtons.add(Array.fill(4, {arg i2;
				Button()
				.states_([["",Color.black,Color.black]])
				.action_{synths[i].storeMidi(i2)}
				.maxWidth_(16).maxHeight_(16)
			}));
		};

		midiSwitches = Array.fill(8, 0);

		midiFuncs = List.newClear(0);

		16.do{arg i;
			8.do{arg i2;
				OSCFunc({arg ...vals;
					//vals.postln;
					netAddr = vals[2];
					if(synths[i]!=nil,{
						synths[i].midiIn(i2, vals[0][1])
					})
				}, '/SynthFader'++(i*8+i2).asString++'/x');

				OSCFunc({arg ...vals;
					//vals.postln;
					//synths.postln;
					netAddr = vals[2];
					if(synths[i]!=nil,{
						synths[i].buttonIn(i*8, i2, vals[0][1])
					})
				}, '/SynthButton'++(i*8+i2).asString++'/x');
			};
			//there are 4 2D Sliders
			4.do{arg i2;
				OSCFunc({arg ...vals;
					//vals.postln;
					netAddr = vals[2];
					if(synths[i]!=nil,{
						synths[i].midiIn(i2*2, vals[0][1])
					})
				}, '/SynthMulti'++(i*4+i2).asString++'/x');
				OSCFunc({arg ...vals;
					//vals.postln;
					netAddr = vals[2];
					if(synths[i]!=nil,{
						synths[i].midiIn(i2*2+1, vals[0][1])
					})
				}, '/SynthMulti'++(i*4+i2).asString++'/y');
			}
		};

		/*8.do{arg i;



		//
		midiFuncs.add(MIDIFunc.cc({arg ...vals;
		vals.postln;
		if(synths[i*2+midiSwitches[i]]!=nil,{
		synths[i*2+midiSwitches[i]].midiIn(vals[1]%8, vals[0]);
		this.setCurrentSynth(i*2+midiSwitches[i]);
		});
		}, ((8*i)..(8*i+7)), 0));
		//
		midiFuncs.add(MIDIFunc.cc({arg ...vals;
		vals.postln;
		if(vals[0]==127,{
		if(synths[i*2+(midiSwitches[i]+1).wrap(0,1)]!=nil, {
		midiSwitches.put(i, (midiSwitches[i]+1).wrap(0,1));
		midiSwitches.postln;
		//set the controller with the current values in the synth
		this.setCurrentSynth(i*2+midiSwitches[i]);
		8.do{arg i2;
		midiController.control(1,(8*i)+i2,127*midiSwitches[i]);
		midiController.control(0,(8*i)+i2, synths[currentSynth].currentMidi[i2]);
		};
		})
		});
		}, (8*i+7), 1));
		//
		midiFuncs.add(MIDIFunc.cc({arg ...vals;
		vals.postln;
		if(synths[i*2+midiSwitches[i]]!=nil, {
		this.setCurrentSynth(i*2+midiSwitches[i]);
		synths[currentSynth].buttonIn(8*i, vals[1]%8, vals[0], midiController);
		});

		}, ((8*i)..(8*i+6)), 1));
		};*/

		win.layout_(
			VLayout(
				//controls[0],
				HLayout(
					VLayout(*controls.copyRange(0, 15)),
					VLayout(*storeButtons.collect{arg item; HLayout(*item)})
				)
			)
		);
	}

	setCurrentSynth {arg num;
		if(currentSynth!=num,{
			synths[currentSynth].mute;
			currentSynth = num;
		});
		synths[currentSynth].unmute;
	}

	pause {
		synths.do{|item| if(item!=nil, {item.pause})}
	}

	unpause {
		synths.do{|item| if(item!=nil, {item.unpause})}
	}

	saveExtra {arg saveArray;
		var temp;
		temp = List.newClear(0);
		synths.do{arg item; if(item!=nil, {temp.add(item.save)},{temp.add(nil)})};
		saveArray.add(temp);
	}

	loadExtra{arg loadArray;
		loadArray[4].do{arg item, i;
			if((synths[i]!=nil)and:(item!=nil),{synths[i].load(item)});
		}
	}

	killMe {
		win.close;
		if(synths!=nil,{
			synths.do{arg item; if(item!=nil,{item.killMe})};
		});
		mixerToSynthBus.free;
		this.killMeSpecial;
	}

}
