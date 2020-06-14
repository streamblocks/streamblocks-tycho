package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Nth;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprNth extends Expression {

	private final Expression structure;
	private final Nth nth;

	public ExprNth(Expression structure, Nth nth) {
		this(null, structure, nth);
	}

	public ExprNth(IRNode original, Expression structure, Nth nth) {
		super(original);
		this.structure = structure;
		this.nth = nth;
	}

	public ExprNth copy(Expression structure, Nth nth) {
		if (Objects.equals(getStructure(), structure) && Objects.equals(getNth(), nth)) {
			return this;
		} else {
			return new ExprNth(this, structure, nth);
		}
	}

	public Expression getStructure() {
		return structure;
	}

	public Nth getNth() {
		return nth;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getStructure());
	}

	@Override
	public Expression transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(Expression.class, getStructure()), getNth());
	}
}
