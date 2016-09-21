package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public final class ParameterDeclarations {
	private ParameterDeclarations() {}

	public static Optional<Tree<ParameterVarDecl>> getEntityValueParameter(Tree<ValueParameter> parameter) {
		return getEntityParameter(parameter, Entity::getValueParameters);
	}

	public static Optional<Tree<TypeDecl>> getEntityTypeParameter(Tree<TypeParameter> parameter) {
		return getEntityParameter(parameter, Entity::getTypeParameters);
	}

	private static <T extends Decl, N extends IRNode, P extends Parameter<N, P>> Optional<Tree<T>> getEntityParameter(Tree<P> parameter, Function<Entity, Collection<T>> getParameters) {
		return parameter.parent()
				.flatMap(node -> node.tryCast(EntityInstanceExpr.class))
				.map(node -> node.child(EntityInstanceExpr::getEntityName))
				.flatMap(EntityDeclarations::getDeclaration)
				.map(entityDecl -> entityDecl.child(GlobalEntityDecl::getEntity))
				.flatMap(entity -> entity.children(getParameters)
						.filter(decl -> decl.node().getName().equals(parameter.node().getName()))
						.findFirst());
	}
}
