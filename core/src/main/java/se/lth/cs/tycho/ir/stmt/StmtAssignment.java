package se.lth.cs.tycho.ir.stmt;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

/**
 * A statement for assigning to a variable.
 */
public class StmtAssignment extends Statement {
    private LValue lvalue;
    private Expression expression;
    private ImmutableList<Annotation> annotations;

    /**
     * Constructs a StmtAssignment.
     *
     * @param lvalue     the left hand side
     * @param expression the right hand side
     */
    public StmtAssignment(LValue lvalue, Expression expression) {
        this(null, ImmutableList.empty(), lvalue, expression);
    }


    public StmtAssignment(List<Annotation> annotations, LValue lvalue, Expression expression) {
        this(null, annotations, lvalue, expression);
    }

    private StmtAssignment(StmtAssignment original, List<Annotation> annotations, LValue lvalue, Expression expression) {
        super(original);
        this.annotations = ImmutableList.from(annotations);
        this.lvalue = lvalue;
        this.expression = expression;
    }

    public StmtAssignment copy(List<Annotation> annotations, LValue lvalue, Expression expression) {
        if (Objects.equals(this.annotations, annotations) && Objects.equals(this.lvalue, lvalue) && Objects.equals(this.expression, expression)) {
            return this;
        }
        return new StmtAssignment(this, annotations, lvalue, expression);
    }

    /**
     * Returns the left hand side of the assignment.
     *
     * @return the left hand side
     */
    public LValue getLValue() {
        return lvalue;
    }

    /**
     * Returns the right hand side of the assignment.
     *
     * @return the right hand side
     */
    public Expression getExpression() {
        return expression;
    }

    public ImmutableList<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        annotations.forEach(action);
        action.accept(lvalue);
        action.accept(expression);
    }

    @Override
    public StmtAssignment transformChildren(Transformation transformation) {
        return copy(transformation.mapChecked(Annotation.class, annotations), (LValue) transformation.apply(lvalue), (Expression) transformation.apply(expression));
    }

    @Override
    public Statement withAnnotations(List<Annotation> annotations) {
        return copy(annotations, lvalue, expression);
    }
}
