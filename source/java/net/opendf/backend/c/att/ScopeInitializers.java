package net.opendf.backend.c.att;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javarag.Bottom;
import javarag.Circular;
import javarag.Inherited;
import javarag.Module;
import javarag.Synthesized;
import net.opendf.ir.IRNode;
import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.Condition;
import net.opendf.ir.entity.am.ICall;
import net.opendf.ir.entity.am.ITest;
import net.opendf.ir.entity.am.Instruction;
import net.opendf.ir.entity.am.Scope;
import net.opendf.ir.entity.am.State;
import net.opendf.ir.entity.am.Transition;
import net.opendf.ir.util.ImmutableList;

public class ScopeInitializers extends Module<ScopeInitializers.Decls> {

	public interface Decls {
		@Inherited
		Set<Scope> persistentScopes(IRNode s);

		@Synthesized
		Set<Scope> scopesToKill(Instruction i);

		@Synthesized
		Set<Scope> scopesToKill(Transition t);

		@Synthesized
		boolean isInitialState(State s);

		@Inherited
		boolean checkInitialState(State node, State s);

		@Circular
		@Synthesized
		Set<Scope> aliveInState(State s);

		@Circular
		@Synthesized
		Set<Scope> aliveAfterInstruction(Instruction pred);

		@Synthesized
		Set<Scope> requiredScopes(Condition t);
		
		@Synthesized
		Set<Scope> requiredScopes(Transition t);
		
		@Synthesized
		Set<Scope> requiredScopes(Instruction i);
		
		@Synthesized
		List<Scope> scopesToInit(Instruction i);
		
		ActorMachine actorMachine(IRNode node);

		State predecessor(Instruction i);

		Set<Instruction> predecessors(State s);

		Set<Scope> scopeDependencies(Scope s);

	}

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

	public Set<Scope> scopesToKill(Instruction i) {
		return new HashSet<>();
	}

	public Set<Scope> scopesToKill(ICall c) {
		ActorMachine am = e().actorMachine(c);
		Transition t = am.getTransition(c.T());
		return e().scopesToKill(t);
	}

	public Set<Scope> scopesToKill(Transition t) {
		ActorMachine am = e().actorMachine(t);
		ImmutableList<Scope> scopes = am.getScopes();
		Set<Scope> kill = new HashSet<>();
		for (int k : t.getScopesToKill()) {
			kill.add(scopes.get(k));
		}
		return kill;
	}

	// ALIVE IN STATE

	public boolean checkInitialState(ActorMachine am, State s) {
		return am.getController().get(0) == s;
	}

	public boolean isInitialState(State s) {
		return e().checkInitialState(s, s);
	}

	@Bottom("aliveInState")
	public Set<Scope> aliveInStateBottom(State s) {
		Set<Scope> persistentScopes = e().persistentScopes(s);
		return persistentScopes;
	}

	public Set<Scope> aliveInState(State s) {
		Set<Scope> alive = null;
		for (Instruction pred : e().predecessors(s)) {
			Set<Scope> after = e().aliveAfterInstruction(pred);
			if (alive == null) {
				alive = new HashSet<>();
				alive.addAll(after);
			} else {
				alive.retainAll(after);
			}
		}
		return alive == null ? e().persistentScopes(s) : alive;
	}

	// ALIVE AFTER INSTRUCTION

	@Bottom("aliveAfterInstruction")
	public Set<Scope> aliveAfterInstructionBottom(Instruction i) {
		Set<Scope> persistentScopes = e().persistentScopes(i);
		return persistentScopes;
	}

	public Set<Scope> aliveAfterInstruction(Instruction i) {
		Set<Scope> alive = new HashSet<>();
		State s = e().predecessor(i);
		alive.addAll(e().aliveInState(s));
		alive.addAll(e().requiredScopes(i));
		alive.removeAll(e().scopesToKill(i));
		return alive;
	}

	// REQUIRED SCOPES

	public Set<Scope> requiredScopes(Instruction i) {
		return new HashSet<>();
	}

	public Set<Scope> requiredScopes(ICall c) {
		ActorMachine am = e().actorMachine(c);
		Transition t = am.getTransition(c.T());
		return e().requiredScopes(t);
	}

	public Set<Scope> requiredScopes(ITest t) {
		ActorMachine am = e().actorMachine(t);
		Condition c = am.getCondition(t.C());
		return e().requiredScopes(c);
	}

	// SCOPES TO INIT

	public List<Scope> scopesToInit(Instruction i) {
		Set<Scope> init = new HashSet<>();
		init.addAll(e().requiredScopes(i));
		State pred = e().predecessor(i);
		Set<Scope> aliveInState = e().aliveInState(pred);
		init.removeAll(aliveInState);
		List<Scope> result = new ArrayList<>();
		while (!init.isEmpty()) {
			Iterator<Scope> iter = init.iterator();
			boolean progress = false;
			while (iter.hasNext()) {
				Scope s = iter.next();
				if (!intersects(init, e().scopeDependencies(s), s)) {
					result.add(s);
					iter.remove();
					progress = true;
				}
			}
			if (!progress)
				throw new Error();
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

}
