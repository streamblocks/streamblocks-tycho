package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.decoration.Namespaces;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GroupImport;
import se.lth.cs.tycho.ir.decl.SingleImport;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Tree.of(task).walk().forEach(node -> checkNode(node, context.getReporter()));
		return task;
	}

	private void checkNode(Tree<? extends IRNode> imp, Reporter reporter) {
		imp.tryCast(SingleImport.class).ifPresent(singleImport -> checkSingleImport(singleImport, reporter));
		imp.tryCast(GroupImport.class).ifPresent(groupImport -> checkGroupImport(groupImport, reporter));
	}

	private void checkSingleImport(Tree<SingleImport> singleImport, Reporter reporter) {
		Stream<? extends Tree<? extends GlobalDecl>> declarations;
		switch (singleImport.node().getKind()) {
			case VAR:
				declarations = Namespaces.getVariableDeclarations(singleImport, singleImport.node().getGlobalName());
				break;
			case TYPE:
				declarations = Namespaces.getTypeDeclarations(singleImport, singleImport.node().getGlobalName());
				break;
			case ENTITY:
				declarations = Namespaces.getEntityDeclarations(singleImport, singleImport.node().getGlobalName());
				break;
			default:
				throw new AssertionError("Unknown enum variant");
		}
		List<? extends Tree<? extends GlobalDecl>> decls = declarations
				.filter(decl -> decl.node().getAvailability() == Availability.PUBLIC)
				.collect(Collectors.toList());
		if (decls.isEmpty()) {
			String message = String.format("The %s '%s' is not declared.", singleImport.node().getKind().getDescription(), singleImport.node().getGlobalName());
			SourceUnit unit = singleImport.findParentOfType(SourceUnit.class).get().node();
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, message, unit, singleImport.node()));
		} else if (decls.size() > 1){
			String files = decls.stream().map(d -> d.findParentOfType(SourceUnit.class).get().node().getLocation())
					.collect(Collectors.joining("\t\n"));
			String message = String.format("The %s '%s' has conflicting declarations in:\n\t%s", singleImport.node().getKind(), singleImport.node().getGlobalName(), files);
			SourceUnit unit = singleImport.findParentOfType(SourceUnit.class).get().node();
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, message, unit, singleImport.node()));
		}
	}

	private void checkGroupImport(Tree<GroupImport> groupImport, Reporter reporter) {
		// TODO: check if the group import imported anything
	}
}
