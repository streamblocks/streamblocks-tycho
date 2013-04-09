package net.opendf.ir.common;

import net.opendf.ir.AbstractIRNode;

public class Field extends AbstractIRNode {
	private String name;
	private int offset;

	public Field(String name) {
		this(name, -1);
	}

	public Field(String name, int offset) {
		this.name = name;
		this.offset = offset;
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
