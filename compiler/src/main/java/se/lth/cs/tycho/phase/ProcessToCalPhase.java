package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.Transformations;
import se.lth.cs.tycho.compiler.UniqueNumbers;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.transformation.proc2cal.ProcessToCal;

public class ProcessToCalPhase implements Phase {
	@Override
	public String getDescription() {
		return "Translates process description to other Cal constructs.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return Transformations.transformEntityDecls(task, decl ->
				decl.withEntity(translate(decl.getEntity(), context.getUniqueNumbers())));
	}
	private Entity translate(Entity entity, UniqueNumbers uniqueNumbers) {
		if (!(entity instanceof CalActor)) {
			return entity;
		} else {
			return ProcessToCal.translate((CalActor) entity, uniqueNumbers);
		}
	}

}
