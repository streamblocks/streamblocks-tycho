package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.type.NominalTypeExpr;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.Optional;

public interface TypeNamespaces {

	ModuleKey<TypeNamespaces> key = task -> MultiJ.from(Implementation.class)
			.bind("tree").to(task.getModule(TreeShadow.key))
			.bind("typeScopes").to(task.getModule(TypeScopes.key))
			.instance();

	Optional<NamespaceDecl> declaration(IRNode node);

	@Module
	interface Implementation extends TypeNamespaces {

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		@Binding(BindingKind.INJECTED)
		TypeScopes typeScopes();

		@Override
		default Optional<NamespaceDecl> declaration(IRNode node) {
			return Optional.empty();
		}

		default Optional<NamespaceDecl> declaration(NominalTypeExpr type) {
			return typeScopes().declarations(sourceUnit(type).getTree())
					.stream()
					.filter(decl -> decl.getName().equals(type.getName()))
					.map(decl -> sourceUnit(decl).getTree())
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
