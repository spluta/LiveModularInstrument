
Joystick_Mod.setPaths(["logitech-extreme-3d-pro"]).start
Joystick_Mod.start

Joystick_Mod.start

Joystick_Mod.swProTransform[1204.asSymbol]

Quarks.gui

HID.findAvailable; // check which devices are attached
HID.findAvailable.do{|item| item.postln}

~myhid = HID.openPath("Bluetooth_057e_2009_5cb87c6c"); // open the Run'N' Drive game controller
~myhid = HID.openPath("Bluetooth_057e_2009_5cb89c65");
~myhid = HID.openPath("USB_057e_2009_14500000")
HID.closeAll

~myhid.info.productName

~mHb = HID.openPath("USB_046d_c215_2100000");

HID.findBy( 1133, 49685 )

s.boot; // boot the server

Ndef( \sinewave, { |freq=500, amp=0.1| SinOsc.ar( freq, 0, amp * 0.2 ) } );
Ndef( \sinewave ).play;

~freqRange = [500, 5000, \exponential].asSpec; // create a frequency range

HIDdef.usage( \freq, { |value| Ndef( \sinewave ).set( \freq, ~freqRange.map( value ) ); }, \X );
HIDdef.usage( \amp, { |value| Ndef( \sinewave ).set( \amp, value ); }, \Y );



~mHb.deviceAction= {|...msg| msg.postln}

~myhid.elements[0].action = {|...msg| msg.postln}

1/7

57 - id 1200

~myhid.elements[1200].action = {|...msg| msg.postln}

~myhid.elements.do{arg item; item.postln}

HID.openDevices

HID.closeAll

HIDFunc.trace( false );

msg[3]
1 - continuous
9 - button

msg[4]
 buttons - 0-11
 x 12 y 13 button 14 yaw 15 fader 16

HID.action = {|...msg|
	if(msg[2]<48){msg.postln}}

HID.action = {}

HID.action.value

(
w = Window();

~sl = List.fill(32, {Slider()});

w.layout_(
	HLayout(*~sl));

w.front
)

c = List.fill(4, {|i| 2**(3-i)})

~nums = Set();

~counter = 0;

[ 77, 73, 76, 65, 75, 66, 74, 71, 67, 70, 69, 72 ]

[ 5, 48, 7, 3, 57, 49, 8, 6, 4, 2, 51, 52, 1 ].sort

-> [ 1, 2, 3, 4, 5, 6, 7, 8, 48, 49, 51, 52, 57 ]

65 - Buttons and some fucking crazy counter
66 - Buttons and some fucking crazy counter
67 - the counter
69 - the main 6 buttons
70 - 4 small buttons
71 - left buttons
72 - joystick 1 insane numbers
73 - insane left joystick
74 - joystick 1 up and down
75 - insane joystick 2 right left
76 - insane joystick 2 right left
77 - joystick 2 up down - 19-201

a = [0];b = [0];
(HID.action = {
	|...msg|

	//~nums.add(msg[2]);
	//msg.postln;
/*	if(msg[4]==72){

		//msg[1].postln;
		a = msg[1].asBinaryDigits
	};
	if(msg[4]==73){
		//msg[1].postln;
		b = msg[1].asBinaryDigits;
		c = b.addAll(a);
		(c.reverse.copyRange(2, 11)*d).sum.postln;
		//a.postln;
	};*/

	if(msg[2]==52){msg[1].postln; msg.postln};

	//[48/*,49,51,52*/].do{|item, i|
	if(msg[2]==1){
		msg[1]/*.linlin(7000, 52500, 0, 1)*/.postln;
		//msg[1].linlin(12000, 59200, 1, 0).postln;
		//msg[1].linlin(6250, 52850, 0, 1).postln;
		//msg[1].linlin(14000, 60800, 1, 0).postln;
		//msg[1].asBinaryDigits.postln//.do{|item2, i2| {~sl[i*8+i2].value = item2}.defer};
		//msg[1].asBinaryDigits.copyRange(0,3).postln;
			//(msg[1].asBinaryDigits.copyRange(0,3)*c).sum.postln;
	}
	//};

		/*msg[1].postln;
		msg[1].asBinaryDigits.reverse.postln*/
	//};
	//if(~counter%100==0){msg.postln};
	//~counter = ~counter+1;
	//~nums.add(msg[4].postln);
};)

~sl[i*8+i2].value = item

HID.action = {}

d = List.fill(10, {|i| 2**i})


a = List.fill(8, 0);
b = List.fill(8, {|i|2**(i)});

(
HID.action = {|...msg|
	//msg.postln;
	switch(msg[3],
		1, {
			switch(msg[2],
				//msg[2].postln;
				48, {
					//sends button states
					/*32768 - all off
					32769 - y
					32770 - x
					32771 - xy
					32772 - b
					32773 - yb
					32774 - xb
					32775 - xyb
					32776 - a
					32777 - ay
					32778 - ax
					32779 - ayx
					32780 - ab
					32781 - aby
					32782 - abx
					32783 - abxy

					32896
					32832
					36864 - start

					*/

					"button ".post; msg[1].postln

				},
				57, {
					//many up and down messages
					//msg[0].postln;
				},

				49, {
					//some kind of slope message?

				//msg.postln

				},
				50, {
					//left control up down!
					 //msg.postln

				},
				52, {
					msg.postln

				},
				53, {
					//
					//msg[0].postln
					//{~sl.value=msg[0]}.defer
				},
				{msg.postln}
			)
			//msg.postln

		},
		9, {
			//msg.postln
			//1-8 is some kind of counter

			if((msg[2]>=1)&&(msg[2]<=8)){a.put(msg[2]-1, msg[0])};
			//a.postln;

			//these are the bits for the right joystick
			//if((msg[2]>=15)&&(msg[2]<=18)){a.put(msg[2]-15, msg[0])};
			//a.postln;

			//(a.reverse*b).sum.postln;

			//15-18 are digital bits counting from 1 to 13 for the right joystick



			//{~sl[msg[2]].value = msg[0]}.defer;
/*			switch(msg[2],
				15, {msg.postln},
				16, {msg.postln}
			)*/

		},

/*		3, {},
		4, {},
		5, {},
		6, {},
		12, {},
		13, {},
		14, {},
		15, {},
		16, {}*/
		{msg.postln}
	);
})

}

Set

