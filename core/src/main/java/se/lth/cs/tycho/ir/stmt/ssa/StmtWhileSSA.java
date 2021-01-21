package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StmtWhileSSA extends Statement {
    public StmtWhileSSA(Expression condition, List<Statement> body, List<Statement> join) {
        this(null, condition, body, join);
    }

    private StmtWhileSSA(StmtWhileSSA original, Expression condition, List<Statement> body, List<Statement> join) {
        super(original);
        this.condition = condition;
        this.body = ImmutableList.from(body);
        this.join = ImmutableList.from(join);
    }

    public StmtWhileSSA copy(Expression condition, List<Statement> body, List<Statement> join) {
        if (this.condition == condition && Lists.sameElements(this.body, body) && Lists.sameElements(this.join, join)) {
            return this;
        }
        return new StmtWhileSSA(this, condition, body, join);
    }

    public Expression getCondition() {
        return condition;
    }

    public StmtWhileSSA withCondition(Expression condition) {
        return copy(condition, body, join);
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public StmtWhileSSA withBody(List<Statement> body) {
        return copy(condition, body, join);
    }

    public StmtWhileSSA withJoin(List<Statement> join) {
        return copy(condition, body, join);
    }

    private Expression condition;
    private ImmutableList<Statement> body;
    private ImmutableList<Statement> join;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(condition);
        body.forEach(action);
        join.forEach(action);
    }

    @Override
    public StmtWhileSSA transformChildren(IRNode.Transformation transformation) {
        return copy(
                transformation.applyChecked(Expression.class, condition),
                transformation.mapChecked(Statement.class, body),
                transformation.mapChecked(Statement.class, join));
    }
}
