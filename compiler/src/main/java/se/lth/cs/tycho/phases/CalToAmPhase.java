package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.Constants;
import se.lth.cs.tycho.phases.cal2am.CalToAm;
import se.lth.cs.tycho.phases.cal2am.KnowledgeRemoval;
import se.lth.cs.tycho.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CalToAmPhase implements Phase {
	@Override
	public String getDescription() {
		return "Translates all Cal actors to actor machines";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return task.withTarget(task.getTarget().withEntityDecls(task.getTarget().getEntityDecls().map(decl -> {
			if (decl.getEntity() instanceof CalActor) {
				CalToAm translator = new CalToAm((CalActor) decl.getEntity(), context.getConfiguration(), context.getAttributeManager().getAttributeModule(Constants.key, task));
				return decl.withEntity(translator.buildActorMachine());
			} else {
				return decl;
			}
		})));
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return ImmutableList.of(
				KnowledgeRemoval.forgetOnExec,
				KnowledgeRemoval.forgetOnWait
		);
	}
}
