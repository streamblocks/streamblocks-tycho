package net.opendf.ir.am;

/**
 * This class represents the test instruction, which has three components: the
 * {@link Condition condition} to be tested, and the controller states to go to
 * when the condition is found to be either true (s1) or false (s0).
 * 
 * <p>
 * <tt>test(c, s1, s0)</tt>
 * 
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class ITest extends Instruction {

	@Override
	public <R, P> R accept(InstructionVisitor<R, P> v, P p) {
		return v.visitTest(this, p);
	}

	public int C() {
		return c;
	}

	public int S1() {
		return s1;
	}

	public int S0() {
		return s0;
	}

	//
	// Ctor
	//

	public ITest(int c, int s1, int s0) {
		this(null, c, s1, s0);
	}

	private ITest(ITest original, int c, int s1, int s0) {
		super(original);
		this.c = c;
		this.s1 = s1;
		this.s0 = s0;
	}

	public ITest copy(int c, int s1, int s0) {
		if (this.c == c && this.s1 == s1 && this.s0 == s0) {
			return this;
		}
		return new ITest(this, c, s1, s0);
	}

	private int c;
	private int s1;
	private int s0;
}
