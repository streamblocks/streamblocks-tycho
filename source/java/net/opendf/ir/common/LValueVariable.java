package net.opendf.ir.common;

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
		this.variable = variable;
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
