package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.NamespaceDecl;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.EntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Namespaces {
	public static Stream<EntityDecl> findEntities(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getEntityDecls, qid);
	}

	public static Stream<EntityDecl> findEntities(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getEntityDecls, qid);
	}

	public static Stream<VarDecl> findVariables(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getVarDecls, qid);
	}

	public static Stream<VarDecl> findVariables(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getVarDecls, qid);
	}

	public static Stream<TypeDecl> findTypes(CompilationTask task, QID qid) {
		return find(task, NamespaceDecl::getTypeDecls, qid);
	}

	public static Stream<TypeDecl> findTypes(SourceUnit unit, QID qid) {
		return find(unit, NamespaceDecl::getTypeDecls, qid);
	}

	private static <D extends Decl> Stream<D> find(CompilationTask task, Function<NamespaceDecl, List<D>> decl, QID qid) {
		return task.getSourceUnits().stream().flatMap(unit -> find(unit, decl, qid));
	}
	private static <D extends Decl> Stream<D> find(SourceUnit unit, Function<NamespaceDecl, List<D>> decls, QID qid) {
		if (unit.getTree().getQID().equals(qid.getButLast())) {
			return decls.apply(unit.getTree()).stream()
					.filter(decl -> decl.getName().equals(qid.getLast().toString()));
		} else {
			return Stream.empty();
		}
	}
}
