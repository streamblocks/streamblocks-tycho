package net.opendf.transform.operators;

import java.util.LinkedList;

import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractBasicTransformer;

public class BinaryOpTransformer extends AbstractBasicTransformer<BinaryOpTable> {
	@Override
	public Expression visitExprBinaryOp(ExprBinaryOp opSeq, BinaryOpTable table) {
		return shuntingYard(opSeq.getOperations(), opSeq.getOperands(), table);
	}

	private Expression shuntingYard(ImmutableList<String> operations, ImmutableList<Expression> operands,
			BinaryOpTable table) {
		LinkedList<Expression> out = new LinkedList<Expression>();
		LinkedList<String> ops = new LinkedList<String>();
		int i = 0;
		out.add(operands.get(i));
		while (i < operations.size()) {
			int prec = table.get(operations.get(i)).getPrecedence();
			while (!ops.isEmpty() && prec <= table.get(ops.getLast()).getPrecedence()) {
				transformOperator(out, ops, table);
			}
			ops.addLast(operations.get(i));
			i += 1;
			out.add(operands.get(i));
		}
		while (!ops.isEmpty()) {
			transformOperator(out, ops, table);
		}
		assert out.size() == 1;
		return out.getFirst();
	}

	private void transformOperator(LinkedList<Expression> out, LinkedList<String> ops, BinaryOpTable table) {
		String operator = ops.removeLast();
		String function = table.get(operator).getFunction();
		ExprVariable func = new ExprVariable(Variable.namedVariable(function));
		Expression right = out.removeLast();
		Expression left = out.removeLast();
		ImmutableList<Expression> args = ImmutableList.of(left, right);
		Expression result = new ExprApplication(func, args);
		out.add(result);
	}
}
