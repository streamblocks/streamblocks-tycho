package se.lth.cs.tycho.phases.cal2am;

import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phases.attributes.Constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Conditions {
	private final CalActor actor;
	private boolean initialized;
	private ImmutableList<Condition> conditions;
	private Map<InputPattern, Integer> inputConditions;
	private Map<OutputExpression, Integer> outputConditions;
	private Map<Expression, Integer> predicateConditions;
	private final Constants constants;

	public Conditions(CalActor actor, Constants constants) {
		this.actor = actor;
		this.initialized = false;
		this.constants = constants;
	}

	private void init() {
		if (!initialized) {
			Map<Port, Map<Integer, PortCondition>> inputConditionMap = new LinkedHashMap<>();
			Map<Port, Map<Integer, PortCondition>> outputConditionMap = new LinkedHashMap<>();
			for (Action action : actor.getActions()) {
				for (InputPattern input : action.getInputPatterns()) {
					PortCondition cond = createCondition(input);
					inputConditionMap.computeIfAbsent(cond.getPortName(), p -> new LinkedHashMap<>())
							.putIfAbsent(cond.N(), cond);
				}
				for (OutputExpression output : action.getOutputExpressions()) {
					PortCondition cond = createCondition(output);
					outputConditionMap.computeIfAbsent(cond.getPortName(), p -> new LinkedHashMap<>())
							.putIfAbsent(cond.N(), cond);
				}
			}

			Set<Condition> conditions = new LinkedHashSet<>();
			inputConditions = new HashMap<>();
			outputConditions = new HashMap<>();
			predicateConditions = new HashMap<>();
			for (Action action : actor.getActions()) {
				for (InputPattern input : action.getInputPatterns()) {
					PortCondition cond = createCondition(input);
					cond = inputConditionMap.get(cond.getPortName()).get(cond.N());
					if (conditions.add(cond)) {
						inputConditions.put(input, conditions.size() - 1);
					}
				}
				for (OutputExpression output : action.getOutputExpressions()) {
					PortCondition cond = createCondition(output);
					cond = outputConditionMap.get(cond.getPortName()).get(cond.N());
					if (conditions.add(cond)) {
						outputConditions.put(output, conditions.size() - 1);
					}
				}
				for (Expression guard : action.getGuards()) {
					PredicateCondition cond = createCondition(guard);
					predicateConditions.put(guard, conditions.size());
					conditions.add(cond);
				}
			}
			this.conditions = conditions.stream().collect(ImmutableList.collector());
			initialized = true;
		}
	}

	private int evalRepeatMultiplier(Expression expr) {
		if (expr == null) {
			return 1;
		} else {
			return constants.intValue(expr).getAsInt();
		}
	}

	private PortCondition createCondition(InputPattern inputPattern) {
		return new PortCondition(
				(Port) inputPattern.getPort().deepClone(),
				inputPattern.getVariables().size() * evalRepeatMultiplier(inputPattern.getRepeatExpr()),
				true);
	}

	private PortCondition createCondition(OutputExpression outputExpression) {
		return new PortCondition(
				(Port) outputExpression.getPort().deepClone(),
				outputExpression.getExpressions().size() * evalRepeatMultiplier(outputExpression.getRepeatExpr()),
				false);
	}

	private PredicateCondition createCondition(Expression guard) {
		return new PredicateCondition(guard);
	}

	public List<Condition> getAllConditions() {
		init();
		return conditions;
	}

	public PortCondition getCondition(InputPattern input) {
		init();
		return (PortCondition) conditions.get(inputConditions.get(input));
	}

	public PortCondition getCondition(OutputExpression output) {
		init();
		return (PortCondition) conditions.get(outputConditions.get(output));
	}

	public PredicateCondition getCondition(Expression guard) {
		init();
		return (PredicateCondition) conditions.get(predicateConditions.get(guard));
	}

	public int getConditionIndex(InputPattern input) {
		init();
		return inputConditions.get(input);
	}

	public int getConditionIndex(OutputExpression output) {
		init();
		return outputConditions.get(output);
	}

	public int getConditionIndex(Expression guard) {
		init();
		return predicateConditions.get(guard);
	}
}
