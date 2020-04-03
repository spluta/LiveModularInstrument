

a = NetAddr("127.0.0.1", 5002)

a.sendMsg('/predict', 0,0,0,0);

b = Set();

b.add([1,2,3,4])

100**4

(
var temp, set, numPer;

~set = Set();

OSCFunc({arg ...msg;

	temp = msg[0].copyRange(2, 18);
	temp = temp.round(0.01);
	~set.add(temp);
	//set.size.postln;

}, '/nnOutputs', nil, 5001);

numPer = 50;

{
numPer.do{|x0|
		x0.postln;
	numPer.do{|y0|
		numPer.do{|x1|
			numPer.do{|y1|
					a.sendMsg('/predict', x0/numPer, y0/numPer, x1/numPer, y1/numPer);
					0.0001.wait;
}}}}}.fork)

~set.size

16 dimensional cross-feedback:

model 0 - 1388128
49734 after 1
98619 after 1 w 100pts
49734/331534

model 1 - 5873
model 2 - 1865147
model 3 - 1134843

all models - 12 million unique outputs by step of 0.02 (seems like it

after 10 - 2092948 (2092948*5)

	after all - 12million (11766265)

12000000/(50**4)

10000/(100**16)

10-20 million points

1e-25 - 1e-23percent
each point - 1e-26 to 1e-24 percent

50**3

(100**4)*0.00001

6.25 million points

32 Dimensional DX7 Module_Mod

1e64 points
1e-55 percent of the space
115505 after 1
5300755 after all


"/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/01_CrossFeedback1/modelFile0.h5"