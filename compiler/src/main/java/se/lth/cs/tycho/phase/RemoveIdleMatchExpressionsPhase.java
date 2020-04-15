package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeclaration;
import se.lth.cs.tycho.reporting.CompilationException;

public class RemoveIdleMatchExpressionsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Removes idle expressions of matches";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class).instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(Match match) {
			Pattern pattern = match.getExpression().getAlternatives().get(0).getPattern();
			if (pattern instanceof PatternDeclaration) {
				return match
						.withDeclaration(match.getDeclaration().withName(((PatternDeclaration) pattern).getDeclaration().getName()))
						.withExpression(null);
			}
			return match;
		}
	}
}
