package se.lth.cs.tycho.ir.expr.pattern;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;

import java.util.Objects;
import java.util.function.Consumer;

public class PatternVariable extends Pattern {

	private Variable variable;

	public PatternVariable(Variable variable) {
		this(null, variable);
	}

	public PatternVariable(IRNode original, Variable variable) {
		super(original);
		this.variable = variable;
	}

	public PatternVariable copy(Variable variable) {
		if (Objects.equals(getVariable(), variable)) {
			return this;
		} else {
			return new PatternVariable(this, variable);
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
	public IRNode transformChildren(Transformation transformation) {
		return copy((Variable) transformation.apply(getVariable()));
	}
}
