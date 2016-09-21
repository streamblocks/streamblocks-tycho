package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.Decl;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.TypeDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public final class GlobalDeclarations {
	public static List<GlobalEntityDecl> findEntities(CompilationTask task, QID qid) {
		return findDeclarations(task, qid, GlobalEntityDecl.class);
	}

	public static GlobalEntityDecl getEntity(CompilationTask task, QID qid) {
		return getDeclaration(task, qid, GlobalEntityDecl.class);
	}

	public static List<TypeDecl> findTypes(CompilationTask task, QID qid) {
		return findDeclarations(task, qid, TypeDecl.class);
	}

	public static TypeDecl getType(CompilationTask task, QID qid) {
		return getDeclaration(task, qid, TypeDecl.class);
	}

	public static List<VarDecl> findVariables(CompilationTask task, QID qid) {
		return findDeclarations(task, qid, VarDecl.class);
	}

	public static VarDecl getVariable(CompilationTask task, QID qid) {
		return getDeclaration(task, qid, VarDecl.class);
	}

	private static <D extends Decl> D getDeclaration(CompilationTask task, QID qid, Class<D> type) {
		List<D> declarations = findDeclarations(task, qid, type);
		if (declarations.isEmpty()) {
			throw new NoSuchElementException(qid.toString());
		} else if (declarations.size() > 1) {
			throw new RuntimeException("Not unique identifier: " + qid.toString());
		} else {
			return declarations.get(0);
		}
	}
	private static <D extends Decl> List<D> findDeclarations(CompilationTask task, QID qid, Class<D> type) {
		QID namespace = qid.getButLast();
		String name = qid.getLast().toString();
		return task.getSourceUnits().stream()
				.filter(unit -> unit.getTree().getQID().equals(namespace))
				.flatMap(unit -> unit.getTree().getAllDecls().stream())
				.filter(type::isInstance)
				.filter(decl -> decl.getName().equals(name))
				.map(type::cast)
				.collect(Collectors.toList());
	}
}
