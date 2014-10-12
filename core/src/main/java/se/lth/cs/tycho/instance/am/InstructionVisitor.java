package se.lth.cs.tycho.instance.am;

public interface InstructionVisitor<R,P> {
	
	public R visitWait(IWait i, P p);
	
	public R visitTest(ITest i, P p);
	
	public R visitCall(ICall i, P p);

}
