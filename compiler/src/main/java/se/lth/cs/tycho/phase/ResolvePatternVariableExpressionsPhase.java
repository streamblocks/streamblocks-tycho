package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.expr.ExprPatternVariable;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.reporting.CompilationException;

public class ResolvePatternVariableExpressionsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Resolves variable expressions bound to pattern variables";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
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

		default IRNode transform(IRNode node) {
			return node.transformChildren(this::transform);
		}

		default CompilationTask transform(CompilationTask task) {
			return task.transformChildren(this::transform);
		}

		default Expression transform(ExprVariable var) {
			VarDecl decl = variables().declaration(var);
			if (decl instanceof PatternVarDecl) {
				return new ExprPatternVariable(var.getVariable());
			}
			return var;
		}
	}
}
