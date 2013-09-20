package net.opendf.analyze.util;

import net.opendf.ir.common.DeclType;
import net.opendf.ir.common.DeclVar;
import net.opendf.ir.common.Expression;
import net.opendf.ir.common.Field;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.LValue;
import net.opendf.ir.common.ParDeclType;
import net.opendf.ir.common.ParDeclValue;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.Statement;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.Variable;
import net.opendf.ir.util.ImmutableList;

public interface BasicTraverser<P> {
	public void traverseExpression(Expression expr, P param);

	public void traverseExpressions(ImmutableList<Expression> expr, P param);

	public void traverseStatement(Statement stmt, P param);

	public void traverseStatements(ImmutableList<Statement> stmt, P param);

	public void traverseLValue(LValue lvalue, P param);

	public void traverseVarDecl(DeclVar varDecl, P param);

	public void traverseVarDecls(ImmutableList<DeclVar> varDecl, P param);

	public void traverseTypeDecl(DeclType typeDecl, P param);

	public void traverseTypeDecls(ImmutableList<DeclType> typeDecl, P param);

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
