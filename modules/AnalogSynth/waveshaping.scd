s = Server.internal.boot;
(

// in order to be turned into a wavetable,

// the signal must be half buffer size plus one

x = Signal.sineFill(513, [0.5, 0.2, 0.3, 0.0, 0.2]);

x.plot;

b = Buffer.alloc(s, 1024, 1);

)

b.sendCollection(x.asWavetableNoWrap);

(

{

var input, output;

input = SinOsc.ar(MouseX.kr(80, 800, \exponential), 0, MouseY.kr);

// Shaper is the waveshaping Ugen

output = Shaper.ar(b.bufnum, input);

output ! 2

}.scope;

)

Signal.chebyFill(1000, [1, 1]).plot;

(
s = Server.internal;

a = Signal.sineFill(1000, [1, 0.2, 0.7]);

// or..

//a = Signal.sineFill(1000, [0, 0.2, 0.8, 0.1, 0.5]);

// the straight version…

// a = Array.interpolation(1000, 0.0, 1.0);

// check out the transfer function

a.plot;

)

// then…

(

b = Buffer.sendCollection(s, a, 1);

)



(
{

var sinFreq, soundIn, playHead, output;

var thisIndex;

sinFreq = MouseX.kr(20, 1000, \exponential).poll;

soundIn = SinOsc.ar(sinFreq, 0, 0.8);

thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum));

// some gentle dynamic waveshaping -

// modulate the range of the indexing

// thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum) * SinOsc.kr(0.6).range(0.15, 1.0));

// some over-aggressive modulation!

// thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum) * SinOsc.ar(sinFreq * 0.25).range(0.15, 1.0));

playHead = BufRd.ar(1, b.bufnum, thisIndex, 0, 4);

// remove any DC weirdness

output = LeakDC.ar(playHead);

output ! 2;

}.scope;

)


(
{

var sinFreq, soundIn, playHead, output;

var thisIndex;

var sinMult, sinOffsetRange;

sinFreq = MouseX.kr(20, 2000, \exponential);

// now move the base position of the sine around, so that different areas of the transfer function get used

// resulting in a nice shifting around of the phase

sinMult = 0.32; sinOffsetRange = 0.64;

soundIn = SinOsc.ar(sinFreq, 0, sinMult, SinOsc.kr(0.17).range(0.0, – sinOffsetRange, sinOffsetRange));

thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum));

playHead = BufRd.ar(1, b.bufnum, thisIndex, 0, 4);

// remove any DC weirdness

output = LeakDC.ar(playHead);

output ! 2;

}.scope;

)

(
{

var sinFreq, soundIn, playHead, output;

var thisIndex;

var bufInput, bufInputFreq;

sinFreq = MouseX.kr(20, 1000, \exponential).poll;

soundIn = SinOsc.ar(sinFreq, 0, 0.8);

thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum));

// fill one cycle of the buffer without glitches -

// the frequency should be a period that fits into the size of the buffer,

// according to the sample rate

bufInputFreq = SampleRate.ir / BufFrames.kr(b.bufnum);

// mix the first 6 tones of the harmonic series – let them fade in and out randomly

bufInput = Mix(

SinOsc.ar(bufInputFreq * (1 .. 6), Array.linrand(6, 0.0, 6.28), SinOsc.kr(Array.exprand(6, 0.1, 0.3), 0, 0.2))

);

// write this into the buffer that is being used as the transfer function

BufWr.ar(bufInput, b.bufnum, Phasor.ar(0, BufRateScale.kr(b.bufnum), 0, BufFrames.kr(b.bufnum)));

playHead = BufRd.ar(1, b.bufnum, thisIndex, 0, 4);

// remove any DC weirdness

output = LeakDC.ar(playHead);

// uncomment to check out the transfer function

// output = BufRd.ar(1, b.bufnum, Phasor.ar(0, BufRateScale.kr(b.bufnum), 0, BufFrames.kr(b.bufnum)), 0, 4);

output ! 2;

}.scope;

)



(
{

var sinFreq, soundIn, playHead, output;

var thisIndex;

var bufInput, bufInputFreq;

var modPoints, env, envgen;

sinFreq = MouseX.kr(20, 1000, \exponential).poll;

soundIn = SinOsc.ar(sinFreq, 0, 0.8);

thisIndex = LinLin.ar(soundIn, -1.0, 1.0, 0.0, BufFrames.kr(b.bufnum));

bufInputFreq = SampleRate.ir / BufFrames.kr(b.bufnum);

// use these points for the env levels – one extra last one (release node) so that the env loops properly

modPoints = [-1.0, -0.2, -0.5, 0.9, 1.0, 1.0];

// uncomment to see the points

// modPoints.plot;

// uncomment to replace the points with modulating values

//	5.do { |i|

//	modPoints[i] = SinOsc.kr(ExpRand(0.1, 0.8), Rand(0.0, 6.28)).range(-0.9, 0.9);

//	};

// make an envelope which will result in a warped transfer function

env = Env(modPoints, [0.25, 0.25, 0.25, 0.25, 0.0], [2, -4, 7, -5, 0], 4, 0);

envgen = EnvGen.ar(env, timeScale: bufInputFreq.reciprocal);

bufInput = envgen;

// write this into the buffer that is being used as the transfer function

BufWr.ar(bufInput, b.bufnum, Phasor.ar(0, BufRateScale.kr(b.bufnum), 0, BufFrames.kr(b.bufnum)));

playHead = BufRd.ar(1, b.bufnum, thisIndex, 0, 4);

// remove any DC weirdness

output = LeakDC.ar(playHead);

// uncomment to check out the transfer function

// output = BufRd.ar(1, b.bufnum, Phasor.ar(0, BufRateScale.kr(b.bufnum), 0, BufFrames.kr(b.bufnum)), 0, 4);

output ! 2;

}.scope;

)