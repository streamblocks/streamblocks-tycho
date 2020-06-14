package se.lth.cs.tycho.ir;

import java.util.function.Consumer;

public class Nth extends AbstractIRNode {

	private final Integer number;

	public Nth(Integer number) {
		this(null, number);
	}

	public Nth(IRNode original, Integer number) {
		super(original);
		this.number = number;
	}

	public Integer getNumber() {
		return number;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
