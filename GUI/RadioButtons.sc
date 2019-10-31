RadioButtons {

	var <>layout;
	var <>action, <value;
	var buttons, <>activeButtons, <>onButtons;

	*new { arg buttons, labels, actions, initVal, initAction=true;

		^super.new.init(buttons, labels, actions, initVal, initAction);
	}

	init { arg buttons, argLabels, argActions, initVal, initAction;
		var numberStep, buttonRow;

		activeButtons = List.newClear.addAll(buttons);
		onButtons = List.newClear;

		buttons.do{arg item, i;
			item.states_(argLabels[i])
			.action_{|v|
				if(activeButtons.indexOf(v)!=nil,{
					activeButtons.do{arg item; item.do{|item2| item2.value=0}};
				});
				buttons[i].value=1;
				onButtons = List.newClear(0);
				buttons.do({|item, i| if(item.value==1, {onButtons.add(i)})});
				argActions[i].value(v.value);
			}
		};

		if(initAction,{buttons[initVal].valueAction=1;});
	}

	deactivateButton {|button|
		activeButtons.remove(button);
	}

	activateButton {|button|
		activeButtons.add(button);
	}

	value_ { arg val;
		buttons.do{arg item; item.do{|item2| item2.value=0}};
		buttons[val[0]][val[1]].value=1;
	}

	valueAction_ { arg val;
		buttons[val[0]][val[1]].valueAction=1;
	}

}