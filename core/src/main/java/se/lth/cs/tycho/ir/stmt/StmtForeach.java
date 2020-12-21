package se.lth.cs.tycho.ir.stmt;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.Generator;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

public class StmtForeach extends Statement {

    private final ImmutableList<Annotation> annotations;
    private final Generator generator;
    private final ImmutableList<Expression> filters;
    private final ImmutableList<Statement> body;

    public StmtForeach(Generator generators, List<Expression> filters, List<Statement> body) {
        this(null, ImmutableList.empty(), generators, filters, body);
    }

    public StmtForeach(List<Annotation> annotations, Generator generators, List<Expression> filters, List<Statement> body) {
        this(null, annotations, generators, filters, body);
    }

    private StmtForeach(StmtForeach original, List<Annotation> annotations, Generator generator, List<Expression> filters, List<Statement> body) {
        super(original);
        this.annotations = ImmutableList.from(annotations);
        this.generator = generator;
        this.filters = ImmutableList.from(filters);
        this.body = ImmutableList.from(body);
    }

    public StmtForeach copy(List<Annotation> annotations, Generator generator, List<Expression> filters, List<Statement> body) {
        if (Objects.equals(this.annotations, annotations) && this.generator == generator && Lists.sameElements(this.filters, filters) && Lists.sameElements(this.body, body)) {
            return this;
        } else {
            return new StmtForeach(this, annotations, generator, filters, body);
        }
    }

    public Generator getGenerator() {
        return generator;
    }

    public StmtForeach withGenerator(Generator generator) {
        return copy(annotations, generator, filters, body);
    }

    public ImmutableList<Expression> getFilters() {
        return filters;
    }

    public StmtForeach withFilters(List<Expression> filters) {
        return copy(annotations, generator, filters, body);
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public StmtForeach withBody(List<Statement> body) {
        return copy(annotations, generator, filters, body);
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(generator);
        filters.forEach(action);
        body.forEach(action);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StmtForeach transformChildren(Transformation transformation) {
        return copy(
                annotations,
                transformation.applyChecked(Generator.class, generator),
                transformation.mapChecked(Expression.class, filters),
                transformation.mapChecked(Statement.class, body));
    }
}
