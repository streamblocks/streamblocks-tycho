package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScheduleInitializersPhase implements Phase {

	@Override
	public String getDescription() {
		return "Adds initializers to action schedule.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.instance(Transformation.class));
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Stream.of(ScheduleUntaggedPhase.class, AddSchedulePhase.class).collect(Collectors.toSet());
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
			if (actor.getInitializers().isEmpty()) {
				return actor;
			} else {
				String initState = "$init";
				QID initTag = QID.of("$init");
				ImmutableList<Transition> transitions = ImmutableList.<Transition> builder()
						.add(new Transition(initState, actor.getScheduleFSM().getInitialState(), ImmutableList.of(initTag)))
						.addAll(actor.getScheduleFSM().getTransitions())
						.build();
				ScheduleFSM schedule = actor.getScheduleFSM().copy(transitions, initState);
				ImmutableList<Action> initActions = actor.getInitializers().map(a -> a.withTag(initTag));
				ImmutableList<Action> allActions = ImmutableList.concat(initActions, actor.getActions());
				return actor.withScheduleFSM(schedule)
						.withActions(allActions)
						.withInitialisers(ImmutableList.empty());
			}
		}
	}
}
