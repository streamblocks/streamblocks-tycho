package se.lth.cs.tycho.transform.caltoam;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.transform.caltoam.CalActorStates.State;
import se.lth.cs.tycho.transform.caltoam.util.BitSets;
import se.lth.cs.tycho.transform.caltoam.util.TestResult;
import se.lth.cs.tycho.util.DAG;

class ConditionHandler {
	private DAG dependencies;
	private BitSet[] actionConditions;
	private BitSet[] actionOutputConditions;

	private ConditionHandler(BitSet[] actionConditions, DAG dependencies, BitSet[] actionOutputConditions) {
		this.actionConditions = actionConditions;
		this.dependencies = dependencies;
		this.actionOutputConditions = actionOutputConditions;
	}

	/** The conjunction of all test results for <code>action</code>. */
	private TestResult testActionConditions(State state, int action) {
		TestResult result = TestResult.True;
		for (int cond : BitSets.iterable(actionConditions[action])) {
			TestResult r = state.getResult(cond);
			switch (r) {
			case Unknown:
				result = TestResult.Unknown;
				break;
			case False:
				return TestResult.False;
			case True:
			}
		}
		return result;
	}

	/**
	 * Returns a subset of the conditions of <code>action</code> feasible for
	 * testing. A condition is feasible for testing if (1) the action is not
	 * disabled and (2) the conditions is not tested and (3) the condition does
	 * not depend on an untested condition.
	 */
	public BitSet getNextConditions(State state, int action) {
		BitSet conds = new BitSet();
		for (int cond : BitSets.iterable(actionConditions[action])) {
			TestResult r = state.getResult(cond);
			switch (r) {
			case Unknown:
				conds.set(cond);
				break;
			case False:
				conds.clear();
				return conds;
			case True:
			}
		}
		dependencies.keepLeavesOf(conds);
		for (int cond : BitSets.iterable(actionOutputConditions[action])) {
			TestResult r = state.getResult(cond);
			switch (r) {
			case Unknown:
				conds.set(cond);
				break;
			case False:
				conds.clear();
				return conds;
			case True:
			}
		}
		return conds;
	}

	/**
	 * Returns a subset of the conditions of all actions in <code>actions</code>
	 * feasible for testing. A condition is feasible for testing if (1) the
	 * action is not disabled and (2) the conditions is not tested and (3) the
	 * condition does not depend on an untested condition.
	 */
	public BitSet getNextConditions(State state, BitSet actions) {
		BitSet conds = new BitSet();
		for (int action : BitSets.iterable(actions)) {
			conds.or(getNextConditions(state, action));
		}
		return conds;
	}

	/**
	 * Removes all elements in <code>actions</code> for which the disjunction of
	 * its test results is <code>False</code>.
	 */
	public void removeDisabledActions(State state, BitSet actions) {
		Iterator<Integer> iter = BitSets.iterator(actions);
		while (iter.hasNext()) {
			TestResult result = testActionConditions(state, iter.next());
			if (result == TestResult.False) {
				iter.remove();
			}
		}
	}

	/**
	 * Removes all elements in <code>actions</code> for which the conjunction of
	 * its test results is not <code>True</code>.
	 */
	public void keepFirableActions(State state, BitSet actions) {
		Iterator<Integer> iter = BitSets.iterator(actions);
		while (iter.hasNext()) {
			int action = iter.next();
			boolean keep = true;
			BitSet allConds = BitSets.union(actionConditions[action], actionOutputConditions[action]);
			for (int cond : BitSets.iterable(allConds)) {
				if (state.getResult(cond) != TestResult.True) {
					keep = false;
					break;
				}
			}
			if (!keep) {
				actions.clear(action);
			}
		}
	}
	
	public static class Builder {
		private int nbrOfConds;
		private int nbrOfActions;

		private Map<Integer, BitSet> conds;
		private Map<Integer, BitSet> outConds;
		private List<Map.Entry<Integer, Integer>> deps;
				
		public Builder() {
			nbrOfConds = 0;
			nbrOfActions = 0;
			conds = new HashMap<>();
			outConds = new HashMap<>();
			deps = new ArrayList<>();
		}
		
		public void addAction(int action) {
			nbrOfActions = Math.max(action+1, nbrOfActions);
			assert !conds.containsKey(action);
			conds.put(action, new BitSet());
			outConds.put(action, new BitSet());
		}
		
		public void addCondition(int action, int condition) {
			nbrOfConds = Math.max(condition+1, nbrOfConds);
			conds.get(action).set(condition);
		}
		
		public void addOutputCondition(int action, int condition) {
			outConds.get(action).set(condition);
		}
		
		public void addDependency(int required, int condition) {
			deps.add(ImmutableEntry.of(condition, required));
		}
		
		public ConditionHandler build() {
			BitSet[] actionConditions = new BitSet[nbrOfActions];
			for (int i = 0; i < nbrOfActions; i++) {
				BitSet c = conds.get(i);
				actionConditions[i] = new BitSet();
				if (c != null) {
					actionConditions[i].or(c);
				}
			}
			BitSet[] actionOutputConditions = new BitSet[nbrOfActions];
			for (int i = 0; i < nbrOfActions; i++) {
				BitSet c = outConds.get(i);
				actionOutputConditions[i] = new BitSet();
				if (c != null) {
					actionOutputConditions[i].or(c);
				}
			}
			DAG.Builder dependencies = new DAG.Builder(nbrOfConds);
			for (Map.Entry<Integer, Integer> entry : deps) {
				dependencies.addArc(entry.getKey(), entry.getValue());
			}
			return new ConditionHandler(actionConditions, dependencies.build(), actionOutputConditions);
		}
	}

}
