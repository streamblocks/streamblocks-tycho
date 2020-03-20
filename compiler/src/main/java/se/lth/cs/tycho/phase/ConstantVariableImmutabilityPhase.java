package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.lvalue.LValueVariable;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class ConstantVariableImmutabilityPhase implements Phase {

	@Override
	public String getDescription() {
		return "Checks immutability of constant variables";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		ImmutabilityChecker checker = MultiJ.from(ImmutabilityChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("variables").to(task.getModule(VariableDeclarations.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.check(task);
		return task;
	}

	@Module
	interface ImmutabilityChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		VariableDeclarations variables();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void check(IRNode node) {
			checkImmutability(node);
			node.forEachChild(this::check);
		}

		default void checkImmutability(IRNode node) {

		}

		default void checkImmutability(LValueVariable lvalue) {
			if (variables().declaration(lvalue).isConstant()) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Cannot assign to const variable " + lvalue.getVariable().getName() + ".", sourceUnit(lvalue), lvalue));
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
