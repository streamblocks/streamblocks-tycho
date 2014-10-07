package net.opendf.ir.entity;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.IRNode;

public abstract class Entity extends AbstractIRNode {

	public Entity(IRNode original) {
		super(original);
	}
	
	public abstract <R, P> R accept(EntityVisitor<R, P> visitor, P param); 

}
