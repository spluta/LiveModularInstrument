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

				Out.ar(outBus, Limiter.ar(In.ar(inBus, 2), limit, 0.01)*env*pauseEnv);
			}).writeDefFile;

			SynthDef("modularMixer1-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = Lag.kr(In.kr(volBus), 0.01);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, In.ar(inBus)*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer1-2", {arg inBus, outBus, volBus, muteBus, panBus, gate=1;
				var env, vol, muteVol, in;

				vol = Lag.kr(In.kr(volBus), 0.01);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);
				in = In.ar(inBus);

				Out.ar(outBus, [in,in]*env*vol*muteVol);
			}).writeDefFile;

			SynthDef("modularMixer2-1", {arg inBus, outBus, volBus, muteBus, gate=1;
				var env, vol, muteVol;

				vol = Lag.kr(In.kr(volBus), 0.01);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				Out.ar(outBus, Mix(In.ar(inBus, 2))*env*vol*muteVol);
			}).writeDefFile;
			SynthDef("modularMixer2-2", {arg inBus, outBus, volBus, muteBus, panBus, gate=1;
				var env, vol, muteVol, in;

				vol = Lag.kr(In.kr(volBus), 0.01);
				muteVol = Lag.kr(In.kr(muteBus), 0.01);

				env = EnvGen.kr(Env.asr(0.01,1,0.01), gate, doneAction:2);

				in = In.ar(inBus, 2);

				Out.ar(outBus, in*env*vol*muteVol);
			}).writeDefFile;


			//N-1 mixers


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
	}

	add11InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer1-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group));
		});
		mixers.postln;
	}

	add12InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer1-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group));
		});
		mixers.postln;
	}

	add21InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer2-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group));
		});
		mixers.postln;
	}

	add22InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer2-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group));
			mixers.postln;
		});
	}

/*	addNNInputMixer {arg inputBusIndex, numBusses;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))});
		});
	}

	add1NInputMixer {arg inputBusIndex, numBusses;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer1-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))}
			);
		});
	}

	addN1InputMixer {arg inputBusIndex, numBusses;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			switch(numBusses,
				2, {	mixers.add(inputBusIndex.asSymbol -> Synth("modularMixer2-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group))}
			);
		});
	}*/


/*	setInputBusses {arg inputBussesIn;

		inputBusses = inputBussesIn;

		mixer.setInputBusses(inputBussesIn);
	}*/

	setInputBusses {arg inputBussesIn;

		//remove the extra mixers
		"inputBusses ".post;inputBusses.postln;
		inputBusses.do{arg item, i;
			index = inputBussesIn.indexOfEqual(item);
			if(index==nil,{
				"remove mixer ".post; item.postln;
				this.removeMixer(item);
			});
			mixers.postln;
		};

		//add the new mixers if they aren't already there
		inputBussesIn.do{arg item;
			if(inputBusses.indexOf(item)==nil,{
				"add it".postln;

				this.addABus(item);
			})
		};

		inputBusses.postln;
		inputBusses = inputBussesIn.deepCopy;
	}

	addABus {arg busIn;
		var symbol;
		"add A Bus".postln;

		if(busIn.asString.beginsWith("D")||busIn.asString.beginsWith("S"),{


			if(busIn.asString.beginsWith("D"),{
				var size;
				"give me a D!".postln;
				busIn = busIn.asString;
				symbol = busIn.asSymbol;
				busIn = busIn.copyRange(1,busIn.size-1).asInteger-1;
				busIn = busIn+ModularServers.getDirectInBus(group.server).postln;
				busIn.postln;
				if(outBus.numChannels == 1,{
					"add an 21 mixer.".postln;
					this.add21InputMixer(symbol, busIn);
				},{
					"add a 22Mixer".postln;
					this.add22InputMixer(symbol, busIn);
				})
			});

			if(busIn.asString.beginsWith("S"),{
				var size, index;
				"give me a S!".postln;
				busIn = busIn.asString;
				symbol = busIn.asSymbol;
				busIn = busIn.copyRange(1,busIn.size-1).asInteger-1;
				busIn = ModularServers.getSoundInBusses(group.server).at(busIn);
				busIn.postln;
				if(outBus.numChannels == 1,{
					"add a mono mixer.".postln;
					this.add11InputMixer(symbol, busIn);
				},{
					"add a 12Mixer".postln;
					this.add12InputMixer(symbol, busIn);
				})
			});

		},{
			busIn=busIn.asInteger;
			if((busIn>0)&&(busIn<17),{
				symbol = busIn.asSymbol;
				busIn = ModularServers.getObjectBusses(group.server).at(busIn-1).index;
				busIn.postln;
				if(outBus.numChannels == 1,{
					"add a N1 mixer.".postln;
					this.add21InputMixer(symbol, busIn);
				},{
					"add a NNMixer".postln;
					this.add22InputMixer(symbol, busIn);
				})
			})
		});

	}

	removeMixer{arg index;
		"kill this one".postln;
		mixers[index.asSymbol].postln;
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