package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.phases.attributes.EntityDeclarations;
import se.lth.cs.tycho.phases.attributes.Ports;
import se.lth.cs.tycho.phases.attributes.VariableDeclarations;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class NameAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes name binding.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Ports ports = context.getAttributeManager().getAttributeModule(Ports.key, task);
		VariableDeclarations varDecls = context.getAttributeManager().getAttributeModule(VariableDeclarations.key, task);
		EntityDeclarations entityDecls = context.getAttributeManager().getAttributeModule(EntityDeclarations.key, task);
		task.getSourceUnits().stream().forEach(unit -> {
			CheckNames analysis = MultiJ.from(CheckNames.class)
					.bind("ports").to(ports)
					.bind("variableDeclarations").to(varDecls)
					.bind("entityDeclarations").to(entityDecls)
					.bind("reporter").to(context.getReporter())
					.bind("sourceUnit").to(unit)
					.instance();
			analysis.check(unit);
		});
		return task;
	}

	@Module
	public interface CheckNames {
		@Binding(BindingKind.INJECTED)
		Ports ports();

		@Binding(BindingKind.INJECTED)
		VariableDeclarations variableDeclarations();

		@Binding(BindingKind.INJECTED)
		EntityDeclarations entityDeclarations();

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
			if (variableDeclarations().declaration(var) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getName() + " is not declared.", sourceUnit(), var));
			}
		}

		default void checkNames(Port port) {
			if (ports().declaration(port) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + port.getName() + " is not declared.", sourceUnit(), port));
			}
		}

		default void checkNames(EntityReferenceGlobal reference) {
			if (entityDeclarations().declaration(reference) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + reference.getGlobalName() + " is not declared.", sourceUnit(), reference));
			}
		}

		default void checkNames(EntityReferenceLocal reference) {
			if (entityDeclarations().declaration(reference) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + reference.getName() + " is not declared.", sourceUnit(), reference));
			}
		}
	}

}
