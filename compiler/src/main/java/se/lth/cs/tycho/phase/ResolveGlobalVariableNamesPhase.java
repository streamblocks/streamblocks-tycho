package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.VariableDeclarations;

import java.util.Optional;

public class ResolveGlobalVariableNamesPhase implements Phase {
	@Override
	public String getDescription() {
		return "Resolves local names of global variables to global names";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("task").to(task)
				.instance();
		return transformation.transform(task);
	}

	@Module
	interface Transformation {
		@Binding(BindingKind.INJECTED)
		CompilationTask task();

		@Binding(BindingKind.LAZY)
		default VariableDeclarations variables() {
            return task().getModule(VariableDeclarations.key);
        }

		@Binding(BindingKind.LAZY)
		default TreeShadow tree() {
            return task().getModule(TreeShadow.key);
        }

		@Binding(BindingKind.LAZY)
		default GlobalNames globalNames() {
            return task().getModule(GlobalNames.key);
        }

		default IRNode transform(IRNode node) {
			return node.transformChildren(this::transform);
		}

		default CompilationTask transform(CompilationTask task) {
			return task.transformChildren(this::transform);
		}

		default Expression transform(ExprVariable var) {
			VarDecl decl = variables().declaration(var);
			Optional<QID> globalName = globalNames().globalName(decl);
			return globalName.<Expression>map(ExprGlobalVariable::new).orElse(var);
		}
	}
}
