package se.lth.cs.tycho.settings;

import se.lth.cs.tycho.ir.util.Lists;

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

    public boolean isDefined(Setting<?> setting) {
        if (manager.get(setting.getKey()) != setting) {
            throw new IllegalArgumentException("Unknown setting");
        } else {
            return configuration.containsKey(setting.getKey());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Setting<T> setting) {
        if (manager.get(setting.getKey()) != setting) {
            throw new IllegalArgumentException("Unknown setting");
        } else if (configuration.containsKey(setting.getKey())) {
            return (T) configuration.get(setting.getKey());
        } else {
            T result = setting.defaultValue(this);
            configuration.put(setting.getKey(), result);
            return result;
        }
    }

    public <T> void set(Setting<T> setting, Object result) {
        if (manager.get(setting.getKey()) != setting) {
            throw new IllegalArgumentException("Unknown setting");
        } else if (configuration.containsKey(setting.getKey())) {
            configuration.replace(setting.getKey(), result);
        } else {
            configuration.put(setting.getKey(), result);
        }
    }

    public SettingsManager getManager(){
        return manager;
    }

    public Configuration withSettingsManager(SettingsManager manager) {
        if (Lists.sameElements(this.manager.getAllSettings(), manager.getAllSettings())) {
            return this;
        } else {
            return new Configuration(manager, configuration);
        }
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

        public <T> Builder set(Setting<T> key, T value) throws UnknownKeyException {
            if (!manager.containsKey(key.getKey()) || manager.get(key.getKey()) != key) {
                throw new UnknownKeyException(key.getKey());
            }
            configuration.put(key.getKey(), value);
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
