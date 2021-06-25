~wekiAddr = NetAddr("127.0.0.1", 6448);

b = Bus.control(s, 22);

(
//b = Bus.control(s, 20);

OSCFunc({|msg|
	msg.postln;
	~wekiAddr.sendMsg("/wek/inputs", msg[2]);
	~wekiAddr.sendMsg("/wek/inputs/2", msg[1]);
}, '/1/xy2');

OSCFunc({|msg|
	msg.postln;
	~wekiAddr.sendMsg("/wek/inputs/3", msg[2]);
	~wekiAddr.sendMsg("/wek/inputs/4", msg[1]);
}, '/1/xy2');

OSCFunc({|msg|
	"weki: ".post; msg.postln;

	b.setn(msg.copyRange(1, 22));

}, '/wek/outputs');

)

~wekiAddr.sendMsg("/wek/outputs-21", 0.8);



b.get