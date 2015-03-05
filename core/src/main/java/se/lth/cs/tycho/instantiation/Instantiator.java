package se.lth.cs.tycho.instantiation;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFConnection;
import se.lth.cs.tycho.ir.entity.xdf.XDFInstance;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.loader.AmbiguityException;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.transform.caltoam.CalActorStates;
import se.lth.cs.tycho.transform.copy.Copy;
import se.lth.cs.tycho.transform.reduction.ControllerWrapper;

public class Instantiator {
	private final DeclarationLoader loader;
	private final Visitor visitor;
	private final ActorToActorMachine translator;

	public Instantiator(DeclarationLoader loader) {
		this(loader, Collections.emptyList());
	}

	public Instantiator(DeclarationLoader loader,
			List<ControllerWrapper<CalActorStates.State, CalActorStates.State>> stateTransformers) {
		this.loader = loader;
		this.visitor = new Visitor();
		this.translator = new ActorToActorMachine(stateTransformers);
	}

	/**
	 * Instantiates the entity into the namespace declaration.
	 * 
	 * @param entity
	 *            the entity to instantiate
	 * @param location
	 *            the namespace declaration from where the instance is created
	 * @return an instance of the specified entity
	 */
	public Instance instantiate(Entity entity, NamespaceDecl location, QID instanceId) {
		Entity copy = entity.accept(Copy.transformer(), null);
		return copy.accept(visitor, new Data(location, instanceId));
	}

	/**
	 * Loads and instantiates the entity with the specified qualified identifier
	 * into the namespace declaration location.
	 * 
	 * @param entityId
	 *            the entity to instantiate
	 * @param location
	 *            the namespace declaration from where the instance is created
	 * @return an instance of the specified entity
	 * @throws AmbiguityException
	 *             if there is more than one entity available
	 */
	public Instance instantiate(QID entityId, NamespaceDecl location, QID instanceId) throws AmbiguityException {
		EntityDecl decl = loader.loadEntity(entityId, location);
		return instantiate(decl.getEntity(), loader.getLocation(decl), instanceId);
	}

	private class Visitor implements EntityVisitor<Instance, Data> {
		@Override
		public Instance visitCalActor(CalActor entity, Data data) {
			if (!entity.getValueParameters().isEmpty() || !entity.getTypeParameters().isEmpty()) {
				throw new UnsupportedOperationException("Can not instantiate an actor with parameters.");
			}
			ActorMachine result = translator.translate(entity, data.location, data.instanceId);
			return result;
		}

		@Override
		public Instance visitNlNetwork(NlNetwork entity, Data data) {
			throw new UnsupportedOperationException("Nl networks are not yet supported.");
		}

		@Override
		public Instance visitXDFNetwork(XDFNetwork entity, Data data) {
			ImmutableList.Builder<Node> nodeBuilder = ImmutableList.builder();
			for (XDFInstance inst : entity.getInstances()) {
				Instance result;
				try {
					String name = inst.getName();
					result = instantiate(inst.getEntity(), data.location, data.instanceId.concat(QID.of(name)));
				} catch (AmbiguityException e) {
					throw new RuntimeException(e);
				}
				nodeBuilder.add(new Node(inst.getName(), result, ImmutableList.empty()));
			}
			ImmutableList<Node> nodes = nodeBuilder.build();
			ImmutableList.Builder<Connection> connBuilder = ImmutableList.builder();
			for (XDFConnection conn : entity.getConnections()) {
				Identifier src = findNode(conn.getSourceInstance(), nodes);
				Identifier dst = findNode(conn.getDestinaitonInstance(), nodes);
				connBuilder.add(new Connection(src, conn.getSourcePort(), dst, conn.getDestinationPort(),
						conn.getToolAttributes()));
			}
			ImmutableList.Builder<PortDecl> inputPorts = ImmutableList.builder();
			for (PortDecl in : entity.getInputPorts()) {
				inputPorts.add(in.copy(in.getName(), in.getType()));
			}
			ImmutableList.Builder<PortDecl> outputPorts = ImmutableList.builder();
			for (PortDecl out : entity.getOutputPorts()) {
				outputPorts.add(out.copy(out.getName(), out.getType()));
			}
			return new Network(nodes, connBuilder.build(), inputPorts.build(), outputPorts.build());
		}

		private Identifier findNode(String name, List<Node> nodes) {
			if (name == null || name.isEmpty()) {
				return null;
			}
			for (Node node : nodes) {
				if (node.getName().equals(name)) {
					return node.getIdentifier();
				}
			}
			throw new RuntimeException("Unknown node: " + name);
		}

	}
	private static class Data {

		private final NamespaceDecl location;
		private final QID instanceId;

		public Data(NamespaceDecl location, QID instanceId) {
			this.location = location;
			this.instanceId = instanceId;
		}

	}

}
