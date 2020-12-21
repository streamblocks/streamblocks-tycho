package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StmtWrite extends Statement {
    private final Port port;
    private final ImmutableList<Expression> values;
    private final Expression repeatExpression;
    private final ImmutableList<Annotation> annotations;

    public StmtWrite(List<Annotation> annotations, Port port, ImmutableList<Expression> values, Expression repeatExpression) {
        this(null, annotations, port, values, repeatExpression);
    }

    public StmtWrite(Port port, ImmutableList<Expression> values, Expression repeatExpression) {
        this(null, ImmutableList.empty(), port, values, repeatExpression);
    }

    private StmtWrite(StmtWrite original, List<Annotation> annotations, Port port, List<Expression> values, Expression repeatExpression) {
        super(original);
        assert port != null;
        this.annotations = ImmutableList.from(annotations);
        this.port = port;
        this.values = ImmutableList.from(values);
        this.repeatExpression = repeatExpression;
    }

    public StmtWrite copy(List<Annotation> annotations, Port port, List<Expression> values, Expression repeatExpression) {
        if (Objects.equals(this.annotations, annotations) && this.port == port && Lists.sameElements(this.values, values) && this.repeatExpression == repeatExpression) {
            return this;
        } else {
            return new StmtWrite(this,annotations,  port, values, repeatExpression);
        }
    }

    public Port getPort() {
        return port;
    }

    public ImmutableList<Expression> getValues() {
        return values;
    }

    public Expression getRepeatExpression() {
        return repeatExpression;
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        action.accept(port);
        values.forEach(action);
        if (repeatExpression != null) action.accept(repeatExpression);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StmtWrite transformChildren(Transformation transformation) {
        return copy(
                annotations,
                (Port) transformation.apply(port),
                (ImmutableList) values.map(transformation),
                repeatExpression == null ? null : (Expression) transformation.apply(repeatExpression)
        );
    }
}
