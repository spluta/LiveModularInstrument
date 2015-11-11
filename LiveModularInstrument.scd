//run the 2 server version of the software

LiveModularInstrument.boot(2, 0, 0, 64, 'normal', [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);

//run the 1 server version of the software
LiveModularInstrument.boot(1, 0, 0, 64, 'normal', [Manta_Mod.start, Lemur_Mod.start("10.0.0.3"), MIDI_Mod.start]);

