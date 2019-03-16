PV_Control : PV_ChainUGen
{
	*new { arg buffer, thresh=1, mulFactor=0.7, limiter = 1, attackReleaseFrames=100, sustainZeroFrames=100, waitGoFrames = 100, tripCount = 10, tripBlockFrames = 1000, highestBin=300;
		^this.multiNew('control', buffer, thresh, mulFactor, limiter, attackReleaseFrames, sustainZeroFrames, waitGoFrames, tripCount, tripBlockFrames, highestBin)
	}
}