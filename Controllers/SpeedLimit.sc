SpeedLimit {
	var <>action, <>dt;
	var <now, <lastTime, <delta, <scheduled = false, <latestArgs;

	*new { |action, dt = 0.1|
		^super.newCopyArgs(action, dt).init
	}
	init {
		now = lastTime = Main.elapsedTime;
	}

	filterValue { |...args|
		now = Main.elapsedTime;
		delta = now - lastTime;

		if (delta >= dt) {
			action.value(*args);
			lastTime = now;
			^this
		};
		// do it later:
		latestArgs = args;
		if (scheduled.not) {
			scheduled = true;
			SystemClock.sched(dt - delta, {
				action.value(*latestArgs);
				lastTime = Main.elapsedTime;
				scheduled = false;
			})
		}
	}

	value {|...args|
		this.filterValue(*args);
	}
}