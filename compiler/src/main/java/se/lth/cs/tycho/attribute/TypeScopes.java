package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.TreeShadow;

public interface TypeScopes {

	ModuleKey<TypeScopes> key = task -> MultiJ.from(TypeScopes.Implementation.class)
			.bind("tree").to(task.getModule(TreeShadow.key))
			.bind("imports").to(task.getModule(ImportDeclarations.key))
			.bind("globalNames").to(task.getModule(GlobalNames.key))
			.bind("typeDeclarations").to(task.getModule(TypeDeclarations.key))
			.instance();

	ImmutableList<TypeDecl> declarations(IRNode node);

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
	}
}
