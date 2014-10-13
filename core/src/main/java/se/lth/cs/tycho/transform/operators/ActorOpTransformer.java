package se.lth.cs.tycho.transform.operators;

import java.util.LinkedList;
import java.util.Map;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.interp.exception.CALCompiletimeException;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.parser.SourceCodeOracle;
import se.lth.cs.tycho.parser.SourceCodeOracle.SourceCodePosition;
import se.lth.cs.tycho.transform.util.ActorMachineTransformerWrapper;
import se.lth.cs.tycho.transform.util.ActorTransformerWrapper;
import se.lth.cs.tycho.transform.util.ErrorAwareBasicTransformer;
import se.lth.cs.tycho.transform.util.NetworkDefinitionTransformerWrapper;
/**
 * Replaces all BinaryOp and UnaryOp nodes in expressions with corresponding ExprApplication.
 * The transformation is done by calling transformActor(CalActor calActor).
 *
 * Semantic Checks:
 * - warns if an unknown binary operation is used (unknown priority)
 * 
 * Prerequisites:
 * - non
 * 
 * @author pera
 */
public class ActorOpTransformer extends ErrorAwareBasicTransformer<Map<String, Integer>> {

	public ActorOpTransformer(SourceCodeOracle sourceOracle) {
		super(sourceOracle);
	}

	//--- wrappers ------------------------------------------------------------
	/**
	 * Replace all operations with function calls.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static CalActor transformActor(CalActor calActor, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		ActorOpTransformer transformer = new ActorOpTransformer(sourceOracle);
		ActorTransformerWrapper<Map<String, Integer>> wrapper = new ActorTransformerWrapper<Map<String, Integer>>(transformer);
		calActor = wrapper.transformActor(calActor, BinOpPriorities.getDefaultMapper());
		transformer.printWarnings();
		transformer.abortIfError();
		return calActor;
	}

	/**
	 * Replace all operations with function calls.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static ActorMachine transformActorMachine(ActorMachine actorMachine, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		ActorOpTransformer transformer = new ActorOpTransformer(sourceOracle);
		ActorMachineTransformerWrapper<Map<String, Integer>> wrapper = new ActorMachineTransformerWrapper<Map<String, Integer>>(transformer);
		actorMachine = wrapper.transformActorMachine(actorMachine, BinOpPriorities.getDefaultMapper());
		transformer.printWarnings();
		transformer.abortIfError();
		return actorMachine;
	}

	/**
	 * Replace all operations with function calls.
	 * Prints all warnings to System.err and throws an exception if any error occurs.
	 * @throws CALCompiletimeException if an error occurs
	 */
	public static NlNetwork transformNetworkDefinition(NlNetwork net, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		ActorOpTransformer transformer = new ActorOpTransformer(sourceOracle);
		NetworkDefinitionTransformerWrapper<Map<String, Integer>> wrapper = new NetworkDefinitionTransformerWrapper<Map<String, Integer>>(transformer);
		net = wrapper.transformNetworkDefinition(net, BinOpPriorities.getDefaultMapper());
		transformer.printWarnings();
		transformer.abortIfError();
		return net;
	}


	//--- transformations ---------------------------------------------------------------

	@Override
	public Expression visitExprBinaryOp(ExprBinaryOp opSeq, Map<String, Integer> table) {
		return shuntingYard(opSeq.getOperations(), opSeq.getOperands(), table);
	}

	@Override
	public Expression visitExprUnaryOp(ExprUnaryOp opSeq, Map<String, Integer> table) {
		String funcName = "$UnaryOperation." + opSeq.getOperation();
		Variable funcVar = Variable.variable(funcName);
		ExprVariable func = new ExprVariable(funcVar);
		Expression arg = transformExpression(opSeq.getOperand(), table);
		Expression result = new ExprApplication(func, ImmutableList.of(arg));
		// register the source code trace
		//SourceCodePosition opPos = sourceOracle.getSrcLocations(opSeq.getIdentifier());
		//opPos = SourceCodePosition.newExcludeEnd(opPos, sourceOracle.getSrcLocations(arg.getIdentifier()));
		//sourceOracle.register(funcVar, opPos);
		return result;
	}

	private int getPriority(String op, Map<String, Integer> pri, IRNode position){
		Integer i = pri.get(op);
		if(i== null){
			//TODO, the position reported by the error message is inaccurate. It should be between operands.get(i) and operands.get(i+1)
			warning("unknown priority for binary operator " + op, position);
			return -1;
		}
		return i;
	}
	private Expression shuntingYard(ImmutableList<String> operations, ImmutableList<Expression> operands,
			Map<String, Integer> priorities) {
		LinkedList<Expression> out = new LinkedList<Expression>();
		LinkedList<String> ops = new LinkedList<String>();
		int i = 0;
		out.add(transformExpression(operands.get(i), priorities));
		while (i < operations.size()) {
			int prec = getPriority(operations.get(i), priorities, operands.get(i));
			while (!ops.isEmpty() && prec <= getPriority(ops.getLast(), priorities, operands.get(i))) {
				transformOperator(out, ops);
			}
			ops.addLast(operations.get(i));
			i += 1;
			out.add(transformExpression(operands.get(i), priorities));
		}
		while (!ops.isEmpty()) {
			transformOperator(out, ops);
		}
		assert out.size() == 1;
		return out.getFirst();
	}

	private void transformOperator(LinkedList<Expression> out, LinkedList<String> ops) {
		String operator = ops.removeLast();
		String function = "$BinaryOperation." + operator;
		Variable funcVar = Variable.variable(function);
		ExprVariable func = new ExprVariable(funcVar);  // use the same Identifier as for funcVar
		Expression right = out.removeLast();
		Expression left = out.removeLast();
		ImmutableList<Expression> args = ImmutableList.of(left, right);
		Expression result = new ExprApplication(func, args);
		// register the source code trace
		//SourceCodePosition before = sourceOracle.getSrcLocations(left.getIdentifier());
		//SourceCodePosition after = sourceOracle.getSrcLocations(right.getIdentifier());
		//sourceOracle.register(result, SourceCodePosition.newIncluding(before, after));
		//sourceOracle.register(funcVar, SourceCodePosition.newBetween(before, after));
		out.add(result);
	}
}