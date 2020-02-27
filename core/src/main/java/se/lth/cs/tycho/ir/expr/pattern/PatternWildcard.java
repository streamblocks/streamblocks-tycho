package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class PatternWildcard extends Pattern {

	public PatternWildcard(IRNode original) {
		super(original);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {

	}

	@Override
	public IRNode transformChildren(Transformation transformation) {
		return this;
	}
}
