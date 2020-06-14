package se.lth.cs.tycho.meta.interp.value;

import java.util.Objects;

public class ValueChar implements Value {

	private final char character;

	public ValueChar(char character) {
		this.character = character;
	}

	public char character() {
		return character;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ValueChar valueChar = (ValueChar) o;
		return character == valueChar.character;
	}

	@Override
	public int hashCode() {
		return Objects.hash(character);
	}

	@Override
	public String toString() {
		return "'" + character() + "'";
	}
}
