package se.lth.cs.tycho.ir.decl;

import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.Entity;

public class EntityDecl extends Decl {

	private final Entity entity;

	public Entity getEntity() {
		return entity;
	}

	private EntityDecl(EntityDecl original, Availability availability, String name, Entity entity) {
		super(original, LocationKind.GLOBAL, availability, DeclKind.ENTITY, name, null);
		this.entity = entity;
	}

	public static EntityDecl global(Availability availability, String name, Entity entity) {
		return new EntityDecl(null, availability, name, entity);
	}

	public EntityDecl copyAsGlobal(Availability availability, String name, Entity entity) {
		if (this.getAvailability() == availability && Objects.equals(this.getName(), name)
				&& this.entity.equals(entity)) {
			return this;
		} else {
			return global(availability, name, entity);
		}
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		action.accept(entity);
	}
}
