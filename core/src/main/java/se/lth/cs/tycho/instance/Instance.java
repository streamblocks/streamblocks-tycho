package se.lth.cs.tycho.instance;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.PortContainer;

public abstract class Instance extends AbstractIRNode implements PortContainer {
	
	public abstract <R, P> R accept(InstanceVisitor<R, P> visitor, P param);

	public Instance(IRNode original) {
		super(original);
	}

}
