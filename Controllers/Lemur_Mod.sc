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

		//"sendOsc ".post; oscMsg.post; val.postln;
		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(oscMsg, val);
			});
		}
	}

	*sendSwitchOSC{|oscMsg|
		oscMsg = oscMsg.copyRange(2, oscMsg.size-2);
		oscMsg = oscMsg.split($,);
		oscMsg = oscMsg.collect{|item, i| if(i==0, {item.asString}, {item.asFloat})};
		netAddrs.do{arg item;
			if(item!=nil,{
				item.sendMsg(*oscMsg);
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

	*start {arg ipIn;
		var address;

		if(responders.size!=0,{responders.do{arg item; item.free}});

		25.do{arg i;

			//FADERS
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

		25.do{arg i;

			//FADERS
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

			//FADERS
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
					//{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer;
					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
					this.sendOSC(controllerKey++"x", val);
				},
				{|val| localControlObject.zAction.value(val)}]
		});
		if(controllerKey.contains("MultiBall"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-2);
			function = [
				{|val|
					{localControlObject.activex_(val)}.defer;
					this.sendOSC(controllerKey++"x", val);
				},
				{|val|
					{localControlObject.activey_(val)}.defer;
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
// 				//FADERS
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
