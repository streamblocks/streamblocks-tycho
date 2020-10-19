package se.lth.cs.tycho.ir.expr;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.Port;

import java.util.function.Consumer;

public class ExprAvailOutput extends Expression {
    private final Port port;

    public ExprAvailOutput(Port port) {
        this(null, port);
    }

    public ExprAvailOutput(ExprAvailOutput original, Port port) {
        super(original);
        this.port = port;
    }

    public ExprAvailOutput copy(Port port) {
        if (this.port == port) {
            return this;
        }
        return new ExprAvailOutput(this, port);
    }

    public Port getPort() {
        return port;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(port);
    }

    @Override
    public Expression transformChildren(IRNode.Transformation transformation) {
        return copy((Port) transformation.apply(port));
    }


}
