package se.lth.cs.tycho.analyze.util;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
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

public interface BasicTraverser<P> {
	public void traverseExpression(Expression expr, P param);

	public void traverseExpressions(ImmutableList<Expression> expr, P param);

	public void traverseStatement(Statement stmt, P param);

	public void traverseStatements(ImmutableList<Statement> stmt, P param);

	public void traverseLValue(LValue lvalue, P param);

	public void traverseVarDecl(VarDecl varDecl, P param);

	public void traverseVarDecls(ImmutableList<VarDecl> varDecl, P param);

	public void traverseTypeDecl(TypeDecl typeDecl, P param);

	public void traverseTypeDecls(ImmutableList<TypeDecl> typeDecl, P param);

	public void traverseValueParameter(VarDecl valueParam, P param);

	public void traverseValueParameters(ImmutableList<VarDecl> valueParam, P param);

	public void traverseTypeParameter(TypeDecl typeParam, P param);

	public void traverseTypeParameters(ImmutableList<TypeDecl> typeParam, P param);

	public void traverseGenerator(GeneratorFilter generator, P param);

	public void traverseGenerators(ImmutableList<GeneratorFilter> generator, P param);

	public void traverseVariable(Variable var, P param);

	public void traverseVariables(ImmutableList<Variable> varList, P param);

	public void traverseField(Field field, P param);

	public void traversePort(Port port, P param);
	
	public void traverseQualifiedIdentifier(QID qid, P param);

	public void traverseTypeExpr(TypeExpr typeExpr, P param);

}
