package se.lth.cs.tycho.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SettingsManager {

	private final Map<String, Setting<?>> settings;

	private SettingsManager(Map<String, Setting<?>> settings) {
		this.settings = settings;
	}

	public boolean containsKey(String key) {
		return settings.containsKey(key);
	}

	public Setting<?> get(String key) {
		return settings.get(key);
	}

	public static Builder builder() {
		return new Builder();
	}

	public List<Setting<?>> getAllSettings() {
		return new ArrayList<>(settings.values());
	}

	public static class Builder {
		private Map<String, Setting<?>> settings;

		public Builder() {
			settings = new TreeMap<>();
		}

		public Builder add(Setting<?> setting) {
			if (settings == null) {
				throw new IllegalStateException("Already built");
			}
			if (settings.containsKey(setting.getKey())) {
				throw new IllegalStateException("The key \"" + setting.getKey() + "\" has already been added.");
			}
			settings.put(setting.getKey(), setting);
			return this;
		}

		public Builder addAll(List<Setting<?>> settings) {
			settings.forEach(this::add);
			return this;
		}

		public SettingsManager build() {
			final Map<String, Setting<?>> settings = this.settings;
			this.settings = null;
			return new SettingsManager(settings);
		}
	}
}
