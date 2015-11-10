BusAssignSink {
	var parent, panel, point, sink, busIns, currentPoint, buttons, pointTemp, buttonListTemp, busIn, busInLabel, busInMap;

	*new {arg parent, panel, point;
		^super.newCopyArgs(parent, panel, point).init;
	}

	init {
		buttons = IdentityDictionary.new;
		busInMap = IdentityDictionary.new;
		currentPoint = Point(point.x+25, point.y);
		busIns = List.new;
		this.makeSink;
		sink.string="Bus";
		sink.background_(Color.red);
		sink.receiveDragHandler={arg v;
			busIn = View.currentDrag[0];
			busInLabel = View.currentDrag[1];
			this.assignBus(busIn, busInLabel);
		};
	}

	assignBus {arg busIn, busInLabel;

		if(parent.confirmValidBus(busIn), {

			busInMap.put(busInLabel.asSymbol, busIn);
			if(busIns.indexOf(busIn)==nil,{
				busIns.add(busIn);
				buttons.put(busIn.asSymbol,
					Button(panel, Rect(currentPoint.x, currentPoint.y, 25, 16))
					.states_([[busInLabel, Color.black, Color.yellow]])
					.action_({arg butt;
						busIns.remove(busInMap[butt.states[0][0].asSymbol]);
						buttons[busInMap[butt.states[0][0].asSymbol].asSymbol].setProperty(\visible, false);
						buttons.removeAt(busInMap[butt.states[0][0].asSymbol].asSymbol);
						parent.setInputBusses(busIns);
						this.updateButtons;
					});
				);
				buttons[busIn.asSymbol].font_(Font("Helvetica",10));
				this.updateButtons;

				parent.setInputBusses(busIns);
			});
		});
	}

	makeSink {
		sink = DragSink(panel, Rect(point.x, point.y, 25, 32));
	}

	getCurrentPoint {
		currentPoint =  Point(point.x+25+(25*(busIns.size%3)), point.y+((busIns.size/3).floor*16));
	}

	removeButtons {
		buttons.keys.do{arg key;
			buttons[key].setProperty(\visible, false);
			buttons.removeAt(key);
		};
		busIns = List.new;
	}

	updateButtons {
		buttonListTemp = List.new;
		buttons.keys.do{arg item;
			buttonListTemp.add([item.asInteger, buttons[item]])
		};
		buttonListTemp=buttonListTemp.sort{arg a,b; a[0]<b[0]};
		buttonListTemp.do{arg item, i;
			pointTemp = Point(point.x+25+(25*(i%3)), point.y+((i/3).floor*16));
			item[1].bounds_(Rect(pointTemp.x, pointTemp.y, 25, 16));
		}
	}

	clearAll {

	}
}

MixerBusAssignSink : BusAssignSink {

	makeSink {
		[parent, panel, point].postln;
		sink = DragSink(panel, Rect(point.x, point.y, 25, 16));
	}

	getCurrentPoint {
		currentPoint =  Point(point.x+(25*(busIns.size+1%2)), point.y+((busIns.size+1/2).floor*16));
	}

	updateButtons {
		buttonListTemp = List.new;
		buttons.keys.do{arg item;
			buttonListTemp.add([item.asInteger, buttons[item]])
		};
		buttonListTemp=buttonListTemp.sort{arg a,b; a[0]<b[0]};
		buttonListTemp.do{arg item, i;
			pointTemp = Point(point.x+(25*(i+1%2)), point.y+((i+1/2).floor*16));
			item[1].bounds_(Rect(pointTemp.x, pointTemp.y, 25, 16));
		}
	}


}

DiscreteInput_Mod : ModularMixerStrip {

	init2 {arg winIn, pointIn;
		win=winIn;
		point = pointIn;

		inputBusses = List.new;

		panel = CompositeView.new(win, Rect(point.x, point.y, 50, 300));

		mixer = ModularMixer(group);
		mixer.outBus = outBus;
		mixer.setVol(1);

		busAssignSink=MixerBusAssignSink(this, panel, Point(0,0));

		numBusses = outBus.numChannels;

	}

	setInputBusses {arg inputBussesIn;
		inputBusses = mixer.setInputBusses(inputBussesIn, numBusses);
	}

	save {
		^inputBusses
	}

	load {arg loadArray;
		loadArray.do{arg item, i;
			var bus, label, index, temp;

			temp = ModularServers.servers[group.server.asSymbol].busMap[0][item.asSymbol];

			if(temp!=nil,{
				#bus, index=temp;
				busAssignSink.assignBus(bus, "S"++index.asString);
				},{
					temp = ModularServers.servers[group.server.asSymbol].busMap[1][item.asSymbol];
					if(temp!=nil,{
						#bus, index=temp;
						busAssignSink.assignBus(bus, "S"++((index*2)).asString++((index*2+1)).asString);
						},{
							bus = ModularServers.servers[group.server.asSymbol].busMap[2][item.asSymbol];
							if(bus!=nil,{busAssignSink.assignBus(bus, bus)});
					})
			});
		};
	}
}