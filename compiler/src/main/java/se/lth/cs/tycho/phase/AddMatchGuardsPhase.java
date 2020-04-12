package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.List;
import java.util.stream.Collectors;

public class AddMatchGuardsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Add match expressions to action guards";
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
			return node.transformChildren(this::apply);
		}

		default IRNode apply(Action action) {
			List<Expression> guards = action.getInputPatterns().stream()
					.flatMap(input -> input.getMatches().stream())
					.map(match -> match.getExpression().deepClone())
					.collect(Collectors.toList());
			guards.addAll(action.getGuards());
			return action.withGuards(guards);
		}
	}
}
