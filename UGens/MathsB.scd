{
	var freq;

	//freq = ImpulseB.ar(1, );

	freq = Pulse.ar(1, 0.5);

	LFTri.ar((freq+1)*200, 0, 0.1);
}.play

{LagUD.ar(LFPulseReset.ar(10, 0, 0.9), 0.5/10, 0.5/10)}.plot(1)

(
{ |freq=1, width=0.2, logExp = 0.5, loop = 0, trig|
	var slewUp, slewDown, slewMod, sig;

	slewUp = freq/width;
	slewDown = freq/(1-width);

	sig = Select.ar(loop, [Trig1.ar(trig, (1/freq)*width), LFPulseReset.ar(freq, 0, width, loop)]);

	slewMod = Slew.ar(sig, slewUp, slewDown);

	slewMod = LinSelectX.kr(logExp*2, [slewMod.explin(0.001, 1, 0, 1), slewMod, slewMod.linexp(0, 1, 0.001, 1)]);
}.play
)

{LFPulseReset.ar(0.5, 0, 0.5, MouseX.kr(-1,1)).poll; nil}.play


{LFPulseReset.ar(10, 0, 0.5, Impulse.kr(3))}.plot(1)

1%1

({
	var sig, maths, freq;

	maths = Maths.ar(MouseX.kr(1, 20), 0.1, 0.8, 1);
	freq = maths*5000+40;
	//LFTri.ar(freq, 0).distort*0.1
	sig = PMOsc.ar(freq, freq/2, MouseY.kr(0,12), 0).distort*0.1;
	sig.dup
}.play)


(
e = Env.linen(2, 5, 2);
Routine.run({
    loop({
        play({
            LinPan2.ar(EnvGen.ar(e) *
                PMOsc.ar(2000.0.rand,800.0.rand, Line.kr(0.0, 12.0.rand,9),0,0.1), 1.0.rand2)});
        2.wait;
    })
}))

(
{
    var ctls, mods, chans, freq;
    freq = LFNoise2.kr(0.3).exprange(200, 3000);
    ctls = [
        // freq, phase, amp
        [freq+2, 0, 1],
        [freq-2, 0, 1],
        [LFNoise2.kr(0.5).exprange(3, 80), 0, 1],
        [LFNoise2.kr(0.5).exprange(3, 800), 0, 1],
        [LFNoise2.kr(0.5).exprange(3, 800), 0, 1],
        [LFNoise2.kr(0.5).exprange(0.3, 10), 0, 1]
    ];
    mods = [
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseX.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseX.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3)
    ];
    chans = FM7.ar(ctls.flatten, mods).slice([2, 3]) * -12.dbamp;
}.play;
)

(
            var x= #[
	[ [ 0.0, -0.33333333333333, -1.0, 0.0 ], [ 0.75, 0.75, 0.0, -0.5 ], [ -0.5, -0.25, 0.25, -0.75 ], [ -0.5, 1.0, 1.0, 1.0 ], [ 0.0, 0.16666666666667, -0.75, -1.0 ], [ 0.5, 0.5, -0.5, 0.33333333333333 ] ],
	[ [ -0.33333333333333, 0.5, -0.5, -0.5 ], [ 0.5, 0.75, 0.25, 0.75 ], [ -0.83333333333333, 0.25, -1.0, 0.5 ], [ 1.5, 0.25, 0.25, -0.25 ], [ -0.66666666666667, -0.66666666666667, -1.0, -0.5 ], [ -1.0, 0.0, -0.83333333333333, -0.33333333333333 ] ],
	[ [ 0.25, -0.5, -0.5, -1.0 ], [ -0.5, 1.0, -1.5, 0.0 ], [ -1.0, -1.5, -0.5, 0.0 ], [ 0.5, -1.0, 1.1666666666667, -0.5 ], [ 0.83333333333333, -0.75, -1.5, 0.5 ], [ 0.25, -1.0, 0.5, 1.0 ] ],
	[ [ 1.0, 0.33333333333333, 0.0, -0.75 ], [ -0.25, 0.0, 0.0, -0.5 ], [ -0.5, -0.5, 0.0, 0.5 ], [ 1.0, 0.75, 0.5, 0.5 ], [ 0.0, 1.5, -0.5, 0.0 ], [ 1.0, 0.0, -0.25, -0.5 ] ],
	[ [ 0.5, -0.25, 0.0, 0.33333333333333 ], [ 0.25, -0.75, 0.33333333333333, -1.0 ], [ -0.25, -0.5, 0.25, -1.1666666666667 ], [ 0.0, 0.25, 0.5, 0.16666666666667 ], [ -1.0, -0.5, 0.83333333333333, -0.5 ], [ 0.83333333333333, -0.75, -0.5, 0.0 ] ],
	[ [ 0.0, -0.75, -0.16666666666667, 0.0 ], [ 1.0, 0.5, 0.5, 0.0 ], [ -0.5, 0.0, -0.5, 0.0 ], [ -0.5, -0.16666666666667, 0.0, 0.5 ], [ -0.25, 0.16666666666667, -0.75, 0.25 ], [ -1.1666666666667, -1.3333333333333, -0.16666666666667, 1.5 ] ]
];
            var y= #[ [ [ 0.0, -0.5, 1.0, 0.0 ], [ -0.5, 1.0, 0.5, -0.5 ], [ 0.0, 0.33333333333333, 1.0, 1.0 ] ], [ [ -0.5, 0.5, 1.0, 1.0 ], [ 0.0, 0.33333333333333, 0.0, 1.5 ], [ -0.5, 0.83333333333333, 1.0, 0.0 ] ], [ [ 0.25, -0.66666666666667, 0.25, 0.0 ], [ 0.5, -0.5, -0.5, -0.5 ], [ 0.5, -0.5, -0.75, 0.83333333333333 ] ], [ [ -0.25, 1.0, 0.0, 0.33333333333333 ], [ -1.25, -0.25, 0.5, 0.0 ], [ 0.0, -1.25, -0.25, -0.5 ] ], [ [ 0.75, -0.25, 1.5, 0.0 ], [ 0.25, -1.5, 0.5, 0.5 ], [ -0.5, -0.5, -0.5, -0.25 ] ], [ [ 0.0, 0.5, -0.5, 0.25 ], [ 0.25, 0.5, -0.33333333333333, 0.0 ], [ 1.0, 0.5, -0.16666666666667, 0.5 ] ] ];
            {
                var ctls= x.collect{|a| a.collect{|b| VarSaw.ar(*b)}};
                var mods= y.collect{|a| a.collect{|b| VarSaw.ar(*b)}};
                var chans= #[0, 1, 5, 4, 2];

	//ctls[0][1].poll;

	(Fold.ar(FM7.ar(ctls*MouseX.kr(0,2000), mods).slice(chans), -0.8, 0.8) * [1, 1, 1, 1, LFPar.kr(0.05).range(-40.dbamp, -60.dbamp)]).clump(2).sum;
            }.play)

(
            var x= #[
	[ [ 1.0, -0.33333333333333, -1.0, 0.0 ], 0, [ -0.5, -0.25, 0.25, -0.75 ]],
	[[ -0.5, 1.0, 1.0, 1.0 ], 0, [ 0.5, 0.5, -0.5, 0.33333333333333 ] ],
	[ [ -0.33333333333333, 0.5, -0.5, -0.5 ], 0, [ -0.83333333333333, 0.25, -1.0, 0.5 ]],
	[[ 1.5, 0.25, 0.25, -0.25 ], 0, [ -1.0, 0.0, -0.83333333333333, -0.33333333333333 ] ],
	[ [ 0.25, -0.5, -0.5, -1.0 ], 0, [ -1.0, -1.5, -0.5, 0.0 ]],
	[[ 0.5, -1.0, 1.1666666666667, -0.5 ], 0, [ 0.25, -1.0, 0.5, 1.0 ] ]
];

            var y= #[ [ [ 0.0, -0.5, 1.0, 0.0 ], [ -0.5, 1.0, 0.5, -0.5 ], [ 0.0, 0.33333333333333, 1.0, 1.0 ] ], [ [ -0.5, 0.5, 1.0, 1.0 ], [ 0.0, 0.33333333333333, 0.0, 1.5 ], [ -0.5, 0.83333333333333, 1.0, 0.0 ] ], [ [ 0.25, -0.66666666666667, 0.25, 0.0 ], [ 0.5, -0.5, -0.5, -0.5 ], [ 0.5, -0.5, -0.75, 0.83333333333333 ] ], [ [ -0.25, 1.0, 0.0, 0.33333333333333 ], [ -1.25, -0.25, 0.5, 0.0 ], [ 0.0, -1.25, -0.25, -0.5 ] ], [ [ 0.75, -0.25, 1.5, 0.0 ], [ 0.25, -1.5, 0.5, 0.5 ], [ -0.5, -0.5, -0.5, -0.25 ] ], [ [ 0.0, 0.5, -0.5, 0.25 ], [ 0.25, 0.5, -0.33333333333333, 0.0 ], [ 1.0, 0.5, -0.16666666666667, 0.5 ] ] ];
            {
	var ctls= x.collect{|a| [VarSaw.ar(*a[0].postln+LFNoise0.kr(0.5).range(-0.007,0.005)), 0, Pulse.ar(*a[2].postln+LFNoise0.kr(0.5).range(-0.007,0.005))]
}.postln;
                //var mods= y.collect{|a| a.collect{|b| VarSaw.ar(*b)}};
                var chans= #[0, 1, 2, 4, 5];

	var mods = [
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseX.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseX.kr(0, 3),
        { LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3),
		{ LFNoise1.kr(0.5).max(0) } ! 6 * MouseY.kr(0, 3)];

	(FM7.ar(ctls.flatten*MouseX.kr(0,100), mods.flatten/**MouseY.kr(0,4)*/).slice(chans) * [1, 1, LFSaw.kr(0.1).range(0, -12.dbamp), 1, -3.dbamp]).clump(2).sum*0.5;
            }.play)

{Pulse.ar(-2, 0.5).poll}.scope



"/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/CrossFeedback1/trainingFile0.csv"

({FM7.ar([[MouseX.kr(20, 10000), 0, 1], [MouseY.kr(20, 10000), 0, 1], [0,0,0], [0,0,0], [0,0,0], [0,0,0]],
	[
		[0, 1, 0, 0, 0, 0],
		[1, 0, 0, 0, 0, 0],
		[0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0]
]).slice([0])*0.1}.play)

		(
{
    var ctls, mods, chans;
    ctls = [
        // freq, phase, amp
        [LFNoise1.kr(0.5).range(300, 310), 0, 1],
        [LFNoise1.kr(0.5).exprange(3, 310), 0, 1],
        [LFNoise1.kr(0.5).exprange(3, 100), 0, 1],
        [LFNoise1.kr(0.5).exprange(3, 100), 0, 1],
        [LFNoise1.kr(0.5).exprange(3, 100), 0, 1],
        [LFNoise1.kr(0.5).exprange(3, 100), 0, 1]
    ];
    mods = [
        [Line.kr(0, 0.001, 2), Line.kr(0.1, 0, 4), 0, MouseY.kr(0, 6), 0, 0],
        [MouseX.kr(0, 6), 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0]
    ];
    chans = [0, 1];
    FM7.ar(ctls, mods).slice(chans) * -12.dbamp; // using "slice" to select specific channels
}.play;
)

DX7