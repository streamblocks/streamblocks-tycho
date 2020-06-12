package se.lth.cs.tycho.meta.interp.op;

import org.multij.Module;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.meta.interp.op.operator.Operator;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorDom;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorMinus;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorNot;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorRng;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorSize;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueInteger;
import se.lth.cs.tycho.meta.interp.value.ValueList;
import se.lth.cs.tycho.meta.interp.value.ValueMap;
import se.lth.cs.tycho.meta.interp.value.ValueReal;
import se.lth.cs.tycho.meta.interp.value.ValueSet;
import se.lth.cs.tycho.meta.interp.value.ValueUndefined;

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
		return new ValueInteger(operand.elements().size());
	}

	default Value apply(OperatorSize op, ValueSet operand) {
		return new ValueInteger(operand.elements().size());
	}

	default Value apply(OperatorSize op, ValueMap operand) {
		return new ValueInteger(operand.mappings().size());
	}

	default Value apply(OperatorMinus op, ValueInteger operand) {
		return new ValueInteger(-operand.integer());
	}

	default Value apply(OperatorMinus op, ValueReal operand) {
		return new ValueReal(-operand.real());
	}

	default Value apply(OperatorDom op, ValueMap operand) {
		return new ValueSet(operand.mappings().stream().map(ImmutableEntry::getKey).collect(Collectors.toSet()));
	}

	default Value apply(OperatorRng op, ValueMap operand) {
		return new ValueList(operand.mappings().map(ImmutableEntry::getValue));
	}
}
