package se.lth.cs.tycho.transformation;

import se.lth.cs.tycho.decoration.ImportDeclarations;
import se.lth.cs.tycho.decoration.Namespaces;
import se.lth.cs.tycho.decoration.Tree;
import se.lth.cs.tycho.decoration.VariableDeclarations;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.Parameter;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.nl.EntityInstanceExpr;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

public final class RenameVariables {
	private RenameVariables() {}

	public static <N extends IRNode> N rename(N root, LongSupplier uniqueNumbers) {
		Tree<N> tree = Tree.of(root);
		Map<Tree<VarDecl>, String> names_ = createNames(tree, uniqueNumbers);

		Map<Tree<VarDecl>, String> names = unifyImportedNames(names_);

		Map<QID, QID> publicVarDecls = publicVarDecls(tree, names);

		Map<Tree<Variable>, String> variableNames = variableNames(tree, names);

		Map<Tree<Parameter<Expression>>, String> parameterNames = parameterNames(tree, names);

		IRNode result = tree.transformNodes(node -> {
			Optional<Tree<Variable>> var = node.tryCast(Variable.class);
			if (var.isPresent()) {
				if (variableNames.containsKey(var.get())) {
					return var.get().node().withName(variableNames.get(var.get()));
				} else {
					return var.get().node();
				}
			}
			Optional<Tree<VarDecl>> decl = node.tryCast(VarDecl.class);
			if (decl.isPresent()) {
				VarDecl varDecl = decl.get().node();
				if (names.containsKey(decl.get())) {
					varDecl = varDecl.withName(names.get(decl.get()));
				}
				if (varDecl.isImport() && publicVarDecls.containsKey(varDecl.getQualifiedIdentifier())) {
					varDecl = varDecl.withQualifiedIdentifier(publicVarDecls.get(varDecl.getQualifiedIdentifier()));
				}
				return varDecl;
			}
			Optional<Tree<Parameter>> parameter = node.tryCast(Parameter.class);
			if (parameter.isPresent()) {
				if (parameterNames.containsKey(parameter.get())) {
					return parameter.get().node().withName(parameterNames.get(parameter.get()));
				} else {
					return parameter.get().node();
				}
			}
			return node.node();
		});

		return (N) result;
	}

	private static Map<Tree<VarDecl>, String> unifyImportedNames(Map<Tree<VarDecl>, String> names) {
		Map<Tree<VarDecl>, String> result = new HashMap<>();
		for (Tree<VarDecl> varDeclTree : names.keySet()) {
			Optional<Tree<VarDecl>> imported = ImportDeclarations.followVariableImport(varDeclTree);
			if (imported.isPresent()) {
				result.put(varDeclTree, names.get(imported.get()));
			} else {
				result.put(varDeclTree, names.get(varDeclTree));
			}
		}
		return result;
	}

	private static Map<QID, QID> publicVarDecls(Tree<?> tree, Map<Tree<VarDecl>, String> names) {
		Map<QID, QID> publicVarDecls = new HashMap<>();
		Namespaces.getAllNamespaces(tree)
				.forEach(ns -> ns.children(NamespaceDecl::getVarDecls)
						.filter(varDecl -> varDecl.node().getAvailability() == Availability.PUBLIC)
						.forEach(varDecl -> {
							QID old = ns.node().getQID().concat(QID.of(varDecl.node().getName()));
							QID renamed = ns.node().getQID().concat(QID.of(names.get(varDecl)));
							publicVarDecls.put(old, renamed);
						}));
		return publicVarDecls;
	}

	private static Map<Tree<Parameter<Expression>>, String> parameterNames(Tree<? extends IRNode> root, Map<Tree<VarDecl>, String> names) {
		Map<Tree<Parameter<Expression>>, String> parameterNames = new HashMap<>();
		root.walk().forEach(tree ->
				tree.tryCast(EntityInstanceExpr.class).ifPresent(inst ->
						inst.children(EntityInstanceExpr::getParameterAssignments).forEach(param ->
								VariableDeclarations.getValueParameterDeclaration(param).ifPresent(decl ->
										Optional.ofNullable(names.get(decl)).ifPresent(name ->
												parameterNames.put(param, name))))));
		return parameterNames;
	}

	private static Map<Tree<Variable>, String> variableNames(Tree<? extends IRNode> tree, Map<Tree<VarDecl>, String> names) {
		Map<Tree<Variable>, String> variableNames = new HashMap<>();
		tree.walk().forEach(t ->
				t.tryCast(Variable.class).ifPresent(var ->
						VariableDeclarations.getDeclaration(var).ifPresent(decl ->
								Optional.ofNullable(names.get(decl)).ifPresent(name ->
										variableNames.put(var, name)))));
		return variableNames;
	}

	private static Map<Tree<VarDecl>, String> createNames(Tree<?> tree, LongSupplier uniqueNumbers) {
		Map<Tree<VarDecl>, String> names = new HashMap<>();
		tree.walk().forEach(node -> {
			Optional<Tree<VarDecl>> decl = node.tryCast(VarDecl.class);
			if (decl.isPresent()) {
				String name = decl.get().node().getOriginalName() + "_" + uniqueNumbers.getAsLong();
				names.put(decl.get(), name);
			}
		});
		return names;
	}

}
