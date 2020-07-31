package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.platform.Platform;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;

public class Context {
    private final Configuration configuration;
    private final Loader loader;
    private final Reporter reporter;
    private final UniqueNumbers uniqueNumbers;
    private final Platform platform;

    public Context(Platform platform, Configuration configuration, Loader loader, Reporter reporter) {
        this.platform = platform;
        this.reporter = reporter;
        this.configuration = configuration;
        this.loader = loader;
        this.uniqueNumbers = new UniqueNumbers();

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Loader getLoader() {
        return loader;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public UniqueNumbers getUniqueNumbers() {
        return uniqueNumbers;
    }

    public Platform getPlatform() { return platform; }
}
