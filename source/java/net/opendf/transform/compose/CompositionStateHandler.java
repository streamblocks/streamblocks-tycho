package net.opendf.transform.compose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.opendf.ir.IRNode.Identifier;
import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.am.Transition;
import net.opendf.ir.common.Port;
import net.opendf.ir.common.PortContainer;
import net.opendf.ir.common.expr.ExprLiteral;
import net.opendf.ir.common.expr.Expression;
import net.opendf.ir.net.Connection;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.ToolAttribute;
import net.opendf.ir.net.ToolValueAttribute;
import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.GenInstruction.Call;
import net.opendf.transform.util.GenInstruction.Test;
import net.opendf.transform.util.GenInstruction.Wait;
import net.opendf.transform.util.StateHandler;

public class CompositionStateHandler implements StateHandler<CompositionStateHandler.State> {
	private final Network network;

	public CompositionStateHandler(Network network) {
		this.network = network;
	}

	@Override
	public List<GenInstruction<State>> getInstructions(State state) {
		List<Call<State>> calls = getCallInstrucitons(state);
		if (!calls.isEmpty()) return listCovariance(calls);
		List<Test<State>> tests = getTestInstructions(state);
		if (!tests.isEmpty()) return listCovariance(tests);
		Wait<State> waits = getWaitInstructions(state);
		return Collections.<GenInstruction<State>> singletonList(waits);
	}
	
	private List<Call<State>> getCallInstrucitons(State state) {
		List<Call<State>> result = new ArrayList<>();
		for (int n = 0; n < state.states.length; n++) {
			List<Instruction> instrs = getInstructionsForNode(state, n);
			List<ICall> calls = filterByClass(ICall.class, instrs);
			for (ICall call : calls) {
				State dest = callDest(state, n, call);
				int trans = transitionNumber(n, call);
				result.add(new Call<>(trans, dest));
			}
		}
		return result;
	}

	private int transitionNumber(int n, ICall call) {
		int startIndex = 0;
		for (int i = 0; i < n; i++) {
			ActorMachine actorMachine = getActorMachine(i);
			startIndex += actorMachine.getTransitions().size();
		}
		return startIndex + call.T();
	}
	
	private int conditionNumber(int n, ITest test) {
		int startIndex = 0;
		for (int i = 0; i < n; i++) {
			ActorMachine actorMachine = getActorMachine(i);
			startIndex += actorMachine.getConditions().size();
		}
		return startIndex + test.C();
	}

	private State callDest(State state, int n, ICall call) {
		State dest = state.copy();
		dest.states[n] = call.S();
		Transition t = getActorMachine(n).getTransition(call.T());
		Identifier node = network.getNodes().get(n).getIdentifier();
		int connIx = 0;
		for (Connection conn : network.getConnections()) {
			if (conn.getDstNodeId() == node && isInternalBuffer(conn)) {
				for (Port port : t.getInputRates().keySet()) {
					if (conn.getDstPort().getName().equals(port.getName())) {
						dest.tokens[connIx] -= t.getInputRate(port);
					}
				}
			}
			if (conn.getSrcNodeId() == node && isInternalBuffer(conn)) {
				for (Port port : t.getOutputRates().keySet()) {
					if (conn.getSrcPort().getName().equals(port.getName())) {
						dest.tokens[connIx] += t.getOutputRate(port);
					}
				}
			}
			connIx += 1;
		}
		return dest;
	}

	private ActorMachine getActorMachine(int n) {
		Object content = network.getNodes().get(n).getContent();
		return (ActorMachine) content;
	}

	private <A, B extends A> List<B> filterByClass(Class<B> klass, List<A> list) {
		List<B> result = new ArrayList<>();
		for (A a : list) {
			if (klass.isInstance(a)) {
				result.add(klass.cast(a));
			}
		}
		return result;
	}

	private List<Test<State>> getTestInstructions(State state) {
		List<Test<State>> result = new ArrayList<>();
		for (int n = 0; n < state.states.length; n++) {
			List<Instruction> instrs = getInstructionsForNode(state, n);
			List<ITest> tests = filterByClass(ITest.class, instrs);
			for (ITest test : tests) {
				State destTrue = state.copy();
				destTrue.states[n] = test.S1();
				State destFalse = state.copy();
				destFalse.states[n] = test.S0();
				int cond = conditionNumber(n, test);
				result.add(new Test<>(cond, destTrue, destFalse));
			}
		}
		return result;
	}

	private Wait<State> getWaitInstructions(State state) {
		State dest = state.copy();
		for (int n = 0; n < state.states.length; n++) {
			List<Instruction> instrs = getInstructionsForNode(state, n);
			List<IWait> waits = filterByClass(IWait.class, instrs);
			for (IWait wait : waits) {
				dest.states[n] = wait.S();
			}
		}
		return new Wait<>(dest);
	}

	private List<Instruction> getInstructionsForNode(State state, int n) {
		List<Instruction> result = new ArrayList<>();
		addInstructionsForNode(state, n, result);
		return result;
	}

	private void addInstructionsForNode(State state, int n, List<Instruction> result) {
		ActorMachine actorMachine = getActorMachine(n);
		Queue<Integer> states = new LinkedList<>();
		states.offer(state.states[n]);
		while (!states.isEmpty()) {
			int s = states.remove();
			for (Instruction i : actorMachine.getInstructions(s)) {
				if (i instanceof ITest) {
					ITest test = (ITest) i;
					Boolean testResult = performTest(state, n, test);
					if (testResult == null) {
						result.add(i);
					} else {
						states.offer(testResult ? test.S1() : test.S0());
					}
				} else {
					result.add(i);
				}
			}
		}
	}
	
	private Boolean performTest(State state, int n, ITest t) {
		Condition c = getActorMachine(n).getCondition(t.C());
		if (!(c instanceof PortCondition)) return null;
		PortCondition pc = (PortCondition) c;
		if (pc.isInputCondition()) {
			Integer tokens = inputTokens(state, n, pc.getPortName());
			if (tokens != null) {
				return tokens >= pc.N();
			}
		} else {
			Integer space = outputSpace(state, n, pc.getPortName());
			if (space != null) {
				return space >= pc.N();
			}
		}
		return null;
	}

	private Integer inputTokens(State state, int nodeIx, Port port) {
		Identifier node = network.getNodes().get(nodeIx).getIdentifier();
		String portName = port.getName();
		int connIx = 0;
		for (Connection conn : network.getConnections()) {
			if (conn.getDstNodeId() == node && conn.getDstPort().getName().equals(portName)) {
				if (isInternalBuffer(conn)) {
					return state.tokens[connIx];
				}
			}
			connIx += 1;
		}
		return null;
	}
	
	private Integer outputSpace(State state, int nodeIx, Port port) {
		Identifier node = network.getNodes().get(nodeIx).getIdentifier();
		String portName = port.getName();
		int connIx = 0;
		int minSpace = -1;
		for (Connection conn : network.getConnections()) {
			if (conn.getSrcNodeId() == node && conn.getSrcPort().getName().equals(portName)) {
				Integer bufferSize = bufferSize(conn);
				if (bufferSize == null) {
					return null;
				}
				int space = bufferSize - state.tokens[connIx];
				if (minSpace < 0 || minSpace > space) {
					minSpace = space;
				}
			}
			connIx += 1;
		}
		return minSpace < 0 ? null : minSpace;
	}

	private boolean isInternalBuffer(Connection conn) {
		return bufferSize(conn) != null;
	}
	
	private Integer bufferSize(Connection conn) {
		ToolAttribute attr = conn.getToolAttribute("buffer_size");
		if (attr != null && attr instanceof ToolValueAttribute) {
			ToolValueAttribute valAttr = (ToolValueAttribute) attr;
			Expression value = valAttr.getValue();
			if (value instanceof ExprLiteral) {
				ExprLiteral literal = (ExprLiteral) value;
				if (literal.getKind() == ExprLiteral.Kind.Integer) {
					return Integer.valueOf(literal.getText());
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <A, B extends A> List<A> listCovariance(List<B> list) {
		return (List<A>) list;
	}

	@Override
	public State initialState() {
		return new State();
	}

	public class State {
		private final int[] states;
		private final int[] tokens;

		public State() {
			this(new int[network.getNodes().size()], new int[network.getConnections().size()]);
		}

		private State(int[] states, int[] tokens) {
			this.states = states;
			this.tokens = tokens;
		}

		public State copy() {
			return new State(Arrays.copyOf(states, states.length), Arrays.copyOf(tokens, tokens.length));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(states);
			result = prime * result + Arrays.hashCode(tokens);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof State)) {
				return false;
			}
			State other = (State) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (!Arrays.equals(states, other.states)) {
				return false;
			}
			if (!Arrays.equals(tokens, other.tokens)) {
				return false;
			}
			return true;
		}

		private CompositionStateHandler getOuterType() {
			return CompositionStateHandler.this;
		}

		@Override
		public String toString() {
			return "State [states=" + Arrays.toString(states) + ", tokens=" + Arrays.toString(tokens) + "]";
		}

	}

}
