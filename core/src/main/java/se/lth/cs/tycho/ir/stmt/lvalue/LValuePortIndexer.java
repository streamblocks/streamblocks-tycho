package se.lth.cs.tycho.ir.stmt.lvalue;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.expr.Expression;

import java.util.function.Consumer;

public class LValuePortIndexer extends LValue {

    private Port port;
    private Expression index;


    public LValuePortIndexer(LValuePortIndexer original, Port port, Expression index) {
        super(original);
        assert port != null;
        this.port = port;
        this.index = index;
    }

    public LValuePortIndexer(Port port, Expression index) {
        this(null, port, index);
        this.index = index;
    }

    public LValuePortIndexer copy(Port port, Expression index) {
        if (this.port == port && this.index.equals(index)) {
            return this;
        } else {
            return new LValuePortIndexer(this, port, index);
        }
    }

    public Expression getIndex() {
        return index;
    }

    public Port getPort(){
        return port;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(port);
        action.accept(index);
    }

    public LValuePortIndexer transformChildren(Transformation transformation) {
        return copy(
                (Port) transformation.apply(port),
                (Expression) transformation.apply(index)
        );
    }

}
