package net.opendf.ir.common;

/**
 * A statement for assigning to a variable.
 */
public class StmtAssignment extends Statement {
	private LValue lvalue;
	private Expression expression;

	/**
	 * Constructs a StmtAssignment.
	 * 
	 * @param lvalue
	 *            the left hand side
	 * @param expression
	 *            the right hand side
	 */
	public StmtAssignment(LValue lvalue, Expression expression) {
		this.lvalue = lvalue;
		this.expression = expression;
	}

	/**
	 * Returns the left hand side of the assignment.
	 * 
	 * @return the left hand side
	 */
	public LValue getLValue() {
		return lvalue;
	}

	/**
	 * Returns the right hand side of the assignment.
	 * 
	 * @return the right hand side
	 */
	public Expression getExpression() {
		return expression;
	}

	@Override
	public <R, P> R accept(StatementVisitor<R, P> v, P p) {
		return v.visitStmtAssignment(this, p);
	}

}
