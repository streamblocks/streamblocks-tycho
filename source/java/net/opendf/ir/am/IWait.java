package net.opendf.ir.am;

/**
 * This class represents the wait instruction, which contains the controller
 * state to transition to.
 * 
 * <p>
 * <tt>wait(s)</tt>
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class IWait extends Instruction {

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitWait(this, p);
	}

	public int S() {
		return s;
	}

	//
	// Ctor
	//

	public IWait(int s) {
		this(null, s);
	}
	
	private IWait(IWait original, int s) {
		super(original);
		this.s = s;
	}
	
	public IWait copy(int s) {
		if (this.s == s) {
			return this;
		}
		return new IWait(this, s);
	}

	private int s;
}
