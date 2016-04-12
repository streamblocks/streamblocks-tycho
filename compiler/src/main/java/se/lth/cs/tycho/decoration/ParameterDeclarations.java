package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Optional;

public final class ParameterDeclarations {
	private ParameterDeclarations() {}

	public static Optional<VarDecl> getVariableDeclaration(Tree<Parameter<Expression>> parameter) {
		Optional<Tree<EntityInstanceExpr>> instance = parameter.parent()
				.flatMap(node -> node.tryCast(EntityInstanceExpr.class));
		if (instance.isPresent()) {
			Optional<Tree<EntityDecl>> entityDecl = EntityDeclarations.getDeclaration(instance.get());

		}
		throw new UnsupportedOperationException("Not implemented.");
	}

}
