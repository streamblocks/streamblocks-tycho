package se.lth.cs.tycho.transformation.reduction;

import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.Controller;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.InstructionVisitor;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;
import se.lth.cs.tycho.ir.entity.am.ctrl.Test;
import se.lth.cs.tycho.ir.entity.am.ctrl.Wait;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.transformation.cal2am.CalToAm;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransformedController implements Controller {
	private final TransformedState initialState;
	private Function<State, State> transformation;
	private Map<State, State> transformationCache;
	private Map<State, TransformedState> stateCache;

	private List<State> stateList;

	private Map<String, Integer> actionPriority;

	private final int totalActions;

	public TransformedController(Controller controller, Function<State, State> transformation) {
		this.transformation = transformation;
		this.transformationCache = new HashMap<>();
		this.stateCache = new HashMap<>();
		this.actionPriority = new HashMap<String, Integer>();
		this.initialState = getState(controller.getInitialState());

		if (controller.getInitialState() instanceof CalToAm.CalState) {
			this.totalActions = ((CalToAm.CalState) controller.getInitialState()).getActor().getActions().size();
		}else{
			this.totalActions = -1;
		}
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

		// Added specifically for CAL actors by Gareth
		int lowestActionIndex = -1;
		if (s instanceof CalToAm.CalState){
			CalToAm.CalState newState = (CalToAm.CalState)s;
			for (Action action: newState.getTestableActions()) {
				String actionName = action.getTag().toString();
				if(!actionPriority.containsKey(actionName)){
					actionPriority.put(actionName, actionPriority.size());
				}
				if(actionPriority.get(actionName) < lowestActionIndex || lowestActionIndex == -1){
					lowestActionIndex = actionPriority.get(actionName);
				}
			}
			//System.out.println("Cal State: " + newState.getTestableActions().size() + " " + lowestActionIndex + " " + Arrays.toString(newState.getTestableActions().toArray()) + " " + actionPriority);
		}
		// Added specifically for CAL actors by Gareth


		TransformedState toReturn = stateCache.computeIfAbsent(transformed, TransformedState::new);
		toReturn.lowestActionIndex = lowestActionIndex;
		return toReturn;
	}

	public class TransformedState implements State {
		public State transformed;
		private List<Instruction> instructions;

		public int lowestActionIndex;

		public TransformedState(State transformed) {
			this.transformed = transformed;
			this.lowestActionIndex = -1;
		}

		public TransformedState(State transformed, int lowestActionIndex) {
			this.transformed = transformed;
			this.lowestActionIndex = -1;
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

		public int getTotalActions() {
			return totalActions;
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
			return new Test(t.condition(), getState(t.targetTrue()), getState(t.targetFalse()), t.getKnowledgePriority());
		}

		@Override
		public Instruction visitWait(Wait t, Void aVoid) {
			return new Wait(getState(t.target()), t.waitsFor());
		}
	};

}
