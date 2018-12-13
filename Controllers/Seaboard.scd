[ 2, 10, 19, 8, 17, 25, 40, 0, 1, 27, 34, 12 ].asSet.addAll([ 3, 2, 10, 19, 0, 1, 9, 18, 26, 8, 17, 25, 16, 24, 33, 11, 3 ]);

Collection

a = IdentityDictionary.new;

a.put('poop', 45)
a['45'].value


MIDIClient.init
MIDIIn.connectAll
MIDIFunc.trace

MIDIFunc.noteOn({arg ...notes; notes.postln}, nil)

MIDIFunc.cc({arg ...notes; notes.postln}, nil)

w = Window();
m = MultiSliderView();
w.layout = VLayout(m);
w.front

MIDIFunc.sysex({arg ...notes; notes.postln; {m.value = notes[0].asArray/127.0}.defer}, nil)

Quarks.gui

Modality-toolkit

MChanVoicer