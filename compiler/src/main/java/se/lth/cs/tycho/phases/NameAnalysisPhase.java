package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.decoration.EntityDeclarations;
import se.lth.cs.tycho.decoration.PortDeclarations;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.phases.attributes.Names;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class NameAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Analyzes name binding.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		CheckNames2 checkNames = MultiJ.from(CheckNames2.class)
				.bind("reporter").to(context.getReporter())
				.instance();
		Tree.of(task).walk().forEach(checkNames::check);
		return task;
//		Names names = context.getAttributeManager().getAttributeModule(Names.key, task);
//		task.getSourceUnits().stream().forEach(unit -> {
//			CheckNames analysis = MultiJ.from(CheckNames.class)
//					.bind("names").to(names)
//					.bind("reporter").to(context.getReporter())
//					.bind("sourceUnit").to(unit)
//					.instance();
//			analysis.check(unit);
//		});
//		return task;
	}

	@Module
	public interface CheckNames2 {
		@Binding Reporter reporter();

		default void check(Tree<? extends IRNode> tree) {
			checkNode(tree, tree.node());
		}

		default void checkNode(Tree tree, IRNode node) {}

		default void checkNode(Tree tree_, Variable var) {
			Tree<Variable> tree = tree_;
			if (!VariableDeclarations.getDeclaration(tree).isPresent()) {
				SourceUnit unit = tree.findParentOfType(SourceUnit.class).map(Tree::node).orElse(null);
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getName() + " is not declared.", unit ,var));
			}
		}

		default void checkNode(Tree tree_, Port port) {
			Tree<Port> tree = tree_;
			if (!PortDeclarations.getDeclaration(tree).isPresent()) {
				SourceUnit unit = tree.findParentOfType(SourceUnit.class).map(Tree::node).orElse(null);
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + port.getName() + " is not declared.", unit, port));
			}
		}

		default void checkNode(Tree tree_, EntityInstanceExpr instance) {
			Tree<EntityInstanceExpr> tree = tree_;
			if (!EntityDeclarations.getDeclaration(tree).isPresent()) {
				SourceUnit unit = tree.findParentOfType(SourceUnit.class).map(Tree::node).orElse(null);
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + instance.getEntityName() + " is not declared.", unit, instance));
			}
		}
	}
	@Module
	public interface CheckNames {
		@Binding(BindingKind.INJECTED)
		Names names();

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

		default void checkNames(Port port) {
			if (names().portDeclaration(port) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Port " + port.getName() + " is not declared.", sourceUnit(), port));
			}
		}

		default void checkNames(EntityInstanceExpr instance) {
			if (names().entityDeclaration(instance) == null) {
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Entity " + instance.getEntityName() + " is not declared.", sourceUnit(), instance));
			}
		}
	}

}
