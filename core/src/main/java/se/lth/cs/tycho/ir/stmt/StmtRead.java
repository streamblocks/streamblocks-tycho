package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StmtRead extends Statement {
    private final Port port;
    private final ImmutableList<LValue> lvalues;
    private final Expression repeatExpression;
    private final ImmutableList<Annotation> annotations;

    public StmtRead(List<Annotation> annotations, Port port, ImmutableList<LValue> lvalues, Expression repeatExpression) {
        this(null, annotations, port, lvalues, repeatExpression);
    }

    public StmtRead(Port port, ImmutableList<LValue> lvalues, Expression repeatExpression) {
        this(null, ImmutableList.empty(), port, lvalues, repeatExpression);
    }

    private StmtRead(StmtRead original, List<Annotation> annotations, Port port, List<LValue> lvalues, Expression repeatExpression) {
        super(original);
        assert port != null;
        this.annotations = ImmutableList.from(annotations);
        this.port = port;
        this.lvalues = ImmutableList.from(lvalues);
        this.repeatExpression = repeatExpression;
    }

    public StmtRead copy(List<Annotation> annotations, Port port, List<LValue> lvalues, Expression repeatExpression) {
        if (this.port == port && Lists.sameElements(this.lvalues, lvalues) && this.repeatExpression == repeatExpression) {
            return this;
        } else {
            return new StmtRead(this, annotations, port, lvalues, repeatExpression);
        }
    }

    public Port getPort() {
        return port;
    }

    public ImmutableList<LValue> getLValues() {
        return lvalues;
    }

    public Expression getRepeatExpression() {
        return repeatExpression;
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public Statement withAnnotations(List<Annotation> annotations) {
        return copy(annotations, port, lvalues, repeatExpression);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        action.accept(port);
        lvalues.forEach(action);
        if (repeatExpression != null) action.accept(repeatExpression);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StmtRead transformChildren(Transformation transformation) {
        return copy(
                transformation.mapChecked(Annotation.class, annotations),
                (Port) transformation.apply(port),
                (ImmutableList) lvalues.map(transformation),
                repeatExpression == null ? null : (Expression) transformation.apply(repeatExpression)
        );
    }
}
