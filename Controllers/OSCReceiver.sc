OSCReceiver_Mod {
	classvar <>sendServerSwitcherRequest = false;
	classvar responders;
	classvar <>sendRequest = false, <>sendTypeRequest = false;
	classvar <>netAddrs, ip, <>whichMode=0;
	classvar <>oscDict, <>oscFunc, <>sendMirrorMsgs;
	classvar <>inPorts, <>outPorts;

	*initClass {
		oscDict = ();
		inPorts=[6000, 6001]; outPorts=[7000, 7001]; sendMirrorMsgs=[true, false];
	}

	*new {
		^super.new.init;
	}

	*setPorts {
		var netAddr;

		netAddrs = List.newClear(0);
/*		inPorts.do{|port|
			try {
				postf("open port %", port);
				thisProcess.openUDPPort(port);
			} {"bad UDP port".postln}
		};*/
		outPorts.do{arg port;
			try {netAddr = NetAddr("127.0.0.1", port)}{netAddr = nil};
			netAddrs.add(netAddr);
		};
	}

	*sendOSC {|controllerKey, val|
		var portKeyList, localPort, localKey;

		//expects a message like /6001/radio3 or /6001/xy3 and the value

		if(controllerKey!=""){
			try {
				portKeyList = controllerKey.asString.split;
				localPort = portKeyList[1].asInteger;

				netAddrs.do{arg item, i;

					if(item.port==(localPort+1000)){
						localKey = "/";
						portKeyList.copyRange(2, portKeyList.size).do{|item|
							localKey=localKey++item++"/"};
						localKey.pop;
						item.sendMsg(localKey, *val);
					}

				}
			}
		}
	}

	*makeAndRun {|portMsg, item, count|
		var goMsg, portKeyList;

		portMsg = portMsg.asString;

		if(count==0){
			if(portMsg.contains("radio")||portMsg.contains("pager")){
				goMsg = [(portMsg++"/"++item).asSymbol, 1];
			}{
				goMsg = [(portMsg++"/x").asSymbol, item];
			}
		}{

			if(count==1){
				if(portMsg.contains("xy")){
					goMsg = [(portMsg++"/y").asSymbol, item];
				}{
					goMsg = [(portMsg++"/z").asSymbol, item];
				}
			}{
				if(count==2){
					goMsg = [(portMsg++"/z").asSymbol, item];
				}
			}
		};

		MidiOscControl.respond(goMsg[0], goMsg[1]);

		if(sendRequest,{
			var type;
			if(portMsg.contains("radio")||portMsg.contains("button")||portMsg.contains("pager")){
				type = \onOff;
			};
			if(portMsg.contains("fader")){
				type = \continuous;
			};
			if(portMsg.contains("xy")){
				type = \slider2D;
			};
			MidiOscControl.setController(goMsg[0], type);
		});
		if(sendTypeRequest,{
			MidiOscControl.setInstantTypeObject(goMsg[0])
		});
	}

	*start {
		this.setPorts;
		oscFunc = { |msg, time, addr, port|
			var portMsg = ('/'++port.asSymbol++msg[0]).asSymbol;
			var data = msg.copyRange(1, msg.size-1);

			if(inPorts.indexOf(port)!=nil){
				if(msg[0] != '/status.reply') {
					if (oscDict[portMsg]==nil){
						oscDict.put(portMsg, data);
						data.do{|item, i| this.makeAndRun(portMsg, item, i)};
					}{
						data.do{|item, i|
							if (oscDict[portMsg][i]!=item){
								oscDict[portMsg][i] = item;
								this.makeAndRun(portMsg, item, i);
							};
						};
					}
				}
			}
		};
		thisProcess.addOSCRecvFunc(oscFunc);
	}

	*pause {

	}

	*stop {
		thisProcess.removeOSCRecvFunc(oscFunc)
	}

	*getFunctionFromKey {arg module, controllerKey, object;
		var nothing, keyShort, localControlObject, function, speedLimit, speedLimitB;

		//[module, controllerKey, object].postln;

		controllerKey = controllerKey.asString;

		localControlObject = object;

		if(controllerKey.contains("button"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-3);
			function = {|val|
				{localControlObject.valueAction_(val)}.defer;
				this.sendOSC(controllerKey, val);
			};
		});

		if(controllerKey.contains("radio"),{
			var whichRadio = controllerKey.copyRange(controllerKey.findAll("/").last+1, controllerKey.size-1).asInteger;
			controllerKey = controllerKey.copyRange(0, controllerKey.size-3);
			function = {|val|
				{localControlObject.valueAction_(1)}.defer;
				this.sendOSC(controllerKey, whichRadio);
			};
		});

		if(controllerKey.contains("pager"),{
			//var whichPage = controllerKey.copyRange(controllerKey.findAll("/").last+1, controllerKey.size-1).asInteger;
			controllerKey = controllerKey.copyRange(0, controllerKey.size-3);
			function = {|val|
				{localControlObject.valueAction_(1)}.defer;
				//this.sendOSC(controllerKey, whichRadio);
			};
		});

		if(controllerKey.contains("fader"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-3);
			speedLimit = SpeedLimit({|val| this.sendOSC(controllerKey, val)}, 0.05);
			function = [
				{|val|
					localControlObject.valueAction_(localControlObject.controlSpec.map(val));
					speedLimit.value(val);
				},
				{|val| localControlObject.zAction.value(val)}]
		});

		if(controllerKey.contains("xy"),{
			controllerKey = controllerKey.copyRange(0, controllerKey.size-3);
			speedLimit = SpeedLimit({this.sendOSC(controllerKey, oscDict[controllerKey.asSymbol])}, 0.05);
			function = [
				{|val|
					localControlObject.activex_(val);
					speedLimit.value;
				},
				{|val|
					localControlObject.activey_(val);
					speedLimit.value;
				},
				{|val| localControlObject.zAction.value(val)}]
		});
		^function
	}

}