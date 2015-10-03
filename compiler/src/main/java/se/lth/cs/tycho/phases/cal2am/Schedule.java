package se.lth.cs.tycho.phases.cal2am;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.Transition;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Schedule {
	private final CalActor actor;

	public Schedule(CalActor actor) {
		this.actor = actor;
	}

	public Set<String> getInitialState() {
		return Collections.singleton(actor.getScheduleFSM().getInitialState());
	}

	public List<Action> getEligibleActions(Set<String> state) {
		List<QID> tags = actor.getScheduleFSM().getTransitions().stream()
				.filter(transition -> state.contains(transition.getSourceState()))
				.map(Transition::getActionTags)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		return actor.getActions().stream()
				.filter(action -> action.getTag() == null
						|| tags.stream().anyMatch(tag -> tag.isPrefixOf(action.getTag())))
				.collect(Collectors.toList());
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
