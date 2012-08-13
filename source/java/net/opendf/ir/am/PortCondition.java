package net.opendf.ir.am;

import net.opendf.ir.common.PortName;

/**
 * PortCondition objects represent input conditions (the condition that there are a certain number of tokens 
 * available on the specified input port), and output conditions (the condition that there is sufficient space available
 * in all buffers receiving output from the specified output port).
 * 
 * In addition to the flag determining whether it is an input condition (the default) or an output condition, a port condition has
 * two components, a {@link PortName port name} and an integer.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 *
 */

public class PortCondition extends Condition {

	@Override
	public ConditionKind kind() {
		return isInputCondition ? ConditionKind.input : ConditionKind.output;
	}
	
	@Override
	public <R,P> R accept(ConditionVisitor<R,P> v, P p) {
		if (isInputCondition)
			return v.visitInputCondition(this, p);
		else
			return v.visitOutputCondition(this, p);
	}
	
	public PortName		getPortName() { return portName; }
	
	public int			N() { return n; }
	
	public boolean		isInputCondition() { return isInputCondition; }

	
	//
	//  Ctor
	//
	

	public PortCondition (PortName portName, int n) {
		this(portName, n, true);
	}

	
	public PortCondition (PortName portName, int n, boolean isInputCondition) {
		this.portName = portName;
		this.n = n;
		this.isInputCondition = isInputCondition;
	}
	
	private boolean 	isInputCondition;
	private PortName	portName;
	private int			n;
}
