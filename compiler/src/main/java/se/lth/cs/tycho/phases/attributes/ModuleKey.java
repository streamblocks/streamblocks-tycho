package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.tycho.comp.CompilationTask;

public interface ModuleKey<T> {
	T createInstance(CompilationTask task);
}
