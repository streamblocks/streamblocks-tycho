package se.lth.cs.tycho.settings;

import java.util.Optional;

public abstract class IntegerSetting implements Setting<Integer> {
	@Override
	public String getType() {
		return "integer";
	}

	@Override
	public Optional<Integer> read(String string) {
		try {
			return Optional.of(Integer.parseInt(string));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

}
