AssignOSCFunc_Mod {

	classvar responders;
	classvar <>sendRequest = false;
	classvar <>netAddr, ip;

	*initClass {}

	*new {
		^super.new.init();
	}

	*start {
		if(responders.size!=0,{responders.do{arg item; item.free}});

		responders = List.newClear[0];
	}

	*addResponder {arg path;
		responders.add(OSCFunc({ |msg| MidiOscControl.respond(msg[0], msg[1]); }, path.asSymbol));
	}
}