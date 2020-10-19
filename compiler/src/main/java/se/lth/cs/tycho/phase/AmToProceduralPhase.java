package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.Transformations;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.transformation.am2procedural.AmToProcedural;

public class AmToProceduralPhase implements Phase {
    @Override
    public String getDescription() {
        return "Translates all AM Actors to Procedural";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        return Transformations.transformEntityDecls(task, decl -> {
            if (decl.getEntity() instanceof CalActor) {
                AmToProcedural translator = new AmToProcedural((ActorMachine) decl.getEntity(), context.getConfiguration(), task.getModule(Types.key), task.getModule(TreeShadow.key));
                return decl.withEntity(translator.buildProcedural());
            } else {
                return decl;
            }
        });
    }
}
