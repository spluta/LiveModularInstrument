NN_Synths_Analysis_Mod : NN_Synth_Mod {
	var xy2eight, whichMLP8, which2MLPs, ringAnalyzer, analysisBus, analysisSynth, analysisRout, instrAmpBus, minCentroid, maxCentroid, centroid01, centroidsBuf;

	*initClass {
		StartUp.add {
			SynthDef("ss_analysis_analysisSynth", {
				var source, spectralShape, min, max;

				source = In.ar(\inBus.kr);
				Out.kr(\instrAmpBus.kr, Amplitude.kr(source));
				spectralShape = FluidSpectralShape.kr(source);
				/*RecordBuf.kr(spectralShape[0], \centroidsBuf.kr);
				min = BufMin.kr(\centroidsBuf.kr);
				max = BufMax.kr(\centroidsBuf.kr);
				Out.kr(\analysisBus.kr, [spectralShape[0], min, max]);*/
				Out.kr(\analysisBus.kr, spectralShape);
			}).writeDefFile
		}
	}

	trigger {|num, val|

		if(num==0){
			synths[0].set(\onOff0, val);
			onOff0 = val;
		}{
			synths[0].set(\onOff1, val);
			onOff1 = val;
		};
		if(val==1){ringAnalyzer.pauseTraining}{ringAnalyzer.resumeTraining};
	}

	initWindow2 {
		{
			instrAmpBus = Bus.control(group.server);
			//analysisBus = Bus.control(group.server, 3);

			analysisBus = Bus.control(group.server, 7);
			centroidsBuf = Buffer(group.server, group.server.sampleRate/group.server.options.blockSize*3);
			group.server.sync;
			minCentroid = 10000;
			maxCentroid = 0;

			xy2eight = XY_to_8.new;
			whichMLP8 = [1,0,0,0,0,0,0,0];
			ringAnalyzer = Fluid_Ring_Analyzer(this, analysisGroup, 3, parent.mixerToSynthBus);
			synths[0].set(\instrAmpBus, instrAmpBus);
			analysisSynth = Synth("ss_analysis_analysisSynth", [\inBus, parent.mixerToSynthBus, \analysisBus, analysisBus, \instrAmpBus, instrAmpBus, \centroidsBuf, centroidsBuf], analysisGroup);
		}.fork;
	}

	setNNInputVals {|vals|
		var xy, z;
		controlValsList = vals;
		if(vals.size>2){whichMLP8 = xy2eight.transform(vals.copyRange(0,1))};
	}

	configure {|vals|
		this.setSlidersAndSynth(vals);
	}

	startAnalysis {
		var order;
		analysisRout = Routine({{
			if(parent.predictOnOff==1){
				//"doit".postln;
				analysisBus.getn(7, {|vals|
/*					if(vals[0]<minCentroid){minCentroid = vals[0]};
					if(vals[0]>maxCentroid){maxCentroid = vals[0]};
					centroid01 = vals[0].linlin(minCentroid, maxCentroid, 0.0, 1.0, \minmax).postln;
					ringAnalyzer.setXYWin(centroid01,0);*/
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

			if(vals["data"]!=nil){
				vals["data"].keys.do{|item| if(item.asInteger>max){max=item}};
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
	var <>parent, <>group, <>dur, <>inBus, ssBusIn, synthBuf, synthBuf2, pcaPointNorm, point, pointStand, pcaPoint, recordBuf, ssBuf, loudBuf, ds, inDS, inNorm, inStand, inPCA, outDS, point, norm, stand, pca, trainPCATask, analyze, time, array2, array3, synth, xyWin, xySlider;

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

	setXYWin {|x=0,y=0|
		{xySlider.setXY(x,y)}.defer;
	}

	pauseTraining {
		"pause".postln;
		trainPCATask.pause;
	}

	resumeTraining {
		"resume".postln;
		trainPCATask.reset;
		trainPCATask.play;
	}

	init {
		{xyWin = Window();
		xySlider = Slider2D(xyWin, Rect(0, 0, 200, 200));
			xyWin.front}.defer;
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
			trainPCATask = Task({inf.do{
				time = Main.elapsedTime;
				FluidBufSpectralShape.process(group.server, recordBuf, 0, -1, 0, -1, ssBuf, 4096, action:{
					FluidBufLoudness.process(group.server, recordBuf, features:loudBuf, action:{
						var array1, array2, array3;

						ssBuf.loadToFloatArray(action:{|array|
							array1=array.clump(7);
							loudBuf.loadToFloatArray(action:{|arrayB|
								array2 = arrayB.clump(2);
								array3 = array1.select{|item, i| array2[i][0]>(-30)};
								if(array3.size>8){
									inDS.load(Dictionary.newFrom([\cols, 7, \data, Dictionary.newFrom(array3)]), {

										stand.fitTransform(inDS, inStand, {
											pca.fitTransform(inStand, inPCA, {
												norm.fit(inPCA, {})
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
		point.loadCollection(vals.asArray, action:{
			stand.transformPoint(point, pointStand, {|buf|
				pca.transformPoint(pointStand, pcaPoint, {
					norm.transformPoint(pcaPoint, pcaPointNorm, {
						pcaPointNorm.loadToFloatArray(action:{|array| this.setXYWin(array[0], array[1])});
						mlps[0].predictPoint(pcaPointNorm, synthBuf, {
							mlps[1].predictPoint(pcaPointNorm, synthBuf2, {
								synthBuf.loadToFloatArray(action:{|array1|
									synthBuf2.loadToFloatArray(action:{|array2|
										parent.configure(array1*amountEach[0]+(array2*amountEach[1]));
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

