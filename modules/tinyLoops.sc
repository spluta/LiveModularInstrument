//with the roli

TinyLoops_Mod :  Module_Mod {
	var texts, functions, rec_group, play_group, current_synth, current_stream, vol_bus, phase_busses, buffers, dur, deviation;

	*initClass {
		StartUp.add {
			SynthDef("tiny_loops_recorder_mod", {
				arg buffer, phase_bus, play_rec = 1;
				var in = In.ar(\in_bus.kr, 2);
				var phase = Phasor.ar(0, BufRateScale.kr(buffer), 0, BufFrames.kr(buffer));

				phase = Select.ar(play_rec, [Latch.ar(phase, 1-play_rec), phase]);
				Out.ar(phase_bus, phase);

				BufWr.ar(in, buffer, phase, 1.0);
			}).writeDefFile;

			SynthDef("tiny_loops_player_mod", {
				arg buffer, phase_bus, play_rec = 1, loop_dur=1.5;
				var rec_play = Delay2.kr(1-play_rec);
				var buf_frames = BufFrames.kr(buffer);
				var in_frame = Latch.ar(In.ar(phase_bus), rec_play);

				var startPos = (in_frame-((loop_dur+0.01)*SampleRate.ir)).wrap(0, BufFrames.kr(buffer));

				var phasor = Phasor.ar(rec_play, 1/SampleRate.ir, 0.0, loop_dur);

				var trig = (phasor-0.001)*rec_play;

				var trig0 = PulseDivider.ar(trig, 2, 1);
				var trig1 = PulseDivider.ar(trig, 2, 0);

				var sq0 = Trig1.ar(trig0, loop_dur-0.02);
				var env0 = Lag.ar(sq0, 0.02);
				var sound0 = PlayBuf.ar(2, buffer, 1, trig0, startPos, 1);

				var sq1 = Trig1.ar(trig1, loop_dur-0.02);
				var env1 = Lag.ar(sq1, 0.02);
				var sound1 = PlayBuf.ar(2, buffer, 1, trig1, startPos, 1);

				var out = (sound0*env0+(sound1*env1))*Lag.kr(rec_play, 0.01);
				var vol = In.kr(\vol_bus.kr);
				Out.ar(\out_bus.kr, out*vol);
			}).writeDefFile;
		}
	}

	init {

		this.makeWindow("TinyLoops");

		this.initControlsAndSynths(5);
		this.makeMixerToSynthBus(2);
		dontLoadControls.addAll([0,1]);

		vol_bus = Bus.control(group.server, 1);
		phase_busses = Array.fill(4, {Bus.audio(group.server)});
		buffers = Array.fill(4, {Buffer.alloc(group.server, group.server.sampleRate*10,2)});

		rec_group = Group.tail(group);
		play_group = Group.tail(group);

		4.do{|i|
			synths.add(Synth("tiny_loops_recorder_mod", [\in_bus, mixerToSynthBus.index, \phase_bus, phase_busses[i], \buffer, buffers[i]], rec_group));
		};

		4.do{|i|
			synths.add(Synth("tiny_loops_player_mod", [\out_bus, outBus, \vol_bus, vol_bus, \phase_bus, phase_busses[i], \buffer, buffers[i]], play_group));
		};

		current_stream = Pseq((0..3), inf).asStream;
		current_synth = current_stream.next;

		texts = List.newClear(0);

		texts = ["stop", "new_loop", "dur", "deviation", "vol"];
		dur = 0;
		deviation = 0;

		functions = [

			{arg val;
				if(val==1){
					synths[current_synth].set(\play_rec, 1);
					synths[current_synth+4].set(\play_rec, 1);
				}
			},
			{arg val;
				if(val==1){
				synths[current_synth].set(\play_rec, 1);
				synths[current_synth+4].set(\play_rec, 1);

				current_synth = current_stream.next;

				synths[current_synth].set(\play_rec, 0);
				synths[current_synth+4].set(\play_rec, 0, \loop_dur, rrand(dur-(dur*deviation), dur+(dur*deviation)).clip(0.02,9.5).postln);
				}
			},

			{arg val;  dur = val.linexp(0,1,0.05,10)},
			{arg val;  deviation = val},
			{arg val;  vol_bus.set(val)}
		];

		functions.do{arg func, i;
			controls.add(TypeOSCFuncObject(this, oscMsgs, i, texts[i], func, true, false));
		};

		win.layout_(
			VLayout(
				VLayout(*controls.collect({arg item; item}))
			)
		);
		win.layout.spacing_(1).margins_(1!4);
		win.view.maxHeight_(5*17);
		win.view.resizeTo(5*17,13*17);
		win.front;
	}
}

