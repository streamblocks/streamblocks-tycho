package se.lth.cs.tycho.ir.entity;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

public abstract class Entity extends AbstractIRNode {

	public Entity(IRNode original) {
		super(original);
	}
	
	public abstract <R, P> R accept(EntityVisitor<R, P> visitor, P param); 

}
