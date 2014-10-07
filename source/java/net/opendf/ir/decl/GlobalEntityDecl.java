package net.opendf.ir.decl;

import net.opendf.ir.AbstractIRNode;
import net.opendf.ir.entity.Entity;
import net.opendf.ir.net.Network;
import net.opendf.ir.net.Node;

/**
 * PortContainer is the common base class of things that contain input and
 * output ports, viz. {@link Network}s and {@link Node}s.
 * 
 * @author Jorn W. Janneck <jwj@acm.org>
 * 
 */

public class GlobalEntityDecl extends AbstractIRNode implements GlobalDecl {
	@Override
	public <R, P> R accept(GlobalDeclVisitor<R, P> visitor, P param) {
		return visitor.visitGlobalEntityDecl(this, param);
	}

	@Override
	public String getName() {
		return name;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public Visibility getVisibility() {
		return Visibility.PUBLIC;
	}

	public GlobalEntityDecl(String name, Entity entity) {
		this(null, name, entity);
	}

	public GlobalEntityDecl(GlobalEntityDecl original, String name, Entity entity) {
		super(original);
		this.name = name;
		this.entity = entity;
	}

	private final String name;
	private final Entity entity;

}
