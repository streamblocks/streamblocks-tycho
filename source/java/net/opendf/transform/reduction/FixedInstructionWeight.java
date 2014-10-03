package net.opendf.transform.reduction;

import net.opendf.transform.util.GenInstruction.Call;
import net.opendf.transform.util.GenInstruction.Test;
import net.opendf.transform.util.GenInstruction.Visitor;
import net.opendf.transform.util.GenInstruction.Wait;

public class FixedInstructionWeight<S> implements Visitor<S, Integer, Void> {
	private final int callWeight;
	private final int testWeight;
	private final int waitWeight;

	public FixedInstructionWeight(int call, int test, int wait) {
		this.callWeight = call;
		this.testWeight = test;
		this.waitWeight = wait;
	}

	@Override
	public Integer visitCall(Call<S> call, Void parameter) {
		return callWeight;
	}

	@Override
	public Integer visitTest(Test<S> test, Void parameter) {
		return testWeight;
	}

	@Override
	public Integer visitWait(Wait<S> wait, Void parameter) {
		return waitWeight;
	}

}
