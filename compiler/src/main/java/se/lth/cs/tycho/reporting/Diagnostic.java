package se.lth.cs.tycho.reporting;

import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Diagnostic {
	private final Kind kind;
	private final String message;
	private final SourceUnit sourceUnit;
	private final IRNode node;

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit, IRNode node) {
		this.kind = kind;
		this.message = message;
		this.sourceUnit = sourceUnit;
		this.node = node;
	}

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit) {
		this(kind, message, sourceUnit, null);
	}

	public Diagnostic(Kind kind, String message) {
		this(kind, message, null, null);
	}

	public Kind getKind() {
		return kind;
	}

	private boolean hasPosition() {
		return node != null && fromLine() > 0 && fromCol() > 0 && toLine() > 0 && toCol() > 0;
	}

	private int fromLine() {
		return node == null ? 0 : node.getFromLineNumber();
	}

	private int fromCol() {
		return node == null ? 0 : node.getFromColumnNumber();
	}

	private int toLine() {
		return node == null ? 0 : node.getToLineNumber();
	}

	private int toCol() {
		return node == null ? 0 : node.getToColumnNumber();
	}

	public String generateMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(kind);
		if (sourceUnit != null) {
			builder.append(" in ");
			builder.append(sourceUnit.getLocation());
			if (hasPosition()) {
				builder.append(" on line ");
				builder.append(fromLine());
			}
		}
		builder.append("\n");
		builder.append(message);
		builder.append("\n");
		if (hasPosition() && !sourceUnit.isSynthetic()) {
			try (InputStream stream = sourceUnit.getInputStream()) {
				List<String> lines = new BufferedReader(new InputStreamReader(stream)).lines()
						.skip(fromLine()-1)
						.limit(toLine() - fromLine() + 1)
						.collect(Collectors.toList());
				int l = 0;
				for (String line : lines) {
					String converted = tabsToSpaces(line);
					builder.append(converted);
					builder.append('\n');
					int w = Math.max(converted.length(), toCol());
					int start = l == 0 ? fromCol() : 1;
					int end = l == lines.size() - 1 ? toCol() : w;
					char[] marker = createMarker(w, start, end);
					builder.append(marker);
					builder.append('\n');
					l = l + 1;
				}
			} catch (IOException e) {
			}
		}
		return builder.toString();
	}

	private char[] createMarker(int width, int start, int end) {
		char[] marker = new char[width];
		Arrays.fill(marker, 0, start - 1, ' ');
		Arrays.fill(marker, start-1, end, '^');
		Arrays.fill(marker, end, width, ' ');
		return marker;
	}

	private String tabsToSpaces(String input) {
		StringBuilder output = new StringBuilder();
		int column = 0;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '\t') {
				int nextTab = (column & ~0b111) + 8;
				int spaces = nextTab - column;
				while (spaces > 0) {
					output.append(' ');
					column++;
					spaces--;
				}
			} else {
				output.append(c);
				column++;
			}
		}
		return output.toString();
	}

	public enum Kind {
		ERROR, WARNING, INFO
	}
}
