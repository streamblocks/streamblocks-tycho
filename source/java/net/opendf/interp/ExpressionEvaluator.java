package net.opendf.interp;

import net.opendf.interp.exception.CALRuntimeException;
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
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.ExprApplication;
import net.opendf.ir.common.ExprBinaryOp;
import net.opendf.ir.common.ExprField;
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

	private RefView evaluate(Expression expr, Environment env) throws CALRuntimeException {
		return expr.accept(this, env);
	}

	@Override
	public RefView visitExprApplication(ExprApplication expr, Environment env) throws CALRuntimeException {
		try{
			RefView r = evaluate(expr.getFunction(), env);
			Function f = converter.getFunction(r);

			ImmutableList<Expression> argExprs = expr.getArgs();
			for (Expression arg : argExprs) {
				stack.push(evaluate(arg, env));
			}

			return f.apply(interpreter);
			// Function.apply() is responsible for removing the arguments from the stack.
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprBinaryOp(ExprBinaryOp expr, Environment env) throws CALRuntimeException {
		CALRuntimeException e = new CALRuntimeException("ExprBinaryOp should be transformed to function call.");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprField(ExprField expr, Environment p) throws CALRuntimeException {
		CALRuntimeException e = new CALRuntimeException("Field is not supported.");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprIf(ExprIf expr, Environment env) throws CALRuntimeException {
		try{
			RefView c = evaluate(expr.getCondition(), env);
			Expression e = converter.getBoolean(c) ? expr.getThenExpr() : expr.getElseExpr();
			return evaluate(e, env);
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprIndexer(ExprIndexer expr, Environment env) throws CALRuntimeException {
		try{
			List l = converter.getList(evaluate(expr.getStructure(), env));
			RefView index = evaluate(expr.getIndex(), env);
			int i = converter.getInt(index);
			l.get(i, stack.push());
			return stack.pop();
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprInput(ExprInput expr, Environment env) throws CALRuntimeException {
		try{
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
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprLambda(ExprLambda expr, Environment env) throws CALRuntimeException {
		try{
			assert expr.isFreeVariablesComputed();
			int[] noSelect = {};
			Environment closureEnv = env.closure(noSelect, noSelect, expr.getFreeVariables(), stack);
			Function f = new LambdaFunction(expr, closureEnv);
			converter.setFunction(stack.push(), f);
			return stack.pop();
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprLet(ExprLet expr, Environment env) throws CALRuntimeException {
		try{
			if(!expr.getTypeDecls().isEmpty()) {
				CALRuntimeException e = new CALRuntimeException("Type declarations in let expressions.");
				e.pushCalStack(expr);
				throw e;
			}
			//FIXME, initialize the variables in a correct order. for a=b, b=1 the order is (b, a)
			// this assumes that the declaration are ordered in a correct evaluation order
			for (DeclVar d : expr.getVarDecls()) {
				stack.push(evaluate(d.getInitialValue(), env));
			}
			RefView r = evaluate(expr.getBody(), env);
			stack.remove(expr.getVarDecls().size());
			return r;
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprList(ExprList expr, Environment env) throws CALRuntimeException {
		try{
			BasicList.Builder builder = new BasicList.Builder();
			buildCollection(expr.getGenerators(), expr.getElements(), builder, env);
			converter.setList(stack.push(), builder.build());
			return stack.pop();
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	private void buildCollection(ImmutableList<GeneratorFilter> generatorList, final ImmutableList<Expression> elements, final Builder builder,
			final Environment env) throws CALRuntimeException {
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
	public RefView visitExprLiteral(ExprLiteral expr, Environment env) throws CALRuntimeException {
		if(expr instanceof ExprValue){
			return ((ExprValue)expr).getValue();
		}
		CALRuntimeException e = new CALRuntimeException("The literal " + expr.getText() + " is not transformed to ExprValue.");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprMap(ExprMap expr, Environment env) throws CALRuntimeException {
		// TODO implement
		CALRuntimeException e = new CALRuntimeException("Map Comprehension, i.e. map {a->: for a, b in {1,2,3}}");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprProc(ExprProc expr, Environment env) throws CALRuntimeException {
		try{
			assert expr.isFreeVariablesComputed();
			int[] noSelect = {};
			Environment closureEnv = env.closure(noSelect, noSelect, expr.getFreeVariables(), stack);
			Procedure p = new ProcProcedure(expr, closureEnv);
			converter.setProcedure(stack.push(), p);
			return stack.pop();
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}

	@Override
	public RefView visitExprSet(ExprSet expr, Environment env) throws CALRuntimeException {
		// TODO implement
		CALRuntimeException e = new CALRuntimeException("Set Comprehension, i.e. {2*a: for a in {1,2,3}}");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprUnaryOp(ExprUnaryOp expr, Environment env) throws CALRuntimeException {
		CALRuntimeException e = new CALRuntimeException("ExprUnaryOp should be transformed to function call.");
		e.pushCalStack(expr);
		throw e;
	}

	@Override
	public RefView visitExprVariable(ExprVariable expr, Environment env) throws CALRuntimeException {
		try{
			VariableLocation var = (VariableLocation)expr.getVariable();
			RefView value;
			if (var.isScopeVariable()){
				value = env.getMemory().get(var);
			} else {
				value = stack.peek(var.getOffset());
			}
			return value;
		} catch(CALRuntimeException e){
			e.pushCalStack(expr);
			throw e;
		}
	}
}
