package se.lth.cs.tycho.transform.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprApplication;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprField;
import se.lth.cs.tycho.ir.expr.ExprIf;
import se.lth.cs.tycho.ir.expr.ExprIndexer;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprMap;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.ExprSet;
import se.lth.cs.tycho.ir.expr.ExprUnaryOp;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.ExpressionVisitor;
import se.lth.cs.tycho.ir.expr.GlobalValueReference;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StatementVisitor;
import se.lth.cs.tycho.ir.stmt.StmtAssignment;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCall;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.ir.stmt.StmtIf;
import se.lth.cs.tycho.ir.stmt.StmtOutput;
import se.lth.cs.tycho.ir.stmt.StmtWhile;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueField;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueIndexer;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.ImmutableList.Builder;

/**
 * BasicTransformer implementation that transforms the nodes by calling the
 * copy-method on the node with its transformed children as arguments. The
 * effect of this is that if no method is overloaded, the same tree is returned.
 * 
 * The nodes are transformed in a depth-first order. List elements are
 * transformed in the list order. Declarations are transformed before its
 * potential uses.
 * 
 * @author gustav
 * 
 * @param <P>
 */
public class AbstractBasicTransformer<P> implements
BasicTransformer<P>,
StatementVisitor<Statement, P>,
ExpressionVisitor<Expression, P>,
LValueVisitor<LValue, P> {

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	protected static MethodHandle methodHandle(Class<?> target, Class<?> arg, String meth) {
		MethodType type = MethodType.methodType(arg, arg, Object.class);
		try {
			return lookup.findVirtual(target, meth, type);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> ImmutableList<T> transformList(MethodHandle method, ImmutableList<T> list, P param) {
		if (list == null) {
			return null;
		}
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		try {
			for (T element : list) {
				builder.add((T) method.invoke(this, element, param));
			}
		} catch(Error e){
			throw e;
		} catch(RuntimeException e){
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return builder.build();
	}

	private static final MethodHandle transExpr = methodHandle(AbstractBasicTransformer.class, Expression.class, "transformExpression");
	private static final MethodHandle transStmt = methodHandle(AbstractBasicTransformer.class, Statement.class, "transformStatement");
	private static final MethodHandle transVarDecl = methodHandle(AbstractBasicTransformer.class, VarDecl.class, "transformVarDecl");
	private static final MethodHandle transTypeDecl = methodHandle(AbstractBasicTransformer.class, TypeDecl.class, "transformTypeDecl");
	private static final MethodHandle transValueParam = methodHandle(AbstractBasicTransformer.class, VarDecl.class, "transformValueParameter");
	private static final MethodHandle transTypeParam = methodHandle(AbstractBasicTransformer.class, TypeDecl.class, "transformTypeParameter");
	private static final MethodHandle transGenerator = methodHandle(AbstractBasicTransformer.class, GeneratorFilter.class, "transformGenerator");

	@Override
	public Expression transformExpression(Expression expr, P param) {
		if (expr == null)
			return null;
		return expr.accept(this, param);
	}

	@Override
	public ImmutableList<Expression> transformExpressions(ImmutableList<Expression> expr, P param) {
		return transformList(transExpr, expr, param);
	}

	@Override
	public Statement transformStatement(Statement stmt, P param) {
		if (stmt == null)
			return null;
		return stmt.accept(this, param);
	}

	@Override
	public ImmutableList<Statement> transformStatements(ImmutableList<Statement> stmt, P param) {
		return transformList(transStmt, stmt, param);
	}

	@Override
	public LValue transformLValue(LValue lvalue, P param) {
		return lvalue.accept(this, param);
	}

	@Override
	public VarDecl transformVarDecl(VarDecl varDecl, P param) {
		assert varDecl != null;
		return varDecl.copyAsLocal(
				transformTypeExpr(varDecl.getType(), param),
				varDecl.getName(),
				varDecl.isConstant(),
				transformExpression(varDecl.getValue(), param));
	}

	@Override
	public ImmutableList<VarDecl> transformVarDecls(ImmutableList<VarDecl> varDecl, P param) {
		return transformList(transVarDecl, varDecl, param);
	}

	@Override
	public TypeDecl transformTypeDecl(TypeDecl typeDecl, P param) {
		return typeDecl;
	}

	@Override
	public ImmutableList<TypeDecl> transformTypeDecls(ImmutableList<TypeDecl> typeDecl, P param) {
		return transformList(transTypeDecl, typeDecl, param);
	}

	@Override
	public VarDecl transformValueParameter(VarDecl valueParam, P param) {
		return valueParam.copyAsParameter(
				transformTypeExpr(valueParam.getType(), param),
				valueParam.getName());
	}

	@Override
	public ImmutableList<VarDecl> transformValueParameters(ImmutableList<VarDecl> valueParam, P param) {
		return transformList(transValueParam, valueParam, param);
	}

	@Override
	public TypeDecl transformTypeParameter(TypeDecl typeParam, P param) {
		return typeParam;
	}

	@Override
	public ImmutableList<TypeDecl> transformTypeParameters(ImmutableList<TypeDecl> typeParam, P param) {
		return transformList(transTypeParam, typeParam, param);
	}

	@Override
	public GeneratorFilter transformGenerator(GeneratorFilter generator, P param) {
		Expression collection = transformExpression(generator.getCollectionExpr(), param);
		ImmutableList<VarDecl> variables = transformVarDecls(generator.getVariables(), param);
		ImmutableList<Expression> filters = transformExpressions(generator.getFilters(), param);
		return generator.copy(variables, collection, filters);
	}

	@Override
	public ImmutableList<GeneratorFilter> transformGenerators(ImmutableList<GeneratorFilter> generator, P param) {
		return transformList(transGenerator, generator, param);
	}

	@Override
	public Variable transformVariable(Variable var, P param) {
		return var;
	}

	@Override
	public Field transformField(Field field, P param) {
		return field;
	}

	@Override
	public Port transformPort(Port port, P param) {
		return port;
	}

	@Override
	public TypeExpr transformTypeExpr(TypeExpr typeExpr, P param) {
		if (typeExpr == null) {
			return null;
		}
		ImmutableList.Builder<Parameter<TypeExpr>> typeParBuilder = ImmutableList.builder();
		for (Parameter<TypeExpr> entry : typeExpr.getTypeParameters()) {
			typeParBuilder.add(new Parameter<>(
					entry.getName(),
					transformTypeExpr(entry.getValue(), param)));
		}
		ImmutableList.Builder<Parameter<Expression>> valParBuilder = ImmutableList.builder();
		for (Parameter<Expression> entry : typeExpr.getValueParameters()) {
			valParBuilder.add(new Parameter<>(
					entry.getName(),
					transformExpression(entry.getValue(), param)));
		}
		return typeExpr.copy(typeExpr.getName(), typeParBuilder.build(), valParBuilder.build());
	}

	@Override
	public LValue visitLValueVariable(LValueVariable lvalue, P parameter) {
		return lvalue.copy(transformVariable(lvalue.getVariable(), parameter));
	}

	@Override
	public LValue visitLValueIndexer(LValueIndexer lvalue, P parameter) {
		return lvalue.copy(
				transformLValue(lvalue.getStructure(), parameter),
				transformExpression(lvalue.getIndex(), parameter));
	}

	@Override
	public LValue visitLValueField(LValueField lvalue, P parameter) {
		return lvalue.copy(
				transformLValue(lvalue.getStructure(), parameter),
				transformField(lvalue.getField(), parameter));
	}

	@Override
	public Expression visitExprApplication(ExprApplication e, P p) {
		return e.copy(
				transformExpression(e.getFunction(), p),
				transformExpressions(e.getArgs(), p));
	}

	@Override
	public Expression visitExprBinaryOp(ExprBinaryOp e, P p) {
		return e.copy(
				e.getOperations(),
				transformExpressions(e.getOperands(), p));
	}

	@Override
	public Expression visitExprField(ExprField e, P p) {
		return e.copy(
				transformExpression(e.getStructure(), p),
				transformField(e.getField(), p));
	}

	@Override
	public Expression visitExprIf(ExprIf e, P p) {
		return e.copy(
				transformExpression(e.getCondition(), p),
				transformExpression(e.getThenExpr(), p),
				transformExpression(e.getElseExpr(), p));
	}

	@Override
	public Expression visitExprIndexer(ExprIndexer e, P p) {
		return e.copy(
				transformExpression(e.getStructure(), p),
				transformExpression(e.getIndex(), p));
	}

	@Override
	public Expression visitExprInput(ExprInput e, P p) {
		if(e.hasRepeat()){
			return e.copy(
					transformPort(e.getPort(), p),
					e.getOffset(),
					e.getRepeat(),
					e.getPatternLength());

		} else {
			return e.copy(
					transformPort(e.getPort(), p),
					e.getOffset());
		}
	}

	@Override
	public Expression visitExprLambda(ExprLambda e, P p) {
		Builder<Variable> builder = ImmutableList.builder();
		if(e.isFreeVariablesComputed()){
			for(Variable v : e.getFreeVariables()){
				builder.add(transformVariable(v, p));
			}
		}
		return e.copy(
				transformTypeParameters(e.getTypeParameters(), p),
				transformValueParameters(e.getValueParameters(), p),
				transformExpression(e.getBody(), p),
				transformTypeExpr(e.getReturnType(), p),
				builder.build(),
				e.isFreeVariablesComputed());
	}

	@Override
	public Expression visitExprLet(ExprLet e, P p) {
		return e.copy(
				transformTypeDecls(e.getTypeDecls(), p),
				transformVarDecls(e.getVarDecls(), p),
				transformExpression(e.getBody(), p));
	}

	@Override
	public Expression visitExprList(ExprList e, P p) {
		ImmutableList<GeneratorFilter> generators = transformGenerators(e.getGenerators(), p);
		ImmutableList<Expression> elements = transformExpressions(e.getElements(), p);
		return e.copy(elements, generators);
	}

	@Override
	public Expression visitExprLiteral(ExprLiteral e, P p) {
		return e;
	}

	@Override
	public Expression visitExprMap(ExprMap e, P p) {
		ImmutableList<GeneratorFilter> generators = transformGenerators(e.getGenerators(), p);
		ImmutableList.Builder<ImmutableEntry<Expression, Expression>> builder = ImmutableList.builder();
		for (ImmutableEntry<Expression, Expression> entry : e.getMappings()) {
			builder.add(entry.copy(
					transformExpression(entry.getKey(), p),
					transformExpression(entry.getValue(), p)));
		}
		return e.copy(builder.build(), generators);
	}

	@Override
	public Expression visitExprProc(ExprProc e, P p) {
		Builder<Variable> builder = ImmutableList.builder();
		if(e.isFreeVariablesComputed()){
			for(Variable v : e.getFreeVariables()){
				builder.add(transformVariable(v, p));
			}
		}
		return e.copy(
				transformTypeParameters(e.getTypeParameters(), p),
				transformValueParameters(e.getValueParameters(), p),
				transformStatement(e.getBody(), p),
				builder.build(),
				e.isFreeVariablesComputed());
	}

	@Override
	public Expression visitExprSet(ExprSet e, P p) {
		ImmutableList<GeneratorFilter> generators = transformGenerators(e.getGenerators(), p);
		ImmutableList<Expression> elements = transformExpressions(e.getElements(), p);
		return e.copy(elements, generators);
	}

	@Override
	public Expression visitExprUnaryOp(ExprUnaryOp e, P p) {
		return e.copy(e.getOperation(), transformExpression(e.getOperand(), p));
	}

	@Override
	public Expression visitExprVariable(ExprVariable e, P p) {
		return e.copy(transformVariable(e.getVariable(), p));
	}
	
	@Override
	public Expression visitGlobalValueReference(GlobalValueReference e, P p) {
		return e.copy(transformQualifiedIdentifier(e.getQualifiedIdentifier(), p), e.isNamespaceReference());
	}

	private QID transformQualifiedIdentifier(QID qid, P p) {
		return qid;
	}

	@Override
	public Statement visitStmtAssignment(StmtAssignment s, P p) {
		return s.copy(transformLValue(s.getLValue(), p), transformExpression(s.getExpression(), p));
	}

	@Override
	public Statement visitStmtBlock(StmtBlock s, P p) {
		return s.copy(
				transformTypeDecls(s.getTypeDecls(), p),
				transformVarDecls(s.getVarDecls(), p),
				transformStatements(s.getStatements(), p));
	}

	@Override
	public Statement visitStmtIf(StmtIf s, P p) {
		return s.copy(
				transformExpression(s.getCondition(), p),
				transformStatement(s.getThenBranch(), p),
				transformStatement(s.getElseBranch(), p));
	}

	@Override
	public Statement visitStmtCall(StmtCall s, P p) {
		return s.copy(transformExpression(s.getProcedure(), p), transformExpressions(s.getArgs(), p));
	}

	@Override
	public Statement visitStmtOutput(StmtOutput s, P p) {
		if(s.hasRepeat()){
			return s.copy(transformExpressions(s.getValues(), p), transformPort(s.getPort(), p), s.getRepeat());
		} else {
			return s.copy(transformExpressions(s.getValues(), p), transformPort(s.getPort(), p));			
		}
	}

	@Override
	public Statement visitStmtConsume(StmtConsume s, P p) {
		return s.copy(transformPort(s.getPort(), p), s.getNumberOfTokens());
	}

	@Override
	public Statement visitStmtWhile(StmtWhile s, P p) {
		return s.copy(transformExpression(s.getCondition(), p), transformStatement(s.getBody(), p));
	}

	@Override
	public Statement visitStmtForeach(StmtForeach s, P p) {
		return s.copy(transformGenerators(s.getGenerators(), p), transformStatement(s.getBody(), p));
	}

}
