package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.phases.attributes.AttributeManager;
import se.lth.cs.tycho.reporting.Reporter;
import se.lth.cs.tycho.settings.Configuration;

public class Context {
	private final Configuration configuration;
	private final Loader loader;
	private final Reporter reporter;
	private final AttributeManager attributeManager;

	public Context(Configuration configuration, Loader loader, Reporter reporter) {
		this.reporter = reporter;
		this.configuration = configuration;
		this.loader = loader;
		this.attributeManager = new AttributeManager();
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

	public AttributeManager getAttributeManager() {
		return attributeManager;
	}

}
