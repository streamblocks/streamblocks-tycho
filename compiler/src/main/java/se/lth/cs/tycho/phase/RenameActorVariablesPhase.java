package se.lth.cs.tycho.phase;


import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.transformation.RenameVariables;

public class RenameActorVariablesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Renames actor and action variables.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
        VariableKinds kinds = MultiJ.from(VariableKinds.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.instance();
		return (CompilationTask) RenameVariables.appendNumber(
				task,
				d -> kinds.isActorVariable(d) || kinds.isActionVariable(d) || kinds.isInputVariable(d),
				context.getUniqueNumbers(),
				task
		);
	}

	@Module
	interface VariableKinds {
		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		default boolean isActorVariable(VarDecl var) {
		    return tree().parent(var) instanceof CalActor;
		}

		default boolean isActorVariable(ParameterVarDecl var) {
			return false;
		}

		default boolean isActionVariable(VarDecl var) {
			return tree().parent(var) instanceof Action;
		}

		default boolean isInputVariable(VarDecl var) {
			return false;
		}

		default boolean isInputVariable(InputVarDecl var) {
			return true;
		}
	}

}
