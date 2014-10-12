package se.lth.cs.tycho.ir.decl;

import se.lth.cs.tycho.instance.net.Network;
import se.lth.cs.tycho.instance.net.Node;
import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.entity.Entity;

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
	
	@Override
	public DeclKind getKind() {
		return DeclKind.ENTITY;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public Availability getAvailability() {
		return availability;
	}

	public GlobalEntityDecl(String name, Entity entity, Availability availability) {
		this(null, name, entity, availability);
	}

	public GlobalEntityDecl(GlobalEntityDecl original, String name, Entity entity, Availability availability) {
		super(original);
		this.name = name;
		this.entity = entity;
		this.availability = availability;
	}

	private final String name;
	private final Entity entity;
	private final Availability availability;

}
