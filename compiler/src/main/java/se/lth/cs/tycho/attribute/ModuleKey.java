package se.lth.cs.tycho.attribute;

import se.lth.cs.tycho.compiler.CompilationTask;

public interface ModuleKey<T> {
	T createInstance(CompilationTask task);
}
