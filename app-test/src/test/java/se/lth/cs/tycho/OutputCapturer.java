package se.lth.cs.tycho;

import se.lth.cs.tycho.reporting.Diagnostic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OutputCapturer {

	private ByteArrayOutputStream newConsole;
	private PrintStream previous;
	private boolean capturing;

	public void start() {
		if (capturing) {
			return;
		}

		previous = System.out;

		newConsole = new ByteArrayOutputStream();
		System.setOut(new PrintStream(newConsole));

		capturing = true;
	}

	public String stop() {
		if (!capturing) {
			return "";
		}

		capturing = false;

		String captured = newConsole.toString();
		newConsole = null;

		System.setOut(previous);
		previous  = null;

		captured = captured.replace(Diagnostic.ANSI_COLOR_RESET, "");
		captured = captured.replace(Diagnostic.ANSI_CYAN, "");
		captured = captured.replace(Diagnostic.ANSI_YELLOW, "");
		captured = captured.replace(Diagnostic.ANSI_BLUE, "");
		captured = captured.replace(Diagnostic.ANSI_RED, "");
		captured = captured.replaceAll("\\s+", " ");

		return captured;
	}
}
