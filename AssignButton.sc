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
	}

	setBounds {arg boundsIn;
		bounds = boundsIn;
		instantButton.bounds_(bounds);
	}

	layout {^instantButton}

	setInstBut {arg val; instantButton.value = val}
}