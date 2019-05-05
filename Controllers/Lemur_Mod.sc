Lemur_Mod {

	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddrs, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*setPorts {|ports|
		var netAddr;

		netAddrs = List.newClear(0);
		ports.do{arg port;
			try {netAddr = NetAddr("127.0.0.1", port)}{netAddr = nil};
			netAddrs.add(netAddr);
		}
	}

	*sendOSCBundle {|oscMsg|
		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendBundle(0.0, oscMsg);
			});
		}
	}

	*sendOSC {|oscMsg, val|

		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val);
			});
		}
	}

	*sendOSCxy {|oscMsg, val|
		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val[1], val[0]);  //x and y are reversed for TouchOSC
			});
		}
	}



	*start {arg ipIn;
		if(responders.size!=0,{responders.do{arg item; item.free}});

		//the responder to switch out the servers
		OSCFunc({ |msg| MidiOscControl.respond(msg[0], [msg[1], msg[2], msg[3], msg[4]]) }, ("/ServerSwitch/x").asSymbol);

		responders = List.newClear[0];

		(1..200).do{arg i;

			//FADERS

			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController(("/Fader"++i.asString).asSymbol, \continuous)
				});
				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject("/Fader"++i.asString++"/x")

				});
			}, ("/Fader"++i.asString++"/x").asSymbol));


			responders.add(
				OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]);
					if(sendRequest,{MidiOscControl.setController( ("/Fader"++i.asString).asSymbol, \continuous)});
				}, ("/Fader"++i.asString++"/z").asSymbol)
			);

			//PADS AND BUTTONS

			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController( ("/Button"++i.asString++"/x").asSymbol, \onOff)
				});
				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject("/Button"++i.asString++"/x")
				});
			}, ("/Button"++i.asString++"/x").asSymbol));


			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController( ("/PadButton"++i.asString++"/x").asSymbol, \increment)
				});
				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject("/PadButton"++i.asString++"/x")
				});

			}, ("/PadButton"++i.asString++"/x").asSymbol));

			//SWITCHES
			//The Switches uses the array of values as part of the path to identify where the message is coming from

			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg.asSymbol, 1);
				if(sendRequest,{
					MidiOscControl.setController(msg.asSymbol, \onOff)
				});
				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject(msg.asSymbol)
				});
			}, ("/Switches"++i.asString++"/x").asSymbol));

			//MULTIBALLZ

			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)
				});
			}, ("/MultiBall"++i.asString++"/x").asSymbol));
			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)
				});

				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject(("/MultiBall"++i.asString).asSymbol)
				});

			}, ("/MultiBall"++i.asString++"/y").asSymbol));
			responders.add(OSCFunc({ |msg|
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController(("/MultiBall"++i.asString).asSymbol, \slider2D)
				});

			}, ("/MultiBall"++i.asString++"/z").asSymbol));
		};
	}

	*resetOSCAddr {arg ip;

	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		#nothing, keyShort = controllerKey.asString.split;
		if(keyShort.beginsWith("Button"),{
			function = {|val| {localControlObject.valueAction_(val)}.defer};
		});

		if(keyShort.beginsWith("PadButton"),{
			function = {|val|
				if(val == 1,{
					{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer
				})
			};
		});

		if(keyShort.beginsWith("Switches"),{
			function = {|val|
				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer};
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

TouchOSC_Mod : Lemur_Mod {

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
				(address++"/z").asSymbol)
		});
	}

	*addMultToggleResponders {|address, type, addZ|
		OSCFunc({ |msg|
			if(msg[1]>0,{
				MidiOscControl.respond(msg[0], msg[1]);
				if(sendRequest,{
					MidiOscControl.setController(address.asSymbol, type);
				});
				if(sendTypeRequest,{
					MidiOscControl.setInstantTypeObject(address)

				});
			});
		}, address.asSymbol);

		if(addZ,{
			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
				(address++"/z").asSymbol)
		});
	}

	*addXYResponders {|address, type, addZ|

		OSCFunc({ |msg|
			MidiOscControl.respond(msg[0], [msg[1], msg[2]]);
			if(sendRequest,{
				MidiOscControl.setController(address.asSymbol, type)
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)

			});
		}, address.asSymbol);

		if(addZ,{
			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
				(address++"/z").asSymbol)
		});
	}

	*start {arg ipIn;
		var address;

		if(responders.size!=0,{responders.do{arg item; item.free}});

		//the responder to switch out the servers
		(1..8).do{arg i;
			OSCFunc({ |...msg|
				MidiOscControl.respond((msg[0][0].asString++"/"++msg[2].port.asString).asSymbol, 1);
				if(sendRequest,{
					MidiOscControl.setController((msg[0][0].asString++"/"++msg[2].port.asString).asSymbol, \onOff);
				});
			}, ("/"++i.asString).asSymbol);
		};




		["n"].addAll((1..8)).do{arg i;

			40.do{arg i2;

				//FADERS

				this.addResponders("/"++i.asString++"/fader"++i2.asString, \continuous, true);
				this.addResponders("/"++i.asString++"/toggle"++i2.asString, \onOff, false);

				(1..2).do{arg row;
					(1..10).do{arg column;
						this.addMultToggleResponders("/"++i.asString++"/multitoggle"++i2.asString++"/"++row.asString++"/"++column.asString, \onOff, false);
					}
				};

				this.addXYResponders("/"++i.asString++"/xy"++i2.asString, \slider2D, true);
			}
		}
	}

	*resetOSCAddr {arg ip;

	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		controllerKey = controllerKey.asString;

		if(controllerKey.contains("/800"),{
			function = {|val|
				{localControlObject.valueAction_(1)}.defer
			};
		});

		if(controllerKey.contains("toggle"),{
			function = {|val|
				{localControlObject.valueAction_(val)}.defer;
				this.sendOSC(controllerKey, val);
			};
		});

		if(controllerKey.contains("multitoggle"),{
			function = {|val|
				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer;
				this.sendOSC(controllerKey, val);
			};
		});
		if(controllerKey.contains("fader"),{
			function =  {|val|
				{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer;
				this.sendOSC(controllerKey, val);
			};
		});
		if(controllerKey.contains("xy"),{
			function = [{|val|
				{
					localControlObject.activex_(val[0]);
					localControlObject.activey_(val[1]);
					this.sendOSCxy(controllerKey, val);
			}.defer}, {|val| localControlObject.zAction.value(val)}]
		});
		^function
	}
}


