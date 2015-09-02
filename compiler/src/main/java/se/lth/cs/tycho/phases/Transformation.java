package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.expr.ExpressionVisitor;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StatementVisitor;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVisitor;

public interface Transformation<P> extends ExpressionVisitor<Expression, P>, StatementVisitor<Statement, P>, LValueVisitor<LValue, P>, EntityVisitor<Entity, P> {
	NamespaceDecl visitNamespaceDecl(NamespaceDecl ns, P param);

	PortDecl visitPortDecl(PortDecl port, P param);

	VarDecl visitVarDecl(VarDecl varDecl, P param);

	EntityDecl visitEntityDecl(EntityDecl entityDecl, P param);

	TypeDecl visitTypeDecl(TypeDecl typeDecl, P param);

	GeneratorFilter visitGenerator(GeneratorFilter generator, P param);

	TypeExpr visitTypeExpr(TypeExpr typeExpr, P param);

}
