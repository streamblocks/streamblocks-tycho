/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 */

package net.opendf.ir.common;

import java.util.Objects;

public class ExprUnaryOp extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprUnaryOp(this, p);
	}

	public ExprUnaryOp(String operation, Expression operand) {
		this(null, operation, operand);
	}

	private ExprUnaryOp(ExprUnaryOp original, String operation, Expression operand) {
		super(original);
		this.operation = operation;
		this.operand = operand;
	}

	public ExprUnaryOp copy(String operation, Expression operand) {
		if (Objects.equals(this.operation, operation) && Objects.equals(this.operand, operand)) {
			return this;
		}
		return new ExprUnaryOp(this, operation, operand);
	}

	public String getOperation() {
		return operation;
	}

	public Expression getOperand() {
		return operand;
	}

	private String operation;
	private Expression operand;
}
