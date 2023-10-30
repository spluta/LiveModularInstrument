ParameterWindow_Mod {
	classvar <>win, visible;

	*new {
		^super.new.init;
	}

	init {}

	*toggleVisible {
		visible = visible.not;
		win.visible_(visible);
		if(visible,{win.front});
		win.userCanClose_(false);

	}
}

InBusWindow_Mod : ParameterWindow_Mod {

	classvar soundInBoxes, directStereoBoxes, directMonoBoxes;

	*makeWindow {
		soundInBoxes = List.new;
		8.do{arg i;
			soundInBoxes.add(DragSource().maxWidth_(40).maxHeight_(15).font_(Font("Helvetica",10))
				.setProperty(\align,\center)
				.object_(["S"++(i+1).asString, "S"++(i+1).asString])
				.string_("S"++(i+1).asString)
				.dragLabel_("S"++(i+1).asString)
			)
		};

		directStereoBoxes = List.newClear(0);
		80.do{arg i;
			directStereoBoxes.add(DragSource().maxWidth_(40).maxHeight_(15).font_(Font("Helvetica",10))
				.setProperty(\align,\center)
				.object_(["D"++(i*2+1).asString, "D"++(i*2+1).asString])
					.string_("D"++(i*2+1).asString)
						.dragLabel_("D"++(i*2+1).asString)
			)
		};

		directMonoBoxes = List.newClear(0);
		160.do{arg i;
			directMonoBoxes.add(DragSource().maxWidth_(40).maxHeight_(15).font_(Font("Helvetica",10))
				.setProperty(\align,\center)
				.object_(["DM"++(i+1).asString, "D"++(i+1).asString])
					.string_("DM"++(i+1).asString)
						.dragLabel_("DM"++(i+1).asString)
			)
		};

		win = Window("Sound In Busses");
		win.view.maxHeight_(80);
		win.view.resizeTo(800,80);
		win.layout_(
			GridLayout.rows(
				soundInBoxes,
				directStereoBoxes.copyRange(0,19),
				directStereoBoxes.copyRange(20,39),
				directStereoBoxes.copyRange(40,59),
				directStereoBoxes.copyRange(60,79),
				directMonoBoxes.copyRange(0,19),
				directMonoBoxes.copyRange(20,39),
				directMonoBoxes.copyRange(40,59),
				directMonoBoxes.copyRange(60,79),
				directMonoBoxes.copyRange(80,99),
				directMonoBoxes.copyRange(100,119),
				directMonoBoxes.copyRange(120,139),
				directMonoBoxes.copyRange(140,169)
			).spacing_(1).margins_(1!4)
		);
		visible = true;
		win.front;
	}
}


ClassWindow_Mod : ParameterWindow_Mod {
	classvar classBoxItems, classBoxes;

	*makeWindow {
		classBoxes = List.new;
		classBoxItems = List.new;
		classBoxItems = ModularClassList.classArray.deepCopy;

		classBoxItems.size.do{arg i;
			classBoxes.add(DragSource().font_(Font("Helvetica",10)).maxHeight_(15));
			classBoxes[i].object = classBoxItems[i];
		};

		win = Window();
		win.layout_(
			GridLayout.columns(*classBoxes.clump((classBoxes.size/3).ceil)).spacing_(1).margins_(1!4)
		);
		visible = true;
		win.front;
	}
}