+ EZSlider {

zAction {
	}


value_ { arg val;
		value = controlSpec.constrain(val);
		{
			numberView.value = value.round(round);
			sliderView.value = controlSpec.unmap(value);
		}.defer;
	}

}