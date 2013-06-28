package net.opendf.transform.operators;

import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprUnaryOp;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.AbstractBasicTransformer;

public class UnaryOpTransformer extends AbstractBasicTransformer<UnaryOpTable> {
	@Override
	public Expression visitExprUnaryOp(ExprUnaryOp unaryOp, UnaryOpTable table) {
		String functionName = table.get(unaryOp.getOperation()).getFunction();
		Variable functionVar = Variable.namedVariable(functionName);
		ExprVariable function = new ExprVariable(functionVar);
		return new ExprApplication(function, ImmutableList.of(unaryOp.getOperand()));
	}
}
