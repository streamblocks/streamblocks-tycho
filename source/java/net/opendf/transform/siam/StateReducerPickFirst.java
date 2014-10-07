package net.opendf.transform.siam;

import java.util.Collections;
import java.util.List;

import net.opendf.ir.entity.am.ActorMachine;
import net.opendf.ir.entity.am.ICall;
import net.opendf.ir.entity.am.ITest;
import net.opendf.ir.entity.am.IWait;
import net.opendf.ir.entity.am.Instruction;
import net.opendf.ir.entity.am.State;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.GenInstruction.Call;
import net.opendf.transform.util.GenInstruction.Test;
import net.opendf.transform.util.GenInstruction.Wait;
import net.opendf.transform.util.StateHandler;

public class StateReducerPickFirst implements StateHandler<Integer> {
	
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
