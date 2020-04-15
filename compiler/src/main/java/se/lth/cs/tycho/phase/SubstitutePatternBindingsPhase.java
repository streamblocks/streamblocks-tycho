package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.entity.cal.Match;
import se.lth.cs.tycho.ir.expr.ExprCase;
import se.lth.cs.tycho.ir.expr.pattern.PatternBinding;
import se.lth.cs.tycho.ir.expr.pattern.PatternVariable;
import se.lth.cs.tycho.reporting.CompilationException;

public class SubstitutePatternBindingsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Substitutes pattern bindings of match expression with pattern variables";
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
			if (match.getExpression() != null) {
				Substitution substitution = MultiJ.from(Substitution.class).instance();
				return match.withExpression((ExprCase) substitution.apply(match.getExpression()));
			}
			return match;
		}
	}

	@Module
	interface Substitution extends IRNode.Transformation {

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(PatternBinding pattern) {
			Variable variable = Variable.variable(pattern.getDeclaration().getName());
			variable.setPosition(pattern, pattern);
			return new PatternVariable(pattern, variable);
		}
	}
}
