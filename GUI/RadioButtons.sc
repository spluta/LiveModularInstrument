RadioButtons {

	var <>layout;
	var <>action, <value;
	var buttons;

	*new { arg buttons, labels, actions, initVal, initAction=true;

		^super.new.init(buttons, labels, actions, initVal, initAction);
	}

	init { arg buttons, argLabels, argActions, initVal, initAction;
		var numberStep, buttonRow;

		//buttons = List.newClear(0);

		buttons.do{arg item, i;
			item.do{arg item2, i2;
				item2.states_(argLabels[i][i2])
					.action_{|v|
						buttons.do{arg item; item.do{|item2| item2.value=0}};
						buttons[i][i2].value=1;
						argActions[i][i2].value(v.value);
					}
			};
			buttons.add(buttonRow);
		};

/*		switch(buttons.size,
			1, {layout = GridLayout.rows(buttons[0])},
			2, {layout = GridLayout.rows(buttons[0],buttons[1])},
			3, {layout = GridLayout.rows(buttons[0],buttons[1],buttons[2])},
			4, {layout = GridLayout.rows(buttons[0],buttons[1],buttons[2],buttons[3])},
			5, {layout = GridLayout.rows(buttons[0],buttons[1],buttons[2],buttons[3],buttons[4])},
			{layout = GridLayout.rows(buttons[0],buttons[1],buttons[2],buttons[3],buttons[4])}
		);*/

		if(initAction,{buttons[initVal[0]][initVal[1]].valueAction=1;});
	}

	value_ { arg val;
		buttons.do{arg item; item.do{|item2| item2.value=0}};
		buttons[val[0]][val[1]].value=1;
	}

	valueAction_ { arg val;
		buttons[val[0]][val[1]].valueAction=1;
	}

}