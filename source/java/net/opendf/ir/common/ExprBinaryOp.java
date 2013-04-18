/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 *  Example, the expression a+b*c is represented as:
 *    operands = {a, b, c}
 *    operations = {+, *}
 */

package net.opendf.ir.common;

import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class ExprBinaryOp extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprBinaryOp(this, p);
	}

	public ExprBinaryOp(ImmutableList<String> operations, ImmutableList<Expression> operands) {
		this(null, operations, operands);
	}

	private ExprBinaryOp(ExprBinaryOp original, ImmutableList<String> operations, ImmutableList<Expression> operands) {
		super(original);
		assert (operations.size() == operands.size() - 1);
		this.operations = operations;
		this.operands = operands;
	}

	public ExprBinaryOp copy(ImmutableList<String> operations, ImmutableList<Expression> operands) {
		if (Lists.equals(this.operations, operations) && Lists.equals(this.operands, operands)) {
			return this;
		} else {
			return new ExprBinaryOp(this, operations, operands);
		}
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
