package se.lth.cs.tycho.ir.entity.nl;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class EntityComprehensionExpr extends AbstractIRNode implements EntityExpr {

    private final Generator generator;
    private final ImmutableList<Expression> filters;
    private final EntityExpr collection;


    public EntityComprehensionExpr(Generator generator, List<Expression> filters, EntityExpr collection) {
        this(null, generator, filters, collection);
    }

    private EntityComprehensionExpr(IRNode original, Generator generator, List<Expression> filters, EntityExpr collection) {
        super(original);
        this.generator = generator;
        this.filters = ImmutableList.from(filters);
        this.collection = collection;
    }

    public EntityComprehensionExpr copy(Generator generator, List<Expression> filters, EntityExpr collection) {
        if (this.generator == generator && Lists.sameElements(this.filters, filters) && this.collection == collection) {
            return this;
        } else {
            return new EntityComprehensionExpr(this, generator, filters, collection);
        }
    }

    public Generator getGenerator() {
        return generator;
    }

    public EntityComprehensionExpr withGenerator(Generator generator) {
        return copy(generator, filters, collection);
    }

    public ImmutableList<Expression> getFilters() {
        return filters;
    }

    public EntityComprehensionExpr withFilters(List<Expression> filters) {
        return copy(generator, filters, collection);
    }

    public EntityExpr getCollection() {
        return collection;
    }

    public EntityComprehensionExpr withCollection(EntityExpr collection) {
        return copy(generator, filters, collection);
    }

    @Override
    public EntityComprehensionExpr deepClone() {
        return (EntityComprehensionExpr) super.deepClone();
    }

    @Override
    public EntityComprehensionExpr clone() {
        return (EntityComprehensionExpr) super.clone();
    }


    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(generator);
        filters.forEach(action);
        action.accept(collection);
    }

    @Override
    public IRNode transformChildren(Transformation transformation) {
        return copy(
                transformation.applyChecked(Generator.class, generator),
                transformation.mapChecked(Expression.class, filters),
                transformation.applyChecked(EntityExpr.class, collection)
        );
    }

    @Override
    public <R, P> R accept(EntityExprVisitor<R, P> v, P p) {
        return v.visitEntityComprehensionExpr(this, p);
    }
}
