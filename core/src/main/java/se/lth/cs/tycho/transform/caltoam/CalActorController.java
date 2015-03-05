package se.lth.cs.tycho.transform.caltoam;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;
import se.lth.cs.tycho.instance.am.Condition;

import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.transform.caltoam.CalActorStates.State;
import se.lth.cs.tycho.transform.caltoam.util.BitSets;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.Controller;

public class CalActorController implements Controller<State> {
	private ScheduleHandler scheduleHandler;
	private ConditionHandler conditionHandler;
	private PriorityHandler priorityHandler;
	private List<Transition> transitions;
	private List<Condition> conditions;
	private CalActorStates calActorStates;
	private final QID instanceId;

	public CalActorController(ScheduleHandler scheduleHandler, ConditionHandler conditionHandler,
			PriorityHandler priorityHandler, List<Transition> transitions, List<Condition> conditions, CalActorStates calActorStates, QID instanceId) {
		this.scheduleHandler = scheduleHandler;
		this.conditionHandler = conditionHandler;
		this.priorityHandler = priorityHandler;
		this.transitions = transitions;
		this.conditions = conditions;
		this.calActorStates = calActorStates;
		this.instanceId = instanceId;
	}

	@Override
	public List<GenInstruction<State>> instructions(State state) {
		BitSet actions = scheduleHandler.scheduledActions(state.getSchedulerState());
		conditionHandler.removeDisabledActions(state, actions);
		priorityHandler.keepHighestPrio(actions);
		
		BitSet conditions = conditionHandler.getNextConditions(state, actions);
		BitSet calls = BitSets.copyOf(actions);
		conditionHandler.keepFirableActions(state, calls);
		
		List<GenInstruction<State>> result = new ArrayList<>();
		
		for (int call : BitSets.iterable(calls)) {
			State destination = state;
			for (Entry<Port, Integer> entry : transitions.get(call).getInputRates().entrySet()) {
				destination = destination.removeTokens(entry.getKey(), entry.getValue());
			}
			for (Entry<Port, Integer> entry : transitions.get(call).getOutputRates().entrySet()) {
				destination = destination.removeSpace(entry.getKey(), entry.getValue());
			}
			destination = destination.clearAbsentTokenResults();
			destination = destination.clearSpaceResults();
			destination = destination.setSchedulerState(scheduleHandler.destinations(state.getSchedulerState(), call));
			destination = destination.clearPredicateResults();
			result.add(new GenInstruction.Call<>(call, destination));
		}
		
		for (int condition : BitSets.iterable(conditions)) {
			State destTrue = state.setResult(condition, true);
			State destFalse = state.setResult(condition, false);
			result.add(new GenInstruction.Test<>(condition, destTrue, destFalse));
		}

		if (result.isEmpty()) {
			result.add(new GenInstruction.Wait<>(state.clearAbsentTokenResults().clearAbsentSpaceResults()));
		}
		
		return result;
	}

	@Override
	public State initialState() {
		return calActorStates.initialState();
	}
	
	@Override
	public QID instanceId() {
		return instanceId;
	}

	@Override
	public Condition getCondition(int c) {
		return conditions.get(c);
	}

	@Override
	public Transition getTransition(int t) {
		return transitions.get(t);
	}
}