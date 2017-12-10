package se.lth.cs.tycho.phases;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.phases.attributes.GlobalNames;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

public class ImportAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		ImportChecker checker = MultiJ.from(ImportChecker.class)
				.bind("global").to(context.getAttributeManager().getAttributeModule(GlobalNames.key, task))
				.bind("tree").to(context.getAttributeManager().getAttributeModule(TreeShadow.key, task))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.checkTree(task);
		return task;
	}

	@Module
	interface ImportChecker {
		@Binding(BindingKind.INJECTED)
		GlobalNames global();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void checkTree(IRNode node) {
		    checkNode(node);
			node.forEachChild(this::checkTree);
		}

		default void checkNode(IRNode node) {}

		default void checkNode(SingleImport imp) {
		    Decl decl;
		    switch (imp.getKind()) {
				case VAR:
				    decl = global().varDecl(imp.getGlobalName(), false);
					break;
				case ENTITY:
					decl = global().entityDecl(imp.getGlobalName(), false);
					break;
				case TYPE:
					decl = global().typeDecl(imp.getGlobalName(), false);
					break;
				default:
					decl = null;
			}
			if (decl == null) {
				String message = String.format("The %s '%s' is not declared.", imp.getKind().getDescription(), imp.getGlobalName());
				SourceUnit unit = sourceUnit(imp);
				reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, message, unit, imp));
			}
		}

		default void checkNode(GroupImport imp) {
		    int count = 0;
			switch (imp.getKind()) {
				case VAR:
				    count = global().namespaceDecls(imp.getGlobalName()).stream()
							.mapToInt(ns -> ns.getVarDecls().size())
							.sum();
					break;
				case ENTITY:
					count = global().namespaceDecls(imp.getGlobalName()).stream()
							.mapToInt(ns -> ns.getEntityDecls().size())
							.sum();
					break;
				case TYPE:
					count = global().namespaceDecls(imp.getGlobalName()).stream()
							.mapToInt(ns -> ns.getTypeDecls().size())
							.sum();
					break;
			}
			if (count == 0) {
				String message = String.format("There is no %s in namespace %s.", imp.getKind().getDescription(), imp.getGlobalName());
				SourceUnit unit = sourceUnit(imp);
				reporter().report(new Diagnostic(Diagnostic.Kind.WARNING, message, unit, imp));
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
