package se.lth.cs.tycho.instantiation.net;

import se.lth.cs.tycho.instance.Instance;
import se.lth.cs.tycho.instance.InstanceThunk;
import se.lth.cs.tycho.instance.net.Connection;
import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.IRNode.Identifier;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
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
import se.lth.cs.tycho.values.TypeThunk;
import se.lth.cs.tycho.values.ValueThunk;

public class NetworkInstantiator {
	private final DeclarationLoader loader;
	private final Visitor visitor;

	public NetworkInstantiator(DeclarationLoader loader) {
		this.loader = loader;
		this.visitor = new Visitor();
	}

	public Instance instantiate(GlobalEntityDecl decl, ImmutableList<Parameter<TypeThunk>> typeParameters,
			ImmutableList<Parameter<ValueThunk>> valueParameters) {
		NamespaceDecl ns = loader.getLocation(decl);
		return decl.getEntity().accept(visitor, new Context(typeParameters, valueParameters, ns));
	}

	private class Visitor implements EntityVisitor<Instance, Context> {
		@Override
		public Instance visitCalActor(CalActor entity, Context param) {
			return new InstanceThunk(entity, param.valueParameters, param.typeParameters, param.location);
		}

		@Override
		public Instance visitNlNetwork(NlNetwork entity, Context param) {
			throw new UnsupportedOperationException("Instantiation of NL networks are not supported.");
		}

		@Override
		public Instance visitGlobalEntityReference(GlobalEntityReference entity, Context param) {
			if (entity.isNamespaceReference()) {
				throw new Error("Can not create an entity instance of a global namespace reference.");
			}
			GlobalEntityDecl decl = loader.loadEntity(entity.getQualifiedIdentifier(), param.location);
			return instantiate(decl, param.typeParameters, param.valueParameters);
		}

		@Override
		public Instance visitXDFNetwork(XDFNetwork entity, Context param) {
			ImmutableList.Builder<Node> nodeBuilder = ImmutableList.builder();
			for (XDFInstance inst : entity.getInstances()) {
				GlobalEntityDecl decl = loader.loadEntity(inst.getEntity(), param.location);
				Instance result = instantiate(decl, ImmutableList.empty(), ImmutableList.empty());
				nodeBuilder.add(new Node(inst.getName(), result, ImmutableList.empty()));
			}
			ImmutableList<Node> nodes = nodeBuilder.build();
			ImmutableList.Builder<Connection> connBuilder = ImmutableList.builder();
			for (XDFConnection conn : entity.getConnections()) {
				Identifier src = findNode(conn.getSourceInstance(), nodes).getIdentifier();
				Identifier dst = findNode(conn.getDestinaitonInstance(), nodes).getIdentifier();
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

		private Node findNode(String name, ImmutableList<Node> nodes) {
			for (Node node : nodes) {
				if (node.getName().equals(name)) {
					return node;
				}
			}
			return null;
		}
	}

	private static class Context {
		private final ImmutableList<Parameter<TypeThunk>> typeParameters;
		private final ImmutableList<Parameter<ValueThunk>> valueParameters;
		private final NamespaceDecl location;

		public Context(ImmutableList<Parameter<TypeThunk>> typeParameters,
				ImmutableList<Parameter<ValueThunk>> valueParameters, NamespaceDecl location) {
			this.typeParameters = typeParameters;
			this.valueParameters = valueParameters;
			this.location = location;
		}

	}

}
