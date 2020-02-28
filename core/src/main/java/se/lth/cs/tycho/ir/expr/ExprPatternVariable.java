package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprPatternVariable extends Expression {

	private final Variable variable;

	public ExprPatternVariable(Variable variable) {
		this(null, variable);
	}

	public ExprPatternVariable(IRNode original, Variable variable) {
		super(original);
		this.variable = variable;
	}

	public ExprPatternVariable copy(Variable variable) {
		if (Objects.equals(getVariable(), variable)) {
			return this;
		} else {
			return new ExprPatternVariable(this, variable);
		}
	}

	public Variable getVariable() {
		return variable;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(getVariable());
	}

	@Override
	public ExprPatternVariable transformChildren(Transformation transformation) {
		return copy((Variable) transformation.apply(getVariable()));
	}
}
