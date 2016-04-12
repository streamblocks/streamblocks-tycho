package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.PortReference;

import java.util.Optional;

public final class InstanceDeclarations {
	private InstanceDeclarations() {}

	public static Optional<EntityInstanceExpr> getDeclaration(Tree<PortReference> portReference) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
