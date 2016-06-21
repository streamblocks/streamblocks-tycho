package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeExpr;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ParameterDeclarations {
	private ParameterDeclarations() {}

	public static Optional<Tree<VarDecl>> getEntityValueParameter(Tree<Parameter<Expression>> parameter) {
		return getEntityParameter(parameter, Entity::getValueParameters);
	}

	public static Optional<Tree<TypeDecl>> getEntityTypeParameter(Tree<Parameter<TypeExpr>> parameter) {
		return getEntityParameter(parameter, Entity::getTypeParameters);
	}

	private static <T extends Decl, N extends IRNode> Optional<Tree<T>> getEntityParameter(Tree<Parameter<N>> parameter, Function<Entity, Collection<T>> getParameters) {
		return parameter.parent()
				.flatMap(node -> node.tryCast(EntityInstanceExpr.class))
				.flatMap(EntityDeclarations::getDeclaration)
				.map(entityDecl -> entityDecl.child(EntityDecl::getEntity))
				.flatMap(entity -> entity.children(getParameters)
						.filter(decl -> decl.node().getName().equals(parameter.node().getName()))
						.findFirst());
	}
}
