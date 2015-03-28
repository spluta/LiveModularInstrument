/*ModularSetInputWindow {
	var <>availableBusIns, <>requestingObject, inputBusses, window, applyButton, soundInButtons, busInButtons, returnList, index;

	*new {arg availableBusIns, requestingObject, inputBusses;
		^super.newCopyArgs(availableBusIns, requestingObject, inputBusses).init;
	}

	init {
		returnList = List.new;
		2.do{
			returnList.add(List.new);
		};
		window = Window.new("Assign Inputs",Rect(654, 690, 5+(35*8), 60+((availableBusIns[1].size/5).ceil*35)));
		window.alwaysOnTop_(true);

		soundInButtons = List.new;
		busInButtons = List.new;

		availableBusIns[0].do{arg item, i;
			soundInButtons.add(Button.new(window,Rect(5+(35*i), 5, 30, 20))
				.states_([ [ "S"++i, Color.blue, Color.black ], [ "S"++i, Color.black, Color.blue ] ])
				.action_{|v|
					if(v.value==1,{
						returnList[0].add(i);
					},{
						returnList[0].remove(i);
					});
				});
		};

		availableBusIns[1].do{arg item, i;
			busInButtons.add(Button.new(window,Rect(5+((i%5)*35), 30+((i/5).floor*25), 30, 20))
				.states_([ [ item.asString, Color.blue, Color.black ], [ item.asString, Color.black, Color.blue ] ])
				.action_{|v|
					if(v.value==1,{
						returnList[1].add(item);
					},{
						returnList[1].remove(item);
					});
				});
		};

		applyButton = Button.new(window, Rect(5, (availableBusIns[1].size/5).ceil*25+30, 50, 30))
			.states_([ [ "Apply", Color.blue, Color.black ]])
				.action_{|v|
					requestingObject.setInputBusses(returnList);
					window.close;
				};


		inputBusses[0].do{arg i; soundInButtons[i].valueAction = 1};
		inputBusses[1].do{arg i;
			index = availableBusIns[1].indexOfEqual(i);
			if(index!=nil,{
				busInButtons[index].valueAction = 1;
			})
		};

		window.front;
	}
}*/

ModularMixer {
	var <>group, <>mixers, index, <>volBus, <>muteBus, <>panBus, singleMixer, <>inputBusses, <>outBus, outBussesMonoStereo, soundInBusses, stereoSoundInBusses;

	*new {arg group;
		^super.newCopyArgs(group).init;
	}

	*initClass {
		{
			SynthDef("limiter_mod", {arg inBus, outBus, limit=0.9, gate = 1, pauseGate = 1;
				var env, pauseEnv;

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				Out.ar(outBus, Limiter.ar(In.ar(inBus, 8), limit, 0.01)*env*pauseEnv);
			}).writeDefFile;

			SynthDef("modularMixer1-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-2", {arg inBus, outBus, volBus, muteBus, panBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, Pan2.ar(in, In.kr(panBus))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-3", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,]*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-4", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,in]*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-5", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,in,in]*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-6", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,in,in,in]*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-7", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,in,in,in,in]*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-8", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in,in,in,in,in,in,in]*env*vol*muteVol);
			}).writeDefFile;


			SynthDef("modularMixer2-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 2))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer2-2", {arg inBus, outBus, volBus, muteBus, panBus, gate=1;
				var env, vol, muteVol, in;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				in = In.ar(inBus, 2);

				Out.ar(outBus, in*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer3-3", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 3)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer4-4", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 4)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer5-5", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 5)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer6-6", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 6)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer7-7", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 7)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer8-8", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus, 8)*env*vol*muteVol);
			}).writeDefFile;

			//N-1 mixers

			SynthDef("modularMixer3-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 3))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer4-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 4))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer5-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 5))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer6-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 6))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer7-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 7))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer8-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = In.kr(volBus);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 8))*env*vol*muteVol);
			}).writeDefFile;
		}.defer(1);
	}

	init {
		volBus = Bus.control(group.server);
		volBus.set(0);
		muteBus = Bus.control(group.server);
		muteBus.set(1);
		panBus = Bus.control(group.server);
		panBus.set(0);

		inputBusses = List.new;

		mixers = IdentityDictionary.new;

		soundInBusses = ModularServers.getSoundInBusses(group.server);
		stereoSoundInBusses = ModularServers.getStereoSoundInBusses(group.server);
	}

	add11InputMixer {arg inputBusIndex, outBusIndex;
		"add11".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group));
		});
	}

	add12InputMixer {arg inputBusIndex, outBusIndex;
		"add12".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group));
		});
	}

	add21InputMixer {arg inputBusIndex, outBusIndex;
		"add21".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group));
		});
	}

	add22InputMixer {arg inputBusIndex, outBusIndex;
		"add22".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group));
		});
	}

	addNNInputMixer {arg inputBusIndex, numBusses;
		"addNN".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))},
				3, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer3-3", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				4, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer4-4", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				5, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer5-5", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				6, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer6-6", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				7, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer7-7", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				8, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer8-8", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))}
			);
		});
	}

	add1NInputMixer {arg inputBusIndex, numBusses;
		"add1N".postln;
		[inputBusIndex, numBusses].postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))},
				3, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-3", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				4, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-4", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				5, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-5", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				6, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-6", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				7, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-7", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				8, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-8", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))}
			);
		});
	}

	addN1InputMixer {arg inputBusIndex, numBusses;
		"addN1".postln;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))},
				3, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer3-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				4, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer4-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				5, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer5-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				6, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer6-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				7, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer7-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))},
				8, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer8-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group))}
			);
		});
	}

	setInputBusses {arg inputBussesIn, numBusses;

		//remove the extra mixers
		inputBusses.do{arg item, i;
			index = inputBussesIn.indexOfEqual(item);
			if(index==nil,{
				this.removeMixer(item);
			});
		};

		//add the new mixers

		inputBussesIn.do{arg item;
			this.addABus(item, numBusses);
		};

		^inputBusses = inputBussesIn.deepCopy;
	}

	addABus {arg item, numBusses;
		//soundInBusses and stereoSoundInBusses can be grabbed from the ModularServers - do that, but also make sure you still need all this trash

		if((soundInBusses.indexOf(item)!=nil)||(stereoSoundInBusses.indexOf(item)!=nil),{
			if((soundInBusses.indexOf(item)!=nil),{
				if(outBus.numChannels == 1,{
					this.add11InputMixer(item);
					},{
						this.add1NInputMixer(item, numBusses);
						})
				},{
					"stereo input bus".postln;
					if(outBus.numChannels == 1,{
						this.add21InputMixer(item);
						},{
		 					this.add22InputMixer(item);
							})
					})
			},{
				if(outBus.numChannels == 1,{
					//force 8 channel input mixed down to 1 channel - hopefully this doesn't add too much noise to the signal
					this.addN1InputMixer(item, 8);
					},{
						this.addNNInputMixer(item, 8);
				});
			});

	}

	resetNumBusses {arg numBusses;
		mixers.do{arg item, i;
			item.set(\gate, 0);
		};
		mixers = IdentityDictionary.new;
	}


	removeMixer{arg index;
		mixers[index.asSymbol].set(\gate, 0);
		mixers.removeAt(index.asSymbol);
	}

	removeAllMixers{arg index;
		mixers.do{arg item;
			item.set(\gate, 0);
		};
		mixers = IdentityDictionary.new;
		inputBusses = List.new;
	}

	mute {
		muteBus.set(0);
	}

	unmute {
		muteBus.set(1);
	}

	setVol {arg val;
		volBus.set(val);
	}
}