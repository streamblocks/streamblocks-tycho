package net.opendf.util.io;

import java.io.InputStream;

public class SourceStream {
	private InputStream inputStream;
	private String filename;
	
	public SourceStream(InputStream inputStream, String filename) {
		this.inputStream = inputStream;
		this.filename = filename;		
	}
	
	public InputStream  getInputStream() {
		return inputStream;
	}
	
	public String getFilename(){
		return filename;
	}
}
