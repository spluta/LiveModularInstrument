Lemur_Mod {

	classvar responders;
	classvar <>sendRequest = false;
	classvar netAddr;

	// addr can be used for identifying multiple Mantas
	*new {
		^super.new.init();
	}

	init {

		try {netAddr = NetAddr("Bernard.local", 8000)}{netAddr = nil};


		//the responder to switch out the servers
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/ServerSwitch/x").asSymbol);

		//these are hardcoded right now, but maybe they shouldn't be
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/MainSwitch/lmi0/x").asSymbol);
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/MainSwitch/lmi1/x").asSymbol);
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/MainSwitch/lmi2/x").asSymbol);
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/MainSwitch/lmi3/x").asSymbol);

		responders = List.newClear[0];

		50.do{arg i;
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController(("/Fader"++i.asString).asSymbol, \continuous)}); }, ("/Fader"++i.asString++"/x").asSymbol));
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController( ("/Fader"++i.asString).asSymbol, \continuous)}); }, ("/Fader"++i.asString++"/z").asSymbol));
		};

		40.do{arg i;
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController( ("/Button"++i.asString++"/x").asSymbol, \onOff)}); }, ("/Button"++i.asString++"/x").asSymbol));


			//The Switches uses the array of values as part of the path to identify where the message is coming from
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg.asSymbol, 1); if(sendRequest,{MidiOscControl.setController(msg.asSymbol, \onOff)}); }, ("/Switches"++i.asString++"/x").asSymbol));
		};


		40.do{arg i;
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)}); }, ("/MultiBall"++i.asString++"/x").asSymbol));
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)}); }, ("/MultiBall"++i.asString++"/y").asSymbol));
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)}); }, ("/MultiBall"++i.asString++"/z").asSymbol));
		};

		40.do{arg i;
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1],msg[2]]); if(sendRequest,{MidiOscControl.setController(("/Range"++i.asString).asSymbol, \range)}); }, ("/Range"++i.asString++"/x").asSymbol));
			responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); if(sendRequest,{MidiOscControl.setController(("/Range"++i.asString).asSymbol, \range)}); }, ("/Range"++i.asString++"/z").asSymbol));
		};
	}

	*resetOSCAddr {try {netAddr = NetAddr("Bernard.local", 8000)}{netAddr = nil};}

	*setWCurrentSetup {arg serverName, setupNum;
		var temp;

		temp = [0,0,0,0,0];
		temp.put(setupNum, 1);
		if (netAddr!=nil, {
			netAddr.sendBundle(0.0, temp.reverse.add("/MainSwitch/"++serverName++"/x").reverse)
		});
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		#nothing, keyShort = controllerKey.asString.split;
		[nothing, keyShort].postln;
		if(keyShort.beginsWith("Button"),{
			function = {|val| {val.postln; localControlObject.valueAction_(val)}.defer};
		});
		if(keyShort.beginsWith("Switches"),{
			"adding Switches function".postln;
			function = {|val|
				{localControlObject.valueAction_(((localControlObject.value.postln+1).wrap(0, localControlObject.states.size-1)))}.defer};
		});
		if(keyShort.beginsWith("Fader"),{
			function =  {|xyz, val|
				switch(xyz.asSymbol,
					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
		});
		if(keyShort.beginsWith("MultiBall"),{
			function = {|xyz, val|
				switch(xyz.asSymbol,
					'x', {{localControlObject.activex_(val)}.defer},
					'y', {{localControlObject.activey_(val)}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
		});
		if(keyShort.beginsWith("Range"),{
			function = {|xyz, val|
				switch(xyz.asSymbol,
					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
					'z',{localControlObject.zAction.value(val)}
				)
			};
			}
		);
		^function
	}

	*getMainSwitchControls {arg serverName, controls;
		var functions, function, controllerKey;

		//for Lemur
		function = {|val|

			if(val[0]==1,{
				{controls[0].valueAction_(controls[0].value+1)}.defer;
				},{
					if(val[1]==1,{
						{controls[1].valueAction_(controls[1].value+1)}.defer;
						},{
							if(val[2]==1,{
								{controls[2].valueAction_(controls[2].value+1)}.defer;
								},{
									if(val[3]==1,{
										{controls[3].valueAction_(controls[3].value+1)}.defer;
									})
							})
					})
			});
		};
		controllerKey = "/MainSwitch/"++serverName++"/x";  //I added the server to the key from the previous version

		^[[function, controllerKey]]
	}

}

