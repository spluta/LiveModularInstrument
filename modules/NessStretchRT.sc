NessyObject_Mod {
	var <>group, <>outBus, <>parent, <>num, <>volBus, <>synth, bufs, zVal, lockVal, speed, speedBus, running, outFileA, outFileB;

	*initClass {
		StartUp.add {
			SynthDef("play_nessie_mod", {|outBus, buf0a, buf0b, buf1a, buf1b, volBus, speedBus, rate=1, gate=1, pauseGate=1, muteGate=1|

				var env = EnvGen.kr(Env.asr(0.01, 1, 2), gate, doneAction:2);
				var sound, pauseEnv, muteEnv;

				var speed = In.kr(speedBus);
				var imp = Impulse.kr(1/((BufDur.kr(buf0a)-4)/speed));
				var vol = In.kr(volBus).lag(0.2);

				sound = [TGrains2.ar(1, imp, buf0a, speed, BufDur.kr(buf0a)/2, BufDur.kr(buf0a)/speed, 0, 1, 2, 2),
					TGrains2.ar(1, imp, buf0b, speed, BufDur.kr(buf0a)/2, BufDur.kr(buf0a)/speed, 0, 1, 2, 2)]+
				[TGrains2.ar(1, imp, buf1a, speed, BufDur.kr(buf1a)/2, BufDur.kr(buf1a)/speed, 0, 1, 2, 2),
					TGrains2.ar(1, imp, buf1b, speed, BufDur.kr(buf1a)/2, BufDur.kr(buf1a)/speed, 0, 1, 2, 2)];

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
				Out.ar(outBus, sound*env*2*pauseEnv*muteEnv*vol);
			}).writeDefFile;//load(ModularServers.servers[\lmi1].server);
		}
	}

	*new {arg group, outBus, parent, num, outFileA, outFileB;
		^super.new.group_(group).outBus_(outBus).parent_(parent).num_(num).init;
	}

	init {
		volBus = Bus.control(group.server, 1);
		speedBus = Bus.control(group.server, 1);
		speed = 1;
		volBus.set(0);
		speedBus.set(speed);
		running = false;
		zVal = 0;
		lockVal = 0;

	}


	makeAndPlayLoop {
		var inTempText, tempText, oneNDone, twoNDone, shortFileA, shortFileB;

		tempText  = "ns_temp"++"_"++group.server.asString++"_"++group.nodeID.asString;

		shortFileA = (Platform.defaultTempDir++tempText++"A.wav").quote;
		shortFileB = (Platform.defaultTempDir++tempText++"B.wav").quote;
		outFileA = (Platform.defaultTempDir++tempText++"_"++"_100A.wav").quote;
		outFileB = (Platform.defaultTempDir++tempText++"_"++"_100B.wav").quote;

		if(synth!=nil){
			synth.set(\gate, 0);
		};

		bufs.do{|buf| if(buf!=nil){buf.free}};

		oneNDone = 0;
		twoNDone = 0;
		parent.lastBuffers[0].write(shortFileA, "wav", completionMessage:{oneNDone = 1});
		parent.lastBuffers[1].write(shortFileB, "wav", completionMessage:{
			{
				while(oneNDone==0){"waiting".postln; 0.025.wait};

				("/Users/spluta1/Documents/rust/ness_stretch/target/release/ness_stretch -m 100 -v 0 -s 4 -c 1 -f "++shortFileB+"-o"+outFileB).unixCmd(action:{twoNDone=1});

				("/Users/spluta1/Documents/rust/ness_stretch/target/release/ness_stretch -m 100 -v 0 -s 9 -c 1 -f "++shortFileA+"-o"+outFileA).unixCmd(
					action:{|msg|
						{


							while(twoNDone==0){"waiting".postln; 0.025.wait};

							("rm"+shortFileA).unixCmd;
							("rm"+shortFileB).unixCmd;

							bufs = [
								Buffer.readChannel(group.server, outFileA, channels:[0]),
								Buffer.readChannel(group.server, outFileA, channels:[1]),
								Buffer.readChannel(group.server, outFileB, channels:[0]),
								Buffer.readChannel(group.server, outFileB, channels:[1])
							];

							group.server.sync;
							0.05.wait;
							("rm "++outFileA).unixCmd;
							("rm "++outFileB).unixCmd;
							synth = Synth("play_nessie_mod", [\buf0a, bufs[0], \buf0b, bufs[1], \buf1a, bufs[2], \buf1b, bufs[3], \outBus, outBus, \volBus, volBus, \speedBus, speedBus], group);
							(bufs[0].duration-2/speed).wait;
						}.fork;
				});
			}.fork;
		});

	}

	setZVal {|val|
		zVal = val;
		[zVal, running];
		if (zVal==1){
			if(running==false){
				running = true;
				this.makeAndPlayLoop;
			}
		}{
			if(lockVal==0){
				running = false;
				synth.set(\gate, 0);
			}
		}
	}

	setShift{|val|
		speed = [0.5,1,2][val];
		speedBus.set(speed);
	}

	setLock{|val|
		lockVal = val;
		if (lockVal==1){
			if(running==false){
				running = true;
				this.makeAndPlayLoop;
			}
		}{
			if(zVal==0){
				running = false;
				synth.set(\gate, 0);
			}
		}
	}

	killMeSpecial {

	}
}

NessStretchRT_Mod : SignalSwitcher_Mod {
	var texts, functions, transferBus, numBufs, durs, recBufs, recBufSeq, currentBuf, recSynth, hpssGroup, recGroup, bufSeq, bufs, osc, lastRecordedBuffer, routNum, recordTask, <>bufNum, nessyObjects, <>lastBuffers;

	*initClass {
		StartUp.add {
			SynthDef("ness_in_mod", {|inBus0, inBus1, whichInBus=0, transferBus, gate=1, pauseGate=1, muteGate=1|
				var sound;
				var pauseEnv, muteEnv, env;
				var sum = SelectX.ar(whichInBus, [In.ar(inBus0, 2), In.ar(inBus1, 2)]);
				sound = FluidHPSS.ar(sum);
				sound = sound.flatten;
				sound = [sound[0], sound[3], sound[1], sound[4]];

				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
				Out.ar(transferBus, sound*pauseEnv*muteEnv*gate);
			}).writeDefFile;

			SynthDef("record_lilnessies_mod", {|transferBus, dur, buf, currentBufNum|
				var env = EnvGen.kr(Env([0,1,1,0,0], [0.01, dur-0.002, 0.001, 0.001]), doneAction:2);
				var in = In.ar(transferBus, 4);
				RecordBuf.ar([in[0], in[1]]*env, buf, loop:0);
				RecordBuf.ar([in[2], in[3]]*env, buf, loop:0);
				SendTrig.kr(Env([0,0,1], [1/8, 0.001]).kr, currentBufNum, 0.9);
			}).writeDefFile;


		}

	}

	init3 {

		synthName = "NessStretchRT";

		win.name = "NessStretchRT"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);

		this.initControlsAndSynths(17);

		dontLoadControls = (0..16);

		hpssGroup = Group.tail(group);
		recGroup = Group.tail(group);

		numBufs = 32;

		nessyObjects = Array.fill(4, {|i| NessyObject_Mod(group, outBus, this, i)});

		transferBus = Bus.audio(group.server, 4);

		synths.add(Synth("ness_in_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \whichInBus, 0, \transferBus, transferBus], hpssGroup));
		4.do{synths.add(nil)};

		//this part records the temporary buffers
		durs = Array.fill(numBufs, {rrand(1/12,1/5)});
		recBufs = Array.fill(numBufs, {|i| Array.fill(2, {Buffer.alloc(group.server, group.server.sampleRate*durs[i], 2)})});
		recBufSeq = Pseq((0..(numBufs-1)), inf).asStream;

		recordTask = Task({inf.do{
			currentBuf = recBufSeq.next;
			Synth("record_lilnessies_mod", [\dur, durs[currentBuf], \buf, recBufs[currentBuf], \currentBufNum, currentBuf, \transferBus, transferBus], recGroup);
			(1/16).wait;
		}}).play;


		//this part writes the temporary buffers to disk
		bufSeq = Pseq((0..(numBufs-1)), inf).asStream;
		bufs = List.newClear(numBufs);
		osc = OSCFunc({ arg msg, time;
			var tempText;

			lastRecordedBuffer = msg[2];

			recBufs[lastRecordedBuffer].collect{|item| item.normalize};
			lastBuffers = recBufs[lastRecordedBuffer];

			//recBufs[lastRecordedBuffer][0].write(Platform.defaultTempDir++tempText++"A.wav", "wav");
			//recBufs[lastRecordedBuffer][1].write(Platform.defaultTempDir++tempText++"B.wav", "wav");

		},'/tr', group.server.addr);


		texts = [
			"whichInput", "vol1", "zVol1", "shift1", "unlock/lock1",
			"vol2", "zVol2", "shift2", "unlock/lock2",
			"vol3", "zVol3", "shift3", "unlock/lock3",
			"vol4", "zVol4", "shift4", "unlock/lock4"
		];

		functions = [
			{arg val; synths[0].set(\whichInBus, val)},

			{arg val; nessyObjects[0].volBus.set((val**2)*2)},
			{arg val;  nessyObjects[0].setZVal(val)},
			{arg val;  nessyObjects[0].setShift(val)},
			{arg val; nessyObjects[0].setLock(val)},

			{arg val; nessyObjects[1].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[1].setZVal(val)},
			{arg val;  nessyObjects[1].setShift(val)},
			{arg val; nessyObjects[1].setLock(val)},

			{arg val; nessyObjects[2].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[2].setZVal(val)},
			{arg val;  nessyObjects[2].setShift(val)},
			{arg val; nessyObjects[2].setLock(val)},

			{arg val; nessyObjects[3].volBus.set((val**1.2)*2)},
			{arg val;  nessyObjects[3].setZVal(val)},
			{arg val;  nessyObjects[3].setShift(val)},
			{arg val; nessyObjects[3].setLock(val)}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func));
		};

		[0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0].do{|item, i| controls[i].setExternal_(item)};

		win.bounds_((70*numMixers)@(150+(texts.size*16)));

		win.layout_(
			VLayout(
				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
				VLayout(*controls.collect({arg item; item.view}))
			)
		);
		win.layout.spacing = 1;
		win.layout.margins = [1,1,1,1];
		win.front;
	}

	killMeSpecial {
		nessyObjects.do{|obj| obj.killMeSpecial}
	}

	pause {
		mixerStrips.do{|item| item.mute};
		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
	}

	unpause {
		mixerStrips.do{|item| item.unmute};
		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
	}

}

// NessyObject_Mod {
// 	var <>group, <>outBus, <>parent, <>num, <>volBus, <>synth, bufs, zVal, lockVal, speed, speedBus, running, outFileA, outFileB;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("play_nessie_mod", {|outBus, buf0a, buf0b, buf1a, buf1b, volBus, speedBus, rate=1, gate=1, pauseGate=1, muteGate=1|
//
// 				var env = EnvGen.kr(Env.asr(0.01, 1, 2), gate, doneAction:2);
// 				var sound, pauseEnv, muteEnv;
//
// 				var speed = In.kr(speedBus);
// 				var imp = Impulse.kr(1/((BufDur.kr(buf0a)-4)/speed));
// 				var vol = In.kr(volBus).lag(0.2);
//
// 				sound = [TGrains2.ar(1, imp, buf0a, speed, BufDur.kr(buf0a)/2, BufDur.kr(buf0a)/speed, 0, 1, 2, 2),
// 				TGrains2.ar(1, imp, buf0b, speed, BufDur.kr(buf0a)/2, BufDur.kr(buf0a)/speed, 0, 1, 2, 2)]+
// 				[TGrains2.ar(1, imp, buf1a, speed, BufDur.kr(buf1a)/2, BufDur.kr(buf1a)/speed, 0, 1, 2, 2),
// 				TGrains2.ar(1, imp, buf1b, speed, BufDur.kr(buf1a)/2, BufDur.kr(buf1a)/speed, 0, 1, 2, 2)];
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
// 				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
// 				Out.ar(outBus, sound*env*2*pauseEnv*muteEnv*vol);
// 			}).writeDefFile;//load(ModularServers.servers[\lmi1].server);
// 		}
// 	}
//
// 	*new {arg group, outBus, parent, num, outFileA, outFileB;
// 		^super.new.group_(group).outBus_(outBus).parent_(parent).num_(num).init;
// 	}
//
// 	init {
// 		volBus = Bus.control(group.server, 1);
// 		speedBus = Bus.control(group.server, 1);
// 		speed = 1;
// 		volBus.set(0);
// 		speedBus.set(speed);
// 		running = false;
// 		zVal = 0;
// 		lockVal = 0;
//
// 	}
//
//
// 	makeAndPlayLoop {
//
// 		var tempText = "ns_temp"++parent.bufNum++"_"++group.server.asString++group.nodeID.asString;
// 		outFileA = (Platform.defaultTempDir++tempText++"_"++num++"_100A.wav").quote;
// 		outFileB = (Platform.defaultTempDir++tempText++"_"++num++"_100B.wav").quote;
//
// 		if(synth!=nil){
// 			synth.set(\gate, 0);
// 		};
//
// 		bufs.do{|buf| if(buf!=nil){buf.free}};
//
// 		("/Users/spluta1/Documents/rust/ness_stretch/target/release/ness_stretch -m 100 -v 0 -s 4 -c 1 -f "++(Platform.defaultTempDir++tempText++"B.wav").quote+"-o"+outFileB).unixCmd;
//
// 		("/Users/spluta1/Documents/rust/ness_stretch/target/release/ness_stretch -m 100 -v 0 -s 9 -c 1 -f "++(Platform.defaultTempDir++tempText++"A.wav").quote+"-o"+outFileA).unixCmd(
// 			action:{|msg|
// 				{
//
//
//
// 					bufs = [
// 						Buffer.readChannel(group.server, Platform.defaultTempDir++tempText++"_"++num++"_100A.wav", channels:[0]),
// 						Buffer.readChannel(group.server, Platform.defaultTempDir++tempText++"_"++num++"_100A.wav", channels:[1]),
// 						Buffer.readChannel(group.server, Platform.defaultTempDir++tempText++"_"++num++"_100B.wav", channels:[0]),
// 						Buffer.readChannel(group.server, Platform.defaultTempDir++tempText++"_"++num++"_100B.wav", channels:[1])
// 					];
//
// 					group.server.sync;
// 					0.05.wait;
// 					("rm "++outFileA).unixCmd;
// 					("rm "++outFileB).unixCmd;
// 					synth = Synth("play_nessie_mod", [\buf0a, bufs[0], \buf0b, bufs[1], \buf1a, bufs[2], \buf1b, bufs[3], \outBus, outBus, \volBus, volBus, \speedBus, speedBus], group);
// 					(bufs[0].duration-2/speed).wait;
// 				}.fork;
// 		});
//
// 	}
//
// 	setZVal {|val|
// 		zVal = val;
// 		[zVal, running];
// 		if (zVal==1){
// 			if(running==false){
// 				running = true;
// 				this.makeAndPlayLoop;
// 			}
// 		}{
// 			if(lockVal==0){
// 				running = false;
// 				synth.set(\gate, 0);
// 			}
// 		}
// 	}
//
// 	setShift{|val|
// 		speed = [0.5,1,2][val];
// 		speedBus.set(speed);
// 	}
//
// 	setLock{|val|
// 		lockVal = val;
// 		if (lockVal==1){
// 			if(running==false){
// 				running = true;
// 				this.makeAndPlayLoop;
// 			}
// 		}{
// 			if(zVal==0){
// 				running = false;
// 				synth.set(\gate, 0);
// 			}
// 		}
// 	}
//
// 	killMeSpecial {
//
// 	}
// }
//
// NessStretchRT_Mod : SignalSwitcher_Mod {
// 	var texts, functions, transferBus, numBufs, durs, recBufs, recBufSeq, currentBuf, recSynth, hpssGroup, recGroup, bufSeq, bufs, osc, lastRecordedBuffer, routNum, recordTask, <>bufNum, nessyObjects;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("ness_in_mod", {|inBus0, inBus1, whichInBus=0, transferBus, gate=1, pauseGate=1, muteGate=1|
// 				var sound;
// 				var pauseEnv, muteEnv, env;
// 				var sum = SelectX.ar(whichInBus, [In.ar(inBus0, 2), In.ar(inBus1, 2)]);
// 				sound = FluidHPSS.ar(sum);
// 				sound = sound.flatten;
// 				sound = [sound[0], sound[3], sound[1], sound[4]];
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate);
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
// 				muteEnv = EnvGen.kr(Env.asr(0,1,0), muteGate, doneAction:0);
// 				Out.ar(transferBus, sound*pauseEnv*muteEnv*gate);
// 			}).writeDefFile;
//
// 			SynthDef("record_lilnessies_mod", {|transferBus, dur, buf, currentBufNum|
// 				var env = EnvGen.kr(Env([0,1,1,0,0], [0.01, dur-0.002, 0.001, 0.001]), doneAction:2);
// 				var in = In.ar(transferBus, 4);
// 				RecordBuf.ar([in[0], in[1]]*env, buf, loop:0);
// 				RecordBuf.ar([in[2], in[3]]*env, buf, loop:0);
// 				SendTrig.kr(Env([0,0,1], [1/8, 0.001]).kr, currentBufNum, 0.9);
// 			}).writeDefFile;
//
//
// 		}
//
// 	}
//
// 	init3 {
//
// 		synthName = "NessStretchRT";
//
// 		win.name = "NessStretchRT"++(ModularServers.getObjectBusses(ModularServers.servers[group.server.asSymbol].server).indexOf(outBus)+1);
//
// 		this.initControlsAndSynths(17);
//
// 		dontLoadControls = (0..16);
//
// 		hpssGroup = Group.tail(group);
// 		recGroup = Group.tail(group);
//
// 		numBufs = 32;
//
// 		nessyObjects = Array.fill(4, {|i| NessyObject_Mod(group, outBus, this, i)});
//
// 		transferBus = Bus.audio(group.server, 4);
//
// 		synths.add(Synth("ness_in_mod", [\inBus0, localBusses[0], \inBus1, localBusses[1], \whichInBus, 0, \transferBus, transferBus], hpssGroup));
// 		4.do{synths.add(nil)};
//
// 		//this part records the temporary buffers
// 		durs = Array.fill(numBufs, {rrand(1/12,1/5)});
// 		recBufs = Array.fill(numBufs, {|i| Array.fill(2, {Buffer.alloc(group.server, group.server.sampleRate*durs[i], 2)})});
// 		recBufSeq = Pseq((0..(numBufs-1)), inf).asStream;
//
// 		recordTask = Task({inf.do{
// 			currentBuf = recBufSeq.next;
// 			Synth("record_lilnessies_mod", [\dur, durs[currentBuf], \buf, recBufs[currentBuf], \currentBufNum, currentBuf, \transferBus, transferBus], recGroup);
// 			(1/16).wait;
// 		}}).play;
//
//
// 		//this part writes the temporary buffers to disk
// 		bufSeq = Pseq((0..(numBufs-1)), inf).asStream;
// 		bufs = List.newClear(numBufs);
// 		osc = OSCFunc({ arg msg, time;
// 			var tempText;
//
// 			lastRecordedBuffer = msg[2];
//
// 			recBufs[lastRecordedBuffer].collect{|item| item.normalize};
// 			bufNum = bufSeq.next;
// 			tempText = "ns_temp"++bufNum++"_"++group.server.asString++group.nodeID.asString;
// 			recBufs[lastRecordedBuffer][0].write(Platform.defaultTempDir++tempText++"A.wav", "wav");
// 			recBufs[lastRecordedBuffer][1].write(Platform.defaultTempDir++tempText++"B.wav", "wav");
//
// 		},'/tr', group.server.addr);
//
//
// 		texts = [
// 			"whichInput", "vol1", "zVol1", "shift1", "unlock/lock1",
// 			"vol2", "zVol2", "shift2", "unlock/lock2",
// 			"vol3", "zVol3", "shift3", "unlock/lock3",
// 			"vol4", "zVol4", "shift4", "unlock/lock4"
// 		];
//
// 		functions = [
// 			{arg val; synths[0].set(\whichInBus, val)},
//
// 			{arg val; nessyObjects[0].volBus.set((val**2)*2)},
// 			{arg val;  nessyObjects[0].setZVal(val)},
// 			{arg val;  nessyObjects[0].setShift(val)},
// 			{arg val; nessyObjects[0].setLock(val)},
//
// 			{arg val; nessyObjects[1].volBus.set((val**1.2)*2)},
// 			{arg val;  nessyObjects[1].setZVal(val)},
// 			{arg val;  nessyObjects[1].setShift(val)},
// 			{arg val; nessyObjects[1].setLock(val)},
//
// 			{arg val; nessyObjects[2].volBus.set((val**1.2)*2)},
// 			{arg val;  nessyObjects[2].setZVal(val)},
// 			{arg val;  nessyObjects[2].setShift(val)},
// 			{arg val; nessyObjects[2].setLock(val)},
//
// 			{arg val; nessyObjects[3].volBus.set((val**1.2)*2)},
// 			{arg val;  nessyObjects[3].setZVal(val)},
// 			{arg val;  nessyObjects[3].setShift(val)},
// 			{arg val; nessyObjects[3].setLock(val)}
// 		];
//
// 		functions.do{arg func, i;
// 			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func));
// 		};
//
// 		[0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0].do{|item, i| controls[i].setExternal_(item)};
//
// 		win.bounds_((70*numMixers)@(150+(texts.size*16)));
//
// 		win.layout_(
// 			VLayout(
// 				HLayout(*mixerStrips.collect({arg item; item.panel})).margins_(0!4).spacing_(0),
// 				VLayout(*controls.collect({arg item; item.view}))
// 			)
// 		);
// 		win.layout.spacing = 1;
// 		win.layout.margins = [1,1,1,1];
// 		win.front;
// 	}
//
// 	killMeSpecial {
// 		nessyObjects.do{|obj| obj.killMeSpecial}
// 	}
//
// 	pause {
// 		mixerStrips.do{|item| item.mute};
// 		synths.do{|item| if(item!=nil, item.set(\pauseGate, 0))};
// 	}
//
// 	unpause {
// 		mixerStrips.do{|item| item.unmute};
// 		synths.do{|item| if(item!=nil,{item.set(\pauseGate, 1); item.run(true);})};
// 	}
//
// }