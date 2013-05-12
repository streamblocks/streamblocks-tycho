package net.opendf.ir.transform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.opendf.ir.common.DeclType;
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
import net.opendf.ir.common.Field;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.LValueField;
import net.opendf.ir.common.LValueIndexer;
import net.opendf.ir.common.LValueVariable;
import net.opendf.ir.common.LValueVisitor;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.StatementVisitor;
import net.opendf.ir.common.StmtAssignment;
import net.opendf.ir.common.StmtBlock;
import net.opendf.ir.common.StmtCall;
import net.opendf.ir.common.StmtConsume;
import net.opendf.ir.common.StmtForeach;
import net.opendf.ir.common.StmtIf;
import net.opendf.ir.common.StmtOutput;
import net.opendf.ir.common.StmtWhile;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;

public class AbstractBasicTransformer<P> implements
		BasicTransformer<P>,
		StatementVisitor<Statement, P>,
		ExpressionVisitor<Expression, P>,
		LValueVisitor<LValue, P> {

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	protected static MethodHandle methodHandle(Class<?> arg, String meth) {
		MethodType type = MethodType.methodType(arg, arg, Object.class);
		try {
			return lookup.findVirtual(AbstractActorTransformer.class, meth, type);
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
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return builder.build();
	}

	private static final MethodHandle transExpr = methodHandle(Expression.class, "transformExpression");
	private static final MethodHandle transStmt = methodHandle(Statement.class, "transformStatement");
	private static final MethodHandle transVarDecl = methodHandle(DeclVar.class, "transformVarDecl");
	private static final MethodHandle transTypeDecl = methodHandle(DeclType.class, "transformTypeDecl");
	private static final MethodHandle transValueParam = methodHandle(ParDeclValue.class, "transformValueParameter");
	private static final MethodHandle transTypeParam = methodHandle(ParDeclType.class, "transformTypeParameter");
	private static final MethodHandle transGenerator = methodHandle(GeneratorFilter.class, "transformGenerator");

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
	public DeclVar transformVarDecl(DeclVar varDecl, P param) {
		return varDecl.copy(
				transformTypeExpr(varDecl.getType(), param),
				varDecl.getName(),
				varDecl.getNamespaceDecl(),
				transformExpression(varDecl.getInitialValue(), param),
				varDecl.isAssignable());
	}

	@Override
	public ImmutableList<DeclVar> transformVarDecls(ImmutableList<DeclVar> varDecl, P param) {
		return transformList(transVarDecl, varDecl, param);
	}

	@Override
	public DeclType transformTypeDecl(DeclType typeDecl, P param) {
		return typeDecl;
	}

	@Override
	public ImmutableList<DeclType> transformTypeDecls(ImmutableList<DeclType> typeDecl, P param) {
		return transformList(transTypeDecl, typeDecl, param);
	}

	@Override
	public ParDeclValue transformValueParameter(ParDeclValue valueParam, P param) {
		return valueParam.copy(
				valueParam.getName(),
				transformTypeExpr(valueParam.getType(), param));
	}

	@Override
	public ImmutableList<ParDeclValue> transformValueParameters(ImmutableList<ParDeclValue> valueParam, P param) {
		return transformList(transValueParam, valueParam, param);
	}

	@Override
	public ParDeclType transformTypeParameter(ParDeclType typeParam, P param) {
		return typeParam;
	}

	@Override
	public ImmutableList<ParDeclType> transformTypeParameters(ImmutableList<ParDeclType> typeParam, P param) {
		return transformList(transTypeParam, typeParam, param);
	}

	@Override
	public GeneratorFilter transformGenerator(GeneratorFilter generator, P param) {
		return generator.copy(
				transformVarDecls(generator.getVariables(), param),
				transformExpression(generator.getCollectionExpr(), param),
				transformExpressions(generator.getFilters(), param));
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
		ImmutableList.Builder<ImmutableEntry<String, TypeExpr>> typeParBuilder = ImmutableList.builder();
		for (ImmutableEntry<String, TypeExpr> entry : typeExpr.getTypeParameters()) {
			typeParBuilder.add(ImmutableEntry.of(
					entry.getKey(),
					transformTypeExpr(entry.getValue(), param)));
		}
		ImmutableList.Builder<ImmutableEntry<String, Expression>> valParBuilder = ImmutableList.builder();
		for (ImmutableEntry<String, Expression> entry : typeExpr.getValueParameters()) {
			valParBuilder.add(ImmutableEntry.of(
					entry.getKey(),
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
		return e.copy(
				transformPort(e.getPort(), p),
				e.getOffset(),
				e.getRepeat(),
				e.getPatternLength());
	}

	@Override
	public Expression visitExprLambda(ExprLambda e, P p) {
		return e.copy(
				transformTypeParameters(e.getTypeParameters(), p),
				transformValueParameters(e.getValueParameters(), p),
				transformExpression(e.getBody(), p),
				transformTypeExpr(e.getReturnType(), p));
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
		return e.copy(
				transformExpressions(e.getElements(), p),
				transformGenerators(e.getGenerators(), p));
	}

	@Override
	public Expression visitExprLiteral(ExprLiteral e, P p) {
		return e;
	}

	@Override
	public Expression visitExprMap(ExprMap e, P p) {
		ImmutableList.Builder<ImmutableEntry<Expression, Expression>> builder = ImmutableList.builder();
		for (ImmutableEntry<Expression, Expression> entry : e.getMappings()) {
			builder.add(entry.copy(
					transformExpression(entry.getKey(), p),
					transformExpression(entry.getValue(), p)));
		}
		return e.copy(builder.build(), transformGenerators(e.getGenerators(), p));
	}

	@Override
	public Expression visitExprProc(ExprProc e, P p) {
		return e.copy(
				transformTypeParameters(e.getTypeParameters(), p),
				transformValueParameters(e.getValueParameters(), p),
				transformStatement(e.getBody(), p));
	}

	@Override
	public Expression visitExprSet(ExprSet e, P p) {
		return e.copy(
				transformExpressions(e.getElements(), p),
				transformGenerators(e.getGenerators(), p));
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
		return s.copy(transformExpressions(s.getValues(), p), transformPort(s.getPort(), p), s.getRepeat());
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
