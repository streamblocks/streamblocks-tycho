package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.GlobalDeclarations;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.ProductTypeDecl;
import se.lth.cs.tycho.ir.decl.SumTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.expr.ExprTypeConstruction;
import se.lth.cs.tycho.ir.expr.ExprVariable;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstructor;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.Objects;
import java.util.Optional;

public interface TypeScopes {

	ModuleKey<TypeScopes> key = task -> MultiJ.from(TypeScopes.Implementation.class)
			.bind("tree").to(task.getModule(TreeShadow.key))
			.bind("imports").to(task.getModule(ImportDeclarations.key))
			.bind("globalNames").to(task.getModule(GlobalNames.key))
			.bind("typeDeclarations").to(task.getModule(TypeDeclarations.key))
			.instance();

	ImmutableList<TypeDecl> declarations(IRNode node);

	Optional<TypeDecl> declaration(IRNode node);

	Optional<TypeDecl> construction(IRNode node);

	@Module
	interface Implementation extends TypeScopes {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		ImportDeclarations imports();

		@Binding(BindingKind.INJECTED)
		GlobalNames globalNames();

		@Binding(BindingKind.INJECTED)
		TypeDeclarations typeDeclarations();

		@Override
		default ImmutableList<TypeDecl> declarations(IRNode node) {
			return ImmutableList.empty();
		}

		default ImmutableList<TypeDecl> declarations(NamespaceDecl ns) {
			ImmutableList<TypeDecl> locals =
					typeDeclarations().declarations(ns);
			ImmutableList<TypeDecl> singles =
					imports().typeImports(ns)
							.stream()
							.map(single -> globalNames().typeDecl(single.getGlobalName(), false))
							.collect(ImmutableList.collector());
			ImmutableList<TypeDecl> groups =
					imports().typeGroupImports(ns)
							.stream()
							.flatMap(group -> typeDeclarations().declarations(globalNames().namespaceDecls(group.getGlobalName()).stream().findFirst().orElse(null)).stream())
							.filter(decl -> ((GlobalTypeDecl) decl).getAvailability().equals(Availability.PUBLIC))
							.collect(ImmutableList.collector());
			return ImmutableList.concat(ImmutableList.concat(locals, singles), groups);
		}

		@Override
		default Optional<TypeDecl> declaration(IRNode node) {
			return Optional.empty();
		}

		default Optional<TypeDecl> declaration(ExprTypeConstruction construction) {
			return declarationOf(construction, construction.getConstructor());
		}

		default Optional<TypeDecl> declaration(ExprVariable var) {
			return declarationOf(var, var.getVariable().getName());
		}

		default Optional<TypeDecl> declaration(NominalTypeExpr type) {
			return declarationOf(type, type.getName());
		}

		default Optional<TypeDecl> declarationOf(IRNode node, String name) {
			return typeDeclarations()
					.declarations(sourceUnit(node).getTree())
					.stream()
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> Objects.equals(decl.getName(), name))
					.map(TypeDecl.class::cast)
					.findAny();
		}

		@Override
		default Optional<TypeDecl> construction(IRNode node) {
			return Optional.empty();
		}

		default Optional<TypeDecl> construction(ExprTypeConstruction construction) {
			return constructionOf(construction, construction.getConstructor());
		}

		default Optional<TypeDecl> construction(ExprVariable var) {
			return constructionOf(var, var.getVariable().getName());
		}

		default Optional<TypeDecl> construction(NominalTypeExpr type) {
			return constructionOf(type, type.getName());
		}

		default Optional<TypeDecl> construction(PatternDeconstructor deconstructor) {
			return constructionOf(deconstructor, deconstructor.getName());
		}

		default Optional<TypeDecl> constructionOf(IRNode node, String name) {
			return typeDeclarations()
					.declarations(sourceUnit(node).getTree())
					.stream()
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> {
						if (decl.getDeclaration() instanceof ProductTypeDecl) {
							return Objects.equals(name, decl.getDeclaration().getName());
						} else if (decl.getDeclaration() instanceof SumTypeDecl) {
							return ((SumTypeDecl) decl.getDeclaration()).getVariants().stream().anyMatch(variant -> Objects.equals(name, variant.getName()));
						} else {
							return false;
						}
					})
					.map(TypeDecl.class::cast)
					.findAny();
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(CompilationTask task) {
			GlobalEntityDecl entityDecl = GlobalDeclarations.getEntity(task, task.getIdentifier());
			return sourceUnit(tree().parent(entityDecl));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
