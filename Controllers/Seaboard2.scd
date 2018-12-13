a = IdentityDictionary();

a.put('turd', "poop")
a.put('turd', "dog")

b = OSCFunc({ arg msg, time; [time, msg].postln }, '/seaboardNote1/1');

n= NetAddr("127.0.0.1", 57120);
n.sendMsg("/seaboardNote"++1.asString++"/1", 45);

MIDIIn.doControlAction(1, 1, 1, 64);

TextField

a = Seaboard.start;

a.mode=0;

OSCFunc({arg ...vals; vals.postln}, '/SeaboardPressure/2');

a.start

OSCFunc.trace

matrix

OSCFunc('/SeaboardPressure').disable;

MIDIIn.connectAll;

MIDIFunc.cc({ arg val, num, chan, src; [val, num, chan, src].postln });

MIDIFunc.bend({ arg val, chan, src; [val, chan, src].postln });

MIDIFunc.touch({ arg val, chan, src; [val, chan, src].postln });

MIDIFunc.polytouch({ arg val, chan, src; [val, chan, src].postln });

2**13

24/(24/6)

a = (); // return a new Event.
a.put(\foo, 2.718);
a.at(\foo);
a.removeAt(\foo)