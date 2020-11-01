package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ExprPhi extends Expression {

    private ImmutableList<Expression> operands;
    private final LValue lValue;
    private LinkedList<Expression> users;

    private ExprPhi(IRNode original, LValue lValue, List<Expression> operands, List<ExprPhi> users){
        super(original);
        this.users = new LinkedList<>(users);
        this.lValue = lValue;
        this.operands = ImmutableList.from(operands);
    }

    public void setOperands(List<Expression> operands) {
        this.operands = ImmutableList.from(operands);
    }

    public LinkedList<Expression> getUsers() {
        return users;
    }

    public void addUser(List<Expression> users) {
        this.users.addAll(users);
    }

    public ExprPhi(LValue lValue, List<Expression> operands){
        this(null, lValue, operands, ImmutableList.empty());
    }

    public ImmutableList<Expression> getOperands() {
        return operands;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }

    @Override
    public Expression transformChildren(Transformation transformation) {
        return null;
    }
}
