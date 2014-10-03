package net.opendf.ir.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class QID {
	private final List<String> parts;

	private QID(List<String> parts) {
		this.parts = parts;
	}

	private QID part(int from, int to) {
		int count = getNameCount();
		if (from < 0 || from >= count || to < from || to > count) {
			return null;
		} else {
			return new QID(parts.subList(from, to));
		}
	}

	public QID getFirst() {
		return part(0, 1);
	}

	public QID getLast() {
		int count = getNameCount();
		return part(count - 1, count);
	}

	public QID getButFirst() {
		return part(1, getNameCount());
	}

	public QID getButLast() {
		return part(0, getNameCount() - 1);
	}

	public QID getName(int index) {
		QID result = part(index, index + 1);
		if (result == null) {
			throw new IndexOutOfBoundsException();
		} else {
			return result;
		}
	}

	public int getNameCount() {
		return parts.size();
	}

	public List<QID> parts() {
		return parts.stream().map(QID::of).collect(Collectors.toList());
	}
	
	public boolean isPrefixOf(QID that) {
		if (this.getNameCount() == 0) {
			return true;
		} else if (this.getFirst().equals(that.getFirst())) {
			return this.getButFirst().isPrefixOf(that.getButFirst());
		} else {
			return false;
		}
	}

	public QID dot(QID name) {
		List<String> result = new ArrayList<>();
		result.addAll(parts);
		result.addAll(name.parts);
		return new QID(result);
	}

	public static QID parse(String name) {
		return new QID(Arrays.asList(name.split("\\.")));
	}

	public static QID of(String... names) {
		if (Arrays.stream(names).anyMatch((s) -> s.contains("."))) {
			throw new IllegalArgumentException(
					"Names may not contain the '.' character");
		} else {
			return new QID(new ArrayList<>(Arrays.asList(names)));
		}
	}

	public static QID empty() {
		return new QID(Collections.emptyList());
	}

	@Override
	public String toString() {
		return String.join(".", parts);
	}

	@Override
	public int hashCode() {
		return parts.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof QID) {
			return ((QID) o).parts.equals(parts);
		} else if (o == null) {
			throw new NullPointerException();
		} else {
			return false;
		}
	}
}