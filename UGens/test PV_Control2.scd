({
	var windowStarts, stream;

	a = WhiteNoise.ar(1);
	b = LocalBuf(4096, 1);
	c = LocalBuf(4096, 1);

	b = FFT(b, a);
	c = PV_Copy(b, c);

	PV_Control2(b, c);

	windowStarts = b > -1;

	stream = Unpack1FFT(c, 4096, (1..20), 0);

	Demand.kr(windowStarts, 0, stream).collect { |anunp, index|
		var label;
        label = index;
        anunp.poll(windowStarts, label)
    };

	IFFT(b)*0.1;

}.play)

({
	var windowStarts, stream;

	//a = WhiteNoise.ar(1);

	//a = SinOsc.ar(400);
	a = SoundIn.ar(1);
	a = Compander.ar(a, a,
					thresh: 0.5,
					slopeBelow: 1,
					slopeAbove: 0.5,
					clampTime: 0.01,
					relaxTime: 0.01
				);

	b = LocalBuf(2048, 1);
	c = LocalBuf(2048, 1);

	b = FFT(b, a);
	c = PV_Copy(b, c);

	PV_Control2(b, c, 0.5, 0.8, 100, 100, 50, 18, 10, 100, 200);

	windowStarts = b > -1;

	stream = Unpack1FFT(c, 2048, (1..200), 0);

	d = Demand.kr(windowStarts, 0, stream);

/*	Demand.kr(windowStarts, 0, stream).collect { |anunp, index|
		var label;
        label = index;
        anunp.poll(windowStarts, label)
    };*/

	//IFFT(b);

	200.do{arg i;
		a = MidEQ.ar(a, (i+1)*(22050/2048), 0.2, d[i].lincurve(0,1,0.5,1,-4).ampdb);
	};

	a
}.play)