package se.lth.cs.tycho.phases;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.ScheduleFSM;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

public class ScheduleUntaggedPhase implements Phase {

	@Override
	public String getDescription() {
		return "Adds untagged actions to action schedule.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.instance(Transformation.class));
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Collections.singleton(AddSchedulePhase.class);
	}

	@Module
	interface Transformation extends IRNode.Transformation {
		@Override
		default IRNode apply(IRNode node) {
			return transform(node);
		}
		default IRNode transform(IRNode node) {
			return node.transformChildren(this);
		}
		default IRNode transform(Decl decl) {
			return decl;
		}
		default IRNode transform(GlobalEntityDecl decl) {
			return decl.transformChildren(this);
		}
		default IRNode transform(Entity entity) {
			return entity;
		}
		default IRNode transform(CalActor actor) {
			if (actor.getActions().stream().noneMatch(action -> action.getTag() == null)) {
				return actor;
			}
			QID untagged = QID.of("$untagged");
			ImmutableList<Action> actions = actor.getActions().stream()
					.map(action -> {
						if (action.getTag() == null) {
							return action.withTag(untagged);
						} else {
							return action;
						}
					})
					.collect(ImmutableList.collector());
			Stream<Transition> untaggedTransitions = actor.getScheduleFSM().getTransitions().stream()
					.flatMap(transition -> Stream.of(transition.getSourceState(), transition.getDestinationState()))
					.sorted()
					.distinct()
					.map(state -> new Transition(state, state, ImmutableList.of(untagged)));

			ImmutableList<Transition> transitions = Stream.concat(actor.getScheduleFSM().getTransitions().stream(), untaggedTransitions)
					.collect(ImmutableList.collector());

			ScheduleFSM schedule = actor.getScheduleFSM().copy(transitions, actor.getScheduleFSM().getInitialState());

			return actor.withActions(actions).withScheduleFSM(schedule);
		}
	}
}
