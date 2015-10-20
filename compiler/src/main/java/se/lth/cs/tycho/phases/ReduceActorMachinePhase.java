package se.lth.cs.tycho.phases;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ctrl.Controller;
import se.lth.cs.tycho.instance.am.ctrl.Exec;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.instance.am.ctrl.Test;
import se.lth.cs.tycho.instance.am.ctrl.Wait;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

public class ReduceActorMachinePhase implements Phase {

	@Override
	public String getDescription() {
		return "Reduces the actor machines to deterministic actor machines.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.instance(ReduceActorMachine.class));
	}

	@Module
	interface ReduceActorMachine extends Function<IRNode, IRNode> {
		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(Decl decl) {
			return decl;
		}

		default IRNode apply(EntityDecl decl) {
			return decl.transformChildren(this);
		}

		default IRNode apply(Entity entity) {
			return entity;
		}

		default IRNode apply(ActorMachine actorMachine) {
			return actorMachine.withController(selectFirst(actorMachine.controller()));
		}
	}

	private static DeterministicController selectFirst(Controller ctrl) {
		Map<State, MutableState> stateMap = new HashMap<>();
		List<MutableState> stateList = new ArrayList<>();
		Queue<State> queue = new ArrayDeque<>();
		queue.add(ctrl.getInitialState());
		while (!queue.isEmpty()) {
			State s = queue.remove();
			if (!stateMap.containsKey(s)) {
				MutableState r = new MutableState(s.getInstructions());
				stateMap.put(s, r);
				stateList.add(r);
				s.getInstructions().get(0).forEachTarget(queue::add);
			}
		}
		stateList.forEach(s -> s.i = Collections.singletonList(s.i.get(0).accept(
				exec -> new Exec(exec.transition(), stateMap.get(exec.target())),
				test -> new Test(test.condition(), stateMap.get(test.targetTrue()), stateMap.get(test.targetFalse())),
				wait -> new Wait(stateMap.get(wait.target()), wait.waitsFor()))));
		return new DeterministicController(stateMap.get(ctrl.getInitialState()), stateList);
	}

	private static final class MutableState implements State {
		private List<Instruction> i;

		public MutableState(List<Instruction> i) {
			this.i = i;
		}

		@Override
		public List<Instruction> getInstructions() {
			return i;
		}
	}

	private static final class DeterministicController implements Controller {
		private final MutableState init;
		private final List<MutableState> stateList;

		public DeterministicController(MutableState init, List<MutableState> stateList) {
			this.init = init;
			this.stateList = stateList;
		}

		@Override
		public MutableState getInitialState() {
			return init;
		}

		@Override
		public List<? extends MutableState> getStateList() {
			return stateList;
		}
	}
}
