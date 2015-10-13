package se.lth.cs.tycho.phases;

import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.phases.attributes.ActorMachineScopes;
import se.lth.cs.tycho.phases.attributes.AttributeManager;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.phases.attributes.Types;
import se.lth.cs.tycho.phases.cbackend.Backend;

public class CBackendPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		AttributeManager manager = context.getAttributeManager();
		Backend backend = MultiJ.from(Backend.class)
				.bind("types").to(manager.getAttributeModule(Types.key, task))
				.bind("names").to(manager.getAttributeModule(Names.key, task))
				.bind("uniqueNumbers").to(context.getUniqueNumbers())
				.bind("tree").to(manager.getAttributeModule(TreeShadow.key, task))
				.bind("scopes").to(manager.getAttributeModule(ActorMachineScopes.key, task))
				.instance();
		task.getTarget().getEntityDecls().forEach(backend.structure()::entityDecl);
		return task;
	}

}
