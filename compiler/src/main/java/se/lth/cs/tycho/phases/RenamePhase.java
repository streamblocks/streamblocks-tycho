package se.lth.cs.tycho.phases;


import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.comp.CompilationTask;
import se.lth.cs.tycho.comp.Context;
import se.lth.cs.tycho.comp.UniqueNumbers;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.GlobalNames;
import se.lth.cs.tycho.phases.attributes.Names;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RenamePhase implements Phase {
	@Override
	public String getDescription() {
		return "Renames variables and entities to have globally unique names.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		Rename rename = MultiJ.from(Rename.class)
				.bind("names").to(context.getAttributeManager().getAttributeModule(Names.key, task))
				.bind("globalNames").to(context.getAttributeManager().getAttributeModule(GlobalNames.key, task))
				.bind("uniqueNumbers").to(context.getUniqueNumbers())
				.instance();
		return (CompilationTask) new Transformation(rename::rename).apply(task);
	}

	private static final class Transformation implements Function<IRNode, IRNode> {
		private final BiFunction<? super IRNode, ? super IRNode, ? extends IRNode> transformation;

		private Transformation(BiFunction<? super IRNode, ? super IRNode, ? extends IRNode> transformation) {
			this.transformation = transformation;
		}

		@Override
		public IRNode apply(IRNode node) {
			return transformation.apply(node, node.transformChildren(this));
		}
	}

	@Module
	interface Rename {
		@Binding(BindingKind.INJECTED)
		Names names();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Binding
		default Map<EntityDecl, String> entityNames() {
			return new IdentityHashMap<>();
		}

		@Binding
		default Map<VarDecl, String> variableNames() {
			return new IdentityHashMap<>();
		}

		@Binding
		UniqueNumbers uniqueNumbers();

		default String generateName(String base) {
			return base + "_" + uniqueNumbers().next();
		}

		default IRNode rename(IRNode original, IRNode node) {
			return node;
		}

		default IRNode rename(CompilationTask original, CompilationTask task) {
			EntityDecl target = globalNames().entityDecl(task.getIdentifier(), false);
			String name = name(target);
			return task.withIdentifier(task.getIdentifier().getButLast().concat(QID.of(name)));
		}

		default String name(VarDecl original) {
			if (variableNames().containsKey(original)) {
				return variableNames().get(original);
			} else if (original.isImport()) {
				VarDecl imported = globalNames().varDecl(original.getQualifiedIdentifier(), false);
				String name = name(imported);
				variableNames().put(original, name);
				return name;
			} else {
				String name = generateName(original.getName());
				variableNames().put(original, name);
				return name;
			}
		}

		default IRNode rename(VarDecl original, VarDecl varDecl) {
			String name = name(original);
			if (varDecl.isImport()) {
				QID qid = varDecl.getQualifiedIdentifier().getButLast().concat(QID.of(name));
				return varDecl.withQualifiedIdentifier(qid).withName(name);
			} else {
				return varDecl.withName(name);
			}
		}

		default IRNode rename(Variable original, Variable var) {
			return var.copy(name(names().declaration(original)));
		}

		default Map<String, String> parameterMap(EntityInstanceExpr original) {
			EntityDecl entityDecl = names().entityDeclaration(original);
			if (entityDecl.isImport()) {
				entityDecl = globalNames().entityDecl(entityDecl.getQualifiedIdentifier(), false);
			}
			return entityDecl.getEntity().getValueParameters().stream().collect(Collectors.toMap(
					VarDecl::getName,
					decl -> variableNames().computeIfAbsent(decl, d -> generateName(d.getName()))));
		}

		default String name(EntityDecl original) {
			if (entityNames().containsKey(original)) {
				return entityNames().get(original);
			} else if (original.isImport()) {
				EntityDecl imported = globalNames().entityDecl(original.getQualifiedIdentifier(), false);
				String name = name(imported);
				entityNames().put(original, name);
				return name;
			} else {
				String name = generateName(original.getName());
				entityNames().put(original, name);
				return name;
			}
		}

		default IRNode rename(EntityDecl original, EntityDecl entityDecl) {
			String name = name(original);
			if (entityDecl.isImport()) {
				QID qid = entityDecl.getQualifiedIdentifier().getButLast().concat(QID.of(name));
				return entityDecl.withQualifiedIdentifier(qid).withName(name);
			} else {
				return entityDecl.withName(name);
			}
		}

		default IRNode rename(EntityInstanceExpr original, EntityInstanceExpr instance) {
			Map<String, String> parameterMap = parameterMap(original);
			ImmutableList<Map.Entry<String, Expression>> assignments = instance.getParameterAssignments()
					.map(entry -> ImmutableEntry.of(parameterMap.get(entry.getKey()), entry.getValue()));
			String name = name(names().entityDeclaration(original));
			return instance.copy(name, assignments, instance.getToolAttributes());
		}
	}
}
