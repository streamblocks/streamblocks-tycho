package se.lth.cs.tycho.phases.cal2am;

import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.instance.am.ctrl.Controller;
import se.lth.cs.tycho.instance.am.ctrl.Instruction;
import se.lth.cs.tycho.instance.am.ctrl.State;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalController implements Controller {
	private final CalActor actor;
	private final Map<String, BitSet> actionsInState;
	private final Map<Expression, PredicateCondition> guards;
	private final Map<String, Map<Integer, PortCondition>> inputConditions;
	private final Map<String, Map<Integer, PortCondition>> outputConditions;

	public CalController(CalActor actor) {
		this.actor = actor;
		this.actionsInState = new HashMap<>();
		this.guards = new HashMap<>();
		this.inputConditions = new HashMap<>();
		this.outputConditions = new HashMap<>();
	}

	private Map<QID, Set<QID>> computePriorityMap() {
		Map<QID, Set<QID>> priority = new HashMap<>();
		for(ImmutableList<QID> prioritySeq : actor.getPriorities()) {
			QID high = null;
			for (QID tag : prioritySeq) {
				if (high != null) {
					priority.computeIfAbsent(high, h -> new HashSet<>()).add(tag);
				}
				high = tag;
			}
		}
		return priority.keySet().stream()
				.collect(Collectors.toMap(Function.identity(), qid -> transitivity(priority, qid)));
	}

	private Set<QID> transitivity(Map<QID, Set<QID>> relation, QID lhs) {
		Set<QID> result = new HashSet<>();
		Queue<QID> queue = new ArrayDeque<>();
		queue.addAll(relation.getOrDefault(lhs, Collections.emptySet()));
		while (!queue.isEmpty()) {
			QID qid = queue.remove();
			if (result.add(qid)) {
				queue.addAll(relation.getOrDefault(qid, Collections.emptySet()));
			}
		}
		return result;
	}

	private PredicateCondition predicateCondition(Expression guard) {
		return guards.computeIfAbsent(guard, g -> new PredicateCondition(g, null));
	}

	private PortCondition inputCondition(Port port, int tokens) {
		return inputConditions.computeIfAbsent(port.getName(), p -> new HashMap<>())
				.computeIfAbsent(tokens, t -> new PortCondition(port, tokens, true));
	}

	private PortCondition outputCondition(Port port, int tokens) {
		return outputConditions.computeIfAbsent(port.getName(), p -> new HashMap<>())
				.computeIfAbsent(tokens, t -> new PortCondition(port, tokens, true));
	}

	@Override
	public State getInitialState() {
		return new CalState();
	}

	private BitSet actionsInState(String state) {
		BitSet result = new BitSet();
		result.or(actionsInState.computeIfAbsent(state, this::computeActionsInState));
		return result;
	}

	private BitSet computeActionsInState(String state) {
		Set<QID> tags = actor.getScheduleFSM().getTransitions().stream()
				.filter(transition -> transition.getSourceState().equals(state))
				.map(transition -> transition.getActionTags())
				.flatMap(List::stream)
				.collect(Collectors.toSet());
		int i = 0;
		BitSet result = new BitSet();
		for (Action action : actor.getActions()) {
			if (action.getTag() == null || tags.contains(action.getTag())) {
				result.set(i);
			}
			i = i + 1;
		}
		return result;
	}

	private BitSet highestPriority(BitSet actions) {
		return null;
	}

	private int requiredTokens(InputPattern inputPattern) {
		int repeat = getRepeat(inputPattern.getRepeatExpr());
		return inputPattern.getVariables().size() * repeat;
	}

	private int requiredSpace(OutputExpression outputExpression) {
		int repeat = getRepeat(outputExpression.getRepeatExpr());
		return outputExpression.getExpressions().size() * repeat;
	}

	private int getRepeat(Expression expr) {
		int repeat;
		if (expr == null) {
			repeat = 1;
		} else {
			assert expr instanceof ExprLiteral;
			ExprLiteral literal = (ExprLiteral) expr;
			assert literal.getKind() == ExprLiteral.Kind.Integer;
			repeat = Integer.parseInt(literal.getText());
		}
		return repeat;
	}

	private enum Info {
		TRUE, FALSE, UNKNOWN;
		public static Info ofNullable(Boolean info) {
			return info == null ? UNKNOWN : (info ? TRUE : FALSE);
		}
	}

	private class CalState implements State {
		private ImmutableList<Instruction> instructions;
		private String scheduleState;
		private Map<Port, Integer> tokensMin;
		private Map<Port, Integer> tokensMax;
		private Map<Port, Integer> spaceMin;
		private Map<Port, Integer> spaceMax;
		private Map<Expression, Boolean> conditions;


		private boolean isSelected(int actionIndex) {
			Action action = actor.getActions().get(actionIndex);
			return action.getInputPatterns().stream().allMatch(in -> tokens(in) == Info.TRUE)
					&& action.getGuards().stream().allMatch(guard -> guard(guard) == Info.TRUE);
		}

		private boolean canFire(int actionIndex) {
			Action action = actor.getActions().get(actionIndex);
			return isSelected(actionIndex) && action.getOutputExpressions().stream().allMatch(out -> space(out) == Info.TRUE);
		}

		private boolean isDisabled(int actionIndex) {
			Action action = actor.getActions().get(actionIndex);
			return action.getInputPatterns().stream().anyMatch(in -> tokens(in) == Info.FALSE)
					|| action.getGuards().stream().anyMatch(guard -> guard(guard) == Info.FALSE);
		}

		private Info tokens(InputPattern in) {
			return checkBounds(in.getPort(), requiredTokens(in), tokensMin, tokensMax);
		}
		private Info space(OutputExpression out) {
			return checkBounds(out.getPort(), requiredSpace(out), tokensMin, tokensMax);
		}
		private Info guard(Expression guard) {
			return Info.ofNullable(conditions.get(guard));
		}

		@Override
		public List<Instruction> getInstructions() {
			if (instructions == null) {
				instructions = createTransitions();
			}
			return instructions;
		}

		private ImmutableList<Instruction> createTransitions() {
			BitSet actionsInState = actionsInState(scheduleState);
			BitSet canFire = actionsInState.stream().filter(this::canFire).collect(BitSet::new, BitSet::set, BitSet::or);
			if (!canFire.isEmpty()) {
				return null;
			}
			BitSet isNotDisabled = actionsInState.stream().filter(a -> !isDisabled(a)).collect(BitSet::new, BitSet::set, BitSet::or);
			Stream<InputPattern> unknownInput = isNotDisabled.stream().mapToObj(actor.getActions()::get)
					.map(Action::getInputPatterns)
					.flatMap(List::stream)
					.filter(in -> tokens(in) == Info.UNKNOWN);
			Stream<OutputExpression> unknownOutput = isNotDisabled.stream().mapToObj(actor.getActions()::get)
					.map(Action::getOutputExpressions)
					.flatMap(List::stream)
					.filter(out -> space(out) == Info.UNKNOWN);
			Stream<Expression> unknownGuards = isNotDisabled.stream().mapToObj(actor.getActions()::get)
					.map(Action::getGuards)
					.flatMap(List::stream)
					.filter(guard -> guard(guard) == Info.UNKNOWN);

			return null;
		}
	}

	private Info checkBounds(Port port, int n, Map<Port, Integer> min, Map<Port, Integer> max) {
		if (min.getOrDefault(port, 0) >= n) {
			return Info.TRUE;
		} else if (max.getOrDefault(port, Integer.MAX_VALUE) < n) {
			return Info.FALSE;
		} else {
			return Info.UNKNOWN;
		}
	}
}
