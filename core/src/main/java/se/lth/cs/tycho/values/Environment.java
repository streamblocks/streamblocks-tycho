package se.lth.cs.tycho.values;

import java.util.HashMap;

public class Environment<V> {
	private final HashMap<String, Box<V>> values;

	public Environment() {
		this(new HashMap<>());
	}

	private Environment(HashMap<String, Box<V>> values) {
		this.values = values;
	}

	public void declare(String name, V value) {
		values.put(name, new Box<>(value));
	}

	public void set(String name, V value) {
		values.get(name).set(value);
	}

	public V get(String name) {
		return values.get(name).get();
	}

	public Environment<V> frame() {
		return new Environment<>(new HashMap<>(this.values));
	}

	private static final class Box<C> {
		private C content;

		public Box(C content) {
			this.content = content;
		}

		public C get() {
			return content;
		}

		public void set(C content) {
			this.content = content;
		}
	}
}
