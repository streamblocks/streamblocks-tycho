package se.lth.cs.tycho.analyze.util;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

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
	public final void traverseVarDecl(VarDecl varDecl, P param) {
		inner.traverseVarDecl(varDecl, param);
	}

	@Override
	public final void traverseVarDecls(ImmutableList<VarDecl> varDecl, P param) {
		inner.traverseVarDecls(varDecl, param);
	}

	@Override
	public final void traverseTypeDecl(TypeDecl typeDecl, P param) {
		inner.traverseTypeDecl(typeDecl, param);
	}

	@Override
	public final void traverseTypeDecls(ImmutableList<TypeDecl> typeDecl, P param) {
		inner.traverseTypeDecls(typeDecl, param);
	}

	@Override
	public final void traverseValueParameter(VarDecl valueParam, P param) {
		inner.traverseValueParameter(valueParam, param);
	}

	@Override
	public final void traverseValueParameters(ImmutableList<VarDecl> valueParam, P param) {
		inner.traverseValueParameters(valueParam, param);
	}

	@Override
	public final void traverseTypeParameter(TypeDecl typeParam, P param) {
		inner.traverseTypeParameter(typeParam, param);
	}

	@Override
	public final void traverseTypeParameters(ImmutableList<TypeDecl> typeParam, P param) {
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
