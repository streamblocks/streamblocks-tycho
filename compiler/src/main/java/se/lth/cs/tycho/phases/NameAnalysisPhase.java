package se.lth.cs.tycho.phases;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.BindingKind;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.phases.attributes.NameBinding;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class NameAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes name binding.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		NameBinding nameBinding = context.getAttributeManager().getAttributeModule(NameBinding.key, task);
		task.getSourceUnits().stream().forEach(unit -> {
			CheckNames analysis = MultiJ.from(CheckNames.class)
					.bind("names").to(nameBinding)
					.bind("reporter").to(context.getReporter())
					.bind("sourceUnit").to(unit)
					.instance();
			analysis.checkNames(unit);
		});
		return task;
	}

	@Module
	public interface CheckNames {
		@Binding(BindingKind.INJECTED)
		NameBinding names();

		@Binding
		Reporter reporter();

		@Binding
		SourceUnit sourceUnit();

		default void check(IRNode node) {
			checkNames(node);
			node.forEachChild(this::check);
		}

		default void checkNames(IRNode node) {}

		default void checkNames(Variable var) {
			if (names().declaration(var) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getName() + " is not declared.", sourceUnit(), var));
			}
		}
	}

}
