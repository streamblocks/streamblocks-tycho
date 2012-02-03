package net.opendf.ir.am;

import net.opendf.ir.AbstractIRNode;

/**
 * This class represents the test instruction, which has three components: the {@link Condition condition} to be tested,
 * and the controller states to go to when the condition is found to be either true (s1) or false (s0).
 * 
 * <p><tt>test(c, s1, s0)</tt>
 * 
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class ITest extends AbstractIRNode implements Instruction {

	@Override
	public void accept(InstructionVisitor v) {
		v.visitTest(this);
	}
	
	public Condition  C() { return c; }
	
	public int  S1() { return s1; }
	
	public int  S0() { return s0; }
	
	//
	//  Ctor
	//
	
	public ITest(Condition c, int s1, int s0) {
		this.c = c;
		this.s1 = s1;
		this.s0 = s0;
	}
	
	
	private Condition 	c;
	private int			s1;
	private int			s0;
}
