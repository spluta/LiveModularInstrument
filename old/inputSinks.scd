QtDiscreteInput_Mod : QtModularMixerStrip {

		init2 {arg isMainMixer=false;

		this.initControlsAndSynths(2);

		mixerGroup = Group.head(group);
		reducerGroup = Group.tail(group);

		transferBus = Bus.audio(group.server, 2);

		inputBusses = List.new;

		mixer = ModularMixer(mixerGroup);
		mixer.outBus = transferBus;

		transferSynth = Synth.tail(reducerGroup, "transferSynthB_mod", [\transferBus, transferBus, \outBus, outBus]);

		panel = CompositeView().maxWidth_(70).maxHeight_(150);

		busAssignSink=QtBusAssignSink(this, 1);

		soundInBusses = ModularServers.getSoundInBusses(group.server);
		this.init3;
	}

	init3 {arg winIn, pointIn;

		transferSynth.free;
		mixer.outBus = outBus;

		panel.layout_(
			VLayout([busAssignSink.panel, align:\top]).margins_(0!4).spacing_(0)
		)

	}

}
