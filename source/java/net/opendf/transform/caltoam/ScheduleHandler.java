package net.opendf.transform.caltoam;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.caltoam.util.BitSets;

class ScheduleHandler {

	private final BitSet[] enabledActions;
	private final BitSet[][] destinationStates;
	private final BitSet initialState;

	private ScheduleHandler(BitSet initialState, BitSet[] enabled, BitSet[][] destinations) {
		this.enabledActions = enabled;
		this.destinationStates = destinations;
		this.initialState = initialState;
	}

	public BitSet initialState() {
		return BitSets.copyOf(initialState);
	}

	public BitSet scheduledActions(BitSet state) {
		BitSet actions = new BitSet();
		for (int s : BitSets.iterable(state)) {
			actions.or(enabledActions[s]);
		}
		return actions;
	}

	public BitSet destinations(BitSet state, int action) {
		BitSet dest = new BitSet();
		for (int s : BitSets.iterable(state)) {
			BitSet d = destinationStates[s][action];
			if (d != null) {
				dest.or(d);
			}
		}
		return dest;
	}

	public static class Builder {
		private final Map<String, Integer> stateMap;
		private final Map<Integer, BitSet> enabled;
		private final Map<Integer, Map<Integer, BitSet>> transitions;
		private int actions;
		private BitSet unscheduled;

		public Builder(String initialState) {
			this.stateMap = new HashMap<>();
			this.enabled = new HashMap<>();
			this.transitions = new HashMap<>();
			createState("$initializers", 0);
			createState(initialState, 1);
			this.actions = 0;
			this.unscheduled = new BitSet();
		}

		public void createState(String name, int state) {
			assert !stateMap.containsKey(name);
			assert !enabled.containsKey(state);
			assert !transitions.containsKey(state);
			stateMap.put(name, state);
			enabled.put(state, new BitSet());
			transitions.put(state, new HashMap<Integer, BitSet>());
		}

		private int getState(String state) {
			if (stateMap.containsKey(state)) {
				return stateMap.get(state);
			} else {
				int result = stateMap.size();
				createState(state, result);
				return result;
			}
		}

		private BitSet destinations(Integer state, Integer action) {
			Map<Integer, BitSet> trans = transitions.get(state);
			BitSet dests = trans.get(action);
			if (dests == null) {
				dests = new BitSet();
				trans.put(action, dests);
			}
			return dests;
		}

		public void addTransition(String source, int action, String destination) {
			int src = getState(source);
			int dst = getState(destination);
			addArc(src, action, dst);
		}

		public void addUnscheduled(int action) {
			actions = Math.max(action + 1, actions);
			unscheduled.set(action);
		}

		private void addArc(int src, int action, int dst) {
			enabled.get(src).set(action);
			destinations(src, action).set(dst);
			actions = Math.max(action + 1, actions);
		}

		public void addInitAction(int action) {
			addArc(0, action, 1);
		}

		public ScheduleHandler build() {
			int states = stateMap.size();
			BitSet[] enabled = new BitSet[states];
			for (int s = 0; s < states; s++) {
				enabled[s] = BitSets.copyOf(this.enabled.get(s));
			}
			for (int s = 1; s < states; s++) {
				enabled[s].or(unscheduled);
			}
			BitSet[][] destinations = new BitSet[states][actions];
			for (int s = 0; s < states; s++) {
				for (Entry<Integer, BitSet> entry : transitions.get(s).entrySet()) {
					destinations[s][entry.getKey()] = BitSets.copyOf(entry.getValue());
				}
			}
			for (int s = 1; s < states; s++) {
				BitSet dest = new BitSet();
				dest.set(s);
				for (int action : BitSets.iterable(unscheduled)) {
					destinations[s][action] = BitSets.copyOf(dest);
				}
			}
			BitSet init = new BitSet();
			init.set(enabled[0].isEmpty() ? 1 : 0);
			return new ScheduleHandler(init, enabled, destinations);
		}

		public List<String> stateList() {
			String[] array = new String[stateMap.size()];
			for (Entry<String, Integer> entry : stateMap.entrySet()) {
				array[entry.getValue()] = entry.getKey();
			}
			return ImmutableList.copyOf(array);
		}
	}

}