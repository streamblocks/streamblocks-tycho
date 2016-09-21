package se.lth.cs.tycho.ir.decl;

import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.Entity;

public class GlobalEntityDecl extends AbstractDecl implements GlobalDecl {

	private final Entity entity;
	private final Availability availability;

	public Entity getEntity() {
		return entity;
	}

	public GlobalEntityDecl withEntity(Entity entity) {
		return entity == this.entity ? this : new GlobalEntityDecl(this, getAvailability(), getName(), entity);
	}

	private GlobalEntityDecl(GlobalEntityDecl original, Availability availability, String name, Entity entity) {
		super(original, name);
		this.entity = entity;
		this.availability = availability;
	}

	public static GlobalEntityDecl global(Availability availability, String name, Entity entity) {
		return new GlobalEntityDecl(null, availability, name, entity);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (entity != null) action.accept(entity);
	}

	@Override
	public GlobalEntityDecl transformChildren(Transformation transformation) {
		if (entity == null) {
			return this;
		} else {
			return withEntity((Entity) transformation.apply(entity));
		}
	}

	public GlobalEntityDecl withName(String name) {
		if (getName().equals(name)) {
			return this;
		} else {
			return new GlobalEntityDecl(this, getAvailability(), name, entity);
		}
	}

	public GlobalEntityDecl withAvailability(Availability availability) {
		if (getAvailability() == availability) {
			return this;
		} else {
			return new GlobalEntityDecl(this, availability, getName(), entity);
		}
	}

	public Availability getAvailability() {
		return availability;
	}
}
