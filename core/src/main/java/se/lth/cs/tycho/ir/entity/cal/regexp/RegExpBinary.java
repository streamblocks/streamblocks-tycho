package se.lth.cs.tycho.ir.entity.cal.regexp;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class RegExpBinary extends RegExp {

    private String operation;
    private RegExp left;
    private RegExp right;

    public RegExpBinary(String operation, RegExp left, RegExp right) {
        this(null, operation, left, right);
    }

    private RegExpBinary(RegExpBinary original, String operation, RegExp left, RegExp right) {
        super(original);
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    public RegExpBinary copy(String operation, RegExp left, RegExp right) {
        if (Objects.equals(this.operation, operation) && Objects.equals(this.left, left) && Objects.equals(this.right, right)) {
            return this;
        }
        return new RegExpBinary(this, operation, left, right);
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(left);
        action.accept(right);
    }

    @Override
    public RegExp transformChildren(Transformation transformation) {
        return copy(operation, (RegExp) transformation.apply(left), (RegExp) transformation.apply(right));
    }
}
