
//with the iPad
/*BuchlaModelSolo_Mod :  Module_Mod {
var filterList, rqMax, freqs, filtNums, outGroup, specs, synthGroup, filterGroup, transferBus, buchlaFilters, straightPitch, straightPitchBool, triggerButtons, continuousButtons, ampSpec, fmAmpSpec, padFunctions, noteOnFunctions, noise1Select, noise2Select, ampModNoiseSelect, trigVal, baseVol, pitchBus;

var arpRout, arpRoutA, arpArray, arpCounter = 0, arpNumber = 12, arpWait = 1, arpWaitSpec, deviation = 0, arpFreq = 400, arpeggioSwitch = false;

*initClass {
StartUp.add {
SynthDef("buchlaFMPatchSolo_mod", {arg inBus, outBus, pitchBus, amp0 = 0, amp1=0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0, amp6 = 0, amp7 = 0, freq0 = 300, freq1=200, arpFreq = 400, ampModFreq=0, ampModVol = 0, noise1Select=0, noise2Select = 0, ampModNoiseSelect=0, t_trig, lagTime = 0.1, gate = 1, pauseGate = 1, zvol=0, zamp2=0, zamp3=0, zamp4=0, zamp5=0, zamp6=0, zamp7=0;
var layer2, layer1, layer0, ampMod, seq, env, pauseEnv, layer1Noise, in, freq, hasFreq;

in = In.ar(inBus);

#freq, hasFreq = Tartini.kr(in);

Out.kr(pitchBus, freq);

amp2 = min(amp2, zamp2);
amp3 = min(amp3, zamp3);
amp4 = min(amp4, zamp4);
amp5 = min(amp5, zamp5);
amp6 = min(amp6, zamp6);
amp7 = min(amp7, zamp7);

layer2 = WhiteNoise.ar(amp5)+PinkNoise.ar(amp6)+BrownNoise.ar(amp7);

layer2 = Select.ar(noise2Select, [layer2, Latch.ar(layer2, Dust.ar(TRand.ar(200, 1000, noise2Select)))]);

layer1Noise = BrownNoise.ar(amp4);
layer1Noise = Select.ar(noise1Select, [layer1Noise, Latch.ar(layer1Noise, Dust.ar(TRand.ar(200, 1000, noise1Select)))]);

layer1 = SinOsc.ar(freq1+layer2,0,amp2)+LFSaw.ar(freq1+layer2,0,amp3)+layer1Noise;

ampModVol = min(Lag.kr(zvol), ampModVol);

ampMod = SinOsc.ar(ampModFreq+(Select.kr(ampModNoiseSelect, [0, LFNoise2.kr(TRand.kr(0.5, 2, ampModNoiseSelect)).range(0, 30)])), 0, ampModVol/2, ampModVol/2);



layer0 = SinOsc.ar(freq0+layer1, 0, ampModVol-(ampMod*Lag.kr((ampModFreq>1), 1/ampModFreq)));


pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);

Out.ar(outBus, [layer0,layer0]*env*pauseEnv)
}).writeDefFile
}
}

init {
this.makeWindow("BuchlaModelSolo",Rect(200, 230, 140, 280));

pitchBus = Bus.control(group.server);

this.initControlsAndSynths(48);
this.makeMixerToSynthBus;

baseVol = 0;
controls.add(QtEZSlider.new("baseVol", ControlSpec(0,1,'amp'),
{|v|
baseVol = v.value;
synths[0].set(\ampModVol, ampSpec.map(baseVol));
}, 0));

straightPitch = 60;
straightPitchBool = false;


controls.add(QtEZSlider.new("trigVal", ControlSpec(1,12,'lin',1),
{|v|
trigVal = v.value;
}, 6));

controls.add(Button.new()
.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
.action_{|v|
if(v.value==1,{
this.setManta;
},{
this.clearMidiOsc;
})
});

controls.add(Button()
.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
.action_{|butt|
switch(butt.value,
0, {
numChannels = 2;
3.do{|i| buchlaFilters[i+1].killMe};
},
1, {
numChannels = 4;
buchlaFilters.put(1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+2));
},
2, {
if(numChannels==2,{
3.do{|i| buchlaFilters.put(i+1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+1))))};
},{
2.do{|i| buchlaFilters.put(i+2,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+2))))};
});
numChannels = 8;

}
)
};
);

win.layout_(
VLayout(
HLayout(controls[0].layout, controls[1].layout),
HLayout(controls[2], controls[3])
)
);


specs = List.new;
specs.add(ControlSpec(200, 1000, 'exponential'));
specs.add(ControlSpec(500, 3000, 'exponential'));
specs.add(ControlSpec(500, 3000, 'exponential'));
specs.add(ControlSpec(500, 3000, 'exponential'));
specs.add(ControlSpec(200, 1000, 'exponential'));
specs.add(ControlSpec(500, 3000, 'exponential'));
specs.add(ControlSpec(0.0001, 100, 'exponential'));

synthGroup = Group.tail(group);
filterGroup = Group.tail(group);
transferBus = Bus.audio(group.server, 2);

synths = List.new;
synths.add(Synth("buchlaFMPatchSolo_mod", [\inBus, mixerToSynthBus, \outBus, transferBus.index, \pitchBus, pitchBus], synthGroup));

buchlaFilters = List.newClear(4);
buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));

triggerButtons = [ 2, 10, 19, 8, 17, 25, 40, 0, 1, 27, 34, 12, 3, 9, 18, 26, 16, 24, 33 ];
continuousButtons = [ 3, 2, 10, 19, 0, 1, 9, 18, 26, 8, 17, 25, 16, 24, 33, 11, 3 ];

ampSpec = ControlSpec(0,1,'linear');
fmAmpSpec = ControlSpec(0,3000,'amp');

this.addFunctions;
this.addPitchArpeggiator;
}

pause {
buchlaFilters.do{arg item; if(item!=nil,{item.pause})};
synths.do{|item| item.set(\pauseGate, 0)};
}

unpause {
buchlaFilters.do{arg item; if(item!=nil,{item.unpause})};
synths.do{|item| item.set(\pauseGate, 1); item.run(true)};
}

setManta {
var counter=0, key2, function;

padFunctions.keys.asSet.addAll(noteOnFunctions.keys).do{arg key;

key2 = "/MultiBall"++(100+key).asString;
oscMsgs.put(counter, key2);

function = {|xyz, val|
switch(xyz.asSymbol,
'y', {padFunctions[key].value(val)},
'z', {noteOnFunctions[key].value(val)}
)
};

MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], function, setups);
counter=counter+1;
};

}

addPitchArpeggiator {
arpArray = List.fill(12, {400});

arpRoutA = Routine({
{
pitchBus.get({arg val;
arpArray = arpArray.rotate;
arpArray.put(0, val);
});
0.2.wait;
}.loop});

SystemClock.play(arpRoutA);

arpRout = Routine({
{


arpFreq = arpArray[arpCounter];

if(deviation.coin, {
arpFreq = arpFreq*[2,3,4,5,6,7,8,9,10,11,12,13,14,15].choose*[0.5,1,2].choose;
});

if(arpeggioSwitch, {
synths[0].set(\freq0, arpFreq);
});

arpCounter = (arpCounter+1)%arpNumber;

arpWait.wait;
}.loop});
SystemClock.play(arpRout);
}

addFunctions {
noteOnFunctions = IdentityDictionary.new;

//the notes
noteOnFunctions.put(triggerButtons[0], {arg val;
specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'));
arpeggioSwitch = false;
});
noteOnFunctions.put(triggerButtons[1], {arg val;
specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'));
arpeggioSwitch = false;
});
noteOnFunctions.put(triggerButtons[2], {arg val;
specs.put(2, ControlSpec(rrand(600,950), rrand(5000, 15000), 'exponential'));
arpeggioSwitch = false;
});


noteOnFunctions.put(triggerButtons[3], {arg val; if(val==1,{specs.put(3, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'))})});
noteOnFunctions.put(triggerButtons[4], {arg val; if(val==1,{specs.put(4, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))})});
noteOnFunctions.put(triggerButtons[5], {arg val; if(val==1,{specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))})});
noteOnFunctions.put(triggerButtons[6], {arg val; if(val==1,{buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}})});

noteOnFunctions.put(triggerButtons[7], {arg val; arpeggioSwitch = true; arpNumber = [3,5,7,11].choose});
noteOnFunctions.put(triggerButtons[8], {arg val; arpeggioSwitch = true; arpWaitSpec = ControlSpec(rrand(0.05, 0.1), rrand(0.2, 0.4), 'exponential');});

noise1Select = Pseq([0,1],inf).asStream;
noise2Select = Pseq([0,1],inf).asStream;
ampModNoiseSelect = Pseq([0,1],inf).asStream;
noteOnFunctions.put(triggerButtons[9], {arg val; synths[0].set(\noise1Select, noise1Select.next)});
noteOnFunctions.put(triggerButtons[10], {arg val; synths[0].set(\noise2Select, noise2Select.next)});
noteOnFunctions.put(triggerButtons[11], {arg val; synths[0].set(\ampModNoiseSelect, ampModNoiseSelect.next)});
noteOnFunctions.put(triggerButtons[12], {arg val; synths[0].set(\zvol, val)});
noteOnFunctions.put(triggerButtons[13], {arg val; synths[0].set(\zamp2, fmAmpSpec.map(val))});
noteOnFunctions.put(triggerButtons[14], {arg val; synths[0].set(\zamp3, fmAmpSpec.map(val))});
noteOnFunctions.put(triggerButtons[15], {arg val; synths[0].set(\zamp4, fmAmpSpec.map(val))});
noteOnFunctions.put(triggerButtons[16], {arg val; synths[0].set(\zamp5, fmAmpSpec.map(val))});
noteOnFunctions.put(triggerButtons[17], {arg val; synths[0].set(\zamp6, fmAmpSpec.map(val))});
noteOnFunctions.put(triggerButtons[18], {arg val; synths[0].set(\zamp7, fmAmpSpec.map(val))});

padFunctions = IdentityDictionary.new;

if(straightPitchBool != true, {
padFunctions.put(continuousButtons[1], {arg val;  synths[0].set(\freq0, specs[0].map(val))});
padFunctions.put(continuousButtons[2], {arg val;  synths[0].set(\freq0, specs[1].map(val))});
padFunctions.put(continuousButtons[3], {arg val;  synths[0].set(\freq0, specs[2].map(val))});
});

arpWaitSpec = ControlSpec(0.05, 0.2, 'exponential');
padFunctions.put(continuousButtons[4], {arg val;  deviation = val});
padFunctions.put(continuousButtons[5], {arg val;  arpWait = arpWaitSpec.map(val)});

padFunctions.put(continuousButtons[6], {arg val;  synths[0].set(\amp2, fmAmpSpec.map(max(val,0)))});

padFunctions.put(continuousButtons[7], {arg val;  synths[0].set(\amp3, fmAmpSpec.map(max(val,0)))});
padFunctions.put(continuousButtons[8], {arg val;  synths[0].set(\amp4, fmAmpSpec.map(max(val,0)))});
padFunctions.put(continuousButtons[9], {arg val;  synths[0].set(\freq1, specs[3].map(max(val,0)))});

padFunctions.put(continuousButtons[10], {arg val;  synths[0].set(\freq1, specs[4].map(max(val,0)))});
padFunctions.put(continuousButtons[11], {arg val;  synths[0].set(\freq1, specs[5].map(max(val,0)))});
padFunctions.put(continuousButtons[12], {arg val;  synths[0].set(\amp5, fmAmpSpec.map(max(val,0)))});

padFunctions.put(continuousButtons[13], {arg val;  synths[0].set(\amp6, fmAmpSpec.map(max(val,0)))});
padFunctions.put(continuousButtons[14], {arg val;  synths[0].set(\amp7, fmAmpSpec.map(max(val,0)))});
padFunctions.put(continuousButtons[15], {arg val;  synths[0].set(\ampModFreq, specs[6].map(max(val,0)))});

padFunctions.put(continuousButtons[16], {arg val;  val = val+baseVol; synths[0].set(\ampModVol, val)});
}

trigger {
synths[0].set(\t_trig, 1);
}

killMeSpecial {
buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
arpRout.stop;
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
//this does not save or load the oscMsgs

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
}*/


//with the Manta
// BuchlaModelSolo_Mod :  Module_Mod {
// 	var filterList, rqMax, freqs, filtNums, outGroup, specs, synthGroup, filterGroup, transferBus, buchlaFilters, straightPitch, straightPitchBool, triggerButtons, continuousButtons, ampSpec, fmAmpSpec, padFunctions, noteOnFunctions, noise1Select, noise2Select, ampModNoiseSelect, trigVal, baseVol, pitchBus;
//
// 	var arpRout, arpRoutA, arpArray, arpCounter = 0, arpNumber = 12, arpWait = 1, arpWaitSpec, deviation = 0, arpFreq = 400, arpeggioSwitch = false;
//
// 	*initClass {
// 		StartUp.add {
// 			SynthDef("buchlaFMPatchSolo_mod", {arg inBus, outBus, pitchBus, amp0 = 0, amp1=0, amp2 = 0, amp3 = 0, amp4 = 0, amp5 = 0, amp6 = 0, amp7 = 0, freq0 = 300, freq1=200, arpFreq = 400, ampModFreq=0, ampModVol = 0, noise1Select=0, noise2Select = 0, ampModNoiseSelect=0, t_trig, lagTime = 0.1, gate = 1, pauseGate = 1;
// 				var layer2, layer1, layer0, ampMod, seq, env, pauseEnv, layer1Noise, in, freq, hasFreq;
//
// 				in = In.ar(inBus);
//
// 				#freq, hasFreq = Tartini.kr(in);
//
// 				Out.kr(pitchBus, freq);
//
// 				layer2 = WhiteNoise.ar(amp5)+PinkNoise.ar(amp6)+BrownNoise.ar(amp7);
//
// 				layer2 = Select.ar(noise2Select, [layer2, Latch.ar(layer2, Dust.ar(TRand.ar(200, 1000, noise2Select)))]);
//
// 				layer1Noise = BrownNoise.ar(amp4);
// 				layer1Noise = Select.ar(noise1Select, [layer1Noise, Latch.ar(layer1Noise, Dust.ar(TRand.ar(200, 1000, noise1Select)))]);
//
// 				layer1 = SinOsc.ar(freq1+layer2,0,amp2)+LFSaw.ar(freq1+layer2,0,amp3)+layer1Noise;
//
// 				ampMod = SinOsc.ar(ampModFreq+(Select.kr(ampModNoiseSelect, [0, LFNoise2.kr(TRand.kr(0.5, 2, ampModNoiseSelect)).range(0, 30)])), 0, ampModVol/2, ampModVol/2);
//
//
//
// 				layer0 = SinOsc.ar(freq0+layer1, 0, ampModVol-(ampMod*Lag.kr((ampModFreq>1), 1/ampModFreq)));
//
//
// 				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);
//
// 				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction: 2);
//
// 				Out.ar(outBus, [layer0,layer0]*env*pauseEnv)
// 			}).writeDefFile
// 		}
// 	}
//
// 	init {
// 		this.makeWindow("BuchlaModelSolo",Rect(200, 230, 140, 280));
//
// 		pitchBus = Bus.control(group.server);
//
// 		this.initControlsAndSynths(48);
// 		this.makeMixerToSynthBus;
//
// 		baseVol = 0;
// 		controls.add(QtEZSlider.new("baseVol", ControlSpec(0,200,'linear'),
// 			{|v|
// 				baseVol = v.value;
// 				synths[0].set(\ampModVol, ampSpec.map(baseVol/200));
// 		}, 0));
//
// 		straightPitch = 60;
// 		straightPitchBool = false;
//
//
// 		controls.add(QtEZSlider.new("trigVal", ControlSpec(1,12,'lin',1),
// 			{|v|
// 				trigVal = v.value;
// 		}, 6));
//
// 		controls.add(Button.new()
// 			.states_([ [ "A-Manta", Color.red, Color.black ] ,[ "C-Manta", Color.black, Color.red ] ])
// 			.action_{|v|
// 				if(v.value==1,{
// 					this.setManta;
// 					},{
// 						this.clearMidiOsc;
// 				})
// 		});
//
// 		controls.add(Button()
// 			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
// 			.action_{|butt|
// 				switch(butt.value,
// 					0, {
// 						numChannels = 2;
// 						3.do{|i| buchlaFilters[i+1].killMe};
// 					},
// 					1, {
// 						numChannels = 4;
// 						buchlaFilters.put(1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+2));
// 					},
// 					2, {
// 						if(numChannels==2,{
// 							3.do{|i| buchlaFilters.put(i+1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+1))))};
// 							},{
// 								2.do{|i| buchlaFilters.put(i+2,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index+(2*(i+2))))};
// 						});
// 						numChannels = 8;
//
// 					}
// 				)
// 			};
// 		);
//
// 		win.layout_(
// 			VLayout(
// 				HLayout(controls[0].layout, controls[1].layout),
// 				HLayout(controls[2], controls[3])
// 			)
// 		);
//
//
// 		specs = List.new;
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(200, 1000, 'exponential'));
// 		specs.add(ControlSpec(500, 3000, 'exponential'));
// 		specs.add(ControlSpec(0.0001, 100, 'exponential'));
//
// 		synthGroup = Group.tail(group);
// 		filterGroup = Group.tail(group);
// 		transferBus = Bus.audio(group.server, 2);
//
// 		synths = List.new;
// 		synths.add(Synth("buchlaFMPatchSolo_mod", [\inBus, mixerToSynthBus, \outBus, transferBus.index, \pitchBus, pitchBus], synthGroup));
//
// 		buchlaFilters = List.newClear(4);
// 		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));
//
// 		triggerButtons = [ 2, 10, 19, 8, 17, 25, 40, 0, 1, 27, 34, 12 ];
// 		continuousButtons = [ 3, 2, 10, 19, 0, 1, 9, 18, 26, 8, 17, 25, 16, 24, 33, 11, 3 ];
//
// 		ampSpec = ControlSpec(0,1,'linear');
// 		fmAmpSpec = ControlSpec(0,3000,'amp');
//
// 		this.addFunctions;
// 		this.addPitchArpeggiator;
// 	}
//
// 	pause {
// 		buchlaFilters.do{arg item; if(item!=nil,{item.pause})};
// 		synths.do{|item| item.set(\pauseGate, 0)};
// 	}
//
// 	unpause {
// 		buchlaFilters.do{arg item; if(item!=nil,{item.unpause})};
// 		synths.do{|item| item.set(\pauseGate, 1); item.run(true)};
// 	}
//
// 	setManta {
// 		var counter=0;
//
// 		padFunctions.keys.do{arg key;
// 			//oscMsgs.put(counter, "/manta/value/"++key.asString);
// 			oscMsgs.put(counter, "/manta/pad/"++key.asString);
// 			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], padFunctions[key], setups);
// 			counter=counter+1;
// 		};
// 		noteOnFunctions.keys.do{arg key;
// 			//oscMsgs.put(counter, "/manta/noteOn/"++key.asString);
// 			oscMsgs.put(counter, "/manta/padOn/"++key.asString);
// 			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], noteOnFunctions[key], setups);
// 			counter=counter+1;
// 		};
//
// 	}
//
// 	addPitchArpeggiator {
// 		arpArray = List.fill(12, {400});
//
// 		arpRoutA = Routine({
// 			{
// 				pitchBus.get({arg val;
// 					arpArray = arpArray.rotate;
// 					arpArray.put(0, val);
// 				});
// 				0.2.wait;
// 		}.loop});
//
// 		SystemClock.play(arpRoutA);
//
// 		arpRout = Routine({
// 			{
//
//
// 				arpFreq = arpArray[arpCounter];
//
// 				if(deviation.coin, {
// 					arpFreq = arpFreq*[2,3,4,5,6,7,8,9,10,11,12,13,14,15].choose*[0.5,1,2].choose;
// 				});
//
// 				if(arpeggioSwitch, {
// 					synths[0].set(\freq0, arpFreq);
// 				});
//
// 				arpCounter = (arpCounter+1)%arpNumber;
//
// 				arpWait.wait;
// 		}.loop});
// 		SystemClock.play(arpRout);
// 	}
//
// 	addFunctions {
// 		noteOnFunctions = IdentityDictionary.new;
//
// 		//the notes
// 		noteOnFunctions.put(triggerButtons[0], {arg val;
// 			specs.put(0, ControlSpec(rrand(40,250), rrand(800, 1500), 'exponential'));
// 			arpeggioSwitch = false;
// 		});
// 		noteOnFunctions.put(triggerButtons[1], {arg val;
// 			specs.put(1, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'));
// 			arpeggioSwitch = false;
// 		});
// 		noteOnFunctions.put(triggerButtons[2], {arg val;
// 			specs.put(2, ControlSpec(rrand(600,950), rrand(5000, 15000), 'exponential'));
// 			arpeggioSwitch = false;
// 		});
//
//
// 		noteOnFunctions.put(triggerButtons[3], {arg val; specs.put(3, ControlSpec(rrand(5,15), rrand(20, 80), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[4], {arg val; specs.put(4, ControlSpec(rrand(400,750), rrand(1500, 3000), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[5], {arg val; specs.put(5, ControlSpec(rrand(600,950), rrand(1800, 10000), 'exponential'))});
// 		noteOnFunctions.put(triggerButtons[6], {arg val; buchlaFilters.do{|item| if(item!=nil,{item.trigger(trigVal)})}});
//
// 		noteOnFunctions.put(triggerButtons[7], {arg val; arpeggioSwitch = true; arpNumber = [3,5,7,11].choose});
// 		noteOnFunctions.put(triggerButtons[8], {arg val; arpeggioSwitch = true; arpWaitSpec = ControlSpec(rrand(0.05, 0.1), rrand(0.2, 0.4), 'exponential');});
//
// 		noise1Select = Pseq([0,1],inf).asStream;
// 		noise2Select = Pseq([0,1],inf).asStream;
// 		ampModNoiseSelect = Pseq([0,1],inf).asStream;
// 		noteOnFunctions.put(triggerButtons[9], {arg val; synths[0].set(\noise1Select, noise1Select.next)});
// 		noteOnFunctions.put(triggerButtons[10], {arg val; synths[0].set(\noise2Select, noise2Select.next)});
// 		noteOnFunctions.put(triggerButtons[11], {arg val; synths[0].set(\ampModNoiseSelect, ampModNoiseSelect.next)});
//
// 		padFunctions = IdentityDictionary.new;
//
// 		if(straightPitchBool != true, {
// 			padFunctions.put(continuousButtons[1], {arg val;  synths[0].set(\freq0, specs[0].map(val/200))});
// 			padFunctions.put(continuousButtons[2], {arg val;  synths[0].set(\freq0, specs[1].map(val/200))});
// 			padFunctions.put(continuousButtons[3], {arg val;  synths[0].set(\freq0, specs[2].map(val/200))});
// 		});
//
// 		arpWaitSpec = ControlSpec(0.05, 0.2, 'exponential');
// 		padFunctions.put(continuousButtons[4], {arg val;  deviation = val/200});
// 		padFunctions.put(continuousButtons[5], {arg val;  arpWait = arpWaitSpec.map(val/200)});
//
// 		padFunctions.put(continuousButtons[6], {arg val;  synths[0].set(\amp2, fmAmpSpec.map(max(val-20,0)/200))});
//
// 		padFunctions.put(continuousButtons[7], {arg val;  synths[0].set(\amp3, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[8], {arg val;  synths[0].set(\amp4, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[9], {arg val;  synths[0].set(\freq1, specs[3].map(max(val-20,0)/200))});
//
// 		padFunctions.put(continuousButtons[10], {arg val;  synths[0].set(\freq1, specs[4].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[11], {arg val;  synths[0].set(\freq1, specs[5].map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[12], {arg val;  synths[0].set(\amp5, fmAmpSpec.map(max(val-20,0)/200))});
//
// 		padFunctions.put(continuousButtons[13], {arg val;  synths[0].set(\amp6, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[14], {arg val;  synths[0].set(\amp7, fmAmpSpec.map(max(val-20,0)/200))});
// 		padFunctions.put(continuousButtons[15], {arg val;  synths[0].set(\ampModFreq, specs[6].map(max(val-20,0)/200))});
//
// 		padFunctions.put(continuousButtons[16], {arg val;  val = val+baseVol; synths[0].set(\ampModVol, ampSpec.map(max(val-20,0)/200))});
// 	}
//
// 	trigger {
// 		synths[0].set(\t_trig, 1);
// 	}
//
// 	killMeSpecial {
// 		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
// 		arpRout.stop;
// 	}
//
// 	save {
// 		var saveArray, temp;
//
// 		saveArray = List.newClear(0);
//
// 		saveArray.add(modName); //name first
//
// 		temp = List.newClear(0); //controller settings
// 		controls.do{arg item;
// 			temp.add(item.value);
// 		};
//
// 		saveArray.add(temp);  //controller messages
// 		//this does not save or load the oscMsgs
//
// 		saveArray.add(win.bounds);
//
// 		this.saveExtra(saveArray);
// 		^saveArray
// 	}
//
// 	load {arg loadArray;
// 		loadArray[1].do{arg controlLevel, i;
// 			if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
// 		};
// 		win.bounds_(loadArray[3]);
// 		this.loadExtra(loadArray);
// 	}
//
// }


/*GingerMan_Mod : Module_Mod {
	var synthGroup, filterGroup, buchlaFilters, transferBus, sinTemp0, sinTemp1, gmanTemp0, gmanTemp1, getSin, getGman, spec, noteOnFunctions, sliderFunctions, startTime, saveButton;

	*initClass {
		StartUp.add {

			SynthDef("gman",{arg sinFreq0, sinFreq1, gmanFreq0, gmanFreq1, dustFreq0, dustFreq1, dustFreq2, dustFreq3, dustFreq4, dustFreq5, selectFAM2=0, outBus, onOff = 1, vol = 0, gate=1, pauseGate = 1;
				var trig0, trig1, trig2, sinFreq, gmanFreq, out, fastAmpMod, slowAmpMod, fastAmpMod2, ampMod, pauseEnv, env;


				sinFreq = LFNoise0.kr(LFNoise0.kr(2).range(0.1, 8)).range(sinFreq0, sinFreq1);
				gmanFreq = LFNoise0.kr(LFNoise0.kr(5).range(0.1, 8)).range(gmanFreq0, gmanFreq1);

				trig0 = Dust.kr(LFNoise0.kr(2).range(dustFreq0, dustFreq1));
				fastAmpMod = Trig1.kr(trig0, TRand.kr(0.015,0.4,trig0));

				trig1 = Dust.kr(LFNoise0.kr(0.5).range(dustFreq2, dustFreq3));
				slowAmpMod = Lag.kr(1-Trig1.kr(trig1, TRand.kr(1.5/(dustFreq2+0.001*2), 1.5/(dustFreq3+0.001*2),trig1)), 0.001);

				trig2 = Dust.kr(LFNoise0.kr(0.5).range(dustFreq4, dustFreq5));
				fastAmpMod2 = Select.kr(selectFAM2, [1, Lag.kr(Trig1.kr(trig2, TRand.kr(0.01,0.05,trig2)), 0.001)]);

				ampMod = fastAmpMod*slowAmpMod*fastAmpMod2*Lag.kr(onOff, 0.001);

				out = SinOsc.ar(sinFreq, GbmanN.ar(gmanFreq) ,vol*ampMod).dup;

				//SendTrig.kr(Trig1.kr(ampMod, 0.01), 1, 1);
				//SendTrig.kr(Trig1.kr((1-ampMod).abs, 0.01), 0, 0);

				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				env = EnvGen.kr(Env.asr(0,1,0), gate, doneAction:2);

				Out.ar(outBus, [out[0], Delay2.ar(out[1])].distort*pauseEnv*env);

				Out.ar(outBus+2, [Delay2.ar(out[0]), out[1]].distort*pauseEnv*env);
				Out.ar(outBus+4, [out[0], Delay2.ar(out[1])].distort*pauseEnv*env);
				Out.ar(outBus+6, [Delay2.ar(out[0]), out[1]].distort*pauseEnv*env);

			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("GingerMan", Rect(600, 300, 200, 60));
		this.initControlsAndSynths(40);

		synthGroup = Group.tail(group);
		filterGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 8);

		synths.add(Synth("gman", [\sinFreq0, 20, \sinFreq1, 20000, \gmanFreq0, 20, \gmanFreq1, 20000, \dustFreq0, 35, \dustFreq1, 40, \dustFreq2, 0, \dustFreq3, 0, \outBus, transferBus.index], synthGroup));

		buchlaFilters = List.newClear(4);
		buchlaFilters.put(0,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index, outBus.index));

		controls.add(Button()
			.states_([["Assign", Color.red, Color.black], ["Clear", Color.black, Color.red]])
			.action_{arg val;
				if(val.value==1,{
					this.setManta;
				},{
					this.clearMidiOsc;
				})
			}
		);

		controls.add(QtEZSlider("vol", ControlSpec(0,2,'amp'),
			{|slider| synths[0].set(\vol, slider.value)}, 1, true, \horz)
		);
		this.addAssignButton(1,\continuous);

		win.layout_(VLayout(controls[0], HLayout(controls[1].layout), assignButtons[1].layout));
		win.layout.spacing = 0;

		noteOnFunctions = IdentityDictionary.new;
		sliderFunctions = IdentityDictionary.new;
		this.addFunctions;

/*		controls.add(Button(win,Rect(10, 65, 60, 20))
			.states_([["2", Color.black, Color.white],["4", Color.black, Color.white],["8", Color.black, Color.white]])
			.action_{|butt|
				switch(butt.value,
					0, {
						numChannels = 2;
						3.do{|i| buchlaFilters[i+1].killMe};
					},
					1, {
						numChannels = 4;
						buchlaFilters.put(1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+2, outBus.index+2));
					},
					2, {
						if(numChannels==2,{
							3.do{|i| buchlaFilters.put(i+1,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+4, outBus.index+(2*(i+1))))};
						},{
							2.do{|i| buchlaFilters.put(i+2,BuchlaFiltersSynths_Mod(filterGroup, transferBus.index+6, outBus.index+(2*(i+2))))};
						});
						numChannels = 8;

					}
				)
			};
		);*/
	}

	setManta {
		var counter=0;

/*		sliderFunctions.keys.do{arg key;
			oscMsgs.put(counter, "/manta/slider/"++key.asString);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], sliderFunctions[key], setups);
			counter=counter+1;
		};*/
		noteOnFunctions.keys.do{arg key;
			var key2;
			/*oscMsgs.put(counter, "/manta/padOn/"++key.asString);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], noteOnFunctions[key], setups);*/

			key2 = "/MultiBall"++(100+key).asString;
			oscMsgs.put(counter, key2);
			MidiOscControl.setControllerNoGui(group.server, oscMsgs[counter], noteOnFunctions[key], setups);
			counter=counter+1;
		};

	}


	addFunctions {
		6.do{arg gmanCount;
			6.do{arg sinCount;
				noteOnFunctions.put(((gmanCount*8)+(sinCount)), {|xyz, val|
						if(xyz.asSymbol=='z', {
						this.getSin(sinCount);
						this.getGman(gmanCount);
						synths[0].set(\sinFreq0, sinTemp0, \sinFreq1, sinTemp1, \gmanFreq0, gmanTemp0, \gmanFreq1, gmanTemp1);
						buchlaFilters.do{arg item; if(item!=nil,{item.trigger})};
					});
				})
			}
		};


		noteOnFunctions.put(46, {|xyz, val|
			if(xyz.asSymbol=='z', {synths[0].set(\onOff, 1)})
		});
		noteOnFunctions.put(47, {|xyz, val|
			if(xyz.asSymbol=='z', {synths[0].set(\onOff, 0)})
		});

		//noteOnFunctions.put(39, {buchlaFilters.do{arg item; if(item!=nil,{item.trigger})};});

		spec = ControlSpec(0,1,\amp);
		//sliderFunctions.put(1, {arg val; synths[0].set(\vol, spec.map(val/8192))});

	}

	getSin  {arg num;
		switch(num,
			0,{
				sinTemp0 = exprand(20, 80);
				sinTemp1 = sinTemp0+40.linrand;
			},
			1,{
				sinTemp0 = exprand(80, 320);
				sinTemp1 = sinTemp0+180.linrand;
			},
			2,{
				sinTemp0 = exprand(320, 1100);
				sinTemp1 = sinTemp0+579.linrand;
			},
			3,{
				sinTemp0 = exprand(1100, 4000);
				sinTemp1 = sinTemp0+2700.linrand;
			},
			4,{
				sinTemp0 = exprand(4000, 10000);
				sinTemp1 = sinTemp0+7777.linrand;
			},
			5,{
				sinTemp0 = exprand(10000, 20000);
				sinTemp1 = sinTemp0+5000.linrand;
			}
		)
	}

	getGman  {arg num;
		switch(num,
			0,{
				gmanTemp0 = exprand(20, 80);
				gmanTemp1 = gmanTemp0+40.linrand;
			},
			1,{
				gmanTemp0 = exprand(80, 320);
				gmanTemp1 = gmanTemp0+180.linrand;
			},
			2,{
				gmanTemp0 = exprand(320, 1100);
				gmanTemp1 = gmanTemp0+579.linrand;
			},
			3,{
				gmanTemp0 = exprand(1100, 4000);
				gmanTemp1 = gmanTemp0+2700.linrand;
			},
			4,{
				gmanTemp0 = exprand(4000, 10000);
				gmanTemp1 = gmanTemp0+7777.linrand;
			},
			5,{
				gmanTemp0 = exprand(10000, 20000);
				gmanTemp1 = gmanTemp0+5000.linrand;
			}
		)
	}

	save {
		var saveArray, temp;

		saveArray = List.newClear(0);

		saveArray.add(modName); //name first

		temp = List.newClear(0); //controller settings
		controls.do{arg item;
			temp.add(item.value);
		};

		saveArray.add(temp);

		//this does not save or load the oscMsgs
		//it takes advantage that the setManta button is a control button
		//and it just saves the state of that button

		saveArray.add(win.bounds);
		^saveArray
	}

	load {arg loadArray;
		loadArray[1].do{arg controlLevel, i;
			if(controls[i].value!=controlLevel, {controls[i].valueAction_(controlLevel)});
		};
		win.bounds_(loadArray[3]);
	}

	killMeSpecial {
		buchlaFilters.do{arg item; if(item!=nil,{item.killMe})};
		transferBus.free;
		group.freeAllMsg;
	}
}*/