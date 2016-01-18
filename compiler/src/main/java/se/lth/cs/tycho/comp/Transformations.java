package se.lth.cs.tycho.comp;

import se.lth.cs.tycho.ir.decl.EntityDecl;

import java.util.function.Function;

public final class Transformations {
	public static CompilationTask transformEntityDecls(CompilationTask task, Function<EntityDecl, EntityDecl> transformation) {
		return task.withSourceUnits(task.getSourceUnits().map(unit ->
				unit.withTree(unit.getTree().withEntityDecls(unit.getTree().getEntityDecls().map(transformation)))));
	}
}
