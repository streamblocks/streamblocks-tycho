package se.lth.cs.tycho.phases.cal2am;

import se.lth.cs.tycho.decoration.StructuralEquality;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.ConstantEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Conditions {
	private final ImmutableList<Condition> conditions;
	private final Map<InputPattern, Integer> inputConditions;
	private final Map<OutputExpression, Integer> outputConditions;
	private final Map<Expression, Integer> predicateConditions;

	public Conditions(CalActor actor, ConstantEvaluator constants) {
		Map<TokenRate, Integer> inputRateCond = new HashMap<>();
		Map<InputPattern, Integer> inputCond = new HashMap<>();
		Map<TokenRate, Integer> outputRateCond = new HashMap<>();
		Map<OutputExpression, Integer> outputCond = new HashMap<>();
		Map<Expression, Integer> predicateCond = new HashMap<>();
		List<Condition> conditions = new ArrayList<>();
		for (Action action : actor.getActions()) {
			for (InputPattern input : action.getInputPatterns()) {
				TokenRate rate = tokenRate(constants, input);
				if (!inputRateCond.containsKey(rate)) {
					inputRateCond.put(rate, conditions.size());
					conditions.add(new PortCondition(input.getPort(), rate.tokens, true));
				}
				inputCond.put(input, inputRateCond.get(rate));
			}
			for (OutputExpression output : action.getOutputExpressions()) {
				TokenRate rate = tokenRate(constants, output);
				if (!outputRateCond.containsKey(rate)) {
					outputRateCond.put(rate, conditions.size());
					conditions.add(new PortCondition(output.getPort(), rate.tokens, false));
				}
				outputCond.put(output, outputRateCond.get(rate));
			}
			for (Expression guard : action.getGuards()) {
				Optional<Integer> conditionIndex = predicateCond.entrySet().stream()
						.filter(entry -> StructuralEquality.equals(entry.getKey(), guard))
						.findFirst()
						.map(Map.Entry::getValue);
				if (conditionIndex.isPresent()) {
					predicateCond.put(guard, conditionIndex.get());
				} else {
					predicateCond.put(guard, conditions.size());
					conditions.add(new PredicateCondition(guard));
				}
			}
		}
		this.conditions = ImmutableList.from(conditions);
		this.inputConditions = inputCond;
		this.outputConditions = outputCond;
		this.predicateConditions = predicateCond;
	}

	private static TokenRate tokenRate(ConstantEvaluator constants, InputPattern input) {
		int vars = input.getVariables().size();
		int repeat = evalRepeatMultiplier(constants, input.getRepeatExpr());
		return new TokenRate(input.getPort(), vars * repeat);
	}
	private static TokenRate tokenRate(ConstantEvaluator constants, OutputExpression output) {
		int vars = output.getExpressions().size();
		int repeat = evalRepeatMultiplier(constants, output.getRepeatExpr());
		return new TokenRate(output.getPort(), vars * repeat);
	}
	private static int evalRepeatMultiplier(ConstantEvaluator constants, Expression expr) {
		if (expr == null) {
			return 1;
		} else {
			return (int) constants.intValue(expr).getAsLong();
		}
	}

	private static final class TokenRate {
		private final Port port;
		private final int tokens;

		public TokenRate(Port port, int tokens) {
			this.port = port;
			this.tokens = tokens;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TokenRate tokenRate = (TokenRate) o;

			if (tokens != tokenRate.tokens) return false;
			return port.equals(tokenRate.port);
		}

		@Override
		public int hashCode() {
			int result = port.hashCode();
			result = 31 * result + tokens;
			return result;
		}
	}


	public List<Condition> getAllConditions() {
		return conditions;
	}

	public PortCondition getCondition(InputPattern input) {
		return (PortCondition) conditions.get(inputConditions.get(input));
	}

	public PortCondition getCondition(OutputExpression output) {
		return (PortCondition) conditions.get(outputConditions.get(output));
	}

	public PredicateCondition getCondition(Expression guard) {
		return (PredicateCondition) conditions.get(predicateConditions.get(guard));
	}

	public int getConditionIndex(InputPattern input) {
		return inputConditions.get(input);
	}

	public int getConditionIndex(OutputExpression output) {
		return outputConditions.get(output);
	}

	public int getConditionIndex(Expression guard) {
		return predicateConditions.get(guard);
	}
}
