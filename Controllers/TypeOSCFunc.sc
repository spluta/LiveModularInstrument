TypeOSCFunc_Mod {

	classvar responders;
	classvar <>sendRequest = false;
	classvar <>netAddr, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		if(responders.size!=0,{responders.do{arg item; item.free}});

		responders = ();
	}

	*addResponder {arg path;
		responders.put(path.asSymbol, OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]) }, path.asSymbol));
	}

	*removeResponder {arg path;
		responders.removeAt(path.asSymbol);
	}

	*setWCurrentSetup {arg serverName, oscMsg;
		var object, setting;

		/*try {
		if (netAddr!=nil, {
			oscMsg = oscMsg.asString.replace("[", "").replace("]", "").replace(" ", "").split($,);
			oscMsg = oscMsg.copyRange(1, oscMsg.size-1).asInteger.reverse.add(oscMsg[0]).reverse;
				//netAddr.postln;
				//oscMsg.postln;
			netAddr.sendBundle(0.0, oscMsg);
		});
		}{"don't send that shit, bitch!"}*/
	}

	*getFunctionFromKey {

		^nil
	}

}