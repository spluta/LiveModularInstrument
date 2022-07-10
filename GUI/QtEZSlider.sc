QtEZSlider {

	var <>controlSpec, <>slider, <>numBox, <>label, <>layout;
	var <>round = 0.001;
	var <>action, <value, <>zAction, viewArray;

	*new { arg label, controlSpec, action, initVal,
			initAction=false, orientation=\vert, viewNumberBox=true;

	^super.new.init(label, controlSpec, action, initVal,
		initAction, orientation, viewNumberBox);
	}

	init { arg argLabel, argControlSpec, argAction, initVal, initAction, orientation, viewNumberBox;
		var numberStep;

		viewArray = List.newClear;

		if(argLabel!=nil,{
			label = StaticText();
			label.string = argLabel;
			label.maxHeight_(15).maxWidth_(60).font_(Font("Helvetica", 10));
			viewArray.add(label);

		},{
			label=nil
		});

		slider = Slider();
		slider.maxHeight_(150);
		viewArray.add(slider);

		numBox = NumberBox();
		numBox.maxWidth_(60);
		numBox.maxHeight_(15);
		numBox.font_(Font("Helvetica", 10));
		if(viewNumberBox, {viewArray.add(numBox)});

		zAction = {}; //the default zAction is to do nothing

		switch(orientation,
			\vert, {slider.orientation=\vertical; layout = VLayout(*viewArray)},
			\vertical, {slider.orientation=\vertical; layout = VLayout(*viewArray)},
			\horz, {slider.orientation=\horizontal; layout = HLayout(*viewArray)},
			\horizontal, {slider.orientation=\horizontal; layout = HLayout(*viewArray)});

		// set view parameters and actions

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);

		initVal = initVal ? controlSpec.default;
		action = argAction;

		slider.action = {
			this.valueAction_(controlSpec.map(slider.value));
		};

		slider.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		slider.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};

		numBox.action = { this.valueAction_(numBox.value) };

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			numBox.alt_scale = 1.0;
			slider.alt_scale = 1.0;
		};

		numBox.step = numberStep;
		numBox.scroll_step = numberStep;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		if (label.notNil) {
			label.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

	}

	asView {^layout}

	maxHeight_ {arg val;
		slider.maxHeight_(val)
	}

	maxWidth_ {arg val;
		viewArray.do{arg item;
			item.maxWidth_(val);
		}
	}

	onClose{controlSpec.removeDependant(this)}

	value_ { arg val;
		value = controlSpec.constrain(val);
		{
			numBox.value = value.round(round);
			slider.value = controlSpec.unmap(value);
		}.defer;
	}

	map {arg val;
		^controlSpec.unmap(value);
	}

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}

	doAction { action.value(this) }

	set { arg label, spec, argAction, initVal, initAction = false;
		label.notNil.if { label.string = label.asString };
		spec.notNil.if { controlSpec = spec.asSpec };
		argAction.notNil.if { action = argAction };

		initVal = initVal ? value ? controlSpec.default;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
	}

	font_{ arg font;

			label.notNil.if{label.font=font};
			numBox.font=font;
	}

}

QtEZRanger {

	var <>controlSpec, <>rangeSlider, <>hiBox, <>loBox, <hi, <lo, <>label, <>layout;
	var <>round = 0.001;
	var <>action, value, <>zAction;

	*new { arg label, controlSpec, action, initVal,
			initAction=false, orientation=\vert;

		^super.new.init(label, controlSpec, action, initVal,
			initAction, orientation);
	}

	init { arg argLabel, argControlSpec, argAction, initVal, initAction, orientation;
		var numberStep;

		rangeSlider = RangeSlider();
		rangeSlider.orientation=orientation;
		hiBox = NumberBox().font_(Font("Helvetica", 10));
		hiBox.maxWidth_(60).maxHeight_(15);
		loBox = NumberBox().font_(Font("Helvetica", 10));
		loBox.maxWidth_(60).maxHeight_(15);
		label = StaticText().font_(Font("Helvetica", 10));
		label.string = argLabel;

		switch(orientation,
			\vert, {rangeSlider.orientation = \vertical; layout = VLayout(label, hiBox, rangeSlider, loBox)},
			\vertical, {rangeSlider.orientation = \vertical; layout = VLayout(label, hiBox, rangeSlider, loBox)},
			\horz, {rangeSlider.orientation = \horizontal; layout = HLayout(label, loBox, rangeSlider.maxHeight_(15), hiBox)},
			\horizontal, {rangeSlider.orientation = \horizontal; layout = HLayout(label, loBox, rangeSlider.maxHeight_(15), hiBox)});

		// set view parameters and actions

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);

		initVal = initVal ? [controlSpec.minval, controlSpec.maxval];
		action = argAction;

		zAction = {}; //the default zAction is to do nothing

		rangeSlider.receiveDragHandler = { arg rangeSlider;
			rangeSlider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		rangeSlider.beginDragAction = { arg rangeSlider;
			controlSpec.map(rangeSlider.value)
		};

		loBox.action_({ |box| this.lo_(box.value).doAction; });
		rangeSlider.action_({ |sl|
				this.lo_(controlSpec.map(sl.lo));
				this.hi_(controlSpec.map(sl.hi));
				this.doAction;
			});
		hiBox.action_({ |box| this.hi_(box.value).doAction; });

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			hiBox.alt_scale = 1.0;
			loBox.alt_scale = 1.0;
			rangeSlider.alt_scale = 1.0;
		};

		hiBox.step = numberStep;
		hiBox.scroll_step = numberStep;
		loBox.step = numberStep;
		loBox.scroll_step = numberStep;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		if (label.notNil) {
			label.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

	}

	asView {^layout}

	map {arg val;
		^controlSpec.unmap(value);
	}

	value { ^[lo, hi] }
	value_ { |vals|
		this.lo_(vals[0]).hi_(vals[1]) }
	valueAction_ { |vals| this.value_(vals).doAction }

	lo_ { |val|
		lo = controlSpec.constrain(val);
		{
			loBox.value_(lo.round(round));
			rangeSlider.lo_(controlSpec.unmap(lo));
		}.defer
	}

	hi_ { |val|
		hi = controlSpec.constrain(val);
		{
			hiBox.value_(hi.round(round));
			rangeSlider.hi_(controlSpec.unmap(hi));
		}.defer
	}

	onClose{controlSpec.removeDependant(this)}

	doAction { action.value(this) }

}

QtEZSlider2D {

	var <>controlSpecX, <>controlSpecY, <>slider, <x, <y, <>layout;
	var <>round = 0.001;
	var <>action, value, <>zAction;

	*new { arg controlSpecX, controlSpecY, action, initVal,
			initAction=false;

		^super.new.init(controlSpecX, controlSpecY, action, initVal,
			initAction);
	}

	init { arg argcontrolSpecX, argcontrolSpecY, argAction, initVal, initAction;
		var numberStep;

		slider = Slider2D();

		x = 0; y = 0;

		// set view parameters and actions

		controlSpecX = argcontrolSpecX.asSpec;
		controlSpecX.addDependant(this);

		controlSpecY = argcontrolSpecY.asSpec;
		controlSpecY.addDependant(this);

		initVal = initVal ? [controlSpecX.default,controlSpecY.default];
		action = argAction;

		zAction = {}; //the default zAction is to do nothing

		slider.action_({ |sl|
			action.value([controlSpecX.map(sl.x), controlSpecY.map(sl.y)]);

			});

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		layout = slider;
	}

	asView {^layout}

	map {arg val;
		^[controlSpecX.unmap(val[0]),controlSpecY.unmap(val[1])];
	}

	value { ^[x, y] }
	value_ { |vals| slider.setXY(vals[0], vals[1]) }
	valueAction_ { |vals|
		this.value_(vals);
		action.value([controlSpecX.map(vals[0]), controlSpecY.map(vals[1])]);
	}

	activex_ { |val|
		{slider.x_(val)}.defer;
		x = val;
		action.value([controlSpecX.map(x), controlSpecY.map(y)]);
	}

	activey_ { |val|
		{slider.y_(val)}.defer;
		y = val;
		action.value([controlSpecX.map(x), controlSpecY.map(y)]);
	}


}

// QtEZLists : EZGui{  // an abstract class
//
// 	var <items, <>globalAction;
//
// 	*new { arg parentView, bounds, label,items, globalAction, initVal=0,
// 		initAction=false, labelWidth,labelHeight=20, layout, gap, margin;
//
// 		^super.new.init(parentView, bounds, label, items, globalAction, initVal,
// 		initAction, labelWidth,labelHeight,layout, gap, margin);
// 	}
//
// 	init { arg parentView, bounds, label, argItems, argGlobalAction, initVal,
// 		initAction, labelWidth, labelHeight, layout,  argGap, argMargin;
//
// 		// try to use the parent decorator gap
// 		this.prMakeMarginGap(parentView, argMargin, argGap);
//
// 		// init the views (handled by subclasses)
// 		this.initViews(  parentView, bounds, label, labelWidth,labelHeight,layout );
//
// 		this.items=argItems ? [];
//
// 		globalAction=argGlobalAction;
//
// 		widget.action={arg obj;
// 			items.at(obj.value).value.value(this);
// 			globalAction.value(this);
// 		};
//
// 		this.value_(initVal);
//
// 		items.notNil.if{
// 			if(initAction){
// 				items.at(initVal).value.value(this); // You must do this like this
// 				globalAction.value(this);	// since listView's array is not accessible yet
// 			};
// 			this.value_(initVal);
// 		};
//
// 	}
//
// 	initViews{}  // override this for your subclass views
//
// 	value{ ^widget.value}
// 	value_{|val| widget.value=val}
//
// 	valueAction_{|val| widget.value_(val); this.doAction}
//
// 	doAction {widget.doAction;}
//
// 	items_{ arg assocArray;
// 		assocArray = assocArray.collect({ |it| if (it.isKindOf(Association), { it }, { it -> nil }) });
// 		items=assocArray;
// 		widget.items=assocArray.collect({|item| item.key});
// 	}
//
// 	item {^items.at(this.value).key}
// 	itemFunc {^items.at(this.value).value}
//
// 	addItem{arg name, action;
// 		this.insertItem(nil, name, action);
// 	}
//
// 	insertItem{ arg index, name, action;
// 		var temp;
// 		index = index ? items.size;
// 		this.items=items.insert(index, name.asSymbol -> action);
// 	}
//
// 	removeItemAt{ arg index;
// 		var temp;
// 		items.removeAt(index);
// 		this.items_(items)
//
// 	}
//
// 	replaceItemAt{ arg index, name, action;
// 		var temp;
// 		name = name ? items.at(index).key;
// 		action = action ? items.at(index).value;
// 		this.removeItemAt(index);
// 		this.insertItem(index, name, action);
//
// 	}
//
// }
// QtEZPopUpMenu : EZLists{
//
// 	initViews{ arg parentView, bounds, label, labelWidth,labelHeight,arglayout;
// 		var labelBounds, listBounds;
//
// 		labelWidth = labelWidth ? 80;
// 		layout=arglayout ? \horz;
// 		labelSize=labelWidth@labelHeight;
//
// 		bounds.isNil.if{bounds= 160@20};
//
// 		// if no parent, then pop up window
// 		# view,bounds = this.prMakeView( parentView,bounds);
//
// 		// calcualate bounds
// 		# labelBounds,listBounds = this.prSubViewBounds(innerBounds, label.notNil);
//
// 		// insert the views
//
// 		/*		label.notNil.if{ //only add a label if desired
// 		if ((layout==\vert)(layout==\vertical)||{
// 		labelView = StaticText.new(view, labelBounds).resize_(2);
// 		labelView.align = \left;
// 		}{
// 		labelView = StaticText.new(view, labelBounds);
// 		labelView.align = \right;
// 		};
// 		labelView.string = label;
// 		};*/
//
// 		widget = PopUpMenu.new(view, listBounds).resize_(5);
// 	}
//
// 	menu {^ widget}
//
// 	setColors{arg stringBackground, stringColor, menuBackground,  menuStringColor,background ;
//
// 		stringBackground.notNil.if{
// 		labelView.notNil.if{labelView.background_(stringBackground)};};
// 		stringColor.notNil.if{
// 		labelView.notNil.if{labelView.stringColor_(stringColor)};};
// 		menuBackground.notNil.if{
// 		this.menu.background_(menuBackground);};
// 		menuStringColor.notNil.if{
// 		this.menu.stringColor_(menuStringColor);};
// 		background.notNil.if{
// 		view.background=background;};
// 	}
//
// }


GUIModule {
	var knob, slider, <>layout;

	*new {

		^super.new.init();
	}

	init {

		knob = Knob();
		slider = QtEZSlider("vol", ControlSpec(0,5), {arg slider; slider.value}, 0);

		layout = VLayout(knob, slider.layout);
	}
}
