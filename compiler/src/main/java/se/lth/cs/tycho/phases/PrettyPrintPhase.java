package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.nl.NlNetwork;
import se.lth.cs.tycho.util.PrettyPrint;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PrettyPrintPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		PrettyPrint print = new PrettyPrint();
		task.getTarget().getEntityDecls().forEach(entityDecl -> {
			Entity entity = entityDecl.getEntity();
			String name = entityDecl.getName();
			if (entity instanceof CalActor) {
				print.print((CalActor) entity, name);
			} else if (entity instanceof NlNetwork) {
				print.print((NlNetwork) entity, name);
			} else if (entity instanceof ActorMachine) {
				System.out.println("Actor Machine: " + name);
				Queue<State> queue = new ArrayDeque<>();
				queue.add(((ActorMachine) entity).controller().getInitialState());
				Map<State, Integer> states = new HashMap<>();
				Set<State> visited = new HashSet<>();
				while (!queue.isEmpty()) {
					State s = queue.remove();
					if (visited.add(s)) {
						states.putIfAbsent(s, states.size());
						int i = states.get(s);
						states.put(s, i);
						for (Instruction instr : s.getInstructions()) {
							instr.forEachTarget(queue::add);
							System.out.print(i);
							instr.accept(
									exec -> {
										states.putIfAbsent(exec.target(), states.size());
										int target = states.get(exec.target());
										System.out.printf(" exec %d %d\n", exec.transition(), target);
										return null;
									},
									test -> {
										states.putIfAbsent(test.targetTrue(), states.size());
										states.putIfAbsent(test.targetFalse(), states.size());
										int targetTrue = states.get(test.targetTrue());
										int targetFalse = states.get(test.targetFalse());
										System.out.printf(" test %d %d %d\n", test.condition(), targetTrue, targetFalse);
										return null;
									},
									wait -> {
										states.putIfAbsent(wait.target(), states.size());
										int target = states.get(wait.target());
										System.out.printf(" wait %d\n", target);
										return null;
									}
							);
						}
					}
				}
				System.out.println();
			}
		});
		return null;
	}
}
