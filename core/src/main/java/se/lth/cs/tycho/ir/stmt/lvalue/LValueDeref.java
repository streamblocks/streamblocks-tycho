package se.lth.cs.tycho.ir.stmt.lvalue;

import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class LValueDeref extends LValue {
	private final LValueVariable variable;

	public LValueDeref(LValueVariable variable) {
		this(null, variable);
	}

	private LValueDeref(IRNode original, LValueVariable variable) {
		super(original);
		this.variable = variable;
	}

	public LValueVariable getVariable() {
		return variable;
	}

	public LValueDeref withVariable(LValueVariable variable) {
		return this.variable == variable ? this : new LValueDeref(this, variable);
	}

	@Override
	public <R, P> R accept(LValueVisitor<R, P> visitor, P parameter) {
		return visitor.visitLValueDeref(this, parameter);
	}

	@Override
	public LValue transformChildren(Transformation transformation) {
		return withVariable(transformation.applyChecked(LValueVariable.class, variable));
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(variable);
	}
}
