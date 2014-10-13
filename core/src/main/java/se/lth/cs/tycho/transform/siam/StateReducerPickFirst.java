package se.lth.cs.tycho.transform.siam;

import java.util.Collections;
import java.util.List;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.State;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class StateReducerPickFirst implements ActorMachineState<Integer> {
	
	private final ImmutableList<State> controller;
	
	public StateReducerPickFirst(ActorMachine am) {
		controller = am.getController();
	}

	@Override
	public List<GenInstruction<Integer>> getInstructions(Integer state) {
		Instruction i = controller.get(state).getInstructions().get(0);
		if (i instanceof ICall) {
			ICall c = (ICall) i;
			return instructionList(new Call<>(c.T(), c.S()));
		} else if (i instanceof ITest) {
			ITest t = (ITest) i;
			return instructionList(new Test<>(t.C(), t.S1(), t.S0()));
		} else if (i instanceof IWait) {
			IWait w = (IWait) i;
			return instructionList(new Wait<>(w.S()));
		}
		return null;
	}
	
	public List<GenInstruction<Integer>> instructionList(GenInstruction<Integer> i) {
		return Collections.singletonList(i);
	}

	@Override
	public Integer initialState() {
		return 0;
	}

}
