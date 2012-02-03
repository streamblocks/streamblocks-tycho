package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;

/**
 * This class represents the wait instruction, which contains the controller state to transition to.
 * 
 * <p><tt>wait(s)</tt>
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class IWait extends AbstractIRNode implements Instruction {

	@Override
	public void accept(InstructionVisitor v) {
		v.visitWait(this);
	}
	
	public int  S() { return s; }
	
	//
	//  Ctor
	//
	
	public IWait(int s) {
		this.s = s;
	}
	
	private int s;
}
