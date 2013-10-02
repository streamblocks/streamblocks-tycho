package net.opendf.interp.preprocess;

import java.util.Map;
import net.opendf.interp.values.ExprValue;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.predef.Predef;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.transform.util.AbstractBasicTransformer;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ActorTransformerWrapper;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;

/**
 * Constant propagation. Currently it only transform ExprLiteral to ExprValue
 * @author pera
 *
 */
public class EvaluateLiteralsTransformer extends AbstractBasicTransformer<Map<String, RefView>> {
	public static Actor transformActor(Actor actor){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer();
		ActorTransformerWrapper<Map<String, RefView>> wrapper = new ActorTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformActor(actor, Predef.predef());
	}

	public static ActorMachine transformActorMachine(ActorMachine actorMachine){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer();
		ActorMachineTransformerWrapper<Map<String, RefView>> wrapper = new ActorMachineTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformActorMachine(actorMachine, Predef.predef());
	}

	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer();
		NetworkDefinitionTransformerWrapper<Map<String, RefView>> wrapper = new NetworkDefinitionTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformNetworkDefinition(net, Predef.predef());
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

}
