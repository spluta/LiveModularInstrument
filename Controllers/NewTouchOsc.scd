n = NetAddr("127.0.0.1", 7000)

a = [0,0,0];

OSCFunc({|msg|
	3.do{|i|
		if (a[i]!=msg[i+1]){
			a[i] = msg[i+1];
			i.post; a[i].postln;
		};
	};

}, \xy1, nil, 7000)

OSCFunc({|msg| msg.postln}, \xy1, nil, 7000)

Lemur_Mod

OSCFunc({|msg|
	3.do{|i|
		if (a[i]!=msg[i+1]){
			a[i] = msg[i+1];
			i.post; a[i].postln;
		};
	};

},"", nil, 7000)

OSCFunc.trace

thisProcess.openUDPPort(7000)

(
var doit = {|portMsg, item, count|
	var goMsg;

	if(count==0){
		if(portMsg.asString.contains("radio")){
			goMsg = [(portMsg++"/"++item).asSymbol, 1];
		}{
			goMsg = [(portMsg++"/x").asSymbol, item];
		}
	}{

		if(count==1){
			if(portMsg.asString.contains("xy")){
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
	goMsg.postln;
};

a = ();

f = { |msg, time, addr, port|
	var portMsg = ('/'++port.asSymbol++msg[0]).asSymbol;
	var data = msg.copyRange(1, msg.size-1);

	msg.postln;
	if(msg[0] != '/status.reply') {
		if (a[portMsg]==nil){
			a.put(portMsg, data);
			data.do{|item, i| doit.value(portMsg, item, i)};
		}{
			data.do{|item, i|
				if (a[portMsg][i]!=item){
					a[portMsg][i] = item;
					(i+" ").post; a[portMsg][i].postln;
					doit.value(portMsg, item, i);
				};
			};
		}
	}
};
thisProcess.addOSCRecvFunc(f);
);

// stop posting.
thisProcess.removeOSCRecvFunc(f)