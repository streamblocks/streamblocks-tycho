package se.lth.cs.tycho.phases.reduction;

import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransformedController implements Controller {
	private final TransformedState initialState;
	private Function<State, State> transformation;
	private Map<State, State> transformationCache;
	private Map<State, TransformedState> stateCache;

	private List<State> stateList;

	public TransformedController(Controller controller, Function<State, State> transformation) {
		this.transformation = transformation;
		this.transformationCache = new HashMap<>();
		this.stateCache = new HashMap<>();
		this.initialState = getState(controller.getInitialState());
	}

	@Override
	public State getInitialState() {
		return initialState;
	}

	@Override
	public List<State> getStateList() {
		if (stateList == null) {
			stateList = Collections.unmodifiableList(Controller.super.getStateList());
			transformation = null;
			transformationCache = null;
			stateCache = null;
		}
		return stateList;
	}

	public static Controller from(Controller ctrl, List<Function<State, State>> transformations) {
		Controller result = ctrl;
		for (Function<State, State> transformation : transformations) {
			result = new TransformedController(result, transformation);
		}
		return result;
	}

	private TransformedState getState(State s) {
		State transformed = transformationCache.computeIfAbsent(s, transformation);
		return stateCache.computeIfAbsent(transformed, TransformedState::new);
	}

	public class TransformedState implements State {
		private State transformed;
		private List<Instruction> instructions;

		public TransformedState(State transformed) {
			this.transformed = transformed;
		}

		@Override
		public List<Instruction> getInstructions() {
			if (instructions == null) {
				instructions = transformed.getInstructions().stream()
						.map(TransformedController.this::reroute)
						.collect(Collectors.toList());
				transformed = null;
			}
			return instructions;
		}

	}

	private Instruction reroute(Instruction i) {
		return i.accept(reroute);
	}

	private final InstructionVisitor<Instruction, Void> reroute = new InstructionVisitor<Instruction, Void>() {
		@Override
		public Instruction visitExec(Exec t, Void aVoid) {
			return new Exec(t.transition(), getState(t.target()));
		}

		@Override
		public Instruction visitTest(Test t, Void aVoid) {
			return new Test(t.condition(), getState(t.targetTrue()), getState(t.targetFalse()));
		}

		@Override
		public Instruction visitWait(Wait t, Void aVoid) {
			return new Wait(getState(t.target()), t.waitsFor());
		}
	};

}
