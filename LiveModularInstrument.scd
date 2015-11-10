LiveModularInstrument.boot(2, 0, 0, 64, 'normal', [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);




LiveModularInstrument.boot(1, 0, 0, 64, 'normal', [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);



gf drums thres slider


Alice Suite

fanfares - duo with ron
   duo with peter on overtones

A Drone

bass solo to duo

elemental
    do what I want
    process jim

12 earthly branches
    free agent - process jim in the middle?
    scary e minor
    process peter at the end in the swirlies

space piece

schizmogenesis



Space Piece

great expectations
    process peter for tune
    bowed cymbols for seconds half
tresilo
    process jim
    melody with peter
    duo with ron


MixerSolo_Mod

ringmod leaking to televisions - why is it 8 chan?

playbuf timer
fix setup with cutter/ring mod
volume on cutter not working on frozen sample
eric's playback through both setups


LiveModularInstrument.boot(1, 0, 0, 64);


LiveModularInstrument.boot(1, 0, 0, 64, 'atdV');
LiveModularInstrument.boot(1, 0, 0, 64, 'wubbels');

marty eurlick
jason robinson


LiveModularInstrument.boot(2, 0, 0, 64, 'normal', [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);


SignalMixer


LiveModularInstrument.boot(1, 0, 0, 64, 'feedback');


Lemur_Mod.resetOSCAddr("10.0.0.3")

Manta_Mod.sendRequest

OSCFunc
m

TFreeze rate
something is wrong with the hiding and viewing mechanism on save/load


try to make something that works!

Hide all current synths on server
Stop all midiOsc from getting to the objects
CPU usage meter


Convolution
FeedbackControl

MidiOscControl.actions


EnvTrigger
ShiftLayer



Done with EQ

add the server switcher
save/load  panel/server/setup

move things to Qt

Server

AmpFollower - doesn't look right
BuchlaModelSolo - assign Manta
Compander - broken?


LiveModularInstrument.addServer;
//something is broken here


writeArchive

MidiOscControl.actions[\lmi0].keys.do{arg item; item.postln}

MidiOscControl.actions[\lmi1]['/manta/value/1']

ModularServers.servers[\lmi0].server.queryAllNodes

ModularServers.servers[\lmi0].server.stereoInBusIndexes

ModularServers.servers[\lmi1].server.queryAllNodes

ModularServers.servers[\lmi2].server.queryAllNodes


LiveModularInstrument.boot(1, 0, 0, 64, 'wubbels');

LiveModularInstrument(1, 0, 0, 64, 'feedback');




part for alex opening with ian's part
send ian the full score

space junk is 4 channels
GrainFreezeDrums - fix to not crash

fucking ONSETS
switch mouse to LEMUR

summer features - load panel
quad setup


LiveModularInstrument(s, 0, 0, 64, 'wubbels');
add the mute to wub-rub

g quarter sharp - up to e

LiveModularInstrument(s, 0, 0, 64, 'atdV');



LiveModularInstrument(s, 0, 0, 128, 'lucerneVideo');

LiveModularInstrument(s, 0, 0, 64, 'feedback');

ModularServers.servers[\lmi0].server.recSampleFormat_("int24");
ModularServers.servers[\lmi0].server.prepareForRecord;
ModularServers.servers[\lmi0].server.record;
ModularServers.servers[\lmi0].server.stopRecording;
ModularServers.servers[\lmi0].server.recSampleFormat

s.recSampleFormat_("int24");
s.prepareForRecord;
s.record;
s.stopRecording;

s.makeGui

Server.local.makeGui

try {a = NetAddr("Bernard.local", 8000)}{a = nil};


OSCFunc.trace(true)
OSCFunc.trace(false)



LiveModularInstrument(s, 0, 0, 64, 'stocky');






LiveModularInstrument(s, 8, 0);

LiveModularInstrument(s, 8, 0, 'feedback');

"/Users/sam/Library/Application Support/SuperCollider/Extensions/interface 3.6/saved/PEQuintetSept2013/"
LiveModularInstrument(s, 0, 0, 'feedback');

OSCdef.freeAll;


LiveModularInstrument(s, 0, 0, 64, 'Matrices');
LiveModularInstrument(s, 0, 0, 'wubbels');
LiveModularInstrument(s, 0, 0, 'neuwirth');

LiveModularInstrument(s, 0, 0, 'installation');

LiveModularInstrument(s, 2, 2);

LiveModularInstrument(s, 14, 14);  //ADAT

LiveModularInstrument(s, 0, 14);  //Analog In - ADAT Out

s.queryAllNodes

FFT feedback software

