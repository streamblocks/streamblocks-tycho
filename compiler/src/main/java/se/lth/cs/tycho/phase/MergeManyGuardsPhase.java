package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.IntegerSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.Collections;
import java.util.List;

public class MergeManyGuardsPhase implements Phase {
	public static final IntegerSetting guardMergeThreshold = new IntegerSetting() {
		@Override
		public String getKey() {
			return "guard-merge-threshold";
		}

		@Override
		public String getDescription() {
			return "The number of guards of an action at which the guards are merged to a conjunction.";
		}

		@Override
		public Integer defaultValue(Configuration configuration) {
			return 5;
		}
	};

	@Override
	public String getDescription() {
		return "Merges long lists of guards to conjunction of guards.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) {
		return MultiJ.from(GuardMergeTransformation.class)
				.bind("threshold").to(context.getConfiguration().get(guardMergeThreshold))
				.instance()
				.transform(task);
	}

	@Override
	public List<Setting<?>> getPhaseSettings() {
		return Collections.singletonList(guardMergeThreshold);
	}

	@Module
	interface GuardMergeTransformation {
		@Binding
		Integer threshold();

		default CompilationTask transform(CompilationTask task) {
			return task.transformChildren(this::transform);
		}

		default IRNode transform(IRNode node) {
			return node.transformChildren(this::transform);
		}

		default Action transform(Action action) {
			if (action.getGuards().size() >= threshold()) {
				return action.withGuards(ImmutableList.of(conjunction(action.getGuards())));
			} else {
				return action;
			}
		}

		default Expression conjunction(List<Expression> expressions) {
			Expression result = null;
			for (Expression expr : expressions) {
				if (result == null) {
					result = expr;
				} else {
					result = new ExprBinaryOp(ImmutableList.of("and"), ImmutableList.of(result, expr));
				}
			}
			return result == null ? new ExprLiteral(ExprLiteral.Kind.True) : result;
		}
	}


}
