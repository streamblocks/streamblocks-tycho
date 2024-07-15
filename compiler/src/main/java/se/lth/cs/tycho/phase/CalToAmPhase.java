package se.lth.cs.tycho.phase;

import se.lth.cs.tycho.attribute.*;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.Transformations;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.OnOffSetting;
import se.lth.cs.tycho.settings.Setting;
import se.lth.cs.tycho.transformation.cal2am.CalToAm;
import se.lth.cs.tycho.transformation.cal2am.KnowledgeRemoval;

import java.util.List;

public class CalToAmPhase implements Phase {
    public static Setting<Boolean> bypassAmGeneration = new OnOffSetting() {
        @Override
        public String getKey() {
            return "bypass-AM-generation";
        }

        @Override
        public String getDescription() {
            return "Bypass the conversion of a CAL actors to Actor machines. This requires that a backend is being " +
                    "used that is able to generate code from normal CAL actors, not AMs. If a backend is used that " +
                    "does not support this, errors will be thrown or else strange code will be generated.";
        }

        @Override
        public Boolean defaultValue(Configuration configuration) {
            return false;
        }
    };

    @Override
    public String getDescription() {
        return "Translates all Cal actors to actor machines";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) {
        return Transformations.transformEntityDecls(task, decl -> {
            // Note by Gareth Callanan on 2024/07/15: It would be nice if each actor could be annotated to tell
            // the compiler not to generate an actor machine, but I have not had time to implement it. So currently
            // either all actors have actor machines or none. As of this date, only the vitis backend supports
            // non-AM actors, so this can cause problem when paritioning the network between hardware and software.
            // boolean hasNoActorMachine = Annotation.hasAnnotationWithName("no_actor_machine",
            //        decl.getEntity().getAnnotations());
            boolean hasNoActorMachine = context.getConfiguration().get(bypassAmGeneration);
            if (decl.getEntity() instanceof CalActor && !hasNoActorMachine) {
                CalToAm translator = new CalToAm((CalActor) decl.getEntity(), context.getConfiguration(),
                        task.getModule(ConstantEvaluator.key),
                        task.getModule(Types.key),
                        task.getModule(TreeShadow.key),
                        task.getModule(Ports.key),
                        task.getModule(VariableDeclarations.key),
                        task.getModule(VariableScopes.key),
                        task.getModule(FreeVariables.key));
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
                bypassAmGeneration
        );
    }
}
