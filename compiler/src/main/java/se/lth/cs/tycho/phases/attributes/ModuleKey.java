package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.tycho.comp.CompilationUnit;

public interface ModuleKey<T> {
	Class<T> getKey();
	T createInstance(CompilationUnit unit, AttributeManager manager);
}
