package net.opendf.ir.am;

/**
 * This class represents the call instruction, which has two components: the
 * {@link Transition transition} to be executed during the call, and the
 * controller state to go to after the call instruction completes.
 * 
 * <tt>call(tc, s)<tt>
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ICall extends Instruction {

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitCall(this, p);
	}

	public int T() {
		return t;
	}

	public int S() {
		return s;
	}

	//
	// Ctor
	//

	public ICall(int t, int s) {
		this(null, t, s);
	}
	
	private ICall(ICall original, int t, int s) {
		super(original);
		this.t = t;
		this.s = s;
	}
	
	public ICall copy(int t, int s) {
		if (this.t == t && this.s == s) {
			return this;
		}
		return new ICall(this, t, s);
	}

	private int t;
	private int s;
}
