(
fork{
8.do{|num|
//num = 1;
~folder = "/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/modules/NN_Synths/01_CrossFeedback0/model1/";

~trainingList = CSVFileReader.read(~folder++"trainingFile"++num++".csv");

~fileInfo = ~trainingList[0].collect{|item| item.asInteger};


~trainingList = ~trainingList.copyRange(1, ~trainingList.size-1).collect({arg item; item.collect({arg item2; item2.asFloat})}).asList;

~finalLists = ~trainingList.collect{|item| [item.copyRange(0, ~fileInfo[0]-1), item.copyRange(~fileInfo[0], ~fileInfo[1]+~fileInfo[0])]}.flop;



	d = Dictionary.new;
	d.add(\cols -> ~fileInfo[1]);
	d.add(\data -> Dictionary.newFrom(~finalLists[1].collect{|item, i| [i.asString, item]}.flatten));

    ~ds = FluidDataSet.new(s,\simple1d_1); s.sync;
    ~ds.load(d); s.sync;
    ~ds.dump;
	~ds.write(~folder++"inDataSet"++num++".json");
	s.sync;
	//~ds.free;
	s.sync;

	d = Dictionary.new;
	d.add(\cols -> ~fileInfo[0]);
	d.add(\data -> Dictionary.newFrom(~finalLists[0].collect{|item, i| [i.asString, item]}.flatten));

    ~ds1 = FluidDataSet.new(s,\simple1d_2); s.sync;
    ~ds1.load(d); s.sync;
    ~ds1.dump;
	~ds1.write(~folder++"outDataSet"++num++".json");
	s.sync;
	//~ds1.free;
	1.wait;

	~hidden = (~fileInfo[1], ~fileInfo[1]+(~fileInfo[0]/5)..~fileInfo[0]).floor.asInteger.copyRange(1,3);
	~mlp = FluidMLPRegressor(s, ~hidden,2,1,0,1000,0.1,0,1,0);
	s.sync;
	~mlp.fit(~ds, ~ds1, {
		~mlp.write(~folder++"modelFile"++num++".json");
	});
	s.sync;
	1.wait;
	~ds.free;~ds1.free;~mlp.free;
}};
)





	