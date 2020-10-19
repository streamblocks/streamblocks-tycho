package se.lth.cs.tycho.ir.stmt.ssa;

import se.lth.cs.tycho.ir.IRNode;
import se.lth.cs.tycho.ir.stmt.Statement;

import java.util.function.Consumer;

public class StmtGoto extends Statement {

    public StmtGoto(String label) {
        this(null, label);
    }

    public StmtGoto(StmtGoto original, String label) {
        super(original);
        this.label = label;
    }

    private final String label;

    public String getLabel() {
        return label;
    }

    public StmtGoto copy(String label) {
        if (this.label.equals(label)) {
            return this;
        }
        return new StmtGoto(this, label);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
    }

    @Override
    public Statement transformChildren(Transformation transformation) {
        return copy(getLabel());
    }
}
