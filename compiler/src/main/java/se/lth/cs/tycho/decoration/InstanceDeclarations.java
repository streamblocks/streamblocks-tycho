package se.lth.cs.tycho.decoration;

import se.lth.cs.tycho.ir.entity.nl.InstanceDecl;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;

import java.util.Optional;
import java.util.stream.Stream;

public final class InstanceDeclarations {
	private InstanceDeclarations() {}

	/**
	 * Returns true if the port reference is a port on the network itself and not a port on one of its containing instances.
	 * @param portReference the port reference
	 * @return true if the port is on the network itself
	 */
	public static boolean isBoundaryPort(PortReference portReference) {
		return portReference.getEntityName() == null;
	}

	/**
	 * Returns the instance declaration that is referred to by the given port reference.
	 *
	 * Throws an exception if the port reference is a port on the network itself.
	 * @param portReference the port reference
	 * @return the declaration of the instance that has the given port
	 * @throws IllegalArgumentException if the port reference is a boundary port
	 */
	public static Optional<Tree<InstanceDecl>> getDeclaration(Tree<PortReference> portReference) {
		String instanceName = portReference.node().getEntityName();
		if (instanceName == null) {
			throw new IllegalArgumentException("Cannot get instance declaration of a boundary port reference.");
		}
		Optional<Tree<NlNetwork>> network = portReference.findParentOfType(NlNetwork.class);
		Stream<Tree<InstanceDecl>> declarations = toStream(network).flatMap(net -> net.children(NlNetwork::getEntities));
		return declarations.filter(decl -> decl.node().getInstanceName().equals(instanceName)).findFirst();
	}

	private static <T> Stream<T> toStream(Optional<T> optional) {
		return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
	}
}
