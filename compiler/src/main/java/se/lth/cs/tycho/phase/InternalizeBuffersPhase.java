package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.*;
import se.lth.cs.tycho.ir.*;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.network.Network;
import se.lth.cs.tycho.ir.stmt.*;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InternalizeBuffersPhase implements Phase {

	private static final String description = "Makes self-looping, condition-free, single-token connections internal.";

	@Override
	public String getDescription() {
		return description;
	}

	private static final OnOffSetting internalizeBuffers = new OnOffSetting() {
		@Override
		public String getKey() {
			return "internalize-buffers";
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Boolean defaultValue(Configuration configuration) {
			return true;
		}
	};

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(internalizeBuffers);
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		if (!context.getConfiguration().get(internalizeBuffers)) {
			return task;
		}
		Map<String, List<Connection>> selfLoops = task.getNetwork().getConnections().stream()
				.filter(c -> c.getSource().getInstance().isPresent())
				.filter(c -> c.getSource().getInstance().equals(c.getTarget().getInstance()))
				.collect(Collectors.groupingBy(c -> c.getSource().getInstance().get()));
		ImmutableList.Builder<Instance> resultInstances = ImmutableList.builder();
		ImmutableList.Builder<GlobalEntityDecl> entities = ImmutableList.builder();
		ArrayList<Connection> connections = new ArrayList<>(task.getNetwork().getConnections());
		for (Instance instance : task.getNetwork().getInstances()) {
			if (selfLoops.containsKey(instance.getInstanceName())) {
				GlobalEntityDecl entity = GlobalDeclarations.getEntity(task, instance.getEntityName());
				List<Connection> conditionFreeAnySize = conditionFree((ActorMachine) entity.getEntity(), selfLoops.get(instance.getInstanceName()));
				List<Connection> conditionFree = singleToken(conditionFreeAnySize);
				if (conditionFree.isEmpty()) {
					resultInstances.add(instance);
				} else {
					connections.removeAll(conditionFree);
					ActorMachine internalized = internalize((ActorMachine) entity.getEntity(), conditionFree, context.getUniqueNumbers());
					String name = entity.getOriginalName() + "_" + context.getUniqueNumbers().next();
					entities.add(GlobalEntityDecl.global(Availability.PUBLIC, name, internalized));
					resultInstances.add(instance.withEntityName(QID.of(name)));
				}
			} else {
				resultInstances.add(instance);
			}
		}
		Network result = task.getNetwork()
				.withConnections(connections)
				.withInstances(resultInstances.build());
		SourceUnit unit = new SyntheticSourceUnit(new NamespaceDecl(QID.empty(), null, null, entities.build(), null));
		return task.withNetwork(result)
				.withSourceUnits(ImmutableList.<SourceUnit> builder().addAll(task.getSourceUnits()).add(unit).build());
	}

	private List<Connection> singleToken(List<Connection> connections) {
		return connections.stream()
				.filter(connection -> bufferSize(connection).equals(OptionalInt.of(1)))
				.collect(Collectors.toList());
	}
	private List<Connection> conditionFree(ActorMachine actorMachine, List<Connection> selfLoops) {
		List<PortCondition> conditions = actorMachine.controller().getStateList().stream()
				.map(State::getInstructions)
				.flatMap(List::stream)
				.filter(i -> i instanceof Test)
				.mapToInt(i -> ((Test) i).condition())
				.distinct()
				.mapToObj(actorMachine.getConditions()::get)
				.filter(c -> c instanceof PortCondition)
				.map(c -> (PortCondition) c)
				.collect(Collectors.toList());
		Set<String> conditionedInputPorts = conditions.stream()
				.filter(PortCondition::isInputCondition)
				.map(c -> c.getPortName().getName())
				.collect(Collectors.toSet());
		Set<String> conditionedOutputPorts = conditions.stream()
				.filter(c -> !c.isInputCondition())
				.map(c -> c.getPortName().getName())
				.collect(Collectors.toSet());
		return selfLoops.stream()
				.filter(connection -> !conditionedOutputPorts.contains(connection.getSource().getPort()))
				.filter(connection -> !conditionedInputPorts.contains(connection.getTarget().getPort()))
				.collect(Collectors.toList());
	}

	private ActorMachine internalize(ActorMachine actorMachine, List<Connection> connections, UniqueNumbers uniqueNumbers) {
		assert connections.stream().allMatch(c -> bufferSize(c).equals(OptionalInt.of(1)));
		assert connections.stream()
				.flatMap(c -> Stream.of(c.getSource(), c.getTarget()))
				.map(Connection.End::getInstance)
				.distinct()
				.count() == 1;

		ImmutableList<PortDecl> inputPorts = getPorts(actorMachine.getInputPorts(), connections, Connection::getTarget);
		ImmutableList<PortDecl> outputPorts = getPorts(actorMachine.getOutputPorts(), connections, Connection::getSource);

		Map<String, LocalVarDecl> outputPortMap = sourcePortVariables(actorMachine, connections, uniqueNumbers);
		Map<String, LocalVarDecl> inputPortMap = targetPortVariables(outputPortMap, connections);

		ImmutableList<Scope> scopes = transformScopes(actorMachine.getScopes(), inputPortMap, outputPortMap);
		ImmutableList<Transition> transitions = transformTransitions(actorMachine.getTransitions(), inputPortMap.keySet(), outputPortMap);
		return actorMachine.copy(
				inputPorts,
				outputPorts,
				actorMachine.getTypeParameters(),
				actorMachine.getValueParameters(),
				scopes, actorMachine.controller(),
				transitions,
				actorMachine.getConditions()).deepClone();
	}

	private ImmutableList<Scope> transformScopes(ImmutableList<Scope> scopes, Map<String, LocalVarDecl> inputPortMap, Map<String, LocalVarDecl> outputPortMap) {
		IRNode.Transformation transformation = new ExprInputToExprVariable(inputPortMap);
		ImmutableList<Scope> transformed = scopes.map(scope -> scope.transformChildren(transformation));
		Scope ports = new Scope(outputPortMap.values().stream().collect(ImmutableList.collector()), true);
		return ImmutableList.<Scope> builder().addAll(transformed).add(ports).build();
	}

	private ImmutableList<Transition> transformTransitions(ImmutableList<Transition> transitions, Set<String> inputPorts, Map<String, LocalVarDecl> outputPortMap) {
		IRNode.Transformation removeConsume = MultiJ.from(RemoveStmtConsume.class)
				.bind("inputPorts").to(inputPorts)
				.instance();
		List<Transition> withoutConsume = removeConsume.mapChecked(Transition.class, transitions);
		IRNode.Transformation writeToAssign = new WriteToAssign(outputPortMap);
		List<Transition> withoutWrite = writeToAssign.mapChecked(Transition.class, withoutConsume);
		return ImmutableList.from(withoutWrite);
	}

	private static class WriteToAssign implements IRNode.Transformation {
		private final Map<String, LocalVarDecl> outputPortMap;

		public WriteToAssign(Map<String, LocalVarDecl> outputPortMap) {
			this.outputPortMap = outputPortMap;
		}

		@Override
		public IRNode apply(IRNode node) {
			if (node instanceof StmtWrite) {
				StmtWrite write = (StmtWrite) node;
				VarDecl portVariable = outputPortMap.get(write.getPort().getName());
				if (portVariable != null) {
					return new StmtAssignment(new LValueVariable(Variable.variable(portVariable.getName())), write.getValues().get(0));
				}
			} else if (node instanceof Transition) {
				Transition transition = (Transition) node;
				Map<Port, Integer> outputRates = transition.getOutputRates().keySet().stream()
						.filter(port -> !outputPortMap.containsKey(port.getName()))
						.collect(Collectors.toMap(Function.identity(), transition.getOutputRates()::get));
				transition = transition.copy(transition.getInputRates(), outputRates, transition.getScopesToKill(), transition.getBody());
				transition = transition.transformChildren(this);
				return transition;
			}
			return node.transformChildren(this);
		}
	}

	private static class ExprInputToExprVariable implements IRNode.Transformation {
		private final Map<String, LocalVarDecl> inputPortMap;

		public ExprInputToExprVariable(Map<String, LocalVarDecl> inputPortMap) {
			this.inputPortMap = inputPortMap;
		}

		@Override
		public IRNode apply(IRNode node) {
			if (node instanceof ExprInput) {
				ExprInput input = (ExprInput) node;
				VarDecl portVariable = inputPortMap.get(input.getPort().getName());
				if (portVariable == null) {
					return node;
				} else {
					return new ExprVariable(Variable.variable(portVariable.getName()));
				}
			} else {
				return node.transformChildren(this);
			}
		}
	}

	@Module
	interface RemoveStmtConsume extends IRNode.Transformation {
		@Binding(BindingKind.INJECTED)
		Set inputPorts();

		@Override
		default IRNode apply(IRNode node) {
			return transform(node.transformChildren(this));
		}

		default IRNode transform(IRNode node) {
			return node;
		}

		default IRNode transform(StmtBlock block) {
			return block.withStatements(filterInternalConsume(block.getStatements()));
		}

		default IRNode transform(StmtIf conditional) {
			return conditional
					.withThenBranch(filterInternalConsume(conditional.getThenBranch()))
					.withElseBranch(filterInternalConsume(conditional.getElseBranch()));
		}

		default IRNode transform(StmtWhile stmtWhile) {
			return stmtWhile.withBody(filterInternalConsume(stmtWhile.getBody()));
		}

		default IRNode transform(StmtForeach foreach) {
			return foreach.withBody(filterInternalConsume(foreach.getBody()));
		}

		default IRNode transform(Transition transition) {
			Map<Port, Integer> inputRates = transition.getInputRates().keySet().stream()
					.filter(port -> !inputPorts().contains(port.getName()))
					.collect(Collectors.toMap(Function.identity(), transition.getInputRates()::get));
			return transition.copy(
					inputRates,
					transition.getOutputRates(),
					transition.getScopesToKill(),
					filterInternalConsume(transition.getBody()));
		}

		default ImmutableList<Statement> filterInternalConsume(ImmutableList<Statement> statements) {
			return statements.stream().filter(s -> !isInternalConsume(s)).collect(ImmutableList.collector());
		}

		default boolean isInternalConsume(Statement stmt) {
			return false;
		}

		default boolean isInternalConsume(StmtConsume consume) {
			return inputPorts().contains(consume.getPort().getName());
		}
	}

	private Map<String, LocalVarDecl> sourcePortVariables(ActorMachine actorMachine, List<Connection> connections, UniqueNumbers uniqueNumbers) {
		return connections.stream()
				.map(Connection::getSource)
				.map(Connection.End::getPort)
				.distinct() // one variable per port, irrespective of how many consumers
				.collect(Collectors.toMap(Function.identity(), port -> {
					TypeExpr type = actorMachine.getOutputPorts().stream().filter(p -> p.getName().equals(port)).findFirst().get().getType();
					return VarDecl.local(type, String.format("%s_%d", port, uniqueNumbers.next()), null, false);
				}));
	}

	private Map<String, LocalVarDecl> targetPortVariables(Map<String, LocalVarDecl> sourcePortVariables, List<Connection> connections) {
		return connections.stream()
				.collect(Collectors.toMap(c -> c.getTarget().getPort(), c -> sourcePortVariables.get(c.getSource().getPort())));
	}

	private ImmutableList<PortDecl> getPorts(List<PortDecl> ports, List<Connection> connections, Function<Connection, Connection.End> getEnd) {
		Set<String> internal = connections.stream().map(getEnd).map(Connection.End::getPort).collect(Collectors.toSet());
		return ports.stream()
				.filter(portDecl -> !internal.contains(portDecl.getName()))
				.collect(ImmutableList.collector());
	}

	private OptionalInt bufferSize(Connection connection) {
		Optional<ToolValueAttribute> attribute = connection.getValueAttribute("buffersize");
		if (attribute.isPresent()) {
			Expression expression = attribute.get().getValue();
			if (expression instanceof ExprLiteral) {
				return ((ExprLiteral) expression).asInt();
			}
		}
		return OptionalInt.empty();
	}
}
