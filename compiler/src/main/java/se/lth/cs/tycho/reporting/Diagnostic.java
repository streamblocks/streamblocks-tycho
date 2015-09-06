package se.lth.cs.tycho.reporting;

import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class Diagnostic {
	private final Kind kind;
	private final String message;
	private final SourceUnit sourceUnit;
	private final int line;
	private final int column;

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit, IRNode node) {
		this.kind = kind;
		this.message = message;
		this.sourceUnit = sourceUnit;
		if (node != null) {
			this.line = node.getLineNumber();
			this.column = node.getColumnNumber();
		} else {
			this.line = -1;
			this.column = -1;
		}
	}

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit) {
		this(kind, message, sourceUnit, null);
	}

	public Diagnostic(Kind kind, String message) {
		this(kind, message, null, null);
	}

	public String getMessage() {
		return message;
	}

	public Kind getKind() {
		return kind;
	}

	public String generateMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append(kind);
		if (sourceUnit != null) {
			builder.append(" in ");
			builder.append(sourceUnit.getLocation());
			if (line >= 0) {
				builder.append(" on line ");
				builder.append(line);
			}
		}
		builder.append("\n");
		builder.append(message);
		builder.append("\n");
		if (sourceUnit != null && line > 0) {
			try (InputStream stream = sourceUnit.getInputStream()) {
				Optional<String> lineText = new BufferedReader(new InputStreamReader(stream)).lines()
						.skip(line - 1)
						.findFirst();
				if (lineText.isPresent()) {
					builder.append(lineText.get());
					builder.append('\n');
					if (column > 0 && column <= lineText.get().length()) {
						String marker = lineText.get().replaceAll("[^\t]", " ");
						char[] chars = marker.toCharArray();
						chars[column - 1] = '^';
						builder.append(chars);
						builder.append('\n');
					}
				}
			} catch (IOException e) {
			}
		}
		return builder.toString();
	}

	public enum Kind {
		ERROR(1), WARNING(2), INFO(3);
		private final int level;
		Kind(int level) {
			this.level = level;
		}
		public int getLevel() {
			return level;
		}
	}
}
