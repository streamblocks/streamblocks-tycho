package se.lth.cs.tycho.instantiation;

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
import se.lth.cs.tycho.transform.caltoam.ActorToActorMachine;
import se.lth.cs.tycho.values.Type;
import se.lth.cs.tycho.values.Value;

public class Instantiator {
	private final DeclarationLoader loader;
	private final Visitor visitor;
	private final ActorToActorMachine translator;

	public Instantiator(DeclarationLoader loader) {
		this.loader = loader;
		this.visitor = new Visitor();
		this.translator = new ActorToActorMachine();
	}

	public Instance instantiate(QID qid, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters) {
		return instantiate(qid, typeParameters, valueParameters, null);
	}

	public Instance instantiate(QID qid, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters, NamespaceDecl location) {
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

	protected Instance instantiateCalActor(CalActor entity, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters, NamespaceDecl location) {
		return translator.translate(entity);
	}

	protected Instance instantiateNlNetwork(NlNetwork entity, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters, NamespaceDecl location) {
		throw new UnsupportedOperationException("Nl networks are not yet supported.");
	}

	protected Instance instantiateXDFNetwork(XDFNetwork entity, List<Parameter<Type>> typeParameters,
			List<Parameter<Value>> valueParameters, NamespaceDecl location) {
		ImmutableList.Builder<Node> nodeBuilder = ImmutableList.builder();
		for (XDFInstance inst : entity.getInstances()) {
			Instance result = instantiate(inst.getEntity(), ImmutableList.empty(), ImmutableList.empty(), location);
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

	protected Instance instantiateGlobalEntityReference(GlobalEntityReference entity,
			List<Parameter<Type>> typeParameters, List<Parameter<Value>> valueParameters,
			NamespaceDecl location) {
		if (entity.isNamespaceReference()) {
			throw new Error("Can not create an entity instance of a global namespace reference.");
		}
		return instantiate(entity.getQualifiedIdentifier(), typeParameters, valueParameters, location);
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

	private class Visitor implements EntityVisitor<Instance, Context> {
		@Override
		public Instance visitCalActor(CalActor entity, Context param) {
			return instantiateCalActor(entity, param.typeParameters, param.valueParameters, param.location);
		}

		@Override
		public Instance visitNlNetwork(NlNetwork entity, Context param) {
			return instantiateNlNetwork(entity, param.typeParameters, param.valueParameters, param.location);
		}

		@Override
		public Instance visitGlobalEntityReference(GlobalEntityReference entity, Context param) {
			return instantiateGlobalEntityReference(entity, param.typeParameters, param.valueParameters, param.location);
		}

		@Override
		public Instance visitXDFNetwork(XDFNetwork entity, Context param) {
			return instantiateXDFNetwork(entity, param.typeParameters, param.valueParameters, param.location);
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
