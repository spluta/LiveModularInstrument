QtEZSlider {

	var <>controlSpec, <>slider, <>numBox, <>label, <>layout;
	var <>round = 0.001;
	var <>action, <value, <>zAction;

	*new { arg label, controlSpec, action, initVal,
			initAction=false, orientation=\vert;

		^super.new.init(label, controlSpec, action, initVal,
			initAction, orientation);
	}

	init { arg argLabel, argControlSpec, argAction, initVal, initAction, orientation;
		var numberStep;

		slider = Slider();
		numBox = NumberBox();
		numBox.maxWidth_(60);
		label = StaticText();
		label.string = argLabel;

		zAction = {}; //the default zAction is to do nothing

		switch(orientation,
			\vert, {slider.orientation=\vertical; layout = VLayout(label, slider, numBox)},
			\vertical, {slider.orientation=\vertical; layout = VLayout(label, slider, numBox)},
			\horz, {slider.orientation=\horizontal; layout = HLayout(label, slider, numBox)},
			\horizontal, {slider.orientation=\horizontal; layout = HLayout(label, slider, numBox)});

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

	maxHeight_ {arg val;
		slider.maxHeight_(val)
	}

	onClose{controlSpec.removeDependant(this)}

	value_ { arg val;
		value = controlSpec.constrain(val);
		numBox.value = value.round(round);
		slider.value = controlSpec.unmap(value);
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
		hiBox = NumberBox();
		hiBox.maxWidth_(60);
		loBox = NumberBox();
		loBox.maxWidth_(60);
		label = StaticText();
		label.string = argLabel;

		switch(orientation,
			\vert, {rangeSlider.orientation = \vertical; layout = VLayout(label, hiBox, rangeSlider, loBox)},
			\vertical, {rangeSlider.orientation = \vertical; layout = VLayout(label, hiBox, rangeSlider, loBox)},
			\horz, {rangeSlider.orientation = \horizontal; layout = HLayout(label, loBox, rangeSlider, hiBox)},
			\horizontal, {rangeSlider.orientation = \horizontal; layout = HLayout(label, loBox, rangeSlider, hiBox)});

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

	map {arg val;
		^controlSpec.unmap(value);
	}

	value { ^[lo, hi] }
	value_ { |vals|
		this.lo_(vals[0]).hi_(vals[1]) }
	valueAction_ { |vals| this.value_(vals).doAction }

	lo_ { |val|
		lo = controlSpec.constrain(val);
		loBox.value_(lo.round(round));
		rangeSlider.lo_(controlSpec.unmap(lo));
	}

	hi_ { |val|
		hi = controlSpec.constrain(val);
		hiBox.value_(hi.round(round));
		rangeSlider.hi_(controlSpec.unmap(hi));
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

	map {arg val;
		^[controlSpecX.unmap(val[0]),controlSpecY.unmap(val[1])];
	}

	value { ^[x, y] }
	value_ { |vals| slider.setXY(vals[0], vals[1]) }
	valueAction_ { |vals|
		this.value_(vals);
		action.value([controlSpecX.map(vals[0]), controlSpecY.map(vals[1])]);
	}

/*	x_ { |val|
		x = controlSpecX.constrain(val);
		slider.x_(controlSpecX.unmap(x));
	}

	y_ { |val|
		y = controlSpecY.constrain(val);
		slider.y_(controlSpecY.unmap(y));
	}*/

	activex_ { |val|
		slider.activex_(val);
	}

	activey_ { |val|
		slider.activey_(val);
	}

/*	onClose{controlSpecX.removeDependant(this);controlSpecY.removeDependant(this);}*/

	//doAction { action.value(this) }
}


GUIModule {
	var knob, slider, <>layout;

	*new {

		^super.new.init();
	}

	init {

		knob = Knob();
		slider = QtEZSlider("vol", ControlSpec(0,5), {arg slider; slider.value.postln}, 0);

		layout = VLayout(knob, slider.layout.postln);
	}
}
