package se.lth.cs.tycho.ir.decl;

import jdk.nashorn.internal.objects.Global;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.entity.Entity;

import java.util.function.Consumer;

public class GlobalEntityDecl extends AbstractDecl implements GlobalDecl {

    private final Entity entity;
    private final Availability availability;
    private final boolean external;

    public Entity getEntity() {
        return entity;
    }

    public GlobalEntityDecl withEntity(Entity entity) {
        return entity == this.entity ? this : new GlobalEntityDecl(this, getAvailability(), getName(), entity, getExternal());
    }

    public GlobalEntityDecl(GlobalEntityDecl original, Availability availability, String name, Entity entity, boolean external) {
        super(original, name);
        this.entity = entity;
        this.availability = availability;
        this.external = external;
    }

    private GlobalEntityDecl(GlobalEntityDecl original, Availability availability, String name, Entity entity) {
        super(original, name);
        this.entity = entity;
        this.availability = availability;
        this.external = false;
    }

    public static GlobalEntityDecl global(Availability availability, String name, Entity entity, boolean external) {
        GlobalEntityDecl entityDecl = new GlobalEntityDecl(null, availability, name, entity, external);
        entityDecl.setPosition(entity);
        return entityDecl;
    }

    public static GlobalEntityDecl global(Availability availability, String name, Entity entity) {

        GlobalEntityDecl entityDecl =  new GlobalEntityDecl(null, availability, name, entity, false);
        entityDecl.setPosition(entity);
        return  entityDecl;
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
            return new GlobalEntityDecl(this, getAvailability(), name, entity, this.external);
        }
    }

    public GlobalEntityDecl withAvailability(Availability availability) {
        if (getAvailability() == availability) {
            return this;
        } else {
            return new GlobalEntityDecl(this, availability, getName(), entity, this.external);
        }
    }

    public Availability getAvailability() {
        return availability;
    }

    public boolean getExternal() {
        return external;
    }

    @Override
    public GlobalEntityDecl clone() {
        return (GlobalEntityDecl) super.clone();
    }

    @Override
    public GlobalEntityDecl deepClone() {
        return (GlobalEntityDecl) super.deepClone();
    }
}
