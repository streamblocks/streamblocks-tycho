package se.lth.cs.tycho.settings;

import java.util.Optional;

public class OptionalSetting<T> implements Setting<Optional<T>> {
	private final Setting<T> setting;
	private final String none;

	public OptionalSetting(Setting<T> setting, String none) {
		this.setting = setting;
		this.none = none;
		assert !setting.read(none).isPresent();
	}
	@Override
	public String getKey() {
		return setting.getKey();
	}

	@Override
	public String getType() {
		return setting.getType() + " | " + none;
	}

	@Override
	public String getDescription() {
		return setting.getDescription();
	}

	@Override
	public Optional<Optional<T>> read(String text) {
		return text.equals(none) ? Optional.of(Optional.empty()) : setting.read(text).map(Optional::of);
	}

	@Override
	public Optional<T> defaultValue(Configuration configuration) {
		return Optional.empty();
	}
}
