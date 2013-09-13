package net.opendf.interp.preprocess;

import java.util.Map;

import net.opendf.interp.values.ExprValue;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.predef.Predef;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExprVariable;
import net.opendf.transform.util.AbstractActorMachineTransformer;

public class EvaluateLiteralsTransformer extends AbstractActorMachineTransformer<Map<String, RefView>> {
	public ActorMachine transformActorMachine(ActorMachine actorMachine){
		return transformActorMachine(actorMachine, Predef.predef());
	}

	public ExprLiteral visitExprLiteral(ExprLiteral e, Map<String, RefView> p){
		return new ExprValue(e);
	}

	@Override
	public Expression visitExprApplication(ExprApplication e, Map<String, RefView> p) {
		if(e.getFunction() instanceof ExprVariable){
			ExprVariable var = (ExprVariable) e.getFunction();
			RefView f = p.get(var.getVariable().getName());
			if(f != null){
				return e.copy(new ExprValue(e, ExprLiteral.Kind.Function, var.getVariable().getName(), f), 
						transformExpressions(e.getArgs(), p));
			}
		}
		return e.copy(
				transformExpression(e.getFunction(), p),
				transformExpressions(e.getArgs(), p));
	}

	private void error(IRNode node, String msg) {
		System.err.println(msg);
		throw new RuntimeException(msg);
	}
}
