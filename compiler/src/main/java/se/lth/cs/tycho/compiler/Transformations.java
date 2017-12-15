package se.lth.cs.tycho.compiler;

import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;

import java.util.function.Function;

public final class Transformations {
	public static CompilationTask transformEntityDecls(CompilationTask task, Function<GlobalEntityDecl, GlobalEntityDecl> transformation) {
		return task.withSourceUnits(task.getSourceUnits().map(unit ->
				unit.withTree(unit.getTree().withEntityDecls(unit.getTree().getEntityDecls().map(transformation)))));
	}
}
