QtBusAssignSink {
	var parent, numColumns, columns, <>panel, sink, busIns, <>busInLabels, currentPoint, buttons, pointTemp, buttonListTemp, columnListTemp, busIn, busInLabel;

	*new {arg parent, numColumns;
		^super.newCopyArgs(parent, numColumns).init;
	}

	init {
		buttons = IdentityDictionary.new;
		busIns = List.new;
		busInLabels = List.new;
		this.makeSink;
		sink.string="Bus";
		sink.background_(Color.red);
		sink.receiveDragHandler={arg v;
			busIn = View.currentDrag[0];
			busInLabel = View.currentDrag[1];
			this.assignBus(busIn, busInLabel);
		};
		panel = CompositeView().maxWidth_(20*numColumns);
		this.updateButtons;
	}

	assignBus {arg busIn;
		var busInLabel;

		busInLabel = busIn.asSymbol;

		if(busInLabels.indexOf(busInLabel)==nil,{
			busInLabels.add(busInLabel);
			buttons.put(busInLabel.asSymbol,
				Button()
				.states_([[busInLabel, Color.black, Color.yellow]])
				.action_({arg butt;
					busInLabels.remove(butt.states[0][0].asSymbol);
					buttons[butt.states[0][0].asSymbol].setProperty(\visible, false);
					buttons.removeAt(butt.states[0][0].asSymbol);
					//"busInLabels".post;
					if(parent!=nil, {parent.setInputBusses(busInLabels)});
					this.updateButtons;
				});
			);
			buttons[busIn.asSymbol].font_(Font("Helvetica",8)).maxWidth_(20).maxHeight_(10);
			this.updateButtons;

			if(parent!=nil, {parent.setInputBusses(busInLabels)});
		});
	}

	makeSink {
		sink = DragSink().maxWidth_(20).maxHeight_(20).font_(Font("Helvetica",8)).align_(\center);
	}

	removeButtons {
		buttons.do{arg butt;

			busInLabels.remove(butt.states[0][0].asSymbol);
			buttons[butt.states[0][0].asSymbol].setProperty(\visible, false);
			buttons.removeAt(butt.states[0][0].asSymbol);
			"busInLabels".post;
			if(parent!=nil, {parent.setInputBusses(busInLabels)});
			this.updateButtons;
		};
		busIns = List.new;
		this.updateButtons;
	}

	updateButtons {
		buttonListTemp = List.new;
		columnListTemp = List.new;
		buttons.keys.do{arg item;
			buttonListTemp.add([item.asInteger, buttons[item]])
		};
		buttonListTemp=buttonListTemp.sort{arg a,b; a[0]<b[0]};
		columnListTemp = Array.fill(numColumns, {List.newClear(0)});
		columnListTemp[0].add([sink, rows:2]);
		buttonListTemp.do{arg button, i;
			if(i<(2*(numColumns-1)),{
				columnListTemp[1+(i%(numColumns-1))].add(button[1]);
			},{
				columnListTemp[((i-(2*(numColumns-1)))%numColumns)].add(button[1]);
			})
		};
		columns = columnListTemp;
		panel.layout_(GridLayout.columns(*columns).margins_(0!4).spacing_(0));

	}

	clearAll {

	}
}
