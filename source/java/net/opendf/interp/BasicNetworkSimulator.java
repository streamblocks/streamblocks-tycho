package net.opendf.interp;

import java.util.Map;

import net.opendf.analyze.memory.VariableInitOrderTransformer;
import net.opendf.interp.preprocess.EvaluateLiteralsTransformer;
import net.opendf.interp.preprocess.VariableOffsetTransformer;
import net.opendf.ir.common.Expression;
import net.opendf.ir.net.ast.NetworkDefinition;
import net.opendf.ir.net.ast.evaluate.NetDefEvaluator;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.operators.ActorOpTransformer;

public class BasicNetworkSimulator {


	public static NetworkDefinition prepareNetworkDefinition(NetworkDefinition net){
		// order variable initializations
		net = VariableInitOrderTransformer.transformNetworkDefinition(net);
		// replace operators with function calls
		net = ActorOpTransformer.transformNetworkDefinition(net);
		// replace global variables with constants, i.e. $BinaryOperation.+ with ExprValue(ConstRef.of(new IntFunctions.Add()))
		net = EvaluateLiteralsTransformer.transformNetworkDefinition(net);
		// compute variable offsets
		VariableOffsetTransformer varT = new VariableOffsetTransformer();
		net = varT.transformNetworkDefinition(net);
		
		Interpreter interpreter = new BasicInterpreter(100);
		NetDefEvaluator eval = new NetDefEvaluator(net, interpreter);
		eval.evaluate(ImmutableList.<Map.Entry<String,Expression>>empty());
		net = eval.getNetworkDefinition();

		return net;
	}
		
}
