package se.lth.cs.tycho.backend.c.util;

public class Joiner {
	private final String before;
	private final String between;
	private final String after;

	public Joiner(String before, String between, String after) {
		this.before = before;
		this.between = between;
		this.after = after;
	}

	public Joiner(String between) {
		this("", between, "");
	}

	public String join(Object... elements) {
		StringBuilder sb = new StringBuilder();
		sb.append(before);
		boolean first = true;
		for (Object e : elements) {
			if (first) {
				first = false;
			} else {
				sb.append(between);
			}
			sb.append(e);
		}
		sb.append(after);
		return sb.toString();
	}

	public <E> String join(Iterable<E> elements) {
		StringBuilder sb = new StringBuilder();
		sb.append(before);
		boolean first = true;
		for (E e : elements) {
			if (first) {
				first = false;
			} else {
				sb.append(between);
			}
			sb.append(e);
		}
		sb.append(after);
		return sb.toString();
	}
}