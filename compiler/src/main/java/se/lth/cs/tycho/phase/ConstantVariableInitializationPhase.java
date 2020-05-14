package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class ConstantVariableInitializationPhase implements Phase {

	@Override
	public String getDescription() {
		return "Checks initialization of constant variable declarations";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		InitializationChecker checker = MultiJ.from(InitializationChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.check(task);
		return task;
	}

	@Module
	interface InitializationChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void check(IRNode node) {
			apply(node);
			node.forEachChild(this::check);
		}

		default void apply(IRNode node) {

		}

		default void apply(LocalVarDecl decl) {
			checkInitialization(decl);
		}

		default void apply(GlobalVarDecl decl) {
			checkInitialization(decl);
		}

		default void checkInitialization(VarDecl decl) {
			if (!decl.isExternal() && decl.isConstant() && decl.getValue() == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Constant variable " + decl.getOriginalName() + " is uninitialized.", sourceUnit(decl), decl));
			}
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
