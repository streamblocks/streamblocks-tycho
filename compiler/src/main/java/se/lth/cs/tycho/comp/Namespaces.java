package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalDecl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.GlobalTypeDecl;
import se.lth.cs.tycho.ir.decl.GlobalVarDecl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Namespaces {
	public static Stream<GlobalEntityDecl> findEntities(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getEntityDecls, qid);
	}

	public static Stream<GlobalEntityDecl> findEntities(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getEntityDecls, qid);
	}

	public static Stream<GlobalVarDecl> findVariables(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getVarDecls, qid);
	}

	public static Stream<GlobalVarDecl> findVariables(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getVarDecls, qid);
	}

	public static Stream<GlobalTypeDecl> findTypes(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getTypeDecls, qid);
	}

	public static Stream<GlobalTypeDecl> findTypes(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getTypeDecls, qid);
	}

	private static <D extends GlobalDecl> Stream<D> find(CompilationTask task, Function<NamespaceDecl, List<D>> decl, QID qid) {
		return task.getSourceUnits().stream().flatMap(unit -> find(unit, decl, qid));
	}
	private static <D extends GlobalDecl> Stream<D> find(SourceUnit unit, Function<NamespaceDecl, List<D>> decls, QID qid) {
		if (unit.getTree().getQID().equals(qid.getButLast())) {
			return decls.apply(unit.getTree()).stream()
					.filter(decl -> decl.getName().equals(qid.getLast().toString()));
		} else {
			return Stream.empty();
		}
	}
}
