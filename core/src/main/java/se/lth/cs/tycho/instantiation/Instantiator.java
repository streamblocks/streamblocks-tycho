package se.lth.cs.tycho.instantiation;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.EntityVisitor;
import se.lth.cs.tycho.ir.entity.GlobalEntityReference;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.xdf.XDFConnection;
import se.lth.cs.tycho.ir.entity.xdf.XDFInstance;
import se.lth.cs.tycho.ir.entity.xdf.XDFNetwork;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.loader.DeclarationLoader;
import se.lth.cs.tycho.transform.caltoam.ActorStates.State;
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.transform.util.ActorMachineState.Transformer;
import se.lth.cs.tycho.values.Type;
import se.lth.cs.tycho.values.Value;

public class Instantiator {
	private final DeclarationLoader loader;
	private final Visitor visitor;
	private final ActorToActorMachine translator;

	public Instantiator(DeclarationLoader loader) {
		this(loader, Collections.emptyList());
	}

	public Instantiator(DeclarationLoader loader, List<Transformer<State, State>> stateTransformers) {
		this.loader = loader;
		this.visitor = new Visitor();
		this.translator = new ActorToActorMachine(stateTransformers);
	}

	public Instance instantiate(QID qid, List<Parameter<Type>> typeParameters, List<Parameter<Value>> valueParameters) {
		return instantiate(qid, typeParameters, valueParameters, null);
	}

	public Instance instantiate(QID qid, List<Parameter<Type>> typeParameters, List<Parameter<Value>> valueParameters,
			NamespaceDecl location) {
		GlobalEntityDecl decl = loader.loadEntity(qid, location);
		return instantiate(decl.getEntity(), typeParameters, valueParameters, loader.getLocation(decl));
	}

	public Instance instantiate(Entity entity, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters) {
		return instantiate(entity, typeParameters, valueParameters, null);
	}

	public Instance instantiate(Entity entity, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters, NamespaceDecl location) {
		return entity.accept(visitor, new Context(typeParameters, valueParameters, location));
	}

	private class Visitor implements EntityVisitor<Instance, Context> {
		@Override
		public Instance visitCalActor(CalActor entity, Context param) {
			return translator.translate(entity);
		}

		@Override
		public Instance visitNlNetwork(NlNetwork entity, Context param) {
			throw new UnsupportedOperationException("Nl networks are not yet supported.");
		}

		@Override
		public Instance visitGlobalEntityReference(GlobalEntityReference entity, Context param) {
			if (entity.isNamespaceReference()) {
				throw new Error("Can not create an entity instance of a global namespace reference.");
			}
			return instantiate(entity.getQualifiedIdentifier(), param.typeParameters, param.valueParameters,
					param.location);
		}

		@Override
		public Instance visitXDFNetwork(XDFNetwork entity, Context param) {
			ImmutableList.Builder<Node> nodeBuilder = ImmutableList.builder();
			for (XDFInstance inst : entity.getInstances()) {
				Instance result = instantiate(inst.getEntity(), ImmutableList.empty(), ImmutableList.empty(),
						param.location);
				nodeBuilder.add(new Node(inst.getName(), result, ImmutableList.empty()));
			}
			ImmutableList<Node> nodes = nodeBuilder.build();
			ImmutableList.Builder<Connection> connBuilder = ImmutableList.builder();
			for (XDFConnection conn : entity.getConnections()) {
				Identifier src = findNode(conn.getSourceInstance(), nodes);
				Identifier dst = findNode(conn.getDestinaitonInstance(), nodes);
				connBuilder.add(new Connection(src, conn.getSourcePort(), dst, conn.getDestinationPort(),
						ImmutableList.empty()));
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
				if (node.getName()
						.equals(name)) {
					return node.getIdentifier();
				}
			}
			throw new RuntimeException("Unknown node: " + name);
		}

	}

	private static class Context {
		private final List<Parameter<Type>> typeParameters;
		private final List<Parameter<Value>> valueParameters;
		private final NamespaceDecl location;

		public Context(List<Parameter<Type>> typeParameters, List<Parameter<Value>> valueParameters,
				NamespaceDecl location) {
			this.typeParameters = typeParameters;
			this.valueParameters = valueParameters;
			this.location = location;
		}

	}

}
