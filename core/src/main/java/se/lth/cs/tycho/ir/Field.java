package se.lth.cs.tycho.ir;

import java.util.Objects;
import java.util.function.Consumer;

public class Field extends AbstractIRNode {
	private String name;

	public Field(String name) {
		this(null, name);
	}

	private Field(Field original, String name) {
		super(original);
		this.name = name;
	}

	public Field copy(String name) {
		if (Objects.equals(this.name, name)) {
			return this;
		}
		return new Field(this, name);
	}

	public String getName() {
		return name;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}
}
