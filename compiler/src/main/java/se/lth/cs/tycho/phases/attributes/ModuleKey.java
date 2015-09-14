package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.tycho.comp.CompilationTask;

public interface ModuleKey<T> {
	Class<T> getKey();
	T createInstance(CompilationTask unit, AttributeManager manager);
}
