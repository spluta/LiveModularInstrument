

s.reboot
~ds.free
// define a few processes
(

~ds = FluidDataSet(s, \mfcc);

~spectralbuf = 4.collect{Buffer.new};
~pitchbuf = 4.collect{Buffer.new};
~statsbuf0 = 4.collect{Buffer.new};
~flatbuf0 = 4.collect{Buffer.new};
~mergebuf = 4.collect{Buffer.new};

~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");

~extractor = FluidProcessSlices({|src,start,num,data|
	var specs, stats0, stats1, writer, flatten0, flatten1,mfccBuf, statsBuf, flatBuf, label, voice, pitch, merge0, merge1, trig;

	label = data.key;
	voice = data.value[\voice];

	trig = Impulse.kr(0.25);

	specs = FluidBufMFCC.kr(src,start,num,numChans:1,features:~spectralbuf[voice], numCoeffs:20, numBands:40,trig:1);

	stats0 = FluidBufStats.kr(~spectralbuf[voice], 0, -1, 1, 19, stats:~statsbuf0[voice],trig:Done.kr(specs));

	flatten0 = FluidBufFlatten.kr(~statsbuf0[voice],~flatbuf0[voice],trig:Done.kr(stats0));

	merge0 = FluidBufCompose.kr(~flatbuf0[voice], 0, 19, 0, -1, 1, ~mergebuf[voice], 0, 0, 0, Done.kr(flatten0));

	writer = FluidDataSetWr.kr(~ds,label, -1, ~mergebuf[voice], Done.kr(merge0));
	//LocalOut.kr(Done.kr(writer));
});

)

/////////////////////////////////////////////////////////////////////////
//loading process

// just run the loader
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;});
)

~loader

//////////////////////////////////////////////////////////////////////////
// description process

// just run the descriptor extractor
(
t = Main.elapsedTime;
~extractor.play(s,~loader.buffer,~loader.index,action:{(Main.elapsedTime - t).postln;"Features done".postln}, tasks:2);
)

// write the dataset to file with the native JSON
~ds.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/dsReducedMFCC.json")

// open the file in your default json editor
"open /Users/spluta/Documents/SC/FluCoMA/solidSession/datasetSSNoDeriv.json".unixCmd

//////////////////////////////////////////////////////////////////////////
// manipulating and querying the data

~ds = FluidDataSet(s,\test);
~ds.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/extractionMFCCTest3.json")
~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");
~loader.buffer.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/buffer.wav")
~loader.index.writeArchive("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/index")
~datasetIndex = FluidDataSet(s, \index);


////
////

//make the 3 dimensional space

s.reboot;

~ds = FluidDataSet(s,\shapes);
~ds.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/dsReducedMFCC.json")
~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");

~

//load the indices to a DataSet
~ds2 = FluidDataSet(s, \stupid);
~ds2.load(~loader.indexAsDataSetDict, {~ds2.print});
~ds2.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/indices.json")


(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;});
)


//think I need to normalize before pca
~norm = FluidNormalize(s);
~stand = FluidStandardize(s);
~dsNorm = FluidDataSet(s,\norm);
~dsNorm2 = FluidDataSet(s,\norm2);
~pca = FluidPCA(s, 2);
~dsPCA = FluidDataSet(s,\pca);
(
~stand.fitTransform(~ds,~dsNorm, {
	~dsNorm.postln;
	~pca.fitTransform(~dsNorm, ~dsPCA, {
		~dsPCA.postln;
		~norm.fitTransform(~dsPCA,~dsNorm2, {
			~dsNorm2.dump{|x| x["data"].keys.do{|key| [key,x["data"][key]].postln}}
		})
	})
})
)

~dsNorm.dump{|x| x.postln}

~dsNorm2.dump{|x| x["data"].keys.do{|key| [key,x["data"][key]].postln}}
~dsPCA.dump{|x| x["data"].keys.do{|key| [key,x["data"][key]].postln}}

~dsPCA.size

 ~points0 = List.newClear(0);
 ~points1 = List.newClear(0);
 ~points2 = List.newClear(0);

(~dsNorm2.dump{|val| val["data"].keys.do{|key|
	~points0.add([val["data"][key][0].linlin(0,1.0,0,200), val["data"][key][1].linlin(0,1.0,0,200)]);
	//~points1.add([val["data"][key][2].linlin(0,1.0,0,200), 0]);
	//~points2.add([val["data"][key][4].linlin(0,1.0,0,200), val["data"][key][5].linlin(0,1.0,0,200)]);
}})

~dsNorm

(
w = Window("scatter", Rect(128, 64, 200, 200));
w.drawFunc = {
	Pen.use {
		~points0.do{|p|
			var r;
			p.postln;
			r = Rect(p[0],p[1],5,5);
			Pen.fillColor = Color.blue;
			Pen.fillOval(r);
		}
	}
};
w.refresh;
w.front;
)


//building a tree
~tree = FluidKDTree(s, 5);
~tree.fit(~dsNorm2,{"Fitted".postln;});

~tree.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/datasetTree.json")

~tree.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/datasetTree.json")

(~dataBuf.free;
~dataBuf = Buffer.loadCollection(s, Array.fill(2, {1.0.rand}), 1, {|buf|
	~friends = Array;
	~tree.kNearest(~dataBuf,{|x| ~friends = x.postln;})
})
)
(Routine{
	5.do{|i|
		var dur;
		v = ~loader.index[~friends[i].asSymbol];
		dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
		{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][1],dur, doneAction: 2)).dup}.play;
		~friends[i].postln;
		min(dur, 0.5).wait;
	};
}.play;
)

PlayBuf.ar

(
var list, sl, but, dur;
w = Window("scatter", Rect(128, 64, 400, 200));

list = List.fill(2, {0});
sl = Array.fill(2, {|i| Slider().action_{|val| list.put(i,val.value); list.postln}});

Routine{
	inf.do{
		~dataBuf.free;
		t = Main.elapsedTime;
		~dataBuf = Buffer.loadCollection(s, list, 1, {|buf|
			~friends = Array;
			~tree.kNearest(~dataBuf,5,{|x|
				var v;
				~friends = x.postln;
				v = ~loader.index[~friends[5.rand].asSymbol];
				dur = (v[\bounds][1] - v[\bounds][0]) / s.sampleRate;
				(Main.elapsedTime-t).postln;
				{BufRd.ar(v[\numchans],~loader.buffer,Line.ar(v[\bounds][0],v[\bounds][0]+(0.1*s.sampleRate),0.1, doneAction: 2)).dup}.play;

			})
		});
		0.1.wait;
	}
}.play;

w.layout_(HLayout(*sl));
w.front
)

0.0059575270001915.reciprocal

44100/64