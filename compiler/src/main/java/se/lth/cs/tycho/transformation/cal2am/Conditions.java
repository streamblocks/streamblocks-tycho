package se.lth.cs.tycho.transformation.cal2am;

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
import se.lth.cs.tycho.attribute.ConstantEvaluator;

import java.util.*;

public class Conditions {
	private final ImmutableList<Condition> conditions;
	private final Map<InputPattern, Integer> inputConditions;
	private final Map<OutputExpression, Integer> outputConditions;
	private final Map<Expression, Integer> predicateConditions;

	// A map with a condition as a key and a list of actions as the item. The list contains every action that
	// is dependent on the condition to execute. The inverse of conditionActionMap.
	private final Map<Condition, List<Action>> conditionActionMap;

	// A map with an action as key and a list of conditions as the item. The list contains every condition that the
	// action requires to be true before it can execute. The inverse of conditionActionMap.
	private final Map<Action, List<Condition>> actionConditionMap;

	// A map with a condition as key and a set of strings as the item. Each string is the name of an FSM state within
	// the actor. Each set contains all FSM states that the condition will be checked in.
	private final Map<Condition, Set<String>> conditionStateMap;


	private final ImmutableList<Condition> conditionsByOrder;

	public Conditions(CalActor actor, ConstantEvaluator constants, Schedule schedule) {
		Map<TokenRate, Integer> inputRateCond = new HashMap<>();
		Map<InputPattern, Integer> inputCond = new HashMap<>();
		Map<TokenRate, Integer> outputRateCond = new HashMap<>();
		Map<OutputExpression, Integer> outputCond = new HashMap<>();
		Map<Expression, Integer> predicateCond = new HashMap<>();
		List<Condition> conditions = new ArrayList<>();

		// 1. Create all conditions
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


		// 2. Set up the conditionActionMap.
		// 3. Set up the actionConditionMap.
		// 4. Set up the conditionStateMap.
		Map<Condition, List<Action>> conditionActionMap = new HashMap<>();
		Map<Action, List<Condition>> actionConditionMap = new HashMap<>();
		Map<Condition, Set<String>> conditionStateMap = new HashMap<>();
		for (Action action : actor.getActions()) {
			List<Condition> conditionInActionMap = new ArrayList<>();
			for (InputPattern input : action.getInputPatterns()) {
				Condition condition = getCondition(input);
				if(!conditionActionMap.containsKey(condition)){
					List<Action> actionList = new ArrayList<>();
					actionList.add(action);
					conditionActionMap.put(condition,actionList);
					Set<String> stateSet = schedule.getStates(action);
					conditionStateMap.put(condition,stateSet);
				}else{
					conditionActionMap.get(condition).add(action);
					conditionStateMap.get(condition).addAll(schedule.getStates(action));
				}
				conditionInActionMap.add(condition);
			}

			for (OutputExpression output : action.getOutputExpressions()) {
				Condition condition = getCondition(output);
				if(!conditionActionMap.containsKey(condition)){
					List<Action> actionList = new ArrayList<>();
					actionList.add(action);
					conditionActionMap.put(condition,actionList);
					Set<String> stateSet = schedule.getStates(action);
					conditionStateMap.put(condition,stateSet);
				}else{
					conditionActionMap.get(condition).add(action);
					conditionStateMap.get(condition).addAll(schedule.getStates(action));
				}
				conditionInActionMap.add(condition);
			}

			for (Expression guard : action.getGuards()) {
				Condition condition = getCondition(guard);
				if(!conditionActionMap.containsKey(condition)){
					List<Action> actionList = new ArrayList<>();
					actionList.add(action);
					conditionActionMap.put(condition,actionList);
					Set<String> stateSet = schedule.getStates(action);
					conditionStateMap.put(condition,stateSet);
				}else{
					conditionActionMap.get(condition).add(action);
					conditionStateMap.get(condition).addAll(schedule.getStates(action));
				}
				conditionInActionMap.add(condition);
			}
			actionConditionMap.put(action,conditionInActionMap);
		}
		this.conditionActionMap = conditionActionMap;
		this.conditionStateMap = conditionStateMap;
		this.actionConditionMap = actionConditionMap;


		// 5. Set up the condition order number
		this.conditionsByOrder = assignConditionOrder();

	}

	private static TokenRate tokenRate(ConstantEvaluator constants, InputPattern input) {
		int matches = input.getMatches().size();
		int repeat = evalRepeatMultiplier(constants, input.getRepeatExpr());
		return new TokenRate(input.getPort(), matches * repeat);
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

	/**
	 * Assign an order number to each condition and generate a list of conditions sorted by this order number ascending.
	 *
	 * This ordering should be described in more detail in: "Callanan, G and Gruian, F, Hardware and Software Generation
	 * from Large Actor Machines"
	 *
	 * This function recursively calls recursiveOrdering. The ordering works as follows:
	 * 1. Start with a set of all unsorted conditions.
	 * 2. Select the condition with the most associated actions and assign this the next order number. Remove this from
	 *    the set
	 * 3. Create a new set of conditions where all conditions contain a subset of the actions covered by the condition
	 *    selected in step 2. (similar to what is done in getContainedActions() function)
	 * 4. Repeat steps 2 and 3 recursively until no conditions are left in the subset, then move one subset up select
	 *    the condition with the largest number of associated actions and repeat steps 2 and 3 again.
	 *
	 */
	private ImmutableList<Condition> assignConditionOrder(){
		// 1. Order conditions by number of actions
		List<Condition> orderedConditionByNumActions = new ArrayList<>();
		orderedConditionByNumActions.addAll(conditions);
		Collections.sort(orderedConditionByNumActions, (o1, o2) -> (new Integer(conditionActionMap.get(o1).size())).compareTo(new Integer(conditionActionMap.get(o2).size())) );
		Collections.reverse(orderedConditionByNumActions);

		// 2. Do a nested search for all conditions
		List<Condition> conditionOrder = new ArrayList<>();
		Set<Condition> addedConditions = new HashSet<>();
		for(Condition condition: orderedConditionByNumActions){
			if(!addedConditions.contains(condition)) {
				addedConditions.add(condition);
				conditionOrder.add(condition);
				List<Condition> newConditions = recursiveOrdering(condition, addedConditions, orderedConditionByNumActions);
				conditionOrder.addAll(newConditions);
			}
		}

		// 3. Assign order to the conditions based on the order they fall within the list
		int orderNumber = 0;
		for (Condition condition: conditionOrder) {
			condition.setOrderNumber(orderNumber++);
		}

		return ImmutableList.from(conditionOrder);
	}

	/**
	 * Recursive funtion called by assignConditionOrder() function
	 *
	 * @param inCondition The condition at the top of this subset
	 * @param addedConditions Set of all conditions that have already been assigned a priority and must be ignored.
	 * @param orderedConditionByNumActions List of all conditions ordered by the number of actions each condition affects ascending
	 * @return List of conditions ordered in the order the order number must be assigned.
	 */
	private List<Condition> recursiveOrdering(Condition inCondition, Set<Condition> addedConditions, List<Condition> orderedConditionByNumActions) {
		List<Action> actionList = conditionActionMap.get(inCondition);
		List<Condition> subConditions = new ArrayList<>();

		// 1. Get all conditions that have the same or less actions as covered by the in condition
		for (Condition condition: orderedConditionByNumActions) {
			List<Action> newActionList = conditionActionMap.get(condition);
			if(actionList.containsAll(newActionList)){
				subConditions.add(condition);
			}
		}

		// 2. Recursively add conditions to the list.
		List<Condition> toReturn = new ArrayList<>();
		for (Condition condition: subConditions) {
			if(!addedConditions.contains(condition)) {
				addedConditions.add(condition);
				toReturn.add(condition);
				toReturn.addAll(recursiveOrdering(condition, addedConditions, orderedConditionByNumActions));
			}
		}

		return toReturn;
	}

	/**
	 * Find all conditions where the actions covered by the condition are a subset of the actions in the inCondition
	 *
	 * More complete description:
	 * Assume that there exist conditions c_i where A_i is the set of all possible actions requiring c_i to be true, and
	 * c_j where A_j is the set of all possible actions requiring c_j to be true. Also assume A_j is a subset of A_i (A_j ⊆
	 * A_i). The actions in A_j depend on both c_i and c_j with no other actions depending on c_j . This function returns
	 * a list of all c_j conditions for a given c_i (where c_i is the inCondition).
	 */
	public List<Condition> getContainedActions(Condition inCondition) {
		List<Condition> conditions = new ArrayList<>();
		List<Action> actions = conditionActionMap.get(inCondition);

		for (Condition condition: this.conditions) {
			List<Action> conditionActions = conditionActionMap.get(condition);
			if(actions.containsAll(conditionActions)){ // If the conditions target
				conditions.add(condition);
			}
		}

		return  conditions;
	}

	/**
	 * Get all CAL FSM states within the actor where at least one action in that state relies on the given condition.
	 *
	 * @param condition Condition to get associated FSM states.
	 * @return Set of strings where each string is the name of a different FSM state.
	 */
	public Set<String> getStatesForCondition(Condition condition){
		return conditionStateMap.get(condition);
	}

	/**
	 * Get every condition that covers the same actions covered by the input condition.
	 *
	 * More complete description:
	 * If input condition c_i has a set of associated actions A_i ∈ {a_z ... a_n} and each a_i has a set of associate
	 * conditions in C_i ∈ {c_x ... c_y}, then return all condition of the union of all conditions from each action
	 * in A_I: C_z ∪ ... ∪ C_n.
 	 */
	public Set<Condition> getCommonActionConditions(Condition inCondition){
		Set<Condition> commonConditions = new HashSet<>();
		for (Action action: conditionActionMap.get(inCondition)) {
			commonConditions.addAll(actionConditionMap.get(action));
		}
		return commonConditions;
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

	/**
	 * Get all conditions ordered by condition order number ascending.
	 *
	 * @return List of ordered conditions
	 */
	public List<Condition> getAllConditionsInOrder() {
		return conditionsByOrder;
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
