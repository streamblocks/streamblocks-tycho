package se.lth.cs.tycho.settings;

import java.util.Optional;

public abstract class StringSetting implements Setting<String> {
	@Override
	public Optional<String> read(String string) {
		return Optional.of(string);
	}

	@Override
	public String getType() {
		return "string";
	}
}
