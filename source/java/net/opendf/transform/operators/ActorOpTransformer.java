package net.opendf.transform.operators;

import java.util.LinkedList;
import java.util.Map;

import net.opendf.interp.exception.CALCompiletimeException;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.cal.Actor;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprUnaryOp;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Variable;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.util.ImmutableList;
import net.opendf.parser.SourceCodeOracle;
import net.opendf.parser.SourceCodeOracle.SourceCodePosition;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ActorTransformerWrapper;
import net.opendf.transform.util.ErrorAwareBasicTransformer;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;
/**
 * Replaces all BinaryOp and UnaryOp nodes in expressions with corresponding ExprApplication.
 * The transformation is done by calling transformActor(Actor actor).
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
	public static Actor transformActor(Actor actor, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
		ActorOpTransformer transformer = new ActorOpTransformer(sourceOracle);
		ActorTransformerWrapper<Map<String, Integer>> wrapper = new ActorTransformerWrapper<Map<String, Integer>>(transformer);
		actor = wrapper.transformActor(actor, BinOpPriorities.getDefaultMapper());
		transformer.printWarnings();
		transformer.abortIfError();
		return actor;
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
	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net, SourceCodeOracle sourceOracle) throws CALCompiletimeException {
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
