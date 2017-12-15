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

import java.util.LinkedHashSet;
import java.util.Set;

public class AddSchedulePhase implements Phase {
	@Override
	public String getDescription() {
		return "Adds a schedule to actors that doesn't have one.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.transformChildren(MultiJ.instance(AddSchedule.class));
	}

	@Module
	interface AddSchedule extends IRNode.Transformation {
		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(Decl decl) {
			return decl;
		}

		default IRNode apply(GlobalEntityDecl entityDecl) {
			return entityDecl.transformChildren(this);
		}

		default IRNode apply(Entity entity) {
			return entity;
		}

		default IRNode apply(CalActor actor) {
			if (actor.getScheduleFSM() == null) {
				Set<QID> tags = new LinkedHashSet<>();
				for (Action a : actor.getActions()) {
					if (a.getTag() != null) {
						tags.add(a.getTag());
					}
				}
				String state = "S";
				Transition transition = new Transition(state, state, tags.stream().collect(ImmutableList.collector()));
				ScheduleFSM schedule = new ScheduleFSM(ImmutableList.of(transition), state);
				return actor.withScheduleFSM(schedule);
			} else {
				return actor;
			}
		}
	}
}
