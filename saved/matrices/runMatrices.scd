LiveModularInstrument.boot(1, [0,1,2,3,4,5,6,7], nil, 64, nil, 'Matrices', [MantaOSC_Mod, Lemur_Mod, MIDI_Mod])

{Out.ar(0, PanAz.ar(5, WhiteNoise.ar, MouseX.kr))}.play