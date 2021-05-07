package se.lth.cs.tycho.ir.entity.cal.regexp;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.ir.util.Lists;

import java.util.function.Consumer;

public class RegExpBinary extends RegExp {

    private ImmutableList<String> operations;
    private ImmutableList<RegExp> operands;

    public RegExpBinary(ImmutableList<String> operations, ImmutableList<RegExp> operands) {
        this(null, operations, operands);
    }

    private RegExpBinary(RegExpBinary original, ImmutableList<String> operations, ImmutableList<RegExp> operands) {
        super(original);
        assert (operations.size() == operands.size() - 1);
        this.operations = operations;
        this.operands = operands;
    }

    public ImmutableList<String> getOperations(){
        return operations;
    }

    public ImmutableList<RegExp> getOperands(){
        return operands;
    }

    public RegExpBinary copy(ImmutableList<String> operations, ImmutableList<RegExp> operands) {
        if (Lists.equals(this.operations, operations) && Lists.equals(this.operands, operands)) {
            return this;
        }
        return new RegExpBinary(this, operations, operands);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        operands.forEach(action);
    }

    @Override
    public RegExpBinary transformChildren(Transformation transformation) {
        return copy(operations, (ImmutableList) operands.map(transformation));
    }
}
