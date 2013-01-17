package net.opendf.interp.attr;

import net.opendf.interp.Environment;
import net.opendf.interp.Stack;
import net.opendf.ir.common.ExprLambda;
import net.opendf.ir.common.ExprProc;

public aspect Closures {
	
	private interface ClosureCreator {}
	
	declare parents : (ExprProc || ExprLambda) implements ClosureCreator;
	
	private int[] ClosureCreator.selectMemoryRefs;
	private int[] ClosureCreator.selectStackRefs;
	private int[] ClosureCreator.selectChannels;
	
	public void ClosureCreator.selectMemoryRefs(int[] memoryRefs) {
		selectMemoryRefs = memoryRefs;
	}
	public void ClosureCreator.selectStackRefs(int[] stackRefs) {
		selectStackRefs = stackRefs;
	}
	public void ClosureCreator.selectChannels(int[] channels) {
		selectChannels = channels;
	}
	
	public Environment ClosureCreator.createClosure(Environment env, Stack s) {
		return env.closure(selectChannels, selectMemoryRefs, s.closure(selectStackRefs));
	}
	
}
