package net.opendf.analyze.util;

import net.opendf.ir.Field;
import net.opendf.ir.GeneratorFilter;
import net.opendf.ir.Port;
import net.opendf.ir.TypeExpr;
import net.opendf.ir.Variable;
import net.opendf.ir.decl.LocalTypeDecl;
import net.opendf.ir.decl.LocalVarDecl;
import net.opendf.ir.decl.ParDeclType;
import net.opendf.ir.decl.ParDeclValue;
import net.opendf.ir.expr.Expression;
import net.opendf.ir.stmt.Statement;
import net.opendf.ir.stmt.lvalue.LValue;
import net.opendf.ir.util.ImmutableList;

public class ActorMachineTraverserWrapper<P> extends AbstractActorMachineTraverser<P> {

	private final BasicTraverser<P> inner;

	public ActorMachineTraverserWrapper(BasicTraverser<P> inner) {
		this.inner = inner;
	}

	@Override
	public final void traverseExpression(Expression expr, P param) {
		inner.traverseExpression(expr, param);
	}

	@Override
	public final void traverseExpressions(ImmutableList<Expression> expr, P param) {
		inner.traverseExpressions(expr, param);
	}

	@Override
	public final void traverseStatement(Statement stmt, P param) {
		inner.traverseStatement(stmt, param);
	}

	@Override
	public final void traverseStatements(ImmutableList<Statement> stmt, P param) {
		inner.traverseStatements(stmt, param);
	}

	@Override
	public final void traverseLValue(LValue lvalue, P param) {
		inner.traverseLValue(lvalue, param);
	}

	@Override
	public final void traverseVarDecl(LocalVarDecl varDecl, P param) {
		inner.traverseVarDecl(varDecl, param);
	}

	@Override
	public final void traverseVarDecls(ImmutableList<LocalVarDecl> varDecl, P param) {
		inner.traverseVarDecls(varDecl, param);
	}

	@Override
	public final void traverseTypeDecl(LocalTypeDecl typeDecl, P param) {
		inner.traverseTypeDecl(typeDecl, param);
	}

	@Override
	public final void traverseTypeDecls(ImmutableList<LocalTypeDecl> typeDecl, P param) {
		inner.traverseTypeDecls(typeDecl, param);
	}

	@Override
	public final void traverseValueParameter(ParDeclValue valueParam, P param) {
		inner.traverseValueParameter(valueParam, param);
	}

	@Override
	public final void traverseValueParameters(ImmutableList<ParDeclValue> valueParam, P param) {
		inner.traverseValueParameters(valueParam, param);
	}

	@Override
	public final void traverseTypeParameter(ParDeclType typeParam, P param) {
		inner.traverseTypeParameter(typeParam, param);
	}

	@Override
	public final void traverseTypeParameters(ImmutableList<ParDeclType> typeParam, P param) {
		inner.traverseTypeParameters(typeParam, param);
	}

	@Override
	public final void traverseGenerator(GeneratorFilter generator, P param) {
		inner.traverseGenerator(generator, param);
	}

	@Override
	public final void traverseGenerators(ImmutableList<GeneratorFilter> generator, P param) {
		inner.traverseGenerators(generator, param);
	}

	@Override
	public final void traverseVariable(Variable var, P param) {
		inner.traverseVariable(var, param);
	}

	@Override
	public final void traverseField(Field field, P param) {
		inner.traverseField(field, param);
	}

	@Override
	public final void traversePort(Port port, P param) {
		inner.traversePort(port, param);
	}

	@Override
	public final void traverseTypeExpr(TypeExpr typeExpr, P param) {
		inner.traverseTypeExpr(typeExpr, param);
	}

}
