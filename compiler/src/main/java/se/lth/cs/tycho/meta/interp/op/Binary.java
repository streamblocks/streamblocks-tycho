package se.lth.cs.tycho.meta.interp.op;

import org.multij.Module;
import se.lth.cs.tycho.ir.util.ImmutableEntry;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.meta.interp.op.operator.*;
import se.lth.cs.tycho.meta.interp.value.*;
import se.lth.cs.tycho.meta.interp.value.ValueLong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Module
public interface Binary {

	default Value apply(Operator op, Value lhs, Value rhs) {
		return ValueUndefined.undefined();
	}

	default Value apply(OperatorAnd op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() & rhs.value());
	}

	default Value apply(OperatorOr op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() | rhs.value());
	}

	default Value apply(OperatorXOr op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() ^ rhs.value());
	}

	default Value apply(OperatorShiftLeft op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() << rhs.value());
	}

	default Value apply(OperatorShiftRight op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() >> rhs.value());
	}

	default Value apply(OperatorConjunction op, ValueBool lhs, ValueBool rhs) {
		return new ValueBool(lhs.bool() && rhs.bool());
	}

	default Value apply(OperatorDisjunction op, ValueBool lhs, ValueBool rhs) {
		return new ValueBool(lhs.bool() || rhs.bool());
	}

	default Value apply(OperatorLowerThan op, ValueLong lhs, ValueLong rhs) {
		return new ValueBool(lhs.value() < rhs.value());
	}

	default Value apply(OperatorLowerThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() < rhs.real());
	}

	default Value apply(OperatorLowerThan op, ValueLong lhs, ValueReal rhs) {
		return new ValueBool(lhs.value() < rhs.real());
	}

	default Value apply(OperatorLowerThan op, ValueReal lhs, ValueLong rhs) {
		return new ValueBool(lhs.real() < rhs.value());
	}

	default Value apply(OperatorLowerThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() < rhs.character());
	}

	default Value apply(OperatorLowerThan op, ValueSet lhs, ValueSet rhs) {
		return new ValueBool(lhs.elements().stream().allMatch(elem -> rhs.elements().contains(elem)) && lhs.elements().size() < rhs.elements().size());
	}

	default Value apply(OperatorLowerThan op, ValueString lhs, ValueString rhs) {
		return new ValueBool(lhs.string().compareTo(rhs.string()) < 0);
	}

	default Value apply(OperatorLowerEqualThan op, ValueLong lhs, ValueLong rhs) {
		return new ValueBool(lhs.value() <= rhs.value());
	}

	default Value apply(OperatorLowerEqualThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() <= rhs.real());
	}

	default Value apply(OperatorLowerEqualThan op, ValueLong lhs, ValueReal rhs) {
		return new ValueBool(lhs.value() <= rhs.real());
	}

	default Value apply(OperatorLowerEqualThan op, ValueReal lhs, ValueLong rhs) {
		return new ValueBool(lhs.real() <= rhs.value());
	}

	default Value apply(OperatorLowerEqualThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() <= rhs.character());
	}

	default Value apply(OperatorLowerEqualThan op, ValueSet lhs, ValueSet rhs) {
		return new ValueBool(lhs.elements().stream().allMatch(elem -> rhs.elements().contains(elem)) && lhs.elements().size() <= rhs.elements().size());
	}

	default Value apply(OperatorLowerEqualThan op, ValueString lhs, ValueString rhs) {
		return new ValueBool(lhs.string().compareTo(rhs.string()) <= 0);
	}

	default Value apply(OperatorGreaterThan op, ValueLong lhs, ValueLong rhs) {
		return new ValueBool(lhs.value() > rhs.value());
	}

	default Value apply(OperatorGreaterThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() > rhs.real());
	}

	default Value apply(OperatorGreaterThan op, ValueLong lhs, ValueReal rhs) {
		return new ValueBool(lhs.value() > rhs.real());
	}

	default Value apply(OperatorGreaterThan op, ValueReal lhs, ValueLong rhs) {
		return new ValueBool(lhs.real() > rhs.value());
	}

	default Value apply(OperatorGreaterThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() > rhs.character());
	}

	default Value apply(OperatorGreaterThan op, ValueSet lhs, ValueSet rhs) {
		return new ValueBool(rhs.elements().stream().allMatch(elem -> lhs.elements().contains(elem)) && rhs.elements().size() < lhs.elements().size());
	}

	default Value apply(OperatorGreaterThan op, ValueString lhs, ValueString rhs) {
		return new ValueBool(lhs.string().compareTo(rhs.string()) > 0);
	}

	default Value apply(OperatorGreaterEqualThan op, ValueLong lhs, ValueLong rhs) {
		return new ValueBool(lhs.value() >= rhs.value());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueReal lhs, ValueReal rhs) {
		return new ValueBool(lhs.real() >= rhs.real());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueLong lhs, ValueReal rhs) {
		return new ValueBool(lhs.value() >= rhs.real());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueReal lhs, ValueLong rhs) {
		return new ValueBool(lhs.real() >= rhs.value());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueChar lhs, ValueChar rhs) {
		return new ValueBool(lhs.character() >= rhs.character());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueSet lhs, ValueSet rhs) {
		return new ValueBool(rhs.elements().stream().allMatch(elem -> lhs.elements().contains(elem)) && rhs.elements().size() <= lhs.elements().size());
	}

	default Value apply(OperatorGreaterEqualThan op, ValueString lhs, ValueString rhs) {
		return new ValueBool(lhs.string().compareTo(rhs.string()) >= 0);
	}

	default Value apply(OperatorPlus op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() + rhs.value());
	}

	default Value apply(OperatorPlus op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() + rhs.real());
	}

	default Value apply(OperatorPlus op, ValueLong lhs, ValueReal rhs) {
		return new ValueReal(lhs.value() + rhs.real());
	}

	default Value apply(OperatorPlus op, ValueReal lhs, ValueLong rhs) {
		return new ValueReal(lhs.real() + rhs.value());
	}

	default Value apply(OperatorPlus op, ValueSet lhs, ValueSet rhs) {
		Set<Value> elements = new HashSet<>(lhs.elements()); elements.addAll(rhs.elements());
		return new ValueSet(elements);
	}

	default Value apply(OperatorPlus op, ValueString lhs, ValueString rhs) {
		return new ValueString(lhs.string() + rhs.string());
	}

	default Value apply(OperatorPlus op, ValueString lhs, ValueLong rhs) {
		return new ValueString(lhs.string() + rhs.value());
	}

	default Value apply(OperatorPlus op, ValueLong lhs, ValueString rhs) {
		return new ValueString(lhs.value() + rhs.string());
	}

	default Value apply(OperatorPlus op, ValueString lhs, ValueReal rhs) {
		return new ValueString(lhs.string() + rhs.real());
	}

	default Value apply(OperatorPlus op, ValueReal lhs, ValueString rhs) {
		return new ValueString(lhs.real() + rhs.string());
	}

	default Value apply(OperatorPlus op, ValueString lhs, ValueBool rhs) {
		return new ValueString(lhs.string() + rhs.bool());
	}

	default Value apply(OperatorPlus op, ValueBool lhs, ValueString rhs) {
		return new ValueString(lhs.bool() + rhs.string());
	}

	default Value apply(OperatorPlus op, ValueString lhs, ValueChar rhs) {
		return new ValueString(lhs.string() + rhs.character());
	}

	default Value apply(OperatorPlus op, ValueChar lhs, ValueString rhs) {
		return new ValueString(lhs.character() + rhs.string());
	}

	default Value apply(OperatorPlus op, ValueMap lhs, ValueMap rhs) {
		Map<Value, List<Value>> union = new HashMap<>();
		for (ImmutableEntry<Value, Value> mapping : lhs.mappings()) {
			List<Value> value = new ArrayList<>(); value.add(mapping.getValue());
			union.put(mapping.getKey(), value);
		}
		for (ImmutableEntry<Value, Value> mapping : rhs.mappings()) {
			if (union.containsKey(mapping.getValue())) {
				union.get(mapping.getValue()).add(mapping.getKey());
			} else {
				List<Value> value = new ArrayList<>(); value.add(mapping.getValue());
				union.put(mapping.getKey(), value);
			}
		}
		List<ImmutableEntry<Value, Value>> mappings = new ArrayList<>();
		for (Map.Entry<Value, List<Value>> entry : union.entrySet()) {
			mappings.add(ImmutableEntry.of(entry.getKey(), new ValueList(entry.getValue())));
		}
		return new ValueMap(ImmutableList.from(mappings));
	}

	default Value apply(OperatorMinus op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() - rhs.value());
	}

	default Value apply(OperatorMinus op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() - rhs.real());
	}

	default Value apply(OperatorMinus op, ValueLong lhs, ValueReal rhs) {
		return new ValueReal(lhs.value() - rhs.real());
	}

	default Value apply(OperatorMinus op, ValueReal lhs, ValueLong rhs) {
		return new ValueReal(lhs.real() - rhs.value());
	}

	default Value apply(OperatorMinus op, ValueSet lhs, ValueSet rhs) {
		return new ValueSet(lhs.elements().stream().filter(elem -> !(rhs.elements().contains(elem))).collect(Collectors.toSet()));
	}

	default Value apply(OperatorTimes op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() * rhs.value());
	}

	default Value apply(OperatorTimes op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() * rhs.real());
	}

	default Value apply(OperatorTimes op, ValueLong lhs, ValueReal rhs) {
		return new ValueReal(lhs.value() * rhs.real());
	}

	default Value apply(OperatorTimes op, ValueReal lhs, ValueLong rhs) {
		return new ValueReal(lhs.real() * rhs.value());
	}

	default Value apply(OperatorTimes op, ValueSet lhs, ValueSet rhs) {
		return new ValueSet(lhs.elements().stream().filter(elem -> rhs.elements().contains(elem)).collect(Collectors.toSet()));
	}

	default Value apply(OperatorDiv op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() / rhs.value());
	}

	default Value apply(OperatorDiv op, ValueReal lhs, ValueReal rhs) {
		return new ValueReal(lhs.real() / rhs.real());
	}

	default Value apply(OperatorDiv op, ValueLong lhs, ValueReal rhs) {
		return new ValueReal(lhs.value() / rhs.real());
	}

	default Value apply(OperatorDiv op, ValueReal lhs, ValueLong rhs) {
		return new ValueReal(lhs.real() / rhs.value());
	}

	default Value apply(OperatorMod op, ValueLong lhs, ValueLong rhs) {
		return new ValueLong(lhs.value() % rhs.value());
	}

	default Value apply(OperatorEqual op, ValueLong lhs, ValueLong rhs) {
		return new ValueBool(lhs.value() == rhs.value());
	}

	default Value apply(OperatorEqual op, Value lhs, Value rhs) {
		return new ValueBool(lhs.equals(rhs));
	}

	default Value apply(OperatorDifferent op, Value lhs, Value rhs) {
		return new ValueBool(!(lhs.equals(rhs)));
	}

	default Value apply(OperatorIn op, Value lhs, ValueList rhs) {
		return new ValueBool(rhs.elements().stream().anyMatch(element -> element.equals(lhs)));
	}

	default Value apply(OperatorIn op, Value lhs, ValueSet rhs) {
		return new ValueBool(rhs.elements().stream().anyMatch(element -> element.equals(lhs)));
	}

	default Value apply(OperatorIn op, Value lhs, ValueMap rhs) {
		return new ValueBool(rhs.mappings().stream().anyMatch(element -> element.getKey().equals(lhs)));
	}

	default Value apply(OperatorIn op, ValueChar lhs, ValueString rhs) {
		return new ValueBool(rhs.string().indexOf(lhs.character()) > -1);
	}

	default Value apply(OperatorFromTo op, ValueLong lhs, ValueLong rhs){
		List<Value> values = new ArrayList<>();
		for(long i = lhs.value(); i <= rhs.value(); i++){
			values.add(new ValueLong(i));
		}
		return new ValueList(values);
	}
}
