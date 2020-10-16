package se.lth.cs.tycho.ir.stmt;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.stmt.lvalue.LValue;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StmtPhi extends Statement {

    private LValue lvalue;
    private ImmutableList<Expression> arguments;

    public StmtPhi(LValue lvalue, List<Expression> arguments) {
        this(null, lvalue, arguments);
    }

    public StmtPhi(Statement original, LValue lvalue, List<Expression> arguments) {
        super(original);
        this.lvalue = lvalue;
        this.arguments = ImmutableList.from(arguments);
    }

    public StmtPhi copy(LValue lvalue, List<Expression> arguments) {
        if (Objects.equals(this.lvalue, lvalue) && Lists.equals(this.arguments, arguments)) {
            return this;
        }
        return new StmtPhi(this, lvalue, arguments);
    }

    public LValue getLValue() {
        return lvalue;
    }

    public ImmutableList<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(lvalue);
        arguments.forEach(action);
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return copy((LValue) transformation.apply(lvalue), (ImmutableList) arguments.map(transformation));
    }
}
