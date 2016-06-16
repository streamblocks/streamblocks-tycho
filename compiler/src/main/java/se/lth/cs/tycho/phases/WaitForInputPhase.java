package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class WaitForInputPhase implements Phase {
	private static final Setting<Boolean> waitForInput = new OnOffSetting() {
		@Override public String getKey() { return "wait-for-input"; }
		@Override public String getDescription() { return "Enables a wait for input."; }
		@Override public Boolean defaultValue(Configuration configuration) { return false; }
	};

	@Override
	public String getDescription() {
		return "Proceeds after input on stdin";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (context.getConfiguration().get(waitForInput)) {
			System.out.print("Press return to continue.");
			new Scanner(System.in).nextLine();
		}
		return task;
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(waitForInput);
	}
}
