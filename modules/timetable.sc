//in order to get this to work, I need to change it to a mixer like item similar to RingModStereo

// SnareSwitch_Mod : Module_Mod {
// 	var mainSnare;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("snareSwitch_mod", {arg inBus0, inBus1, outBus, t_trig, mainGate=1, snareGate=0, vol=0, gate = 1;
// 				var in0, in1, snare, env, mainEnv, snareEnv;
//
// 				mainEnv = EnvGen.kr(Env.asr(0.01, 1, 0.01), mainGate);
// 				snareEnv = EnvGen.kr(Env.asr(0.01, 1, 0.01), snareGate);
//
// 				snare = (SinOsc.ar(TRand.kr(50, 90, t_trig), 0, 0.4)+SinOsc.ar(TRand.kr(50, 90, t_trig), 0, 0.4))*snareEnv;
//
// 				in0 = In.ar(inBus0, 2)*mainEnv;
// 				in1 = In.ar(inBus1, 2)*mainEnv;
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
//
// 				Out.ar(outBus, in0*env);
// 				Out.ar(outBus+2, in1*env);
// 				Out.ar(outBus+4, [snare,snare]*env*vol);
// 				Out.ar(outBus+6, [snare,snare]*env*2);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("SnareSwitch", Rect(318, 645, 200, 130));
//
// 		this.initControlsAndSynths(5);
//
// 		mixerToSynthBusses = List.new;
// 		2.do{mixerToSynthBusses.add(Bus.audio(group.server, 2))};
//
// 		synths = List.new;
// 		synths.add(Synth("snareSwitch_mod", [\inBus0, mixerToSynthBusses[0].index, \inBus1, mixerToSynthBusses[1].index, \outBus, outBus], group));
//
// 		mainSnare = 0;
//
// 		controls.add(Button.new(win, Rect(5, 5, 95, 16))
// 			.states_([["snare", Color.black, Color.red], ["snare", Color.red, Color.black]])
// 			.action_{|v|
// 				controls[1].value = 1;
// 				v.value = 0;
// 				synths[0].set(\t_trig, 1, \snareGate, 1, \mainGate, 0);
// 				mainSnare = 1;
// 			}
// 		);
// 		this.addAssignButton(0, \onOff, Rect(5, 25, 95, 16));
//
// 		controls.add(Button.new(win, Rect(100, 5, 95, 16))
// 			.states_([["pass", Color.black, Color.red], ["pass", Color.red, Color.black]])
// 			.action_{|v|
// 				controls[0].value = 1;
// 				v.value = 0;
// 				synths[0].set(\snareGate, 0, \mainGate, 1);
// 				mainSnare = 0;
// 			}
// 		);
// 		this.addAssignButton(1, \onOff, Rect(100, 25, 95, 16));
//
// 		controls.add(Button.new(win, Rect(5, 45, 95, 16))
// 			.states_([["mute", Color.black, Color.red], ["mute", Color.red, Color.black]])
// 			.action_{|v|
// 				controls[3].value = 1;
// 				v.value = 0;
// 				synths[0].set(\snareGate, 0, \mainGate, 0);
// 			};
// 		);
// 		this.addAssignButton(2, \onOff, Rect(5, 65, 95, 16));
//
// 		controls.add(Button.new(win, Rect(100, 45, 95, 16))
// 			.states_([["unmute", Color.black, Color.red], ["unmute", Color.red, Color.black]])
// 			.action_{|v|
// 				controls[2].value = 1;
// 				v.value = 0;
// 				if(mainSnare == 0,{
// 					synths[0].set(\t_trig, 1, \snareGate, 0, \mainGate, 1);
// 					},{
// 						synths[0].set(\t_trig, 1, \snareGate, 1, \mainGate, 0);
// 				});
// 			};
// 		);
// 		this.addAssignButton(3, \onOff, Rect(100, 65, 95, 16));
//
// 		controls.add(EZSlider(win, Rect(5, 85, 170, 16),"vol", ControlSpec(0.0,1.0,\amp),
// 			{|v|
// 				synths[0].set(\vol, v.value);
// 		}, 0, false, 40, 40, 0, 16, \horz));
// 		this.addAssignButton(4, \continuous, Rect(175, 85, 16, 16));
//
// 	}
//
// 	/*	save {arg xmlDoc;
// 	xmlSynth = xmlDoc.createElement(modName);
// 	mantaData.do{arg item, i;
// 	xmlSynth.setAttribute("manta"++i.asString, item.asString);
// 	};
// 	midiData.do{arg item, i;
// 	xmlSynth.setAttribute("midi"++i.asString, item.asString);
// 	};
// 	xmlSynth.setAttribute("controls4", controls[4].value.asString);
// 	xmlSynth.setAttribute("bounds", win.bounds.asString);
// 	^xmlSynth;
// 	}
//
// 	load {arg xmlSynth;
// 	this.loadControllers(xmlSynth);
//
// 	midiHidTemp = xmlSynth.getAttribute("controls4");
// 	if(midiHidTemp!=nil,{
// 	controls[4].valueAction_(midiHidTemp.interpret);
// 	});
// 	win.bounds_(xmlSynth.getAttribute("bounds").interpret);
// 	win.front;
// 	}*/
// }
//
// LoopVidBuf_Mod : Module_Mod {
// 	var buffers, recGroup, playGroup, currentBuffer, volBus;
//
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("recNLoopRecorder_mod", {arg bufnum, inBus, outBus, volBus, t_trig, gate = 1;
// 				var in, env, out, vol, trig;
//
// 				in = In.ar(inBus, 2);
//
// 				in = Compander.ar(in, in,
// 					thresh: 1,
// 					slopeBelow: 1,
// 					slopeAbove: 0.5,
// 					clampTime: 0.01,
// 					relaxTime: 0.01
// 				);
//
// 				trig = Trig1.kr(t_trig, 0.1);
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
//
// 				RecordBuf.ar(in, bufnum, loop: 1, trigger: t_trig);
//
// 				vol = In.kr(volBus);
//
// 				Out.ar(outBus, in*env*vol);
// 			}).writeDefFile;
//
// 			SynthDef("recNLoopPlayer_mod", {arg bufnum, outBus, volBus, gate = 1;
// 				var in0, in1, env, out, vol;
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
//
// 				out = PlayBuf.ar(2, bufnum, loop: 1);
//
// 				vol = In.kr(volBus);
//
// 				ReplaceOut.ar(outBus, out*env*vol);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		win = Window.new("LoopVidBuf", Rect(318, 645, 200, 70));
// 		win.userCanClose_(false);
// 		modName = "LoopVidBuf";
// 		win.front;
//
// 		this.initControlsAndSynths(1);
//
// 		mixerToSynthBusses = List.new;
// 		mixerToSynthBusses.add(Bus.audio(group.server, 2));

// 		recGroup = Group.head(group);
// 		playGroup = Group.tail(group);
//
// 		volBus = Bus.control(group.server);
//
// 		synths = List.newClear(2);
// 		buffers = List.new;
// 		3.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.25, 1.5)), 2))};
// 		currentBuffer = 0;
// 		"buffer".postln;
// 		synths.put(0, Synth("recNLoopRecorder_mod",[\bufnum, buffers[0].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \outBus, outBus, \volBus, volBus.index], recGroup));
// 		/*
// 		controls.add(Button.new(win, Rect(5, 5, 60, 16))
// 		.states_([["play", Color.red, Color.black], ["play", Color.black, Color.red]])
// 		.action_{|v|
// 		switch(currentBuffer.postln,
// 		0, {
// 		buffers[1].free;
// 		buffers.put(1,Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.1, 2.5)), 2));
// 		synths[0].set(\gate, 0);
// 		synths.put(0, Synth("recNLoopRecorder_mod",[\bufnum, buffers[1].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \outBus, outBus, \volBus, volBus.index], recGroup));
// 		if(synths[1]!=nil,{
// 		synths[1].set(\gate, 0);
// 		});
// 		synths.put(1, Synth("recNLoopPlayer_mod", [\bufnum, buffers[0].bufnum.postln, \outBus, outBus, \volBus, volBus.index], playGroup));
// 		currentBuffer = 1;
// 		},
// 		1, {
// 		buffers[2].free;
// 		buffers.put(2,Buffer.alloc(group.server, group.server.sampleRate*(rrand(1, 2.5)), 2));
// 		synths[0].set(\gate, 0);
// 		synths.put(0, Synth("recNLoopRecorder_mod",[\bufnum, buffers[2].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \outBus, outBus, \volBus, volBus.index], recGroup));
// 		if(synths[1]!=nil,{
// 		synths[1].set(\gate, 0);
// 		});
// 		synths.put(1, Synth("recNLoopPlayer_mod", [\bufnum, buffers[1].bufnum.postln, \outBus, outBus, \volBus, volBus.index], playGroup));
// 		currentBuffer = 2;
// 		},
// 		2, {
// 		buffers[0].free;
// 		buffers.put(0,Buffer.alloc(group.server, group.server.sampleRate*(rrand(1, 2.5)), 2));
// 		synths[0].set(\gate, 0);
// 		synths.put(0, Synth("recNLoopRecorder_mod",[\bufnum, buffers[0].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \outBus, outBus, \volBus, volBus.index], recGroup));
// 		if(synths[1]!=nil,{
// 		synths[1].set(\gate, 0);
// 		});
// 		synths.put(1, Synth("recNLoopPlayer_mod", [\bufnum, buffers[2].bufnum.postln, \outBus, outBus, \volBus, volBus.index], playGroup));
// 		currentBuffer = 0;
// 		}
// 		);
// 		controls[1].value = 1;
// 		controls[2].value = 1;
// 		v.value = 0;
// 		}
// 		);
// 		this.addAssignButton(0, \onOff, Rect(5, 25, 60, 16));
//
// 		controls.add(Button.new(win, Rect(70, 5, 60, 16))
// 		.states_([["stop", Color.red, Color.black], ["stop", Color.black, Color.red]])
// 		.action_{|v|
// 		if(synths[1]!=nil,{
// 		synths[1].set(\gate, 0);
// 		});
// 		controls[0].value = 1;
// 		controls[2].value = 1;
// 		v.value = 0;
// 		};
// 		);
// 		this.addAssignButton(1, \onOff, Rect(70, 25, 60, 16));
//
// 		controls.add(Button.new(win, Rect(135, 5, 60, 16))
// 		.states_([["last", Color.red, Color.black], ["last", Color.black, Color.red]])
// 		.action_{|v|
// 		if(synths[1]!=nil,{
// 		synths[1].set(\gate, 0);
// 		});
// 		synths.put(1, Synth("recNLoopPlayer_mod", [\bufnum, buffers[(currentBuffer-1).wrap(0,2)].bufnum, \outBus, outBus, \volBus, volBus.index], playGroup));
// 		controls[0].value = 1;
// 		controls[1].value = 1;
// 		v.value = 0;
// 		};
// 		);
// 		this.addAssignButton(2, \onOff, Rect(135, 25, 60, 16));*/
//
// 		controls.add(EZSlider(win, Rect(5, 45, 170, 16),"vol", ControlSpec(0.0,8.0,\amp),
// 			{|v|
// 				volBus.set(v.value);
// 		}, 4, true, 40, 40, 0, 16, \horz));
// 		this.addAssignButton(0, \continuous, Rect(175, 45, 16, 16));
// 	}
//
// 	/*	load {arg xmlSynth;
// 	this.loadMidiData(xmlSynth);
// 	mantaData.size.do{arg i;
// 	midiHidTemp = xmlSynth.getAttribute("manta"++i.asString).interpret;
// 	if(midiHidTemp!=nil,{
// 	this.setManta(midiHidTemp[0], midiHidTemp[1], i);
// 	});
// 	};
// 	midiHidTemp = xmlSynth.getAttribute("controls3".asString);
// 	if(midiHidTemp!=nil,{
// 	controls[3].valueAction_(midiHidTemp.interpret);
// 	});
// 	win.bounds_(xmlSynth.getAttribute("bounds").interpret);
// 	win.front;
// 	}*/
// }
//
// LoopVidBuf2_Mod : Module_Mod {
// 	var buffers, recGroup, playGroup, currentBuffers, volBusses, blipRates;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("recNLoopRecorder2_mod", {arg bufnum, useBus, inBus, outBus, volBus, blipRate, t_trig, gate = 1;
// 				var in, inL, inR, env, out, vol, trig;
//
// 				in = In.ar(inBus, 8);
//
// 				inL = in[0]+in[2]+in[6];
// 				inR = in[1]+in[3]+in[7];
//
// 				in = Select.ar(useBus, [inL, inR]);
//
// 				in = Compander.ar(in, in,
// 					thresh: 1,
// 					slopeBelow: 1,
// 					slopeAbove: 0.5,
// 					clampTime: 0.01,
// 					relaxTime: 0.01
// 				);
//
// 				vol = In.kr(volBus);
// 				in = (in*vol + Blip.ar(blipRate, 15, (1-(Amplitude.kr(in)*vol)))).fold2(1);
//
// 				trig = Trig1.kr(t_trig, 0.1);
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
//
// 				RecordBuf.ar(in, bufnum, loop: 1, trigger: t_trig);
//
//
// 				Out.ar(outBus, in*env);
// 			}).writeDefFile;
//
// 			SynthDef("recNLoopPlayer2_mod", {arg bufnum, outBus, volBus, gate = 1;
// 				var in0, in1, env, out, vol;
//
// 				env = EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:2);
//
// 				out = PlayBuf.ar(1, bufnum, loop: 1);
//
// 				//vol = In.kr(volBus);
//
// 				ReplaceOut.ar(outBus, out*env);
// 			}).writeDefFile;
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("LoopVidBuf2", Rect(318, 645, 200, 140));
// 		//		win = Window.new("LoopVidBuf2", Rect(318, 645, 200, 140));
// 		//		win.userCanClose_(false);
// 		//		modName = "LoopVidBuf2";
// 		//		win.front;
//
// 		this.initControlsAndSynths(2);
//
// 		mixerToSynthBusses = List.new;
// 		mixerToSynthBusses.add(Bus.audio(group.server, 8));
//
// 		recGroup = Group.head(group);
// 		playGroup = Group.tail(group);
//
// 		volBusses = List.new;
// 		2.do{volBusses.add(Bus.control(group.server))};
//
// 		blipRates = List.new;
// 		blipRates.add(70);
// 		blipRates.add(60);
//
// 		synths = List.newClear(4);
// 		buffers = List.new;
// 		6.do{buffers.add(Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.25, 1.5)), 1))};
// 		currentBuffers = List.new;
// 		currentBuffers.add(0);
// 		currentBuffers.add(3);
//
// 		synths.put(0, Synth("recNLoopRecorder2_mod",[\bufnum, buffers[0].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \useBus, 0, \outBus, outBus.index, \volBus, volBusses[0].index, \blipRate, blipRates[0]], recGroup));
// 		synths.put(1, Synth("recNLoopRecorder2_mod",[\bufnum, buffers[3].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \useBus, 1, \outBus, outBus.index+1, \volBus, volBusses[1].index, \blipRate, blipRates[1]], recGroup));
//
// 		2.do{arg i;/*
// 			controls.add(Button.new(win, Rect(5, 5+(60*i), 60, 16))
// 			.states_([["play", Color.red, Color.black], ["play", Color.black, Color.red]])
// 			.action_{|v|
// 			switch(currentBuffers[i].postln%3,
// 			0, {
// 			buffers[1+(i*3)].free;
// 			buffers.put(1+(i*3),Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.1, 2.5)), 1));
// 			synths[i].set(\gate, 0);
// 			synths.put(i, Synth("recNLoopRecorder2_mod",[\bufnum, buffers[1+(i*3)].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \useBus, i, \outBus, outBus+i, \volBus, volBusses[i].index, \blipRate, blipRates[i]], recGroup));
// 			if(synths[2+(i)]!=nil,{
// 			synths[2+(i)].set(\gate, 0);
// 			});
// 			synths.put(2+(i), Synth("recNLoopPlayer2_mod", [\bufnum, buffers[0+(i*3)].bufnum.postln, \outBus, outBus+i, \volBus, volBusses[i].index], playGroup));
// 			currentBuffers.put(i, 1+(i*3));
// 			},
// 			1, {
// 			buffers[2+(i*3)].free;
// 			buffers.put(2+(i*3),Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.1, 2.5)), 1));
// 			synths[i].set(\gate, 0);
// 			synths.put(i, Synth("recNLoopRecorder2_mod",[\bufnum, buffers[2+(i*3)].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \useBus, i, \outBus, outBus+i, \volBus, volBusses[i].index, \blipRate, blipRates[i]], recGroup));
// 			if(synths[2+(i)]!=nil,{
// 			synths[2+(i)].set(\gate, 0);
// 			});
// 			synths.put(2+(i), Synth("recNLoopPlayer2_mod", [\bufnum, buffers[1+(i*3)].bufnum.postln, \outBus, outBus+i, \volBus, volBusses[i].index], playGroup));
// 			currentBuffers.put(i, 2+(i*3));
// 			},
// 			2, {
// 			buffers[(i*3)].free;
// 			buffers.put((i*3),Buffer.alloc(group.server, group.server.sampleRate*(rrand(0.1, 2.5)), 1));
// 			synths[i].set(\gate, 0);
// 			synths.put(i, Synth("recNLoopRecorder2_mod",[\bufnum, buffers[(i*3)].bufnum.postln, \inBus, mixerToSynthBusses[0].index, \useBus, i, \outBus, outBus+i, \volBus, volBusses[i].index, \blipRate, blipRates[i]], recGroup));
// 			if(synths[2+(i)]!=nil,{
// 			synths[2+(i)].set(\gate, 0);
// 			});
// 			synths.put(2+(i), Synth("recNLoopPlayer2_mod", [\bufnum, buffers[2+(i*3)].bufnum.postln, \outBus, outBus+i, \volBus, volBusses[i].index], playGroup));
// 			currentBuffers.put(i, (i*3));
// 			}
// 			);
//
// 			controls[1+(i*4)].value = 1;
// 			controls[2+(i*4)].value = 1;
// 			v.value = 0;
// 			}
// 			);
// 			this.addAssignButton(0+(i*4), 0, Rect(5, 25+(60*i), 60, 16));
//
// 			controls.add(Button.new(win, Rect(70, 5+(60*i), 60, 16))
// 			.states_([["stop", Color.red, Color.black], ["stop", Color.black, Color.red]])
// 			.action_{|v|
// 			if(synths[2+i]!=nil,{
// 			synths[2+i].set(\gate, 0);
// 			});
// 			controls[0+(i*4)].value = 1;
// 			controls[2+(i*4)].value = 1;
// 			v.value = 0;
// 			};
// 			);
// 			this.addAssignButton(1+(i*4), 0, Rect(70, 25+(60*i), 60, 16));
//
// 			controls.add(Button.new(win, Rect(135, 5+(60*i), 60, 16))
// 			.states_([["last", Color.red, Color.black], ["last", Color.black, Color.red]])
// 			.action_{|v|
// 			if(synths[2+i]!=nil,{
// 			synths[2+i].set(\gate, 0);
// 			});
// 			synths.put(1, Synth("recNLoopPlayer2_mod", [\bufnum, buffers[(currentBuffers[i]-1).wrap(0,2)].bufnum, \outBus, outBus+i, \volBus, volBusses[i].index], playGroup));
// 			controls[0+(i*4)].value = 1;
// 			controls[1+(i*4)].value = 1;
// 			v.value = 0;
// 			};
// 			);
// 			this.addAssignButton(2+(i*4), 0, Rect(135, 25+(60*i), 60, 16));
// 			*/
// 			controls.add(EZSlider(win, Rect(5, 45+(60*i), 170, 16),"vol", ControlSpec(0.0,8.0,\amp),
// 				{|v|
// 					volBusses[i].set(v.value);
// 			}, 4, true, 40, 40, 0, 16, \horz));
// 			this.addAssignButton(i, 1, Rect(175, 45+(60*i), 16, 16));
// 		};
// 	}
//
// 	/*	load {arg xmlSynth;
// 	this.loadMidiData(xmlSynth);
// 	mantaData.size.do{arg i;
// 	midiHidTemp = xmlSynth.getAttribute("manta"++i.asString).interpret;
// 	if(midiHidTemp!=nil,{
// 	this.setManta(midiHidTemp[0], midiHidTemp[1], i);
// 	});
// 	};
// 	2.do{arg i;
// 	midiHidTemp = xmlSynth.getAttribute(("controls3"++(i*4)).asString);
// 	if(midiHidTemp!=nil,{
// 	controls[3].valueAction_(midiHidTemp.interpret);
// 	});
// 	};
// 	win.bounds_(xmlSynth.getAttribute("bounds").interpret);
// 	win.front;
// 	}*/
// }