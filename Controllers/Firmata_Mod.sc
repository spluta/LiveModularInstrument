// Firmata_Mod {
//
// 	classvar responders;
// 	classvar <>sendRequest = false;
// 	classvar firmata, address;
//
// 	*initClass {}
//
// 	*new {
// 		^super.new.init();
// 	}
//
// 	*start {arg addressIn;
// 		address = ipIn;
//
// 		//try {netAddr = NetAddr(ip, 8000)}{netAddr = nil};
// 		try {firmata = FirmataDevice(address.asSymbol)} {firmata=nil};
// 		//the responder to switch out the servers
//
//
// 		responders = List.newClear[0];
//
// 		firmata.analogPinAction_({arg num, val;
// 			[num, val].postln;
// 		})
// 	}
//
// 	*resetOSCAddr {arg ip;
// 		try {netAddr = NetAddr(ip.asString, 8000)}{netAddr = nil};
// 	}
//
// 	*setWCurrentSetup {arg serverName, oscMsg;
// 		var object, setting;
//
// 		try {
// 			if (netAddr!=nil, {
// 				oscMsg = oscMsg.asString.replace("[", "").replace("]", "").replace(" ", "").split($,);
// 				oscMsg = oscMsg.copyRange(1, oscMsg.size-1).asInteger.reverse.add(oscMsg[0]).reverse;
// 				oscMsg.postln;
// 				netAddr.sendBundle(0.0, oscMsg);
// 			});
// 		}{"don't send that shit, bitch!"}
// 	}
//
// 	*getFunctionFromKey {arg module, controllerKey, object;
// 		var nothing, keyShort, localControlObject, function;
//
// 		localControlObject = object;
//
// 		#nothing, keyShort = controllerKey.asString.split;
// 		[nothing, keyShort].postln;
// 		if(keyShort.beginsWith("Button"),{
// 			function = {|val| {val.postln; localControlObject.valueAction_(val)}.defer};
// 		});
//
// 		if(keyShort.beginsWith("PadButton"),{
// 			"adding Switches function".postln;
// 			function = {|val|
// 				if(val == 1,{
// 					{localControlObject.valueAction_(((localControlObject.value.postln+1).wrap(0, localControlObject.states.size-1)))}.defer
// 				})
// 			};
// 		});
//
// 		if(keyShort.beginsWith("Switches"),{
// 			"adding Switches function".postln;
// 			function = {|val|
// 			{localControlObject.valueAction_(((localControlObject.value.postln+1).wrap(0, localControlObject.states.size-1)))}.defer};
// 		});
// 		if(keyShort.beginsWith("Fader"),{
// 			function =  {|xyz, val|
// 				switch(xyz.asSymbol,
// 					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
// 					'z',{localControlObject.zAction.value(val)}
// 				)
// 			};
// 		});
// 		if(keyShort.beginsWith("MultiBall"),{
// 			function = {|xyz, val|
// 				switch(xyz.asSymbol,
// 					'x', {{localControlObject.activex_(val)}.defer},
// 					'y', {{localControlObject.activey_(val)}.defer},
// 					'z',{localControlObject.zAction.value(val)}
// 				)
// 			};
// 		});
// 		if(keyShort.beginsWith("Range"),{
// 			function = {|xyz, val|
// 				switch(xyz.asSymbol,
// 					'x',{{localControlObject.valueAction_(localControlObject.controlSpec.map(val))}.defer},
// 					'z',{localControlObject.zAction.value(val)}
// 				)
// 			};
// 			}
// 		);
// 		^function
// 	}
//
// 	*getMainSwitchControls {arg serverName, controls;
// 		var functions, function, controllerKey;
//
// 		//for Lemur
// 		function = {|val|
//
// 			if(val[0]==1,{
// 				{controls[0].valueAction_(controls[0].value+1)}.defer;
// 				},{
// 					if(val[1]==1,{
// 						{controls[1].valueAction_(controls[1].value+1)}.defer;
// 						},{
// 							if(val[2]==1,{
// 								{controls[2].valueAction_(controls[2].value+1)}.defer;
// 								},{
// 									if(val[3]==1,{
// 										{controls[3].valueAction_(controls[3].value+1)}.defer;
// 									})
// 							})
// 					})
// 			});
// 		};
// 		controllerKey = "/MainSwitch/"++serverName++"/x";  //I added the server to the key from the previous version
//
// 		^[[function, controllerKey]]
// 	}
//
// }
//
