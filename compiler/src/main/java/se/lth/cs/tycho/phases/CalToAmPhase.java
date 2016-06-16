package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.Transformations;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.ConstantEvaluator;
import se.lth.cs.tycho.phases.cal2am.CalToAm;
import se.lth.cs.tycho.phases.cal2am.KnowledgeRemoval;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.List;

public class CalToAmPhase implements Phase {
	@Override
	public String getDescription() {
		return "Translates all Cal actors to actor machines";
	}

	public static final OnOffSetting actionAmbiguityDetection = new OnOffSetting() {
		@Override
		public String getKey() {
			return "action-ambiguity-detection";
		}

		@Override
		public String getDescription() {
			return "Emits code that dynamically detects action ambiguities.";
		}

		@Override
		public Boolean defaultValue(Configuration configuration) {
			return false;
		}
	};

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return Transformations.transformEntityDecls(task, decl -> {
			if (decl.getEntity() instanceof CalActor) {
				CalToAm translator = new CalToAm((CalActor) decl.getEntity(), context.getConfiguration(), context.getAttributeManager().getAttributeModule(ConstantEvaluator.key, task));
				return decl.withEntity(translator.buildActorMachine());
			} else {
				return decl;
			}
		});
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return ImmutableList.of(
				KnowledgeRemoval.forgetOnExec,
				KnowledgeRemoval.forgetOnWait,
				actionAmbiguityDetection
		);
	}
}
