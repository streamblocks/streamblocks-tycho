package net.opendf.ir.am;

import java.util.Objects;

import net.opendf.ir.common.Port;

/**
 * PortCondition objects represent input conditions (the condition that there
 * are a certain number of tokens available on the specified input port), and
 * output conditions (the condition that there is sufficient space available in
 * all buffers receiving output from the specified output port).
 * 
 * In addition to the flag determining whether it is an input condition (the
 * default) or an output condition, a port condition has two components, a
 * {@link Port port} and an integer.
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
	public <R, P> R accept(ConditionVisitor<R, P> v, P p) {
		if (isInputCondition)
			return v.visitInputCondition(this, p);
		else
			return v.visitOutputCondition(this, p);
	}

	public Port getPortName() {
		return port;
	}

	public int N() {
		return n;
	}

	public boolean isInputCondition() {
		return isInputCondition;
	}

	//
	// Ctor
	//

	public PortCondition(Port portName, int n) {
		this(null, portName, n, true);
	}

	public PortCondition(Port port, int n, boolean isInputCondition) {
		this(null, port, n, isInputCondition);
	}

	private PortCondition(PortCondition original, Port port, int n, boolean isInputCondition) {
		super(original);
		this.port = port;
		this.n = n;
		this.isInputCondition = isInputCondition;
	}

	public PortCondition copy(Port port, int n, boolean isInputCondition) {
		if (Objects.equals(this.port, port) && this.n == n && this.isInputCondition == isInputCondition) {
			return this;
		}
		return new PortCondition(this, port, n, isInputCondition);
	}

	private boolean isInputCondition;
	private Port port;
	private int n;
}
