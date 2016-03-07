package se.lth.cs.tycho.ir.decl;

import java.util.function.Consumer;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.Entity;

public class EntityDecl extends Decl {

	private final Entity entity;

	public Entity getEntity() {
		return entity;
	}

	public EntityDecl withEntity(Entity entity) {
		return entity == this.entity ? this : new EntityDecl(this, getAvailability(), getName(), entity, null);
	}

	private EntityDecl(EntityDecl original, Availability availability, String name, Entity entity, QID qid) {
		super(original, LocationKind.GLOBAL, availability, DeclKind.ENTITY, name, qid);
		this.entity = entity;
	}

	public static EntityDecl global(Availability availability, String name, Entity entity) {
		return new EntityDecl(null, availability, name, entity, null);
	}

	public static EntityDecl importDecl(Availability availability, String name, QID qid) {
		assert qid != null;
		return new EntityDecl(null, availability, name, null, qid);
	}

	@Override
	public void forEachChild(Consumer<? super IRNode> action) {
		if (entity != null) action.accept(entity);
	}

	@Override
	public EntityDecl transformChildren(Transformation transformation) {
		if (entity == null) {
			return this;
		} else {
			return withEntity((Entity) transformation.apply(entity));
		}
	}

	public EntityDecl withName(String name) {
		if (getName().equals(name)) {
			return this;
		} else {
			return new EntityDecl(this, getAvailability(), name, entity, getQualifiedIdentifier());
		}
	}

	public EntityDecl withAvailability(Availability availability) {
		if (getAvailability() == availability) {
			return this;
		} else {
			return new EntityDecl(this, availability, getName(), entity, getQualifiedIdentifier());
		}
	}

	public EntityDecl withQualifiedIdentifier(QID qid) {
		if (getQualifiedIdentifier().equals(qid)) {
			return this;
		} else {
			return new EntityDecl(this, getAvailability(), getName(), entity, qid);
		}
	}
}
