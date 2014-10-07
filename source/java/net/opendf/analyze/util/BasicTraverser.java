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

public interface BasicTraverser<P> {
	public void traverseExpression(Expression expr, P param);

	public void traverseExpressions(ImmutableList<Expression> expr, P param);

	public void traverseStatement(Statement stmt, P param);

	public void traverseStatements(ImmutableList<Statement> stmt, P param);

	public void traverseLValue(LValue lvalue, P param);

	public void traverseVarDecl(LocalVarDecl varDecl, P param);

	public void traverseVarDecls(ImmutableList<LocalVarDecl> varDecl, P param);

	public void traverseTypeDecl(LocalTypeDecl typeDecl, P param);

	public void traverseTypeDecls(ImmutableList<LocalTypeDecl> typeDecl, P param);

	public void traverseValueParameter(ParDeclValue valueParam, P param);

	public void traverseValueParameters(ImmutableList<ParDeclValue> valueParam, P param);

	public void traverseTypeParameter(ParDeclType typeParam, P param);

	public void traverseTypeParameters(ImmutableList<ParDeclType> typeParam, P param);

	public void traverseGenerator(GeneratorFilter generator, P param);

	public void traverseGenerators(ImmutableList<GeneratorFilter> generator, P param);

	public void traverseVariable(Variable var, P param);

	public void traverseVariables(ImmutableList<Variable> varList, P param);

	public void traverseField(Field field, P param);

	public void traversePort(Port port, P param);

	public void traverseTypeExpr(TypeExpr typeExpr, P param);

}
