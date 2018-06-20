package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.EntityDeclarations;
import se.lth.cs.tycho.attribute.ParameterDeclarations;
import se.lth.cs.tycho.attribute.Ports;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.TypeParameter;
import se.lth.cs.tycho.ir.ValueParameter;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceGlobal;
import se.lth.cs.tycho.ir.entity.nl.EntityReferenceLocal;
import se.lth.cs.tycho.ir.expr.ExprGlobalVariable;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class NameAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes name binding.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		CheckNames analysis = MultiJ.from(CheckNames.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("ports").to(task.getModule(Ports.key))
				.bind("variableDeclarations").to(task.getModule(VariableDeclarations.key))
				.bind("entityDeclarations").to(task.getModule(EntityDeclarations.key))
				.bind("parameterDeclarations").to(task.getModule(ParameterDeclarations.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		analysis.check(task);
		return task;
	}

	@Module
	public interface CheckNames {
		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		Ports ports();

		@Binding(BindingKind.INJECTED)
		VariableDeclarations variableDeclarations();

		@Binding(BindingKind.INJECTED)
		EntityDeclarations entityDeclarations();

		//@Binding(BindingKind.INJECTED)
		//ModuleDeclarations moduleDeclarations();

		//@Binding(BindingKind.INJECTED)
		//ModuleMembers moduleMembers();

		@Binding(BindingKind.INJECTED)
		ParameterDeclarations parameterDeclarations();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}

		default void check(IRNode node) {
			checkNames(node);
			node.forEachChild(this::check);
		}

		default void checkNames(IRNode node) {}

		default void checkNames(Variable var) {
			if (variableDeclarations().declaration(var) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getName() + " is not declared.", sourceUnit(var), var));
			}
		}

		default void checkNames(ExprGlobalVariable var) {
			if (variableDeclarations().declaration(var) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getGlobalName() + " is not declared.", sourceUnit(var), var));
			}
		}

		default void checkNames(Port port) {
			if (ports().declaration(port) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + port.getName() + " is not declared.", sourceUnit(port), port));
			}
		}

		default void checkNames(EntityReferenceGlobal reference) {
			if (entityDeclarations().declaration(reference) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + reference.getGlobalName() + " is not declared.", sourceUnit(reference), reference));
			}
		}

		default void checkNames(EntityReferenceLocal reference) {
			if (entityDeclarations().declaration(reference) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + reference.getName() + " is not declared.", sourceUnit(reference), reference));
			}
		}

		default boolean isTypeExpr(IRNode node) {
			return false;
		}

		default boolean isTypeExpr(TypeExpr expr) {
			return true;
		}

		default void checkNames(ValueParameter parameter) {
			if (!isTypeExpr(tree().parent(parameter))) {
				if (parameterDeclarations().valueParameterDeclaration(parameter) == null) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Parameter " + parameter.getName() + " is not declared.", sourceUnit(parameter), parameter));
				}
			}
		}

		default void checkNames(TypeParameter parameter) {
			if (!isTypeExpr(tree().parent(parameter))) {
				if (parameterDeclarations().typeParameterDeclaration(parameter) == null) {
					reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Parameter " + parameter.getName() + " is not declared.", sourceUnit(parameter), parameter));
				}
			}
		}
	}

}
