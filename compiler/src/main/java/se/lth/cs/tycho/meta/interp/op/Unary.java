package se.lth.cs.tycho.meta.interp.op;

import org.multij.Module;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.meta.interp.op.operator.*;
import se.lth.cs.tycho.meta.interp.value.*;
import se.lth.cs.tycho.meta.interp.value.ValueLong;

import java.util.stream.Collectors;

@Module
public interface Unary {

    default Value apply(Operator op, Value operand) {
        return ValueUndefined.undefined();
    }

    default Value apply(OperatorNot op, ValueBool operand) {
        return new ValueBool(!operand.bool());
    }

    default Value apply(OperatorSize op, ValueList operand) {
        return new ValueLong(operand.elements().size());
    }

    default Value apply(OperatorSize op, ValueSet operand) {
        return new ValueLong(operand.elements().size());
    }

    default Value apply(OperatorSize op, ValueMap operand) {
        return new ValueLong(operand.mappings().size());
    }

    default Value apply(OperatorMinus op, ValueLong operand) {
        return new ValueLong(-operand.value());
    }

    default Value apply(OperatorMinus op, ValueReal operand) {
        return new ValueReal(-operand.real());
    }

    default Value apply(OperatorInverse op, ValueLong operand) {
        return new ValueLong(~operand.value());
    }

    default Value apply(OperatorDom op, ValueMap operand) {
        return new ValueSet(operand.mappings().stream().map(ImmutableEntry::getKey).collect(Collectors.toSet()));
    }

    default Value apply(OperatorRng op, ValueMap operand) {
        return new ValueList(operand.mappings().map(ImmutableEntry::getValue));
    }
}
