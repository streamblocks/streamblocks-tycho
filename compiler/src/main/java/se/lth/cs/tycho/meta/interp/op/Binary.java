package se.lth.cs.tycho.meta.interp.op;

import org.multij.Module;
import se.lth.cs.tycho.meta.interp.op.operator.Operator;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorAnd;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorConjunction;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorDifferent;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorDisjunction;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorDiv;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorEqual;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorGreaterEqualThan;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorGreaterThan;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorIn;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorLowerEqualThan;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorLowerThan;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorMinus;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorMod;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorOr;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorPlus;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorShiftLeft;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorShiftRight;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorTimes;
import se.lth.cs.tycho.meta.interp.op.operator.OperatorXOr;
import se.lth.cs.tycho.meta.interp.value.Value;
import se.lth.cs.tycho.meta.interp.value.ValueBool;
import se.lth.cs.tycho.meta.interp.value.ValueChar;
import se.lth.cs.tycho.meta.interp.value.ValueInteger;
import se.lth.cs.tycho.meta.interp.value.ValueList;
import se.lth.cs.tycho.meta.interp.value.ValueReal;
import se.lth.cs.tycho.meta.interp.value.ValueUndefined;

@Module
public interface Binary {

	default Value apply(Operator op, Value lhs, Value rhs) {
		return ValueUndefined.undefined();
	}

	default Value apply(OperatorAnd op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() & rhs.integer());
	}

	default Value apply(OperatorOr op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() | rhs.integer());
	}

	default Value apply(OperatorXOr op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() ^ rhs.integer());
	}

	default Value apply(OperatorShiftLeft op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() << rhs.integer());
	}

	default Value apply(OperatorShiftRight op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() >> rhs.integer());
	}

	default Value apply(OperatorConjunction op, ValueBool lhs, ValueBool rhs) {
		return new ValueBool(lhs.bool() && rhs.bool());
	}

	default Value apply(OperatorDisjunction op, ValueBool lhs, ValueBool rhs) {
		return new ValueBool(lhs.bool() || rhs.bool());
	}

	default Value apply(OperatorLowerThan op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueBool(lhs.integer() < rhs.integer());
	}

	default Value apply(OperatorLowerThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() < rhs.real());
	}

	default Value apply(OperatorLowerThan op, ValueInteger lhs, ValueReal rhs) {
		return new ValueBool(lhs.integer() < rhs.real());
	}

	default Value apply(OperatorLowerThan op, ValueReal lhs, ValueInteger rhs) {
		return new ValueBool(lhs.real() < rhs.integer());
	}

	default Value apply(OperatorLowerThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() < rhs.character());
	}

	default Value apply(OperatorLowerEqualThan op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueBool(lhs.integer() <= rhs.integer());
	}

	default Value apply(OperatorLowerEqualThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() <= rhs.real());
	}

	default Value apply(OperatorLowerEqualThan op, ValueInteger lhs, ValueReal rhs) {
		return new ValueBool(lhs.integer() <= rhs.real());
	}

	default Value apply(OperatorLowerEqualThan op, ValueReal lhs, ValueInteger rhs) {
		return new ValueBool(lhs.real() <= rhs.integer());
	}

	default Value apply(OperatorLowerEqualThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() <= rhs.character());
	}

	default Value apply(OperatorGreaterThan op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueBool(lhs.integer() > rhs.integer());
	}

	default Value apply(OperatorGreaterThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() > rhs.real());
	}

	default Value apply(OperatorGreaterThan op, ValueInteger lhs, ValueReal rhs) {
		return new ValueBool(lhs.integer() > rhs.real());
	}

	default Value apply(OperatorGreaterThan op, ValueReal lhs, ValueInteger rhs) {
		return new ValueBool(lhs.real() > rhs.integer());
	}

	default Value apply(OperatorGreaterThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() > rhs.character());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueBool(lhs.integer() >= rhs.integer());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() >= rhs.real());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueInteger lhs, ValueReal rhs) {
		return new ValueBool(lhs.integer() >= rhs.real());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueReal lhs, ValueInteger rhs) {
		return new ValueBool(lhs.real() >= rhs.integer());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() >= rhs.character());
	}

	default Value apply(OperatorPlus op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() + rhs.integer());
	}

	default Value apply(OperatorPlus op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() + rhs.real());
	}

	default Value apply(OperatorPlus op, ValueInteger lhs, ValueReal rhs) {
		return new ValueReal(lhs.integer() + rhs.real());
	}

	default Value apply(OperatorPlus op, ValueReal lhs, ValueInteger rhs) {
		return new ValueReal(lhs.real() + rhs.integer());
	}

	default Value apply(OperatorMinus op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() - rhs.integer());
	}

	default Value apply(OperatorMinus op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() - rhs.real());
	}

	default Value apply(OperatorMinus op, ValueInteger lhs, ValueReal rhs) {
		return new ValueReal(lhs.integer() - rhs.real());
	}

	default Value apply(OperatorMinus op, ValueReal lhs, ValueInteger rhs) {
		return new ValueReal(lhs.real() - rhs.integer());
	}

	default Value apply(OperatorTimes op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() * rhs.integer());
	}

	default Value apply(OperatorTimes op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() * rhs.real());
	}

	default Value apply(OperatorTimes op, ValueInteger lhs, ValueReal rhs) {
		return new ValueReal(lhs.integer() * rhs.real());
	}

	default Value apply(OperatorTimes op, ValueReal lhs, ValueInteger rhs) {
		return new ValueReal(lhs.real() * rhs.integer());
	}

	default Value apply(OperatorDiv op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() / rhs.integer());
	}

	default Value apply(OperatorDiv op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() / rhs.real());
	}

	default Value apply(OperatorDiv op, ValueInteger lhs, ValueReal rhs) {
		return new ValueReal(lhs.integer() / rhs.real());
	}

	default Value apply(OperatorDiv op, ValueReal lhs, ValueInteger rhs) {
		return new ValueReal(lhs.real() / rhs.integer());
	}

	default Value apply(OperatorMod op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueInteger(lhs.integer() % rhs.integer());
	}

	default Value apply(OperatorEqual op, ValueInteger lhs, ValueInteger rhs) {
		return new ValueBool(lhs.integer() == rhs.integer());
	}

	default Value apply(OperatorEqual op, Value lhs, Value rhs) {
		return new ValueBool(lhs.equals(rhs));
	}

	default Value apply(OperatorDifferent op, Value lhs, Value rhs) {
		return new ValueBool(!(lhs.equals(rhs)));
	}

	default Value apply(OperatorIn op, Value lhs, ValueList rhs) {
		if (lhs == ValueUndefined.undefined()) {
			return ValueUndefined.undefined();
		}
		return new ValueBool(rhs.elements().stream().anyMatch(element -> element.equals(lhs)));
	}
}
