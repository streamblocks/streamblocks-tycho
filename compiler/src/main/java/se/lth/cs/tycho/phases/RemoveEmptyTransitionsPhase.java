package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.Transformations;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.phases.reduction.TransformedController;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RemoveEmptyTransitionsPhase implements Phase {
	@Override
	public String getDescription() {
		return "Removes exec instructions of empty transitions.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return Transformations.transformEntityDecls(task, this::transformEntityDecl);
	}

	private EntityDecl transformEntityDecl(EntityDecl entityDecl) {
		if (entityDecl.getEntity() instanceof ActorMachine) {
			return entityDecl.withEntity(removeEmptyExec((ActorMachine) entityDecl.getEntity()));
		} else {
			return entityDecl;
		}
	}

	private ActorMachine removeEmptyExec(ActorMachine actorMachine) {
		BitSet emptyTransitions = IntStream.range(0, actorMachine.getTransitions().size())
				.filter(i -> isEmpty(actorMachine.getTransitions().get(i).getBody()))
				.collect(BitSet::new, BitSet::set, BitSet::or);
		return actorMachine.withController(transform(actorMachine.controller(), emptyTransitions));
	}

	private Controller transform(Controller controller, BitSet emptyTransitions) {
		return new TransformedController(controller, state -> new FilteredState(state, emptyTransitions));
//		return new RemoveEmptyExecController(controller, emptyTransitions);
	}

	private static Visitor visitor = new Visitor();
	private static class Visitor implements InstructionVisitor<Stream<Instruction>, BitSet> {
		@Override
		public Stream<Instruction> visitExec(Exec t, BitSet emptyTransitions) {
			if (emptyTransitions.get(t.transition())) {
				return t.target().getInstructions().stream()
						.flatMap(i -> i.accept(this, emptyTransitions));
			} else {
				return Stream.of(t);
			}
		}

		@Override
		public Stream<Instruction> visitTest(Test t, BitSet emptyTransitions) {
			return Stream.of(t);
		}

		@Override
		public Stream<Instruction> visitWait(Wait t, BitSet emptyTransitions) {
			return Stream.of(t);
		}
	}

	private static class FilteredState implements State {
		private List<Instruction> instructions;
		private State original;
		private BitSet emptyTransitions;

		public FilteredState(State original, BitSet emptyTransitions) {
			this.original = original;
			this.emptyTransitions = emptyTransitions;
		}

		@Override
		public List<Instruction> getInstructions() {
			if (original != null) {
				instructions = original.getInstructions().stream()
						.flatMap((Instruction i) -> i.accept(visitor, emptyTransitions))
						.collect(Collectors.toList());
				instructions = prioritize(instructions);
				original = null;
			}
			return instructions;
		}

	}

	private static List<Instruction> prioritize(List<Instruction> instructions) {
		List<Instruction> exec = new ArrayList<>();
		List<Instruction> test = new ArrayList<>();
		List<Instruction> wait = new ArrayList<>();
		for (Instruction i : instructions) {
			if (i instanceof Exec) {
				exec.add(i);
			} else if (i instanceof Test) {
				test.add(i);
			} else if (i instanceof Wait) {
				wait.add(i);
			} else {
				throw new AssertionError();
			}
		}
		if (!exec.isEmpty()) return exec;
		if (!test.isEmpty()) return test;
		if (!wait.isEmpty()) return wait;
		throw new AssertionError();
	}

	private boolean isEmpty(Statement s) {
		return s instanceof StmtBlock && ((StmtBlock) s).getStatements().stream().allMatch(this::isEmpty);
	}
}
