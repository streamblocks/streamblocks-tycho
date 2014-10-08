package se.lth.cs.tycho.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import se.lth.cs.tycho.util.logging.Logging;

public class DirectoryStreamLocator implements StreamLocator {

	public SourceStream getAsStream(String name) {
		File f = new File(dirpath + name);
		try {
			InputStream is = new FileInputStream(f);
			SourceStream ss = (is == null) ? null : new SourceStream(is, dirpath + name);
			
			Logging.dbg().info("DirectoryStreamLocator: " + dirpath + "::" + name + " -- " + ((is == null) ? "failed" : "succeeded") + ".");
			return ss;
		}
		catch (Exception e) {
			Logging.dbg().info("DirectoryStreamLocator: " + dirpath + "::" + name + " -- failed.");
			return null;
		}
	}
	
	public DirectoryStreamLocator(String dirpath) {
		this.dirpath = dirpath + File.separator;
	}

	private String dirpath;
	
	public String toString ()
	{
	    return super.toString() + "[" + this.dirpath + "]";
	}
}
