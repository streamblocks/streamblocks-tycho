package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.Statement;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StmtPhi extends Statement {

    private final LValue lvalue;
    private final ImmutableList<Expression> operands;

    public StmtPhi(LValue lvalue, List<Expression> operands) {
        this(null, lvalue, operands);
    }

    public StmtPhi(Statement original, LValue lvalue, List<Expression> operands) {
        super(original);
        this.lvalue = lvalue;
        this.operands = ImmutableList.from(operands);
    }

    public StmtPhi copy(LValue lvalue, List<Expression> operands) {
        if (Objects.equals(this.lvalue, lvalue) && Lists.equals(this.operands, operands)) {
            return this;
        }
        return new StmtPhi(this, lvalue, operands);
    }


    public LValue getLValue() {
        return lvalue;
    }

    public ImmutableList<Expression> getOperands() {
        return operands;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(lvalue);
        operands.forEach(action);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return copy((LValue) transformation.apply(lvalue), (ImmutableList) operands.map(transformation));
    }
}
