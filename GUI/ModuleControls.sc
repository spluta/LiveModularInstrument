// ModuleControl_Mod {
// 	var <>location, <>element, <>oscMsg, <>netAddr, <>assignButton;
//
// 	*new {arg location;
// 		^super.new.location_(location).init;
// 	}
//
// 	init {}
//
// 	addButton {|states, action, addAssign, module|
// 		element = Button().states_(states)
// 		.action_{|butt|
// 			action.value(butt);
// 			if(oscMsg!=nil, {
// 				Lemur_Mod.sendOSC(oscMsg);
// 			})
// 		}
// 		.maxHeight_(15);
// 		if(addAssign==true, {
//
// 		});
// 	}
//
// 	addRanger {|label, spec, action, initVal, initAction, orientation|
// 		element = QtEZRanger(label, spec, action, initVal, initAction, orientation);
// 	}
//
// 	makeAssignButton {
// 		assignButton = AssignButton.new().instantAction_{|butt|
// 			if(butt.value==1,{
//
// 				waitForSetNum = num;
// 				MidiOscControl.requestInstantAssign(module, element, \onOff, module.group.server);
// 				},{
// 					MidiOscControl.clearInstantAssign;
// 					MidiOscControl.clearController(module.group.server, oscMsg); //send a message to clear the OSC data from the MidiOscControl
// 					oscMsg = nil;
// 			})
// 		}
// 	}
// }
//
// ModuleControls_Mod {
// 	var <>module, <>size, <>controls, currentSize=0;
//
// 	*new {arg module;
// 		^super.new.module_(module).init;
// 	}
//
// 	init {
// 		controls = List.newClear(size);
// 	}
//
// 	addButton {|states, action, addAssign|
// 		controls.add(ModuleControl_Mod(currentSize).addButton(states, action, addAssign, module));
// 		currentSize = currentSize+1;
// 	}
//
// 	addRanger {|label, spec, action, initVal, initAction, orientation|
// 		controls.add(ModuleControl_Mod(currentSize).addRanger(label, spec, action, initVal, initAction, orientation));
// 	}
//
// 	setOscMsg {|elementNum, oscMsg|
// 		controls[elementNum].oscMsg = oscMsg;
// 	}
// }