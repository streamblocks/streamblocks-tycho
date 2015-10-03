package se.lth.cs.tycho.instance.am.ctrl;

import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
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

	private Instruction convert(se.lth.cs.tycho.instance.am.Instruction i) {
		return i.accept(converter);
	}

	private class StateAdaptor implements State {
		private final se.lth.cs.tycho.instance.am.State state;
		List<Instruction> instructions;

		StateAdaptor(se.lth.cs.tycho.instance.am.State state) {
			this.state = state;
		}

		@Override
		public List<Instruction> getInstructions() {
			if (instructions == null) {
				instructions = state.getInstructions().stream().map(LegacyAdaptor.this::convert).collect(Collectors.toList());
			}
			return instructions;
		}
	}

	private class Converter implements InstructionVisitor<Instruction, Void> {

		@Override
		public Instruction visitWait(IWait i, Void aVoid) {
			return new Wait(states.get(i.S()), null);
		}

		@Override
		public Instruction visitTest(ITest i, Void aVoid) {
			return new Test(i.C(), states.get(i.S1()), states.get(i.S0()));
		}

		@Override
		public Instruction visitCall(ICall i, Void aVoid) {
			return new Exec(i.T(), states.get(i.S()));
		}
	}

}
