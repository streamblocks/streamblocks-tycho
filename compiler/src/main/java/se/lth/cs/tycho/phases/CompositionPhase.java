package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.*;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.composition.Composer;
import se.lth.cs.tycho.phases.composition.Connection;
import se.lth.cs.tycho.phases.composition.SourcePort;
import se.lth.cs.tycho.phases.composition.TargetPort;
import se.lth.cs.tycho.phases.reduction.MergeStates;
import se.lth.cs.tycho.phases.reduction.TransformedController;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositionPhase implements Phase {
	@Override
	public String getDescription() {
		return "Performes actor composition.";
	}

	public static final OnOffSetting eagerTestSetting = new OnOffSetting() {

		@Override
		public String getKey() {
			return "composition-eager-test";
		}

		@Override
		public String getDescription() {
			return "Performs eager test in composition.";
		}

		@Override
		public Boolean defaultValue(Configuration configuration) {
			return false;
		}
	};

	static final OnOffSetting actorComposition = new OnOffSetting() {
		@Override
		public String getKey() {
			return "actor-composition";
		}

		@Override
		public String getDescription() {
			return "Enables actor composition.";
		}

		@Override
		public Boolean defaultValue(Configuration configuration) {
			return true;
		}
	};

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Arrays.asList(actorComposition, eagerTestSetting);
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (!context.getConfiguration().get(actorComposition)) {
			return task;
		}
		Map<String, List<se.lth.cs.tycho.ir.network.Connection>> compositions =
				task.getNetwork().getConnections().stream()
						.filter(this::hasCompositionId)
						.collect(Collectors.groupingBy(this::compositionId));

		ImmutableList.Builder<GlobalEntityDecl> composedEntities = ImmutableList.builder();
		for (String compositionId : compositions.keySet()) {
			task = task.withNetwork(compose(task, compositions.get(compositionId), composedEntities, compositionId, context));
		}
		SourceUnit compUnit = new SyntheticSourceUnit(new NamespaceDecl(QID.empty(), null, null, composedEntities.build(), null));
		return task.withSourceUnits(ImmutableList.<SourceUnit> builder().addAll(task.getSourceUnits()).add(compUnit).build());
	}

	private Network compose(CompilationTask task, List<se.lth.cs.tycho.ir.network.Connection> connections, Consumer<GlobalEntityDecl> composedEntities, String compositionId, Context context) {
		assert connections.stream().allMatch(c -> c.getSource().getInstance().isPresent() && c.getTarget().getInstance().isPresent()) : "Cannot compose connections to network border.";
		Set<String> instanceNameSet = connections.stream()
				.flatMap(connection -> Stream.of(connection.getSource(), connection.getTarget()))
				.map(se.lth.cs.tycho.ir.network.Connection.End::getInstance)
				.map(Optional::get)
				.collect(Collectors.toSet());

		List<Instance> instances = task.getNetwork().getInstances().stream()
				.filter(instance -> instanceNameSet.contains(instance.getInstanceName()))
				.collect(Collectors.toList());

		List<String> instanceNames = instances.stream()
				.map(Instance::getInstanceName)
				.collect(Collectors.toList());

		List<ActorMachine> actorMachines = instances.stream()
                .map(Instance::getEntityName)
				.map(name -> getActorMachine(task, name))
				.collect(Collectors.toList());

		List<Connection> compositionConnections = connections.stream()
				.map(connection -> {
					se.lth.cs.tycho.ir.network.Connection.End src = connection.getSource();
					se.lth.cs.tycho.ir.network.Connection.End tgt = connection.getTarget();
					return new Connection(
							new SourcePort(instanceNames.indexOf(src.getInstance().get()), src.getPort()),
							new TargetPort(instanceNames.indexOf(tgt.getInstance().get()), tgt.getPort()),
							bufferSize(connection));
				})
				.collect(Collectors.toList());

		List<ValueParameter> parameters = instances.stream()
				.flatMap(instance -> instance.getValueParameters().stream())
				.collect(Collectors.toList());

		ActorMachine composition = new Composer(actorMachines, compositionConnections, context).compose().deepClone();
		composition = composition.withController(TransformedController.from(composition.controller(), Stream.generate(MergeStates::new).limit(10).collect(Collectors.toList())));
		String compositionInstanceName = uniqueInstanceName(task.getNetwork(), compositionId);
		String originalEntityName = compositionId;
		String compositionEntityName = compositionId + "_" + context.getUniqueNumbers().next();
		composedEntities.accept(GlobalEntityDecl.global(Availability.PUBLIC, originalEntityName, composition).withName(compositionEntityName));
		Stream<Instance> notComposed = task.getNetwork().getInstances().stream().filter(instance -> !instanceNameSet.contains(instance.getInstanceName()));
		Stream<Instance> composed = Stream.of(new Instance(compositionInstanceName, QID.of(compositionEntityName), parameters, null));
		List<Instance> resultInstances = Stream.concat(notComposed, composed).collect(Collectors.toList());

		List<Map<String, String>> inputPortMaps = getPortMap(actorMachines, composition, ActorMachine::getInputPorts);
		List<Map<String, String>> outputPortMaps = getPortMap(actorMachines, composition, ActorMachine::getOutputPorts);

		List<se.lth.cs.tycho.ir.network.Connection> resultConnections = new ArrayList<>();
		for (se.lth.cs.tycho.ir.network.Connection connection : task.getNetwork().getConnections()) {
			se.lth.cs.tycho.ir.network.Connection.End src = connection.getSource();
			if (src.getInstance().isPresent() && instanceNameSet.contains(src.getInstance().get())) {
				src = src.withInstance(Optional.of(compositionInstanceName))
						.withPort(outputPortMaps.get(instanceNames.indexOf(src.getInstance().get())).get(src.getPort()));
			}
			se.lth.cs.tycho.ir.network.Connection.End tgt = connection.getTarget();
			if (tgt.getInstance().isPresent() && instanceNameSet.contains(tgt.getInstance().get())) {
				tgt = tgt.withInstance(Optional.of(compositionInstanceName))
						.withPort(inputPortMaps.get(instanceNames.indexOf(tgt.getInstance().get())).get(tgt.getPort()));
			}
			se.lth.cs.tycho.ir.network.Connection rerouted = connection.withSource(src).withTarget(tgt);
			resultConnections.add(rerouted);
		}

		return task.getNetwork().withConnections(resultConnections).withInstances(resultInstances);
	}

	private String uniqueInstanceName(Network network, String base) {
		Set<String> names = network.getInstances().stream()
				.map(Instance::getInstanceName)
				.collect(Collectors.toSet());
		String result = base;
		int i = 1;
		while (names.contains(result)) {
			result = String.format("%s_%d", base, i);
			i = i + 1;
		}
		return result;
	}

	private List<Map<String, String>> getPortMap(List<ActorMachine> actorMachines, ActorMachine composition, Function<ActorMachine, List<PortDecl>> getPorts) {
		List<Map<String, String>> portNameMaps = new ArrayList<>();
		int portIndex = 0;
		for (ActorMachine actorMachine : actorMachines) {
			Map<String, String> portNameMap = new HashMap<>();
			for (PortDecl port : getPorts.apply(actorMachine)) {
				portNameMap.put(port.getName(), getPorts.apply(composition).get(portIndex).getName());
				portIndex += 1;
			}
			portNameMaps.add(portNameMap);
		}
		return portNameMaps;
	}

	private int bufferSize(Attributable connection) {
		return ((ExprLiteral) connection.getValueAttribute("buffersize").get().getValue()).asInt().getAsInt();
	}

	private ActorMachine getActorMachine(CompilationTask task, QID name) {
		GlobalEntityDecl decl = GlobalDeclarations.getEntity(task, name);
		assert decl.getEntity() instanceof ActorMachine : "Can only compose actor machines.";
		return (ActorMachine) decl.getEntity();
	}


	private boolean hasCompositionId(se.lth.cs.tycho.ir.network.Connection connection) {
		return connection.getValueAttribute("composition").isPresent();
	}
	private String compositionId(se.lth.cs.tycho.ir.network.Connection connection) {
		Optional<ToolValueAttribute> attribute = connection.getValueAttribute("composition");
		assert attribute.isPresent();
		Expression value = attribute.get().getValue();
		assert value instanceof ExprLiteral : "Composition attribute must be a constant.";
		ExprLiteral literal = (ExprLiteral) value;
		Optional<String> string = literal.asString();
		assert string.isPresent() : "Composition attribute must be a string.";
		return string.get();
	}

}
