package net.opendf.interp.preprocess;

public class VarPos {
	private final int position;
	private final boolean onStack;
	
	private VarPos(int pos, boolean stack) {
		position = pos;
		onStack = stack;
	}
	
	public static VarPos stack(int pos) {
		return new VarPos(pos, true);
	}
	public static VarPos mem(int pos) {
		return new VarPos(pos, false);
	}
	
	public boolean isOnStack() { return onStack; }
	public boolean isInMemory() { return !onStack; }
	public int getPosition() { return position; }
	
	

}
