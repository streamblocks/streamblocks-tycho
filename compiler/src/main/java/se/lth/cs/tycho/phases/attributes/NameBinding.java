package se.lth.cs.tycho.phases.attributes;

import se.lth.cs.multij.Binding;
import se.lth.cs.multij.Module;
import se.lth.cs.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.SourceUnit;
import se.lth.cs.tycho.ir.GeneratorFilter;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.expr.ExprLambda;
import se.lth.cs.tycho.ir.expr.ExprLet;
import se.lth.cs.tycho.ir.expr.ExprList;
import se.lth.cs.tycho.ir.expr.ExprProc;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.StmtBlock;
import se.lth.cs.tycho.ir.stmt.StmtForeach;
import se.lth.cs.tycho.phases.TreeShadow;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Module
public interface NameBinding {
	ModuleKey<NameBinding> key = new ModuleKey<NameBinding>() {
		@Override
		public Class<NameBinding> getKey() {
			return NameBinding.class;
		}

		@Override
		public NameBinding createInstance(CompilationTask unit, AttributeManager manager) {
			return MultiJ.from(Implementation.class)
					.bind("tree").to(manager.getAttributeModule(TreeShadow.key, unit))
					.instance();
		}
	};


	VarDecl declaration(Variable var);

	@Module
	interface Implementation extends NameBinding {
		@Binding
		TreeShadow tree();

		@Binding
		default Map<Variable, VarDecl> declarationMap() {
			return new ConcurrentHashMap<>();
		}

		default VarDecl declaration(Variable var) {
			return declarationMap().computeIfAbsent(var, v -> lookup(v, v.getName()));
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

		Optional<VarDecl> localLookup(GeneratorFilter generator, IRNode node, String name);

		default Optional<VarDecl> localLookup(GeneratorFilter generator, Expression context, String name) {
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
			throw new Error();
		}

		default Optional<VarDecl> localLookup(GeneratorFilter generator, VarDecl context, String name) {
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
			return findGlobalVar(ns.getQID().concat(QID.of(name)), true);
		}

		default Optional<VarDecl> findGlobalVar(QID qid, boolean includingPrivate) {
			QID ns = qid.getButLast();
			CompilationTask unit = (CompilationTask) tree().root();
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
}
