package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ExprPhi extends Expression {

    private ImmutableList<Expression> operands;
    private final Variable lValue;
    private LinkedList<Expression> users;
    private  boolean isUndefined;
    private LinkedList<Expression> originalVar;

    private ExprPhi(IRNode original, Variable lValue, List<Expression> operands, List<ExprPhi> users, boolean isUndefined){
        super(original);
        this.users = new LinkedList<>(users);
        this.lValue = lValue;
        this.operands = ImmutableList.from(operands);
        this.isUndefined = isUndefined;
    }

    public void becomesUndefined(){
        this.isUndefined = true;
    }

    public boolean isUndefined(){
        return isUndefined;
    }

    public Variable getlValue() {
        return lValue;
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

    public ExprPhi(Variable lValue, List<Expression> operands){
        this(null, lValue, operands, ImmutableList.empty(), false);
    }

    public ImmutableList<Expression> getOperands() {
        return operands;
    }


    //TODO
    @Override
    public void forEachChild(Consumer<? super IRNode> action) {

    }
    @Override
    public Expression transformChildren(Transformation transformation) {
        return null;
    }
}
