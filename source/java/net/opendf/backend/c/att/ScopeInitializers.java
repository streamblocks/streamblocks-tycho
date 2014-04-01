package net.opendf.backend.c.att;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javarag.FixedPointStart;
import javarag.FixedPointStep;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.Scope;
import net.opendf.ir.am.State;
import net.opendf.ir.am.Transition;
import net.opendf.ir.util.ImmutableList;

public class ScopeInitializers extends Module<ScopeInitializers.Required> {

	@Inherited
	public Set<Scope> persistentScopes(ActorMachine am) {
		Set<Scope> result = new HashSet<>();
		result.addAll(am.getScopes());
		for (Transition t : am.getTransitions()) {
			for (int s : t.getScopesToKill()) {
				result.remove(am.getScopes().get(s));
			}
		}
		return result;
	}

	// KILL

	@Synthesized
	public Set<Scope> scopesToKill(Instruction i) {
		return new HashSet<>();
	}

	@Synthesized
	public Set<Scope> scopesToKill(ICall c) {
		ActorMachine am = get().actorMachine(c);
		Transition t = am.getTransition(c.T());
		return get().scopesToKill(t);
	}

	@Synthesized
	public Set<Scope> scopesToKill(Transition t) {
		ActorMachine am = get().actorMachine(t);
		ImmutableList<Scope> scopes = am.getScopes();
		Set<Scope> kill = new HashSet<>();
		for (int k : t.getScopesToKill()) {
			kill.add(scopes.get(k));
		}
		return kill;
	}

	// ALIVE IN STATE

	@Inherited
	public boolean checkInitialState(ActorMachine am, State s) {
		return am.getController().get(0) == s;
	}

	@Synthesized
	public boolean isInitialState(State s) {
		return get().checkInitialState(s, s);
	}

	@FixedPointStart("aliveInState")
	@Synthesized
	public Set<Scope> aliveInStateStart(State s) {
		Set<Scope> persistentScopes = get().persistentScopes(s);
		return persistentScopes;
	}

	@FixedPointStep
	@Synthesized
	public Set<Scope> aliveInState(State s) {
		Set<Scope> alive = null;
		for (Instruction pred : get().predecessors(s)) {
			Set<Scope> after = get().aliveAfterInstruction(pred);
			if (alive == null) {
				alive = new HashSet<>();
				alive.addAll(after);
			} else {
				alive.retainAll(after);
			}
		}
		return alive == null ? get().persistentScopes(s) : alive;
	}

	// ALIVE AFTER INSTRUCTION

	@FixedPointStart("aliveAfterInstruction")
	@Synthesized
	public Set<Scope> aliveAfterInstructionStart(Instruction i) {
		Set<Scope> persistentScopes = get().persistentScopes(i);
		return persistentScopes;
	}

	@Synthesized
	@FixedPointStep
	public Set<Scope> aliveAfterInstruction(Instruction i) {
		Set<Scope> alive = new HashSet<>();
		State s = get().predecessor(i);
		alive.addAll(get().aliveInState(s));
		alive.addAll(get().requiredScopes(i));
		alive.removeAll(get().scopesToKill(i));
		return alive;
	}

	// REQUIRED SCOPES

	@Synthesized
	public Set<Scope> requiredScopes(Instruction i) {
		return new HashSet<>();
	}

	@Synthesized
	public Set<Scope> requiredScopes(ICall c) {
		ActorMachine am = get().actorMachine(c);
		Transition t = am.getTransition(c.T());
		return get().requiredScopes(t);
	}

	@Synthesized
	public Set<Scope> requiredScopes(ITest t) {
		ActorMachine am = get().actorMachine(t);
		Condition c = am.getCondition(t.C());
		return get().requiredScopes(c);
	}

	// SCOPES TO INIT

	@Synthesized
	public List<Scope> scopesToInit(Instruction i) {
		Set<Scope> init = new HashSet<>();
		init.addAll(get().requiredScopes(i));
		State pred = get().predecessor(i);
		Set<Scope> aliveInState = get().aliveInState(pred);
		init.removeAll(aliveInState);
		List<Scope> result = new ArrayList<>();
		while (!init.isEmpty()) {
			Iterator<Scope> iter = init.iterator();
			boolean progress = false;
			while (iter.hasNext()) {
				Scope s = iter.next();
				if (!intersects(init, get().scopeDependencies(s), s)) {
					result.add(s);
					iter.remove();
					progress = true;
				}
			}
			if (!progress) throw new Error();
		}
		return result;
	}

	private <A, B> boolean intersects(Set<A> a, Set<B> b, Object ignore) {
		Set<?> small;
		Set<?> large;
		if (a.size() < b.size()) {
			small = a;
			large = b;
		} else {
			small = b;
			large = a;
		}
		for (Object o : small) {
			if (o != ignore && large.contains(o)) {
				return true;
			}
		}
		return false;
	}

	public interface Required {

		ActorMachine actorMachine(IRNode node);

		int lookupScopeId(Scope scope, Scope scope2);

		Set<Scope> requiredScopes(Condition t);

		Set<Scope> requiredScopes(Transition t);

		Set<Scope> requiredScopes(Instruction i);

		boolean isInitialState(State s);

		Set<Scope> persistentScopes(IRNode s);

		boolean checkInitialState(State node, State s);

		Set<Scope> scopesToKill(Instruction i);

		Set<Scope> aliveInState(State s);

		State predecessor(Instruction i);

		Set<Scope> aliveAfterInstruction(Instruction pred);

		Set<Instruction> predecessors(State s);

		Set<Scope> scopesToKill(Transition t);

		Set<Scope> scopeDependencies(Scope s);

	}

}
