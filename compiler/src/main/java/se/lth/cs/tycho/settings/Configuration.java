package se.lth.cs.tycho.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Configuration {
	private final SettingsManager manager;
	private final Map<String, Object> configuration;

	public Configuration(SettingsManager manager, Map<String, Object> configuration) {
		this.manager = manager;
		this.configuration = configuration;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Setting<T> setting) {
		if (manager.get(setting.getKey()) != setting) {
			throw new IllegalArgumentException("Unknown setting");
		} else if (configuration.containsKey(setting.getKey())) {
			return (T) configuration.get(setting.getKey());
		} else {
			T result = setting.defaultValue();
			configuration.put(setting.getKey(), result);
			return result;
		}
	}

	public Object get(String key) {
		return get(manager.get(key));
	}

	public static Builder builder(SettingsManager manager) {
		return new Builder(manager);
	}

	public static class Builder {
		private final SettingsManager manager;
		private final Map<String, Object> configuration;
		public Builder(SettingsManager manager) {
			this.manager = manager;
			this.configuration = new HashMap<>();
		}

		public Builder set(String key, String value) throws UnknownKeyException, ReadException {
			if (!manager.containsKey(key)) {
				throw new UnknownKeyException(key);
			}
			Setting<?> setting = manager.get(key);
			Optional<?> readValue = setting.read(value);
			if (!readValue.isPresent()) {
				throw new ReadException(key, value);
			}
			configuration.put(key, readValue.get());
			return this;
		}

		public Configuration build() {
			return new Configuration(manager, configuration);
		}

		public static class UnknownKeyException extends Exception {
			private final String key;
			public UnknownKeyException(String key) {
				this.key = key;
			}

			public String getKey() {
				return key;
			}
		}

		public static class ReadException extends Exception {
			private final String key;
			private final String value;
			public ReadException(String key, String value) {
				this.key = key;
				this.value = value;
			}

			public String getKey() {
				return key;
			}

			public String getValue() {
				return value;
			}
		}
	}
}
