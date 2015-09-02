package se.lth.cs.tycho.reporting;

import se.lth.cs.tycho.comp.SourceUnit;

public class Diagnostic {
	private final Kind kind;
	private final String message;
	private final SourceUnit sourceUnit;
	private final int fromLine;
	private final int fromCol;
	private final int toLine;
	private final int toCol;

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit, int fromLine, int fromCol, int toLine, int toCol) {
		this.kind = kind;
		this.message = message;
		this.sourceUnit = sourceUnit;
		this.fromLine = fromLine;
		this.fromCol = fromCol;
		this.toLine = toLine;
		this.toCol = toCol;
	}

	public Diagnostic(Kind kind, String message, SourceUnit sourceUnit) {
		this(kind, message, sourceUnit, -1, -1, -1, -1);
	}

	public Diagnostic(Kind kind, String message) {
		this(kind, message, null, -1, -1, -1, -1);
	}

	public String getMessage() {
		return message;
	}

	public Kind getKind() {
		return kind;
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
