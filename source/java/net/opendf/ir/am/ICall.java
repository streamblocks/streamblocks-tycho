package net.opendf.ir.am;

/**
 * This class represents the call instruction, which has two components: the {@link Transition transition} to be executed during the call, and the 
 * controller state to go to after the call instruction completes.
 * 
 * In general, a call instruction invalidates all scopes that were defined since the last call instruction. A scope may be retained if it
 * can be shown that the transition executed during the call instruction does not affect the values of the variables defined in it.
 * 
 * <p><tt>call(tc, s)<tt>
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class ICall extends Instruction {

	@Override
	public <R,P> R accept(InstructionVisitor<R,P> v, P p) {
		return v.visitCall(this, p);
	}
	
	public Transition		T() { return t; }
	
	public int				S() { return s; }
	
	//
	//  Ctor
	//
	
	public ICall(Transition t, int s) {
		this.t = t;
		this.s = s;
	}
	
	
	private Transition  	t;
	private int             s;
}
