LFPulseReset : PureUGen {
	*ar {
		arg freq = 440.0, phase = 0.0, width = 0.5, reset = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, phase, width, reset).madd(mul, add)
	}

	*kr {
		arg freq = 440.0, phase = 0.0, width = 0.5, reset = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, phase, width, reset).madd(mul, add)
	}

	signalRange { ^\unipolar }
}