package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.Objects;
import java.util.function.Consumer;

public class StmtReturn extends Statement {

    private final Expression expression;

    public StmtReturn(Expression expression) {
        this(null, expression);
    }

    public StmtReturn(StmtReturn original, Expression expression) {
        super(original);
        this.expression = expression;
    }


    public StmtReturn copy(Expression expression) {
        if (Objects.equals(this.expression, expression)) {
            return this;
        }
        return new StmtReturn(this, expression);
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(expression);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return copy((Expression) transformation.apply(expression));
    }
}
