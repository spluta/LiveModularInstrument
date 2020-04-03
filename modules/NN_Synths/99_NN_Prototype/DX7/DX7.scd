

(
a = SynthDef("FM7_NNMod", {
	var ctls, mods, sig, envs, onOff;

	ctls = 2.collect{|i|2.collect{|i2| Pulse.ar(*["frC","widC","mulC","addC"].collect{|name| NamedControl.kr((name++i++i2).postln.asSymbol, 1.5.linrand+0.5, 0.1)})}.insert(1,0)}.addAll(0!3!4);

	mods = 2.collect{|i|2.collect{|i2| Pulse.ar(*["frM","widM","mulM","addM"].collect{|name| NamedControl.kr((name++i++i2).postln.asSymbol, 1.5.linrand+0.5, 0.1)})}.addAll(0!4)}.addAll(0!6!4);

	sig = Splay.ar(FM7.ar(ctls, mods).slice([0,1,2]), 1)*0.5;

	envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	onOff = Lag.kr(In.kr(\onOffBus.kr), 0.01);

	sig = Limiter.ar(sig*envs*onOff*Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1), 0.9);

	Out.ar(\outBus.kr(0), sig);

}).load(ModularServers.servers[\lmi1].server)
)

(
a = SynthDef("FM7_NNMod", {
	var ctls, mods, sig, envs, onOff;

	ctls = [
		[Pulse.ar(\frC00.kr(1.5.linrand+0.5), \widC00.kr(1.5.linrand+0.5), \mulC00.kr(1.5.linrand+0.5), \addC00.kr(1.5.linrand+0.5)).poll, 0, Pulse.ar(\frC01.kr(1.5.linrand+0.5), \widC01.kr(1.5.linrand+0.5), \mulC01.kr(1.5.linrand+0.5), \addC01.kr(1.5.linrand+0.5))],
		[Pulse.ar(\frC10.kr(1.5.linrand+0.5), \widC10.kr(1.5.linrand+0.5), \mulC10.kr(1.5.linrand+0.5), \addC10.kr(1.5.linrand+0.5)), 0, Pulse.ar(\frC11.kr(1.5.linrand+0.5), \widC11.kr(1.5.linrand+0.5), \mulC11.kr(1.5.linrand+0.5), \addC11.kr(1.5.linrand+0.5))],
		[Pulse.ar(\frC20.kr(1.5.linrand+0.5), \widC20.kr(1.5.linrand+0.5), \mulC20.kr(1.5.linrand+0.5), \addC20.kr(1.5.linrand+0.5)), 0, Pulse.ar(\frC21.kr(1.5.linrand+0.5), \widC21.kr(1.5.linrand+0.5), \mulC21.kr(1.5.linrand+0.5), \addC21.kr(1.5.linrand+0.5))],
		[0,0,0,],
		[0,0,0,],
		[0,0,0,]
	];

	mods = [
		[Pulse.ar(\frM00.kr(1.5.linrand+0.5), \widM00.kr(1.5.linrand+0.5), \mulM00.kr(1.5.linrand+0.5), \addM00.kr(1.5.linrand+0.5)), Pulse.ar(\frM01.kr(1.5.linrand+0.5), \widM01.kr(1.5.linrand+0.5), \mulM01.kr(1.5.linrand+0.5), \addM01.kr(1.5.linrand+0.5)), Pulse.ar(\frM02.kr(1.5.linrand+0.5), \widM02.kr(1.5.linrand+0.5), \mulM02.kr(1.5.linrand+0.5), \addM02.kr(1.5.linrand+0.5)), 0, 0, 0],
		[Pulse.ar(\frM10.kr(1.5.linrand+0.5), \widM10.kr(1.5.linrand+0.5), \mulM10.kr(1.5.linrand+0.5), \addM10.kr(1.5.linrand+0.5)), Pulse.ar(\frM11.kr(1.5.linrand+0.5), \widM11.kr(1.5.linrand+0.5), \mulM11.kr(1.5.linrand+0.5), \addM11.kr(1.5.linrand+0.5)), Pulse.ar(\frM12.kr(1.5.linrand+0.5), \widM12.kr(1.5.linrand+0.5), \mulM12.kr(1.5.linrand+0.5), \addM12.kr(1.5.linrand+0.5)), 0, 0, 0],
		[Pulse.ar(\frM20.kr(1.5.linrand+0.5), \widM20.kr(1.5.linrand+0.5), \mulM20.kr(1.5.linrand+0.5), \addM20.kr(1.5.linrand+0.5)), Pulse.ar(\frM21.kr(1.5.linrand+0.5), \widM21.kr(1.5.linrand+0.5), \mulM21.kr(1.5.linrand+0.5), \addM21.kr(1.5.linrand+0.5)), Pulse.ar(\frM22.kr(1.5.linrand+0.5), \widM22.kr(1.5.linrand+0.5), \mulM22.kr(1.5.linrand+0.5), \addM22.kr(1.5.linrand+0.5)), 0, 0, 0],
		0!6, 0!6, 0!6];

	sig = Splay.ar(FM7.ar(ctls, mods).slice([0,1,2]), 1)*0.5;

	envs = Envs.kr(\muteGate.kr(1), \pauseGate.kr(1), \gate.kr(1));

	onOff = 1;//Lag.kr(In.kr(\onOffBus.kr), 0.01);

	Out.ar(\outBus.kr(0), sig*envs*onOff*\vol.kr(1)/**Lag.kr(In.kr(\volBus.kr), 0.05).clip(0,1)*/);

}).load(ModularServers.servers[\lmi1].server)
)





a

a.set(\vol, 1)

/*	ctls = [
		[Pulse.ar(\frC00.kr(0), \widC00.kr(0), \mulC00.kr(0), \addC00.kr(0)), 0, Pulse.ar(\frC01.kr(0), \widC01.kr(0), \mulC01.kr(0), \addC01.kr(0))],
		[Pulse.ar(\frC10.kr(0), \widC10.kr(0), \mulC10.kr(0), \addC10.kr(0)), 0, Pulse.ar(\frC11.kr(0), \widC11.kr(0), \mulC11.kr(0), \addC11.kr(0))],
		[Pulse.ar(\frC20.kr(0), \widC20.kr(0), \mulC20.kr(0), \addC20.kr(0)), 0, Pulse.ar(\frC21.kr(0), \widC21.kr(0), \mulC21.kr(0), \addC21.kr(0))],
		[0,0,0,],
		[0,0,0,],
		[0,0,0,]
	];*/

/*	mods = [
		[Pulse.ar(\frM00.kr(0), \widM00.kr(0), \mulM00.kr(0), \addM00.kr(0)), Pulse.ar(\frM01.kr(0), \widM01.kr(0), \mulM01.kr(0), \addM01.kr(0)), Pulse.ar(\frM02.kr(0), \widM02.kr(0), \mulM02.kr(0), \addM02.kr(0)), 0, 0, 0],
		[Pulse.ar(\frM10.kr(0), \widM10.kr(0), \mulM10.kr(0), \addM10.kr(0)), Pulse.ar(\frM11.kr(0), \widM11.kr(0), \mulM11.kr(0), \addM11.kr(0)), Pulse.ar(\frM12.kr(0), \widM12.kr(0), \mulM12.kr(0), \addM12.kr(0)), 0, 0, 0],
		[Pulse.ar(\frM20.kr(0), \widM20.kr(0), \mulM20.kr(0), \addM20.kr(0)), Pulse.ar(\frM21.kr(0), \widM21.kr(0), \mulM21.kr(0), \addM21.kr(0)), Pulse.ar(\frM22.kr(0), \widM22.kr(0), \mulM22.kr(0), \addM22.kr(0)), 0, 0, 0],
		0!6, 0!6, 0!6];*/

a = { SinOsc.ar(3.collect{|i| NamedControl.kr(("freq"++i).postln.asSymbol, rrand(200,500), 0.1)}).sum * 0.1 }.play;

a.set(\freq1, 800)

("\\"++"frC"++0++0).interpret

Symbol

s.boot
//--more repetitive 2ch instant clicks 'n cuts with code generation /f0
//when you hear something you like, you can save it by copy&paste from the post window
(
Routine.run{
        var syn, time= 4;              //allow each patch to run for 16sec / 8bars
        inf.do{|i|
                ("\n//fm7 patch"+i+"generated on"+Date.localtime).post;
                syn= ("
                        (
                        var x= #"++({{{1.5.linrand+0.5}!4}!3}!2).asCompileString++";
                        var y= #"++({{{1.5.linrand+0.5}!4}!2}!2).asCompileString++";
                        {
var ctls= x.collect{|a| [LFSaw.ar(*a[0]), 0, LFSaw.ar(*a[2])]};
var mods= y.collect{|a| a.collect{|b| LFSaw.kr(*b)}.addAll(0!4)};

var chans= #[0, 1];
ctls = ctls.addAll(0!3!4);
mods = mods.addAll(0!6!4);


                                FM7.ar(ctls, mods).slice(chans)*0.5;
                        }.play)").postln.interpret;
                time.wait;
                syn.free;
        };
};
)

(
Routine.run{
        var syn, time= 4;              //allow each patch to run for 16sec / 8bars
        inf.do{|i|
                ("\n//fm7 patch"+i+"generated on"+Date.localtime).post;
                syn= ("
                        (
                        var x= #"++({{{1.5.linrand+0.5}!4}!3}!3).asCompileString++";
                        var y= #"++({{{1.5.linrand+0.5}!4}!3}!3).asCompileString++";
                        {
var ctls= x.collect{|a| a.collect{|b| Pulse.kr(*b)}};
var mods= y.collect{|a| a.collect{|b| Pulse.kr(*b)}.addAll(0!3)};

var chans= #[0, 1];
ctls = ctls.addAll(0!3!3);
mods = mods.addAll(0!6!3);


                                FM7.ar(ctls, mods).slice(chans)*0.5;
                        }.play)").postln.interpret;
                time.wait;
                syn.free;
        };
};
)

Fb1