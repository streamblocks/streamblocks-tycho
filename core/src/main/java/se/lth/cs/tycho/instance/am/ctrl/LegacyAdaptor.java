package se.lth.cs.tycho.instance.am.ctrl;

import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.InstructionVisitor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LegacyAdaptor implements Controller {
	private final List<State> states;
	private final Converter converter = new Converter();

	public LegacyAdaptor(List<se.lth.cs.tycho.instance.am.State> states) {
		this.states = states.stream().map(StateAdaptor::new).collect(Collectors.toList());
	}

	@Override
	public State getInitialState() {
		return states.get(0);
	}

	@Override
	public List<State> getAllStates() { return Collections.unmodifiableList(states); }

	private Transition convert(Instruction i) {
		return i.accept(converter);
	}

	private class StateAdaptor implements State {
		private final se.lth.cs.tycho.instance.am.State state;
		List<Transition> transitions;

		StateAdaptor(se.lth.cs.tycho.instance.am.State state) {
			this.state = state;
		}

		@Override
		public List<Transition> getTransitions() {
			if (transitions == null) {
				transitions = state.getInstructions().stream().map(LegacyAdaptor.this::convert).collect(Collectors.toList());
			}
			return transitions;
		}
	}

	private class Converter implements InstructionVisitor<Transition, Void> {

		@Override
		public Transition visitWait(IWait i, Void aVoid) {
			return Wait.of(states.get(i.S()));
		}

		@Override
		public Transition visitTest(ITest i, Void aVoid) {
			return Test.of(i.C(), states.get(i.S1()), states.get(i.S0()));
		}

		@Override
		public Transition visitCall(ICall i, Void aVoid) {
			return Exec.of(i.T(), states.get(i.S()));
		}
	}

}
