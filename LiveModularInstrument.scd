n = NetAddr("left.local", 7000);
n.sendMsg('/1/fader26', 1.0.rand);

n = NetAddr("right.local", 7001);
n.sendMsg('/fader33', 1.0.rand);

thisProcess.openUDPPort(6000);
thisProcess.openUDPPort(6001);

OSCFunc.trace(true);
OSCFunc.trace(false);


FeedbackSynth_Mod
Joystick_Mod.start

MIDIClient.init;
MIDIIn.connectAll
(
"dscacheutil -q host -a name 'right.local'".unixCmd;
"dscacheutil -q host -a name 'left.local'".unixCmd;
)


(
var blackChans = 16;
var analogChans = 0;//8;
var micChans = 0;//4;
var aesADAT = 0;//18;
var startChan = blackChans+analogChans+micChans+aesADAT;
//July2022
OSCReceiver_Mod.ips_(["left.local","right.local"]);

LiveModularInstrument.boot(8, startChan+[1,2,3,4,5,6,7,8], (128!7).add(2)/*sets the blockSize*/, 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/Oct_27_23");
)


(
var blackChans = 16;
var analogChans = 0;//8;
var micChans = 0;//4;
var aesADAT = 0;//18;
var startChan = blackChans+analogChans+micChans+aesADAT;
//July2022
OSCReceiver_Mod.ips_(["left.local","right.local"]);

LiveModularInstrument.boot(8, startChan+[1,2,3,4,5,6,7,8], (128!7).add(2)/*sets the blockSize*/, 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"])/*, Seaboard.channel_(9), Seaboard_Mod*/, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/June_26_23B");
)

(
var blackChans = 0;
var analogChans = 0;//8;
var micChans = 0;//4;
var aesADAT = 0;//10;
var startChan = blackChans+analogChans+micChans+aesADAT;
//July2022
LiveModularInstrument.boot(1, startChan+[1,2,3,4,5,6,7,8], (128!1)/*sets the blockSize*/, 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"])/*, Seaboard.channel_(9), Seaboard_Mod*/, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/testProto");
)



//add this to June_22_23 as its own page
//add mariel's proto stuff to that page

(
var blackChans = 16;
var analogChans = 8;
var micChans = 4;
var aesADAT = 18;
var startChan = blackChans+analogChans+micChans+aesADAT;
//July2022
LiveModularInstrument.boot(1, startChan+[1,2,3,4,5,6,7,8], (128!7).add(64)/*sets the blockSize*/, 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard.channel_(9), Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/Mincek_08_21_I");
)




ProtoType_Mod

{SinOsc.ar(200, 0, 0.1)}.play

MIDIClient.init

(
//Matrix4GL - April 2023
//
LiveModularInstrument.boot(1, [1,2,3,4,5,6,7,8], [128]/*sets the blockSize*/, 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard.channel_(9), Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod]);
)

(
//TECTONICS
var blackChans = 16;
var analogChans = 8;
var micChans = 4;
var aesADAT = 18;
var startChan = blackChans+analogChans+micChans+aesADAT;
LiveModularInstrument.boot(4, startChan+[1,2,3,4,5,6,7,8], [128,128,64], 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard.channel_(9), Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/FeedbackSax/tectonics_2_9B");
)



(
//TECTONICS
var blackChans = 16;
var analogChans = 8;
var micChans = 4;
var aesADAT = 18;
var startChan = blackChans+analogChans+micChans+aesADAT;
LiveModularInstrument.boot(4, startChan+[1,2,3,4,5,6,7,8], [128,128,64], 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard.channel_(9), Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/FeedbackSax/tectonics_2_9B");
)



(
//TV Feedback 2023
LiveModularInstrument.boot(2, [1,2,5,6,7,8,3,4], [128], 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard.channel_(9), Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod],"/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/TVs2023E");
)




(var blackChans = 0;
var analogChans = 0;
var micChans = 0;
var aesADAT = 0;
var startChan = blackChans+analogChans+micChans+aesADAT;
LiveModularInstrument.boot(1, startChan+[1,2,5,6,7, 8,9,8], [128], 'normal', [OSCReceiver_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod],"/Users/spluta1/Library/Application Support/SuperCollider/saved LMI Sessions/testVerb"));
)



(
//May2020
LiveModularInstrument.boot(11, [1,2,5,6,7, 8,9,8], 'normal', [Lemur_Mod.setPorts([8000, 56151]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro2"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/TimeSpans_N");
)


(
LiveModularInstrument.boot(1, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 55488]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, MIDI_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/Mincek_08_21_I");
)


"RingMod".toLower

MIDIFunc.trace(true)



Server.killAll

sendCollection

s.options.outDevice_("ZoomAudioD");
s.options.inDevice_("MacBook Pro Microphone");

LotkaVolterraC

s.reboot
{SoundIn.ar(0)}.play

//Konus
(
LiveModularInstrument.boot(2, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 63278]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta1/Documents/Scores/binary_momentary IV/SC/feedback2021C");
)

OSCReceiver_Mod.oscDict['/7000/8/xy1']

(
LiveModularInstrument.boot(1, [1,2,3,4,9,10,13,13], 'normal', [OSCReceiver_Mod, Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/touchOSC");
)


62.midicps/2




ModularServers.servers[\lmi1].server.queryAllNodes

FluidMLPRegressor

remove point is broken

ServerOptions.devices;
s.options.device = "ZoomAudioD"


//when switching to predict==0, get the vals from the bus and write them to the other bus
(
LiveModularInstrument.boot(1, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 55163]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/tinyLoops");
)


(
LiveModularInstrument.boot(1, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 55163]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/fb_test");
)


(
LiveModularInstrument.boot(1, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 59001]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod],
	"/Users/spluta1/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/proto"
);
)

a = NetAddr("127.0.0.1", 8000)

a.sendMsg('/Container/Fader/x', 1.0.rand)

a = ModularServers.servers[\lmi1].server.asString;
b = a.copyRange(3,a.size-1)

Seaboard.start;
Seaboard_Mod.start;

SystemOverwrites

ModularVolumeObject



OSCFunc.trace(true)
OSCFunc.trace(false)

NetAddr.langPort

//get Lemur Ports
(
)

(
HID.findAvailable;
a = HID.available;
a.do{|item| if(item.vendorName=="Logitech"){item.postln}}
)
Joystick_Mod.running_(true);
Joystick_Mod.running_(false);
Joystick_Mod.setPaths(["logitech-extreme-3d-pro"])
Joystick_Mod.start




LiveModularInstrument.boot(2, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 61846]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/1 ipad/testQuit")


(
LiveModularInstrument.boot(3, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 61846]), Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/1 ipad/testNN7");
)





"/Users/spluta1/Documents/Scores/binary_momentary IV/SC/"

//Feedback TV
(
	LiveModularInstrument.boot(1, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 63278]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/SkyTV");
)


//Konus
(
LiveModularInstrument.boot(2, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 63278]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta1/Documents/Scores/binary_momentary IV/SC/feedback2021B");
)



LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 61759]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["USB_046d_c215_14410000"]), Leap_Mod],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/nn_TV");


LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 55688]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["USB_046d_c215_14410000"])],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/testNN2");


LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 55688]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["logitech-extreme-3d-pro", "nintendo-switch-pro", "nintendo-switch-pro"])],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/1 ipad/testSP0");


StartUp.objects.size


71.midicps

65.midicps/4

VSTPlugin


Joystick_Mod.setPaths(["USB_046d_c215_2100000"]).start


//nothin
LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 60509]), Joystick_Mod.setPaths(["USB_046d_c215_2100000"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);


Slew


OSCFunc.trace

MIDIFunc.trace

57.midicps

//
//nothin
LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 58124]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);


//zActions_env and update Lemur Sliders

LiveModularInstrument.boot(1, [1,2,3,4,5, 6,7,8], 'normal', [Lemur_Mod.setPorts([8000, 62176]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/NN_Synths");

LiveModularInstrument.boot(1, [1,2,3,4,5, 6,7,8], 'normal', [Lemur_Mod.setPorts([8000, 62176]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);

//Bryn Harrison
LiveModularInstrument.boot(1, [1,2,4,9,11, 6,7,8], 'normal', [Lemur_Mod.setPorts([8000, 58124]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/Harrison");


//Just MIDIMixer
LiveModularInstrument.boot(1, [1,2,3,4,10, 19,3,4], 'normal', [MIDI_Mod]);

MidiOscControl.actions[\lmi1]

MidiOscControl.actions[\lmi7]


TypeOSCFunc_Mod.responders.do{arg item; item.postln;}
MidiOscControl.actions[\lmi1]['/1/fader15'].value(0.1)

MidiOscControl.actions[\lmi1]['/Container2/Fader4/z'].value(1)

MidiOscControl.actions[\lmi1].keys.do{|i|i.postln;}

ServerSwitcher

TouchOSC_Mod.netAddrs[1].sendMsg('/n/multitoggle1/1/3', 1)

MidiOscControl.actions

OSCFunc({|...msg| msg.postln;}, '/Reverb/x', nil, 9002)

OSCFunc.trace

OSCFunc.trace(false)



Lemur_Mod.setPorts([8000, 8001])

ModularServers.getObjectBusses(ModularServers.servers[\lmi0].server).collect({arg item; item.index}).indexOf(104)+1




ServerOptions.devices

Server.local.options.device_("Fireface UFX II (23856143)");

LiveModularInstrument.boot(2, [14,15,16,17,18, 19,20,21], 'normal', [Lemur_Mod, Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);

(
MIDIClient.init;
MIDIIn.connectAll;
MIDIFunc.trace(false);
)

//fly's eyes
LiveModularInstrument.boot(1, [0,1,4,5,6,7,2,3], 'normal', [Lemur_Mod, Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);

OSCFunc.trace


ModularServers.servers[\lmi1].server.queryAllNodes

ModularServers.servers[\lmi1].server.asString=="lmi1"

	n = NetA

LiveModularInstrument.boot(3, [0,1,4,5,6,7,2,3], nil, 64, nil, 'normal', [MantaOSC_Mod, Lemur_Mod]);

//run the 2 server version of the softwar2SignalSwitcher
LiveModularInstrument.boot(2, [0,1,4,5,6,7,2,3], nil, 64, nil, 'normal', [MantaOSC_Mod, Lemur_Mod]);

"/Users/spluta/Library/"

"/Users/spluta/Library/Application Support/SuperCollider/"

LiveModularInstrument.boot(1, [0,1,2,3,4,5,6,7], nil, 64, nil, 'normal', [MantaOSC_Mod, Lemur_Mod, Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);

LiveModularInstrument.boot(1, [8,9,10,11,0,5,6,7], nil, 64, nil, 'Matrices', [MantaOSC_Mod, Lemur_Mod, MIDI_Mod]);

LiveModularInstrument.boot(1, [0,1,2,3,4,5,6,7], nil, 64, nil, 'feedback', [MantaOSC_Mod, Lemur_Mod, MIDI_Mod]);[ 52, S 0 ]


LiveModularInstrument.boot(1, nil, nil, 64, nil, 'wubbels', [MantaCocoa_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);

s.numInputBusChannels

FreqScope.new(400, 200, 0, server: ModularServers.servers[\lmi0].server);
Scope

(
w = Window.new("my own scope", Rect(20, 20, 400, 500));
w.view.decorator = FlowLayout(w.view.bounds);
c = Stethoscope.new(ModularServers.servers[\lmi0].server, view:w.view);
w.onClose = { c.free }; // don't forget this
w.front;
)



//run the 1 server version of the software for feedback
LiveModularInstrument.boot(1, 0, 0, 64, nil, 'feedback', [MantaCocoa_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);


LiveModularInstrument.boot(1, 0, 0, 64, 44100, 'normal', [nil, nil, MIDI_Mod.start]);
Stethoscope(ModularServers.servers[\lmi0].server,1);


LiveModularInstrument.boot(1, 0, 0, 64, 44100, 'neuwirth', [MantaCocoa_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);

//incorporate Modality into the system


ModularServers.servers[\lmi10].server.queryAllNodes


ModularServers.servers[\lmi0].server.prepareForRecord
ModularServers.servers[\lmi0].server.record
ModularServers.servers[\lmi0].server.stop

OSCFunc.trace(false)

Integrator
