package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.function.Consumer;

public class StmtWhileSSA extends Statement {
    public StmtWhileSSA(Expression condition, List<Statement> body, List<Statement> header) {
        this(null, condition, body, header);
    }

    private StmtWhileSSA(StmtWhileSSA original, Expression condition, List<Statement> body, List<Statement> header) {
        super(original);
        this.condition = condition;
        this.body = ImmutableList.from(body);
        this.header = ImmutableList.from(header);
    }

    public StmtWhileSSA copy(Expression condition, List<Statement> body, List<Statement> header) {
        if (this.condition == condition && Lists.sameElements(this.body, body) && Lists.sameElements(this.header, header)) {
            return this;
        }
        return new StmtWhileSSA(this, condition, body, header);
    }

    public Expression getCondition() {
        return condition;
    }

    public StmtWhileSSA withCondition(Expression condition) {
        return copy(condition, body, header);
    }

    public ImmutableList<Statement> getBody() {
        return body;
    }

    public StmtWhileSSA withBody(List<Statement> body) {
        return copy(condition, body, header);
    }

    public ImmutableList<Statement> getHeader() {
        return header;
    }

    public StmtWhileSSA withheader(List<Statement> header) {
        return copy(condition, body, header);
    }

    private Expression condition;
    private ImmutableList<Statement> body;
    private ImmutableList<Statement> header;

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(condition);
        body.forEach(action);
        header.forEach(action);
    }

    @Override
    public StmtWhileSSA transformChildren(IRNode.Transformation transformation) {
        return copy(
                transformation.applyChecked(Expression.class, condition),
                transformation.mapChecked(Statement.class, body),
                transformation.mapChecked(Statement.class, header));
    }
}
