

s.reboot
~ds.free
// define a few processes
(

~ds = FluidDataSet(s,\test); // still need a name on the server to make sure we do not forget it exists. it is now permanent aka will resist cmd+.
//define as many buffers as we have parallel voices/threads in the extractor processing (default is 4)
~spectralbuf = 4.collect{Buffer.new};
~pitchbuf = 4.collect{Buffer.new};
~statsbuf0 = 4.collect{Buffer.new};
~statsbuf1 = 4.collect{Buffer.new};
~flatbuf0 = 4.collect{Buffer.new};
~flatbuf1 = 4.collect{Buffer.new};
~mergebuf = 4.collect{Buffer.new};

// here we instantiate a loader which creates a single large buffer with a dictionary of what was included in it
// ~loader = FluidLoadFolder("/Volumes/machins/projets/newsfeed/smallnum/");
~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");

// here we instantiate a process of description and dataset writing, which will run each slice of the previous slice and write the entry. Note the chain of Done.kr triggers.
/*~extractor = FluidProcessSlices({|src,start,num,data|
	var specs, stats0, stats1, writer, flatten0, flatten1,mfccBuf, statsBuf, flatBuf, label, voice, pitch, merge0, merge1;

	label = data.key;
	voice = data.value[\voice];

	specs = FluidBufSpectralShape.kr(src,start,num,numChans:1,features:~spectralbuf[voice],trig:1);
	pitch = FluidBufPitch.kr(src,start,num,numChans:1,features:~pitchbuf[voice],trig:Done.kr(specs));

	stats0 = FluidBufStats.kr(~spectralbuf[voice], 0, -1, 0, 4, stats:~statsbuf0[voice],trig:Done.kr(pitch));
	stats1 = FluidBufStats.kr(~pitchbuf[voice], 0, -1, 0, 1, stats:~statsbuf1[voice],trig:Done.kr(stats0));

	flatten0 = FluidBufFlatten.kr(~statsbuf0[voice],~flatbuf0[voice],trig:Done.kr(stats1));
	flatten1 = FluidBufFlatten.kr(~statsbuf1[voice],~flatbuf1[voice],trig:Done.kr(flatten0));

	merge0 = FluidBufCompose.kr(~flatbuf0[voice], 0, -1, 0, -1, 1, ~mergebuf[voice], 0, 0, 0, Done.kr(flatten1));
	merge1 = FluidBufCompose.kr(~flatbuf1[voice], 0, -1, 0, -1, 1, ~mergebuf[voice], 28, 0, 0, Done.kr(merge0));

	writer = FluidDataSetWr.kr(~ds,label, -1, ~mergebuf[voice], Done.kr(merge1))
});*/
~extractor = FluidProcessSlices({|src,start,num,data|
	var specs, stats0, stats1, writer, flatten0, flatten1,mfccBuf, statsBuf, flatBuf, label, voice, pitch, merge0, merge1;

	label = data.key;
	voice = data.value[\voice];

	specs = FluidBufMFCC.kr(src,start,num,numChans:1,features:~spectralbuf[voice], numCoeffs:30, numBands:60,trig:1);
	//pitch = FluidBufPitch.kr(src,start,num,numChans:1,features:~pitchbuf[voice],trig:Done.kr(specs));

	stats0 = FluidBufStats.kr(~spectralbuf[voice], 0, -1, 1, 29, stats:~statsbuf0[voice],trig:Done.kr(specs));
	//stats1 = FluidBufStats.kr(~pitchbuf[voice], 0, -1, 0, 1, stats:~statsbuf1[voice],trig:Done.kr(stats0));

	flatten0 = FluidBufFlatten.kr(~statsbuf0[voice],~flatbuf0[voice],trig:Done.kr(stats0));
	//flatten1 = FluidBufFlatten.kr(~statsbuf1[voice],~flatbuf1[voice],trig:Done.kr(flatten0));

	merge0 = FluidBufCompose.kr(~flatbuf0[voice], 0, 39, 0, -1, 1, ~mergebuf[voice], 0, 0, 0, Done.kr(flatten0));
	//merge1 = FluidBufCompose.kr(~flatbuf1[voice], 0, -1, 0, -1, 1, ~mergebuf[voice], 28, 0, 0, Done.kr(merge0));

	writer = FluidDataSetWr.kr(~ds,label, -1, ~mergebuf[voice], Done.kr(merge0))
});

)

/////////////////////////////////////////////////////////////////////////
//loading process

// just run the loader
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;});
)

//////////////////////////////////////////////////////////////////////////
// description process

// just run the descriptor extractor
(
t = Main.elapsedTime;
~extractor.play(s,~loader.buffer,~loader.index,action:{(Main.elapsedTime - t).postln;"Features done".postln});
)

// write the dataset to file with the native JSON
~ds.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/extractionFullMFCC.json")

// open the file in your default json editor
"open /Users/spluta/Documents/SC/FluCoMA/solidSession/datasetSSNoDeriv.json".unixCmd

//////////////////////////////////////////////////////////////////////////
// manipulating and querying the data

~ds = FluidDataSet(s,\test);
~ds.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/extractionMFCCTest3.json")
~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");
~loader.buffer.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/buffer.wav")
~loader.index.writeArchive("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/index")


////
////

//make the 3 dimensional space

s.reboot;

~ds = FluidDataSet(s,\shapes);
~ds.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/extraction.json")
~loader = FluidLoadFolder("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/AudioFiles3/");
(
t = Main.elapsedTime;
~loader.play(s,action:{(Main.elapsedTime - t).postln;"Loaded".postln;});
)


//think I need to normalize before pca
~norm = FluidNormalize(s);
~stand = FluidStandardize(s);
~dsNorm = FluidDataSet(s,\norm);
~dsNorm2 = FluidDataSet(s,\norm2);
~pca = FluidPCA(s);
~dsPCA = FluidDataSet(s,\pca);
(
~stand.fitTransform(~ds,~dsNorm, {
	~dsNorm.postln;
	~pca.fitTransform(~dsNorm, ~dsPCA,  2, {
		~dsPCA.postln;
		~norm.fitTransform(~dsPCA,~dsNorm2, {
			~dsNorm2.dump{|x| x["data"].keys.do{|key| [key,x["data"][key]].postln}}
		})
	})
})
)

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
~tree = FluidKDTree(s);
~tree.fit(~dsNorm2,{"Fitted".postln;});

~tree.read("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/model0/datasetTree.json")

~tree.write("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/07_SamplePlayer0/datasetTree.json")

(~dataBuf.free;
~dataBuf = Buffer.loadCollection(s, Array.fill(2, {1.0.rand}), 1, {|buf|
	~friends = Array;
	~tree.kNearest(~dataBuf,1,{|x| ~friends = x.postln;})
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