VDelayInline_Mod : Module_Mod {
	var buffers, transferGroup, synthGroup, transferBus, volBus, currentSynth, synthSeq, delBus, bufStream, clipBus, releaseFeedbackBus, ampModFreqBus, ampModOnOffBus;

	*initClass {
		StartUp.add {
			//this is stereo right now. it would be nice if it were 8 channel...kind of hairy

			SynthDef(\vDelayPassThrough, {arg inBus, transferBus, outBus, passThrough=1, interruptInline=0;
				Out.ar(transferBus, In.ar(inBus, 2));
				Out.ar(outBus, In.ar(inBus, 2)*Lag2.kr(passThrough, 0.01)*Lag2.kr(interruptInline, 0.01));
			}).writeDefFile;

			SynthDef(\vDelay_mod, {arg inBus, outBus, volBus, sinFreq = 0.05, delBus, clipBus, releaseFeedbackBus, ampModFreqBus, ampModOnOffBus, buffer, flipOn=0, gate = 1, pauseGate = 1;
				var in, tapPhase, tap, localIn, env, pauseEnv, delTime, delTimeB, feedbackSig, clipVal, ampModTrig, out, ampModFreq;

				//#localIn0, localIn1 = LocalIn.ar(2);

				localIn = LocalIn.ar(1);

				in = (In.ar(inBus)*Lag.kr(gate, 0.05))+localIn;

				delTimeB = Gate.kr(Lag2.kr(In.kr(delBus), 0.05), gate);

				tapPhase = DelTapWr.ar(buffer, in);

				tap= DelTapRd.ar(buffer, tapPhase,
					delTimeB+SinOsc.kr(sinFreq, 0, 0.01, 0.01), // tap times
					1,                      // no interp
					1         // muls for each tap
				);

				clipVal = In.kr(clipBus);

				ampModFreq = In.kr(ampModFreqBus);
				ampModTrig = Dust.kr(ampModFreq);


				tap = tap*(Select.kr(In.kr(ampModOnOffBus), [1, Demand.kr(ampModTrig, 0, Dseq([-1,1], inf))]));

				feedbackSig = Clip.ar(tap, clipVal.neg, clipVal);

				LocalOut.ar(feedbackSig*SelectX.kr(gate, [In.kr(releaseFeedbackBus),0.95]));

				env = EnvGen.kr(Env.asr(0.02,1,10*In.kr(releaseFeedbackBus)), gate, doneAction: 2);
				pauseEnv = EnvGen.kr(Env.asr(0,1,0), pauseGate, doneAction:1);

				out = tap*env*pauseEnv*In.kr(volBus)*(Lag.kr(flipOn, 0.05));

				Out.ar(outBus, out)
			}).writeDefFile;
		}
	}

	init {
		this.makeWindow("VDelayInline", Rect(807, 393, 217, 217));
		this.initControlsAndSynths(9);

		this.makeMixerToSynthBus(2);

		buffers = Array.fill(64, {Buffer.alloc(group.server, group.server.sampleRate*0.5, 1)});
		bufStream = Pseq((0..63), inf).asStream;
		/*
		buffer0 = Buffer.alloc(group.server, group.server.sampleRate*0.5, 1);
		buffer1 = Buffer.alloc(group.server, group.server.sampleRate*0.5, 1);*/

		transferBus = Bus.audio(group.server, 2);
		volBus = Bus.control(group.server);
		volBus.set(0);
		delBus = Bus.control(group.server);
		delBus.set(0);

		releaseFeedbackBus = Bus.control(group.server);
		releaseFeedbackBus.set(0);

		ampModFreqBus = Bus.control(group.server);
		ampModFreqBus.set(0);

		ampModOnOffBus = Bus.control(group.server);
		ampModOnOffBus.set(0);

		clipBus = Bus.control(group.server);
		clipBus.set(1);

		transferGroup = Group.tail(group);
		synthGroup = Group.tail(group);

		synthSeq = Pseq((0..3), inf).asStream;
		currentSynth = synthSeq.next;

		4.do{arg i2;
			2.do{arg i;
				synths.add(Synth("vDelay_mod", [\inBus, transferBus.index+i, \outBus, outBus.index+i, \volBus, volBus, \sinFreq, rrand(0.045, 0.055), \delBus, delBus, \clipBus, clipBus, \releaseFeedbackBus, releaseFeedbackBus, \ampModFreqBus, ampModFreqBus, \ampModOnOffBus, ampModOnOffBus, \buffer, buffers[bufStream.next]], synthGroup));
			}
		};

		synths.add(Synth("vDelayPassThrough", [\inBus, mixerToSynthBus, \transferBus, transferBus, \outBus, outBus, \passThrough, 1], transferGroup));

		controls.add(QtEZSlider.new("vol", ControlSpec(0,1,'amp'),
			{|v|
				volBus.set(v.value);
		}, 0, true));
		this.addAssignButton(0,\continuous);


		controls.add(Button());
		controls.add(Button());
		this.addAssignButton(1, \onOff);
		this.addAssignButton(2, \onOff);

		RadioButtons([controls[1],controls[2]],
			[
				[[ "delay", Color.red, Color.black ], [ "delay", Color.black, Color.red ]], [[ "through", Color.red, Color.black ], [ "through", Color.black, Color.red ]]
			],
			[
				{
					this.turnOn
				},{
					this.turnOff
				}
			],
			1);

		controls.add(QtEZSlider2D.new(ControlSpec(0,0.4), ControlSpec(0,1),
			{arg vals;
				delBus.set(vals.value[0]);

				clipBus.set(vals.value[1]);

				//controls[2].valueAction_([(vals.value[0]-(0.65*vals.value[1]*yRange)).clip(0,1), (vals.value[0]+(0.65*vals.value[1]*yRange)).clip(0,1)]);
			}
		));
		this.addAssignButton(3,\slider2D);



		controls.add(Button()
			.states_([ [ "NoZActions", Color.red, Color.black ],  [ "ZActions!", Color.blue, Color.black ]])
			.action_{|v|
				if(v.value==1,{
					controls[3].zAction = {|val|
						if(val.value==1,{this.turnOn},{this.turnOff})
					};
				},{
					controls[3].zAction = {};
				}
				);
			};
		);


		controls.add(QtEZSlider.new("fback%", ControlSpec(0,0.95),
			{|v|
				releaseFeedbackBus.set(v.value);
		}, 0, true));
		this.addAssignButton(5,\continuous);

		//this button is actually first in the display
		controls.add(Button()
			.states_([ [ "Interrupt", Color.red, Color.black ],  [ "Inline", Color.blue, Color.black ]])
			.action_{|v|
				synths[8].set(\interruptInline, v.value)
		});
		this.addAssignButton(6,\onOff);

		win.layout_(
			HLayout(
				VLayout(controls[6].maxWidth_(60).maxHeight_(15), assignButtons[6].layout, controls[0].layout,assignButtons[0].layout),
				VLayout(
					HLayout(controls[1].maxWidth_(60).maxHeight_(15),controls[2].maxWidth_(60).maxHeight_(15)),
					controls[3].layout,
					assignButtons[3].layout,
					controls[4]
				),
				VLayout(
					controls[5].layout,
					assignButtons[5].layout
				)
			)
		);
		win.layout.spacing = 0;
		win.layout.margins = [0,0,0,0];
	}

	turnOn {
		synths[8].set(\passThrough, 0);
		currentSynth = synthSeq.next;
		2.do{arg i;
			synths[currentSynth*2+i].set(\flipOn, 1);
		};
	}

	turnOff {
		2.do{arg i;
			synths[8].set(\passThrough, 1);
			synths[currentSynth*2+i].set(\gate, 0);

			synths.put(currentSynth*2+i, Synth("vDelay_mod", [\inBus, transferBus.index+i, \outBus, outBus.index+i, \volBus, volBus, \sinFreq, rrand(0.045, 0.055), \delBus, delBus, \clipBus, clipBus, \releaseFeedbackBus, releaseFeedbackBus, \ampModFreqBus, ampModFreqBus, \ampModOnOffBus, ampModOnOffBus, \buffer, buffers[bufStream.next]], synthGroup));
		}
	}

}