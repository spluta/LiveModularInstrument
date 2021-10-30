{
	Mix(SinOsc.ar((1..200)*100, 0, 0.005))
}.play

{SinOsc.ar(XLine.kr(15000, 20, 15))}.play


{
	x = MouseX.kr(0,1);
	SinOsc.ar(200).fold(x.neg,x)}.scope



(
OSCFunc({|msg, time, addr, port|
	[msg, time, addr, port].postln;
	a.sendMsg("/xy2", msg[1], msg[2])
}, "/xy1", NetAddr("127.0.0.1", 6000), 57120)
)


thisProcess.openUDPPort(6000)

thisProcess.openPorts

NetAddr.langPort

OSCFunc.trace

a = NetAddr("127.0.0.1", 6001)

a.sendMsg("/label1", "hello")

a.sendMsg("/fader3", 0.1)

a.sendMsg("/xy1", 0.5, 0.4)

a = [OSCReceiver_Mod]

a.indexOf(Lemur_Mod)

true.asInteger

OSCReceiver_Mod.inPorts.collect{|item| thisProcess.openUDPPort(item).asInteger}.sum==OSCReceiver_Mod.inPorts.size