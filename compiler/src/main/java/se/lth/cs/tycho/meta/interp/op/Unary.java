package se.lth.cs.tycho.meta.interp.op;

import org.multij.Module;
import se.lth.cs.tycho.meta.interp.op.operator.Operator;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorMinus;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorNot;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueInteger;
import se.lth.cs.tycho.meta.interp.value.ValueReal;
import se.lth.cs.tycho.meta.interp.value.ValueUndefined;

@Module
public interface Unary {

	default Value apply(Operator op, Value operand) {
		return ValueUndefined.undefined();
	}

	default Value apply(OperatorNot op, ValueBool operand) {
		return new ValueBool(!operand.bool());
	}

	default Value apply(OperatorMinus op, ValueInteger operand) {
		return new ValueInteger(-operand.integer());
	}

	default Value apply(OperatorMinus op, ValueReal operand) {
		return new ValueReal(-operand.real());
	}
}
