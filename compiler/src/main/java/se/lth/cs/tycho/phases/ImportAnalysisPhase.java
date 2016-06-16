package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.GlobalDeclarations;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImportAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Checks that all import declarations import something.";
	}

	@Override
	public Set<Class<? extends Phase>> dependencies() {
		return Collections.singleton(DeclarationAnalysisPhase.class);
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		check(task, NamespaceDecl::getVarDecls, GlobalDeclarations::findVariables, context.getReporter());
		check(task, NamespaceDecl::getEntityDecls, GlobalDeclarations::findEntities, context.getReporter());
		check(task, NamespaceDecl::getTypeDecls, GlobalDeclarations::findTypes, context.getReporter());
		return task;
	}

	private void checkStarImports(CompilationTask task, Reporter reporter) {
		Set<QID> namespacesWithVariables = task.getSourceUnits().stream()
				.map(unit -> unit.getTree())
				.filter(ns -> !ns.getVarDecls().isEmpty())
				.map(ns -> ns.getQID())
				.collect(Collectors.toSet());

		for (SourceUnit unit : task.getSourceUnits()) {
			for (StarImport starImport : unit.getTree().getStarImports()) {
				if (!namespacesWithVariables.contains(starImport.getQID())) {
					reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Import not found: " + starImport.getQID(), unit, starImport));
				}
			}
		}
	}
	private <D extends Decl> void check(CompilationTask task, Function<NamespaceDecl, List<D>> getDecl, BiFunction<CompilationTask, QID, List<D>> findGlobal, Reporter reporter) {
		for (SourceUnit unit : task.getSourceUnits()) {
			for (D decl : getDecl.apply(unit.getTree())) {
				if (decl.isImport()) {
					List<D> decls = findGlobal.apply(task, decl.getQualifiedIdentifier());
					switch (decls.size()) {
						case 0:
							reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Import not found: " + decl.getQualifiedIdentifier(), unit, decl));
							break;
						case 1:
							break;
						default:
							throw new AssertionError("Compiler error: Ambiguous declarations should be checked before imports.");
					}
				}
			}
		}
	}
}
