package net.opendf.interp.values;

import net.opendf.interp.Environment;
import net.opendf.interp.Simulator;
import net.opendf.ir.common.ExprLambda;

public class LambdaFunction implements Function {
	
	public final ExprLambda lambda;
	public final Environment closure;
	
	public LambdaFunction(ExprLambda lambda, Environment closure) {
		this.lambda = lambda;
		this.closure = closure;
	}

	@Override
	public Value copy() {
		return this;
	}

	@Override
	public RefView apply(int args, Simulator sim) {
		if (args != lambda.getValueParameters().length) {
			throw new IllegalArgumentException("Wrong number of arguments");
		}
		return sim.evaluator().evaluate(lambda.getBody(), closure);
	}

}
