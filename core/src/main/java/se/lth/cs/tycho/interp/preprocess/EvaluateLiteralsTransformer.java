package se.lth.cs.tycho.interp.preprocess;

import java.util.Map;

import se.lth.cs.tycho.interp.values.ExprValue;
import se.lth.cs.tycho.interp.values.RefView;
import se.lth.cs.tycho.interp.values.predef.Predef;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.Actor;
import se.lth.cs.tycho.ir.entity.nl.NetworkDefinition;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.parser.SourceCodeOracle;
import se.lth.cs.tycho.transform.util.ActorMachineTransformerWrapper;
import se.lth.cs.tycho.transform.util.ActorTransformerWrapper;
import se.lth.cs.tycho.transform.util.ErrorAwareBasicTransformer;
import se.lth.cs.tycho.transform.util.NetworkDefinitionTransformerWrapper;

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
