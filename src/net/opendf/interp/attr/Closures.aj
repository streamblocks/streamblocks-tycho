package net.opendf.interp.attr;

import net.opendf.interp.Environment;
import net.opendf.interp.Stack;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprProc;

public aspect Closures {

	private interface ClosureCreator {
	}

	declare parents : (ExprProc || ExprLambda) implements ClosureCreator;

	private int[] ClosureCreator.selectMemoryRefs;
	private int[] ClosureCreator.selectStackRefs;
	private int[] ClosureCreator.selectChannelIn;
	private int[] ClosureCreator.selectChannelOut;

	public void ClosureCreator.selectMemoryRefs(int[] memoryRefs) {
		selectMemoryRefs = memoryRefs;
	}

	public void ClosureCreator.selectStackRefs(int[] stackRefs) {
		selectStackRefs = stackRefs;
	}

	public void ClosureCreator.selectChannelIn(int[] channels) {
		selectChannelIn = channels;
	}

	public void ClosureCreator.selectChannelOut(int[] channels) {
		selectChannelOut = channels;
	}

	public Environment ClosureCreator.createClosure(Environment env, Stack s) {
		return env.closure(selectChannelIn, selectChannelOut, selectMemoryRefs, s.closure(selectStackRefs));
	}

}
