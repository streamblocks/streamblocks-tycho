package net.opendf.interp.values;

import net.opendf.interp.Environment;
import net.opendf.interp.ProceduralExecutor;
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
	public RefView apply(ProceduralExecutor exec) {
		return exec.evaluate(lambda.getBody(), closure);
	}

}
