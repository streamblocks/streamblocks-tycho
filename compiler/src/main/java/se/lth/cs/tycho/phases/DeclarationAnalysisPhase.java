package se.lth.cs.tycho.phases;

import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.InputVarDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeclarationAnalysisPhase implements Phase {
	@Override
	public String getDescription() {
		return "Checks if declared names are unique.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Map<QID, List<SourceUnit>> varDecls = new HashMap<>();
		Map<QID, List<SourceUnit>> typeDecls = new HashMap<>();
		Map<QID, List<SourceUnit>> entityDecls = new HashMap<>();
		for (SourceUnit sourceUnit : task.getSourceUnits()) {
			NamespaceDecl ns = sourceUnit.getTree();
			QID qid = ns.getQID();
			collectNonLocalNames(sourceUnit, varDecls, qid, ns.getVarDecls());
			collectNonLocalNames(sourceUnit, typeDecls, qid, ns.getTypeDecls());
			collectNonLocalNames(sourceUnit, entityDecls, qid, ns.getEntityDecls());
		}
		for (SourceUnit sourceUnit : task.getSourceUnits()) {
			NamespaceDecl ns = sourceUnit.getTree();
			QID qid = ns.getQID();
			addConflictingLocalNames(sourceUnit, varDecls, qid, ns.getVarDecls());
			addConflictingLocalNames(sourceUnit, typeDecls, qid, ns.getTypeDecls());
			addConflictingLocalNames(sourceUnit, entityDecls, qid, ns.getEntityDecls());
		}

		checkGlobalNames(varDecls, "Variable", context.getReporter());
		checkGlobalNames(typeDecls, "Type", context.getReporter());
		checkGlobalNames(entityDecls, "Entity", context.getReporter());

		CheckLocalNames check = new CheckLocalNames(context.getReporter());
		check.accept(task);

		return task;
	}

	private boolean checkGlobalNames(Map<QID, List<SourceUnit>> decls, String kind, Reporter reporter) {
		boolean success = true;
		for (Map.Entry<QID, List<SourceUnit>> entry : decls.entrySet()) {
			if (entry.getValue().size() > 1) {
				reporter.report(new Diagnostic(Diagnostic.Kind.ERROR,
						kind + " " + entry.getKey() + " has several definitions:\n\t" +
						entry.getValue().stream().map(SourceUnit::getLocation).collect(Collectors.joining("\n\t"))
				));
				success = false;
			}
		}
		return success;
	}

	private <S, D extends GlobalDecl> void collectNonLocalNames(S source, Map<QID, List<S>> sourceMap, QID ns, List<D> decls) {
		for (D d : decls) {
			if (d.getAvailability() != Availability.LOCAL) {
				QID qid = ns.concat(QID.of(d.getName()));
				sourceMap.computeIfAbsent(qid, q -> new ArrayList<>())
						.add(source);
			}
		}
	}

	private <S, D extends GlobalDecl> void addConflictingLocalNames(S source, Map<QID, List<S>> sourceMap, QID ns, List<D> decls) {
		for (D d : decls) {
			if (d.getAvailability() == Availability.LOCAL) {
				QID qid = ns.concat(QID.of(d.getName()));
				if (sourceMap.containsKey(qid)) {
					sourceMap.get(qid).add(source);
				}
			}
		}
	}

	private static class CheckLocalNames implements Consumer<IRNode> {
		private final Reporter reporter;

		public CheckLocalNames(Reporter reporter) {
			this.reporter = reporter;
		}

		@Override
		public void accept(IRNode node) {
			if (node instanceof NamespaceDecl) {
				check(((NamespaceDecl) node).getTypeDecls());
				check(((NamespaceDecl) node).getVarDecls());
				check(((NamespaceDecl) node).getEntityDecls());
			} else if (node instanceof ExprLet) {
				check(((ExprLet) node).getTypeDecls());
				check(((ExprLet) node).getVarDecls());
			} else if (node instanceof ExprLambda) {
				check(((ExprLambda) node).getValueParameters());
			} else if (node instanceof ExprProc) {
				check(((ExprProc) node).getValueParameters());
			} else if (node instanceof StmtBlock) {
				check(((StmtBlock) node).getTypeDecls());
				check(((StmtBlock) node).getVarDecls());
			} else if (node instanceof Generator) {
				check(((Generator) node).getVarDecls());
			} else if (node instanceof Action) {
				Action action = (Action) node;
				check(action.getTypeDecls());
				Stream<LocalVarDecl> actionVars = action.getVarDecls().stream();
				Stream<InputVarDecl> inputVars = action.getInputPatterns().stream().flatMap(inputPattern -> inputPattern.getVariables().stream());
				check(Stream.concat(inputVars, actionVars));
			} else if (node instanceof CalActor) {
				check(((CalActor) node).getTypeDecls());
				check(((CalActor) node).getVarDecls());
			}
			node.forEachChild(this);
		}

		private void check(ImmutableList<? extends Decl> decls) {
			check(decls.stream());
		}

		private void check(Stream<? extends Decl> decls) {
			Set<String> names = new HashSet<>();
			decls.forEach(decl -> {
				if (!names.add(decl.getName())) {
					reporter.report(new Diagnostic(
							Diagnostic.Kind.ERROR,
							decl.getName() + " is already declared in this scope."));
				}
			});
		}
	}
}
