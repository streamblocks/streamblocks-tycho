package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalTypeDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParDeclType;
import se.lth.cs.tycho.ir.decl.ParDeclValue;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

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
