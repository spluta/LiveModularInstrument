NN_Synths_Analysis_Mod : NN_Synth_Mod {
	var xy2eight, whichMLP8, which2MLPs, ringAnalyzer, analysisBus, analysisSynth, analysisRout, instrAmpBus;

	*initClass {
		StartUp.add {
			SynthDef("ss_analysis_analysisSynth", {
				var source;

				source = In.ar(\inBus.kr);
				Out.kr(\instrAmpBus.kr, Amplitude.kr(source));
				Out.kr(\analysisBus.kr, FluidSpectralShape.kr(source));
			}).writeDefFile
		}
	}

	initWindow2 {
		instrAmpBus = Bus.control(group.server);
		analysisBus = Bus.control(group.server);

		xy2eight = XY_to_8.new;
		whichMLP8 = [1,0,0,0,0,0,0,0];
		ringAnalyzer = Fluid_Ring_Analyzer(this, analysisGroup, 3, parent.mixerToSynthBus);
		synths[0].set(\instrAmpBus, instrAmpBus);
		analysisSynth = Synth("ss_analysis_analysisSynth", [\inBus, parent.mixerToSynthBus, \analysisBus, analysisBus, \instrAmpBus, instrAmpBus], analysisGroup);
	}

	setNNInputVals {|vals|
		var xy, z;
		controlValsList = vals;
		//"vals ".post; vals.postln;
		if(vals.size>2){whichMLP8 = xy2eight.transform(vals.copyRange(0,1))};
	}

	configure {|vals|
		//vals.postln;
		this.setSlidersAndSynth(vals);
	}

	startAnalysis {
		var order;
		"startAnalysis".postln;
		analysisRout = Routine({{
			if(parent.predictOnOff==1){
				//"doit".postln;
				analysisBus.getn(7, {|vals|
					order = whichMLP8.order.reverse;
					which2MLPs = order.copyRange(0,1);
					ringAnalyzer.analyzeMe(vals, [mlps[which2MLPs[0]], mlps[which2MLPs[1]]], [whichMLP8[order[0]], whichMLP8[order[1]]]);
				});
			};
			(4*(512/44100)).wait
			//1.wait
		}.loop}).play;
	}

	addPoint {
		outDataSet.dump({|vals|
			var max=0, newPoint;
			vals.postln;

			if(vals["data"]!=nil){
				vals["data"].keys.do{|item| if(item.asInteger.postln>max){max=item}};
				max.postln;
				newPoint = max.asInteger+1;
			}{newPoint = 0};

			Buffer.loadCollection(group.server, controlValsList.copyRange(0,1).asArray, 1, {|array|
				inDataSet.addPoint(newPoint.asString, array);
				inDataSet.print;
				Buffer.loadCollection(group.server, valsList.asArray, 1, {|array|
					outDataSet.addPoint(newPoint.asString, array);
					outDataSet.print;
					numPoints = numPoints+1;
					keys.add(newPoint.asString);
				});
			});
		})

	}

	killMeSpecial {
		mlps.do{|item| item.free};
		mlpInBuf.free;
		mlpOutBuf.free;
		inDataSet.free;
		outDataSet.free;
		ringAnalyzer.killMe;
		analysisSynth.free;
	}
}


Fluid_Ring_Analyzer {
	var <>parent, <>group, <>dur, <>inBus, ssBusIn, synthBuf, synthBuf2, pcaPointNorm, point, pointStand, pcaPoint, recordBuf, ssBuf, loudBuf, ds, inDS, inNorm, inStand, inPCA, outDS, point, norm, stand, pca, trainPCARout, analyze, time, array2, array3, synth;

	*new {arg parent, group, dur, inBus;
		^super.new.parent_(parent).group_(group).dur_(dur).inBus_(inBus).init;
	}

	*initClass {
		StartUp.add {
			SynthDef("ring_recorder", {
				RecordBuf.ar(In.ar(\inBus.kr), \recordBuf.kr);
			}).writeDefFile
		}
	}

	init {
		[parent, group, dur, inBus].postln;
		{
			recordBuf = Buffer.alloc(group.server, group.server.sampleRate*dur);
			synth = Synth("ring_recorder", [\recordBuf, recordBuf, \inBus, inBus], group);

			synthBuf = Buffer(group.server);
			synthBuf2 = Buffer(group.server);

			pointStand = Buffer(group.server);
			pcaPoint = Buffer(group.server);
			pcaPointNorm = Buffer(group.server);


			ssBuf = Buffer.new(group.server);
			loudBuf = Buffer.new(group.server);

			ds = FluidDataSet(group.server, "loudy"++NN_Synth_DataSetID.next);
			inDS = FluidDataSet(group.server, "trp"++NN_Synth_DataSetID.next);
			inNorm = FluidDataSet(group.server, "trpNorm"++NN_Synth_DataSetID.next);
			inStand = FluidDataSet(group.server, "trpStand"++NN_Synth_DataSetID.next);
			inPCA = FluidDataSet(group.server, "trpPCA"++NN_Synth_DataSetID.next);
			outDS = FluidDataSet(group.server, "synth"++NN_Synth_DataSetID.next);
			point = Buffer.alloc(group.server, 7);

			norm = FluidNormalize(group.server);
			stand = FluidStandardize(group.server);
			pca = FluidPCA(group.server);

			1.wait;
			group.server.sync;
			trainPCARout = Routine({inf.do{
				time = Main.elapsedTime;
				FluidBufSpectralShape.process(group.server, recordBuf, 0, -1, 0, -1, ssBuf, 4096, action:{
					FluidBufLoudness.process(group.server, recordBuf, features:loudBuf, action:{
						var array1, array2, array3;

						ssBuf.loadToFloatArray(action:{|array|
							array1=array.clump(7);
							//array1.postln;
							loudBuf.loadToFloatArray(action:{|arrayB|
								array2 = arrayB.clump(2);
								array3 = array1.select{|item, i| array2[i][0]>(-30)};
								//"array3 ".post; array3 = array3.collect{|item, i| [i.asSymbol, item]}.flatten.postln;
								if(array3.size>8){
									inDS.load(Dictionary.newFrom([\cols, 7, \data, Dictionary.newFrom(array3)]), {

										stand.fitTransform(inDS, inStand, {
											//inStand.print;
											pca.fitTransform(inStand, inPCA, {
												//inPCA.print;
												norm.fit(inPCA, {/*(time - Main.elapsedTime).postln*/})
											});
										});
									});
								};
							});
						});
					})
				});
				3.wait;
			}}).play;
			group.server.sync;
			2.wait;
			parent.startAnalysis;
		}.fork;

	}

	analyzeMe {|vals, mlps, amountEach|
		//mlps.postln;
		//amountEach.postln;
		point.loadCollection(vals.asArray, action:{
			//point.postln;
			stand.transformPoint(point, pointStand, {|buf|
				//pointStand.postln;
				pca.transformPoint(pointStand, pcaPoint, {
					//pcaPoint.postln;
					norm.transformPoint(pcaPoint, pcaPointNorm, {
						//pcaPointNorm.postln;
						mlps[0].predictPoint(pcaPointNorm, synthBuf, {
							mlps[1].predictPoint(pcaPointNorm, synthBuf2, {
								synthBuf.loadToFloatArray(action:{|array1|
									synthBuf2.loadToFloatArray(action:{|array2|
										parent.configure(array1)
										//parent.configure((array1*amountEach[0]+(array2*amountEach[1]))/2)
								})})
							})
						});
					});
				});
			});
		});
	}

	killMe {
		synth.fre;
	}
}

