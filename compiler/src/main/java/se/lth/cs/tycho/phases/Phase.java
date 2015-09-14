package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;

public interface Phase {
	default String getName() {
		String className = this.getClass().getSimpleName();
		if (className.endsWith("Phase")) {
			return className.substring(0, className.length() - "Phase".length());
		} else {
			return className;
		}
	}
	String getDescription();
	CompilationTask execute(CompilationTask task, Context context);
	default List<Setting<?>> getPhaseSettings() {
		return Collections.emptyList();
	}
}
