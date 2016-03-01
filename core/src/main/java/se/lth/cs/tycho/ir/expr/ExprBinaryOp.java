/* 
 *  @author Per Andersson<Per.Andersson@cs.lth.se>, Lund University
 *  Example, the expression a+b*c is represented as:
 *    operands = {a, b, c}
 *    operations = {+, *}
 */

package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class ExprBinaryOp extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprBinaryOp(this, p);
	}

	public ExprBinaryOp(ImmutableList<String> operations, ImmutableList<Expression> operands) {
		this(null, operations, operands);
	}

	public ExprBinaryOp(IRNode original, ImmutableList<String> operations, ImmutableList<Expression> operands) {
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

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		operands.forEach(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	public AbstractIRNode transformChildren(Transformation transformation) {
		return copy(operations, (ImmutableList) operands.map(transformation));
	}
}
