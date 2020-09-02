//Make a simple mapping between a ramp and a sine cycle, test with an exponentional ramp
(
~source = FluidDataSet(s,\mlp_regressor_source);
~target = FluidDataSet(s,\mlp_regressor_target);
~test = FluidDataSet(s,\mlp_regressor_dest);
~output = FluidDataSet(s,\mlp_regress_out);
~tmpbuf = Buffer.alloc(s,1);
~regressor = FluidMLPRegressor(s,[2], FluidMLPRegressor.tanh, FluidMLPRegressor.tanh, maxIter: 1000, learnRate: 0.1, momentum: 0.1, batchSize: 1, validation: 0);
)

//Make source, target and test data
(
~sourcedata = 128.collect{|i|i/128};
~targetdata = 128.collect{|i| [sin(2*pi*i/128), i/128] };
~testdata = 128.collect{|i|(i/128)**2};

~source.load(
    Dictionary.with(
        *[\cols -> 1,\data -> Dictionary.newFrom(
            ~sourcedata.collect{|x, i| [i.asString, [x]]}.flatten)]);
);

~target.load(
d = Dictionary.with(
        *[\cols -> 1,\data -> Dictionary.newFrom(
            ~targetdata.collect{|x, i| [i.asString, [x]]}.flatten)]);
);

~test.load(
    Dictionary.with(
        *[\cols -> 1,\data -> Dictionary.newFrom(
            ~testdata.collect{|x, i| [i.asString, [x]]}.flatten)]);
);

~targetdata.plot;
~source.print;
~target.print;
~test.print;
)

// Now make a regressor and fit it to the source and target, and predict against test
//grab the output data whilst we're at it, so we can inspect


(
~source = FluidDataSet(s,\mlp_regressor_source);
~target = FluidDataSet(s,\mlp_regressor_target);
~test = FluidDataSet(s,\mlp_regressor_dest);
~output = FluidDataSet(s,\mlp_regress_out);
~tmpbuf = Buffer.alloc(s,1);
~regressor = FluidMLPRegressor(s,[2], FluidMLPRegressor.tanh, FluidMLPRegressor.tanh, maxIter: 1000, learnRate: 0.1, momentum: 0.1, batchSize: 1, validation: 0);
)


// run this to train the network for up to 1000(max epochs to map source to target. fit() returns loss. If this is -1, then training has failed. Run until the printed error is satisfactory to you
~sourcedata = 128.collect{|i|i/128};
~targetdata = 128.collect{|i| [sin(2*pi*i/128), i/128] };

~source.load(
    Dictionary.with(
        *[\cols -> 1,\data -> Dictionary.newFrom(
            ~sourcedata.collect{|x, i| [i.asString, [x]]}.flatten)]);
);

~target.load(
d = Dictionary.with(
        *[\cols -> 2,\data -> Dictionary.newFrom(
            ~targetdata.collect{|x, i| [i.asString, x]}.flatten.postln)]);
);

~regressor.fit(~source, ~target, {|x|x.postln;});

//you can change parameters of the MLPregressor with setters
~regressor.learnRate = 0.01;
~regressor.momentum = 0;
~regressor.validation= 0.2;

(
~outputdata = Array(128);
~regressor.predict(~test, ~output, action:{
        ~output.dump{|x| 128.do{|i|
        ~outputdata.add(x["data"][i.asString][0])
    }};
});
)

//We should see a single cycle of a chirp. If not, fit a little more epochs
~outputdata.plot;

// single point transform on arbitrary value

~outbuf = Buffer.new(s, 2);
~inbuf = Buffer.loadCollection(s,1.0.rand!1);
~regressor.predictPoint(~inbuf,~outbuf,{|x|x.postln;x.getn(0,2,{|y|y.postln;};)});


//Setup
(
~inputPoint = Buffer.alloc(s,1);
~predictPoint = Buffer.alloc(s,2);
~pitchingBus = Bus.control;
~catchingBus = Bus.control;
)

(
~regressor.inBus_(~pitchingBus).outBus_(~catchingBus).inBuffer_(~inputPoint).outBuffer_(~predictPoint);

~inputSynth = {
    var input = Saw.kr(2).linlin(-1,1,0,1);
    var trig = Impulse.kr(ControlRate.ir/10);
    BufWr.kr(input,~inputPoint,0);
    Out.kr(~pitchingBus.index,[trig]);
	Poll.kr(In.kr(~catchingBus.index),BufRd.kr(1,~predictPoint,Array.iota(2)),"mapped value")
}.play;

)