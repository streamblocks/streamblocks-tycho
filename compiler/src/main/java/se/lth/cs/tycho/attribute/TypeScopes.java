package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.expr.ExprConstruction;
import se.lth.cs.tycho.ir.expr.ExprVariable;
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

		default Optional<TypeDecl> declaration(ExprConstruction construction) {
			return typeDeclarations()
					.declarations(sourceUnit(construction).getTree())
					.stream()
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> Objects.equals(decl.getName(), construction.getType()))
					// name analysis ??? .filter(decl -> decl.getTuples().stream().anyMatch(tuple -> Objects.equals(tuple.getName(), reference.getChild())))
					.map(TypeDecl.class::cast)
					.findAny();
		}

		default Optional<TypeDecl> declaration(ExprVariable var) {
			return typeDeclarations()
					.declarations(sourceUnit(var).getTree())
					.stream()
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> Objects.equals(decl.getName(), var.getVariable().getName()))
					// name analysis ??? .filter(decl -> decl.getTuples().stream().anyMatch(tuple -> Objects.equals(tuple.getName(), reference.getChild())))
					.map(TypeDecl.class::cast)
					.findAny();
		}

		default Optional<TypeDecl> declaration(NominalTypeExpr type) {
			return typeDeclarations()
					.declarations(sourceUnit(type).getTree())
					.stream()
					.map(GlobalTypeDecl.class::cast)
					.filter(decl -> Objects.equals(decl.getName(), type.getName()))
					// name analysis ??? .filter(decl -> decl.getTuples().stream().anyMatch(tuple -> Objects.equals(tuple.getName(), reference.getChild())))
					.map(TypeDecl.class::cast)
					.findAny();
		}

		default SourceUnit sourceUnit(IRNode node) {
			return sourceUnit(tree().parent(node));
		}

		default SourceUnit sourceUnit(SourceUnit unit) {
			return unit;
		}
	}
}
