Leap_Mod {

	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddr, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*addResponders {|address, type, addZ|

		OSCFunc({ |msg|
			MidiOscControl.respond(msg[0], msg[1]);
			if(sendRequest,{
				MidiOscControl.setController(address.asSymbol, type);
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)

			});
		}, address.asSymbol);

		if(addZ,{
			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
				(address.replace("/x", "/z")).asSymbol)
		});
	}

	*start {arg ipIn;
		var address;

		netAddr = NetAddr("127.0.0.1", NN_Synth_ID.next);

		("/usr/local/bin/processing-java --sketch="++("/Users/spluta/Library/Application Support/SuperCollider/Extensions/LiveModularInstrument/Controllers/Leap/").quote+"--run"+netAddr.port+"&").unixCmd;

		ServerQuit.add({NetAddr("127.0.0.1", netAddr.port).sendMsg('/close')});

		if(responders.size!=0,{responders.do{arg item; item.free}});


		this.addResponders("/leapContX", \continuous, false);
		this.addResponders("/leapContY", \continuous, false);
		this.addResponders("/leapContZ", \continuous, false);
		this.addResponders("/leapContSphere", \continuous, false);
		this.addResponders("/leapOnOff", \onOff, false);

	}

	*resetOSCAddr {arg ip;

	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		controllerKey = controllerKey.asString;

		if(controllerKey.contains("leapOnOff"),{
			function = {|val|
				{localControlObject.valueAction_(val)}.defer;
			};
		});
		if(controllerKey.contains("Cont"),{
			function =
				{|val|
					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
				}
		});
		^function
	}
}

