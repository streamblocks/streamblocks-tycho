package net.opendf.util.io;

import java.io.InputStream;

import net.opendf.util.logging.Logging;

public class ClassLoaderStreamLocator implements StreamLocator {

	public SourceStream getAsStream(String name) {
		InputStream is = loader.getResourceAsStream(name);
		SourceStream ss = (is == null) ? null : new SourceStream(is, "using classloader:" + loader.toString() ); //FIXME
		Logging.dbg().info("ClassLoaderStreamLocator: Locating '" + name + "' " + (is == null ? "failed" : "succeeded") + ".");
		return ss;
	}
	
	public ClassLoaderStreamLocator(ClassLoader loader) {
		this.loader = loader;
	}
	
	private ClassLoader loader;

}

