package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Variable;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ExprPhi extends Expression {

    private ImmutableList<LocalVarDecl> operands;
    private final Variable lValue;
    private LinkedList<LocalVarDecl> users;
    private  boolean isUndefined;
    private LinkedList<Expression> originalVar;

    private ExprPhi(IRNode original, Variable lValue, List<LocalVarDecl> operands, List<LocalVarDecl> users, boolean isUndefined){
        super(original);
        this.users = new LinkedList<>(users);
        this.lValue = lValue;
        this.operands = ImmutableList.from(operands);
        this.isUndefined = isUndefined;
    }

    public void clearNullArgs(){
        operands = (ImmutableList<LocalVarDecl>) operands.stream().filter(Objects::nonNull).collect(Collectors.toList());
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

    public void setOperands(List<LocalVarDecl> operands) {
        this.operands = ImmutableList.from(operands);
    }

    public void addSingleOperand(LocalVarDecl op){
        this.operands = ImmutableList.concat(operands, ImmutableList.of(op));
    }

    public LinkedList<LocalVarDecl> getUsers() {
        return users;
    }

    public void addUser(List<LocalVarDecl> users) {
        this.users.addAll(users);
    }

    public ExprPhi(Variable lValue, List<LocalVarDecl> operands){
        this(null, lValue, operands, ImmutableList.empty(), false);
    }

    public ImmutableList<LocalVarDecl> getOperands() {
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
