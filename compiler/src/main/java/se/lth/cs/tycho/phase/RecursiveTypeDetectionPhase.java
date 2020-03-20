package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.TypeScopes;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.AlgebraicTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.reporting.Diagnostic;
import se.lth.cs.tycho.reporting.Reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecursiveTypeDetectionPhase implements Phase {

	@Override
	public String getDescription() {
		return "Detects (mutually) recursive type declarations";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		RecursiveTypeChecker checker = MultiJ.from(RecursiveTypeChecker.class)
				.bind("tree").to(task.getModule(TreeShadow.key))
				.bind("typeScopes").to(task.getModule(TypeScopes.key))
				.bind("reporter").to(context.getReporter())
				.instance();
		checker.check(task);
		return task;
	}

	@Module
	interface RecursiveTypeChecker {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Binding(BindingKind.INJECTED)
		Reporter reporter();

		default void check(IRNode node) {
			checkDeclaration(node);
			node.forEachChild(this::check);
		}

		default void checkDeclaration(IRNode node) {

		}

		default void checkDeclaration(NamespaceDecl namespace) {
			checkCycle(typeScopes().declarations(namespace));
		}

		default void checkCycle(ImmutableList<TypeDecl> declarations) {
			Map<Integer, TypeDecl> IntegerToTypeDecl = new HashMap<>();
			Map<TypeDecl, Integer> TypeDeclToInteger = new HashMap<>();
			for (int i = 0; i < declarations.size(); ++i) {
				IntegerToTypeDecl.put(i, declarations.get(i));
				TypeDeclToInteger.put(declarations.get(i), i);
			}

			int[][] graph = graph(declarations, TypeDeclToInteger);

			List<Integer> whitelist = new ArrayList<>();
			List<Integer> blacklist = new ArrayList<>();
			List<Integer> greylist = new ArrayList<>();

			for (int i = 0; i < graph.length; ++i) {
				whitelist.add(i);
			}

			while (!whitelist.isEmpty()) {
				for (int current = 0; current < graph.length; ++current) {
					if (whitelist.contains(current)) {
						if (dfs(current, whitelist, greylist, blacklist, graph)) {
							GlobalTypeDecl recursiveTypeDecl = (GlobalTypeDecl) IntegerToTypeDecl.get(current);
							reporter().report(new Diagnostic(Diagnostic.Kind.ERROR, "Recursive declaration for type " + recursiveTypeDecl.getName() + ".", sourceUnit(recursiveTypeDecl), recursiveTypeDecl));
						}
					}
				}
			}
		}

		default int[][] graph(ImmutableList<TypeDecl> declarations, Map<TypeDecl, Integer> TypeDeclToInteger) {
			int[][] graph = new int[declarations.size()][declarations.size()];
			declarations.forEach(declaration -> {
				AlgebraicTypeDecl algebraicTypeDecl = ((GlobalTypeDecl) declaration).getDeclaration();
				if (algebraicTypeDecl instanceof ProductTypeDecl) {
					((ProductTypeDecl) algebraicTypeDecl).getFields().forEach(field -> {
						typeScopes().declaration(field.getType()).ifPresent(decl -> {
							graph[TypeDeclToInteger.get(declaration)][TypeDeclToInteger.get(decl)] = 1;
						});
					});
				} else if (algebraicTypeDecl instanceof SumTypeDecl) {
					((SumTypeDecl) algebraicTypeDecl).getVariants().forEach(variant -> {
						variant.getFields().forEach(field -> {
							typeScopes().declaration(field.getType()).ifPresent(decl -> {
								graph[TypeDeclToInteger.get(declaration)][TypeDeclToInteger.get(decl)] = 1;
							});
						});
					});
				}
			});
			return graph;
		}

		default boolean dfs(Integer current, List<Integer> whitelist, List<Integer> greylist, List<Integer> blacklist, int[][] graph) {
			whitelist.remove(current);
			greylist.add(current);

			for (int vertex = 0; vertex < graph.length; ++vertex) {
				if (graph[current][vertex] != 0) {
					if (blacklist.contains(vertex)) {
						continue;
					}
					if (greylist.contains(vertex)) {
						return true;
					}
					if (dfs(vertex, whitelist, greylist, blacklist, graph)) {
						return true;
					}
				}
			}

			greylist.remove(current);
			blacklist.add(current);

			return false;
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
