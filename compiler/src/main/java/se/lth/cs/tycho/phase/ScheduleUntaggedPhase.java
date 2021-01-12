package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.UniqueNumbers;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ScheduleUntaggedPhase implements Phase {

	@Override
	public String getDescription() {
		return "Adds untagged actions to action schedule.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.from(Transformation.class).bind("numbers").to(context.getUniqueNumbers()).instance());
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Collections.singleton(AddSchedulePhase.class);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		UniqueNumbers numbers();

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
			numbers().reset();
			List<QID> untagged = new ArrayList<>();
			ImmutableList<Action> actions = actor.getActions().stream()
					.map(action -> {
						if (action.getTag() == null) {
							QID tag = QID.of(String.format("$untagged%d", numbers().next()));
							untagged.add(tag);
							return action.withTag(tag);
						} else {
							return action;
						}
					})
					.collect(ImmutableList.collector());
			Stream<Transition> untaggedTransitions = actor.getScheduleFSM().getTransitions().stream()
					.flatMap(transition -> Stream.of(transition.getSourceState(), transition.getDestinationState()))
					.sorted()
					.distinct()
					.map(state -> new Transition(state, state, ImmutableList.from(untagged)));

			ImmutableList<Transition> transitions = Stream.concat(actor.getScheduleFSM().getTransitions().stream(), untaggedTransitions)
					.collect(ImmutableList.collector());

			ScheduleFSM schedule = actor.getScheduleFSM().copy(transitions, actor.getScheduleFSM().getInitialState());

			return actor.withActions(actions).withScheduleFSM(schedule);
		}
	}
}
