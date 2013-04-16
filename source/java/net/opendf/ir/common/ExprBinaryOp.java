/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 *  Example, the expression a+b*c is represented as:
 *    operands = {a, b, c}
 *    operations = {+, *}
 */

package net.opendf.ir.common;

import net.opendf.ir.util.ImmutableList;

public class ExprBinaryOp extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprBinaryOp(this, p);
	}

	public ExprBinaryOp(ImmutableList<String> operations, ImmutableList<Expression> operands) {
		assert (operations.size() == operands.size() - 1);
		this.operations = operations;
		this.operands = operands;
	}

	public ImmutableList<String> getOperations() {
		return operations;
	}

	public ImmutableList<Expression> getOperands() {
		return operands;
	}

	private ImmutableList<String> operations;
	private ImmutableList<Expression> operands;
}
