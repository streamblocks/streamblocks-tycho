package net.opendf.interp;

import net.opendf.interp.values.Builder;
import net.opendf.interp.values.BasicList;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.LambdaFunction;
import net.opendf.interp.values.List;
import net.opendf.interp.values.ProcProcedure;
import net.opendf.interp.values.Procedure;
import net.opendf.interp.values.RefView;
import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprEntry;
import net.opendf.ir.common.ExprIf;
import net.opendf.ir.common.ExprIndexer;
import net.opendf.ir.common.ExprInput;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprLet;
import net.opendf.ir.common.ExprList;
import net.opendf.ir.common.ExprLiteral;
import net.opendf.ir.common.ExprMap;
import net.opendf.ir.common.ExprProc;
import net.opendf.ir.common.ExprSet;
import net.opendf.ir.common.ExprUnaryOp;
import net.opendf.ir.common.ExprVariable;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.ExpressionVisitor;
import net.opendf.ir.common.GeneratorFilter;

public class ExpressionEvaluator implements ExpressionVisitor<RefView, Environment>, Evaluator {

	private final Simulator simulator;

	public ExpressionEvaluator(Simulator simulator) {
		this.simulator = simulator;
	}

	public RefView evaluate(Expression expr, Environment env) {
		return expr.accept(this, env);
	}

	public Stack getStack() {
		return simulator.stack();
	}

	@Override
	public RefView visitExprApplication(ExprApplication expr, Environment env) {
		TypeConverter converter = simulator.converter();
		RefView r = evaluate(expr.getFunction(), env);
		Function f = converter.getFunction(r);
		Expression[] argExprs = expr.getArgs();
		Stack stack = simulator.stack();
		for (Expression arg : argExprs) {
			stack.push(evaluate(arg, env));
		}
		return f.apply(argExprs.length, simulator);
	}

	@Override
	public RefView visitExprBinaryOp(ExprBinaryOp expr, Environment env) {
		throw notTransformed();
	}

	@Override
	public RefView visitExprEntry(ExprEntry expr, Environment env) {
		// TODO implement
		throw notImplemented();
	}

	@Override
	public RefView visitExprIf(ExprIf expr, Environment env) {
		TypeConverter converter = simulator.converter();
		RefView c = evaluate(expr.getCondition(), env);
		Expression e = converter.getBoolean(c) ? expr.getThenExpr() : expr.getElseExpr();
		return evaluate(e, env);
	}

	@Override
	public RefView visitExprIndexer(ExprIndexer expr, Environment env) {
		TypeConverter converter = simulator.converter();
		Stack stack = simulator.stack();
		stack.push(evaluate(expr.getStructure(), env));
		for (Expression indexExpr : expr.getLocation()) {
			List l = converter.getList(stack.pop());
			RefView index = evaluate(indexExpr, env);
			int i = converter.getInt(index);
			l.get(i, stack.push());
		}
		return stack.pop();
	}

	@Override
	public RefView visitExprInput(ExprInput expr, Environment env) {
		Stack stack = simulator.stack();
		Channel channel = env.getChannel(expr.getChannelId());
		if (!expr.hasRepeat()) {
			channel.peek(expr.getOffset(), stack.push());
			return stack.pop();
		} else {
			BasicList.Builder builder = new BasicList.Builder();
			final int first = expr.getOffset();
			final int delta = expr.getPatternLength();
			final int last = delta * expr.getRepeat() + first;
			for (int i = first; i < last; i += delta) {
				channel.peek(i, stack.push());
				builder.add(stack.pop());
			}
			simulator.converter().setList(stack.push(), builder.build());
			return stack.pop();
		}
	}

	@Override
	public RefView visitExprLambda(ExprLambda expr, Environment env) {
		Stack stack = simulator.stack();
		TypeConverter converter = simulator.converter();
		Environment closure = expr.createClosure(env, simulator.stack());
		Function f = new LambdaFunction(expr, closure);
		converter.setFunction(stack.push(), f);
		return stack.pop();
	}

	@Override
	public RefView visitExprLet(ExprLet expr, Environment env) {
		Stack stack = simulator.stack();
		int stackAllocs = 0;
		Declarator decl = simulator.declarator();
		for (DeclType d : expr.getTypeDecls()) {
			stackAllocs += decl.declare(d, env);
		}
		for (DeclVar d : expr.getVarDecls()) {
			stackAllocs += decl.declare(d, env);
		}
		RefView r = evaluate(expr.getBody(), env);
		stack.remove(stackAllocs);
		return r;
	}

	@Override
	public RefView visitExprList(ExprList expr, Environment env) {
		BasicList.Builder builder = new BasicList.Builder();
		buildCollection(expr.getGenerators(), expr.getElements(), builder, env);
		Stack stack = simulator.stack();
		TypeConverter converter = simulator.converter();
		converter.setList(stack.push(), builder.build());
		return stack.pop();
	}

	private void buildCollection(GeneratorFilter[] generators, final Expression[] elements, final Builder builder,
			final Environment env) {
		Runnable buildList = new Runnable() {
			public void run() {
				for (Expression e : elements) {
					builder.add(evaluate(e, env));
				}
			}
		};
		simulator.generator().generate(generators, buildList, env);
	}

	@Override
	public RefView visitExprLiteral(ExprLiteral expr, Environment env) {
		return expr.getValue();
	}

	@Override
	public RefView visitExprMap(ExprMap expr, Environment env) {
		// TODO implement
		throw notImplemented();
	}

	@Override
	public RefView visitExprProc(ExprProc expr, Environment env) {
		Stack stack = simulator.stack();
		TypeConverter converter = simulator.converter();
		Environment closure = expr.createClosure(env, simulator.stack());
		Procedure p = new ProcProcedure(expr, closure);
		converter.setProcedure(stack.push(), p);
		return stack.pop();
	}

	@Override
	public RefView visitExprSet(ExprSet expr, Environment env) {
		// TODO implement
		throw notImplemented();
	}

	@Override
	public RefView visitExprUnaryOp(ExprUnaryOp expr, Environment env) {
		throw notTransformed();
	}

	@Override
	public RefView visitExprVariable(ExprVariable expr, Environment env) {
		int pos = expr.getVariablePosition();
		boolean stack = expr.isVariableOnStack();
		if (stack)
			return simulator.stack().peek(pos);
		else
			return env.getMemory().get(pos);
	}

	private IllegalArgumentException notTransformed() {
		return new IllegalArgumentException("Tree not transformed");
	}

	private UnsupportedOperationException notImplemented() {
		return new UnsupportedOperationException("Not implemented");
	}
}
