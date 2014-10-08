package net.opendf.transform.caltoam;

import java.util.BitSet;

import net.opendf.util.DAG;

class PriorityHandler {
	private DAG priorities;

	private PriorityHandler(DAG priorities) {
		this.priorities = priorities;
	}

	public void keepHighestPrio(BitSet actions) {
		priorities.keepLeavesOf(actions);
	}

	public static class Builder {
		private DAG.Builder priorities;

		public Builder(int nbrOfActions) {
			priorities = new DAG.Builder(nbrOfActions);
		}

		public void addPriority(int high, int low) {
			priorities.addArc(low, high);
		}

		public PriorityHandler build() {
			return new PriorityHandler(priorities.build());
		}
	}
}
