Seaboard_Mod {

	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddr, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		if(responders.size!=0,{responders.do{arg item; item.free}});

		responders = List.newClear[0];

		200.do{arg i;
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); /*if(sendRequest,{MidiOscControl.setController(("/SeaboardNote/"++i.asString).asSymbol, \onOff)});*/ }, ("/SeaboardNote/"++i.asString).asSymbol));

			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); /*if(sendRequest,{MidiOscControl.setController(("/SeaboardXYTrig/"++i.asString).asSymbol, \onOff)}); */}, ("/SeaboardXYTrig/"++i.asString).asSymbol));

			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); /*if(sendRequest,{MidiOscControl.setController(("/SeaboardY/"++i.asString).asSymbol, \continous)}); */}, ("/SeaboardY/"++i.asString).asSymbol));

			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); /*if(sendRequest,{MidiOscControl.setController(("/SeaboardX/"++i.asString).asSymbol, \continuous)}); */}, ("/SeaboardX/"++i.asString).asSymbol));

			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); /*if(sendRequest,{MidiOscControl.setController(("/SeaboardPressure/"++i.asString).asSymbol, \continuous)});*/ }, ("/SeaboardPressure/"++i.asString).asSymbol));
		};
	}

	*setWCurrentSetup {
	}

	*getFunctionFromKey {

		^nil
	}
}

Seaboard {

	classvar <>netAddr, <>ip, <>mode, <>lowNote, <>keyboardWidth, <>triggerBoardDimensions, <>channel = 9, responders, keys, freshKeys, key, freshKey, xyVal;

	classvar <>sendRequest = false, <>sendTypeRequest = false;

	*initClass {
	}

	*new {
		^super.new.init();
	}

	*start {arg modeIn = 0, ipIn = "127.0.0.1", lowNoteIn=48, keyboardWidthIn = 24, triggerBoardDimensionsIn = [6,6], map;

		MIDIIn.disconnectAll;
		MIDIIn.connectAll;

		ip = ipIn;
		mode = modeIn;
		lowNote = lowNoteIn;
		keyboardWidth = keyboardWidthIn;
		triggerBoardDimensions = triggerBoardDimensionsIn;

		netAddr = NetAddr(ip, NetAddr.langPort);

		keys = ();
		freshKeys = ();


		map = IdentityDictionary[ ('8' -> IdentityDictionary[]), ('9' -> IdentityDictionary[]) ];

(60..63).do{|i| map['8'].add(i.asSymbol->(i-12).asSymbol)};
(60..63).do{|i| map['9'].add(i.asSymbol->(i-8).asSymbol)};
(64..67).do{|i| map['8'].add(i.asSymbol->(i-8).asSymbol)};
(64..67).do{|i| map['9'].add(i.asSymbol->(i-4).asSymbol)};
(68..71).do{|i| map['8'].add(i.asSymbol->(i-4).asSymbol)};
(68..71).do{|i| map['9'].add(i.asSymbol->(i).asSymbol)};
(72..75).do{|i| map['8'].add(i.asSymbol->(i).asSymbol)};
(72..75).do{|i| map['9'].add(i.asSymbol->(i+4).asSymbol)};


		responders = List.newClear(0);
		responders.add(
			MIDIFunc.noteOn({arg vel, num, chan, src;
				keys.put((chan.asString++"/"++src.asString).asSymbol, num);
				freshKeys.put((chan.asString++"/"++src.asString).asSymbol, num);

				num = map[chan.asSymbol][num.asSymbol];

				switch(mode,
					0, {netAddr.sendMsg("/SeaboardNote/"++num.asString, 1, vel)},
					1,  {netAddr.sendMsg("/SeaboardNote", num.asString, 1, vel)},
				);
		}, nil, nil));


		responders.add(
			MIDIFunc.noteOff({arg vel, num, chan, src;
				keys.put((chan.asString++"/"++src.asString).asSymbol, nil);
				freshKeys.put((chan.asString++"/"++src.asString).asSymbol, nil);

				num = map[chan.asSymbol][num.asSymbol];

				switch(mode,`
					0, {netAddr.sendMsg("/SeaboardNote/"++num.asString, 0, vel)},
					1,  {netAddr.sendMsg("/SeaboardNote", num.asString, 0, vel)}
				);
		}, nil, channel));

		responders.add(
			MIDIFunc.cc({arg val, num, chan, src;
				key = keys[(chan.asString++"/"++src.asString).asSymbol];
				freshKey = freshKeys[(chan.asString++"/"++src.asString).asSymbol];
				if(freshKey!=nil,{
					if(((freshKey+1-lowNote)>=0),{
					xyVal = this.getXY(freshKey, val);
					switch(mode,
						0, {netAddr.sendMsg("/SeaboardXYTrig/"++xyVal)},
						1, {netAddr.sendMsg("/SeaboardXYTrig", xyVal)}
					);
					freshKeys.put((chan.asString++"/"++src.asString).asSymbol, nil);
				})});
				if(key!=nil,{
					switch(mode,
						0, {netAddr.sendMsg("/SeaboardY/"++key.asString, val/127)},
						1, {netAddr.sendMsg("/SeaboardY", key.asString, val/127)}
					);
				})
		}, nil, channel));

		responders.add(
			MIDIFunc.bend({ arg val, chan, src;

				key = keys[(chan.asString++"/"++src.asString).asSymbol];
				if(key!=nil,{
					switch(mode,
							0, {netAddr.sendMsg("/SeaboardX/"++key.asString, val/(2**14))},
							1, {netAddr.sendMsg("/SeaboardX", key.asString, val/(2**14))}
					);
				})
		}, channel));



		responders.add(
			MIDIFunc.polytouch({ arg val, key, chan, src;
				//[val, key, chan].postln;
				key = map[chan.asSymbol][key.asSymbol];
				//key.postln;
				if(key!=nil,{
					switch(mode,
								0, {netAddr.sendMsg(("/SeaboardPressure/"++key.asString), val/127)},
								1, {netAddr.sendMsg("/SeaboardPressure", key.asString, val/127)}
					)
				})
		}, nil, nil));

/*		responders.add(
			MIDIFunc.touch({ arg val, chan, num, src;
				9.postln;
				key = keys[(chan.asString++"/"++src.asString).asSymbol];
				[key, val].
		ln;
				if(key!=nil,{
					switch(mode,
								0, {netAddr.sendMsg("/SeaboardPressure/"++key.asString, val/127)},
								1, {netAddr.sendMsg("/SeaboardPressure", key.asString, val/127)}
					)
				})
		}, 8));*/



	}

	*getXY {|freshKey, val|
			^(((freshKey+1-lowNote)/(keyboardWidth/triggerBoardDimensions[0])).floor
							+(val.linlin(0,127,-0.49,triggerBoardDimensions[1]+0.49).round*triggerBoardDimensions[0])).asString
	}

	*panic {
		keys = ();
		127.do{arg num; netAddr.sendMsg("/SeaboardNote/"++num.asString, 0, 127)};
	}

	*setWCurrentSetup {
	}

	*getFunctionFromKey {

		^nil
	}
}
