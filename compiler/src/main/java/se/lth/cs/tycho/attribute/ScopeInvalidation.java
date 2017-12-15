package se.lth.cs.tycho.attribute;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Scope;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.expr.ExprInput;
import se.lth.cs.tycho.ir.stmt.StmtConsume;
import se.lth.cs.tycho.ir.stmt.StmtRead;
import se.lth.cs.tycho.util.BitSets;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class ScopeInvalidation {
	private final ActorMachine actorMachine;
	private final ActorMachineScopes scopes;
	private final List<Set<String>> portsConsumedFromByTransitions;
	private final Map<String, BitSet> scopesReadingFromPort;
	private final Map<Scope, Integer> scopeIndex;
	private final ScopeDependencies scopeDependencies;


	public ScopeInvalidation(ActorMachine actorMachine, ActorMachineScopes scopes, ScopeDependencies scopeDependencies) {
		this.actorMachine = actorMachine;
		this.scopes = scopes;
		this.scopeDependencies = scopeDependencies;
		portsConsumedFromByTransitions = new ArrayList<>();
		for (Transition t : actorMachine.getTransitions()) {
			Set<String> inputPorts = new HashSet<>();
			collectPortsConsumedFrom(t, inputPorts::add);
			portsConsumedFromByTransitions.add(inputPorts);
		}
		{
			scopesReadingFromPort = new HashMap<>();
			int i = 0;
			for (Scope s : actorMachine.getScopes()) {
				Set<String> ports = new HashSet<>();
				collectPortsReadByScope(s, ports::add);
				for (String p : ports) {
					scopesReadingFromPort.computeIfAbsent(p, x -> new BitSet()).set(i);
				}
				i = i + 1;
			}
		}
		{
			scopeIndex = new HashMap<>();
			int i = 0;
			for (Scope s : actorMachine.getScopes()) {
				scopeIndex.put(s, i);
				i = i + 1;
			}
		}
	}

	public BitSet killed(Instruction i) {
		return i.accept(
				exec -> killedByExec(exec),
				test -> new BitSet(),
				wait -> new BitSet()
		);
	}

	private BitSet killedByExec(Exec exec) {
		BitSet changedScopes = directlyAffectedScopes(exec);
		BitSet affectedScopes = changedScopes.stream()
				.mapToObj(this::scopeDependencies)
				.reduce(changedScopes, BitSets::union);
		return BitSets.intersection(affectedScopes, scopes.transientScopes(actorMachine));
	}

	private BitSet directlyAffectedScopes(Exec exec) {
		BitSet result = scopes.required(actorMachine, exec);
		for (String p : updatedInputPorts(exec)) {
			result = BitSets.union(result, dependsOnInputPort(p));
		}
		return result;
	}

	private BitSet scopeDependencies(int s) {
		Scope scope = actorMachine.getScopes().get(s);
		Set<Scope> deps = scopeDependencies.ofScope(scope);
		IntStream scopes = deps.stream().mapToInt(scopeIndex::get);
		return BitSets.collect(scopes);
	}

	private BitSet dependsOnInputPort(String port) {
		return scopesReadingFromPort.getOrDefault(port, new BitSet());
	}

	private Set<String> updatedInputPorts(Exec exec) {
		return portsConsumedFromByTransitions.get(exec.transition());
	}

	private void collectPortsConsumedFrom(IRNode node, Consumer<String> ports) {
		if (node instanceof StmtConsume) {
			ports.accept(((StmtConsume) node).getPort().getName());
		} else if (node instanceof StmtRead) {
			ports.accept(((StmtRead) node).getPort().getName());
		} else if (node != null) {
			node.forEachChild(n -> collectPortsConsumedFrom(n, ports));
		}
	}

	private void collectPortsReadByScope(IRNode node, Consumer<String> ports) {
		if (node instanceof ExprInput) {
			ports.accept(((ExprInput) node).getPort().getName());
		} else {
			node.forEachChild(n -> collectPortsReadByScope(n, ports));
		}
	}
}
