package se.lth.cs.tycho.messages;

public class Message {
	public enum Kind {
		ERROR, WARNING, NOTE;
	}

	private final String text;
	private final Kind kind;

	public Message(String text, Kind kind) {
		this.text = text;
		this.kind = kind;
	}

	public String getText() {
		return text;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return kind + ": " + text;
	}

	public static Message error(String text) {
		return new Message(text, Kind.ERROR);
	}

	public static Message warning(String text) {
		return new Message(text, Kind.WARNING);
	}

	public static Message note(String text) {
		return new Message(text, Kind.NOTE);
	}
}
