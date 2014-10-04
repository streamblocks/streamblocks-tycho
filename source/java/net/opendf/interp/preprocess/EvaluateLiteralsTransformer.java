package net.opendf.interp.preprocess;

import java.util.Map;

import net.opendf.interp.values.ExprValue;
import net.opendf.interp.values.RefView;
import net.opendf.interp.values.predef.Predef;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.expr.ExprApplication;
import net.opendf.ir.common.expr.ExprLiteral;
import net.opendf.ir.common.expr.ExprVariable;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ActorTransformerWrapper;
import net.opendf.transform.util.ErrorAwareBasicTransformer;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;

/**
 * Constant propagation. Currently it only transform ExprLiteral to ExprValue
 * 
 * Semantic Checks:
 * - non
 * 
 * Prerequisites:
 * - non
 * 
 * @author pera
 */
public class EvaluateLiteralsTransformer extends ErrorAwareBasicTransformer<Map<String, RefView>> {
	public EvaluateLiteralsTransformer(SourceCodeOracle sourceOracle) {
		super(sourceOracle);
	}

	public static Actor transformActor(Actor actor, SourceCodeOracle sourceOracle){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer(sourceOracle);
		ActorTransformerWrapper<Map<String, RefView>> wrapper = new ActorTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformActor(actor, Predef.predef());
	}

	public static ActorMachine transformActorMachine(ActorMachine actorMachine, SourceCodeOracle sourceOracle){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer(sourceOracle);
		ActorMachineTransformerWrapper<Map<String, RefView>> wrapper = new ActorMachineTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformActorMachine(actorMachine, Predef.predef());
	}

	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net, SourceCodeOracle sourceOracle){
		EvaluateLiteralsTransformer freeVarTransformer = new EvaluateLiteralsTransformer(sourceOracle);
		NetworkDefinitionTransformerWrapper<Map<String, RefView>> wrapper = new NetworkDefinitionTransformerWrapper<Map<String, RefView>>(freeVarTransformer);
		return wrapper.transformNetworkDefinition(net, Predef.predef());
	}

	@Override
	public ExprLiteral visitExprLiteral(ExprLiteral e, Map<String, RefView> p){
		return new ExprValue(e);
	}

	@Override
	public Expression visitExprApplication(ExprApplication e, Map<String, RefView> predefFunctions) {
		if(e.getFunction() instanceof ExprVariable){
			// check if the function is predefined, i.e. built in functions such as +, -, application()
			ExprVariable var = (ExprVariable) e.getFunction();
			RefView f = predefFunctions.get(var.getVariable().getName());
			if(f != null){
				return e.copy(new ExprValue(e, ExprLiteral.Kind.Function, var.getVariable().getName(), f), 
						transformExpressions(e.getArgs(), predefFunctions));
			}
		}
		return e.copy(
				transformExpression(e.getFunction(), predefFunctions),
				transformExpressions(e.getArgs(), predefFunctions));
	}

}
