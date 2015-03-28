Timer_Mod : Module_Mod {
	var timerOn, timer, timeString, timeStarted, reset;


	init {
		this.makeWindow("Timer", Rect(500, 500, 290, 300));

		this.initControlsAndSynths(3);

		timerOn = false;
		reset = true;
		timeStarted = 0;

		AppClock.sched(0.0, {arg time;
			if(timerOn,{
				timeString.string = (time-timeStarted).asTimeString.copyRange(3, 9);
			});
			0.1
		});

		controls.add(Button(win, Rect(5, 5, 90, 20))
			.states_([["start", Color.green, Color.black]])
			.action_({arg butt;
				timerOn = true;
				if(reset == true,{
					AppClock.sched(0.0, {arg time; timeStarted = time; nil});
					reset = false;
				});
			})
		);

		this.addAssignButton(0,\onOff, Rect(5, 25, 90, 20));

		controls.add(Button(win, Rect(100, 5, 90, 20))
			.states_([["stop", Color.red, Color.black]])
			.action_({arg butt;
					timerOn = false;
			})
		);

		this.addAssignButton(1,\onOff, Rect(100, 25, 90, 20));

		controls.add(Button(win, Rect(195, 5, 90, 20))
			.states_([["reset", Color.blue, Color.black]])
			.action_({arg butt;
				reset = true;
				AppClock.sched(0.0, {arg time; timeStarted = time; nil});
				timeString.string = "0";
			})
		);

		this.addAssignButton(2,\onOff, Rect(195, 25, 90, 20));

		timeString = StaticText(win, Rect(5, 50, 290, 150));
		timeString.string = "0";
		timeString.font = Font("Helvetica", 72);

	}
}