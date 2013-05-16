package net.opendf.transform.outcond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opendf.ir.am.ActorMachine;
import net.opendf.ir.am.Condition;
import net.opendf.ir.am.ICall;
import net.opendf.ir.am.ITest;
import net.opendf.ir.am.IWait;
import net.opendf.ir.am.Instruction;
import net.opendf.ir.am.PortCondition;
import net.opendf.ir.common.Port;
import net.opendf.ir.util.ImmutableEntry;
import net.opendf.ir.util.ImmutableList;
import net.opendf.transform.caltoam.util.TestResult;
import net.opendf.transform.util.GenInstruction;
import net.opendf.transform.util.GenInstruction.Test;
import net.opendf.transform.util.GenInstruction.Wait;
import net.opendf.transform.util.GenInstruction.Call;
import net.opendf.transform.util.StateHandler;

public class OutputConditionStateHandler implements StateHandler<OutputConditionState> {

	private final ActorMachine actorMachine;
	private final ImmutableList.Builder<Condition> conditions;
	private int nextCondition;
	private final Map<Entry<Integer, Integer>, Integer> conditionMap;

	public OutputConditionStateHandler(ActorMachine actorMachine) {
		this.actorMachine = actorMachine;
		this.conditions = ImmutableList.builder(); 
		ImmutableList<Condition> oldConds = actorMachine.getConditions();
		this.conditions.addAll(oldConds);
		this.nextCondition = oldConds.size();
		this.conditionMap = new HashMap<>();
	}
	
	public ImmutableList<Condition> getConditions() {
		return conditions.build();
	}

	@Override
	public List<GenInstruction<OutputConditionState>> getInstructions(OutputConditionState state) {
		ImmutableList<Instruction> instructions = actorMachine.getInstructions(state.getInnerState());
		ImmutableList.Builder<GenInstruction<OutputConditionState>> builder = ImmutableList.builder();
		for (Instruction i : instructions) {
			if (i instanceof ITest) {
				ITest test = (ITest) i;
				builder.add(new Test<>(test.C(), state.setInnerState(test.S1()), state.setInnerState(test.S0())));
			} else if (i instanceof IWait) {
				IWait wait = (IWait) i;
				builder.add(new Wait<>(state.setInnerState(wait.S())));
			} else if (i instanceof ICall) {
				ICall call = (ICall) i;
				builder.addAll(getInstructionsFromCall(state, call));
			}
		}
		ImmutableList<GenInstruction<OutputConditionState>> result = builder.build();
		if (result.isEmpty()) {
			return ImmutableList.<GenInstruction<OutputConditionState>> of(new Wait<>(state.removeTransientInfo()));
		} else {
			return result;
		}
	}
	
	private ImmutableList<GenInstruction<OutputConditionState>> getInstructionsFromCall(OutputConditionState state, ICall call) {
		ImmutableList.Builder<GenInstruction<OutputConditionState>> builder = ImmutableList.builder();
		TestResult totalResult = TestResult.True;
		for (Entry<Port, Integer> entry : getOutputRates(call).entrySet()) {
			Port port = entry.getKey();
			int portOffset = port.getOffset();
			int tokens = entry.getValue();
			TestResult portResult = state.getPortTestResult(portOffset, tokens);
			totalResult = totalResult.and(portResult);
			switch (portResult) {
			case Unknown:
				OutputConditionState s0 = state.setTestResult(portOffset, tokens, false);
				OutputConditionState s1 = state.setTestResult(portOffset, tokens, true);
				builder.add(new Test<OutputConditionState>(getCondition(port, tokens), s1, s0));
				break;
			case False:
				return ImmutableList.empty();
			}
		}
		if (totalResult == TestResult.True) {
			OutputConditionState destination = state.setInnerState(call.S());
			for (Entry<Port, Integer> entry : getOutputRates(call).entrySet()) {
				int port = entry.getKey().getOffset();
				int tokens = entry.getValue();
				destination = destination.removeSpace(port, tokens);
			}
			builder.add(new Call<>(call.T(), destination));
		}
		return builder.build();
	}
	
	private int getCondition(Port port, int tokens) {
		Entry<Integer, Integer> condition = ImmutableEntry.of(port.getOffset(), tokens);
		if (conditionMap.containsKey(condition)) {
			return conditionMap.get(condition);
		} else {
			conditionMap.put(condition, nextCondition);
			conditions.add(new PortCondition(port, tokens, false));
			return nextCondition++;
		}
	}

	private Map<Port, Integer> getOutputRates(ICall call) {
		return actorMachine.getTransition(call.T()).getOutputRates();
	}

	@Override
	public OutputConditionState initialState() {
		return new OutputConditionState(0, actorMachine.getOutputPorts().size());
	}

}
