package se.lth.cs.tycho.transform.siam;

import java.util.List;
import java.util.stream.Collectors;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.InstructionVisitor;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.instance.am.Transition;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.Controller;
import se.lth.cs.tycho.transform.util.GenInstruction;

public class ActorMachineController implements Controller<Integer> {

	private static final Converter CONVERTER = new Converter();
	private final ImmutableList<State> controller;
	private final QID instanceId;
	private final ActorMachine actorMachine;

	public ActorMachineController(ActorMachine am, QID instanceId) {
		this.instanceId = instanceId;
		this.controller = am.getController();
		this.actorMachine = am;
	}

	@Override
	public List<GenInstruction<Integer>> instructions(Integer state) {
		return controller.get(state).getInstructions().stream().map(this::convert).collect(Collectors.toList());
	}

	private GenInstruction<Integer> convert(Instruction instr) {
		return instr.accept(CONVERTER);
	}

	@Override
	public Integer initialState() {
		return 0;
	}

	@Override
	public Condition getCondition(int c) {
		return actorMachine.getCondition(c);
	}

	@Override
	public Transition getTransition(int t) {
		return actorMachine.getTransition(t);
	}

	private static class Converter implements InstructionVisitor<GenInstruction<Integer>, Void> {

		@Override
		public GenInstruction<Integer> visitWait(IWait i, Void p) {
			return new GenInstruction.Wait<>(i.S());
		}

		@Override
		public GenInstruction<Integer> visitTest(ITest i, Void p) {
			return new GenInstruction.Test<>(i.C(), i.S1(), i.S0());
		}

		@Override
		public GenInstruction<Integer> visitCall(ICall i, Void p) {
			return new GenInstruction.Call<>(i.T(), i.S());
		}

	}

	@Override
	public QID instanceId() {
		return instanceId;
	}

}
