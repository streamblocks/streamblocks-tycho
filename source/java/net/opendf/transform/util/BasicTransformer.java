package net.opendf.transform.util;

import net.opendf.ir.common.Field;
import net.opendf.ir.common.GeneratorFilter;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.TypeExpr;
import net.opendf.ir.common.Variable;
import net.opendf.ir.common.decl.LocalTypeDecl;
import net.opendf.ir.common.decl.LocalVarDecl;
import net.opendf.ir.common.decl.ParDeclType;
import net.opendf.ir.common.decl.ParDeclValue;
import net.opendf.ir.common.decl.VarDecl;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.common.lvalue.LValue;
import net.opendf.ir.common.stmt.Statement;
import net.opendf.ir.util.ImmutableList;

public interface BasicTransformer<P> {
	public Expression transformExpression(Expression expr, P param);
	public ImmutableList<Expression> transformExpressions(ImmutableList<Expression> expr, P param);

	public Statement transformStatement(Statement stmt, P param);
	public ImmutableList<Statement> transformStatements(ImmutableList<Statement> stmt, P param);

	public LValue transformLValue(LValue lvalue, P param);

	public LocalVarDecl transformVarDecl(LocalVarDecl varDecl, P param);
	public ImmutableList<LocalVarDecl> transformVarDecls(ImmutableList<LocalVarDecl> varDecl, P param);

	public LocalTypeDecl transformTypeDecl(LocalTypeDecl typeDecl, P param);
	public ImmutableList<LocalTypeDecl> transformTypeDecls(ImmutableList<LocalTypeDecl> typeDecl, P param);

	public ParDeclValue transformValueParameter(ParDeclValue valueParam, P param);
	public ImmutableList<ParDeclValue> transformValueParameters(ImmutableList<ParDeclValue> valueParam, P param);

	public ParDeclType transformTypeParameter(ParDeclType typeParam, P param);
	public ImmutableList<ParDeclType> transformTypeParameters(ImmutableList<ParDeclType> typeParam, P param);

	public GeneratorFilter transformGenerator(GeneratorFilter generator, P param);
	public ImmutableList<GeneratorFilter> transformGenerators(ImmutableList<GeneratorFilter> generator, P param);

	public Variable transformVariable(Variable var, P param);
	public Field transformField(Field field, P param);
	public Port transformPort(Port port, P param);
	public TypeExpr transformTypeExpr(TypeExpr typeExpr, P param);

}
