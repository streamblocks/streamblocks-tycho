package net.opendf.ir.am;

import net.opendf.ir.IRNode;

/**
 * Classes implementing this interface represent instructions of the actor machine controller. 
 * 
 * @see ICall
 * @see ITest
 * @see IWait
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public interface Instruction extends IRNode {
	
	public void accept(InstructionVisitor v);

}
