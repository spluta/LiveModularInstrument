Lemur_Mod {

	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddrs, ip, <>whichMode=0;

	*initClass {}

	*new {
		^super.new.init;
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
				if(oscMsg.asString.containsStringAt(0, "/Container/")){
					item.sendMsg(oscMsg.asString.replace("/Container/", "/Container2/"), val)
				};
				//{if(oscMsg.containsStringAt(0, "/Container2/")){item.sendMsg(oscMsg.replace("/Container2/", "/Container/"), val)}};
			});
		}
	}

	*sendSwitchOSC{|oscMsg|
		var oscMsgB;
		oscMsg = oscMsg.copyRange(2, oscMsg.size-2);
		oscMsg = oscMsg.split($,);
		oscMsg = oscMsg.collect{|item, i| if(i==0, {item.asString}, {item.asFloat})};
		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(*oscMsg);
				item.sendMsg(*oscMsg.collect{|item, i| if(i==0){item.replace("/Container/", "/Container2/")}{item}});
			});
		}
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

	*addRespondersSingleIpad {|address, type, addZ|
		//"/Container/Fader/x".replace("/Container/", "/Container2/");
		var address2;

		try {
		address2 = address.asString.replace("/Container/", "/Container2/");

		OSCFunc({ |msg|
			MidiOscControl.respond(msg[0], msg[1]);
			if(sendRequest,{
				MidiOscControl.setController(address.asSymbol, type);
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)

			});
		}, address.asSymbol);

		OSCFunc({ |msg|
			try{msg.put(0, msg[0].asString.replace("/Container2/", "/Container/"))};
			MidiOscControl.respond(msg[0], msg[1]);
			if(sendRequest,{
				MidiOscControl.setController(address.asSymbol, type);
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(address)

			});
		}, address2.asSymbol);

		if(addZ,{
			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
				(address.asString.replace("/x", "/z")).asSymbol);
			OSCFunc({ |msg|
				try{msg.put(0, msg[0].asString.replace("/Container2/", "/Container/"))};
				MidiOscControl.respond(msg[0], msg[1])},
				(address2.asString.replace("/x", "/z")).asSymbol);
		});
		}{"nope ".postln; address.postln;}
	}

	*addSwitches {|address|
		responders.add(OSCFunc({ |msg|
			MidiOscControl.respond(msg.asSymbol, 1);
			if(sendRequest,{
				MidiOscControl.setController(msg.asSymbol, \onOff)
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(msg.asSymbol)
			});
		}, address.asSymbol));
	}

	*addSwitchesSingleIPad {|address|
		var address2;

		address2 = address.asString.replace("/Container/", "/Container2/");

/*		OSCFunc({ |msg|
			MidiOscControl.respond(msg.asSymbol, 1);
			if(sendRequest,{
				MidiOscControl.setController(msg.asSymbol, \onOff)
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(msg.asSymbol)
			});
		}, address.asSymbol);*/

		OSCFunc({ |msg|
			MidiOscControl.respond(msg.asSymbol, 1);
			if(sendRequest,{
				MidiOscControl.setController(msg.asSymbol, \onOff);
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(msg.asSymbol)

			});
		}, address.asSymbol);

		OSCFunc({ |msg|
			try{msg = msg.asString.replace("/Container2/", "/Container/")};
			MidiOscControl.respond(msg.asSymbol, 1);
			if(sendRequest,{
				MidiOscControl.setController(msg.asSymbol, \onOff);
			});
			if(sendTypeRequest,{
				MidiOscControl.setInstantTypeObject(msg.asSymbol)

			});
		}, address2.asSymbol);
	}

	*setMode {|num|
		switch(num,
			0, {
				"1 iPad Mode".postln;

				50.do{arg i;

					//CONTROLS on main page
					if(i==0,{
						this.addResponders("/Fader/x", \continuous, true);
						this.addResponders("/CustomButton/x", \onOff, false);
						this.addResponders("/MultiBall/x", \slider2D, true);
						this.addResponders("/MultiBall/y", \slider2D, false);
						this.addSwitches("/Switches/x");
					});
					this.addResponders("/Fader"++i.asString++"/x", \continuous, true);
					this.addResponders("/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Switches"++i.asString++"/x");
				};

				200.do{arg i;
					//CONTROLS from the main tabbed control
					if(i==0,{
						this.addRespondersSingleIpad("/Container/Fader/x", \continuous, true);
						this.addRespondersSingleIpad("/Container/CustomButton/x", \onOff, false);
						this.addRespondersSingleIpad("/Container/MultiBall/x", \slider2D, true);
						this.addRespondersSingleIpad("/Container/MultiBall/y", \slider2D, false);
						this.addSwitchesSingleIPad("/Container/Switches/x");
					});
					this.addRespondersSingleIpad("/Container/Fader"++i.asString++"/x", \continuous, true);
					this.addRespondersSingleIpad("/Container/CustomButton"++i.asString++"/x", \onOff, false);
					this.addRespondersSingleIpad("/Container/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addRespondersSingleIpad("/Container/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitchesSingleIPad("/Container/Switches"++i.asString++"/x");

				};

				100.do{arg i;

					//CONTROLS for NNSynth
					if(i==0,{
						this.addResponders("/Container3/Container/Fader/x", \continuous, true);
						/*this.addResponders("/Container3/Container/CustomButton/x", \onOff, false);
						this.addResponders("/Container3/Container/MultiBall/x", \slider2D, true);
						this.addResponders("/Container3/Container/MultiBall/y", \slider2D, false);
						this.addSwitches("/Container3/Container/Switches/x");*/
					});
					this.addResponders("/Container3/Container/Fader"++i.asString++"/x", \continuous, true);
					/*this.addResponders("/Container3/Container/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/Container3/Container/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/Container3/Container/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Container3/Container/Switches"++i.asString++"/x");*/

				};

				50.do{arg i;

					//general CONTROLS on right panel
					if(i==0,{
						this.addResponders("/Container3/Fader/x", \continuous, true);
						this.addResponders("/Container3/CustomButton/x", \onOff, false);
						this.addResponders("/Container3/MultiBall/x", \slider2D, true);
						this.addResponders("/Container3/MultiBall/y", \slider2D, false);
						this.addSwitches("/Container3/Switches/x");
					});
					this.addResponders("/Container3/Fader"++i.asString++"/x", \continuous, true);
					this.addResponders("/Container3/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/Container3/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/Container3/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Container3/Switches"++i.asString++"/x");

				};



			},

			1,{
				50.do{arg i;

					//CONTROLS on main page
					if(i==0,{
						this.addResponders("/Fader/x", \continuous, true);
						this.addResponders("/CustomButton/x", \onOff, false);
						this.addResponders("/MultiBall/x", \slider2D, true);
						this.addResponders("/MultiBall/y", \slider2D, false);
						this.addSwitches("/Switches/x");
					});
					this.addResponders("/Fader"++i.asString++"/x", \continuous, true);
					this.addResponders("/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Switches"++i.asString++"/x");
				};

				100.do{arg i;

					//CONTROLS for NNSynth
					if(i==0,{
						this.addResponders("/Container2/Container2/Fader/x", \continuous, true);
						/*this.addResponders("/Container2/Container2/CustomButton/x", \onOff, false);
						this.addResponders("/Container2/Container2/MultiBall/x", \slider2D, true);
						this.addResponders("/Container2/Container2/MultiBall/y", \slider2D, false);
						this.addSwitches("/Container2/Container2/Switches/x");*/
					});
					this.addResponders("/Container2/Container2/Fader"++i.asString++"/x", \continuous, true);
					/*this.addResponders("/Container2/Container2/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/Container2/Container2/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/Container2/Container2/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Container2/Container2/Switches"++i.asString++"/x");*/

				};

				50.do{arg i;

					//general CONTROLS on right panel
					if(i==0,{
						this.addResponders("/Container2/Fader/x", \continuous, true);
						this.addResponders("/Container2/CustomButton/x", \onOff, false);
						this.addResponders("/Container2/MultiBall/x", \slider2D, true);
						this.addResponders("/Container2/MultiBall/y", \slider2D, false);
						this.addSwitches("/Container2/Switches/x");
					});
					this.addResponders("/Container2/Fader"++i.asString++"/x", \continuous, true);
					this.addResponders("/Container2/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/Container2/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/Container2/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Container2/Switches"++i.asString++"/x");

				};

				200.do{arg i;

					//CONTROLS from the main tabbed control
					if(i==0,{
						this.addResponders("/Container/Fader/x", \continuous, true);
						this.addResponders("/Container/CustomButton/x", \onOff, false);
						this.addResponders("/Container/MultiBall/x", \slider2D, true);
						this.addResponders("/Container/MultiBall/y", \slider2D, false);
						this.addSwitches("/Container/Switches/x");
					});
					this.addResponders("/Container/Fader"++i.asString++"/x", \continuous, true);
					this.addResponders("/Container/CustomButton"++i.asString++"/x", \onOff, false);
					this.addResponders("/Container/MultiBall"++i.asString++"/x", \slider2D, true);
					this.addResponders("/Container/MultiBall"++i.asString++"/y", \slider2D, true);
					this.addSwitches("/Container/Switches"++i.asString++"/x");

				};

			}
		)
	}



	*start {arg ipIn;
		var address;

		if(responders.size!=0,{responders.do{arg item; item.free}});

		this.setMode(whichMode);

	}

	*resetOSCAddr {arg ip;

	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function;

		localControlObject = object;

		controllerKey = controllerKey.asString;

		if(controllerKey.contains("CustomButton"),{
			function = {|val|
				{localControlObject.valueAction_(val)}.defer;
				this.sendOSC(controllerKey, val);
			};
		});

		if(controllerKey.contains("Switches"),{
			function = {|val|
				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer;
				this.sendSwitchOSC(controllerKey);
			};
		});
		if(controllerKey.contains("Fader"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-2);
			function = [
				{|val|
					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
					this.sendOSC(controllerKey++"x", val);
				},
				{|val| localControlObject.zAction.value(val)}]
		});
		if(controllerKey.contains("MultiBall"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-2);
			function = [
				{|val|
					localControlObject.activex_(val);
					this.sendOSC(controllerKey++"x", val);
				},
				{|val|
					localControlObject.activey_(val);
					this.sendOSC(controllerKey++"y", val);
				},
				{|val| localControlObject.zAction.value(val)}]
		});
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

// TouchOSC_Mod : Lemur_Mod {
//
// 	*sendOSCxy {|oscMsg, val|
// 		netAddrs.do{arg item;
// 			if(item!=nil,{
// 				item.sendMsg(oscMsg, val[1], val[0]);  //x and y are reversed for TouchOSC
// 			});
// 		}
// 	}
//
// 	*addResponders {|address, type, addZ|
//
// 		OSCFunc({ |msg|
// 			MidiOscControl.respond(msg[0], msg[1]);
// 			if(sendRequest,{
// 				MidiOscControl.setController(address.asSymbol, type);
// 			});
// 			if(sendTypeRequest,{
// 				MidiOscControl.setInstantTypeObject(address)
//
// 			});
// 		}, address.asSymbol);
//
// 		if(addZ,{
// 			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
// 			(address++"/z").asSymbol)
// 		});
// 	}
//
// 	*addMultToggleResponders {|address, type, addZ|
// 		OSCFunc({ |msg|
// 			if(msg[1]>0,{
// 				MidiOscControl.respond(msg[0], msg[1]);
// 				if(sendRequest,{
// 					MidiOscControl.setController(address.asSymbol, type);
// 				});
// 				if(sendTypeRequest,{
// 					MidiOscControl.setInstantTypeObject(address)
//
// 				});
// 			});
// 		}, address.asSymbol);
//
// 		if(addZ,{
// 			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
// 			(address++"/z").asSymbol)
// 		});
// 	}
//
// 	*addXYResponders {|address, type, addZ|
//
// 		OSCFunc({ |msg|
// 			MidiOscControl.respond(msg[0], [msg[1], msg[2]]);
// 			if(sendRequest,{
// 				MidiOscControl.setController(address.asSymbol, type)
// 			});
// 			if(sendTypeRequest,{
// 				MidiOscControl.setInstantTypeObject(address)
//
// 			});
// 		}, address.asSymbol);
//
// 		if(addZ,{
// 			OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1])},
// 			(address++"/z").asSymbol)
// 		});
// 	}
//
// 	*start {arg ipIn;
// 		var address;
//
// 		if(responders.size!=0,{responders.do{arg item; item.free}});
//
// 		//the responder to switch out the servers
// 		(1..8).do{arg i;
// 			OSCFunc({ |...msg|
// 				MidiOscControl.respond((msg[0][0].asString++"/"++msg[2].port.asString).asSymbol, 1);
// 				if(sendRequest,{
// 					MidiOscControl.setController((msg[0][0].asString++"/"++msg[2].port.asString).asSymbol, \onOff);
// 				});
// 			}, ("/"++i.asString).asSymbol);
// 		};
//
//
//
//
// 		["n"].addAll((1..8)).do{arg i;
//
// 			40.do{arg i2;
//
// 				//CONTROLS
//
// 				this.addResponders("/"++i.asString++"/fader"++i2.asString, \continuous, true);
// 				this.addResponders("/"++i.asString++"/toggle"++i2.asString, \onOff, false);
//
// 				(1..2).do{arg row;
// 					(1..10).do{arg column;
// 						this.addMultToggleResponders("/"++i.asString++"/multitoggle"++i2.asString++"/"++row.asString++"/"++column.asString, \onOff, false);
// 					}
// 				};
//
// 				this.addXYResponders("/"++i.asString++"/xy"++i2.asString, \slider2D, true);
// 			}
// 		}
// 	}
//
// 	*resetOSCAddr {arg ip;
//
// 	}
//
// 	*getFunctionFromKey {arg module, controllerKey, object;
// 		var nothing, keyShort, localControlObject, function;
//
// 		localControlObject = object;
//
// 		controllerKey = controllerKey.asString;
//
// 		if(controllerKey.contains("/800"),{
// 			function = {|val|
// 				{localControlObject.valueAction_(1)}.defer
// 			};
// 		});
//
// 		if(controllerKey.contains("toggle"),{
// 			function = {|val|
// 				{localControlObject.valueAction_(val)}.defer;
// 				this.sendOSC(controllerKey, val);
// 			};
// 		});
//
// 		if(controllerKey.contains("multitoggle"),{
// 			function = {|val|
// 				{localControlObject.valueAction_(((localControlObject.value+1).wrap(0, localControlObject.states.size-1)))}.defer;
// 				this.sendOSC(controllerKey, val);
// 			};
// 		});
// 		if(controllerKey.contains("fader"),{
// 			function =  {|val|
// 				{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer;
// 				this.sendOSC(controllerKey, val);
// 			};
// 		});
// 		if(controllerKey.contains("xy"),{
// 			function = [{|val|
// 				{
// 					localControlObject.activex_(val[0]);
// 					localControlObject.activey_(val[1]);
// 					this.sendOSCxy(controllerKey, val);
// 			}.defer}, {|val| localControlObject.zAction.value(val)}]
// 		});
// 		^function
// 	}
// }
//
//
