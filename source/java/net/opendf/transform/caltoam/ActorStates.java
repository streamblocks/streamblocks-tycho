package net.opendf.transform.caltoam;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import net.opendf.ir.am.Condition;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.PredicateCondition;
import net.opendf.ir.common.Port;
import net.opendf.transform.caltoam.util.BitSets;
import net.opendf.transform.caltoam.util.TestResult;

public class ActorStates {
	private final List<Condition> conditions;
	private final List<String> stateList;
	private final int nbrOfPorts;
	private final BitSet scheduleInit;

	public ActorStates(List<Condition> conditions, List<String> stateList, BitSet scheduleInit, int nbrOfPorts) {
		this.conditions = conditions;
		this.stateList = stateList;
		this.nbrOfPorts = nbrOfPorts;
		this.scheduleInit = scheduleInit;
	}

	public State initialState() {
		return new State();
	}

	public class State {
		private State() {
			this(scheduleInit, BigInteger.ZERO, BigInteger.ZERO, new int[nbrOfPorts], new int[nbrOfPorts]);
		}

		private State(BitSet states, BigInteger predCondTrue, BigInteger predCondFalse,
				int[] presentTokens, int[] absentTokens) {
			this.states = states;
			this.predCondTrue = predCondTrue;
			this.predCondFalse = predCondFalse;
			this.presentTokens = presentTokens;
			this.absentTokens = absentTokens;
		}

		private final BitSet states;
		private final BigInteger predCondTrue;
		private final BigInteger predCondFalse;
		private int[] presentTokens;
		private int[] absentTokens;

		public BitSet getSchedulerState() {
			return BitSets.copyOf(states);
		}

		private TestResult getPredTestResult(int condition) {
			if (predCondTrue.testBit(condition)) {
				assert !predCondFalse.testBit(condition);
				return TestResult.True;
			}
			if (predCondFalse.testBit(condition)) {
				assert !predCondTrue.testBit(condition);
				return TestResult.False;
			}
			return TestResult.Unknown;
		}

		private TestResult getPortTestResult(Port port, int tokens) {
			int offset = port.getOffset();
			if (presentTokens[offset] >= tokens) {
				return TestResult.True;
			}
			int absent = absentTokens[offset];
			if (absent > 0 && absent <= tokens) {
				return TestResult.False;
			}
			return TestResult.Unknown;
		}

		public TestResult getResult(int condition) {
			Condition c = conditions.get(condition);
			if (c instanceof PredicateCondition) {
				return getPredTestResult(condition);
			}
			if (c instanceof PortCondition) {
				PortCondition portCond = (PortCondition) c;
				return getPortTestResult(portCond.getPortName(), portCond.N());
			}
			assert false;
			return TestResult.Unknown;
		}

		public State setResult(int condition, boolean result) {
			Condition c = conditions.get(condition);
			if (c instanceof PredicateCondition) {
				BigInteger predCondTrue = this.predCondTrue;
				BigInteger predCondFalse = this.predCondFalse;
				if (result) {
					assert !predCondFalse.testBit(condition);
					predCondTrue = predCondTrue.setBit(condition);
				} else {
					assert !predCondTrue.testBit(condition);
					predCondFalse = predCondFalse.setBit(condition);
				}
				return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens);
			}
			if (c instanceof PortCondition) {
				PortCondition portCond = (PortCondition) c;
				Port port = portCond.getPortName();
				int tokens = portCond.N();
				int[] presentTokens = this.presentTokens;
				int[] absentTokens = this.absentTokens;
				if (result) {
					presentTokens = Arrays.copyOf(presentTokens, nbrOfPorts);
					presentTokens[port.getOffset()] = tokens;
				} else {
					absentTokens = Arrays.copyOf(absentTokens, nbrOfPorts);
					absentTokens[port.getOffset()] = tokens;
				}
				return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens);
			}
			assert false;
			return null;
		}

		public State clearPredicateResults() {
			return new State(states, BigInteger.ZERO, BigInteger.ZERO, presentTokens, absentTokens);
		}

		public State clearAbsentTokenResults() {
			return new State(states, predCondTrue, predCondFalse, presentTokens, new int[nbrOfPorts]);
		}

		public State removeTokens(Port port, int n) {
			int[] presentTokens = this.presentTokens;
			int offset = port.getOffset();
			if (presentTokens[offset] > 0) {
				presentTokens = Arrays.copyOf(presentTokens, nbrOfPorts);
				int tokens = presentTokens[offset] - n;
				assert tokens >= 0;
				presentTokens[offset] = tokens;
			}
			int[] absentTokens = this.absentTokens;
			if (absentTokens[offset] > 0) {
				absentTokens = Arrays.copyOf(absentTokens, nbrOfPorts);
				int tokens = absentTokens[offset] - n;
				assert tokens > 0;
				absentTokens[offset] = tokens;
			}
			return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens);
		}

		public State setSchedulerState(BitSet scheduleStates) {
			return new State(BitSets.copyOf(scheduleStates), predCondTrue, predCondFalse, presentTokens, absentTokens);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(absentTokens);
			result = prime * result + ((predCondFalse == null) ? 0 : predCondFalse.hashCode());
			result = prime * result + ((predCondTrue == null) ? 0 : predCondTrue.hashCode());
			result = prime * result + Arrays.hashCode(presentTokens);
			result = prime * result + ((states == null) ? 0 : states.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof State))
				return false;
			State other = (State) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.equals(absentTokens, other.absentTokens))
				return false;
			if (predCondFalse == null) {
				if (other.predCondFalse != null)
					return false;
			} else if (!predCondFalse.equals(other.predCondFalse))
				return false;
			if (predCondTrue == null) {
				if (other.predCondTrue != null)
					return false;
			} else if (!predCondTrue.equals(other.predCondTrue))
				return false;
			if (!Arrays.equals(presentTokens, other.presentTokens))
				return false;
			if (states == null) {
				if (other.states != null)
					return false;
			} else if (!states.equals(other.states))
				return false;
			return true;
		}

		private ActorStates getOuterType() {
			return ActorStates.this;
		}

		private String predicatesToString() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			int c = 0;
			boolean first = true;
			for (Condition cond : conditions) {
				if (cond instanceof PredicateCondition) {
					switch (getPredTestResult(c)) {
					case True:
						if (!first) sb.append(", ");
						first = false;
						sb.append(c).append("=").append("T");
						break;
					case False:
						if (!first) sb.append(", ");
						first = false;
						sb.append(c).append("=").append("F");
						break;
					case Unknown:
					}
				}
				c += 1;
			}
			sb.append('}');
			return sb.toString();
		}

		private String tokensToString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (int p = 0; p < nbrOfPorts; p++) {
				if (p > 0) {
					sb.append(", ");
				}
				sb.append('(');
				sb.append(presentTokens[p]);
				sb.append(", ");
				int absent = absentTokens[p];
				sb.append(absent > 0 ? absent : "inf");
				sb.append(")");
			}
			sb.append("]");
			return sb.toString();
		}
		
		private String statesToString() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			boolean first = true;
			for (int s : BitSets.iterable(states)) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(stateList.get(s));
			}
			sb.append('}');
			return sb.toString();
		}

		@Override
		public String toString() {
			return "State [states=" + statesToString() + ", predicates=" + predicatesToString() + ", ports="
					+ tokensToString() + "]";
		}

	}
}