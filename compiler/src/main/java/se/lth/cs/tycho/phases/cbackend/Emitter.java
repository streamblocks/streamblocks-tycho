package se.lth.cs.tycho.phases.cbackend;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class Emitter {

	private int indentation;
	private final PrintWriter writer;

	public Emitter(Path file) throws IOException {
		writer = new PrintWriter(Files.newBufferedWriter(file));
	}

	public void close() {
		writer.close();
	}

	public void increaseIndentation() {
		indentation++;
	}

	public void decreaseIndentation() {
		indentation--;
	}

	public void emit(String format, Object... values) {
		if (writer == null) {
			throw new IllegalStateException("No output file is currently open.");
		}
		if (!format.isEmpty()) {
			int indentation = this.indentation;
			while (indentation > 0) {
				writer.print('\t');
				indentation--;
			}
			writer.printf(format, values);
		}
		writer.println();
	}

	public void emitRawLine(CharSequence text) {
		if (writer == null) {
			throw new IllegalStateException("No output file is currently open.");
		}
		writer.println(text);
	}
}
