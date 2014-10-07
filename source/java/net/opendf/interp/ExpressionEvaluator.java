package net.opendf.interp;

import net.opendf.interp.values.BasicList;
import net.opendf.interp.values.BasicRef;
import net.opendf.interp.values.Builder;
import net.opendf.interp.values.ExprValue;
import net.opendf.interp.values.Function;
import net.opendf.interp.values.LambdaFunction;
import net.opendf.interp.values.List;
import net.opendf.interp.values.ProcProcedure;
import net.opendf.interp.values.Procedure;
import net.opendf.interp.values.RefView;
import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.expr.ExprApplication;
import net.opendf.ir.expr.ExprBinaryOp;
import net.opendf.ir.expr.ExprField;
import net.opendf.ir.expr.ExprIf;
import net.opendf.ir.expr.ExprIndexer;
import net.opendf.ir.expr.ExprInput;
import net.opendf.ir.expr.ExprLambda;
import net.opendf.ir.expr.ExprLet;
import net.opendf.ir.expr.ExprList;
import net.opendf.ir.expr.ExprLiteral;
import net.opendf.ir.expr.ExprMap;
import net.opendf.ir.expr.ExprProc;
import net.opendf.ir.expr.ExprSet;
import net.opendf.ir.expr.ExprUnaryOp;
import net.opendf.ir.expr.ExprVariable;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.expr.ExpressionVisitor;
import net.opendf.ir.util.ImmutableList;

public class ExpressionEvaluator implements ExpressionVisitor<RefView, Environment> {

	private final Interpreter interpreter;
	private final Stack stack;
	private final TypeConverter converter;
	private final GeneratorFilterHelper generator;
	private final BasicRef tmp;

	public ExpressionEvaluator(Interpreter executor) {
		this.interpreter = executor;
		this.stack = executor.getStack();
		this.converter = TypeConverter.getInstance();
		this.generator = new GeneratorFilterHelper(executor);
		this.tmp = new BasicRef();
	}

	private RefView evaluate(Expression expr, Environment env) {
		return expr.accept(this, env);
	}

	@Override
	public RefView visitExprApplication(ExprApplication expr, Environment env) {
        RefView r = evaluate(expr.getFunction(), env);
		Function f = converter.getFunction(r);
		
        ImmutableList<Expression> argExprs = expr.getArgs();
		for (Expression arg : argExprs) {
			stack.push(evaluate(arg, env));
		}

		return f.apply(interpreter);
		// Function.apply() is responsible for removing the arguments from the stack.
	}

	@Override
	public RefView visitExprBinaryOp(ExprBinaryOp expr, Environment env) {
		throw notTransformed("ExprBinaryOp should be transformed to function call.");
	}

	@Override
	public RefView visitExprField(ExprField e, Environment p) {
		// TODO Auto-generated method stub
		throw notImplemented("field access");
	}

	@Override
	public RefView visitExprIf(ExprIf expr, Environment env) {
		RefView c = evaluate(expr.getCondition(), env);
		Expression e = converter.getBoolean(c) ? expr.getThenExpr() : expr.getElseExpr();
		return evaluate(e, env);
	}

	@Override
	public RefView visitExprIndexer(ExprIndexer expr, Environment env) {
		List l = converter.getList(evaluate(expr.getStructure(), env));
		RefView index = evaluate(expr.getIndex(), env);
		int i = converter.getInt(index);
		l.get(i, stack.push());
		return stack.pop();
	}

	@Override
	public RefView visitExprInput(ExprInput expr, Environment env) {
		Channel.OutputEnd channel = env.getSourceChannelOutputEnd(expr.getPort().getOffset());
		if (!expr.hasRepeat()) {
			channel.peek(expr.getOffset(), tmp);
			return tmp;
		} else {
			BasicList.Builder builder = new BasicList.Builder();
			final int first = expr.getOffset();
			final int delta = expr.getPatternLength();
			final int last = delta * expr.getRepeat() + first;
			for (int i = first; i < last; i += delta) {
				channel.peek(i, tmp);
				builder.add(tmp);  // the list creates a copy
			}
			converter.setList(tmp, builder.build());
			return tmp;
		}
	}

	@Override
	public RefView visitExprLambda(ExprLambda expr, Environment env) {
		assert expr.isFreeVariablesComputed();
		int[] noSelect = {};
		Environment closureEnv = env.closure(noSelect, noSelect, expr.getFreeVariables(), stack);
		Function f = new LambdaFunction(expr, closureEnv);
		converter.setFunction(stack.push(), f);
		return stack.pop();
	}

	@Override
	public RefView visitExprLet(ExprLet expr, Environment env) {
		if(!expr.getTypeDecls().isEmpty()) {
			throw notImplemented("Type declarations in let expressions.");
		}
		//FIXME, initialize the variables in a correct order. for a=b, b=1 the order is (b, a)
		// this assumes that the declaration are ordered in a correct evaluation order
		for (LocalVarDecl d : expr.getVarDecls()) {
			stack.push(evaluate(d.getInitialValue(), env));
		}
		RefView r = evaluate(expr.getBody(), env);
		stack.remove(expr.getVarDecls().size());
		return r;
	}

	@Override
	public RefView visitExprList(ExprList expr, Environment env) {
		BasicList.Builder builder = new BasicList.Builder();
		buildCollection(expr.getGenerators(), expr.getElements(), builder, env);
		converter.setList(stack.push(), builder.build());
		return stack.pop();
	}

	private void buildCollection(ImmutableList<GeneratorFilter> generatorList, final ImmutableList<Expression> elements, final Builder builder,
			final Environment env) {
		Runnable buildList = new Runnable() {
			public void run() {
				for (Expression e : elements) {
					builder.add(evaluate(e, env));
				}
			}
		};
		generator.interpret(generatorList, buildList, env);
	}

	@Override
	public RefView visitExprLiteral(ExprLiteral expr, Environment env) {
		if(expr instanceof ExprValue){
			return ((ExprValue)expr).getValue();
		}
		throw notTransformed("The literal " + expr.getText() + " is not transformed to ExprValue.");
	}

	@Override
	public RefView visitExprMap(ExprMap expr, Environment env) {
		// TODO implement
		throw notImplemented("Map Comprehension, i.e. map {a->: for a, b in {1,2,3}}");
	}

	@Override
	public RefView visitExprProc(ExprProc expr, Environment env) {
		assert expr.isFreeVariablesComputed();
		int[] noSelect = {};
		Environment closureEnv = env.closure(noSelect, noSelect, expr.getFreeVariables(), stack);
		Procedure p = new ProcProcedure(expr, closureEnv);
		converter.setProcedure(stack.push(), p);
		return stack.pop();
	}

	@Override
	public RefView visitExprSet(ExprSet expr, Environment env) {
		// TODO implement
		throw notImplemented("Set Comprehension, i.e. {2*a: for a in {1,2,3}}");
	}

	@Override
	public RefView visitExprUnaryOp(ExprUnaryOp expr, Environment env) {
		throw notTransformed("ExprUnaryOp should be transformed to function call.");
	}

	@Override
	public RefView visitExprVariable(ExprVariable expr, Environment env) {
		VariableLocation var = (VariableLocation)expr.getVariable();
		RefView value;
		if (var.isScopeVariable()){
			value = env.getMemory().get(var);
		} else {
			value = stack.peek(var.getOffset());
		}
		return value;
	}

	private IllegalArgumentException notTransformed(String msg) {
		return new IllegalArgumentException(msg);
	}

	private UnsupportedOperationException notImplemented(String msg) {
		return new UnsupportedOperationException(msg);
	}
}
