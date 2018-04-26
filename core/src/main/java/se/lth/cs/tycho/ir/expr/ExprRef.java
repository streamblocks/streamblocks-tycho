package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;

import java.util.function.Consumer;

public class ExprRef extends Expression {
	private final Variable variable;

	public ExprRef(Variable variable) {
		this(null, variable);
	}
	private ExprRef(ExprRef original, Variable variable) {
		super(original);
		this.variable = variable;
	}

	private ExprRef copy(Variable variable) {
		if (this.variable == variable) {
			return this;
		} else {
			return new ExprRef(this, variable);
		}
	}

	public Variable getVariable() {
		return variable;
	}

	public ExprRef withVariable(Variable variable) {
		return copy(variable);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(variable);
	}

	@Override
	public ExprRef transformChildren(Transformation transformation) {
		return copy(transformation.applyChecked(Variable.class, variable));
	}

}
