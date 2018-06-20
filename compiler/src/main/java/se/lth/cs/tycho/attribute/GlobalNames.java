package se.lth.cs.tycho.attribute;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Availability;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.phase.TreeShadow;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GlobalNames {
	ModuleKey<GlobalNames> key = task -> MultiJ.from(Implementation.class)
            .bind("root").to(task)
            .bind("tree").to(task.getModule(TreeShadow.key))
            .instance();

	GlobalEntityDecl entityDecl(QID qid, boolean includingPrivate);
	VarDecl varDecl(QID qid, boolean includingPrivate);
	TypeDecl typeDecl(QID qid, boolean includingPrivate);

	Set<NamespaceDecl> namespaceDecls(QID qid);

	Optional<QID> globalName(Decl declaration);

	@Module
	interface Implementation extends GlobalNames {
		@Binding(BindingKind.INJECTED)
		CompilationTask root();

		@Binding(BindingKind.INJECTED)
		TreeShadow tree();

		default Optional<QID> globalName(IRNode node) {
			return Optional.empty();
		}

		default Optional<QID> globalName(Decl decl) {
			return globalName(tree().parent(decl))
					.map(parent -> parent.concat(QID.of(decl.getName())));
		}

		default Optional<QID> globalName(NamespaceDecl decl) {
			return Optional.of(decl.getQID());
		}


		@Override
		default GlobalEntityDecl entityDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getEntityDecls().stream(), includingPrivate);
		}

		@Override
		default GlobalVarDecl varDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getVarDecls().stream(), includingPrivate);
		}

		@Override
		default TypeDecl typeDecl(QID qid, boolean includingPrivate) {
			return find(qid, ns -> ns.getTypeDecls().stream(), includingPrivate);
		}

		default <D extends GlobalDecl> D find(QID qid, Function<NamespaceDecl, Stream<D>> getDecls, boolean includingPrivate) {
			QID ns = qid.getButLast();
			String id = qid.getLast().toString();
			EnumSet<Availability> availability = includingPrivate ? pubPriv : pub;
			return root().getSourceUnits().stream()
					.map(SourceUnit::getTree)
					.filter(tree -> tree.getQID().equals(ns))
					.flatMap(getDecls)
					.filter(decl -> decl.getName().equals(id))
					.filter(decl -> availability.contains(decl.getAvailability()))
					.findFirst()
					.orElse(null);
		}

		default Set<NamespaceDecl> namespaceDecls(QID qid) {
			return root().getSourceUnits().stream()
					.map(SourceUnit::getTree)
					.filter(tree -> tree.getQID().equals(qid))
					.collect(Collectors.toSet());
		}

		EnumSet<Availability> pub = EnumSet.of(Availability.PUBLIC);
		EnumSet<Availability> pubPriv = EnumSet.of(Availability.PUBLIC, Availability.PRIVATE);

	}
}
