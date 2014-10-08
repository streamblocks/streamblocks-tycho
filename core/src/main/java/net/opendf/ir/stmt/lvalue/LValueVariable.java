package net.opendf.ir.stmt.lvalue;

import java.util.Objects;

import net.opendf.ir.IRNode;
import net.opendf.ir.Variable;

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
	public <R, P> R accept(LValueVisitor<R, P> visitor, P parameter) {
		return visitor.visitLValueVariable(this, parameter);
	}
}