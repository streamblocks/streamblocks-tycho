package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;

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

public abstract class Instruction extends AbstractIRNode {
	
	public abstract <R,P> R accept(InstructionVisitor<R,P> v, P p);
	public <R> R accept(InstructionVisitor<R,Void> v) {
		return accept(v,null);
	}
	
	public Instruction(Instruction original) {
		super(original);
	}

}
