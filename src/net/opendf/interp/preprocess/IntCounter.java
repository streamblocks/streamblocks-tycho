package net.opendf.interp.preprocess;

public class IntCounter {
	private int next;

	public IntCounter() {
		next = 0;
	}

	public int next() {
		return next++;
	}

	@Override
	public String toString() {
		return "IntCounter [next=" + next + "]";
	}
}
