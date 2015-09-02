package se.lth.cs.tycho.settings;

import java.util.Optional;

public abstract class OnOffSetting implements Setting<Boolean> {
	@Override
	public String getType() {
		return "on | off";
	}

	@Override
	public Optional<Boolean> read(String string) {
		switch(string) {
			case "on": return Optional.of(true);
			case "off": return Optional.of(false);
			default: return Optional.empty();
		}
	}
}
