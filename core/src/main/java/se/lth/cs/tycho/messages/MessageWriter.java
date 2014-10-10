package se.lth.cs.tycho.messages;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MessageWriter implements MessageListener {
	private final Writer writer;

	public MessageWriter(Writer writer) {
		this.writer = writer;
	}
	
	public MessageWriter() {
		this(new OutputStreamWriter(System.out));
	}

	@Override
	public void report(Message message) {
		try {
			writer.write(message.toString());
			writer.write('\n');
			writer.flush();
		} catch (IOException e) {
			throw new Error(e);
		}
	}

}
