package se.lth.cs.tycho.ir.stmt.lvalue;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;

/**
 * An LValue that is a variable.
 */
public class LValueVariable extends LValue {
	private Variable variable;

	/**
	 * Constructs an LValueVariable for a given variable.
	 * 
	 * @param variable
	 *            the variable.
	 */
	public LValueVariable(Variable variable) {
		this(null, variable);
	}

	public LValueVariable(IRNode original, Variable variable) {
		super(original);
		this.variable = variable;
	}

	public LValueVariable copy(Variable variable) {
		if (Objects.equals(this.variable, variable)) {
			return this;
		}
		return new LValueVariable(this, variable);
	}

	/**
	 * Returns the variable.
	 * 
	 * @return the variable
	 */
	public Variable getVariable() {
		return variable;
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(variable);
	}

	@Override
	public LValueVariable transformChildren(Transformation transformation) {
		return copy((Variable) transformation.apply(variable));
	}
}
