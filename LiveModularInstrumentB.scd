Seaboard.start;
Seaboard_Mod.start;

SystemOverwrites

ModularVolumeObject

b = OSCFunc({|msg, time, addr|
	addr.postln;
}, '/Container2/Container2/Fader/x').oneShot;

OSCFunc.trace
NetAddr.langPort;

//get Lemur Ports
(a = OSCFunc({|msg, time, addr|
	addr.postln;
}, '/Switches/x').oneShot;
b = OSCFunc({|msg, time, addr|
	addr.postln;
}, '/Container2/Switches/x').oneShot;
)

(
HID.findAvailable;
a = HID.available;
a.do{|item| if(item.vendorName=="Logitech"){item.postln}}
)



(
//April2020
LiveModularInstrument.boot(9, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 58575]), Joystick_Mod.setPaths(["USB_046d_c215_2100000"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/May2020B");
)

(
LiveModularInstrument.boot(9, [1,2,5,6,4,4,3,3], 'normal', [Lemur_Mod.setPorts([8000, 61846]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Documents/Scores/Konus/SC/testSetup");
)

//Feedback TV
(
	LiveModularInstrument.boot(1, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 63278]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/SkyTV");
)


//Konus
(
LiveModularInstrument.boot(1, [1,2,3,4,9,10,13,13], 'normal', [Lemur_Mod.setPorts([8000, 63278]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod], "/Users/spluta/Documents/Scores/Konus/SC/feedback1c");
)

LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 61759]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["USB_046d_c215_14410000"]), Leap_Mod],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/nn_TV");


LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 55688]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["USB_046d_c215_14410000"])],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/testNN2");


LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 55688]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod, Joystick_Mod.setPaths(["USB_046d_c215_14410000"])],"/Users/spluta/Library/Application Support/SuperCollider/Extensions/saved LMI Sessions/testNN3");


StartUp.objects.size


71.midicps

65.midicps/4

VSTPlugin



//nothin
LiveModularInstrument.boot(1, [1,2,3,4,7, 8,7,8], 'normal', [Lemur_Mod.setPorts([8000, 58124]), Joystick_Mod.setPaths(["USB_046d_c215_14410000"]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);


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

ModularServers.servers[\lmi0].server.recChannels_(2)
ModularServers.servers[\lmi0].server.prepareForRecord
ModularServers.servers[\lmi0].server.record
ModularServers.servers[\lmi0].server.stopRecording



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


ModularServers.servers[\lmi1].server.queryAllNodes


ModularServers.servers[\lmi0].server.prepareForRecord
ModularServers.servers[\lmi0].server.record
ModularServers.servers[\lmi0].server.stop

OSCFunc.trace(false)