e = SCMIRAudioFile(Platform.resourceDir +/+ "sounds/a11wlk01.wav", [[MFCC, 13], [Chromagram, 12]]);

e = SCMIRAudioFile("/Users/spluta/Library/Application Support/SuperCollider/sounds/snare441.aif", [[Onsets]]);
e = SCMIRAudioFile("/Users/spluta/Library/Application Support/SuperCollider/sounds/PEQuintet/drums.aif", [[Onsets]]);
//shortcut versions also work, defaults will be applied for MFCC (10 coeffs) and Chromagram (12TET)
//e = SCMIRAudioFile(Platform.resourceDir +/+"sounds/a11wlk01.wav",[MFCC,Chromagram]);

e.extractFeatures()

e.extractOnsets()

a = e.onsetdata
e.numonsets

e.duration

{e.extractFeatures()}.fork //wrapped in fork gives posted feedback as it goes; won't block main thread

e.save("/Users/nickcollins/Desktop/a11wlk01.scmirZ")    //save .scmirZ files using ZArchive; binary storage of feature data and other instance variables

e.featuredata.do{arg item; item.postln}

e.numfeatures

e.numframes

b = Buffer.read(s, "/Users/spluta/Library/Application Support/SuperCollider/sounds/PEQuintet/drums.aif");

Routine({inf.do{
	var num, dur;

	num = a.size.rand;
	num.postln;
	if(num!=(a.size-1),{dur = a[num+1]-a[num]},{dur = b.duration-a[num]});
	{PlayBuf.ar(2, b, 1, 1, a[num]*b.sampleRate, 0, 0)*EnvGen.kr(Env.new([0,1,1,0], [0.01, dur-0.02, 0.01]), doneAction:2)}.play;
	(dur-0.02).wait;
}}).play

c.value


