package se.lth.cs.tycho.phase;

import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.Transition;
import se.lth.cs.tycho.ir.expr.pattern.Pattern;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlias;
import se.lth.cs.tycho.ir.expr.pattern.PatternAlternative;
import se.lth.cs.tycho.ir.expr.pattern.PatternDeconstruction;
import se.lth.cs.tycho.ir.expr.pattern.PatternExpression;
import se.lth.cs.tycho.ir.expr.pattern.PatternList;
import se.lth.cs.tycho.ir.expr.pattern.PatternLiteral;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AddPrioritiesPhase implements Phase {

	@Override
	public String getDescription() {
		return "Adds priorities to actors that don't have any.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class).instance();
		return task.transformChildren(transformation);
	}

	private enum Priority {
		NULL(0), LOW(1), HIGH(5);

		final long value;

		Priority(long value) {
			this.value = value;
		}
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(CalActor actor) {

			Map<QID, List<Action>> actionsOf = actor.getActions().stream().collect(Collectors.groupingBy(Action::getTag));

			List<List<Action>> states = actor.getScheduleFSM().getTransitions().stream()
					.collect(Collectors.groupingBy(Transition::getSourceState))
					.values()
					.stream()
					.flatMap(transitions -> transitions.stream().map(transition -> transition.getActionTags().stream()))
					.map(tags -> tags.filter(tag -> actionsOf.containsKey(tag)).map(tag -> actionsOf.get(tag).get(0)).collect(Collectors.toList()))
					.filter(actions -> !(actions.isEmpty()))
					.collect(Collectors.toList());

			List<ImmutableList<QID>> priorities = new ArrayList<>(actor.getPriorities());

			for (List<Action> state : states) {
				Map<Set<Port>, List<Action>> prioritizable = state.stream()
						.filter(action -> !(action.getInputPatterns().isEmpty()))
						.collect(Collectors.groupingBy(action -> action.getInputPatterns().stream().map(InputPattern::getPort).collect(Collectors.toSet())));

				List<ImmutableList<QID>> priorities1 = prioritizable.values().stream()
						.flatMap(actions -> actions.stream().flatMap(thiz -> actions.stream()
								.filter(that -> thiz != that && priorityOf(thiz) > priorityOf(that))
								.map(that -> ImmutableList.of(thiz.getTag(), that.getTag()))))
						.collect(Collectors.toList());

				priorities.addAll(priorities1);
			}

			return actor.withPriorities(priorities);
		}

		default long priorityOf(Action action) {
			return action.getInputPatterns().stream()
					.flatMap(input -> input.getMatches().stream().map(match -> priority(match.getExpression().getAlternatives().get(0).getPattern())))
					.reduce(Priority.NULL.value, (acc, p) -> acc + p);
		}

		default long priority(Pattern pattern) {
			return Priority.NULL.value;
		}

		default long priority(PatternLiteral pattern) {
			return Priority.HIGH.value;
		}

		default long priority(PatternAlias pattern) {
			return Priority.LOW.value;
		}

		default long priority(PatternExpression pattern) {
			return Priority.LOW.value;
		}

		default long priority(PatternDeconstruction pattern) {
			return pattern.getPatterns().stream().map(this::priority).reduce(Priority.HIGH.value, (acc, p) -> acc + p);
		}

		default long priority(PatternList pattern) {
			return pattern.getPatterns().stream().map(this::priority).reduce(Priority.HIGH.value, (acc, p) -> acc + p);
		}

		default long priority(PatternAlternative pattern) {
			return pattern.getPatterns().stream().map(this::priority).reduce(Priority.NULL.value, (acc, p) -> acc + p);
		}
	}
}
