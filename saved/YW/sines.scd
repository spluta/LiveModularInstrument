{Splay.ar(SinOsc.ar(Line.ar((96..104).midicps, Array.series(9, 36, 4).midicps, 30),0, 0.05))}.play;


{Splay.ar(SinOsc.ar(Line.ar((21..30).midicps, Array.series(9, 36, 4).midicps, 30),0, 0.05))}.play


{WhiteNoise.ar(1)}.play


s.recSampleFormat_("int24");
s.prepareForRecord;
s.record;
s.stopRecording;
s.recSampleFormat