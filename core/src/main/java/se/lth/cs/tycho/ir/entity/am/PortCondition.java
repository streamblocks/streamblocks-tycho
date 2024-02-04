package se.lth.cs.tycho.ir.entity.am;

import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.am.ctrl.ConditionVisitor;

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
 * @author Jorn W. Janneck
 * 
 */

public class PortCondition extends Condition {

	@Override
	public ConditionKind kind() {
		return isInputCondition ? ConditionKind.input : ConditionKind.output;
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

	@Override
	public <R, P> R accept(ConditionVisitor<R, P> v, P p) {
		if (isInputCondition)
			return v.visitInputCondition(this, p);
		else
			return v.visitOutputCondition(this, p);
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
		if (this.port == port && this.n == n && this.isInputCondition == isInputCondition) {
			return this;
		}
		return new PortCondition(this, port, n, isInputCondition);
	}

	private boolean isInputCondition;
	private Port port;
	private int n;

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(port);
	}

	@Override
	public PortCondition transformChildren(Transformation transformation) {
		return copy((Port) transformation.apply(port), n, isInputCondition);
	}

	@Override
	public PortCondition deepClone() {
		return (PortCondition) super.deepClone();
	}
}
