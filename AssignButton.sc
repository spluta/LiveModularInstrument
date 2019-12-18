AssignButton {
	var <>win, <>bounds, <>instantButton, <>instantAction;

	*new {arg win, bounds;
		^super.new.win_(win).bounds_(bounds).init;
	}

	init {
		instantAction = {};
		instantButton = Button.new(win, bounds)
			.states_([ [ "AI", Color.red, Color.black ] ,[ "C", Color.black, Color.red ] ])
			.action_{|v|
				instantAction.(v);
			};
		if(bounds==nil,{instantButton.maxHeight_(15).maxWidth_(60)});
	}

	setBounds {arg boundsIn;
		bounds = boundsIn;
		instantButton.bounds_(bounds);
	}

	layout {^instantButton}

	asView {^instantButton}

	setInstBut {arg val; instantButton.value = val}
}

TypeOSCAssignButton {
	var <>instantButton, <>instantAction;

	*new {
		^super.new.init;
	}

	init {
		instantAction = {};
		instantButton = Button().font_(Font("Helvetica", 10)).maxWidth_(15).maxHeight_(15)
		.states_([["AI",Color.red,Color.black],["OK",Color.black,Color.red]])
		.action_({arg butt;
			instantAction.(butt);
		});
	}

	layout {^instantButton}

	setInstBut {arg val; instantButton.value = val}
}