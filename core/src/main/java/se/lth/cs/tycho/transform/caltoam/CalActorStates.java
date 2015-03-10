package se.lth.cs.tycho.transform.caltoam;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.instance.am.PredicateCondition;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.transform.caltoam.util.BitSets;
import se.lth.cs.tycho.transform.caltoam.util.TestResult;

public class CalActorStates {
	private final List<Condition> conditions;
	private final List<String> stateList;
	private final int nbrOfInputPorts;
	private final int nbrOfOutputPorts;
	private final BitSet scheduleInit;

	public CalActorStates(List<Condition> conditions, List<String> stateList, BitSet scheduleInit, int nbrOfInputPorts, int nbrOfOutputPorts) {
		this.conditions = conditions;
		this.stateList = stateList;
		this.nbrOfInputPorts = nbrOfInputPorts;
		this.nbrOfOutputPorts = nbrOfOutputPorts;
		this.scheduleInit = scheduleInit;
	}

	public State initialState() {
		return new State();
	}

	public class State {
		private State() {
			this(scheduleInit, BigInteger.ZERO, BigInteger.ZERO, new int[nbrOfInputPorts], new int[nbrOfInputPorts], new int[nbrOfOutputPorts], new int[nbrOfOutputPorts]);
		}

		private State(BitSet states, BigInteger predCondTrue, BigInteger predCondFalse,
				int[] presentTokens, int[] absentTokens, int[] presentSpace, int[] absentSpace) {
			this.states = states;
			this.predCondTrue = predCondTrue;
			this.predCondFalse = predCondFalse;
			this.presentTokens = presentTokens;
			this.absentTokens = absentTokens;
			this.presentSpace = presentSpace;
			this.absentSpace = absentSpace;
		}

		private final BitSet states;
		private final BigInteger predCondTrue;
		private final BigInteger predCondFalse;
		private int[] presentTokens;
		private int[] absentTokens;
		private int[] presentSpace;
		private int[] absentSpace;

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

		private TestResult getPortTestResult(Port port, int tokens, boolean inputPort) {
			int[] present, absent;
			if (inputPort) {
				present = presentTokens;
				absent = absentTokens;
			} else {
				present = presentSpace;
				absent = absentSpace;
			}
			int offset = port.getOffset();
			if (present[offset] >= tokens) {
				return TestResult.True;
			}
			int nbrOfAbsent = absent[offset];
			if (nbrOfAbsent > 0 && nbrOfAbsent <= tokens) {
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
				return getPortTestResult(portCond.getPortName(), portCond.N(), portCond.isInputCondition());
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
				return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, absentSpace);
			}
			if (c instanceof PortCondition) {
				PortCondition portCond = (PortCondition) c;
				Port port = portCond.getPortName();
				int tokens = portCond.N();
				int[] presentTokens = this.presentTokens;
				int[] absentTokens = this.absentTokens;
				int[] presentSpace = this.presentSpace;
				int[] absentSpace = this.absentSpace;
				
				if (portCond.isInputCondition()) {
					if (result) {
						presentTokens = Arrays.copyOf(presentTokens, presentTokens.length);
						presentTokens[port.getOffset()] = tokens;
					} else {
						absentTokens = Arrays.copyOf(absentTokens, absentTokens.length);
						absentTokens[port.getOffset()] = tokens;
					}
				} else {
					if (result) {
						presentSpace = Arrays.copyOf(presentSpace, presentSpace.length);
						presentSpace[port.getOffset()] = tokens;
					} else {
						absentSpace = Arrays.copyOf(absentSpace, absentSpace.length);
						absentSpace[port.getOffset()] = tokens;
					}
				}
				return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, absentSpace);
			}
			assert false;
			return null;
		}

		public State clearPredicateResults() {
			return new State(states, BigInteger.ZERO, BigInteger.ZERO, presentTokens, absentTokens, presentSpace, absentSpace);
		}
		
		public State clearTokenResults() {
			return new State(states, predCondTrue, predCondFalse, new int[nbrOfInputPorts], new int[nbrOfInputPorts], presentSpace, absentSpace);
		}

		public State clearAbsentTokenResults() {
			return new State(states, predCondTrue, predCondFalse, presentTokens, new int[nbrOfInputPorts], presentSpace, absentSpace);
		}
		
		public State clearAbsentSpaceResults() {
			return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, new int[nbrOfOutputPorts]);
		}

		public State clearSpaceResults() {
			return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, new int[nbrOfOutputPorts], new int[nbrOfOutputPorts]);
		}

		public State removeTokens(Port port, int n) {
			int[] presentTokens = this.presentTokens;
			int offset = port.getOffset();
			if (presentTokens[offset] > 0) {
				presentTokens = Arrays.copyOf(presentTokens, nbrOfInputPorts);
				int tokens = presentTokens[offset] - n;
				assert tokens >= 0;
				presentTokens[offset] = tokens;
			}
			int[] absentTokens = this.absentTokens;
			if (absentTokens[offset] > 0) {
				absentTokens = Arrays.copyOf(absentTokens, nbrOfInputPorts);
				int tokens = absentTokens[offset] - n;
				assert tokens > 0;
				absentTokens[offset] = tokens;
			}
			return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, absentSpace);
		}
		
		public State removeSpace(Port port, int n) {
			int[] presentSpace = this.presentSpace;
			int offset = port.getOffset();
			if (presentSpace[offset] > 0) {
				presentSpace = Arrays.copyOf(presentSpace, presentSpace.length);
				int space = presentSpace[offset] - n;
				assert space >= 0;
				presentSpace[offset] = space;
			}
			int[] absentSpace = this.absentSpace;
			if (absentSpace[offset] > 0) {
				absentSpace = Arrays.copyOf(absentSpace, absentSpace.length);
				int space = absentSpace[offset] - n;
				assert space > 0;
				absentSpace[offset] = space;
			}
			return new State(states, predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, absentSpace);
		}

		public State setSchedulerState(BitSet scheduleStates) {
			return new State(BitSets.copyOf(scheduleStates), predCondTrue, predCondFalse, presentTokens, absentTokens, presentSpace, absentSpace);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((predCondFalse == null) ? 0 : predCondFalse.hashCode());
			result = prime * result + ((predCondTrue == null) ? 0 : predCondTrue.hashCode());
			result = prime * result + Arrays.hashCode(presentTokens);
			result = prime * result + Arrays.hashCode(absentTokens);
			result = prime * result + Arrays.hashCode(presentSpace);
			result = prime * result + Arrays.hashCode(absentSpace);
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
			if (!Arrays.equals(absentTokens, other.absentTokens))
				return false;
			if (!Arrays.equals(presentSpace, other.presentSpace)) 
				return false;
			if (!Arrays.equals(absentSpace, other.absentSpace))
				return false;
			if (states == null) {
				if (other.states != null)
					return false;
			} else if (!states.equals(other.states))
				return false;
			return true;
		}

		private CalActorStates getOuterType() {
			return CalActorStates.this;
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
			int[] present = IntStream.concat(IntStream.of(presentTokens), IntStream.of(presentSpace)).toArray();
			int[] absent = IntStream.concat(IntStream.of(absentTokens), IntStream.of(absentSpace)).toArray();
			for (int p = 0; p < present.length; p++) {
				if (p > 0) {
					sb.append(", ");
				}
				sb.append('(');
				sb.append(present[p]);
				sb.append(", ");
				int isAbsent = absent[p];
				sb.append(isAbsent > 0 ? isAbsent : "inf");
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