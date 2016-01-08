package se.lth.cs.tycho.transform.util;

import se.lth.cs.tycho.ir.Field;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
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

	public VarDecl transformVarDecl(VarDecl varDecl, P param);
	public ImmutableList<VarDecl> transformVarDecls(ImmutableList<VarDecl> varDecl, P param);

	public TypeDecl transformTypeDecl(TypeDecl typeDecl, P param);
	public ImmutableList<TypeDecl> transformTypeDecls(ImmutableList<TypeDecl> typeDecl, P param);

	public VarDecl transformValueParameter(VarDecl valueParam, P param);
	public ImmutableList<VarDecl> transformValueParameters(ImmutableList<VarDecl> valueParam, P param);

	public TypeDecl transformTypeParameter(TypeDecl typeParam, P param);
	public ImmutableList<TypeDecl> transformTypeParameters(ImmutableList<TypeDecl> typeParam, P param);

	public GeneratorFilter transformGenerator(GeneratorFilter generator, P param);
	public ImmutableList<GeneratorFilter> transformGenerators(ImmutableList<GeneratorFilter> generator, P param);

	public Variable transformVariable(Variable var, P param);
	public Field transformField(Field field, P param);
	public Port transformPort(Port port, P param);
	public TypeExpr transformTypeExpr(TypeExpr typeExpr, P param);

}
