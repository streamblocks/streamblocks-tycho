package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;

public class Context {
	private final Configuration configuration;
	private final Loader loader;
	private final Reporter reporter;
	private final UniqueNumbers uniqueNumbers;

	public Context(Configuration configuration, Loader loader, Reporter reporter) {
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

}
