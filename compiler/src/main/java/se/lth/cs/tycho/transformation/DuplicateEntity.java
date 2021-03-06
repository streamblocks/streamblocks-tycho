package se.lth.cs.tycho.transformation;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.compiler.SyntheticSourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.attribute.GlobalNames;

import java.util.*;
import java.util.function.Function;
import java.util.function.LongSupplier;

public final class DuplicateEntity {
	public DuplicateEntity() {}

	public static CompilationTask duplicateEntity(CompilationTask task, String instanceName, LongSupplier uniqueNumbers) {
		Instance instance = task.getNetwork().getInstances().stream()
				.filter(inst -> inst.getInstanceName().equals(instanceName))
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("No such entity instance"));

        GlobalEntityDecl original = task.getModule(GlobalNames.key).entityDecl(instance.getEntityName(),false);

		GlobalEntityDecl entity = original;

		String localName = entity.getOriginalName() + "_" + uniqueNumbers.getAsLong();
		QID namespace = instance.getEntityName().getButLast();
		QID globalName = namespace.concat(QID.of(localName));
		entity = (GlobalEntityDecl) RenameVariables.appendNumber(entity, d -> true, uniqueNumbers, task);
		entity = entity.withName(localName);
		entity = entity.withEntity(
				entity.getEntity()
						.withInputPorts(renamePorts(entity.getEntity().getInputPorts(), uniqueNumbers))
						.withOutputPorts(renamePorts(entity.getEntity().getOutputPorts(), uniqueNumbers)));

		Map<String, String> inputPorts = portNames(original.getEntity(), entity.getEntity(), Entity::getInputPorts);
		Map<String, String> outputPorts = portNames(original.getEntity(), entity.getEntity(), Entity::getOutputPorts);
		List<Connection> connections = task.getNetwork().getConnections().map(connection -> {
			if (connection.getSource().getInstance().equals(Optional.of(instanceName))) {
				connection = connection
						.withSource(connection.getSource()
								.withPort(outputPorts.get(connection.getSource().getPort())));
			}
			if (connection.getTarget().getInstance().equals(Optional.of(instanceName))) {
				connection = connection
						.withTarget(connection.getTarget()
								.withPort(inputPorts.get(connection.getTarget().getPort())));
			}
			return connection;
		});

		entity = renamePortUses(entity, inputPorts, outputPorts);

		Map<String, String> parameterNames = parameterNames(original.getEntity(), entity.getEntity());
		List<Instance> instances = task.getNetwork().getInstances().map(inst -> {
			if (inst.getInstanceName().equals(instanceName)) {
				return inst.withEntityName(globalName)
						.withValueParameters(inst.getValueParameters()
								.map(par -> par.withName(parameterNames.get(par.getName()))));
			} else {
				return inst;
			}
		});

		Network net = task.getNetwork()
				.withConnections(connections)
				.withInstances(instances);
		SourceUnit unit = new SyntheticSourceUnit(new NamespaceDecl(namespace, null, null, ImmutableList.of(entity.deepClone()), null));

		return task.withNetwork(net).withSourceUnits(ImmutableList.concat(task.getSourceUnits(), ImmutableList.of(unit)));

	}

	private static GlobalEntityDecl renamePortUses(GlobalEntityDecl entity, Map<String, String> inputPorts, Map<String, String> outputPorts) {
		Map<String, String> ports = new HashMap<>(inputPorts);
		ports.putAll(outputPorts);
		assert ports.size() == inputPorts.size() + outputPorts.size();
		IRNode.Transformation renamePorts = new IRNode.Transformation() {
			@Override
			public IRNode apply(IRNode node) {
				if (node instanceof Port) {
					Port port = (Port) node;
					return port.withName(ports.get(port.getName()));
				} else {
					return node.transformChildren(this);
				}
			}
		};
		return renamePorts.applyChecked(GlobalEntityDecl.class, entity);
	}

	private static ImmutableList<PortDecl> renamePorts(ImmutableList<PortDecl> ports, LongSupplier uniqueNumbers) {
		return ports.map(port -> port.withName(port.getName() + "_" + uniqueNumbers.getAsLong()));
	}

	private static Map<String, String> portNames(Entity original, Entity renamed, Function<Entity, List<PortDecl>> getPorts) {
		Map<String, String> names = new HashMap<>();
		Iterator<PortDecl> originalPorts = getPorts.apply(original).iterator();
		Iterator<PortDecl> renamedPorts = getPorts.apply(renamed).iterator();
		while (originalPorts.hasNext()) {
			assert renamedPorts.hasNext();
			PortDecl o = originalPorts.next();
			PortDecl r = renamedPorts.next();
			names.put(o.getName(), r.getName());
		}
		return names;
	}

	private static Map<String, String> parameterNames(Entity original, Entity renamed) {
		Map<String, String> names = new HashMap<>();
		Iterator<ParameterVarDecl> originalParameters = original.getValueParameters().iterator();
		Iterator<ParameterVarDecl> renamedParameters = renamed.getValueParameters().iterator();
		while (originalParameters.hasNext()) {
			assert renamedParameters.hasNext();
			VarDecl o = originalParameters.next();
			VarDecl r = renamedParameters.next();
			names.put(o.getName(), r.getName());
		}
		return names;
	}

}
