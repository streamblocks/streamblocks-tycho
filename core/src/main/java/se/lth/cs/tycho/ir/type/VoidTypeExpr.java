package se.lth.cs.tycho.ir.type;

import se.lth.cs.tycho.ir.AbstractIRNode;
import se.lth.cs.tycho.ir.IRNode;

import java.util.function.Consumer;

public class VoidTypeExpr extends AbstractIRNode implements TypeExpr<VoidTypeExpr> {

    public VoidTypeExpr(){
        this(null);
    }

    public VoidTypeExpr(IRNode original){
        super(original);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
    }

    @Override
    public VoidTypeExpr transformChildren(Transformation transformation) {
        return new VoidTypeExpr();
    }
}
