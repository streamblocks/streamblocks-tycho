package net.opendf.transform.caltoam;

import java.util.BitSet;

import net.opendf.transform.caltoam.util.IntDAG;

class PriorityHandler {
	private IntDAG priorities;

	private PriorityHandler(IntDAG priorities) {
		this.priorities = priorities;
	}

	public void keepHighestPrio(BitSet actions) {
		priorities.keepRoots(actions);
	}

	public static class Builder {
		private IntDAG priorities;
		private boolean built;

		public Builder(int nbrOfActions) {
			priorities = new IntDAG(nbrOfActions);
			built = false;
		}

		public void addPriority(int high, int low) {
			if (built) {
				throw new IllegalStateException("Already built");
			}
			priorities.addEdge(high, low);
		}

		public PriorityHandler build() {
			built = true;
			return new PriorityHandler(priorities);
		}
	}
}
