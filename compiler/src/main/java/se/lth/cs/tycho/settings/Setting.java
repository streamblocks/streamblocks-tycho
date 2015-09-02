package se.lth.cs.tycho.settings;

import java.util.Optional;

public interface Setting<T> {
	String getKey();
	String getType();
	String getDescription();
	Optional<T> read(String string);
	T defaultValue();
}
