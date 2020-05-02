package se.lth.cs.tycho.phase;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.compiler.UniqueNumbers;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.ActionCase;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionCaseToActionsPhase implements Phase {

	@Override
	public String getDescription() {
		return "Translates action case to Cal action constructs.";
	}

	@Override
	public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
		Transformation transformation = MultiJ.from(Transformation.class)
				.bind("numbers").to(context.getUniqueNumbers())
				.instance();
		return task.transformChildren(transformation);
	}

	@Module
	interface Transformation extends IRNode.Transformation {

		@Binding(BindingKind.INJECTED)
		UniqueNumbers numbers();

		@Override
		default IRNode apply(IRNode node) {
			return node.transformChildren(this);
		}

		default IRNode apply(CalActor actor) {
			Map<QID, List<QID>> tags = new HashMap<>();
			Map<QID, QID> mapping = new HashMap<>();

			List<Action> actions = new ArrayList<>(actor.getActions());
			List<ImmutableList<QID>> priorities = new ArrayList<>();

			for (ActionCase actionCase : actor.getActionCases()) {
				String name = actionCase.getTag() == null ? "$untagged" : actionCase.getTag().toString();

				List<Action> actions1 = actionCase.getActions().stream()
						.map(action -> action.withTag(QID.of(String.format("%s%d", name, numbers().next()))))
						.collect(Collectors.toList());
				actions.addAll(actions1);

				List<ImmutableList<QID>> priorities1 = Collections.singletonList(actions1.stream()
						.map(Action::getTag)
						.collect(ImmutableList.collector()));
				priorities.addAll(priorities1);

				if (actionCase.getTag() != null) {
					tags.put(actionCase.getTag(), actions1.stream().map(Action::getTag).collect(Collectors.toList()));
					actions1.forEach(action -> mapping.put(action.getTag(), actionCase.getTag()));
				}
			}

			for (ImmutableList<QID> priority : actor.getPriorities()) {
				priorities.addAll(substitute(priority, tags, mapping));
			}

			return actor.withActions(actions).withPriorities(priorities).withActionCases(Collections.emptyList());
		}

		default List<ImmutableList<QID>> substitute(List<QID> priority, Map<QID, List<QID>> tags, Map<QID, QID> mapping) {
			List<ImmutableList<QID>> substitution = new ArrayList<>();
			substituteAcc(priority, tags, new ArrayList<>(), substitution);
			return substitution.stream().filter(s -> isSubstitution(priority, s, mapping)).collect(Collectors.toList());
		}

		default void substituteAcc(List<QID> priority, Map<QID, List<QID>> tags, List<QID> prefix, List<ImmutableList<QID>> accumulator) {
			if (priority.isEmpty()) {
				accumulator.add(ImmutableList.from(prefix));
				return;
			}
			for (QID tag : priority) {
				for (QID newTag : tags.getOrDefault(tag, Collections.singletonList(tag))) {
					List<QID> newPrefix = new ArrayList<>(prefix); newPrefix.add(newTag);
					substituteAcc(priority.subList(1, priority.size()), tags, newPrefix, accumulator);
				}
			}
		}

		default boolean isSubstitution(List<QID> original, List<QID> substitution, Map<QID, QID> mapping) {
			return Lists.equals(original, substitution.stream().map(s -> mapping.getOrDefault(s, s)).collect(Collectors.toList()));
		}
	}
}
