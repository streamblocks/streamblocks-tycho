package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.AbstractIRNode;

public class Field extends AbstractIRNode {
	private String name;
	private int offset;

	public Field(String name) {
		this(null, name, -1);
	}

	public Field(String name, int offset) {
		this(null, name, offset);
		assert offset >= 0;
	}

	private Field(Field original, String name, int offset) {
		super(original);
		this.name = name;
		this.offset = offset;
	}

	public Field copy(String name) {
		return copy(name, -1);
	}

	public Field copy(String name, int offset) {
		if (Objects.equals(this.name, name) && this.offset == offset) {
			return this;
		}
		return new Field(this, name, offset);
	}

	public String getName() {
		return name;
	}

	public boolean hasOffset() {
		return offset >= 0;
	}

	public int getOffset() {
		return offset;
	}

}
