package net.opendf.ir.am;

public interface InstructionVisitor {
	
	public void  visitWait(IWait i);
	
	public void  visitTest(ITest i);
	
	public void  visitCall(ICall i);

}
