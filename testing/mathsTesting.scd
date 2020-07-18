
{TestLag.ar(K2A.ar(10),K2A.ar(0.1),K2A.ar(0.1))}.plot(1)


HyperDisCo

{ |x| ~expMapping.(x, 0, 1, 0.0001, 1, exp(log(300)/2)) }.plotGraph(from: 0, to: 1)
{ |x| x.linexp(0, 1, 0.0001, 1) }.plotGraph(from: 0, to: 1)

(
~expMapping = { |x, inMin = 0, inMax = 1, outMin = 0, outMax = 1|
	((1/outMin)*outMax)**(x/(inMax-inMin))*outMin
}
)

~expMapping.(0.5, 0, 1, 0.0001, 3).postln;

(10000*3**0.5)*(0.0001/3)*3;
0.5.linexp(0, 1, 0.0001, 3);

(10000*3**(0.5/2))*(0.0001/3)*3;
0.5.linexp(0, 2, 0.0001, 3);

((1/outMin)*outMax)**(x/(inMax-inMin))*outMin

(((1..10)/10).do{|i|
	~expMapping.(i, 0, 1, 0.0001, 1,).postln;
	i.linexp(0,1, 0.0001,1).postln;
	((10000**i)*0.0001).postln;
})

// all the same

{ |x| ~expMapping.(x, 0, 2, 0.01, 3, exp(log(300)/2)) }.plotGraph(from: 0, to: 2)
{ |x| x.lincurve(0, 2, 0.01, 3, log(300)) }.plotGraph(from: 0, to: 2)
{ |x| x.linexp(0, 2, 0.01, 3) }.plotGraph(from: 0, to: 2)


a = (y1 / y0) ** (1 / (x1 - x0))


(_+_).(1, 2) // -> 3



a = (_+_+_)
a.value(1, 2, 3) // -> a BinaryOpFunction

a.value

(
a = {
    var sig, mathsA, eof, eor, mathsB, sig1, noise;

	#mathsA, eof, eor = Maths.ar(0.01, MouseY.kr(0.001,1), 0.99, (1-MouseButton.kr(0,1,0.001)), 0, 0);  //using both the function and the trigger outputs
    sig = LFTri.ar(100+(2000*mathsA), 0, 0.1);
    mathsB = Maths.ar(0.001, min(0.1, MouseX.kr(0.001,1)), 0.9, 0, 1, eof)[0];
    noise = mathsB*WhiteNoise.ar(0.1);

    sig1 = LFTri.ar(20+(mathsB*1400), 0, 0.1);
	sig;
    (sig+sig1+noise).dup;
//mathsA
	//[eof,eor]
}.scope

)



(
a = {
    var sig, mathsA, eof, eor, mathsB, sig1, noise;
	//
	#mathsA, eof, eor = MathsT.ar(
		MouseX.kr(0.002,1),
			MouseY.kr(0.005,1),
				0.99,
		Impulse.ar(1));  //using both the function and the trigger outputs
    sig = LFTri.ar(100+(2000*mathsA.poll), 0, 0.1);


	[sig,eor]
}.scope

)

(
a = {
    var sig, mathsA, eof, eor, mathsB, sig1, noise;
	//
	#mathsA, eof, eor = MathsC.ar(
		MouseX.kr(0.002,1),
			MouseY.kr(0.005,1),
				0.99,
		MouseButton.kr(0,1,0.001));  //using both the function and the trigger outputs
    sig = LFTri.ar(100+(2000*mathsA.poll), 0, 0.1);


	[sig,eof]
}.scope

)





(
a = {
	var sig, mathsA, eof, mathsB, sig1, noise;

	#mathsA, eof = Maths.ar(
		K2A.ar(MouseX.kr(0.001, 3)),
		K2A.ar(MouseY.kr(0.01, 3)),
		K2A.ar(0.9),
		K2A.ar(1),
		K2A.ar(0),
		K2A.ar(MouseButton.kr(0,1,0.001)>0));  //using both the function and the trigger outputs
	sig = LFTri.ar(100+(2000*mathsA), 0, 0.1);
	sig
	//mathsA
}.scope
//
)

a = {LFTri.ar(100+(1000*Maths.ar(MouseX.kr, MouseY.kr, 0.2, 1, MouseButton.kr(lag:0.001))[0]), 0, 0.1)}.play

a = {LFTri.ar(100+(1000*Maths.ar(MouseX.kr, MouseY.kr, 0.2, 1, MouseButton.kr(lag:0.001))[0]), 0, 0.1)}.play


(
a = {
	var sig, mathsA, eof, mathsB, sig1, noise;

	#mathsA, eof = MathsT.ar(
		K2A.ar(MouseX.kr(1, 30)),
		K2A.ar(0.5),
		K2A.ar(0.3),
		K2A.ar(MouseButton.kr(0,1,0.001)>0));  //using both the function and the trigger outputs
	sig = LFTri.ar(100+(2000*mathsA), 0, 0.1);
	mathsA
}.scope(1)

)

*ar { | freq, logExp, loop, trig |
		if(in1.rate != 'audio') {in1 = K2A.ar(in1)};

      ^this.multiNew('audio', in1, in2, in3, in4, in5, in6)
  }



(
a = {
	var sig, mathsA, eof, mathsB, sig1, noise;

	mathsA= Maths.ar(
		K2A.ar(MouseX.kr(1, 30)),
		K2A.ar(0.9),
		K2A.ar(0.9),
		1-(K2A.ar(MouseButton.kr(0,1,0.001))>0),
		K2A.ar(0),
		K2A.ar(0));  //using both the function and the trigger outputs
	sig = LFTri.ar(100+(2000*mathsA[0]), 0, 0.1);
	/*mathsB = MathsT.ar(MouseX.kr(1, 30), 0.1, MouseY.kr(0,1), eof)[0];
	noise = mathsB*WhiteNoise.ar(0.1);

	sig1 = LFTri.ar(20+(mathsB*1400), 0, 0.1);*/
//sig
	mathsA
	//(sig+sig1+noise).dup
}.plot(1)

)