HID.findAvailable; // check which devices are attached
~myhid = HID.openPath("USB_046d_c215_14410000"); // open the Run'N' Drive game controller
~myhid.path

HID.findBy( 1133, 49685 )

s.boot; // boot the server

Ndef( \sinewave, { |freq=500, amp=0.1| SinOsc.ar( freq, 0, amp * 0.2 ) } );
Ndef( \sinewave ).play;

~freqRange = [500, 5000, \exponential].asSpec; // create a frequency range

HIDdef.usage( \freq, { |value| Ndef( \sinewave ).set( \freq, ~freqRange.map( value ) ); }, \X );
HIDdef.usage( \amp, { |value| Ndef( \sinewave ).set( \amp, value ); }, \Y );

HID.findAvailable.do{|item| item.postln}

~myhid.deviceAction= {|...msg| msg.postln}
~myhid.close

HID.openDevices

HID.closeAll

HIDFunc.trace( true );

msg[3]
1 - continuous
9 - button

msg[4]
 buttons - 0-11
 x 12 y 13 button 14 yaw 15 fader 16

HID.action = {|...msg| msg.postln}

HID.action.value

HID.action = {|...msg|
	switch(msg[4],
		1, {},
		2, {},
		3, {},
		4, {},
		5, {},
		6, {},
		12, {},
		13, {},
		14, {},
		15, {},
		16, {}
	)
}

}

Set

