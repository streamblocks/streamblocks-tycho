package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;

import java.util.function.Consumer;

public class ExprPortIndexer extends Expression {

    private Port port;
    private Expression index;

    public ExprPortIndexer(ExprPortIndexer original, Port port, Expression index) {
        super(original);
        this.port = port;
        this.index = index;
    }

    public ExprPortIndexer(Port port, Expression index) {
        this(null, port, index);
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

    public ExprPortIndexer copy(Port port, Expression index) {
        if (this.port == port && this.index.equals(index)) {
            return this;
        }
        return new ExprPortIndexer(this, port, index);
    }

    @Override
    public ExprPortIndexer transformChildren(Transformation transformation) {
        return copy(
                (Port) transformation.apply(port),
                (Expression) transformation.apply(index)
        );
    }
}
