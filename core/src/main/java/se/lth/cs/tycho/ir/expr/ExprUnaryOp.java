/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 */

package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class ExprUnaryOp extends Expression {

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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(operand);
	}

	@Override
	public ExprUnaryOp transformChildren(Transformation transformation) {
		return copy(operation, (Expression) transformation.apply(operand));
	}
}
