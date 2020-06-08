package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.VariableScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtCase;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		LocalNameChecker checker = MultiJ.from(LocalNameChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("variableScopes").to(task.getModule(VariableScopes.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.accept(task);

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

	@Module
	interface LocalNameChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		VariableScopes variableScopes();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void accept(IRNode node) {
			visit(node);
			node.forEachChild(this::accept);
		}

		default void visit(IRNode node) {

		}

		default void visit(NamespaceDecl node) {
			check(node.getTypeDecls().stream());
			check(node.getVarDecls().stream());
			check(node.getEntityDecls().stream());
		}

		default void visit(ExprLet node) {
			check(node.getTypeDecls().stream());
			check(node.getVarDecls().stream());
		}

		default void visit(ExprLambda node) {
			check(node.getValueParameters().stream());
		}

		default void visit(ExprProc node) {
			check(node.getValueParameters().stream());
		}

		default void visit(StmtBlock node) {
			check(node.getTypeDecls().stream());
			check(node.getVarDecls().stream());
		}

		default void visit(Generator node) {
			check(node.getVarDecls().stream());
		}

		default void visit(Action node) {
			check(node.getTypeDecls().stream());
			check(variableScopes().declarations(node).stream());
		}

		default void visit(CalActor node) {
			check(node.getTypeDecls().stream());
			check(node.getVarDecls().stream());
		}

		default void visit(ProductTypeDecl node) {
			check(node.getValueParameters().stream());
			check(node.getTypeParameters().stream());
			check(ImmutableList.from((node.getFields())).stream());
		}

		default void visit(SumTypeDecl node) {
			check(node.getValueParameters().stream());
			check(node.getTypeParameters().stream());
			check(node.getVariants().stream());
		}

		default void visit(SumTypeDecl.VariantDecl node) {
			check(ImmutableList.from(node.getFields()).stream());
		}

		default void visit(ExprCase.Alternative node) {
			check(variableScopes().declarations(node).stream());
		}

		default void visit(StmtCase.Alternative node) {
			check(variableScopes().declarations(node).stream());
		}

		default void check(Stream<? extends Decl> decls) {
			Set<String> names = new HashSet<>();
			decls.forEach(decl -> {
				if (!names.add(decl.getName())) {
					reporter().report(new Diagnostic(
							Diagnostic.Kind.ERROR,
							decl.getName() + " is already declared in this scope.", sourceUnit(decl), decl));
				}
			});
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
