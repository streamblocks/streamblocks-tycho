package se.lth.cs.tycho.transformation.cal2am;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Schedule {
	private final CalActor actor;
	private final Map<String, List<Action>> eligible;

	// For every action in the actor, this map stores all the FSM states that the action can execute within. This is
	// the reverse of the eligible map.
	private final Map<Action, Set<String>> actionToStateMapping;

	public Schedule(CalActor actor) {
		this.actor = actor;
		this.eligible = new HashMap<>();
		eligible.put(actor.getScheduleFSM().getInitialState(), new ArrayList<>());
		for (Transition t : actor.getScheduleFSM().getTransitions()) {
			eligible.putIfAbsent(t.getSourceState(), new ArrayList<>());
			eligible.putIfAbsent(t.getDestinationState(), new ArrayList<>());
		}
		Map<QID, Set<String>> eligibleIn = new HashMap<>();
		for (Transition transition : actor.getScheduleFSM().getTransitions()) {
			for (QID tag : transition.getActionTags()) {
				eligibleIn.computeIfAbsent(tag, t -> new HashSet<>()).add(transition.getSourceState());
			}
		}
		for (Action action : actor.getActions()) {
			if (action.getTag() != null) {
				QID tag = action.getTag();
				while (!tag.equals(QID.empty())) {
					eligibleIn.getOrDefault(tag, Collections.emptySet())
							.forEach(state -> eligible.computeIfAbsent(state, s -> new ArrayList<>()).add(action));
					tag = tag.getButLast();
				}
			}
		}
		for (Action action : actor.getActions()) {
			if (action.getTag() == null) {
				eligible.values().forEach(list -> list.add(action));
			}
		}

		this.actionToStateMapping = setActionToStateMapping();
	}

	/**
	 * Generate the map of action to sets of FSM states the action can execute within by reversing the eligible map.
	 *
	 * @return Return this generated map
	 */
	private Map<Action, Set<String>> setActionToStateMapping() {
		Map<Action, Set<String>> actionToStateMappingRet = new HashMap<>();

		for (Map.Entry<String, List<Action>> entry : this.eligible.entrySet()) {
			for(Action action: entry.getValue()){
				if(actionToStateMappingRet.containsKey(action)){
					actionToStateMappingRet.get(action).add(entry.getKey());
				}else{
					actionToStateMappingRet.put(action, new HashSet<String>());
					actionToStateMappingRet.get(action).add(entry.getKey());
				}
			}
		}
		return  actionToStateMappingRet;
	}

	/**
	 * Get the set of all actor FSM states that a particular action within an actor is able to fire within.
	 *
	 * @param action The action to get all eligible FSM states for.
	 * @return Set of strings with each string being the name of a different FSM state the action can execute in.
	 */
	public Set<String> getStates(Action action){
		return actionToStateMapping.get(action);
	}

	public Set<String> getInitialState() {
		return Collections.singleton(actor.getScheduleFSM().getInitialState());
	}
	public List<Action> getEligibleActions(Set<String> state) {
		if (state.size() == 1) {
			return eligible.getOrDefault(state.iterator().next(), Collections.emptyList());
		} else {
			throw new UnsupportedOperationException("Non-deterministic schedule."); // TODO implement
		}
	}

	public Map<String, List<Action>> getEligible(){
		return eligible;
	}

	public Set<String> targetState(Set<String> sourceState, Action action) {
		if (action.getTag() == null) {
			return sourceState;
		}

		Set<String> result = actor.getScheduleFSM().getTransitions().stream()
				.filter(transition -> sourceState.contains(transition.getSourceState()))
				.filter(transition -> transition.getActionTags().stream().anyMatch(tag -> tag.isPrefixOf(action.getTag())))
				.map(Transition::getDestinationState)
				.collect(Collectors.toSet());
		if (result.isEmpty()) {
			throw new IllegalArgumentException("Action is not eligible in this state");
		}
		return result;
	}
}
