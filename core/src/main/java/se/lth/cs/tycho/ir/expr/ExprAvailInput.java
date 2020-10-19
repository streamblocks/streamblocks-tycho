package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;

import java.util.function.Consumer;

public class ExprAvailInput extends Expression {

    private final Port port;

    public ExprAvailInput(Port port) {
        this(null, port);
    }

    public ExprAvailInput(ExprAvailInput original, Port port) {
        super(original);
        this.port = port;
    }

    public ExprAvailInput copy(Port port) {
        if (this.port == port) {
            return this;
        }
        return new ExprAvailInput(this, port);
    }

    public Port getPort() {
        return port;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(port);
    }

    @Override
    public Expression transformChildren(Transformation transformation) {
        return copy((Port) transformation.apply(port));
    }


}
