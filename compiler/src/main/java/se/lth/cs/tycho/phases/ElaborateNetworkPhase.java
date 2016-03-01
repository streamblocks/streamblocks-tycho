package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.GlobalDeclarations;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.ToolAttribute;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityExpr;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.ir.entity.nl.PortReference;
import se.lth.cs.tycho.ir.entity.nl.StructureConnectionStmt;
import se.lth.cs.tycho.ir.entity.nl.StructureStatement;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElaborateNetworkPhase implements Phase {
	@Override
	public String getDescription() {
		return "Elaborates the entities that are networks.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.withNetwork(fullyElaborate(task, task.getNetwork(), new HashSet<>()));
	}

	public Network fullyElaborate(CompilationTask task, Network network, Set<String> names) {
		Network result = uniqueNames(network, names);
		for (Instance instance : result.getInstances()) {
			EntityDecl entity = GlobalDeclarations.getEntity(task, QID.of(instance.getEntityName()));
			if (entity.getEntity() instanceof NlNetwork) {
				Network elaborated = elaborate((NlNetwork) entity.getEntity());
				elaborated = fullyElaborate(task, elaborated, names);
				result = connectElaboratedInstance(result, instance.getInstanceName(), elaborated);
			}
		}
		return result;
	}

	private Network uniqueNames(Network network, Set<String> names) {
		Map<String, String> dictionary = new HashMap<>();
		for (Instance instance : network.getInstances()) {
			String name = instance.getInstanceName();
			int i = 0;
			while (names.contains(name)) {
				name = instance.getInstanceName() + "_" + i++;
			}
			dictionary.put(instance.getInstanceName(), name);
			names.add(name);
		}
		ImmutableList<Instance> instances = network.getInstances().stream()
				.map(instance -> instance.withName(dictionary.get(instance.getInstanceName())))
				.collect(ImmutableList.collector());
		ImmutableList<Connection> connections = network.getConnections().stream()
				.map(connection -> {
					Connection.End src = connection.getSource().withInstance(
							connection.getSource().getInstance().map(dictionary::get));
					Connection.End tgt = connection.getTarget().withInstance(
							connection.getTarget().getInstance().map(dictionary::get));
					return connection.copy(src, tgt);
				}).collect(ImmutableList.collector());
		return network.withInstances(instances).withConnections(connections);
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Collections.singleton(CreateNetworkPhase.class);
	}

	private Network connectElaboratedInstance(Network outer, String instanceName, Network inner) {
		assert inner.getConnections().stream().noneMatch(c ->
				!c.getSource().getInstance().isPresent() &&
						!c.getTarget().getInstance().isPresent());

		assert inner.getInstances().stream()
				.map(Instance::getInstanceName)
				.noneMatch(outer.getInstances().stream()
						.map(Instance::getInstanceName)
						.collect(Collectors.toSet())::contains);

		Map<String, List<Connection>> incoming = inner.getConnections().stream()
				.filter(c -> !c.getSource().getInstance().isPresent())
				.collect(Collectors.groupingBy(c -> c.getSource().getPort()));
		Map<String, List<Connection>> outgoing = inner.getConnections().stream()
				.filter(c -> !c.getTarget().getInstance().isPresent())
				.collect(Collectors.groupingBy(c -> c.getTarget().getPort()));

		ImmutableList.Builder<Connection> builder = ImmutableList.builder();

		for (Connection connOuter : outer.getConnections()) {
			if (connOuter.getTarget().getInstance().equals(Optional.of(instanceName))) {
				Connection.End src = connOuter.getSource();
				String port = connOuter.getTarget().getPort();
				for (Connection connInner : incoming.getOrDefault(port, Collections.emptyList())) {
					Connection.End tgt = connInner.getTarget();
					builder.add(new Connection(src, tgt).withAttributes(mergeAttributes(connOuter, connInner)));
				}
			} else if (connOuter.getSource().getInstance().equals(Optional.of(instanceName))) {
				Connection.End tgt = connOuter.getTarget();
				String port = connOuter.getSource().getPort();
				for (Connection connInner : outgoing.getOrDefault(port, Collections.emptyList())) {
					Connection.End src = connInner.getSource();
					builder.add(new Connection(src, tgt).withAttributes(mergeAttributes(connOuter, connInner)));
				}
			} else {
				builder.add(connOuter);
			}
		}
		for (Connection connInner : inner.getConnections()) {
			if (connInner.getSource().getInstance().isPresent() && connInner.getTarget().getInstance().isPresent()) {
				builder.add(connInner);
			}
		}

		Stream<Instance> outerInstances = outer.getInstances().stream()
				.filter(instance -> !instance.getInstanceName().equals(instanceName));
		Stream<Instance> innerInstances = inner.getInstances().stream();

		ImmutableList<Instance> instances = Stream.concat(outerInstances, innerInstances)
				.collect(ImmutableList.collector());

		return new Network(outer.getInputPorts(), outer.getOutputPorts(), instances, builder.build());
	}

	private List<ToolAttribute> mergeAttributes(Connection connSrc, Connection connTgt) {
		ImmutableList<ToolAttribute> attributes = ImmutableList.concat(connSrc.getAttributes(), connTgt.getAttributes());
		long count = attributes.stream()
				.map(ToolAttribute::getName)
				.distinct()
				.count();
		assert count == attributes.size();
		return attributes;
	}

	private Network elaborate(NlNetwork network) {
		assert network.getValueParameters().isEmpty();
		assert network.getTypeParameters().isEmpty();
		assert network.getVarDecls().isEmpty();

		ImmutableList<PortDecl> inputPorts = network.getInputPorts().map(PortDecl::deepClone);
		ImmutableList<PortDecl> outputPorts = network.getOutputPorts().map(PortDecl::deepClone);

		ImmutableList.Builder<Instance> instances = ImmutableList.builder();
		for (Map.Entry<String, EntityExpr> entity : network.getEntities()) {
			assert entity.getValue() instanceof EntityInstanceExpr;
			EntityInstanceExpr expr = (EntityInstanceExpr) entity.getValue();
			Instance instance = new Instance(
					entity.getKey(),
					expr.getEntityName(),
					expr.getParameterAssignments().map(entry -> Parameter.of(entry.getKey(), entry.getValue().deepClone())),
					ImmutableList.empty())
					.withAttributes(expr.getAttributes().map(ToolAttribute::deepClone));
			instances.add(instance);
		}

		ImmutableList.Builder<Connection> connections = ImmutableList.builder();
		for (StructureStatement stmt : network.getStructure()) {
			assert stmt instanceof StructureConnectionStmt;
			StructureConnectionStmt conn = (StructureConnectionStmt) stmt;
			assert conn.getSrc().getEntityIndex().isEmpty();
			assert conn.getDst().getEntityIndex().isEmpty();
			Connection connection = new Connection(convert(conn.getSrc()), convert(conn.getDst()))
					.withAttributes(conn.getAttributes().map(ToolAttribute::deepClone));
			connections.add(connection);
		}
		return new Network(inputPorts, outputPorts, instances.build(), connections.build());
	}

	private Connection.End convert(PortReference portReference) {
		return new Connection.End(Optional.ofNullable(portReference.getEntityName()), portReference.getPortName());
	}
}
