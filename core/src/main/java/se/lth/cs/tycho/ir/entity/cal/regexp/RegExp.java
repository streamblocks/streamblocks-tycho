package se.lth.cs.tycho.ir.entity.cal.regexp;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.expr.Expression;

public abstract class RegExp extends AbstractIRNode {
    public RegExp(IRNode original) {
        super(original);
    }

    @Override
    public RegExp deepClone() {
        return (RegExp) super.deepClone();
    }

    @Override
    public abstract RegExp transformChildren(Transformation transformation);
}
