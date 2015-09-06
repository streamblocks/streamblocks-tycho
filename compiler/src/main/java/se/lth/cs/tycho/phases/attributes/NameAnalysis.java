package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationUnit;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.StarImport;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.phases.TreeShadow;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Module
public interface NameAnalysis {
	ModuleKey<NameAnalysis> key = new ModuleKey<NameAnalysis>() {
		@Override
		public Class<NameAnalysis> getKey() {
			return NameAnalysis.class;
		}

		@Override
		public NameAnalysis createInstance(CompilationUnit unit, AttributeManager manager) {
			return MultiJ.from(NameAnalysis.class)
					.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
					.instance();
		}
	};

	@Binding
	TreeShadow tree();

	@Binding
	default Map<StarImport, Set<String>> starImported() {
		return new ConcurrentHashMap<>();
	}

	@Binding
	default Map<Variable, VarDecl> declarationMap() {
		return new ConcurrentHashMap<>();
	}

	default VarDecl declaration(Variable var) {
		return declarationMap().computeIfAbsent(var, v -> lookup(v, v.getName()));
	}

	default void checkNames(IRNode node, SourceUnit unit, Reporter reporter) {
		node.forEachChild(child -> checkNames(child, unit, reporter));
	}

	default void checkNames(CompilationUnit unit, SourceUnit s, Reporter reporter) {
		unit.getSourceUnits().parallelStream().forEach(x -> checkNames(x, null, reporter));
	}

	default void checkNames(SourceUnit node, SourceUnit unit, Reporter reporter) {
		node.forEachChild(child -> checkNames(child, node, reporter));
	}

	default void checkNames(Variable var, SourceUnit unit, Reporter reporter) {
		if (var.getName().startsWith("$BinaryOperation.") || var.getName().startsWith("$UnaryOperation.") || var.getName().equals("println") || var.getName().equals("print")) {
			return;
		}
		if (declaration(var) == null) {
			reporter.report(new Diagnostic(Diagnostic.Kind.ERROR, "Variable " + var.getName() + " is not declared.", unit, var));
		}
	}

	default VarDecl lookup(IRNode context, String name) {
		IRNode node = tree().parent(context);
		while (node != null) {
			Optional<VarDecl> d = localLookup(node, context, name);
			if (d.isPresent()) {
				return d.get();
			}
			context = node;
			node = tree().parent(node);
		}
		return null;
	}

	default Optional<VarDecl> localLookup(IRNode node, IRNode context, String name) {
		return Optional.empty();
	}

	default Optional<VarDecl> localLookup(ExprLet let, IRNode context, String name) {
		return findInList(let.getVarDecls(), name);
	}

	default Optional<VarDecl> localLookup(ExprLambda lambda, IRNode context, String name) {
		return findInList(lambda.getValueParameters(), name);
	}

	default Optional<VarDecl> localLookup(ExprProc proc, IRNode context, String name) {
		return findInList(proc.getValueParameters(), name);
	}

	default Optional<VarDecl> localLookup(StmtBlock block, IRNode context, String name) {
		return findInList(block.getVarDecls(), name);
	}

	default Optional<VarDecl> localLookup(ExprList list, IRNode context, String name) {
		Stream<VarDecl> decls = list.getGenerators().stream()
				.flatMap(generator -> generator.getVariables().stream());
		return findInStream(decls, name);
	}

	default Optional<VarDecl> localLookup(ExprList list, GeneratorFilter context, String name) {
		for (GeneratorFilter g : list.getGenerators()) {
			if (g == context) {
				return Optional.empty();
			}
			for (VarDecl d : g.getVariables()) {
				if (d.getName().equals(name)) {
					return Optional.of(d);
				}
			}
		}
		return Optional.empty();
	}

	default Optional<VarDecl> localLookup(GeneratorFilter generator, IRNode context, String name) {
		if (generator.getCollectionExpr() == context) {
			// The collection expression may only refer to variables declared before this generator.
			return Optional.empty();
		} else if (generator.getFilters().contains(context)) {
			// Filters may refer to their generator variables.
			for (VarDecl d : generator.getVariables()) {
				if (d.getName().equals(name)) {
					return Optional.of(d);
				}
			}
		}
		return Optional.empty();
	}

	default Optional<VarDecl> localLookup(StmtForeach foreach, IRNode context, String name) {
		Stream<VarDecl> decls = foreach.getGenerators().stream()
				.flatMap(generator -> generator.getVariables().stream());
		return findInStream(decls, name);
	}

	default Optional<VarDecl> localLookup(StmtForeach foreach, GeneratorFilter context, String name) {
		for (GeneratorFilter g : foreach.getGenerators()) {
			if (g == context) {
				return Optional.empty();
			}
			for (VarDecl d : g.getVariables()) {
				if (d.getName().equals(name)) {
					return Optional.of(d);
				}
			}
		}
		return Optional.empty();
	}

	default Optional<VarDecl> localLookup(Action action, IRNode context, String name) {
		Stream<VarDecl> actionVars = action.getVarDecls().stream();
		Stream<VarDecl> inputVars = action.getInputPatterns().stream()
				.flatMap(inputPattern -> inputPattern.getVariables().stream());

		return findInStream(Stream.concat(actionVars, inputVars), name);
	}

	default Optional<VarDecl> localLookup(CalActor actor, IRNode context, String name) {
		return findInStream(Stream.concat(actor.getVarDecls().stream(), actor.getValueParameters().stream()), name);
	}

	default Optional<VarDecl> localLookup(NamespaceDecl ns, IRNode context, String name) {
		Optional<VarDecl> result = findInList(ns.getVarDecls(), name);
		if (result.isPresent()) {
			return result;
		}
		result = findGlobalVar(ns.getQID().concat(QID.of(name)), true);
		if (result.isPresent()) {
			return result;
		}

		VarDecl imported = null; // Ambiguous imports are bound arbitrarily.
		for (StarImport starImport : ns.getStarImports()) {
			Optional<VarDecl> globalVar = findGlobalVar(starImport.getQID().concat(QID.of(name)), false);
			if (globalVar.isPresent()) {
				starImported().computeIfAbsent(starImport, x -> new HashSet()).add(name);
				imported = globalVar.get();
			}
		}
		return Optional.ofNullable(imported);
	}

	default Optional<VarDecl> findGlobalVar(QID qid, boolean includingPrivate) {
		QID ns = qid.getButLast();
		CompilationUnit unit = (CompilationUnit) tree().root();
		for(SourceUnit sourceUnit : unit.getSourceUnits()) {
			if (sourceUnit.getTree().getQID().equals(ns)) {
				Optional<VarDecl> d = findInList(sourceUnit.getTree().getVarDecls(), qid.getLast().toString());
				if (d.isPresent()) {
					if (d.get().getAvailability() == Availability.PUBLIC) {
						return d;
					}
					if (includingPrivate && d.get().getAvailability() == Availability.PRIVATE) {
						return d;
					}
				}
			}
		}
		return Optional.empty();
	}

	default Optional<VarDecl> findInList(List<VarDecl> decls, String name) {
		return findInStream(decls.stream(), name);
	}

	default Optional<VarDecl> findInStream(Stream<VarDecl> decls, String name) {
		return decls.filter(decl -> decl.getName().equals(name)).findAny();
	}
}
