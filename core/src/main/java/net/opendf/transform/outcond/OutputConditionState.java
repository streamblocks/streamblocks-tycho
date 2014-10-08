package net.opendf.transform.outcond;

import java.util.Arrays;

import net.opendf.transform.caltoam.util.TestResult;

public class OutputConditionState {
	private final int innerState;
	private final int[] available;
	private final int[] notAvailable;

	private OutputConditionState(int innerState, int[] available, int[] notAvailable) {
		this.innerState = innerState;
		this.available = available;
		this.notAvailable = notAvailable;
	}

	public OutputConditionState(int innerState, int outputPorts) {
		this(innerState, new int[outputPorts], new int[outputPorts]);
	}

	public int getInnerState() {
		return innerState;
	}

	public TestResult getPortTestResult(int port, int tokens) {
		if (available[port] >= tokens) {
			return TestResult.True;
		}
		int na = notAvailable[port];
		if (na > 0 && na <= tokens) {
			return TestResult.False;
		}
		return TestResult.Unknown;
	}

	public OutputConditionState setInnerState(int state) {
		return new OutputConditionState(state, available, notAvailable);
	}

	public OutputConditionState setTestResult(int port, int tokens, boolean result) {
		int[] available = this.available;
		int[] notAvailable = this.notAvailable;
		if (result) {
			available = Arrays.copyOf(available, available.length);
			assert available[port] < tokens;
			available[port] = tokens;
		} else {
			notAvailable = Arrays.copyOf(notAvailable, notAvailable.length);
			assert notAvailable[port] == 0 || notAvailable[port] > tokens;
			notAvailable[port] = tokens;
		}
		return new OutputConditionState(innerState, available, notAvailable);
	}

	public OutputConditionState removeTransientInfo() {
		return new OutputConditionState(innerState, available, new int[notAvailable.length]);
	}
	
	public OutputConditionState removeSpace(int port, int tokens) {
		int[] available = Arrays.copyOf(this.available, this.available.length);
		int[] notAvailable = Arrays.copyOf(this.notAvailable, this.notAvailable.length);
		assert available[port] >= tokens;
		available[port] -= tokens;
		if (notAvailable[port] > 0) {
			assert notAvailable[port] >= tokens;
			notAvailable[port] -= tokens;
		}
		return new OutputConditionState(innerState, available, notAvailable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(available);
		result = prime * result + innerState;
		result = prime * result + Arrays.hashCode(notAvailable);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OutputConditionState))
			return false;
		OutputConditionState other = (OutputConditionState) obj;
		if (!Arrays.equals(available, other.available))
			return false;
		if (innerState != other.innerState)
			return false;
		if (!Arrays.equals(notAvailable, other.notAvailable))
			return false;
		return true;
	}

	private String portCondToString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < available.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append('(');
			sb.append(available[i]);
			sb.append(", ");
			int na = notAvailable[i];
			sb.append(na == 0 ? "inf" : na);
			sb.append(')');
		}
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return "OutputConditionState [innerState=" + innerState + ", ports=" + portCondToString() + "]";
	}
}
