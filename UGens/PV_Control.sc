PV_Control : PV_ChainUGen
{
	*new { arg buffer, thresh=0.5, mulFactor=0.7, limiter = 100, attackReleaseFrames=100, sustainZeroFrames=50, waitGoFrames = 18, tripCount = 10, tripBlockFrames = 100, highestBin=100;
		^this.multiNew('control', buffer, thresh, mulFactor, limiter, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, highestBin)
	}
}

PV_Control2 : PV_ChainUGen
{
	*new { arg buffer0, buffer1, thresh=0.5, mulFactor=0.7, limiter = 100, attackReleaseFrames=100, sustainZeroFrames=50, waitGoFrames = 18, tripCount = 10, tripBlockFrames = 100, highestBin=100;
		^this.multiNew('control', buffer0, buffer1, thresh, mulFactor, limiter, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, highestBin)
	}
}