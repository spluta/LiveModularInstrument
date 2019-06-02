Seaboard.start
Seaoboard_Mod.start

(
//Server.local.options.device = "Fireface UFX II (23856143)";

LiveModularInstrument.boot(9, [9,10,11,4,1,10,3,3], 'normal', [TouchOSC_Mod.setPorts([8001, 8002]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);
)

LiveModularInstrument.boot(1, [1,2,3,4,10, 19,3,4], 'normal', [TouchOSC_Mod.setPorts([8001, 8002]), Seaboard, Seaboard_Mod, TypeOSCFunc_Mod]);


//Just MIDI
LiveModularInstrument.boot(1, [1,2,3,4,10, 19,3,4], 'normal', [MIDI_Mod]);




TypeOSCFunc_Mod.responders
MidiOscControl.actions

ServerSwitcher

TouchOSC_Mod.netAddrs[1].sendMsg('/n/multitoggle1/1/3', 1)

MidiOscControl.actions

OSCFunc({|...msg| msg.postln;}, '/Reverb/x', nil, 9002)

OSCFunc.trace

OSCFunc.trace(false)

Lemur_Mod.setPorts([8000, 8001])

ModularServers.getObjectBusses(ModularServers.servers[\lmi0].server).collect({arg item; item.index}).indexOf(104)+1

/Fader22/x


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

ModularServers.servers[\lmi0].server

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


ModularServers.servers[\lmi0].server.queryAllNodes


ModularServers.servers[\lmi0].server.prepareForRecord
ModularServers.servers[\lmi0].server.record
ModularServers.servers[\lmi0].server.stop

OSCFunc.trace(false)