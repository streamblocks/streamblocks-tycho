package se.lth.cs.tycho.meta.interp.value;

import se.lth.cs.tycho.ir.entity.nl.EntityExpr;

import java.util.Objects;

public class ValueEntityExpr implements Value {

    private final EntityExpr entityExpr;

    public ValueEntityExpr(EntityExpr entityExpr) {
        this.entityExpr = entityExpr;
    }

    public EntityExpr getEntityExpr() {
        return entityExpr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueEntityExpr that = (ValueEntityExpr) o;
        return entityExpr.equals(that.entityExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityExpr);
    }
}

