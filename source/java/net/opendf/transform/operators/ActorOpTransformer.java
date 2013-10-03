package net.opendf.transform.operators;

import java.util.LinkedList;
import java.util.Map;

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
import net.opendf.transform.util.AbstractBasicTransformer;
import net.opendf.transform.util.ActorMachineTransformerWrapper;
import net.opendf.transform.util.ActorTransformerWrapper;
import net.opendf.transform.util.NetworkDefinitionTransformerWrapper;
/**
 * Replaces all BinaryOp and UnaryOp nodes in expressions with corresponding ExprApplication.
 * The transformation is done by calling transformActor(Actor actor).
 *
 */
public class ActorOpTransformer extends AbstractBasicTransformer<Map<String, Integer>> {

	public static Actor transformActor(Actor actor){
		ActorOpTransformer transformer = new ActorOpTransformer();
		ActorTransformerWrapper<Map<String, Integer>> wrapper = new ActorTransformerWrapper<Map<String, Integer>>(transformer);
		return wrapper.transformActor(actor, BinOpPriorities.getDefaultMapper());
	}

	public static ActorMachine transformActorMachine(ActorMachine actorMachine){
		ActorOpTransformer transformer = new ActorOpTransformer();
		ActorMachineTransformerWrapper<Map<String, Integer>> wrapper = new ActorMachineTransformerWrapper<Map<String, Integer>>(transformer);
		return wrapper.transformActorMachine(actorMachine, BinOpPriorities.getDefaultMapper());
	}

	public static NetworkDefinition transformNetworkDefinition(NetworkDefinition net){
		ActorOpTransformer transformer = new ActorOpTransformer();
		NetworkDefinitionTransformerWrapper<Map<String, Integer>> wrapper = new NetworkDefinitionTransformerWrapper<Map<String, Integer>>(transformer);
		return wrapper.transformNetworkDefinition(net, BinOpPriorities.getDefaultMapper());
	}

	@Override
	public Expression visitExprBinaryOp(ExprBinaryOp opSeq, Map<String, Integer> table) {
		return shuntingYard(opSeq.getOperations(), opSeq.getOperands(), table);
	}

	@Override
	public Expression visitExprUnaryOp(ExprUnaryOp opSeq, Map<String, Integer> table) {
		String funcName = "$UnaryOperation." + opSeq.getOperation();
		ExprVariable func = new ExprVariable(Variable.variable(funcName));
		Expression arg = transformExpression(opSeq.getOperand(), table);
		return new ExprApplication(func, ImmutableList.of(arg));
	}

	static int getPriority(String op, Map<String, Integer> pri){
		Integer i = pri.get(op);
		if(i== null){
			System.err.println("WARNING: unknown priority for binary operator " + op);
			//TODO, report error
			return 0;
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
			int prec = getPriority(operations.get(i), priorities);
			while (!ops.isEmpty() && prec <= getPriority(ops.getLast(), priorities)) {
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
		ExprVariable func = new ExprVariable(Variable.variable(function));
		Expression right = out.removeLast();
		Expression left = out.removeLast();
		ImmutableList<Expression> args = ImmutableList.of(left, right);
		Expression result = new ExprApplication(func, args);
		out.add(result);
	}
}
