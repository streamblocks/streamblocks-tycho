package se.lth.cs.tycho.ir.entity.cal.regexp;

import se.lth.cs.tycho.ir.IRNode;

import java.util.Objects;
import java.util.function.Consumer;

public class RegExpUnary extends RegExp {

    private String operation;
    private RegExp operand;

    public RegExpUnary(String operation, RegExp operand) {
        this(null, operation, operand);
    }

    private RegExpUnary(RegExpUnary original, String operation, RegExp operand) {
        super(original);
        this.operation = operation;
        this.operand = operand;
    }


    public RegExpUnary copy(String operation, RegExp operand) {
        if (Objects.equals(this.operation, operation) && Objects.equals(this.operand, operand)) {
            return this;
        }
        return new RegExpUnary(this, operation, operand);
    }

    public String getOperation() {
        return operation;
    }

    public RegExp getOperand() {
        return operand;
    }

    @Override
    public void forEachChild(Consumer<? super IRNode> action) {
        action.accept(operand);
    }

    @Override
    public RegExp transformChildren(Transformation transformation) {
        return copy(operation, (RegExp) transformation.apply(operand));
    }
}
