ModularMixer {
	var <>group, <>mixers, index, <>volBus, <>muteBus, <>panBus, singleMixer, <>inputBusses, <>outBus, outBussesMonoStereo, soundInBusses, stereoSoundInBusses, busCounter;

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
		busCounter = 0; //only used
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
	}

	add12InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer1-2", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index, \panBus, panBus.index], group));
		});
	}

	add21InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer2-1", [\inBus, inputBusIndex, \outBus, outBus.index, \volBus, volBus.index, \muteBus, muteBus.index], group));
		});
	}

	add22InputMixer {arg inputBusLabel, inputBusIndex;
		singleMixer = mixers[inputBusIndex.asSymbol];
		if(singleMixer==nil,{
			mixers.add(inputBusLabel.asSymbol -> Synth("modularMixer2-2", [\inBus, inputBusIndex, \outBus, outBus, \volBus, volBus, \muteBus, muteBus, \panBus, panBus], group));
		});
	}

	setInputBussesSequentially {arg inputBussesIn;
		//remove the extra mixers
		inputBusses.do{arg item, i;
			index = inputBussesIn.indexOfEqual(item);
			if(index==nil,{
				this.removeMixer(item);
			});
		};

		//add the new mixers if they aren't already there
		inputBussesIn.do{arg item;
			if(inputBusses.indexOf(item)==nil,{

				this.addABus(item);
			})
		};

		inputBusses = inputBussesIn.deepCopy;

	}

	setInputBusses {arg inputBussesIn;

		//remove the extra mixers
		inputBusses.do{arg item, i;
			index = inputBussesIn.indexOfEqual(item);
			if(index==nil,{
				this.removeMixer(item);
			});
		};

		//add the new mixers if they aren't already there
		inputBussesIn.do{arg item;
			if(inputBusses.indexOf(item)==nil,{

				this.addABus(item);
			})
		};

		inputBusses = inputBussesIn.deepCopy;
	}

	addABus {arg busIn;
		var symbol;

		if(busIn.asString.beginsWith("D")||busIn.asString.beginsWith("S"),{


			if(busIn.asString.beginsWith("D"),{
				var size;
				busIn = busIn.asString;
				symbol = busIn.asSymbol;
				busIn = busIn.copyRange(1,busIn.size-1).asInteger-1;
				busIn = busIn+ModularServers.getDirectInBus(group.server);
				if(outBus.numChannels == 1,{
					this.add21InputMixer(symbol, busIn);
				},{
					this.add22InputMixer(symbol, busIn);
				})
			});

			if(busIn.asString.beginsWith("S"),{
				var size, index;
				busIn = busIn.asString;
				symbol = busIn.asSymbol;
				busIn = busIn.copyRange(1,busIn.size-1).asInteger-1;
				busIn = ModularServers.getSoundInBusses(group.server).at(busIn);
				if(outBus.numChannels == 1,{
					this.add11InputMixer(symbol, busIn);
				},{
					this.add12InputMixer(symbol, busIn);
				})
			});

		},{
			busIn=busIn.asInteger;
			if((busIn>0)&&(busIn<26),{
				symbol = busIn.asSymbol;
				busIn = ModularServers.getObjectBusses(group.server).at(busIn-1).index;
				if(outBus.numChannels == 1,{
					this.add21InputMixer(symbol, busIn);
				},{
					this.add22InputMixer(symbol, busIn);
				})
			})
		});

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