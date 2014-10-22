package se.lth.cs.tycho.transform.outcond;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.lth.cs.tycho.instance.am.ActorMachine;
import se.lth.cs.tycho.instance.am.Condition;
import se.lth.cs.tycho.instance.am.ICall;
import se.lth.cs.tycho.instance.am.ITest;
import se.lth.cs.tycho.instance.am.IWait;
import se.lth.cs.tycho.instance.am.Instruction;
import se.lth.cs.tycho.instance.am.PortCondition;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.transform.caltoam.util.TestResult;
import se.lth.cs.tycho.transform.util.GenInstruction;
import se.lth.cs.tycho.transform.util.ActorMachineState;
import se.lth.cs.tycho.transform.util.GenInstruction.Call;
import se.lth.cs.tycho.transform.util.GenInstruction.Test;
import se.lth.cs.tycho.transform.util.GenInstruction.Wait;

public class OutputConditionStateHandler implements ActorMachineState<OutputConditionState> {

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
			Port copy = new Port(port.getName(), port.getOffset());
			conditions.add(new PortCondition(copy, tokens, false));
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
