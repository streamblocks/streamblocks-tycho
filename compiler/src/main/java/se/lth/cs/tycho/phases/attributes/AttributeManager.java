package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.tycho.comp.CompilationTask;

import java.util.IdentityHashMap;
import java.util.WeakHashMap;

public class AttributeManager {
	private final WeakHashMap<CompilationTask, IdentityHashMap<ModuleKey<?>, Object>> caches;

	public AttributeManager() {
		this.caches = new WeakHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttributeModule(ModuleKey<T> key, CompilationTask unit) {
		return (T) caches
				.computeIfAbsent(unit, u -> new IdentityHashMap<>())
				.computeIfAbsent(key, k -> k.createInstance(unit, this));
	}
}
